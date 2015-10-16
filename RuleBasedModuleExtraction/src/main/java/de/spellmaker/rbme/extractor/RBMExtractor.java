package de.spellmaker.rbme.extractor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import de.spellmaker.rbme.rule.Rule;
import de.spellmaker.rbme.rule.RuleSet;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class RBMExtractor {
	public static Set<OWLAxiom> extractModule(RuleSet ruleSet, OWLOntology ontology, Set<OWLClass> signature){
		Set<Rule> rules = ruleSet.getRules();				//set of created rules
		
		//initialize the processing queue to the signature
		ProcessingQueue procQueue = new ProcessingQueue(signature);

		//add signature, but remove owl:thing from it
		//OWLEntity owlThing = (new OWLDataFactoryImpl()).getOWLThing();
		//signature.stream().filter(x -> !x.equals(owlThing)).forEach(x -> procQueue.add(x));
		
		//index all rules by their body elements
		Map<Object, List<Rule>> ruleMap = new HashMap<>();
		for(Rule rule : rules){
			//process bodyless rules
			if(rule.isFinished()){
				Object o = rule.getHead();
				if(o instanceof OWLClassAssertionAxiom){
					OWLClassAssertionAxiom ax = (OWLClassAssertionAxiom) o;
					ax.getClassExpression().getSignature().forEach(x -> procQueue.add(x));
					procQueue.addToModule(ax);
				}
				else if(o instanceof OWLObjectPropertyAssertionAxiom){
					OWLObjectPropertyAssertionAxiom ax = (OWLObjectPropertyAssertionAxiom) o;
					OWLObjectPropertyExpression prop = ax.getProperty();
					prop.getSignature().forEach(x -> procQueue.add(x));			
					procQueue.addToModule(ax);
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
		while(!procQueue.isEmpty()){
			Object front = procQueue.poll();
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
						axiom.getSignature().forEach(x -> procQueue.add(x));
						procQueue.addToModule(axiom);
					}
					else{
						//in case of an intermediate rule, add the head
						procQueue.add(cRule.getHead());
					}
				}
			}
		}
		return procQueue.getModule();
	}
}