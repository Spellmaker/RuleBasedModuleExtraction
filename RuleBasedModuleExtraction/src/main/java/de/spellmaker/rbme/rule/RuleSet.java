package de.spellmaker.rbme.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

public class RuleSet extends OWLObjectVisitorAdapter implements Iterable<Rule>{
	private Set<OWLObject> baseSignature;
	private Set<OWLAxiom> baseModule;
	private Map<OWLObject, List<Integer>> ruleMap;	
	private Set<Rule> rules;
	private int pos;
	
	private int size = -1;
	
	public RuleSet(){
		this.ruleMap = new HashMap<>();
		this.rules = new LinkedHashSet<>();
		this.baseSignature = new LinkedHashSet<>();
		this.baseModule = new LinkedHashSet<>();
		this.pos = 0;
	}
	
	public void finalize(){
		size = rules.size();
		baseSignature = Collections.unmodifiableSet(baseSignature);
		baseModule = Collections.unmodifiableSet(baseModule);
		ruleMap = Collections.unmodifiableMap(ruleMap);
		rules = Collections.unmodifiableSet(rules);
	}
	
	public Rule getRule(int i){
		Iterator<Rule> it = rules.iterator();
		Rule c = it.next();
		for(int j = 0; j < i; j++){
			c = it.next();
		}
		return c;
	}
	
	public void add(Rule r){
		if(r.size() > 0){
			if(this.rules.add(r)){
				for(OWLObject o : r){
					List<Integer> current = ruleMap.get(o);
					if(current == null) current = new LinkedList<>();
					current.add(pos);
					ruleMap.put(o, current);
				}
				pos++;
			}
		}
		else if(r.getHead() != null){
			r.getHead().accept(this);
		}
	}
	
	@Override
	public void visit(OWLClassAssertionAxiom ax){
		baseSignature.addAll(ax.getClassExpression().getSignature());
		baseModule.add(ax);
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax){
		OWLObjectPropertyExpression prop = ax.getProperty();
		baseSignature.addAll(prop.getSignature());
		baseModule.add(ax);
	}
	
	public Set<OWLAxiom> getBaseModule(){
		return baseModule;
	}
	
	public Set<OWLObject> getBaseSignature(){
		return baseSignature;
	}
	
	public int size(){
		return size;
	}
	
	public List<Integer> findRules(OWLObject o){
		return ruleMap.get(o);
	}

	@Override
	public Iterator<Rule> iterator() {
		return rules.iterator();
	}
}
