package de.spellmaker.rbme.rule;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Interface for classes which compile OWL ontologies of different expressivities into rules.
 * These rules can be used to extract modules out of the source ontologie.
 * @author spellmaker
 *
 */
public interface RuleBuilder {
	/**
	 * Compiles the given OWL axioms into a set of rules
	 * @param axioms The axioms of the source ontologie
	 * @return A rule set managing the created rules
	 */
	public RuleSet buildRules(Set<OWLAxiom> axioms);
	
	/*public static Map<OWLObject, List<Integer>> buildRuleMap(Set<Rule> rules){
		int pos = 0;
		Map<OWLObject, List<Integer>> ruleMap = new HashMap<>();
		for(Rule rule : rules){
			//ignore bodyless rules
			if(rule.size() > 0){
				for(OWLObject o : rule){
					List<Integer> current = ruleMap.get(o);
					if(current == null) current = new LinkedList<>();
					current.add(pos);
					ruleMap.put(o, current);
				}
				pos++;
			}
		}
		return ruleMap;
	}*/
}
