package de.spellmaker.rbme.rule;

import java.util.Iterator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import de.spellmaker.rbme.util.ClassPrinter;

/**
 * Represents a module extraction rule
 * @author spellmaker
 *
 */
public class Rule implements Iterable<OWLObject>{
	private final OWLObject[] body;
	private final OWLObject head;
	private final OWLAxiom axiom;
	
	public Rule(OWLObject head, OWLAxiom axiom, OWLObject ...body){
		this.axiom = axiom;
		this.head = head;
		if(body != null){
			this.body = new OWLObject[body.length];
			for(int i = 0; i < body.length; i++) this.body[i] = body[i];
		}
		else{
			this.body = null;
		}
	}
	
	public OWLAxiom getAxiom(){
		return axiom;
	}
		
	/**
	 * Provides access to the rules head
	 * @return The head of the rule
	 */
	public OWLObject getHead(){
		return head;
	}
	
	public OWLObject get(int i){
		return body[i];
	}
	
	@Override
	public String toString(){
		String res = "";
		for(Object e : body){
			res = res + (res.equals("") ? "" : " & ") + ClassPrinter.printClass(e);
		}
		if(head instanceof OWLAxiom) 	res += " -> " + ClassPrinter.printAxiom((OWLAxiom) head);
		else 							res += " -> " + ClassPrinter.printClass(head);
		return res;
	}
	
	@Override
	public int hashCode(){
		int res = 0;
		if(head != null) res += head.hashCode();
		if(axiom != null) res += axiom.hashCode();
		for(Object o : body) res += o.hashCode();
		return res;
	}
	
	public int size(){
		return (body == null) ? 0 : body.length;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Rule){
			Rule other = (Rule) o;
			
			boolean res = (other.head == null && head == null) || 
					(other.head != null && head != null && other.head.toString().equals(head.toString()));
			res = res && (other.axiom == null && axiom == null) ||
					(other.axiom != null && axiom != null && other.axiom.toString().equals(axiom.toString()));
			
			if(res && other.body.length == body.length){
				Iterator<OWLObject> otherIter = other.iterator();
				Iterator<OWLObject> myIter = iterator();
				while(myIter.hasNext()){
					if(!otherIter.next().toString().equals(myIter.next().toString())) return false;
				}
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Iterator<OWLObject> iterator() {
		return new ArrayIterator(body);
	}
}

class ArrayIterator implements Iterator<OWLObject>{
	private final OWLObject[] array;
	private int position;
	
	public ArrayIterator(OWLObject[] array){
		this.array = array;
		this.position = -1;
	}
	
	@Override
	public boolean hasNext() {
		return position < array.length - 1;
	}

	@Override
	public OWLObject next() {
		return array[++position];
	}
	
}
