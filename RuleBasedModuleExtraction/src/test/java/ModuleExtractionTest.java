import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

import org.junit.Test;
import org.semanticweb.HermiT.Configuration.WarningMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.Main;
import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.Rule;
import de.spellmaker.rbme.util.ModuleCheck;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ModuleExtractionTest {
	private ModuleCheck mCheck;
	private SyntacticLocalityModuleExtractor extractor;
	private Set<Rule> ruleSet;
	private long startTime;
	
	private int betterRule = 0;
	private int betterAPI = 0;
	
	@Test public void TestModuleExtraction() throws OWLOntologyCreationException{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = null;
		try{
			ontology = m.loadOntologyFromOntologyDocument(new File(Main.onto_testpath));
		}
		catch(Exception e){
			fail("Missing ontology file '" + Main.onto_path + "'");
		}
		//OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File("C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\files\\approximated_896c66df-2415-4e7a-8a3e-aed1f56be49d_ine_roller.ttl_functional.owl"));
		List<OWLClass> ontologySignature = new ArrayList<>();
		mCheck = new ModuleCheck(m);
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
	
		int max = 100;
		
		startTime = System.currentTimeMillis();
		extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		ruleSet =(new ELRuleBuilder()).buildRules(ontology.getAxioms());
		testSet(new HashSet<>(), ontologySignature, -1, ontology, max);
		System.out.println("test took " + (System.currentTimeMillis() - startTime) +  " ms");
		System.out.println("rules were better in " + betterRule + " cases");
		System.out.println("api was better in " + betterAPI + " cases");
	}
	
	private int testSet(Set<OWLClass> currentSet, List<OWLClass> sourceSet, int largestElement, OWLOntology ontology, int max){
		if(max < 0) return max;
		for(int i = largestElement + 1; i < sourceSet.size(); i++){
			currentSet.add(sourceSet.get(i));
			
			testSignature(currentSet, ontology);
			
			max = testSet(currentSet, sourceSet, i, ontology, max - 1);
			currentSet.remove(sourceSet.get(i));
		}
		return max;
	}
	
	private void testSignature(Set<OWLClass> signature, OWLOntology ontology){
		RBMExtractor rbmextractor = new RBMExtractor();
		Set<OWLAxiom> module = (rbmextractor).extractModule(ruleSet, Collections.unmodifiableSet(signature));
		Set<OWLAxiom> module2 = extractor.extract(Collections.unmodifiableSet(signature));

		if(mCheck.isSyntacticalLocalModule(ontology, module2) != null) System.out.println("owlapi module is not syntactically local module");
		if(mCheck.isSemanticalLocalModule(ontology, module2) != null) System.out.println("owlapi module is not semantically local module");
		
		OWLAxiom testRB = mCheck.isSyntacticalLocalModule(ontology, module);
		assertTrue("is no syntactical local module due to axiom '" + testRB + "'", testRB == null);
		
		testRB = mCheck.isSemanticalLocalModule(ontology, module);
		assertTrue("is no semantical local module due to axiom '" + testRB + "'", testRB == null);
		
		
		int msize = module.size();
		int m2size = module2.size();
		Set<OWLAxiom> tmp = new HashSet<>(module);
		module.removeAll(module2);
		module2.removeAll(tmp);
		
		//module.forEach(x -> System.out.println("additional axiom in rule module: '" + x));
		//module2.forEach(x -> System.out.println("additional axiom in local module: '" + x));
		
		if(msize < m2size){
			betterRule++;
			System.out.println("rules found a smaller module");
		}
		
		if(signature.toString().equals("[<http://purl.org/goodrelations/v1#Offering>, owl:Thing, <http://purl.org/goodrelations/v1#QualitativeValue>, <http://purl.org/goodrelations/v1#PaymentMethodCreditCard>, <http://schema.org/Offer>, <http://purl.org/goodrelations/v1#WarrantyScope>, <http://purl.org/goodrelations/v1#QuantitativeValueFloat>, <http://schema.org/Product>, <http://purl.org/goodrelations/v1#Location>, <http://purl.org/goodrelations/v1#QuantitativeValue>, <http://purl.org/goodrelations/v1#ProductOrServiceModel>, <http://purl.org/goodrelations/v1#PaymentMethod>, <http://purl.org/goodrelations/v1#PriceSpecification>, <http://schema.org/Person>, <http://purl.org/goodrelations/v1#DayOfWeek>, <http://purl.org/goodrelations/v1#PaymentChargeSpecification>]")){
			System.out.println("found signature matching with problematic signature");
			System.out.println(rbmextractor.textBuffer);
			fail("found matching signature");
		}
		
		if(m2size < msize){			
			betterAPI++;
			System.out.println("api found a smaller module");
			System.out.println(rbmextractor.textBuffer);
			fail("rule based module is too big; " + msize + " vs " + m2size + " with signature " + signature);
		} 
	}
}
