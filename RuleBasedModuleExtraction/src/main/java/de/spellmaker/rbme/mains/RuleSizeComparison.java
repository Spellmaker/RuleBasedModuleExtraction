package de.spellmaker.rbme.mains;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.spellmaker.rbme.mains.workers.RuleSizeWorker;
import de.spellmaker.rbme.ore.OREManager;
import de.spellmaker.rbme.ore.ORENoFilter;

public class RuleSizeComparison {
	public static void main(String[] args) throws Exception{
		List<File> files = new LinkedList<>();
		OREManager ore = new OREManager();
		ore.load(Paths.get("C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset"), "el/consistency", "el/classification", "el/instantiation");	
		files.addAll(ore.filterOntologies(new ORENoFilter(), 0));
		//files.add(new File("EL-GALEN.owl"));
		
		ExecutorService mainPool = Executors.newFixedThreadPool(5);
		List<Future<Integer[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(mainPool.submit(new RuleSizeWorker(files.get(i))));
		}
		int finished = 0;
		boolean terminated = false;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Integer[]> f = futures.get(i);
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
	}
	
	public static void makeOutput(Integer[] res){
		System.out.println("size: " + res[0] + " elrules: " + res[1] + " bmrules: " + res[2]);
	}
}
