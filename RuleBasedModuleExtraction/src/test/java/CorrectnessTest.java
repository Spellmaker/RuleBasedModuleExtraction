
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.*;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.util.ClassPrinter;
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
		List<Set<Integer>> moduleList = ModuleBuilder.getModules(ontology, ruleSet, false);
		List<Set<Integer>> defined = ModuleBuilder.definedAxioms;
		System.out.println("done building modules");
		for(int i = 0; i < moduleList.size(); i++){
			Set<OWLAxiom> cModule = moduleList.get(i).stream().map(x -> (OWLAxiom) ruleSet.lookup(x)).collect(Collectors.toSet());
			Set<OWLAxiom> cDefined = defined.get(i).stream().map(x -> (OWLAxiom) ruleSet.lookup(x)).collect(Collectors.toSet());
			
			OWLAxiom error = mCheck.isSemanticalLocalModule(ontology, cModule, cDefined);
			assertTrue("RBME result is not a semantical local module due to axiom " + error + " signatures is " + ModuleBuilder.signatures.get(i).stream().map(x -> ruleSet.lookup(x)).collect(Collectors.toSet()), error == null);
			System.out.println("(" + i + "/" + moduleList.size() + ")");
		}
		
		
		
		/*int cnt = 0;
		Set<OWLClass> takenCare = new HashSet<>();
		for(OWLClass c : ontologySignature){
			if(takenCare.contains(c)) continue;
			
			if(cnt % 100 == 0) System.out.println("class " + cnt + " of " + ontologySignature.size());
			Set<OWLClass> sign = new HashSet<>();
			sign.add(c);
			RBMExtractor rbme = new RBMExtractor();
			Set<OWLAxiom> module = rbme.extractModule(ruleSet, Collections.unmodifiableSet(sign));
			Set<OWLAxiom> defined = new HashSet<>();
			rbme.getDefinedAxioms().forEach(x -> defined.add((OWLAxiom) ruleSet.lookup(x)));
			
			module.forEach(x -> takenCare.addAll(x.getClassesInSignature()));
			OWLAxiom a1 = mCheck.isSemanticalLocalModule(ontology, module, defined);
			if(a1 != null){
				Set<OWLEntity> modSig = new HashSet<>();
				module.forEach(x -> modSig.addAll(x.getSignature()));
				modSig.forEach(x -> System.out.println(x));
			}
			assertTrue("RBME result is not a semantical local module due to axiom " + a1 + " signature is " + sign, a1 == null);
			cnt++;
		}*/
	}
	
	/*@Test public void SyntacticCorrectnessTestRBME(){
		int cnt = 0;
		Set<OWLClass> takenCare = new HashSet<>();
		for(OWLClass c : ontologySignature){
			if(takenCare.contains(c)) continue;
			
			if(cnt % 100 == 0) System.out.println("class " + cnt + " of " + ontologySignature.size());
			Set<OWLClass> sign = new HashSet<>();
			sign.add(c);
			RBMExtractor rbme = new RBMExtractor();
			Set<OWLAxiom> module = rbme.extractModule(ruleSet, Collections.unmodifiableSet(sign));
			Set<OWLAxiom> defined = new HashSet<>();
			rbme.getDefinedAxioms().forEach(x -> defined.add((OWLAxiom) ruleSet.lookup(x)));
			module.forEach(x -> takenCare.addAll(x.getClassesInSignature()));
			OWLAxiom a1 = mCheck.isSyntacticalLocalModule(ontology, module, defined);
			assertTrue("RBME result is not a syntactical local module due to axiom " + a1 + " signature is " + sign, a1 == null);
			cnt++;
		}
	}*/
	
	/*@Test public void SemanticCorrectnessTestOWLAPI(){
		int cnt = 0;
		Set<OWLClass> takenCare = new HashSet<>();
		for(OWLClass c : ontologySignature){
			if(takenCare.contains(c)) continue;
			
			if(cnt % 100 == 0) System.out.println("class " + cnt + " of " + ontologySignature.size());
			Set<OWLClass> sign = new HashSet<>();
			sign.add(c);
			Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(sign));
			module.forEach(x -> takenCare.addAll(x.getClassesInSignature()));
			OWLAxiom a1 = mCheck.isSemanticalLocalModule(ontology, module);
			assertTrue("OWLAPI result is not a semantical local module due to axiom " + a1 + " signature is " + sign, a1 == null);
			cnt++;
		}
		/*System.out.println("start semantic correctness owlapi");
		for(OWLClass c : ontologySignature){
			System.out.println("================start");
			Set<OWLClass> sign = new HashSet<>();
			sign.add(c);
			Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(sign));
			OWLAxiom a1 = mCheck.isSemanticalLocalModule(ontology, module);
			if(a1 != null){
				System.out.println(a1);
				System.out.println("signature: ");
				for(OWLClass cl : sign){
					System.out.println(ClassPrinter.printClass(cl));
				}
				System.out.println("module: ");
				for(OWLAxiom ax : module){
					
					System.out.println(ClassPrinter.printAxiom(ax));
				}
			}
			System.out.println("==================passed");
			assertTrue("OWLAPI result is not a semantical local module due to axiom " + a1 + " signature is " + sign, a1 == null);
		}

		System.out.println("end semantic correctness owlapi");* /
	}
	
	@Test public void SyntacticCorrectnessTestOWLAPI(){
		int cnt = 0;
		Set<OWLClass> takenCare = new HashSet<>();
		for(OWLClass c : ontologySignature){
			if(takenCare.contains(c)) continue;
			
			if(cnt % 100 == 0) System.out.println("class " + cnt + " of " + ontologySignature.size());
			Set<OWLClass> sign = new HashSet<>();
			sign.add(c);
			Set<OWLAxiom> module = extractor.extract(Collections.unmodifiableSet(sign));
			module.forEach(x -> takenCare.addAll(x.getClassesInSignature()));
			OWLAxiom a1 = mCheck.isSyntacticalLocalModule(ontology, module);
			assertTrue("OWLAPI result is not a semantical local module due to axiom " + a1 + " signature is " + sign, a1 == null);
			cnt++;
		}
	}*/
}
