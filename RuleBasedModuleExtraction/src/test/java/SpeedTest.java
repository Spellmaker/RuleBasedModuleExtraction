
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
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class SpeedTest {
	private SyntacticLocalityModuleExtractor extractor;
	private RuleSet ruleSet;
	private long startTime;
	private long endTime;
	private long totalTime;
	private List<OWLClass> ontologySignature;
	private OWLOntology ontology;
	
	@Before
	public void setUp() throws Exception{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		ontology = null;
		ontology = m.loadOntologyFromOntologyDocument(new File(TestSuite.onto_testpath));
		
		ontologySignature = new ArrayList<>();
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
		
		startTime = System.currentTimeMillis();
		ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		endTime = System.currentTimeMillis();
		System.out.println("rule generation took " + (endTime - startTime) + "ms");
		startTime = System.currentTimeMillis();
		extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		endTime = System.currentTimeMillis();
		System.out.println("instantiation of the module extractor took " + (endTime - startTime) + "ms");
	}
	
	@Test public void SpeedTestRBME(){
		totalTime = 0;
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				startTime = System.currentTimeMillis();
				Set<OWLAxiom> module = (new RBMExtractor(false, false)).extractModule(ruleSet, Collections.unmodifiableSet(testSet));
				endTime = System.currentTimeMillis();
				totalTime += endTime - startTime;
				assertTrue(module.size() >= 0);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
		System.out.println("RBME took " + totalTime + "ms");
	}
	
	@Test public void SpeedTestOWLApi(){
		totalTime = 0;
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				startTime = System.currentTimeMillis();
				Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(testSet));
				endTime = System.currentTimeMillis();
				totalTime += endTime - startTime;
				assertTrue(module.size() >= 0);
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
		System.out.println("OWLApi took " + totalTime + "ms");
	}
}
