package de.spellmaker.rbme.rule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.spellmaker.rbme.util.ClassPrinter;

public class Rule {
	private Set<Object> body;
	private Object head;
	
	public Rule(Object head, Object ...body){
		if(head == null) throw new NullPointerException("Rule head cannot be null");
		
		this.head = head;
		this.body = new HashSet<Object>();
		for(Object o : body){
			this.body.add(o);
		}
	}

	public boolean offer(Object o){
		this.body.remove(o);
		return isFinished();
	}
	
	public boolean isFinished(){
		return this.body.size() == 0;
	}
	
	public Set<Object> getBody(){
		return Collections.unmodifiableSet(body);
	}
	
	public Object getHead(){
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
	
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Rule){
			Rule other = (Rule) o;
			if(other.head.toString().equals(head.toString()) && other.body.size() == body.size()){
				Iterator<Object> otherIter = other.body.iterator();
				Iterator<Object> myIter = body.iterator();
				while(myIter.hasNext()){
					if(!otherIter.next().toString().equals(myIter.next().toString())) return false;
				}
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Rule clone(){
		Object[] copyBody = new Object[body.size()];
		Iterator<Object> iter = body.iterator();
		for(int i = 0; i < body.size(); i++){
			copyBody[i] = iter.next();
		}
		return new Rule(head, copyBody);
	}
}
