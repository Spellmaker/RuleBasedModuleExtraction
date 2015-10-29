package de.spellmaker.rbme.rule;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLEntity;

import de.spellmaker.rbme.util.ClassPrinter;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

/**
 * Represents a module extraction rule
 * @author spellmaker
 *
 */
public class Rule implements Iterable<Integer>{
	private final int[] body;
	private final int head;
	private final int axiom;
	private final List<Integer> axiomSignature;
	
	public Rule(RuleSet parent, OWLObject head, OWLAxiom axiom, OWLObject ...body){		
		this.axiom = (axiom == null) ? -1 : parent.addObject(axiom);
		this.head = (head == null) ? -1 : parent.addObject(head);
		
		axiomSignature = new LinkedList<>();
		if(axiom != null){
			for(OWLObject o : axiom.getSignature()){
				axiomSignature.add(parent.addObject(o));
				if(o instanceof OWLClass || o instanceof OWLObjectProperty){
					parent.add(new Rule(parent, new OWLDeclarationAxiomImpl((OWLEntity) o, Collections.emptyList()), null, o));
				}
			}
		}
		
		if(body != null){
			this.body = new int[body.length];
			for(int i = 0; i < body.length; i++) this.body[i] = parent.addObject(body[i]);
		}
		else{
			this.body = null;
		}
	}
	
	public int getAxiom(){
		return axiom;
	}
	
	public List<Integer> getAxiomSignature(){
		return Collections.unmodifiableList(axiomSignature);
	}
		
	/**
	 * Provides access to the rules head
	 * @return The head of the rule
	 */
	public int getHead(){
		return head;
	}
	
	public int get(int i){
		return body[i];
	}
	
	@Override
	public String toString(){
		String res = "";
		for(Object e : body){
			res = res + (res.equals("") ? "" : " & ") + ClassPrinter.printClass(e);
		}
		//if(head instanceof OWLAxiom) 	res += " -> " + ClassPrinter.printAxiom((OWLAxiom) head);
		//else 							res += " -> " + ClassPrinter.printClass(head);
		return res;
	}
	
	@Override
	public int hashCode(){
		int res = head + axiom;
		for(int i : body) res += i;
		return res;
	}
	
	public int size(){
		return (body == null) ? 0 : body.length;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Rule){
			Rule other = (Rule) o;
			
			if(head == other.head && axiom == other.axiom){
				if(other.body.length == body.length){
					Iterator<Integer> otherIter = other.iterator();
					Iterator<Integer> myIter = iterator();
					while(myIter.hasNext()){
						if(otherIter.next() != myIter.next()) return false;
					}
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new ArrayIterator(body);
	}
}

class ArrayIterator implements Iterator<Integer>{
	private final int[] array;
	private int position;
	
	public ArrayIterator(int[] array){
		this.array = array;
		this.position = -1;
	}
	
	@Override
	public boolean hasNext() {
		return position < array.length - 1;
	}

	@Override
	public Integer next() {
		return array[++position];
	}
	
}
