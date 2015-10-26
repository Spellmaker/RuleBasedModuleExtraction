package de.spellmaker.rbme;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.util.ClassPrinter;
import de.spellmaker.rbme.util.ModuleCheck;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class Main {
	public static String onto_path = "onto.owl";//"EL-GALEN.owl";//"C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\files\\approximated_896c66df-2415-4e7a-8a3e-aed1f56be49d_ine_roller.ttl_functional.owl";
	
	public static void main(String[] args) throws Exception{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = null;
		try{
			ontology = m.loadOntologyFromOntologyDocument(new File(onto_path));
		}
		catch(Exception e){
			System.out.println("Missing ontology file '" + onto_path + "'");
			System.exit(0);
		}
		//OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File("C:\\Users\\spellmaker\\workspace\\RuleBasedModuleExtraction\\onto.owl"));
		
		Set<OWLClass> signature = new HashSet<>();
		//addClass(signature, "http://schema.org/Organization");
		//addClass(signature, "http://purl.org/goodrelations/v1#BusinessFunction");
		addClass(signature, "http://chen.moe/onto/med/Cystic_Fibrosis");
		addClass(signature, "http://chen.moe/onto/med/Genetic_Disorder");
		RuleSet rules = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		System.out.println("Size: " + rules.size());
		System.out.println(rules);
		
		Set<OWLAxiom> module = (new RBMExtractor()).extractModule(rules, signature);
		System.out.println("Rule based extractor:");
		printModule(module);
		System.out.println();
		System.out.println("OWLAPI extractor:");
		SyntacticLocalityModuleExtractor extr = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		Set<OWLEntity> signature2 = new HashSet<>();
		signature.forEach(x -> signature2.add(x));
		Set<OWLAxiom> module2 = extr.extract(signature2);
		printModule(module2);
		
		ModuleCheck mCheck = new ModuleCheck(m);
		/*for(OWLAxiom o : module){
			if(!module2.remove(o)){
				System.out.println("axiom '" + ClassPrinter.printAxiom(o) + "' is not in locality module");
			}
		}
		System.out.println("Missing axioms:");
		for(OWLAxiom o: module2){
			System.out.println(ClassPrinter.printAxiom(o) + " " + o.getClass());
		}*/
		
		OWLAxiom testRuleBased = mCheck.isSyntacticalLocalModule(ontology, module);
		OWLAxiom testLocality = mCheck.isSyntacticalLocalModule(ontology, module2);
		
		if(testRuleBased == null)
			System.out.println("Rule based approach passed the syntactic module check");
		else
			System.out.println("Rule based approach failed the syntactic module check: '" + testRuleBased + "' is not local");
		

		if(testLocality == null)
			System.out.println("Locality based approach passed syntactic the module check");
		else
			System.out.println("Locality based approach failed syntactic the module check: '" + testRuleBased + "' is not local");
		
		testRuleBased = mCheck.isSemanticalLocalModule(ontology, module);
		testLocality = mCheck.isSemanticalLocalModule(ontology, module2);
		
		if(testRuleBased == null)
			System.out.println("Rule based approach passed the semantic module check");
		else
			System.out.println("Rule based approach failed the semantic module check: '" + testRuleBased + "' is not local");
		

		if(testLocality == null)
			System.out.println("Locality based approach passed semantic the module check");
		else
			System.out.println("Locality based approach failed semantic the module check: '" + testRuleBased + "' is not local");
		
	}
	
	private static void printModule(Set<OWLAxiom> module){
		System.out.println("Module size: " + module.size());
		for(OWLAxiom a : module){
			if(a instanceof OWLDeclarationAxiom) continue;
			if(a instanceof OWLClassAssertionAxiom) continue;
			if(a instanceof OWLObjectPropertyAssertionAxiom) continue;
			System.out.println(ClassPrinter.printAxiom(a));
		}
	}
	
	private static void addClass(Set<OWLClass> signature, String iri){
		signature.add(new OWLClassImpl(IRI.create(iri)));
	}
}
