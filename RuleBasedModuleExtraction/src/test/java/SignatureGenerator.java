
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

public class SignatureGenerator {
	public static int testSet(SignatureTest tc, Set<OWLClass> currentSet, List<OWLClass> sourceSet, int largestElement, OWLOntology ontology, int max){
		if(max < 0) return max;
		for(int i = largestElement + 1; i < sourceSet.size(); i++){
			currentSet.add(sourceSet.get(i));
			
			tc.method(currentSet, ontology);
			
			max = testSet(tc, currentSet, sourceSet, i, ontology, max - 1);
			if(max < 0) break;
			currentSet.remove(sourceSet.get(i));
		}
		return max;
	}
}
