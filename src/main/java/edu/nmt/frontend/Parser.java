package edu.nmt.frontend;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	private Node parseTree;
	private ArrayList<Node> stack;	// stores each token as a node as they are read in
	private LockedStack lockedStack; 
	
	public Parser(Grammar g, List<Token> tok) {
		grammar = g;
		tokens = tok;
		stack = new ArrayList<Node>();
		lockedStack = new LockedStack();
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/test.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
		p.grammar.loadGrammar();
		//System.out.println(p.canBeReduced("type identifier l_par"));
		//System.out.println(p.getCommonAncestor(p.grammar.computePrevs("bool_op"), "identifier"));
		//System.out.println(p.parse());
		System.out.println(p.predictGoal(p.tokensToNodes()));
		//System.out.println("output: " + p.test(p.tokensToNodes(), "statement", true));
		//List<List<List<Node>>> frags = new ArrayList<List<List<Node>>>();
		//List<String> layout = new ArrayList<String>();
		//p.reduceFragments(p.parseString(p.tokensToNodes()), frags, layout);
		//System.out.println(frags);
		//System.out.println(p.parseString(p.tokensToNodes()));
		//System.out.println(p.getCommonAncestor(p.grammar.computeFirsts("while l_paren"), "expression"));
	}
	
	public static String getSpacedArray(String[] strarr) {
		String output = "";
		
		for (String s : strarr) {
			output += s + " ";
		}
		
		return output.trim();		
	}
	
	public static String getSpacedArray(List<Node> strarr) {
		String output = "";
		
		for (Node s : strarr) {
			output += s + " ";
		}
		
		return output.trim();		
	}
	
	/*
	 * checks if set contains sym or ancestors of sym
	 * ancestor = reduced equivalent of sym
	 * @param hs is the set to be evaluated
	 * @param sym is the symbol to check hs for
	 * @return common ancestor
	 */	
	public String getCommonAncestor(HashSet<String> hs, String sym) {
		HashSet<String> ancestors = this.grammar.getAncestors(sym);

		for (String s : hs) {
			if (ancestors.contains(s)) {
				return s;
			}
		}
		
		return null;
	}
	
	public Node getParseTree() {
		return this.parseTree;
	}
	
	private List<Node> tokensToNodes() {
		List<Node> nodes = new ArrayList<Node>();
		
		for (Token token : tokens) {
			nodes.add(new Node(token));
		}
		
		return nodes;
	}
	
	public boolean stackContains(String sym) {
		for (Node n : stack) {
			if (n.toString().equals(sym))
				return true;
		}
		
		return false;
	}
	
	public List<List<Node>> groupTokens(List<Node> nodes) {
		List<List<Node>> trees = new ArrayList<List<Node>>();
		
		System.out.println("ps input: " + nodes);
		
		List<Node> tmp = new ArrayList<Node>();
		List<String> open = new ArrayList<String>();
		
		for (Node node : nodes) {
			if (node.token.getTokenLabel().equals("l_paren")) {
				
				if (open.isEmpty()) {
					if (!tmp.isEmpty()) {
						trees.add(tmp);
						tmp = new ArrayList<Node>();
					}
				}
				
				tmp.add(node);
				
				if (open.isEmpty() || open.contains("l_paren")) {
					open.add("l_paren");
				}
			} else if (node.token.getTokenLabel().equals("l_brace")) {
				
				if (open.isEmpty()) {
					if (!tmp.isEmpty()) {
						trees.add(tmp);
						tmp = new ArrayList<Node>();
					}
				}
				
				tmp.add(node);
				
				if (open.isEmpty() || open.contains("l_brace")) {
					open.add("l_brace");
				}				
			} else if (node.token.getTokenLabel().equals("r_paren")) {
				tmp.add(node);
				if (open.isEmpty() || (open.contains("l_paren") && open.size() == 1)) {
					trees.add(tmp);
					tmp = new ArrayList<Node>();
				}
				open.remove("l_paren");
			} else if (node.token.getTokenLabel().equals("r_brace")) {
				tmp.add(node);
				if (open.isEmpty() || (open.contains("l_brace") && open.size() == 1)) {
					trees.add(tmp);
					tmp = new ArrayList<Node>();
				}
				open.remove("l_brace");
			} else if (node.token.getTokenLabel().equals("semi")) {
				tmp.add(node);
				if (open.isEmpty()) {
					trees.add(tmp);
					tmp = new ArrayList<Node>();
				}
			} else {
				tmp.add(node);
			}
		}
		
		System.out.println("ps output: " + trees);
		
		return trees;
	}
	
	public List<Node> fuse(List<Node> nd, List<Node> checkStack) {
		for (Node node : nd) {
			checkStack.add(node);
		}
		
		return checkStack;
	}
	
	/**
	 * reduce groups of tokens into specific categories defined by grammar
	 * @param fragments are the group of tokens
	 * @param reducedFrags : output subset of token stream with desired goal in mind
	 * @param layout       : output list of goals associated with reducedFrags
	 */
	public void reduceFragments(List<List<Node>> fragments, List<List<List<Node>>> reducedFrags, List<String> layout) {
		HashSet<String> choicesString = new HashSet<String>();
		int i = 0;
		
		System.out.println("\nREDUCED FRAGS\n");
		System.out.println("input: " + fragments + "\n");
		
		while (i < fragments.size()) {
			String state = getSpacedArray(fragments.get(i));
			String lookahead = "";
			List<List<Node>> reduction = new ArrayList<List<Node>>();
			int j = 0;
			
			if (state.contains("semi")) {
				layout.add("varDeclaration");
				reduction.add(fragments.get(i));
				reducedFrags.add(reduction);
				i++;
				continue;
			}
			
			/* get a set of possible choices that contain the state */
			HashSet<Rule> choices = this.grammar.getPossibleRules(state);
			
			System.out.println("choices: " + choices);
			
			for (Rule choice : choices) {
				choicesString.add(choice.getLeftSide());
			}
			
			reduction.add(fragments.get(i+j));
			
			/* loop until there is until one choice left */
			while (choicesString.size() > 1) {
				j++;
				
				/* get the lookahead */
				lookahead = fragments.get(i+j).get(0).toString();
			 
				if (lookahead.equals("l_brace")) {
					 lookahead = "compoundStmt";
				}
				 
				for (Rule choice : choices) {
					String laState = state + " " + lookahead;
					laState = laState.trim();
					
					//System.out.println(getSpacedArray(choice.getRightSide()));
					
					if (!getSpacedArray(choice.getRightSide()).contains(state) 
							|| !getSpacedArray(choice.getRightSide()).contains(lookahead)) {
						//System.out.println("Removing " + choice.getLeftSide());
						choicesString.remove(choice.getLeftSide());
					}
				}	
				
				reduction.add(fragments.get(i+j));
			}
			
			System.out.println("chose: " + choicesString + "\n");
			
			reducedFrags.add(reduction);
			
			for (String s : choicesString) {
				layout.add(s);
			}
			
			i += j+1;
		}
		
		System.out.println("layout is : " + layout);
		System.out.println("reduced frags : " + reducedFrags);
	}
	
	/**
	 * parse a list of fragments
	 * @param fragments are specific groups of token from groupTokens
	 * @return the root node of tree if parsing is successful
	 */
	public Node parse(List<List<Node>> fragments) {
		List<String> layout = new ArrayList<String>();
		List<List<List<Node>>> reducedFrags = new ArrayList<List<List<Node>>>();
		List<Node> finalNodes = new ArrayList<Node>();
		
		/* reduce fragments into a top-down set */
		reduceFragments(fragments, reducedFrags, layout);
		
		/* 
		 * for each goal specified by the layout
		 * determine if each subset of tokens are able to reach the goal 
		 */
		for (int i = 0; i < reducedFrags.size(); i++) {
			/* this is the check */
			Node checked = checkParse(reducedFrags.get(i), layout.get(i));
			
			/* if any of subsets fail to reach goal, parsing will fail */
			if (checked == null) {
				return null;
			} else {
				finalNodes.add(checked);
			}
		}
		
		/* reduce the final set mapped by layout into a program node */
		return reduce(finalNodes, "program", true);
	}
	
	public boolean evalStack(List<Node> check, String goal, int index) {
		String[] test = goal.split(" ");
		boolean isValid = true;
		
		try {
			for (int i = 0; i < index; i++) {
				if (!check.get(i).toString().equals(test[i])) {
					isValid = false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		
		return isValid;
	}
	
	public List<String> toArrayList(String[] arr) {
		List<String> out = new ArrayList<String>();
		
		for (int i = 0; i < arr.length; i++) {
			out.add(arr[i]);
		}
		
		return out;
	} 
	
	public String arraySubstring(List<Node> nodes, int start, int end) {
		String out = "";
		
		for (int i = start; i < end; i++) {
			out += nodes.get(i) + " ";
		}
		
		return out.trim();
	}
	
	public boolean arrContains(String[] arr, String sym) {
		for (String s : arr) {
			if (sym.equals(s))
				return true;
		}
		
		return false;
	}
	
	public Rule predictGoal(List<Node> nodes) {
		Rule prediction = null;
		boolean stop = false;
		
		for (int i = nodes.size() - 1; i >= 0; i--) {
			for (Rule rule : this.grammar.getRules()) {
				String rhs = getSpacedArray(rule.getRightSide());
				
				if (nodes.size() == 1) {
					if (rhs.equals(nodes.get(0).toString())) {
						prediction = rule;
						stop = true;
						break;
					} 					
				} else if (rule.getRightSide().length > 1) {
					if (i > 0 && rhs.contains(arraySubstring(nodes, 0, i+1))) {
						prediction = rule;
						stop = true;
						break;
					} else if (i == 0 && arrContains(rule.getRightSide(), nodes.get(i).toString())) {
						prediction = rule;
						stop = true;
						break;					
					}					
				}
			}
			
			if (stop)
				break;
		} 
		
		if (prediction == null)
			System.out.println("Prediction failed first phase");
		
		/* there should exist a rule where at least the first or second terminal exist */
		if (prediction == null) {
			for (int i = 0; i < nodes.size(); i++) {
				for (Rule rule : this.grammar.getRules()) {
					String rhs = getSpacedArray(rule.getRightSide());
					
					if (rhs.contains(nodes.get(i).toString()) 
							&& (rule.getRightSide().length == nodes.size() 
									|| rule.getRightSide().length > 1)) {
						prediction = rule;
						stop = true;
						break;
					}
				}						
			}			
		}
		
		System.out.println("choice " + prediction);
		return prediction;
	}
	
	/**
	 * bottom-up reduction of a list of tokens to a specific goal
	 * @param nodes is the list of tokens to reduce
	 * @param goal is the goal to ultimately achieve
	 * @param root is true if the reduction is directly achieving for goal
	 * @return the root node of label goal
	 */
	public Node reduce(List<Node> nodes, String goal, boolean root) {
		/* attempt to reduce the stream of symbols into the goal */
		stack = new ArrayList<Node>();
		lockedStack = new LockedStack();
		Node semi = null;
		Node parent = null;
		
		System.out.println("nodes " + nodes + " wants " + goal);
		
		if (goal.equals("statement")) {
			if (!nodes.get(nodes.size()-1).toString().equals("semi")) {
				return null;
			} else {
				semi = nodes.remove(nodes.size()-1);
				goal = "statementsemi";
			}			
		}
		
		if (nodes.get(0).toString().equals(goal)) 
			return nodes.get(0);
		
		if (goal.equals("statementsemi") && this.grammar.computePrevs("semi").contains(nodes.get(0).toString())) {
			parent = nodes.get(0);
			
			Node newChild = parent;
			parent = new Node(new Token(null,  "statement", null, null));
			parent.addChild(newChild);
			parent.addChild(new Node(new Token(null, "semi", null, null)));
			
			return parent;
		}
		
		//HashSet<Rule> choices = new HashSet<Rule>();
		Rule choice = predictGoal(nodes);
		
		List<Integer> indexes = new ArrayList<Integer>();
		int i = 0;
		
		for (Node n : nodes) {
			
			if (i == choice.getRightSide().length-1) {
				indexes.add(new Integer(i));
				continue;
			}
			
			if (i >= choice.getRightSide().length) {
				indexes.add(new Integer(i - 1));
			} else {
				if (this.grammar.getVariables().contains(choice.getRightSide()[i]))
					i++;
				
				System.out.println(i);
				
				if (choice.getRightSide()[i].equals(n.toString())) {
					indexes.add(new Integer(++i - 1));
				} else {
					indexes.add(new Integer(i - 1));
				}				
			}
		}
		
		System.out.println(indexes);
		
		List<Node> next = new ArrayList<Node>();
		
		if (nodes.get(0).toString().equals(goal)) {
			return nodes.get(0);
		} else {			
			for (i = 0; i < choice.getRightSide().length; i++) {
				List<Node> tmp = new ArrayList<Node>();				
				if (choice.getRightSide().length > 1) {
					parent = new Node(new Token(null,  choice.getRightSide()[i], null, null));
					
					for (int j = 0; j < indexes.size(); j++) {
						if (indexes.get(j).equals(i)) {
							tmp.add(nodes.get(j));
						}
					}
					
					System.out.println("tmp: " + tmp);
					parent.addChild(reduce(tmp, choice.getRightSide()[i], false));
					next.add(parent);					
				} else {
					parent = new Node(new Token(null,  choice.getLeftSide(), null, null));
					parent.addChild(nodes.get(i));
					tmp.add(parent);
					next.add(reduce(tmp, goal, false));
				}
			}
				
		}
		
		parent = new Node(new Token(null,  choice.getLeftSide(), null, null));
		
		for (Node node : next) {
			if (node == null) {
				return null;
			} else {
				parent.addChild(node);
			}
		}
		
		if (!parent.toString().equals(goal) && root) {
			List<Node> finalTest = new ArrayList<Node>();
			finalTest.add(parent);
			return reduce(finalTest, goal, true);
		}
		
		//System.out.println("Final parent " + parent);
		
		if (parent.toString().equals(goal))
			System.out.println("Final parent " + parent);
		
		return parent;
	}
	
	/**
	 * checks if a subset of tokens can reach a specific goal
	 * @param list is the subset of tokens
	 * @param goal is the goal to achieve
	 * @return root node of the subset with label goal
	 */
	public Node checkParse(List<List<Node>> list, String goal) {
		List<Node> checkStack = new ArrayList<Node>();
		
		System.out.println("checkParse nodes : " + list);
		
		List<Rule> options = this.grammar.getRules(goal);
		String lgoal = "";
		String rgoal = "";
		
		int i = 0;
		
		for (List<Node> nd : list) {
			String state = getSpacedArray(nd);
			System.out.println("state = " + state);	
			
			lgoal = "";
			
			List<Rule> newOptions = new ArrayList<Rule>();
			for (Rule rule : options) {
				if (getSpacedArray(rule.getRightSide()).contains(state)) {
					newOptions.add(rule);
				}
			}
			
			if (!newOptions.isEmpty()) {
				options = newOptions;
			}
			
			lgoal = options.get(0).getLeftSide();
			rgoal = getSpacedArray(options.get(0).getRightSide());
			
			System.out.println("lgoal is " + lgoal);
			System.out.println("rgoal is " + rgoal);
			
			if (rgoal.contains(state)) {
				System.out.println("fused");
				fuse(nd, checkStack);
			} else if (nd.get(0).toString().equals("l_paren")) {
				if (nd.get(1).toString().equals("r_paren")) {
					fuse(nd, checkStack);
				} else if (nd.get(nd.size()-1).toString().equals("r_paren") ) {
					/* remove l_paren and r_paren */
					Node left = nd.remove(0);
					Node right = nd.remove(nd.size()-1);
					
					/* add l_paren to the stack */
					checkStack.add(left);
					
					/* get new goal */
					String newGoal = rgoal;
					newGoal = newGoal.substring(newGoal.indexOf("l_paren") + 7, newGoal.lastIndexOf("r_paren")).trim();
					
					System.out.println("nd = " + nd);
					System.out.println("newGoal = " + newGoal);
					
					Node parsed = reduce(nd, newGoal, true);
					
					if (parsed != null && parsed.toString().equals(newGoal)) {
						checkStack.add(parsed);
						checkStack.add(right);
					} else {
						return null;
					}
					
				} else {
					System.out.println("Missing closing parenthesis");
				}
			} else if (nd.get(0).toString().equals("l_brace")) {
				if (nd.get(1).toString().equals("r_brace")) {
					fuse(nd, checkStack);
				} else if (nd.get(nd.size()-1).toString().equals("r_brace") ) {
					/* remove l_brace and r_brace */
					Node left = nd.remove(0);
					Node right = nd.remove(nd.size()-1);
					
					/* get new goal */
					String newGoal = "statement";
					
					//System.out.println("nd = " + nd);
					//System.out.println("newGoal = " + newGoal);
					
					Node stmtList = null;
					
					/* everything in this split should return a statement */
					List<List<Node>> ndFrags = groupTokens(nd);
					List<List<List<Node>>> ndReduced = new ArrayList<List<List<Node>>>();
					List<String> goals = new ArrayList<String>();
					
					reduceFragments(ndFrags, ndReduced, goals);
					
					ndFrags = ndReduced.get(0);
					
					for (i = 0; i < ndFrags.size(); i++) {
						System.out.println("ndSplit " + ndFrags.get(i));
						Node parsed = reduce(ndFrags.get(i), goals.get(i), true);
						
						System.out.println("parsed is " + parsed + "\n");
						
						if (parsed.toString().equals("statement")) {
							if (stmtList == null) {
								stmtList = new Node(new Token(null, "statementList", null, null));
								stmtList.addChild(parsed);
							} else {
								Node stmt = new Node(new Token(null, "statementList", null, null));
								stmt.addChild(stmtList);
								stmt.addChild(parsed);
								stmtList = stmt;
							}
						} else {
							return null;
						}
					}
					
					Node compound = new Node(new Token(null, "compoundStmt", null, null));
					compound.addChild(left);
					compound.addChild(stmtList);
					compound.addChild(right);
					
					checkStack.add(compound);
					
					System.out.println("stack est " + checkStack);
					
				} else {
					System.out.println("Missing closing parenthesis");
					return null;
				}				
			} else {
				System.out.println("nd = " + nd);
				reduce(nd, goal, true);
			}
			
			System.out.println("checkSTack " + checkStack);
			
			if (evalStack(checkStack, rgoal, rgoal.split(" ").length)) {
				Node parent = new Node(new Token(null, lgoal, null, null));
				
				while (!checkStack.isEmpty()) {
					parent.addChild(checkStack.get(0));
					checkStack.remove(0);
				}
				
				checkStack.add(parent);
				
				System.out.println("newSTack " + checkStack);
				return parent;
			}
		}
		
		return null;
	}
	
	public boolean parse() {
		List<List<Node>> tmp = groupTokens(tokensToNodes());
		Node root = parse(tmp);
		
		if (tmp == null || tmp.isEmpty() || root == null)
			return false;
		else
			return root.toString().equals("program");
	}
}
