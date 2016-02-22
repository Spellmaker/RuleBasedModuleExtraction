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

import de.spellmaker.rbme.mains.workers.ModuleSizeWorker;

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
		//files.add(new File("EL-GALEN.owl"));
		files.add(new File("snomedStated_INT_20140731.owl"));
		
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);
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
}
