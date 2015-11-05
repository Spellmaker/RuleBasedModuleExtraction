
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
	
	@Test public void SemanticCorrectnessTestRBME(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> module = (new RBMExtractor()).extractModule(ruleSet, testSet);
				OWLAxiom a1 = mCheck.isSemanticalLocalModule(ontology, module);
				assertTrue("RBME result is not a semantical local module due to axiom " + a1, a1 == null);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
	}
	
	@Test public void SyntacticCorrectnessTestRBME(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> module = (new RBMExtractor()).extractModule(ruleSet, testSet);
				OWLAxiom a1 = mCheck.isSyntacticalLocalModule(ontology, module);
				assertTrue("RBME result is not a syntactical local module due to axiom " + a1, a1 == null);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
	}
	
	@Test public void SemanticCorrectnessTestOWLAPI(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(testSet));
				OWLAxiom a1 = mCheck.isSemanticalLocalModule(ontology, module);
				assertTrue("OWLAPI result is not a semantical local module due to axiom " + a1, a1 == null);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
	}
	
	@Test public void SyntacticCorrectnessTestOWLAPI(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(testSet));
				OWLAxiom a1 = mCheck.isSyntacticalLocalModule(ontology, module);
				assertTrue("OWLAPI result is not a syntactical local module due to axiom " + a1, a1 == null);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
	}
}
