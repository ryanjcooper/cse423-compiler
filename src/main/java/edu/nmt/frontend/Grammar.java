package edu.nmt.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.nmt.frontend.parser.Rule;
import edu.nmt.util.IOUtil;

public class Grammar {

	private List<Rule> rules;
    private HashSet<String> terminals;
    private HashSet<String> variables;
	private File finp;
	private String fcontents;
	
	public Grammar(File file) throws FileNotFoundException { 
		finp = file;
		if (!finp.exists()) {
			throw new FileNotFoundException();
		}
	}
	
	public Grammar(String fileName) throws FileNotFoundException { 
		finp = new File(fileName);
		if (!finp.exists()) {
			throw new FileNotFoundException();
		}
		
	}
	
	public void loadGrammar() throws IOException {
		fcontents = IOUtil.readFileToString(finp);
		String[] lines = fcontents.split("\n");
		rules = new ArrayList<Rule>();
		terminals = new HashSet<String>();
		variables = new HashSet<String>();
		
		String LHS = null;
		for (String line : lines) {
			if (!line.contains("\t") && (!line.trim().isEmpty())) {
				// First line
				LHS = line.trim();
				variables.add(LHS);
			} else if (line.trim().isEmpty()) {
				continue;
			} else {
				String[] RHS = line.trim().split("\\s+");
				Rule r = new Rule(LHS, RHS);
				if (rules.contains(r)) {
					System.err.println("Warning: Duplicate rule " + r.toString());
				} else {
					rules.add(r);
	                for (String terminal : RHS) {
	                    terminals.add(terminal);
	                }
				}
			}
		}
        for (String variable : variables) {
            terminals.remove(variable);
        }
	}
	
	public List<Rule> getRules() {
		return rules;
	}
	
	public boolean isTerminal(String sym) {
		return this.terminals.contains(sym);
	}
	
    public static void main(String[] args) throws IOException {
    	Grammar g = new Grammar("config/grammar.cfg");
    	g.loadGrammar();
    }
	
}
