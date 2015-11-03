package de.spellmaker.rbme;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
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

import de.spellmaker.rbme.evaluation.OntologieData;
import de.spellmaker.rbme.extractor.RBMExtractor;
import de.spellmaker.rbme.rule.ELRuleBuilder;
import de.spellmaker.rbme.rule.RuleSet;
import de.spellmaker.rbme.test.TestSuite;
import de.spellmaker.rbme.util.ClassPrinter;
import de.spellmaker.rbme.util.ModuleCheck;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class Main {
	public static String onto_path = "onto.owl";//"EL-GALEN.owl";//;
	public static String onto_testpath = "C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\files\\approximated_896c66df-2415-4e7a-8a3e-aed1f56be49d_ine_roller.ttl_functional.owl";
	private final static boolean doChecks = true;
	public static void main(String[] args) throws Exception{
		
		List<File> ontologies = new ArrayList<>();
		//add all ontologies
		//ontologies.add(new File(OntologiePaths.contest1));
		//ontologies.add(new File("onto.owl"));
		System.out.println("[INFO] Adding ore el ontologies");
		ontologies.addAll(getEL_ORE("C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\"));
		System.out.println("[INFO] Collected " + ontologies.size() + " ontologies");
		
		//set test iteration values
		int min_iter = 100;//1000;
		int max_iter = 1000;//10000;
		int step_iter = 50;//100;
		
		int max_onto = 10; //ontologies.size();
		
		//derived values
		int values = (max_iter - min_iter) / step_iter;
		
		//initialize evaluation data structures
		OntologieData[] ontoData = new OntologieData[ontologies.size()];
		long owlapi_results[][] = new long[ontologies.size()][values];
		long rbme_results[][] = new long[ontologies.size()][values];
		long startTime = 0;
		long endTime = 0;

		ModuleCheck mCheck = null;
		
		for(int index = 0; index < max_onto; index++){
			OWLOntologyManager m = OWLManager.createOWLOntologyManager();
			if(doChecks) mCheck = new ModuleCheck(m);
			System.out.println("[INFO] processing ontologie '" + ontologies.get(index) + "'");
			OntologieData odata = new OntologieData();
			ontoData[index] = odata;
			startTime = System.currentTimeMillis();
			OWLOntology ontology = m.loadOntologyFromOntologyDocument(ontologies.get(index));
			endTime = System.currentTimeMillis();
			
			odata.iri = ontology.getOntologyID().toString();
			odata.file = ontologies.get(index).toString();
			odata.loadTime = endTime - startTime;
			odata.axiomCount = ontology.getAxiomCount();
			
			odata.passedCorrectnessOWLAPI = true;
			odata.passedCorrectnessRBME = true;
			odata.passedSize = true;
			
			List<OWLClass> ontologySignature = new ArrayList<>();
			ontology.getSignature().stream().filter(x -> x instanceof OWLClass).forEach(x -> ontologySignature.add((OWLClass)x));
			
			startTime = System.currentTimeMillis();
			RuleSet ruleSet = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
			endTime = System.currentTimeMillis();
			
			odata.ruleGenTime = endTime - startTime;
			
			startTime = System.currentTimeMillis();
			SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
			endTime = System.currentTimeMillis();
			
			odata.owlapi_instTime = endTime - startTime;
			
			//check the module extractions on correctness?
			if(doChecks){
				System.out.println("[INFO] performing correctness and size tests");
				for(int i = 0; i < ontologySignature.size(); i++){
					OWLClass element = ontologySignature.get(i);
					Set<OWLClass> sign = new HashSet<>();
					sign.add(element);
					Set<OWLAxiom> moduleOWLAPI = extractor.extract(Collections.unmodifiableSet(sign));
					Set<OWLAxiom> moduleRBME = (new RBMExtractor()).extractModule(ruleSet, sign);
					
					//check general correctness
					if(mCheck.isSemanticalLocalModule(ontology, moduleOWLAPI) != null){
						System.out.println("[WARN] OWLAPI produced a not semantically local module for ontologie " + ontologies.get(index));
						odata.passedCorrectnessOWLAPI = false;
					}
					if(mCheck.isSyntacticalLocalModule(ontology, moduleOWLAPI) != null){
						System.out.println("[WARN] OWLAPI produced a not syntactically local module for ontologie " + ontologies.get(index));
						odata.passedCorrectnessOWLAPI = false;
					}
					if(mCheck.isSemanticalLocalModule(ontology, moduleRBME) != null){
						System.out.println("[WARN] rbme produced a not semantically local module for ontologie " + ontologies.get(index));
						odata.passedCorrectnessRBME = false;
					}
					if(mCheck.isSyntacticalLocalModule(ontology, moduleRBME) != null){
						System.out.println("[WARN] rbme produced a not syntactically local module for ontologie " + ontologies.get(index));
						odata.passedCorrectnessRBME = false;
					}
					
					//check size
					if(moduleOWLAPI.size() < moduleRBME.size()){
						System.out.println("[WARN] rbme produced a bigger module than the owl api for ontologie " + ontologies.get(index));
						odata.passedSize = false;
					}
				}
			}
			
			System.out.println("[INFO] processing owlapi");
			for(int iteration = min_iter; iteration < max_iter; iteration += step_iter){
				startTime = System.currentTimeMillis();
				for(int i = 0; i < ontologySignature.size(); i++){
					OWLClass element = ontologySignature.get(i);
					Set<OWLEntity> sign = new HashSet<>();
					sign.add(element);
					for(int loop = 0; loop < iteration; loop++){
						extractor.extract(sign);
					}
				}
				endTime = System.currentTimeMillis();
				
				owlapi_results[index][(iteration - min_iter) / step_iter] = endTime - startTime;
			}
			
			System.out.println("[INFO] processing rbme");
			for(int iteration = min_iter; iteration < max_iter; iteration += step_iter){
				startTime = System.currentTimeMillis();
				for(int i = 0; i < ontologySignature.size(); i++){
					OWLClass element = ontologySignature.get(i);
					Set<OWLClass> sign = new HashSet<>();
					sign.add(element);
					for(int loop = 0; loop < iteration; loop++){
						(new RBMExtractor()).extractModule(ruleSet, sign);
					}
				}
				endTime = System.currentTimeMillis();

				rbme_results[index][(iteration - min_iter) / step_iter] = endTime - startTime;
			}
		}
		
		System.out.println("[INFO] building data file");
		
		StringBuilder result = new StringBuilder();
		//create ontologie data table
		result.append("iri;axiomCount;loadTime;ruleGenTime;owlapi_instTime;passedCorrectnessOWLAPI;passedCorrectnessRBME;passedSize;file\n");
		for(int index = 0; index < max_onto; index++){
			result.append(ontoData[index].iri).append(";").append(ontoData[index].axiomCount).append(";");
			result.append(ontoData[index].loadTime).append(";").append(ontoData[index].ruleGenTime).append(";");
			result.append(ontoData[index].owlapi_instTime).append(";");
			result.append(ontoData[index].passedCorrectnessOWLAPI).append(";");
			result.append(ontoData[index].passedCorrectnessRBME).append(";");
			result.append(ontoData[index].passedSize).append(";");
			result.append(ontoData[index].file).append("\n");
		}
		result.append("\n");
		//create measurements table
		result.append("iteration;");
		for(int index = 0; index < max_onto; index++){
			result.append("owlapi time (").append(index).append(");");
			result.append("rbme time (").append(index).append(");");
		}
		result.append("\n");
		for(int i = 0; i < values; i++){
			result.append(min_iter + i * step_iter).append(";");
			for(int index = 0; index < max_onto; index++){
				result.append(owlapi_results[index][i]).append(";").append(rbme_results[index][i]).append(";");
			}
			result.append("\n");
		}
		
		
		BufferedWriter bw = null;
		while(true){
			try{
				bw = new BufferedWriter(new FileWriter(new File("out.csv")));
				bw.write(result.toString());
				bw.close();
				System.out.println("[INFO] Output written to 'out.csv'");
				break;
			}
			catch(IOException e){
				bw.close();
				System.out.println("[ERROR] Could not write output file. Enter 'q' to quit or 'r' to try again");
				Scanner scan = new Scanner(System.in);
				if(scan.nextLine().equals("q")){
					break;
				}
				else{
					System.out.println("[INFO] retrying...");
				}
				scan.close();
			}
		}
		System.out.println("[INFO] terminated");
		
		//ModuleExtractionTest test = new ModuleExtractionTest();
		//test.TestModuleExtraction();
		/*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
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
		for(OWLAxiom o : module){
			if(!module2.remove(o)){
				System.out.println("axiom '" + ClassPrinter.printAxiom(o) + "' is not in locality module");
			}
		}
		System.out.println("Missing axioms:");
		for(OWLAxiom o: module2){
			System.out.println(ClassPrinter.printAxiom(o) + " " + o.getClass());
		}
		
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
		*/
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
	
	private static List<File> getEL_ORE(String basepath){
		Path list = Paths.get("orefiles.txt");
		if(Files.exists(list)){	
			System.out.println("[INFO] Reading test files from 'orefiles.txt'");
			BufferedReader br = null;
			try{
				br = new BufferedReader(new FileReader(list.toFile()));
				List<File> result = new LinkedList<>();
				String s = br.readLine();
				while(s != null){
					result.add(new File(s));
					s = br.readLine();
				}
				br.close();
				return result;
			}
			catch(Exception e){
				if(br != null)
					try {
						br.close();
					} catch (IOException e1) {
						//this should never happen
						e1.printStackTrace();
					}
			}
		}
		System.out.println("[INFO] Could not access 'orefiles.txt', rederiving file list");
		Path ore_folder = Paths.get(basepath);
		Path el = ore_folder.resolve("el");
		File el_class = el.resolve("classification").resolve("fileorder.txt").toFile();
		File el_cons = el.resolve("consistency").resolve("fileorder.txt").toFile();
		File el_instantiation = el.resolve("instantiation").resolve("fileorder.txt").toFile();
				
		Set<File> files = new HashSet<>();
		try{
			System.out.println("[INFO] resolving classification files");
			getFiles(ore_folder.resolve("files"), el_class, files);
		}
		catch(IOException e){
			System.out.println("[WARN] Could not gather files from classification");
		}
		try{
			System.out.println("[INFO] resolving consistency files");
			getFiles(ore_folder.resolve("files"), el_cons, files);
		}
		catch(IOException e){
			System.out.println("[WARN] Could not gather files from consistency");
		}
		try{
			System.out.println("[INFO] resolving instantiation files");
			getFiles(ore_folder.resolve("files"), el_instantiation, files);
		}
		catch(IOException e){
			System.out.println("[WARN] Could not gather files from instantiation");
		}
		List<File> result = new LinkedList<>();
		result.addAll(files);
		
		//store list
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(list.toFile()));
			for(File f : result){
				bw.write(f.toString() + "\n");
			}
			System.out.println("[INFO] Stored file list in 'orefiles.txt'");
		}
		catch(IOException e){
			System.out.println("[WARN] Could not store file list in 'orefiles.txt'");
		}
		finally{
			if(bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					//this should never happen
					e.printStackTrace();
				}
		}
		return result;
	}
	
	private static void getFiles(Path filePath, File fileorder, Set<File> files) throws FileNotFoundException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileorder));
		String line = br.readLine();
		while(line != null){
			files.add(filePath.resolve(line).toFile());
			line = br.readLine();
		}
		br.close();
	}
}
