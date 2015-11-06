package de.spellmaker.rbme;

import java.io.IOException;
import java.nio.file.Paths;

import de.spellmaker.rbme.ore.OREManager;

public class CopyOntologies {
	public static void main(String[] args) throws IOException{
		OREManager manager = new OREManager();
		manager.load(Paths.get(args[0]), "el\\consistency", "el\\classification", "el\\instantiation");
		manager.copyOntologies(Paths.get(args[1]), x -> true, 0);
	}
}
