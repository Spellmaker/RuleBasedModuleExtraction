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
	private Set<OWLAxiom> module;
	private Set<OWLObject> knownNotBottom;
	private Queue<OWLObject> queue;
	private OWLEntity owlThing;
	
	/**
	 * Uses the rules provided by the rule set to extract a module using the given signature
	 * @param ruleSet A set of rules constructed by a RuleBuilder 
	 * @param signature A set of OWL classes forming a signature
	 * @return A set of OWL axioms forming a module for the signature
	 */
	public Set<OWLAxiom> extractModule(RuleSet rules, Set<OWLClass> signature){	
		//initialize the processing queue to the signature
		module = new HashSet<>();
		knownNotBottom = new HashSet<>(signature);
		
		//Note: this filter can be dropped, if we assume that signatures do not contain owl:thing
		signature = signature.stream().filter(x -> !x.isOWLThing()).collect(Collectors.toSet());
		
		signature.forEach(x -> module.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		queue = new LinkedList<>(signature);
		
		//OWL Thing is always assumed to be not bottom
		OWLDataFactory factory = new OWLDataFactoryImpl();
		owlThing = factory.getOWLThing();
		queue.add(owlThing);
		knownNotBottom.add(owlThing);

		int[] ruleCounter = new int[rules.size()];
		OWLObject[] ruleHeads = new OWLObject[rules.size()]; 
		OWLAxiom[] ruleAxioms = new OWLAxiom[rules.size()];
		
		//add base module and signature
		module.addAll(rules.getBaseModule());
		rules.getBaseSignature().forEach(x -> addQueue(x));
		
		int pos = 0;
		for(Rule rule : rules){
			ruleCounter[pos] = rule.size();
			ruleHeads[pos] = rule.getHead();
			ruleAxioms[pos] = rule.getAxiom();
			pos++;
		}
		
		//main processing loop
		for(OWLObject front = queue.poll(); front != null; front = queue.poll()){
			//process all rules, which have the front element in their body
			List<Integer> matchRules = rules.findRules(front);
			if(matchRules == null) continue;
			
			for(Integer cRule : matchRules){
				if(ruleCounter[cRule] <= 0) continue;
				//check for rule completion, that is, if all body elements 
				//have been found to be possibly not bottom
				if(--ruleCounter[cRule] <= 0){
					OWLObject head = ruleHeads[cRule];
					
					if(head == null){
						//in case the head is an axiom, add all new vocabulary from the axiom
						//into the processing queue
						ruleAxioms[cRule].getSignature().forEach(x -> addQueue(x));
						module.add(ruleAxioms[cRule]);
					}
					else{
						if(head instanceof OWLAxiom){
							System.out.println("this is awkward...");
						}
						//in case of an intermediate rule, add the head
						addQueue(head);
					}
				}
			}
		}
		return module;
	}
	
	private boolean addQueue(OWLObject o){
		//add the entity to the list of those known to be possibly not bottom
		if(knownNotBottom.add(o)){
			//add the entity to the processing queue
			return queue.add(o);
		}
		return false;
	}
}
