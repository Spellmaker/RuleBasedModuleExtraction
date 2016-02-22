import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.RuleSet;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ModuleBuilder {
	public static List<Set<Integer>> definedAxioms = null;
	public static List<Set<Integer>> signatures = null;
	
	public static List<Set<Integer>> getModules(OWLOntology ontology, RuleSet ruleSet, boolean doDefinitions){
		Set<OWLEntity> sign = ontology.getSignature();//.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet());
		definedAxioms = new ArrayList<>();
		List<Set<Integer>> moduleList = new ArrayList<>();
		List<Set<Integer>> tmpSignatures = new ArrayList<>();
		List<Set<Integer>> tmpDefinedAxioms = new ArrayList<>();
		Set<OWLEntity> takenCare = new HashSet<>();

		int c = 0;
		for(OWLEntity e : sign){
			System.out.println("(" + c + "/" + sign.size() + ")");
			c++;
			if(takenCare.contains(e)) continue;
			if(!(e instanceof OWLClass) &&!(e instanceof OWLObjectProperty)) continue;
			
			Set<OWLEntity> currentSignature = new HashSet<>();
			currentSignature.add(e);
			Set<Integer> currentSignatureInt = new HashSet<>();
			currentSignatureInt.add(ruleSet.putObject(e));
			
			RBMExtractor rbme = new RBMExtractor(doDefinitions, false);
			Set<OWLAxiom> currentModule = new HashSet<>(rbme.extractModule(ruleSet, currentSignature));
			Set<Integer> currentModuleInt = currentModule.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet());
			
			//build defined axiom list
			Set<Integer> defined = new HashSet<>(rbme.getDefinedAxioms());//.stream().map(x -> (OWLAxiom) ruleSet.lookup(x)).collect(Collectors.toSet()); 
			
			boolean append = true;
			for(int i = 0; i < moduleList.size(); i++){
				Set<Integer> otherModule = moduleList.get(i);
				if(otherModule == null) continue;
				
				if(otherModule.containsAll(currentModuleInt)){
					append = false;
					break;
				}
				if(currentModuleInt.containsAll(otherModule)){
					moduleList.set(i, null);
					tmpDefinedAxioms.set(i, null);
					tmpSignatures.set(i, null);
				}
			}
			
			if(append){
				currentModule.forEach(x -> takenCare.addAll(x.getSignature()));
				moduleList.add(currentModuleInt);
				tmpDefinedAxioms.add(defined);
				tmpSignatures.add(currentSignatureInt);
			}
		}
		
		signatures = new ArrayList<>();
		definedAxioms = new ArrayList<>();
		
		//compact result list
		List<Set<Integer>> result = new ArrayList<>();
		for(int i = 0; i < moduleList.size(); i++){
			if(moduleList.get(i) == null) continue;
			result.add(moduleList.get(i));//.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet()));
			signatures.add(tmpSignatures.get(i));//.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet()));
			definedAxioms.add(tmpDefinedAxioms.get(i));//.get(i).stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet()));
		}	

		return result;
	}
	
	public static List<Set<OWLAxiom>> getModules(OWLOntology ontology, SyntacticLocalityModuleExtractor extractor){

		return null;
	}
}