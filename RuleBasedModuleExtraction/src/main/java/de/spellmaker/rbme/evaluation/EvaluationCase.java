package de.spellmaker.rbme.evaluation;

import java.io.File;
import java.util.List;

public interface EvaluationCase {
	public void evaluate(List<File> ontologies, List<String> options) throws Exception;
}
