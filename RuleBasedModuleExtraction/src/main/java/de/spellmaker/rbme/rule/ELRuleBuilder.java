package de.spellmaker.rbme.rule;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

/**
 * EL implementation of the RuleBuilder interface.
 * Compiles an EL Ontology into a set of rules to extract modules from it
 * @author spellmaker
 *
 */
public class ELRuleBuilder implements RuleBuilder, OWLAxiomVisitor, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor{
	private RuleSet rs;
	private List<OWLObject> unknownObjects;
	public boolean printUnknown = false;
	
	@Override
	public RuleSet buildRules(Set<OWLAxiom> axioms){

		rs = new RuleSet();
		unknownObjects = new LinkedList<>();
		
		axioms.forEach(x -> x.accept(this));
		
		rs.finalize();
		
		if(unknownObjects.size() > 0){
			//System.out.println("warning: could not generate rules for at least " + unknownObjects.size() + " constructors");
			if(printUnknown){
				Set<Class<?>> classes = new HashSet<>();
				for(Object o : unknownObjects){
					if(!classes.contains(o.getClass())){
						classes.add(o.getClass());
						System.out.println("unknown constructor: " + o.getClass());
					}
				}
			}
		}
		return rs;
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
		
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLClass ce) {
		if(!ce.isTopEntity()) rs.add(new Rule(null, rs.putObject(new OWLDeclarationAxiomImpl(ce, Collections.emptyList())), null, rs.putObject(ce)));
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] arr = new Integer[ops.size()];
		int pos = 0;
		for(Iterator<OWLClassExpression> iter = ops.iterator(); iter.hasNext();){
			arr[pos++] = rs.putObject(iter.next()); 
		}
		
		rs.add(new Rule(rs.putObject(ce), null, null, arr));
		for(OWLClassExpression e : ops){
			e.accept(this);
		}
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		for(OWLClassExpression oce : ce.getOperands()){
			rs.add(new Rule(rs.putObject(ce), null, null, rs.putObject(oce)));
			oce.accept(this);
		}
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		rs.add(new Rule(null, 
				rs.putObject(new OWLDeclarationAxiomImpl((OWLEntity) ce.getProperty(), Collections.emptyList())), null,
				rs.putObject(ce.getProperty())));
		rs.add(new Rule(rs.putObject(ce), null, null, rs.putObject(ce.getFiller()), rs.putObject(ce.getProperty())));
		ce.getFiller().accept(this);		
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub
		rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(axiom.getEntity())));
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression expr = axiom.getSubClass();
		rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(expr)));
		expr.accept(this);
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		for(OWLObjectPropertyExpression e : axiom.getProperties()){
			rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(e)));
			e.accept(this);
		}
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		OWLObjectPropertyExpression expr = axiom.getSubProperty();
		rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(expr)));
		expr.accept(this);
		//axiom.getSuperProperty().accept(this);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
		OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);
		
		rs.add(new Rule(null, rs.putObject(axiom), ((left instanceof OWLClass) ? rs.putObject(left) : null), rs.putObject(right)));
		left.accept(this);
		rs.add(new Rule(null, rs.putObject(axiom), ((right instanceof OWLClass) ? rs.putObject(right) : null), rs.putObject(left)));
		right.accept(this);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(axiom.getProperty())));
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		Integer[] props = new Integer[axiom.getPropertyChain().size()];
		for(int i = 0; i < axiom.getPropertyChain().size(); i++){
			props[i] = rs.putObject(axiom.getPropertyChain().get(i));
		}
		rs.add(new Rule(null, rs.putObject(axiom), null, props));
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(SWRLRule rule) {
		// TODO Auto-generated method stub
		unknownObjects.add(rule);
	}

	@Override
	public Collection<OWLObject> unknownObjects() {
		return unknownObjects;
	}

	@Override
	public void visit(OWLObjectProperty property) {
		if(!property.isTopEntity()) 
			rs.add(new Rule(null, rs.putObject(new OWLDeclarationAxiomImpl(property, Collections.emptyList())), null, rs.putObject(property)));
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataProperty property) {
		// TODO Auto-generated method stub
		
	}
}
