package de.spellmaker.rbme.evaluation;

import java.util.List;

public class ResultBuilder {
	public static StringBuilder buildResult(int min_iter, int step_iter, int max_iter, List<OntologieData> ontoData, List<long[]> owlapi_results, List<long[]> rbme_results){
		StringBuilder result = new StringBuilder();
		//create ontologie data table
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
	}
}
