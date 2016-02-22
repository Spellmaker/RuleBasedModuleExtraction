package de.spellmaker.rbme.evaluation.workers;

import java.io.File;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.rule.BottomModeRuleBuilder;
import de.spellmaker.rbme.rule.CompressedRuleBuilder;
import de.spellmaker.rbme.rule.CompressedRuleSet;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import objectexplorer.MemoryMeasurer;

public class RuleSizeWorker implements Callable<Long[]>{
	private File file;
	
	public RuleSizeWorker(File file){
		this.file = file;
	}
	
	
	@Override
	public Long[] call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
		
		RuleSet r1 = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		RuleSet r2 = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		CompressedRuleSet r3 = (new CompressedRuleBuilder()).buildRules(ontology.getAxioms());
		
		Long[] res = new Long[8];
		res[0] = (long) ontology.getAxioms().size();
		res[1] = MemoryMeasurer.measureBytes(ontology);
		res[2] = (long) r1.size();
		res[3] = MemoryMeasurer.measureBytes(r1);
		res[4] = (long) r2.size();
		res[5] = MemoryMeasurer.measureBytes(r2);
		res[6] = (long) r3.size();
		res[7] = MemoryMeasurer.measureBytes(r3);
		return res;
	}

}
