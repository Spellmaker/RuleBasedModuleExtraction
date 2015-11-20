package de.spellmaker.rbme.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class RuleSet extends OWLObjectVisitorAdapter implements Iterable<Rule>{
	private Set<Integer> baseSignature;
	private Set<OWLAxiom> baseModule;
	private Map<Integer, List<Integer>> ruleMap;	
	
	private Map<Integer, List<Integer>> axiomSignatures;
	private List<OWLObject> dictionary;
	private Map<OWLObject, Integer> invDictionary;
	private OWLObject[] arrDictionary;
	
	
	private Set<Rule> rules;
	private int pos;
	
	private int size = -1;
	
	public RuleSet(){
		this.ruleMap = new HashMap<>();
		this.rules = new LinkedHashSet<>();
		this.baseSignature = new LinkedHashSet<>();
		this.baseModule = new LinkedHashSet<>();
		this.pos = 0;
		dictionary = new LinkedList<>();
		invDictionary = new HashMap<>();
		axiomSignatures = new HashMap<>();
		
		//the rule set always knows owl:thing
		OWLDataFactory factory = new OWLDataFactoryImpl();
		putObject(factory.getOWLThing());
	}
	
	public OWLObject lookup(int i){
		return arrDictionary[i];
	}
	
	public List<Integer> getAxiomSignature(int i){
		return axiomSignatures.get(i);
	}
	
	public int putObject(OWLObject o){
		Integer index = invDictionary.get(o);
		if(index == null){
			//object is not known
			index = dictionary.size();
			dictionary.add(o);
			invDictionary.put(o, index);
			
			if(o instanceof OWLAxiom){
				List<Integer> sign = new LinkedList<>();
				OWLAxiom ax = (OWLAxiom) o;
				for(OWLObject obj : ax.getSignature()){
					sign.add(putObject(obj));
				}
				axiomSignatures.put(index, Collections.unmodifiableList(sign));
				ax.accept(this);
			}
		}
		
		return index;
	}
	
	public void finalize(){
		size = rules.size();
		baseSignature = Collections.unmodifiableSet(baseSignature);
		baseModule = Collections.unmodifiableSet(baseModule);
		ruleMap = Collections.unmodifiableMap(ruleMap);
		rules = Collections.unmodifiableSet(rules);
		
		arrDictionary = dictionary.toArray(new OWLObject[1]);
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
		/*else if(r.getHead() != null){
			r.getHead().accept(this);
		}
		else{
			r.getAxiom().accept(this);
		}*/
	}
	
	@Override
	public void visit(OWLClassAssertionAxiom ax){
		//baseSignature.addAll(ax.getClassExpression().getSignature());
		//baseModule.add(ax);
		ax.getClassExpression().getSignature().forEach(x -> baseSignature.add(putObject(x)));
		putObject(ax);
		baseModule.add(ax);
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax){
		OWLObjectPropertyExpression prop = ax.getProperty();
		prop.getSignature().forEach(x -> baseSignature.add(putObject(x)));
		putObject(ax);
		baseModule.add(ax);
		//baseSignature.addAll(prop.getSignature());
		//baseModule.add(ax);
	}
	
	public Set<OWLAxiom> getBaseModule(){
		return baseModule;
	}
	
	public Set<Integer> getBaseSignature(){
		return baseSignature;
	}
	
	public int size(){
		return size;
	}
	
	public int dictionarySize(){
		return arrDictionary.length;
	}
	
	public List<Integer> findRules(Integer o){
		return ruleMap.get(o);
	}

	@Override
	public Iterator<Rule> iterator() {
		return rules.iterator();
	}
}
