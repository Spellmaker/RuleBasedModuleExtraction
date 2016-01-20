
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.*;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapiv3.OWL;

import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.util.ClassPrinter;
import de.spellmaker.rbme.util.ModuleCheck;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ModuleSizeTest {
	private SyntacticLocalityModuleExtractor extractor;
	private RuleSet ruleSet;
	private List<OWLClass> ontologySignature;
	private OWLOntology ontology;
	private OWLOntologyManager m;
	
	private List<Set<OWLAxiom>> owlmodules;
	private List<Set<OWLAxiom>> rbmemodules;
	
	private int bigger;
	
	@Before
	public void setUp() throws Exception{
		m = OWLManager.createOWLOntologyManager();
		ontology = null;
		ontology = m.loadOntologyFromOntologyDocument(new File(TestSuite.onto_testpath));
		
		ontologySignature = new ArrayList<>();
		ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
	
		ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		
		owlmodules = new ArrayList<>(ontology.getAxiomCount());
		rbmemodules = new ArrayList<>(ontology.getAxiomCount());
		
		bigger = 0;
	}
	
	@Test public void sizeTest(){
		List<Set<Integer>> modules = ModuleBuilder.getModules(ontology, ruleSet, false);
		Set<OWLAxiom> axiomSet = new HashSet<>(ontology.getAxioms());
		modules.forEach(x -> x.forEach(y -> axiomSet.remove(ruleSet.lookup(y))));
		System.out.println("found " + modules.size() + " modules");
		modules.forEach(x -> System.out.println(x.size()));
		int axioms = 0;
		for(Set<Integer> m : modules){
			axioms += m.size();
		}
		System.out.println("total axioms: " + axioms);
		System.out.println("rbmemodule avg: " + (axioms / modules.size()));
		System.out.println("missing axioms: " + axiomSet.size());
		axiomSet.forEach(x -> System.out.println(x));
		
		//check if no module is contained in another
		/*for(int i = 0; i < modules.size(); i++){
			for(int j = i + 1; j < modules.size(); j++){
				List<OWLAxiom> m1 = new ArrayList<>(modules.get(i));
				List<OWLAxiom> m2 = new ArrayList<>(modules.get(j));
				if(m2.size() < m1.size()){
					List<OWLAxiom> swap = m1;
					m1 = m2;
					m2 = swap;
				}
				
				for(OWLAxiom a : m2){
					m1.remove(a);
				}
				if(m1.size() <= 0){
					System.out.println("module1:");
					System.out.println("signature was " + ModuleBuilder.signatures.get(i));
					modules.get(i).stream().filter(x -> !ruleSet.getBaseModule().contains(x)).forEach(x -> System.out.println(x));
					ModuleCheck mCheck = new ModuleCheck(m);
					System.out.println("is correct module 1: " + mCheck.isSemanticalLocalModule(ontology, modules.get(i)));
					//modules.get(i).forEach(x -> System.out.println(x));
					System.out.println("module2:");
					System.out.println("signature was " + ModuleBuilder.signatures.get(j));
					modules.get(j).stream().filter(x -> !ruleSet.getBaseModule().contains(x)).forEach(x -> System.out.println(x));
					System.out.println("is correct module 2: " + mCheck.isSemanticalLocalModule(ontology, modules.get(j)));
					System.out.println("signature of module 1:");
					Set<OWLEntity> coll = new HashSet<>();
					modules.get(i).forEach(x -> coll.addAll(x.getSignature()));
					coll.forEach(x -> System.out.println(x));
					System.out.println("signature of module 2:");
					Set<OWLEntity> coll2 = new HashSet<>();
					modules.get(j).forEach(x -> coll2.addAll(x.getSignature()));
					coll2.forEach(x -> System.out.println(x));
				}
				assertTrue("modules should never be contained in each other", m1.size() > 0);
			}
		}*/
		
		
		assertTrue("all axioms should be in a module", axiomSet.size() == 0);
		
		
		/*Set<OWLEntity> sign = ontology.getSignature();
		
		Map<OWLEntity, Integer> mapping = new HashMap<>();
		
		StringBuilder out = new StringBuilder();
		
		List<Integer> modSizes = new LinkedList<>();
		
		Set<OWLAxiom> ontoAxioms = new HashSet<>(ontology.getAxioms());
		
		for(OWLEntity e : sign){
			Set<OWLEntity> s = new HashSet<>();
			//if(!(e instanceof OWLClass)) continue;
			s.add((OWLEntity) e);
			 
			Set<OWLAxiom> moduleRBME = (new RBMExtractor()).extractModule(ruleSet, s);
			Set<OWLEntity> modSig = new HashSet<>();
			for(OWLAxiom a : moduleRBME){
				modSig.addAll(a.getSignature());
				ontoAxioms.remove(a);
			}
			Integer pos = null;
			for(OWLEntity ent : modSig){
				pos = mapping.get(ent);
				if(pos != null) break;
			}

			if(pos == null){
				modSizes.add(moduleRBME.size());
				modSig.forEach(x -> mapping.put(x, modSizes.size() - 1));
			}
			else{
				if(modSizes.get(pos) < moduleRBME.size()){
					modSizes.set(pos, moduleRBME.size());
					for(OWLEntity ent : modSig){
						mapping.put(ent, pos);
					}
				}
			}
		}

		double avg_rbme = 0;
		for(Integer i : modSizes){
			avg_rbme += i;
			out.append("size: " + i + "\n");
		}
		System.out.println(out);
		System.out.println("modules found: " + modSizes.size());
		System.out.println("total axioms: " + avg_rbme);
		System.out.println("rbmemodule avg: " + (avg_rbme / modSizes.size()));
		System.out.println("missing axioms: " + ontoAxioms.size());
		ontoAxioms.forEach(x -> System.out.println(x));*/
	}
	
	private void printModule(Set<OWLAxiom> module){
		System.out.println("module:");
		for(OWLAxiom a : module){
			System.out.println(ClassPrinter.printAxiom(a));
		}
	}
	
	private boolean isSuperSet(Set<OWLAxiom> set1, Set<OWLAxiom> set2){
		for(OWLAxiom a : set2){
			if(!set1.contains(a)){
				return false;
			}
		}
		return true;
	}
}
