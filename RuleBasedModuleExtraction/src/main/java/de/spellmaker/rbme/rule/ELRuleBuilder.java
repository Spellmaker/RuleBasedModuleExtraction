package de.spellmaker.rbme.rule;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

public class ELRuleBuilder implements RuleBuilder{
	
	@Override
	public RuleSet buildRules(Set<OWLAxiom> axioms){
		Set<Rule> rules = new HashSet<>();
		axioms.forEach(x -> rules.addAll(createRule(x)));
		return new RuleSet(rules);
	}
	
	private Set<Rule> createRule(OWLAxiom axiom){
		Set<Rule> result = new HashSet<>();
		if(axiom instanceof OWLEquivalentClassesAxiom){
			OWLEquivalentClassesAxiom a = (OWLEquivalentClassesAxiom) axiom;
			Set<OWLClassExpression> expr = a.getClassExpressions();
			
			for(OWLClassExpression e : expr){
				result.add(new Rule(axiom, e));
				result.addAll(createRule(e));
			}
		}
		else if(axiom instanceof OWLSubClassOfAxiom){
			OWLSubClassOfAxiom a = (OWLSubClassOfAxiom) axiom;
			OWLClassExpression expr = a.getSubClass();
			result.add(new Rule(axiom, expr));
			result.addAll(createRule(expr));
		}
		else if(axiom instanceof OWLClassAssertionAxiom){
			result.add(new Rule(axiom));
		}
		else if(axiom instanceof OWLObjectPropertyAssertionAxiom){
			result.add(new Rule(axiom));
		}
		else if(axiom instanceof OWLDeclarationAxiom){
			//skip
		}
		else if(axiom instanceof OWLSubPropertyChainOfAxiom){
			//TODO: implement
			//System.out.println("WARNING: Unprocessed Axiom '" + axiom + "'");
		}
		else if(axiom instanceof OWLSubObjectPropertyOfAxiom){
			//TODO: implement
			//System.out.println("WARNING: Unprocessed Axiom '" + axiom + "'");
		}
		else{
			throw new IllegalArgumentException("unknown axiomtype " + axiom.getClass());
		}
		return result;
	}
	
	private Set<Rule> createRule(OWLClassExpression expr){
		Set<Rule> result = new HashSet<>();
		if(expr instanceof OWLClass){
			//nothing to do
		}
		else if(expr instanceof OWLObjectIntersectionOf){
			OWLObjectIntersectionOf intersect = (OWLObjectIntersectionOf) expr;
			Set<OWLClassExpression> ops = intersect.getOperands();
			result.add(new Rule(expr, ops.toArray()));
			for(OWLClassExpression e : ops){
				result.addAll(createRule(e));
			}
		}
		else if(expr instanceof OWLObjectSomeValuesFrom){
			OWLObjectSomeValuesFrom exist = (OWLObjectSomeValuesFrom) expr;
			result.add(new Rule(expr, exist.getFiller(), exist.getProperty()));
			result.addAll(createRule(exist.getFiller()));
		}
		
		return result;
	}
}
