package de.spellmaker.rbme.test.util;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

public interface SignatureTest {
	public void method(Set<OWLClass> testSet, OWLOntology ontology);
}
