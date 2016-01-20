import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.spellmaker.rbme.rule.RuleSet;

public class ModuleList{
	private List<boolean[]> modules;
	private RuleSet rules;
	
	public ModuleList(List<boolean[]> modules, RuleSet rules){
		this.modules = modules;
		this.rules = rules;
	}
	
	public ModuleIterator getIterator(int i){
		return new ModuleIterator(modules.get(i), rules);
	}
	
	public int size(){
		return modules.size();
	}
}

class ModuleIterator implements Iterator<OWLAxiom>{
	boolean[] module;
	RuleSet rules;
	Integer nextElement;
	int pos;
	
	public ModuleIterator(boolean[] module, RuleSet rules){
		this.module = module;
		this.rules = rules;
		pos = -1;
		findElement();
	}
	
	private void findElement(){
		nextElement = null;
		while(pos < module.length){
			pos++;
			if(module[pos]){
				nextElement = pos;
			}
		}
	}
	
	
	@Override
	public boolean hasNext() {
		return nextElement != null;
	}

	@Override
	public OWLAxiom next() {
		OWLAxiom res = (OWLAxiom) rules.lookup(nextElement);
		findElement();
		return res;
	}
	
}
