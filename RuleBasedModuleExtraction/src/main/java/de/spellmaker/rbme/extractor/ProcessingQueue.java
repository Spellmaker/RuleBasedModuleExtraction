package de.spellmaker.rbme.extractor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

/**
 * Main processing queue for the rule-based module extraction.
 * This class encapsulates the data structures to ensure that entities are added to the correct structures.
 * Most queue functions remain unimplemented
 * @author spellmaker
 *
 */
public class ProcessingQueue implements Queue<Object>{
	private Set<OWLAxiom> module;
	private Set<Object> knownNotBottom;
	private Queue<Object> queue;
	private OWLEntity owlThing;
	
	public ProcessingQueue(Set<OWLClass> signature){
		OWLDataFactory factory = new OWLDataFactoryImpl();
		owlThing = factory.getOWLThing();
		module = new HashSet<>();
		knownNotBottom = new HashSet<>(signature);
		signature.stream().filter(x -> !x.equals(owlThing)).forEach(x -> module.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		queue = new LinkedList<>(signature);
		
		//OWL Thing is always assumed to be not bottom
		queue.add(owlThing);
		knownNotBottom.add(owlThing);
	}
	
	/**
	 * Adds the given axiom to the module build buy the processing queue
	 * @param ax An OWL Axiom
	 */
	public void addToModule(OWLAxiom ax){
		module.add(ax);
	}
	
	/**
	 * Provides access to the module build in the processing queue
	 * The returned module is only correct when the processing has terminated
	 * @return A set of OWL Axioms forming a module
	 */
	public Set<OWLAxiom> getModule(){
		return module;
	}
	
	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return queue.iterator();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(Object e) {
		//add the entity to the list of those known to be possibly not bottom
		if(knownNotBottom.add(e)){
			//only if it is actually new knowledge process further
			if(e instanceof OWLClass || e instanceof OWLObjectProperty){
				//add declarations for classes and properties to the module
				module.add(new OWLDeclarationAxiomImpl((OWLEntity)e, Collections.emptyList()));
			}
			//add the entity to the processing queue
			return queue.add(e);
		}
		return false;
	}

	@Override
	public boolean offer(Object e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove() {
		return queue.remove();
	}

	@Override
	public Object poll() {
		return queue.poll();
	}

	@Override
	public Object element() {
		return queue.element();
	}

	@Override
	public Object peek() {
		return queue.peek();
	}

}
