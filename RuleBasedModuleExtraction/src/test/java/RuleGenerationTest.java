
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;

public class RuleGenerationTest {
	private RuleSet ruleSet;
	private List<OWLClass> ontologySignature;
	private OWLOntology ontology;
	
	@Before
	public void setUp() throws Exception{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		ontology = null;
		ontology = m.loadOntologyFromOntologyDocument(new File(TestSuite.onto_testpath));
		
		ontologySignature = new ArrayList<>();
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
		
		
	}
	
	@Test public void ruleGenerationTest(){
		RuleBuilder ruleBuilder = new ELRuleBuilder();
		ruleSet = ruleBuilder.buildRules(ontology.getAxioms());
		assertTrue("no rules have been generated", ruleSet.size() > 0);
		
		if(ruleBuilder.unknownObjects().size() > 0){
			String message = "found " + ruleBuilder.unknownObjects().size() + " unknown OWLObjects: ";
			for(Iterator<OWLObject> iter = ruleBuilder.unknownObjects().iterator(); iter.hasNext(); ){
				message += iter.next() + (iter.hasNext() ? ", " : "");
			}
			fail("rule builder encountered unknown objects: " + message);
		}
	}
}
