package de.spellmaker.rbme.rule;

import java.util.Collection;
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

enum Mode{
	BottomMode, TopMode
}

public class BottomModeRuleBuilder implements RuleBuilder, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor, OWLAxiomVisitor {
	private Mode cMode;
	private List<Rule> ruleBuffer;
	private RuleSet ruleSet;
	private List<OWLObject> unknownObjects;
	
	public BottomModeRuleBuilder(){
	}
	@Override
	public void visit(OWLClass ce) {
		//ignore mode here
		//Declaration axiom rule is added elsewhere
		//if(!ce.isTopEntity()) ruleBuffer.add(new Rule(null, ruleSet.putObject(new OWLDeclarationAxiomImpl(ce, Collections.emptyList())), null, ruleSet.putObject(ce)));
		//mode is now bottom, as we interpret classes with bottom
		cMode = Mode.BottomMode;
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		//process branches
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] ruleArgs = new Integer[ops.size()];
		int index = 0;
		
		List<Rule> bottomRules = new LinkedList<>();
		List<Rule> topRules = new LinkedList<>();
		
		boolean allTop = true;
		for(OWLClassExpression o : ops){
			//evaluate child
			o.accept(this);
			//if the child is either bottom or unknown
			if(cMode == Mode.BottomMode){
				//if it is the first bottom mode child, remove
				//all top mode childs from the operands
				if(allTop){
					index = 0;
					allTop = false;
				}
				bottomRules.addAll(ruleBuffer);
			}
			//if the child is either top or unkown _and_ there has been no bottom mode child
			else if(allTop){
				topRules.addAll(ruleBuffer);
			}
			else{
				ruleBuffer.clear();
				continue;
			}
			ruleBuffer.clear();
			
			ruleArgs[index++] = ruleSet.putObject(o);
		}
		
