package de.spellmaker.rbme.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

import de.spellmaker.rbme.extractor.RBMExtractor;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

public class RuleSet extends OWLObjectVisitorAdapter implements Iterable<Rule>{
	private Set<Integer> baseSignature;
	private Set<OWLAxiom> baseModule;
	private Map<Integer, List<Integer>> ruleMap;	
	
	private Map<Integer, List<Integer>> axiomSignatures;
	private List<OWLObject> dictionary;
	private List<Boolean> isDeclRule;
	private Map<OWLObject, Integer> invDictionary;
	private OWLObject[] arrDictionary;
	private Boolean[] arrisDeclRule;
	
	
	private Set<Rule> rules;
	private Rule[] rulesArray;
	private int pos;
	
	private int size = -1;
	
	public RuleSet(){
		this.ruleMap = new HashMap<>();
		this.rules = new LinkedHashSet<>();
		this.baseModule = new LinkedHashSet<>();
		this.baseSignature = new LinkedHashSet<>();
		this.isDeclRule = new LinkedList<>();
		this.pos = 0;
		dictionary = new LinkedList<>();
		invDictionary = new HashMap<>();
		axiomSignatures = new HashMap<>();
		arrDictionary = null;
		
		//the rule set always knows owl:thing
		OWLDataFactory factory = new OWLDataFactoryImpl();
		putObject(factory.getOWLThing());
	}
	
	public OWLObject lookup(int i){
		return arrDictionary[i];
	}
	
	public OWLObject debugLookup(int i){
		return dictionary.get(i);
	}
	
	public List<Integer> getAxiomSignature(int i){
		return axiomSignatures.get(i);
	}
	
	public boolean isDeclRule(int i){
		return arrisDeclRule[i];
	}
	
	public int putObject(OWLObject o){
		Integer index = invDictionary.get(o);
		if(index == null){
			if(arrDictionary != null) throw new UnsupportedOperationException("RuleSet has already been finalized, cannot add object '" + o + "'");
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
		//run module extraction once with the base signature to determine the correct
		//base module and -signature		
		size = rules.size();
		ruleMap = Collections.unmodifiableMap(ruleMap);
		
		rulesArray = new Rule[rules.size()];
		int cnt = 0;
		for(Rule r : rules){
			rulesArray[cnt++] = r;
		}
		rules = null;//Collections.unmodifiableSet(rules);
		
		arrDictionary = dictionary.toArray(new OWLObject[1]);
		arrisDeclRule = isDeclRule.toArray(new Boolean[1]);
		dictionary = Collections.unmodifiableList(dictionary);
		
		RBMExtractor rbme = new RBMExtractor(false, false);
		Set<OWLEntity> sig = new HashSet<>();
		baseSignature = new LinkedHashSet<>();
		baseModule.forEach(x -> sig.addAll(x.getSignature()));
		baseModule = rbme.extractModule(this, sig);
		baseModule.forEach(x -> x.getSignature().forEach(y -> baseSignature.add(putObject(y))));
		
		baseSignature = Collections.unmodifiableSet(baseSignature);
		baseModule = Collections.unmodifiableSet(baseModule);
	}
	
	public Rule getRule(int i){
		/*Iterator<Rule> it = rules.iterator();
		Rule c = it.next();
		for(int j = 0; j < i; j++){
			c = it.next();
		}
		return c;*/
		return rulesArray[i];
	}
	
	public void add(Rule r){
		if(rules == null){
			throw new UnsupportedOperationException("RuleSet has already been finalized, cannot add rule '" + r + "'");
		}
		if(r.size() > 0){
			if(this.rules.add(r)){
				if(r.getAxiom() != null){
					isDeclRule.add(dictionary.get(r.getAxiom()) instanceof OWLDeclarationAxiom);
				}
				else isDeclRule.add(false);
				
				for(Integer o : r){
					List<Integer> current = ruleMap.get(o);
					if(current == null) current = new LinkedList<>();
					current.add(pos);
					ruleMap.put(o, current);
				}
				pos++;
			}
		}
	}
	
	@Override
	public void visit(OWLClassAssertionAxiom ax){
		//baseSignature.addAll(ax.getClassExpression().getSignature());
		//baseModule.add(ax);
		ax.getClassExpression().getSignature().forEach(x -> baseSignature.add(putObject(x)));
		baseSignature.add(putObject(ax.getIndividual()));
		putObject(ax);
		for(OWLEntity ent : ax.getClassExpression().getSignature()){
			if(!(ent instanceof OWLClass)) continue;
			
			OWLAxiom declAxiom = new OWLDeclarationAxiomImpl(ent, Collections.emptyList());
			putObject(declAxiom);
			baseModule.add(declAxiom);
		}
		baseModule.add(ax);
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax){
		OWLObjectPropertyExpression prop = ax.getProperty();
		prop.getSignature().forEach(x -> baseSignature.add(putObject(x)));
		ax.getIndividualsInSignature().forEach(x -> baseSignature.add(putObject(x)));
		putObject(ax);
		prop.getSignature().stream().filter(x -> x instanceof OWLObjectProperty).forEach(x -> baseModule.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		baseModule.add(ax);
		//baseSignature.addAll(prop.getSignature());
		//baseModule.add(ax);
	}
	
	@Override
	public void visit(OWLDifferentIndividualsAxiom ax){
		baseModule.add(ax);
		for(OWLIndividual ind : ax.getIndividuals()){
			baseSignature.add(putObject(ind));
		}
	}
	
	@Override
	public void visit(OWLSameIndividualAxiom ax){
		baseModule.add(ax);
		for(OWLIndividual ind : ax.getIndividuals()){
			baseSignature.add(putObject(ind));
		}
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
		return new ArrayIterator<Rule>(rulesArray);
	}
}
