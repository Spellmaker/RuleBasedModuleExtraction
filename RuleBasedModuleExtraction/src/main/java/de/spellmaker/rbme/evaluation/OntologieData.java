package de.spellmaker.rbme.evaluation;

public class OntologieData {
	public String iri;
	public String file;
	public long axiomCount;
	public long loadTime;
	public long ruleGenTime;
	public long owlapi_instTime;
	public boolean passedCorrectnessOWLAPI;
	public boolean passedCorrectnessRBME;
	public boolean passedSize;
	public int iterations;
	public long owlapi_result;
	public long rbme_result;
}
