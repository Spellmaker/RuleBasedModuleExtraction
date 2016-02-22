package de.spellmaker.rbme.mains;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.clarkparsia.owlapi.modularity.locality.LocalityEvaluator;

import de.spellmaker.rbme.OntologiePaths;
import de.spellmaker.rbme.extractor.CompressedExtractor;
import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.extractor.RBMExtractorNoDef;
import de.spellmaker.rbme.reachability.ReachabilityModuleExtractor;
import de.spellmaker.rbme.rule.BottomModeRuleBuilder;
import de.spellmaker.rbme.rule.CompressedRule;
import de.spellmaker.rbme.rule.CompressedRuleBuilder;
import de.spellmaker.rbme.rule.CompressedRuleSet;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.util.ClassPrinter;
import de.spellmaker.rbme.util.TerminologyCheck;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class SimpleExtract {
	public static void main(String[] args) throws Exception{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.galen));
		
		RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		CompressedRuleSet crs = (new CompressedRuleBuilder()).buildRules(ontology.getAxioms());
		System.out.println("normal ruleset: " + rs.size() + " rules");
		System.out.println("compressed ruleset: " + crs.size() + " rules");
		/*for(OWLEntity e : ontology.getSignature()){
			if(e.toString().equals("<http://chen.moe/onto/med/Cystic_Fibrosis>") ||
					e.toString().equals("<http://chen.moe/onto/med/Genetic_Disorder>")){
				sign.add(e);
			}
			System.out.println(e);
		}*/
		
		
		/*for(OWLEntity e : ontology.getSignature()){
			if(e.toString().equals("<http://www.co-ode.org/ontologies/galen#Fluorouracil>")){
				sign.add(e); break;
			}
		}
		
		RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
		CompressedExtractor compr = new CompressedExtractor();
		
		Set<OWLAxiom> m1 = rbme.extractModule(rs, sign);
		Set<OWLAxiom> m2 = compr.extractModule(crs, sign);
		System.out.println("uncompressed:");
		System.out.println(m1.size());
		System.out.println("compressed:");
		System.out.println(m2.size());*/
		
		List<OWLEntity> entityList = new ArrayList<>();
		for(OWLEntity e : ontology.getSignature()){
			if(!(e instanceof OWLClass)) continue;
			entityList.add(e);
		}
		
		Set<Set<OWLEntity>> signatures = new HashSet<>();
		Random r = new Random();
		for(int i = 0; i < 1000; i++){
			Set<OWLEntity> nsig = new HashSet<>();
			nsig.add(entityList.get(r.nextInt(entityList.size())));
			signatures.add(nsig);
		}
		
		//correctness
		for(Set<OWLEntity> e : signatures){					
			Set<OWLAxiom> m1 = (new RBMExtractorNoDef(false)).extractModule(rs, e);
			Set<OWLAxiom> m2 = (new CompressedExtractor()).extractModule(crs, e);
			
			for(OWLAxiom a : m2){
				if(!m1.contains(a)){
					System.out.println("compressed has additional axiom " + ClassPrinter.printAxiom(a));
				}
			}
			for(OWLAxiom a : m1){
				if(!m2.contains(a)){
					System.out.println("compressed is missing axiom " + ClassPrinter.printAxiom(a));
				}
			}
		}
		
		//time
		long start, end;
		RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
		start = System.currentTimeMillis();
		for(Set<OWLEntity> e : signatures){
			Set<OWLAxiom> m1 = rbme.extractModule(rs, e);
		}
		end = System.currentTimeMillis();
		System.out.println("rbme: " + (end - start));
		
		CompressedExtractor compressed = new CompressedExtractor();
		start = System.currentTimeMillis();
		for(Set<OWLEntity> e : signatures){
			Set<OWLAxiom> m1 = compressed.extractModule(crs, e);
		}
		end = System.currentTimeMillis();
		System.out.println("compressed: " + (end - start));
		
		
		/*ReachabilityModuleExtractor extr = new ReachabilityModuleExtractor(ontology);
		RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		RBMExtractor rbme = new RBMExtractor(false, false);
		
		for(OWLEntity e : ontology.getSignature()){
			Set<OWLEntity> signature = new HashSet<>();
			signature.add(e);
			Set<OWLAxiom> m1 = extr.extractModule(signature);
			Set<OWLAxiom> m2 = rbme.extractModule(rs, signature);
			
			if(!m1.containsAll(m2) || !m2.containsAll(m1)){
				System.out.println("found mismatch for entity " + e);
				System.out.println("rbme module size: " + m2.size() + " reachability module size: " + m1.size());
				if(!m1.containsAll(m2)){
					System.out.println("axioms missing from reachability module: ");
					for(OWLAxiom a : m2){
						if(a instanceof OWLDeclarationAxiom) continue;
						if(!m1.contains(a)) System.out.println(ClassPrinter.printAxiom(a));
					}
				}
				if(!m2.containsAll(m1)){
					System.out.println(" * axioms missing from rbme module: ");
					for(OWLAxiom a : m1){
						if(a instanceof OWLDeclarationAxiom) continue;
						if(!m2.contains(a)) System.out.println(ClassPrinter.printAxiom(a));
					}
				}
				
				System.out.println(" * reachability module is: ");
				for(OWLAxiom a : m1){
					System.out.println(ClassPrinter.printAxiom(a));
				}
				System.out.println(" * rbme module is: ");
				for(OWLAxiom a : m2){
					System.out.println(ClassPrinter.printAxiom(a));
				}
			}
		}
		*/
		
		
		/*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		System.out.println("loading ontologie");
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File("EL-GALEN.owl"));
		
		System.out.println(ontology.getSignature());
		
		RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		RBMExtractor rbme = new RBMExtractor(false, true);
		
		Set<OWLEntity> sign = new HashSet<>();
		for(OWLEntity e : ontology.getSignature()){
			if(e.toString().equals("<http://www.co-ode.org/ontologies/galen#Fluorouracil>")){
				sign.add(e); break;
			}
		}
		Set<OWLAxiom> module = rbme.extractModule(rs, sign);
		System.out.println(module.size());
		int cnt = 0;
		for(OWLAxiom a : module) if(a instanceof OWLDeclarationAxiom) cnt++;
		System.out.println(cnt + " decl axioms");
		RBMExtractor rbme2 = new RBMExtractor(true, true);
		module = rbme2.extractModule(rs, sign);
		System.out.println(module.size());
		cnt = 0;
		for(OWLAxiom a : module) if(a instanceof OWLDeclarationAxiom) cnt++;
		System.out.println(cnt + " decl axioms");*/
	}
}
