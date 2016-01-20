package de.spellmaker.rbme.mains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestModuleSizes {
	public static List<Integer> readGenerating(String file) throws Exception{
		List<Integer> generating = new LinkedList<>();
		if(Files.exists(Paths.get(file))){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = "";
			while((s = br.readLine()) != null){
				if(!s.equals("")){
					generating.add(Integer.parseInt(s));
				}
			}
			br.close();
		}
		return generating;
	}
	
	public static void main(String[] args) throws Exception{
		//determine ontologies to test
		//OREManager ore = new OREManager();
		//ore.load(Paths.get(args[0]), "el/consistency", "el/classification", "el/instantiation");	
		List<File> files = new LinkedList<>();// = ore.filterOntologies(new ORESizeFilter(0, 100, 500), "logical_axiom_count");
		files.add(new File("EL-GALEN.owl"));
		//files.add(new File("snomedStated_INT_20140731.owl"));
		
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(3);
		List<Future<Double[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(mainPool.submit(new ModuleSizeWorker(files.get(i), extractorPool, i)));
		}
		int finished = 0;
		boolean terminated = false;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Double[]> f = futures.get(i);
				if(f.isDone()){
					finished++;
					futures.remove(i);
					i--;
					System.out.println("finished task (" + finished + "/" + files.size() + ")");
					makeOutput(f.get());
				}
			}
			if(futures.size() <= 0){
				System.out.println("thread pool terminated");
				terminated = true;
			}
		}
		mainPool.shutdown();
		extractorPool.shutdown();
	}
	
	public static void makeOutput(Double[] res){
		System.out.println("biggest module without optimization: " + res[0]);
		System.out.println("biggest module with optimization: " + res[1]);
		double percent = 100 - (res[1] * 100) / res[0];
		System.out.println("percent reduction: " + percent);
		System.out.println("avg module size without optimization: " + res[2]);
		System.out.println("avg module size with optimization: " + res[3]);
		percent = 100 - (res[3] * 100) / res[2];
		System.out.println("percent reduction: " + percent);
	}
	
	/*public static List<Set<Integer>> definedAxioms = null;
	public static List<Set<Integer>> signatures = null;
	public static List<Integer> indices = null;
	
	public static List<Set<Integer>> getModules(OWLOntology ontology, RuleSet ruleSet, boolean doDefinitions, List<Integer> generating){
		return getModules(ontology, ruleSet, doDefinitions);
		/*List<OWLEntity> sign = new ArrayList<>(ontology.getSignature());
		List<Set<Integer>> moduleList = new ArrayList<>(generating.size());
		signatures = new ArrayList<>(generating.size());
		definedAxioms = new ArrayList<>(generating.size());
		//int cnt = 0;
		for(Integer s_i : generating){
			//cnt++;
			OWLEntity e = sign.get(s_i);
			//if(cnt % 100 == 0) System.out.println("(" + cnt + "/" + generating.size() + ")");
			Set<OWLEntity> currentSignature = new HashSet<>();
			Set<Integer> currentSignatureInt = new HashSet<>();
			currentSignature.add(e);
			currentSignatureInt.add(ruleSet.putObject(e));
			RBMExtractor rbme = new RBMExtractor(doDefinitions);
			Set<OWLAxiom> currentModule = new HashSet<>(rbme.extractModule(ruleSet, currentSignature));
			Set<Integer> currentModuleInt = currentModule.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet());
			Set<Integer> defined = new HashSet<>(rbme.getDefinedAxioms());
			
			definedAxioms.add(defined);
			signatures.add(currentSignatureInt);
			moduleList.add(currentModuleInt);
		}
		return moduleList;		* /
	}
	
	public static List<Set<Integer>> getModules(OWLOntology ontology, RuleSet ruleSet, boolean doDefinitions){
		List<OWLEntity> sign = new ArrayList<>(ontology.getSignature());//.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet());
		definedAxioms = new ArrayList<>();
		List<Set<Integer>> moduleList = new ArrayList<>();
		List<Set<Integer>> tmpSignatures = new ArrayList<>();
		List<Set<Integer>> tmpDefinedAxioms = new ArrayList<>();
		Set<OWLEntity> takenCare = new HashSet<>();
		
		indices = new LinkedList<>();

		for(int s_i = 0; s_i < sign.size(); s_i++){
			OWLEntity e = sign.get(s_i);
			//System.out.println("(" + s_i + "/" + sign.size() + ")");
			if(takenCare.contains(e)) continue;
			
			if(!(e instanceof OWLClass) &&!(e instanceof OWLObjectProperty)) continue;
			
			Set<OWLEntity> currentSignature = new HashSet<>();
			currentSignature.add(e);
			Set<Integer> currentSignatureInt = new HashSet<>();
			currentSignatureInt.add(ruleSet.putObject(e));
			
			RBMExtractor rbme = new RBMExtractor(doDefinitions);
			Set<OWLAxiom> currentModule = new HashSet<>(rbme.extractModule(ruleSet, currentSignature));
			Set<Integer> currentModuleInt = currentModule.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet());
			
			//build defined axiom list
			Set<Integer> defined = new HashSet<>(rbme.getDefinedAxioms());//.stream().map(x -> (OWLAxiom) ruleSet.lookup(x)).collect(Collectors.toSet()); 
			
			boolean append = true;
			for(int i = 0; i < moduleList.size(); i++){
				Set<Integer> otherModule = moduleList.get(i);
				if(otherModule == null) continue;
				
				if(otherModule.containsAll(currentModuleInt)){
					append = false;
					break;
				}
				if(currentModuleInt.containsAll(otherModule)){
					moduleList.set(i, null);
					tmpDefinedAxioms.set(i, null);
					tmpSignatures.set(i, null);
					indices.set(i, null);
				}
			}
			
			if(append){
				currentModule.forEach(x -> takenCare.addAll(x.getSignature()));
				moduleList.add(currentModuleInt);
				tmpDefinedAxioms.add(defined);
				tmpSignatures.add(currentSignatureInt);
				indices.add(s_i);
			}
		}
		
		signatures = new ArrayList<>();
		definedAxioms = new ArrayList<>();
		
		//compact result list
		List<Set<Integer>> result = new ArrayList<>();
		for(int i = 0; i < moduleList.size(); i++){
			if(moduleList.get(i) == null) continue;
			result.add(moduleList.get(i));//.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet()));
			signatures.add(tmpSignatures.get(i));//.stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet()));
			definedAxioms.add(tmpDefinedAxioms.get(i));//.get(i).stream().map(x -> ruleSet.putObject(x)).collect(Collectors.toSet()));
		}	
		
		List<Integer> final_indices = new LinkedList<>();
		for(Integer i : indices) 
			if(i != null) 
				final_indices.add(i);
		Collections.sort(final_indices);
		indices = final_indices;
		return result;
	}*/
}
