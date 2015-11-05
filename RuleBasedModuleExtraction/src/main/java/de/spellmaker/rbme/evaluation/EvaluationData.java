package de.spellmaker.rbme.evaluation;

public class EvaluationData {
	public final OntologieData ontoData;
	public final long[] owlapi_results;
	public final long[] rbme_results;
	
	public EvaluationData(OntologieData ontoData, long[] owlapi_results, long[] rbme_results){
		this.ontoData = ontoData;
		this.owlapi_results = owlapi_results;
		this.rbme_results = rbme_results;
	}
}
