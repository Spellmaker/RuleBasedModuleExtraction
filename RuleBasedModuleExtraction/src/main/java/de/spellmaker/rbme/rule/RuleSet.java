package de.spellmaker.rbme.rule;

import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates a set of rules, prohibiting the modification of the original rules.
 * @author spellmaker
 *
 */
public class RuleSet {
	private Set<Rule> rules;
	
	public RuleSet(Set<Rule> rules){
		this.rules = new HashSet<>();
		rules.forEach(x -> this.rules.add(x.clone()));
	}
	
	/**
	 * Creates a modifiable copy of the rules managed by this set.
	 * @return A copy of the rules managed by this set
	 */
	public Set<Rule> getRules(){
		Set<Rule> resultSet = new HashSet<>();
		this.rules.forEach(x -> resultSet.add(x.clone()));
		return resultSet;
	}

}
