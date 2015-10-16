package de.spellmaker.rbme.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.LocalityEvaluator;
import com.clarkparsia.owlapi.modularity.locality.SemanticLocalityEvaluator;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

/**
 * Checks for the correctness of a module
 * @author spellmaker
 *
 */
public class ModuleCheck {
	private OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
	private SemanticLocalityEvaluator locality;
	private SyntacticLocalityEvaluator locality2 = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
	
	public ModuleCheck(OWLOntologyManager manager){
		locality = new SemanticLocalityEvaluator(manager, factory);
	}	
	
	/**
	 * Checks for syntactical locality of the module in the given ontology
	 * @param ontology An OWL ontology
	 * @param module A module
	 * @return null, if the module is correct or the axiom for which the module is not correct
	 */
	public OWLAxiom isSyntacticalLocalModule(OWLOntology ontology, Set<OWLAxiom> module){
		return check(ontology, module, locality2);
	}
	
	/**
	 * Checks for semantical locality of the module in the given ontology using a reasoner
	 * @param ontology An OWL ontology
	 * @param module A module
	 * @return null, if the module is correct or the axiom for which the module is not correct
	 */
	public OWLAxiom isSemanticalLocalModule(OWLOntology ontology, Set<OWLAxiom> module){
		return check(ontology, module, locality);
	}
	
	private OWLAxiom check(OWLOntology ontology, Set<OWLAxiom> module, LocalityEvaluator evaluator){
		Set<OWLEntity> signature = new HashSet<>();
		module.forEach(x -> signature.addAll(x.getSignature()));
		for(OWLAxiom axiom : ontology.getAxioms()){
			if(module.contains(axiom)) continue;
			if(axiom instanceof OWLDeclarationAxiom) continue;
			if(axiom instanceof OWLSubPropertyChainOfAxiom) continue;
			if(axiom instanceof OWLSubObjectPropertyOfAxiom) continue;
			if(axiom instanceof OWLTransitiveObjectPropertyAxiom) continue;
			
			if(!evaluator.isLocal(axiom, signature)) return axiom;
		}
		return null;
	}
}
