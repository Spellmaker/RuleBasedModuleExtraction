package de.spellmaker.rbme.evaluation;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class EvaluationMain {

	/**
	 * 
	 * @param args:
	 * args[0]: Test selector, options are rule-size, module-size, rand-time, rule-gen
	 * remainder: options
	 * -f <list> list of ontologies
	 * -o <list> list of options
	 */
	public static void main(String[] args) {
		if(args.length <= 0){
			System.out.println("ERROR: No evaluation name provided");
			System.exit(0);
		}
		EvaluationCase ec = null;
		if(args[0].equals("rule-size")){
			ec = new RuleSizeComparison();
		}
		else if(args[0].equals("module-size")){
			ec = new TestModuleSizes();
		}
		else if(args[0].equals("rand-time")){
			ec = new TestRandTimes();
		}
		else if(args[0].equals("rule-gen")){
			ec = new TestRuleGeneration();
		}
		else if(args[0].equals("current")){
			ec = new CurrentEvaluation();
		}
		
		List<File> ontologies = new LinkedList<>();
		List<String> options = new LinkedList<>();
		
		if(args.length <= 1){
			System.out.println("WARNING: No file list or options provided");
		}
		else{
			int mode = 0;
			for(int i = 1; i < args.length; i++){
				if(args[i].equals("-f")){
					mode = 1;
				}
				else if(args[i].equals("-o")){
					mode = 2;
				}
				else if(mode == 1){
					ontologies.add(new File(args[i]));
				}
				else if(mode == 2){
					options.add(args[i]);
				}
			}
		}
		try {
			ec.evaluate(ontologies, options);
		}
		catch(Exception e){
			System.out.println("evaluation failed");
			e.printStackTrace();
		}
	}

}