		if(allTop){
			//mode is now top mode. As soon as one element is known to be possibly different from top
			//the whole conjunction is known to be possibly different from top
			for(int i = 0; i < index; i++){
				ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleArgs[i]));
			}
			ruleBuffer.addAll(topRules);
			//mode is still set from the last element
		}
		else{
			//mode is now bot mode. As long as one element is known to be bot, the whole conjunction is still bot
			Integer[] shortArgs = new Integer[index];
			for(int i = 0; i < index; i++) shortArgs[i] = ruleArgs[i];
			ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleArgs));
			ruleBuffer.addAll(bottomRules);
			cMode = Mode.BottomMode;
		}
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		//process branches
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] ruleArgs = new Integer[ops.size()];
		int index = 0;
		
		List<Rule> bottomRules = new LinkedList<>();
		List<Rule> topRules = new LinkedList<>();
		
		boolean allBottom = true;
		for(OWLClassExpression o : ops){
			//evaluate child
			o.accept(this);
			//if the child is either top or unknown
			if(cMode == Mode.TopMode){
				//if it is the first top mode child, remove
				//all bottom mode childs from the operands
				if(allBottom){
					index = 0;
					allBottom = false;
				}
				topRules.addAll(ruleBuffer);
			}
			//if the child is either top or unkown _and_ there has been no bottom mode child
			else if(allBottom){
				bottomRules.addAll(ruleBuffer);
			}
			else{
				ruleBuffer.clear();
				continue;
			}
			ruleBuffer.clear();
			
			ruleArgs[index++] = ruleSet.putObject(o);
		}
		
		if(allBottom){
			//mode is now bottom mode. As soon as one element is known to be possibly different from bottom
			//the whole conjunction is known to be possibly different from bottom
			for(int i = 0; i < index; i++){
				ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleArgs[i]));
			}
			ruleBuffer.addAll(bottomRules);
			//mode is still set from the last element
		}
		else{
			//mode is now top mode. As long as one element is known to be top, the whole conjunction is still top
			Integer[] shortArgs = new Integer[index];
			for(int i = 0; i < index; i++) shortArgs[i] = ruleArgs[i];
			ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleArgs));
			ruleBuffer.addAll(topRules);
			cMode = Mode.TopMode;
		}
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		ce.getOperand().accept(this);
		if(cMode == Mode.BottomMode) cMode = Mode.TopMode;
		else if(cMode == Mode.TopMode) cMode = Mode.BottomMode;
		
		ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleSet.putObject(ce.getOperand())));
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		//process filler
		ce.getFiller().accept(this);
		if(cMode == Mode.BottomMode){
			//R, C -> ER.C
			ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleSet.putObject(ce.getFiller()), ruleSet.putObject(ce.getProperty())));
		}
		else if(cMode == Mode.TopMode){
			cMode = Mode.BottomMode;
			ruleBuffer.clear();
			ruleBuffer.addAll(propertyRules);
			//R -> ER.C
			ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleSet.putObject(ce.getProperty())));
		}
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ce.getFiller().accept(this);
		
		if(cMode == Mode.BottomMode){
			ruleBuffer.clear();
			//R -> VR.C
			ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleSet.putObject(ce.getProperty())));
			ruleBuffer.addAll(propertyRules);
			cMode = Mode.BottomMode;
		}
		else if(cMode == Mode.TopMode){
			//R, C -> VR.C
			ruleBuffer.add(new Rule(ruleSet.putObject(ce), null, null, ruleSet.putObject(ce.getProperty()), ruleSet.putObject(ce.getFiller())));
		}
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectProperty property) {
		//if(!property.isTopEntity())
		//	ruleBuffer.add(new Rule(null, ruleSet.putObject(new OWLDeclarationAxiomImpl(property, Collections.emptyList())), null, ruleSet.putObject(property)));
		//Declaration axiom is added elsewhere
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		unknownObjects.add(property);
	}

	@Override
	public void visit(OWLDataProperty property) {
		unknownObjects.add(property);
	}

	@Override
	public RuleSet buildRules(Set<OWLAxiom> axioms) {
		cMode = Mode.BottomMode;
		ruleBuffer = new LinkedList<>();
		unknownObjects = new LinkedList<>();
		ruleSet = new RuleSet();
		axioms.forEach(x -> x.accept(this));
		ruleSet.finalize();
		if(!unknownObjects.isEmpty()) System.out.println("warning: could not generate rules for at least " + unknownObjects.size() + " things");
		return ruleSet;
	}

	@Override
	public Collection<OWLObject> unknownObjects() {
		return unknownObjects;
	}
	
	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		ruleSet.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(axiom.getEntity())));
	}
	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		ruleBuffer.clear();
		axiom.getSubClass().accept(this);
		Mode subClassMode = cMode;
		List<Rule> subClassRules = ruleBuffer;
		ruleBuffer.clear();
		axiom.getSuperClass().accept(this);
		
		if(subClassMode == Mode.BottomMode){
			if(cMode == Mode.BottomMode){
				//A -> A c B
				ruleBuffer.clear();
				ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(axiom.getSubClass())));
				ruleBuffer.addAll(subClassRules);
			}
			else if(cMode == Mode.TopMode){
				//A, B -> A c B
				ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(axiom.getSubClass()), ruleSet.putObject(axiom.getSuperClass())));
				ruleBuffer.addAll(subClassRules);
			}
		}
		else if(subClassMode == Mode.TopMode){
			if(cMode == Mode.BottomMode){
				// -> A c B
				ruleBuffer.clear();
				ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null));
			}
			else if(cMode == Mode.TopMode){
				//B -> A c B
				ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(axiom.getSuperClass())));
			}
		}
		
		ruleBuffer.forEach(x -> ruleSet.add(x));
	}
	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		ruleBuffer.clear();
		for(OWLObjectPropertyExpression p : axiom.getProperties()){
			p.accept(this);
			ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(p)));
		}
		ruleBuffer.forEach(x -> ruleSet.add(x));
	}
	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		ruleSet.add(new Rule(null, ruleSet.putObject(axiom), null));
	}
	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		ruleSet.add(new Rule(null, ruleSet.putObject(axiom), null));
	}
	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getSubProperty().accept(this);
		ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(axiom.getSubProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.add(x));
	}
	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		ruleSet.add(new Rule(null, ruleSet.putObject(axiom), null));
	}
	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		ruleBuffer.clear();

		OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
		OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);
		
		left.accept(this);
		Mode leftMode = cMode;
		
		right.accept(this);
		
		if(leftMode != cMode){
			ruleBuffer.clear();
			// -> A = B
			ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null));
		}
		else{
			//A -> A = B, B
			//B -> A = B, A
			ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), ((left instanceof OWLClass) ? ruleSet.putObject(left) : null), ruleSet.putObject(right)));
			ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), ((right instanceof OWLClass) ? ruleSet.putObject(right) : null), ruleSet.putObject(left)));
		}
		
		ruleBuffer.forEach(x -> ruleSet.add(x));
	}
	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleSet.putObject(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.add(x));
	}
	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		ruleSet.add(new Rule(null, ruleSet.putObject(axiom), null));
	}
	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		ruleBuffer.clear();
		Integer[] ruleArgs = new Integer[axiom.getPropertyChain().size()];
		int index = 0;
		for(OWLObjectPropertyExpression p : axiom.getPropertyChain()){
			p.accept(this);
			ruleArgs[index++] = ruleSet.putObject(p);
		}
		ruleBuffer.add(new Rule(null, ruleSet.putObject(axiom), null, ruleArgs));
		
		ruleBuffer.forEach(x -> ruleSet.add(x));
	}
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(SWRLRule rule) {
		unknownObjects.add(rule);
	}

}
