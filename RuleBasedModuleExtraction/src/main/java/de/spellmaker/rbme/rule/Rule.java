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
	
	public Rule(OWLObject head, OWLObject ...body){
		if(head == null) throw new NullPointerException("Rule head cannot be null");
		
		this.head = head;
		if(body != null){
			this.body = new OWLObject[body.length];
			for(int i = 0; i < body.length; i++) this.body[i] = body[i];
		}
		else{
			this.body = null;
		}
	}
		
	/**
	 * Provides access to the rules head
	 * @return The head of the rule
	 */
	public OWLObject getHead(){
		return head;
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
		int res = head.hashCode();
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
			
			if(other.head.toString().equals(head.toString()) && other.body.length == body.length){
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