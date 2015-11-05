package de.spellmaker.rbme;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import de.spellmaker.rbme.evaluation.OntologieData;
import de.spellmaker.rbme.evaluation.WorkerThread;

public class Evaluation {	
	
	public static void main(String[] args) throws Exception{
		List<File> ontologies = new ArrayList<>();
		//add all ontologies
		//ontologies.add(new File(OntologiePaths.contest1));
		//ontologies.add(new File("onto.owl"));
		System.out.println("[INFO] Adding ore el ontologies");
		ontologies.addAll(ORELoader.getEL_ORE(args[0]));
		System.out.println("[INFO] Collected " + ontologies.size() + " ontologies");
		
		//set test iteration values
		int iteration_count = 1000;
		
		int max_onto = 1000; //ontologies.size();
		int max_axioms = 100000;
		int min_axioms = 10000;
		
		ExecutorService pool = Executors.newFixedThreadPool(2);
		List<Future<OntologieData>> futures = new ArrayList<>(max_onto);
		for(int i = 0; i < max_onto; i++){
			WorkerThread current = new WorkerThread(iteration_count, ontologies.get(i), false, min_axioms, max_axioms);
			futures.add(pool.submit(current));
		}
	
		List<Future<OntologieData>> finished = new ArrayList<>(max_onto);
		boolean hasUnfinished = true;
		while(hasUnfinished){
			hasUnfinished = false;
			for(int i = 0; i < futures.size(); i++){
				Future<OntologieData> f = futures.get(i);
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
		
		StringBuilder result = new StringBuilder();
		//create ontologie data table
		result.append("iri;axiomCount;loadTime;ruleGenTime;owlapi_instTime;passedCorrectnessOWLAPI;passedCorrectnessRBME;passedSize;owlapi_time;rbme_time;iterations;file\n");
		for(Future<OntologieData> f : finished){
			OntologieData data = f.get();
			if(data == null) continue;
			result.append(data.iri).append(";").append(data.axiomCount).append(";");
			result.append(data.loadTime).append(";").append(data.ruleGenTime).append(";");
			result.append(data.owlapi_instTime).append(";");
			result.append(data.passedCorrectnessOWLAPI).append(";");
			result.append(data.passedCorrectnessRBME).append(";");
			result.append(data.passedSize).append(";");
			result.append(data.owlapi_result).append(";");
			result.append(data.rbme_result).append(";");
			result.append(data.iterations).append(";");
			result.append(data.file).append("\n");
		}
		result.append("\n");
		
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
		System.out.println("[INFO] evaluation finished");
	}

}
