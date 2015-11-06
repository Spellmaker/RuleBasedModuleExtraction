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
		OREManager manager = new OREManager();
		manager.load(Paths.get(args[0]), "el\\consistency", "el\\classification", "el\\instantiation");//, "dl\\classification", "dl\\instantiation", "dl\\consistency");		
		
		List<File> ontologies = new ArrayList<>();
		System.out.println("[INFO] Adding ore el ontologies");
		ontologies.addAll(manager.filterOntologies(x -> Integer.parseInt(x[0]) < 150, "logical_axiom_count"));
		System.out.println("[INFO] Collected " + ontologies.size() + " ontologies");
	
		int iteration_count = 1000;
		int max_onto = 10; 
		
		List<Map<String, String>> data = new LinkedList<>();
		for(Future<Map<String, String>> f : runTest(ontologies, max_onto, iteration_count)){
			data.add(f.get());
		}
		
		StringBuilder result = ResultBuilder.buildResult(data, manager, 
				"file", "logical_axiom_count", "abox_size", "rbme_result", "owlapi_result");
		
		handleOutput(result);
		System.out.println("[INFO] evaluation finished");
	}

	private static List<Future<Map<String, String>>> runTest(List<File> ontologies, int max_onto, int iteration_count){
		System.out.println("method run");
		ExecutorService pool = Executors.newFixedThreadPool(4);
		List<Future<Map<String, String>>> futures = new ArrayList<>(max_onto);
		for(int i = 0; i < max_onto && i < ontologies.size(); i++){
			WorkerThread current = new WorkerThread(iteration_count, ontologies.get(i), false);
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
	
	private static void handleOutput(StringBuilder output) throws IOException{
		BufferedWriter bw = null;
		while(true){
			try{
				bw = new BufferedWriter(new FileWriter(new File("out.csv")));
				bw.write(output.toString());
				bw.close();
				System.out.println("[INFO] Output written to 'out.csv'");
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
