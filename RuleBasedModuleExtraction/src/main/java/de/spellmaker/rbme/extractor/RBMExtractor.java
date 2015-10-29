package de.spellmaker.rbme.extractor;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import de.spellmaker.rbme.rule.Rule;
import de.spellmaker.rbme.rule.RuleSet;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

/**
 * Extracts a local module from an ontology
 * @author spellmaker
 *
 */
public class RBMExtractor {
	private Set<Integer> module;
	private Set<Integer> knownNotBottom;
	private Queue<Integer> queue;
	private int owlThing;
	
	/**
	 * Uses the rules provided by the rule set to extract a module using the given signature
	 * @param ruleSet A set of rules constructed by a RuleBuilder 
	 * @param signature A set of OWL classes forming a signature
	 * @return A set of OWL axioms forming a module for the signature
	 */
	public List<OWLAxiom> extractModule(RuleSet rules, Set<OWLClass> signature){	
		//initialize the processing queue to the signature
		module = new HashSet<>();
		knownNotBottom = new HashSet<>();
		signature.forEach(x -> knownNotBottom.add(rules.addObject(x)));
		
		//Note: this filter can be dropped, if we assume that signatures do not contain owl:thing
		signature = signature.stream().filter(x -> !x.isOWLThing()).collect(Collectors.toSet());
		
		//signature.forEach(x -> module.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		queue = new LinkedList<>();
		for(OWLObject o : signature){
			queue.add(rules.addObject(o));
			module.add(rules.addObject(new OWLDeclarationAxiomImpl((OWLEntity) o, Collections.emptyList())));
		}
		
		
		//OWL Thing is always assumed to be not bottom
		OWLDataFactory factory = new OWLDataFactoryImpl();
		owlThing = rules.addObject(factory.getOWLThing());
		queue.add(owlThing);
		knownNotBottom.add(owlThing);

		int[] ruleCounter = new int[rules.size()];
		int[] ruleHeads = new int[rules.size()]; 
		int[] ruleAxioms = new int[rules.size()];
		@SuppressWarnings("unchecked")
		List<Integer>[] axiomSignatures = new List[rules.size()];
		
		//add base module and signature
		module.addAll(rules.getBaseModule());
		rules.getBaseSignature().forEach(x -> addQueue(x));
		
		int pos = 0;
		for(Rule rule : rules){
			ruleCounter[pos] = rule.size();
			ruleHeads[pos] = rule.getHead();
			ruleAxioms[pos] = rule.getAxiom();
			axiomSignatures[pos] = rule.getAxiomSignature();
			pos++;
		}
		
		//main processing loop
		for(Integer front = queue.poll(); front != null; front = queue.poll()){
			System.out.println("loop");
			//process all rules, which have the front element in their body
			List<Integer> matchRules = rules.findRules(front);
			if(matchRules == null) continue;
			
			for(Integer cRule : matchRules){
				if(ruleCounter[cRule] <= 0) continue;
				//check for rule completion, that is, if all body elements 
				//have been found to be possibly not bottom
				if(--ruleCounter[cRule] <= 0){
					int head = ruleHeads[cRule];
					
					if(head == -1){
						//in case the head is an axiom, add all new vocabulary from the axiom
						//into the processing queue
						axiomSignatures[cRule].forEach(x -> addQueue(x));
						module.add(ruleAxioms[cRule]);
					}
					else{
						//in case of an intermediate rule, add the head
						addQueue(head);
					}
				}
			}
		}
		
		List<OWLAxiom> result = new LinkedList<>();
		module.forEach(x -> result.add((OWLAxiom) rules.getObject(x)));
		return result;
	}
	
	private boolean addQueue(int o){
		//add the entity to the list of those known to be possibly not bottom
		if(knownNotBottom.add(o)){
			//add the entity to the processing queue
			return queue.add(o);
		}
		return false;
	}
}
