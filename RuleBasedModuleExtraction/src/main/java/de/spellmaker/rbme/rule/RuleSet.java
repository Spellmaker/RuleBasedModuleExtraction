package de.spellmaker.rbme.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

public class RuleSet extends OWLObjectVisitorAdapter implements Iterable<Rule>{
	private Set<Integer> baseSignature;
	private Set<Integer> baseModule;
	private Map<Integer, List<Integer>> ruleMap;	
	
	private Set<Rule> rules;
	private int pos;
	
	private Map<OWLObject, Integer> objToInt;
	private Map<Integer, OWLObject> intToObj;
	private int objectCount;
	
	private int size = -1;
	
	public RuleSet(){
		this.ruleMap = new HashMap<>();
		this.rules = new LinkedHashSet<>();
		this.baseSignature = new LinkedHashSet<>();
		this.baseModule = new LinkedHashSet<>();
		this.pos = 0;
		
		objectCount = 0;
		objToInt = new HashMap<>();
		intToObj = new HashMap<>();
	}
	
	public int addObject(OWLObject o){
		Integer res = objToInt.get(o);
		if(res == null){
			objToInt.put(o, objectCount);
			intToObj.put(objectCount, o);
			res = objectCount;
			objectCount++;
		}
		return res;
	}
	
	public OWLObject getObject(int i){
		return intToObj.get(i);
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
				for(Integer o : r){
					List<Integer> current = ruleMap.get(o);
					if(current == null) current = new LinkedList<>();
					current.add(pos);
					ruleMap.put(o, current);
				}
				pos++;
			}
		}
		else if(r.getHead() != -1){
			intToObj.get(r.getHead()).accept(this);
		}
		else{
			intToObj.get(r.getAxiom()).accept(this);
		}
	}
	
	@Override
	public void visit(OWLClassAssertionAxiom ax){
		ax.getClassExpression().getSignature().forEach(x -> baseSignature.add(addObject(x)));
		baseModule.add(addObject(ax));
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax){
		ax.getProperty().getSignature().forEach(x -> baseSignature.add(addObject(x)));
		baseModule.add(addObject(ax));
	}
	
	public Set<Integer> getBaseModule(){
		return baseModule;
	}
	
	public Set<Integer> getBaseSignature(){
		return baseSignature;
	}
	
	public int size(){
		return size;
	}
	
	public List<Integer> findRules(int o){
		return ruleMap.get(o);
	}

	@Override
	public Iterator<Rule> iterator() {
		return rules.iterator();
	}
}
