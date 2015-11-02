package de.spellmaker.rbme.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.spellmaker.rbme.OntologiePaths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CorrectnessTest.class,
	SizeTest.class,
	SpeedTest.class,
	RuleGenerationTest.class
})
public class TestSuite {
	public static final int max_cases = 10;
	public static final String onto_testpath = OntologiePaths.galen;	
}
