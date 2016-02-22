package de.spellmaker.rbme.mains.workers;

import java.io.File;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.rule.BottomModeRuleBuilder;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;

public class RuleSizeWorker implements Callable<Integer[]>{
	private File file;
	
	public RuleSizeWorker(File file){
		this.file = file;
	}
	
	
	@Override
	public Integer[] call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
		
		RuleSet r1 = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		RuleSet r2 = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		
		Integer[] res = new Integer[3];
		res[0] = ontology.getAxioms().size();
		res[1] = r1.size();
		res[2] = r2.size();
		return res;
	}

}
