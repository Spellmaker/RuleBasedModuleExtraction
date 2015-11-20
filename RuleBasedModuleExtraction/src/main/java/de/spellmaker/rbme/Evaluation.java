package de.spellmaker.rbme;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import de.spellmaker.rbme.evaluation.ResultBuilder;
import de.spellmaker.rbme.evaluation.WorkerThread;
import de.spellmaker.rbme.ore.OREManager;

public class Evaluation {	
	
	public static void main(String[] args) throws Exception{
		/*
		 * args[0] = ore_path
		 * args[1] = ontologie filter
		 * args[2] = rule generation count
		 * args[3] = iteration count
		 * args[4] = file name
		 */
		OREManager manager = new OREManager();
		manager.load(Paths.get(args[0]), "el/consistency", "el/classification", "el/instantiation");//, "dl\\classification", "dl\\instantiation", "dl\\consistency");		
		
		List<File> ontologies = new ArrayList<>();
		System.out.println("[INFO] Adding ore el ontologies");
		ontologies.addAll(manager.filterOntologies(x -> Integer.parseInt(x[0]) < 1000, "logical_axiom_count"));
		System.out.println("[INFO] Collected " + ontologies.size() + " ontologies");
		StringBuilder result = ResultBuilder.buildResult(getData(ontologies, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])), manager, true, "logical_axiom_count", "ruleGenTime", "owlapi_instTime", "owlapi_result", "rbme_result", "rule_iterations", "iterations");
		handleOutput(result, args[4]);
		System.out.println("[INFO] Evaluation finished");
		
	
		/*int rule_gens = 100;
		int iteration_count = 1000;
		int max_onto = ontologies.size(); 
		
		StringBuilder result = ResultBuilder.buildResult(getData(ontologies, max_onto, 1, iteration_count), manager, true,
				"file", "logical_axiom_count", "abox_size", "ruleGenTime", "owlapi_instTime", "owlapi_result", "rbme_result", "rule_iterations", "iterations");
		handleOutput(result, "out1_2.csv");
		System.out.println("[INFO] Completed step 1");
		
		ontologies.clear();
		ontologies.addAll(manager.filterOntologies(x -> Integer.parseInt(x[0]) > 1000 && Integer.parseInt(x[0]) < 10000, "logical_axiom_count"));
		max_onto = ontologies.size(); 
		iteration_count = 100;
		result = ResultBuilder.buildResult(getData(ontologies, max_onto, 1, iteration_count), manager, false,
				"file", "logical_axiom_count", "abox_size", "ruleGenTime", "owlapi_instTime", "owlapi_result", "rbme_result", "rule_iterations", "iterations");
		handleOutput(result, "out2_2.csv");
		System.out.println("[INFO] Completed step 2");
		
		ontologies.clear();
		ontologies.addAll(manager.filterOntologies(x -> Integer.parseInt(x[0]) > 10000, "logical_axiom_count"));
		max_onto = ontologies.size(); 
		iteration_count = 10;
		result = ResultBuilder.buildResult(getData(ontologies, max_onto, 1, iteration_count), manager, false,
				"file", "logical_axiom_count", "abox_size", "ruleGenTime", "owlapi_instTime", "owlapi_result", "rbme_result", "rule_iterations", "iterations");
		
		handleOutput(result, "out3_2.csv");
		System.out.println("[INFO] evaluation finished");*/
	}
	
	private static List<Map<String, String>> getData(List<File> ontologies, int max_onto, int rule_gens, int iteration_count) throws Exception{
		List<Map<String, String>> data = new LinkedList<>();
		for(Future<Map<String, String>> f : runTest(ontologies, max_onto, rule_gens, iteration_count)){
			data.add(f.get());
		}
		return data;
	}

	private static List<Future<Map<String, String>>> runTest(List<File> ontologies, int max_onto, int rule_gens, int iteration_count){
		ExecutorService pool = Executors.newFixedThreadPool(8);
		List<Future<Map<String, String>>> futures = new ArrayList<>(max_onto);
		for(int i = 0; i < max_onto && i < ontologies.size(); i++){
			WorkerThread current = new WorkerThread(rule_gens, iteration_count, ontologies.get(i), false);
			futures.add(pool.submit(current));
		}		
		
		List<Future<Map<String, String>>> finished = new ArrayList<>(max_onto);
		boolean hasUnfinished = true;
		while(hasUnfinished){
			hasUnfinished = false;
			for(int i = 0; i < futures.size(); i++){
				Future<Map<String, String>> f = futures.get(i);
				if(f.isDone()){
					finished.add(f);
					futures.remove(i);
					i--;
					System.out.println("[INFO] (" + finished.size() + "/" + max_onto + ")");
				}
				else{
					hasUnfinished = true;
				}
			}
		}
		
		System.out.println("[INFO] All threads terminated");
		pool.shutdown();
		
		return finished;
	}
	
	private static void handleOutput(StringBuilder output, String fname) throws IOException{
		BufferedWriter bw = null;
		while(true){
			try{
				bw = new BufferedWriter(new FileWriter(new File(fname)));
				bw.write(output.toString());
				bw.close();
				System.out.println("[INFO] Output written to '" + fname + "'");
				break;
			}
			catch(IOException e){
				if(bw != null) bw.close();
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
	}
}
