package de.spellmaker.rbme.evaluation;

import java.util.List;
import java.util.Map;

import de.spellmaker.rbme.ore.OREManager;

public class ResultBuilder {	
	public static StringBuilder buildResult(List<Map<String, String>> data, OREManager manager, String...labels){
		StringBuilder result = new StringBuilder();
		result.append(labels[0]);
		for(int i = 1; i < labels.length; i++){
			result.append(";").append(labels[i]);
		}
		result.append("\n");
		for(Map<String, String> line : data){
			String fname = line.get("file");
			
			String field = line.get(labels[0]);
			if(field == null) field = manager.getMetadata(fname, labels[0]);
			
			result.append(field);
			for(int i = 1; i < labels.length; i++){
				field = line.get(labels[i]);
				if(field == null) field = manager.getMetadata(fname, labels[i]);
				result.append(";").append(field);
			}
			result.append("\n");
		}
		
		return result;
		
		/*//create ontologie data table
		result.append("iri;axiomCount;loadTime;ruleGenTime;owlapi_instTime;passedCorrectnessOWLAPI;passedCorrectnessRBME;passedSize;file\n");
		for(int index = 0; index < ontoData.size(); index++){
			result.append(ontoData.get(index).iri).append(";").append(ontoData.get(index).axiomCount).append(";");
			result.append(ontoData.get(index).loadTime).append(";").append(ontoData.get(index).ruleGenTime).append(";");
			result.append(ontoData.get(index).owlapi_instTime).append(";");
			result.append(ontoData.get(index).passedCorrectnessOWLAPI).append(";");
			result.append(ontoData.get(index).passedCorrectnessRBME).append(";");
			result.append(ontoData.get(index).passedSize).append(";");
			result.append(ontoData.get(index).file).append("\n");
		}
		result.append("\n");
		//create measurements table
		result.append("iteration;");
		for(int index = 0; index < ontoData.size(); index++){
			result.append("owlapi time (").append(index).append(");");
			result.append("rbme time (").append(index).append(");");
		}
		result.append("\n");
		int values = (max_iter - min_iter) / step_iter;
		for(int i = 0; i < values; i++){
			result.append(min_iter + i * step_iter).append(";");
			for(int index = 0; index < ontoData.size(); index++){
				result.append(owlapi_results.get(index)[i]).append(";").append(rbme_results.get(index)[i]).append(";");
			}
			result.append("\n");
		}
		return result;
		*/
	}
}
