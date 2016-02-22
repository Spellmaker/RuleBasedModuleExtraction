package de.spellmaker.rbme.mains;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.spellmaker.rbme.mains.workers.RuleGenerationWorker;

public class TestRuleGeneration {
	public static void main(String[] args) throws Exception{
		//determine ontologies to test
		//OREManager ore = new OREManager();
		//ore.load(Paths.get(args[0]), "el/consistency", "el/classification", "el/instantiation");	
		List<File> files = new LinkedList<>();// = ore.filterOntologies(new ORESizeFilter(0, 100, 500), "logical_axiom_count");
		//files.add(new File("EL-GALEN.owl"));
		files.add(new File("EL-GALEN.owl"));
		files.add(new File("snomedStated_INT_20140731.owl"));
		
		int iterations = 1000;
		System.out.println("performing " + iterations  + " iterations of rule generation");
		RuleGenerationWorker.iterations = iterations;
		//setup threads
		ExecutorService genPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(genPool.submit(new RuleGenerationWorker(files.get(i), i)));
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
					System.out.println("Rulegen Time ELRules: " + f.get()[0] + " Time ModeRules: " + f.get()[1]);
				}
			}
			if(futures.size() <= 0){
				System.out.println("thread pool terminated");
				terminated = true;
			}
		}
		genPool.shutdown();
	}
}
