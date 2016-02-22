package de.spellmaker.rbme.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.util.ModuleCheck;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class WorkerThread implements Callable<Map<String, String>> {
	//configuration data
	private int iterations;
	private int rule_iterations;
	private File ontologyFile;
	private boolean doChecks;
	//result data
	private Map<String, String> ontoData;
	
	public WorkerThread(int rule_iterations, int iterations, File ontologyFile, boolean doChecks){
		this.iterations = iterations;
		this.ontologyFile = ontologyFile;
		this.doChecks = doChecks;
		this.rule_iterations = (rule_iterations >= 1) ? rule_iterations : 1;
	}
	
	private void printInfo(String msg){
		System.out.println("[INFO](" + ontologyFile + ") " + msg);
	}

	@Override
	public Map<String, String> call() throws Exception {
		printInfo("started");
		
		long startTime = 0;
		long endTime = 0;
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		ModuleCheck mCheck = null;
		if(doChecks) mCheck = new ModuleCheck(m);
		ontoData = new HashMap<>();
		printInfo("loading ontology");
		startTime = System.currentTimeMillis();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(ontologyFile);
		endTime = System.currentTimeMillis();
		printInfo("ontology size is " + ontology.getAxiomCount());
		
		ontoData.put("IRI", ontology.getOntologyID().toString());
		ontoData.put("file", ontologyFile.getName().toString());
		ontoData.put("loadTime", "" + (endTime - startTime));
		ontoData.put("axiomCount", "" + ontology.getAxiomCount());
		boolean corrOWLAPI = true;
		boolean corrRBME = true;
		boolean passSize = true;
		
		List<OWLClass> ontologySignature = new ArrayList<>();
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
		
		startTime = System.currentTimeMillis();
		RuleSet ruleSet = null;
		for(int i = 0; i < rule_iterations; i++){
			ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		}
		endTime = System.currentTimeMillis();
		
		ontoData.put("rule_iterations", "" + rule_iterations);
		ontoData.put("ruleGenTime" , "" + (endTime - startTime));
		
		startTime = System.currentTimeMillis();
		SyntacticLocalityModuleExtractor extractor = null;
		for(int i = 0; i < rule_iterations; i++){
			extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		}
		endTime = System.currentTimeMillis();
		
		ontoData.put("owlapi_instTime", "" + (endTime - startTime));
		if(doChecks){
			printInfo("performing correctness and size tests");
			for(int i = 0; i < ontologySignature.size(); i++){
				OWLClass element = ontologySignature.get(i);
				Set<OWLEntity> sign = new HashSet<>();
				sign.add(element);
				Set<OWLAxiom> moduleOWLAPI = extractor.extract(Collections.unmodifiableSet(sign));
				Set<OWLAxiom> moduleRBME = (new RBMExtractor(false, false)).extractModule(ruleSet, sign);
				
				//check general correctness
				if(mCheck.isSemanticalLocalModule(ontology, moduleOWLAPI) != null){
					corrOWLAPI = false;
				}
				if(mCheck.isSyntacticalLocalModule(ontology, moduleOWLAPI) != null){
					corrOWLAPI = false;
				}
				if(mCheck.isSemanticalLocalModule(ontology, moduleRBME) != null){
					corrRBME = false;
				}
				if(mCheck.isSyntacticalLocalModule(ontology, moduleRBME) != null){
					corrRBME = false;
				}
				
				//check size
				if(moduleOWLAPI.size() < moduleRBME.size()){
					passSize = false;
				}
			}
		}
		else{
			printInfo("skipping tests");
		}
		
		ontoData.put("passedCorrectnessOWLAPI", "" + corrOWLAPI);
		ontoData.put("passedCorrectnessRBME", "" + corrRBME);
		ontoData.put("passedSize",  "" + passSize);

		ontoData.put("iterations", "" + iterations);
		printInfo("perfoming " + iterations + " iterations");
		printInfo("processing owlapi");
		startTime = System.currentTimeMillis();
		for(int i = 0; i < iterations; i++){
			for(int j = 0; j < ontologySignature.size(); j++){
				OWLClass element = ontologySignature.get(j);
				Set<OWLEntity> sign = new HashSet<>();
				sign.add(element);
				extractor.extract(sign);
			}
		}
		endTime = System.currentTimeMillis();
		ontoData.put("owlapi_result", "" + (endTime - startTime));
		
		printInfo("processing rbme");
		startTime = System.currentTimeMillis();
		for(int i = 0; i < iterations; i++){
			for(int j = 0; j < ontologySignature.size(); j++){
				OWLClass element = ontologySignature.get(j);
				Set<OWLEntity> sign = new HashSet<>();
				sign.add(element);
				(new RBMExtractor(false, false)).extractModule(ruleSet, sign);
			}
		}
		endTime = System.currentTimeMillis();
		ontoData.put("rbme_result", "" + (endTime - startTime));
		
		printInfo("terminated");
		
		return ontoData;
	}
}
