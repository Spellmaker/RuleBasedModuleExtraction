package de.spellmaker.rbme.ore;

public class ORENoFilter implements OREFilter{

	@Override
	public boolean accept(String... s) {
		return true;
	}

}
