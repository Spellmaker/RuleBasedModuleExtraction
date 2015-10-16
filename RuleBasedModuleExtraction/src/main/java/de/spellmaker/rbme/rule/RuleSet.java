package de.spellmaker.rbme.rule;

import java.util.HashSet;
import java.util.Set;

public class RuleSet {
	private Set<Rule> rules;
	
	public RuleSet(Set<Rule> rules){
		this.rules = new HashSet<>();
		rules.forEach(x -> this.rules.add(x.clone()));
	}
	
	public Set<Rule> getRules(){
		Set<Rule> resultSet = new HashSet<>();
		this.rules.forEach(x -> resultSet.add(x.clone()));
		return resultSet;
	}

}
