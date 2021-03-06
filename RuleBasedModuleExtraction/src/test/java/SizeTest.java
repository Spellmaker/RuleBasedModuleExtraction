
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

public class SizeTest {
	private SyntacticLocalityModuleExtractor extractor;
	private RuleSet ruleSet;
	private List<OWLClass> ontologySignature;
	private OWLOntology ontology;
	
	private int bigger;
	
	@Before
	public void setUp() throws Exception{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		ontology = null;
		ontology = m.loadOntologyFromOntologyDocument(new File(TestSuite.onto_testpath));
		
		ontologySignature = new ArrayList<>();
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
	
		ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		
		bigger = 0;
	}
	
	@Test public void sizeTest(){
		SignatureGenerator.testSet(new SignatureTest() {
			@Override
			public void method(Set<OWLClass> testSet, OWLOntology ontology) {
				Set<OWLAxiom> moduleRBME = (new RBMExtractor(false, false)).extractModule(ruleSet, Collections.unmodifiableSet(testSet));
				Set<OWLAxiom> moduleOWLApi = extractor.extract(Collections.unmodifiableSet(testSet));
				//stop the compiler from optimizing away the module generation
				assertTrue(moduleRBME.size() >= 0);
				assertTrue(moduleOWLApi.size() >= 0);
				
				assertTrue(moduleRBME.size() <= moduleOWLApi.size());
				//debug code, allows to display the additional axioms
				
				/*Set<OWLAxiom> tmp1 = new HashSet<>(moduleRBME);
				Set<OWLAxiom> tmp2 = new HashSet<>(moduleOWLApi);
				
				moduleRBME.forEach(x -> tmp2.remove(x));
				moduleOWLApi.forEach(x -> tmp1.remove(x));				
				
				if(tmp1.size() > 0) tmp1.forEach(x -> System.out.println("additional axiom in rbme: " + x));
				if(tmp2.size() > 0) tmp2.forEach(x -> System.out.println("additional axiom in owlapi: " + x));
				
				if(tmp1.size() > 0 || tmp2.size() > 0){
					System.out.println(testSet);
					
					fail();
				}
				
				if(moduleRBME.size() > moduleOWLApi.size()){
					bigger ++;
		
					moduleOWLApi.forEach(x -> moduleRBME.remove(x));
					System.out.println("module is smaller by " + moduleRBME.size() + " axioms");
					System.out.println("additional axioms are: ");
					moduleRBME.forEach(x -> System.out.println(ClassPrinter.printAxiom(x)));
				}*/
				
			}
		}, new HashSet<>(), ontologySignature, -1, ontology, TestSuite.max_cases);
		assertTrue("OWLApi found " + bigger + " smaller module(s)", bigger == 0);
	}
}
