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
    private HashMap<String, HashSet<String>> equilSets;
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
        computeEquilSet();
        System.out.println(equilSets);
	}
	
	public List<Rule> getRules() {
		return rules;
	}
	
	public List<Rule> getRules(String lhs) {
		ArrayList<Rule> out = new ArrayList<Rule>();
		
		for (Rule rule : rules) {
			if (rule.getLeftSide().equals(lhs))
				out.add(rule);
		}
		
		return out;
	}
	
	public HashMap<String, HashSet<String>> getFollowSets() {
		return this.followSets;
	}
	
	public HashMap<String, HashSet<String>> getFirstSets() {
		return this.firstSets;
	}
	
	public HashMap<String, HashSet<String>> getEquilSets() {
		return this.equilSets;
	}	
	
	public HashSet<String> getVariables() {
		return this.variables;
	}
	 
	private void computeFirstSets() {
        firstSets = new HashMap<String, HashSet<String>>();

        /* init all first sets for all non-terminals */
        for (String s : variables) {
            HashSet<String> temp = new HashSet<String>();
            firstSets.put(s, temp);
        }
        
        /* init all first sets for all terminals */
        for (String s : terminals) {
            HashSet<String> temp = new HashSet<String>();
            firstSets.put(s, temp);
        }        
        
        /* loop through all possible symbols in grammar */
        for (String sym : firstSets.keySet()) {
        	/* look through all rules, RHS, and search for the symbol
        	 * that appears after sym
        	 */
        	HashSet<String> firsts = firstSets.get(sym);
        	for (Rule rule : rules) {
        		String[] rhs = rule.getRightSide();
        		for (int i = 0; i < rhs.length; i++) {
        			if (rhs[i].equals(sym) && i+1 < rhs.length) {
        				firsts.add(rhs[i+1]);
        			}
        		}
        	}
        }
    }	 
	
	private void computeEquilSet() {
		equilSets = new HashMap<String, HashSet<String>>();
		
        /* init all symbols */
        for (String s : firstSets.keySet()) {
            HashSet<String> temp = new HashSet<String>();
            equilSets.put(s, temp);
        }  
        
        /* loop through all possible symbols in grammar */
        for (String sym : equilSets.keySet()) {
        	/* look through all rules, RHS, and search for this symbol by itself
        	 */
        	HashSet<String> equals = equilSets.get(sym);
        	String nt = sym;
    		equals.add(nt);
        	
        	while (true) {
        		nt = getReducedEquil(sym);
        		
        		if (nt.equals(sym))
        			break;
        		
        		equals.add(nt);
        		sym = nt;
        	}
        }        
	}
	
	public HashSet<String> getAncestors(String sym) {
		HashSet<String> ancestors = new HashSet<String>();
    	String nt = sym;
    	
    	while (true) {
    		nt = getReducedEquil(sym);
    		
    		if (nt.equals(sym))
    			break;
    		
    		ancestors.add(nt);
    		sym = nt;
    	}
		
    	return ancestors;
	}
	
	public String getReducedEquil(String sym) {
		for (Rule rule : rules) {
			String[] rhs = rule.getRightSide();
			
			if (Parser.getSpacedArray(rhs).equals(sym)) {
				return rule.getLeftSide();
			}
		}
		
		return sym;
	}
	
	public HashSet<String> computePrevs(String sym) {
		HashSet<String> prevs = new HashSet<String>();
		
		for (Rule rule : rules) {
			String[] rhs = rule.getRightSide();
			if (Parser.getSpacedArray(rhs).contains(sym)) {
				for (int i = 0; i < rhs.length; i++) {
					if (rhs[i].equals(sym) && i-1 >= 0) {
						prevs.add(rhs[i-1]);
					} else if (rhs[i].equals(sym) && i == 0) {
						prevs.add("$");
					}
				}
			}
		}
		
		return prevs;		
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
	 
	 public String greaterPrecedence(String nt1, String nt2) {
		 if (getLineage(nt1).contains(nt2)) {
			 return nt1;
		 } else if (getLineage(nt2).contains(nt1)) {
			 return nt2;
		 } else {
			 return null;
		 }
	 }
	 
	public HashSet<String> computeFirsts(String sym) {
		HashSet<String> firsts = new HashSet<String>();
		
		for (Rule rule : rules) {
			String[] rhs = rule.getRightSide();
			if (Parser.getSpacedArray(rhs).contains(sym)) {
				String[] symSplit = sym.split(" ");
				
				for (int i = 0; i < rhs.length; i++) {
					if (rhs[i].equals(symSplit[symSplit.length-1])) {
						if (i+1 < rhs.length)
							firsts.add(rhs[i+1]);
						else
							firsts.add("$");
					}
				}
			}
		}
		
		return firsts;
	}
	
	public HashSet<String> getLineage(String sym) {
		HashSet<String> out = new HashSet<String>();
		computeLineage(sym, out);
		out.remove(sym);
		return out;
	}
	
	public void computeLineage(String sym, HashSet<String> fam) {
		/* add sym to the output */
		fam.add(sym);
		
		/* get every rule that contains sym on rhs */
		for (Rule rule : rules) {
			if (Parser.getSpacedArray(rule.getRightSide()).contains(sym)) {
				if (!fam.contains(rule.getLeftSide())) {
					computeLineage(rule.getLeftSide(), fam);
				}
			}			
		}
	}
	
	public HashSet<Rule> getPossibleRules(String sym) {
		HashSet<Rule> possRules = new HashSet<Rule>();
		
		for (Rule rule : rules) {
			if (Parser.getSpacedArray(rule.getRightSide()).contains(sym))
				possRules.add(rule);
		}
		
		return possRules;
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
	
	public boolean isTerminal(String sym) {
		return this.terminals.contains(sym);
	}
	
	public HashSet<String> getStartStates(String sym) {
		HashSet<String> starts = new HashSet<String>();
		HashSet<String> out = new HashSet<String>();
		
		if (terminals.contains(sym)) {
			out.add(sym);
			return out;
		}
		
		for (Rule rule : this.getRules(sym)) {
			String[] rhs = rule.getRightSide();
			
			if (variables.contains(rhs[0]) && !sym.equals(rhs[0])) {
				for (String s : getStartStates(rhs[0])) {
					starts.add(s);
				}
			} else {
				starts.add(rhs[0]);
			}
		}
		
		for (String s : starts) {
			if (!variables.contains(s)) {
				out.add(s);
			}
		}
		
		return out;
	}
	
    public static void main(String[] args) throws IOException {
    	Grammar g = new Grammar("config/grammar.cfg");
    	g.loadGrammar();
    	//g.getLineage("numeric_constant", tmp);
    	System.out.println(g.greaterPrecedence("declarationList", "statement"));
    	//System.out.println(g.getPossibleRules("l_brace"));
    	String tmp = "type identifier l_paren params r_paren semi";
    	//System.out.println(tmp.substring(tmp.indexOf("l_paren") + 7, tmp.lastIndexOf("r_paren")));
    	System.out.println(g.followSets.get("expression"));
    	System.out.println(g.getStartStates("declarationList"));
    	//System.out.println(g.getAncestors("argList"));
    }
	
}
