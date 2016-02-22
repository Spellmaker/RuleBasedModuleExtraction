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

import de.spellmaker.rbme.mains.workers.RandTimeWorker;

public class TestRandTimes {
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
		RandTimeWorker.entities = Integer.parseInt(args[0]);
		RandTimeWorker.iterations = Integer.parseInt(args[1]);
		//determine ontologies to test
		//OREManager ore = new OREManager();
		//ore.load(Paths.get(args[0]), "el/consistency", "el/classification", "el/instantiation");	
		List<File> files = new LinkedList<>();// = ore.filterOntologies(new ORESizeFilter(0, 100, 500), "logical_axiom_count");
		files.add(new File(args[2]));
		//files.add(new File(OntologiePaths.medical));
		//files.add(new File("snomedStated_INT_20140731.owl"));
		
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(mainPool.submit(new RandTimeWorker(files.get(i), extractorPool, i)));
		}
		int finished = 0;
		boolean terminated = false;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Long[]> f = futures.get(i);
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
	
	public static void makeOutput(Long[] res){
		Long largest = 0L;
		for(Long l : res){
			if(l > largest) largest = l;
		}
		
		System.out.println("OWLAPI:\t\t" + fill(res[0], largest));
		System.out.println("FAME EL ND:\t" + fill(res[1], largest));
		System.out.println("FAME M ND:\t" + fill(res[2], largest));
		System.out.println("FAME EL D:\t" + fill(res[3], largest));
		System.out.println("FAME M D:\t" + fill(res[4], largest));
		System.out.println("FAMEND EL:\t" + fill(res[5], largest));
		System.out.println("FAMEND M:\t" + fill(res[6], largest));
	}

	public static String fill(long number, long digits){
		String res = "" + number;
		String comp = "" + digits;
		while(res.length() < comp.length()){
			res = " " + res;
		}
		return res;
	}
}
