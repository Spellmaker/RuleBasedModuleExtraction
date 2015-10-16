package de.spellmaker.rbme.rule;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public interface RuleBuilder {
	public RuleSet buildRules(Set<OWLAxiom> axioms);
}
