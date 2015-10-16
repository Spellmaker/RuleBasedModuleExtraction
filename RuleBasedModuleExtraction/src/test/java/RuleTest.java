import static org.junit.Assert.*;

import org.junit.Test;

import de.spellmaker.rbme.rule.Rule;

public class RuleTest {
	@Test public void testConstructor(){
		Object o1 = new Object();
		Object o2 = new Object();
		Rule r1 = new Rule(o1, o2);
		
		assertTrue("Can construct rules", true);
	}

	@Test(expected=NullPointerException.class)
	public void testNullHead(){
		Rule r1 = new Rule(null, new Object());
		
		assertTrue("Rule was not constructed", r1 == null);
	}
	
	@Test public void testEquality(){
		Object o1 = new Object();
		Object o2 = new Object();
		Rule r1 = new Rule(o1, o2, o2);
		Rule r2 = new Rule(o1, o2);
		
		assertTrue("Rule body contains same element only once", r1.equals(r2));
		
		Object o3 = new Object();
		r1 = new Rule(o1, o2, o3);
		r2 = new Rule(o1, o3, o2);
		
		assertTrue("order in body does not matter", r1.equals(r2));
	}
	
	@Test public void testOffer(){
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		
		Rule r1 = new Rule(o1, o2, o3);
		Rule r2 = new Rule(o1, o3, o2);
		assertTrue("Rule is not completed yet", !r1.offer(o2));
		r2.offer(o2);
		assertTrue("Rule is completed after the body has been cleared", r1.offer(o3));
		assertTrue("Offer order does not matter", r2.offer(o3));
		
	}
}
