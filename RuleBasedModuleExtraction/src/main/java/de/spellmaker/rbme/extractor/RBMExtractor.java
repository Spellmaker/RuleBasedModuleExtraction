package de.spellmaker.rbme.extractor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
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
	private Set<Object> knownNotBottom;
	private Queue<Object> queue;
	private OWLEntity owlThing;
	
	
	
	/**
	 * Uses the rules provided by the rule set to extract a module using the given signature
	 * @param ruleSet A set of rules constructed by a RuleBuilder 
	 * @param signature A set of OWL classes forming a signature
	 * @return A set of OWL axioms forming a module for the signature
	 */
	public Set<OWLAxiom> extractModule(RuleSet ruleSet, Set<OWLClass> signature){
		Set<Rule> rules = ruleSet.getRules();				//set of created rules
		
		//initialize the processing queue to the signature
		//ProcessingQueue procQueue = new ProcessingQueue(signature);
		OWLDataFactory factory = new OWLDataFactoryImpl();
		owlThing = factory.getOWLThing();
		module = new HashSet<>();
		knownNotBottom = new HashSet<>(signature);
		signature.stream().filter(x -> !x.equals(owlThing)).forEach(x -> module.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		queue = new LinkedList<>(signature);
		
		//OWL Thing is always assumed to be not bottom
		queue.add(owlThing);
		knownNotBottom.add(owlThing);
		
		
		
		
		//index all rules by their body elements
		Map<Object, List<Rule>> ruleMap = new HashMap<>();
		for(Rule rule : rules){
			//process bodyless rules
			if(rule.isFinished()){
				Object o = rule.getHead();
				if(o instanceof OWLClassAssertionAxiom){
					OWLClassAssertionAxiom ax = (OWLClassAssertionAxiom) o;
					ax.getClassExpression().getSignature().forEach(x -> addQueue(x));
					module.add(ax);
					//procQueue.addToModule(ax);
				}
				else if(o instanceof OWLObjectPropertyAssertionAxiom){
					OWLObjectPropertyAssertionAxiom ax = (OWLObjectPropertyAssertionAxiom) o;
					OWLObjectPropertyExpression prop = ax.getProperty();
					prop.getSignature().forEach(x -> addQueue(x));			
					module.add(ax);
				}
				
				continue;
			}
			
			for(Object o : rule.getBody()){
				List<Rule> current = ruleMap.get(o);
				if(current == null) current = new LinkedList<>();
				current.add(rule);
				ruleMap.put(o, current);
			}
		}
		//main processing loop
		for(Object front = queue.poll(); front != null; front = queue.poll()){
			//process all rules, which have the front element in their body
			List<Rule> matchRules = ruleMap.get(front);
			if(matchRules == null) continue;
			
			//element will never enter the queue again, therefore the memory of the list can be freed
			ruleMap.remove(front);
			
			for(Rule cRule : matchRules){
				//check for rule completion, that is, if all body elements 
				//have been found to be possibly not bottom
				if(cRule.offer(front)){
					Object head = cRule.getHead();
					if(head instanceof OWLAxiom){
						//in case the head is an axiom, add all new vocabulary from the axiom
						//into the processing queue
						OWLAxiom axiom = (OWLAxiom) head;
						axiom.getSignature().forEach(x -> addQueue(x));
						module.add(axiom);
					}
					else{
						//in case of an intermediate rule, add the head
						addQueue(cRule.getHead());
					}
				}
			}
		}
		return module;
	}
	
	private boolean addQueue(Object o){
		//add the entity to the list of those known to be possibly not bottom
		if(knownNotBottom.add(o)){
			//only if it is actually new knowledge process further
			if(o instanceof OWLClass || o instanceof OWLObjectProperty){
				//add declarations for classes and properties to the module
				module.add(new OWLDeclarationAxiomImpl((OWLEntity)o, Collections.emptyList()));
			}
			//add the entity to the processing queue
			return queue.add(o);
		}
		return false;
	}
}
