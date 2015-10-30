package de.spellmaker.rbme.test;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.test.util.SignatureGenerator;
import de.spellmaker.rbme.test.util.SignatureTest;
import de.spellmaker.rbme.util.ModuleCheck;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class CorrectnessTest {
	private SyntacticLocalityModuleExtractor extractor;
	private RuleSet ruleSet;
	private ModuleCheck mCheck;
	private List<OWLClass> ontologySignature;
	private OWLOntology ontology;
	
	@Before
	public void setUp() throws Exception{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		ontology = null;
		ontology = m.loadOntologyFromOntologyDocument(new File(TestSuite.onto_testpath));
		
		ontologySignature = new ArrayList<>();
		mCheck = new ModuleCheck(m);
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
	
		ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
	}
	
	@Test public void CorrectnessTestRBME(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> module = (new RBMExtractor()).extractModule(ruleSet, testSet);
				assertTrue("RBME result is not a semantical local module", mCheck.isSemanticalLocalModule(ontology, module) == null);
				assertTrue("RBME result is not a syntactical local module", mCheck.isSyntacticalLocalModule(ontology, module) == null);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
	}
	
	@Test public void CorrectnessTestOWLApi(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(testSet));
				assertTrue("OWLApi result is not a semantical local module", mCheck.isSemanticalLocalModule(ontology, module) == null);
				assertTrue("OWLApi result is not a syntactical local module", mCheck.isSyntacticalLocalModule(ontology, module) == null);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
	}

}
