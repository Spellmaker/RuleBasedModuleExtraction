package de.spellmaker.rbme.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

public class WorkerThread implements Callable<OntologieData> {
	//configuration data
	private int iterations;
	private File ontologyFile;
	private boolean doChecks;
	private int maxsize;
	private int minsize;
	//result data
	private OntologieData ontoData;
	
	public WorkerThread(int iterations, File ontologyFile, boolean doChecks, int minsize, int maxsize){
		this.iterations = iterations;
		this.ontologyFile = ontologyFile;
		this.doChecks = doChecks;
		this.maxsize = maxsize;
		this.minsize = minsize;
	}
	
	private void printInfo(String msg){
		System.out.println("[INFO](" + ontologyFile + ") " + msg);
	}

	@Override
	public OntologieData call() throws Exception {
		printInfo("started");
		
		long startTime = 0;
		long endTime = 0;
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		ModuleCheck mCheck = null;
		if(doChecks) mCheck = new ModuleCheck(m);
		ontoData = new OntologieData();
		printInfo("loading ontology");
		startTime = System.currentTimeMillis();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(ontologyFile);
		endTime = System.currentTimeMillis();
		printInfo("ontology size is " + ontology.getAxiomCount());
		
		if((ontology.getAxiomCount() > maxsize && maxsize > 0) || ontology.getAxiomCount() < minsize){
			printInfo("skipping due to size restrictions");
			return null;
		}
		ontoData.iri = ontology.getOntologyID().toString();
		ontoData.file = ontologyFile.toString();
		ontoData.loadTime = endTime - startTime;
		ontoData.axiomCount = ontology.getAxiomCount();
		
		ontoData.passedCorrectnessOWLAPI = true;
		ontoData.passedCorrectnessRBME = true;
		ontoData.passedSize = true;
		
		List<OWLClass> ontologySignature = new ArrayList<>();
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
		
		startTime = System.currentTimeMillis();
		RuleSet ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		endTime = System.currentTimeMillis();
		
		ontoData.ruleGenTime = endTime - startTime;
		
		startTime = System.currentTimeMillis();
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		endTime = System.currentTimeMillis();
		
		ontoData.owlapi_instTime = endTime - startTime;
		if(doChecks){
			printInfo("performing correctness and size tests");
			for(int i = 0; i < ontologySignature.size(); i++){
				OWLClass element = ontologySignature.get(i);
				Set<OWLClass> sign = new HashSet<>();
				sign.add(element);
				Set<OWLAxiom> moduleOWLAPI = extractor.extract(Collections.unmodifiableSet(sign));
				Set<OWLAxiom> moduleRBME = (new RBMExtractor()).extractModule(ruleSet, sign);
				
				//check general correctness
				if(mCheck.isSemanticalLocalModule(ontology, moduleOWLAPI) != null){
					ontoData.passedCorrectnessOWLAPI = false;
				}
				if(mCheck.isSyntacticalLocalModule(ontology, moduleOWLAPI) != null){
					ontoData.passedCorrectnessOWLAPI = false;
				}
				if(mCheck.isSemanticalLocalModule(ontology, moduleRBME) != null){
					ontoData.passedCorrectnessRBME = false;
				}
				if(mCheck.isSyntacticalLocalModule(ontology, moduleRBME) != null){
					ontoData.passedCorrectnessRBME = false;
				}
				
				//check size
				if(moduleOWLAPI.size() < moduleRBME.size()){
					ontoData.passedSize = false;
				}
			}
		}
		else{
			printInfo("skipping tests");
		}

		ontoData.iterations = iterations;
		printInfo("perfoming " + ontoData.iterations + " iterations");
		printInfo("processing owlapi");
		startTime = System.currentTimeMillis();
		for(int i = 0; i < ontoData.iterations; i++){
			for(int j = 0; j < ontologySignature.size(); j++){
				OWLClass element = ontologySignature.get(j);
				Set<OWLEntity> sign = new HashSet<>();
				sign.add(element);
				extractor.extract(sign);
			}
		}
		endTime = System.currentTimeMillis();
		ontoData.owlapi_result = endTime - startTime;
		
		printInfo("processing rbme");
		startTime = System.currentTimeMillis();
		for(int i = 0; i < ontoData.iterations; i++){
			for(int j = 0; j < ontologySignature.size(); j++){
				OWLClass element = ontologySignature.get(j);
				Set<OWLClass> sign = new HashSet<>();
				sign.add(element);
				(new RBMExtractor()).extractModule(ruleSet, sign);
			}
		}
		endTime = System.currentTimeMillis();
		ontoData.rbme_result = endTime - startTime;
		
		printInfo("terminated");
		
		return ontoData;
	}
}
