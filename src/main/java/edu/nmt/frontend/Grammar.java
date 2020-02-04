package edu.nmt.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.nmt.util.IOUtil;

public class Grammar {

	private List<Rule> rules;
    private HashSet<String> terminals;
    private HashSet<String> variables;
    private String startVariable;
    private HashMap<String, HashSet<String>> firstSets;
    private HashMap<String, HashSet<String>> followSets;
	private File finp;
	private String fcontents;
	
	public Grammar(File file) throws IOException { 
		finp = file;
		if (!finp.exists()) {
			throw new FileNotFoundException();
		}
	}
	
	public Grammar(String fileName) throws IOException { 
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
				if (LHS == null) {
					startVariable = line.trim();
				}
				
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
        computeFirstSets();
        System.out.println(firstSets);
        computeFollowSet();
        System.out.println(followSets);
	}
	
	public List<Rule> getRules() {
		return rules;
	}
	
	 private void computeFirstSets() {
        firstSets = new HashMap<String, HashSet<String>>();

        for (String s : variables) {

            HashSet<String> temp = new HashSet<String>();
            firstSets.put(s, temp);
        }
        while (true) {
            boolean isChanged = false;
            for (String variable : variables) {
                HashSet<String> firstSet = new HashSet<String>();
                for (Rule rule : rules) {
                    if (rule.getLeftSide().equals(variable)) {
                        HashSet<String> addAll = computeFirst(rule.getRightSide(), 0);
                        firstSet.addAll(addAll);
                    }
                }
                if (!firstSets.get(variable).containsAll(firstSet)) {
                    isChanged = true;
                    firstSets.get(variable).addAll(firstSet);
                }

            }
            if (!isChanged) {
                break;
            }
        }

        firstSets.put("S'", firstSets.get(startVariable));
    }

    private void computeFollowSet() {
    	followSets = new HashMap<String, HashSet<String>>();
        for (String s : variables) {
            HashSet<String> temp = new HashSet<String>();
            followSets.put(s, temp);
        }
        HashSet<String> start = new HashSet<String>();
        start.add("$");
        followSets.put("S'", start);

        while (true) {
            boolean isChange = false;
            for (String variable : variables) {
                for (Rule rule : rules) {
                    for (int i = 0; i < rule.getRightSide().length; i++) {
                        if (rule.getRightSide()[i].equals(variable)) {
                            if (i == rule.getRightSide().length - 1) {
                            	followSets.get(variable).addAll(followSets.get(rule.leftSide));
                            } else {
                                HashSet<String> first = computeFirst(rule.getRightSide(), i + 1);
                                if (first.contains("epsilon")) {
                                    first.remove("epsilon");
                                    first.addAll(followSets.get(rule.leftSide));
                                }
                                if (!followSets.get(variable).containsAll(first)) {
                                    isChange = true;
                                    followSets.get(variable).addAll(first);
                                }
                            }
                        }
                    }
                }
            }
            if (!isChange) {
                break;
            }
        }
    }
	    
    public HashSet<String> computeFirst(String[] string, int index) {
        HashSet<String> first = new HashSet<String>();
        if (index == string.length) {
            return first;
        }
        if (terminals.contains(string[index])) {
            first.add(string[index]);
            return first;
        }

        if (variables.contains(string[index])) {
            for (String str : firstSets.get(string[index])) {
                first.add(str);
            }
        }

        if (first.contains("epsilon")) {
            if (index != string.length - 1) {
                first.addAll(computeFirst(string, index + 1));
                first.remove("epsilon");
            }
        }
        return first;
    }
	
    public static void main(String[] args) throws IOException {
    	Grammar g = new Grammar("config/grammar.cfg");
    	g.loadGrammar();
    	
    }
	
}
