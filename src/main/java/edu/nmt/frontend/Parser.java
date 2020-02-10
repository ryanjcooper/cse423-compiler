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
		Scanner scanner = new Scanner("test/divide.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
		p.grammar.loadGrammar();
		//System.out.println(p.canBeReduced("type identifier l_par"));
		//System.out.println(p.getCommonAncestor(p.grammar.computePrevs("bool_op"), "identifier"));
		System.out.println(p.parse());
		//System.out.println("output: " + p.test(p.tokensToNodes(), "statement", true));
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
	
	public static String getSpacedArray(ArrayList<Node> strarr) {
		String output = "";
		
		for (Node s : strarr) {
			output += s + " ";
		}
		
		return output.trim();		
	}	

	/*
	 * determines whether a symbol can be reduced to a non-terminal
	 * @param state is the stack state at a specific iteration
	 * @param lookahead is the next symbol to be added to the stack
	 * @return state reduced to a non-terminal
	 */
	private boolean canReduce(String nt, String state, Node lookahead, String lookbehind) {
		HashSet<String> lbFirstSets = (lookbehind != null) ? this.grammar.getFirstSets().get(lookbehind) : null;	
		HashSet<String> ntFollowSet = this.grammar.getFollowSets().get(nt);
		
		/* nt or nt ancestor must be in lookbehind first set */
		if (lbFirstSets == null || getCommonAncestor(lbFirstSets, nt) != null) {
			/* lookahead must be in nt's follow set */
			if (lookahead == null || ntFollowSet.contains(lookahead.toString())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean isRHS(String state) {
		for (Rule rule : this.grammar.getRules()) {
			if (getSpacedArray(rule.getRightSide()).equals(state)) {
				return true;
			}
		}
		
		return false;		
	}
	
	public String reduceTo(String src, String dest) {
		String tmp = src;
		
		//System.out.println("state \"" + src + "\" to \"" + dest + "\"");
		
		if (src.equals(dest) || dest == null) {
			return src;
		}
		
		while (!tmp.equals(dest)) {
			String next = this.grammar.getReducedEquil(tmp);
			
			if (tmp.equals(next))
				break;
			
			System.out.println("state \"" + tmp + "\" --> \"" + next + "\"\n");
			replace(tmp, next);
			
			
			
			tmp = next;
		}
		
		return dest;
	}
	
	/*
	 * checks if set contains non-terminal
	 * @param hs is the set to be evaluated
	 * @return true if set contains nt, else false
	 */	
	public boolean hasNonTerminal(HashSet<String> hs) {
		for (String s : hs) {
			if (this.grammar.getVariables().contains(s))
				return true;
		}
		
		return false;
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
	
	/*
	 * replace the last n elements on the stack with nt
	 * @param nt is the newest symbol to be added to the stack
	 * @param n are the symbols to be replaced by nt
	 * @param stack is the stack to be manipulated
	 * @return altered stack
	 */
	public ArrayList<Node> replace(String n, String nt) {
		/* check if nt and n are identical else continue */
		if (nt.equals(n)) {
			return stack;
		}
		
		/* every successful call to replace adds a new node to the tree */
		Node parent = new Node(new Token(null, nt, null, null));	// new non-terminal node
		
		/* pop n nodes off stack and add them to parent node */
		for (int i = 0; i < n.split(" ").length; i++) {
			parent.addChild(stack.remove(stack.size()-1));
		}
		
		/* push nt node onto the stack */
		stack.add(parent);
		
		return stack;
	}
	
	public Node getParseTree() {
		return this.parseTree;
	}
	
	private ArrayList<Node> tokensToNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		for (Token token : tokens) {
			nodes.add(new Node(token));
		}
		
		return nodes;
	}
	
	/* short for "can ever be reduced" 
	 * checks if this state exists on the rhs of any rules
	 * @param state is the current state to check for reduction
	 * @return true if the state can eventually be reduced, false else
	 */
	public boolean canBeReduced(String state) {
		for (Rule rule : this.grammar.getRules()) {
			if (getSpacedArray(rule.getRightSide()).contains(state)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean stackContains(String sym) {
		for (Node n : stack) {
			if (n.toString().equals(sym))
				return true;
		}
		
		return false;
	}
	
	public String newReduce(String state, String lookahead) {
		String lookbehind = lockedStack.peekString();
		HashSet<String> lbWants = new HashSet<String>();
		String lbWant = "lbwant";
		String laWant = "lawant";
		HashSet<String> laWants = new HashSet<String>();
		String nt = this.grammar.getReducedEquil(state);
		boolean useLB = false;
		boolean useLA = false;
		int flag = 0;
		
		//System.out.println("Attempting to reduce " + state + "...\n");
		
		if (state.equals("declarationList") && !lockedStack.isEmpty() && lookahead != null) {
			return state;
		}
		
		if ((state.contains("r_brace") && !state.contains("l_brace"))
				|| (state.contains("r_paren") && !state.contains("l_paren"))) {
			flag = 2;
			//System.out.println("flag set to 2");
		}
		
		/* check if state can be reduced */
		if (nt.equals(state)) {
			//System.out.println("state \"" + state + "\"" + " cannot be reduced\n");
		}
		
		//System.out.println("\"" + state + "\" can be reduced to " + nt + "\n");
		
		/* get what lookbehind wants */
		if (lookbehind != null) {
			lbWants = this.grammar.computeFirsts(lookbehind);
			//System.out.println("Wants: " + lbWants + "\n");
			
			/* check if it can become something lb wants */
			lbWant = getCommonAncestor(this.grammar.computeFirsts(lookbehind), state);
			//System.out.println("Want: " + lbWant + "\n");
			
			if (lbWant != null)
				useLB = true;
		}
		
		/* get what lookahead wants */
		if (lookahead != null) {
			laWants = this.grammar.computePrevs(lookahead);
			//System.out.println("le Wants: " + laWants + "\n");
			laWant = getCommonAncestor(laWants, state);
			//System.out.println("le Want " + laWant + "\n");
			
			if (laWants.contains(state) 
				|| !laWants.contains("$") 
				|| lookahead.equals("l_paren") 
				|| lookahead.equals("l_brace"))
				useLA = true;
		}
		
		//System.out.println("useLB " + useLB);
		//System.out.println("useLA " + useLA);
		//System.out.println("laWant " + laWant);
		//System.out.println("lbWant " + lbWant);
		
		/* determine what to do based on lookahead and lookbehind */
		if (flag == 0 && (useLA || useLB)) {
			/* first check both */
			
			if (useLA && useLB && laWant != null && lbWant != null) {
				if (laWant != null && laWant.equals(lbWant)) {
					//System.out.println("why");
					if (state.equals(laWant)) {
						return state;
					}
					
					nt = laWant;
					flag = 2;
				} else {
					nt = this.grammar.greaterPrecedence(laWant, lbWant);
					
					if (nt.equals(lbWant)) {
						//System.out.println(lookbehind.contains("l_paren") && !state.contains("r_paren"));
						
						if (lookbehind.contains("l_paren") && !state.contains("r_paren")) {
							flag = 1;
						} else {
							flag = 2;
						}
					} else
						flag = 1;
				}
			} else if (useLB) {
				nt = lbWant;
				state = reduceTo(state, lbWant);
				
				flag = 2;
			} else if (useLA) {
				nt = laWant;
				if (this.grammar.computePrevs(lookahead).contains(nt)) {
					flag = 1;
				} else {
					//System.out.println("Rejected because \"" + nt + "\" not valid with lookahead \"" + lookahead + "\"");
					return state;
				}
			}
		}
		
		switch (flag) {
		default:
		case 2:
			//System.out.println(2);
			state = reduceTo(state, nt);
			/* get lookbehind state */
			String lbState = lookbehind + " " + state;
			
			while (true) {
				lookbehind = lockedStack.peekString();
				
				lbState = lookbehind + " " + state;
				lbState = lbState.trim();
						
				/* check if lookbehind state can be reduced */
				nt = this.grammar.getReducedEquil(lbState);
				
				if (lookbehind != null && !nt.equals(lbState)) {	// not reducible
					
					System.out.println("lbState : " + lbState + " != nt :" + nt);
					
					state = lookbehind + " " + state;
					state = state.trim();
					
					nt = this.grammar.getReducedEquil(state);
					
					if (!lockedStack.isEmpty())
						stack = lockedStack.morph(stack);	
					
					state = reduceTo(state, nt);
				} else {
					nt = this.grammar.getReducedEquil(state);
					
					if (state.equals(nt))
						break;					
					
					nt = newReduce(state, lookahead);
					
					if (state.equals(nt))
						break;
					
					state = nt;
				}
			}
			
			return state;
		case 1:
			return reduceTo(state, nt);
		}
	}
	
	public ArrayList<ArrayList<Node>> parseString(ArrayList<Node> nodes) {
		ArrayList<ArrayList<Node>> trees = new ArrayList<ArrayList<Node>>();
		
		System.out.println("ps nodes: " + nodes);
		
		ArrayList<Node> tmp = new ArrayList<Node>();
		ArrayList<String> open = new ArrayList<String>();
		
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
				if (open.isEmpty() || (open.contains("l_paren") && open.size() == 1)) {
					tmp.add(node);
					trees.add(tmp);
					tmp = new ArrayList<Node>();
				}
				open.remove("l_paren");
			} else if (node.token.getTokenLabel().equals("r_brace")) {
				if (open.isEmpty() || (open.contains("l_brace") && open.size() == 1)) {
					tmp.add(node);
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
	
	public ArrayList<Node> fuse(ArrayList<Node> src, ArrayList<Node> dest) {
		for (Node node : src) {
			dest.add(node);
		}
		
		return dest;
	}
	
	public Node newParse(ArrayList<ArrayList<Node>> trees) {
		ArrayList<String> map = new ArrayList<String>();
		ArrayList<ArrayList<ArrayList<Node>>> reducesTrees = new ArrayList<ArrayList<ArrayList<Node>>>();
		HashSet<String> choicesString;
		int i = 0;
		
		System.out.println(trees.get(i+1).get(0).toString());
		
		while (i < trees.size()) {
			String state = getSpacedArray(trees.get(i));
			String lookahead = "";
			ArrayList<ArrayList<Node>> tmp = new ArrayList<ArrayList<Node>>();
			int j = 0;
			
			System.out.println(trees.get(i));
			
			if (state.contains("semi")) {
				map.add("varDeclaration");
				tmp.add(trees.get(i));
				reducesTrees.add(tmp);
				i++;
				continue;
			}
			
			choicesString = new HashSet<String>();
			HashSet<Rule> choices = this.grammar.getPossibleRules(state);
			
			for (Rule choice : choices) {
				choicesString.add(choice.getLeftSide());
			}
			
			//System.out.println(choicesString);
			
			
			tmp.add(trees.get(i+j));
			
			while (choicesString.size() > 1) {
				
				j++;
				
				 lookahead = trees.get(i+j).get(0).toString();
				 
				 if (lookahead.equals("l_brace")) {
					 lookahead = "compoundStmt";
				 }
				 
				 //System.out.println("lookahead " + lookahead);
				 
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
				
				tmp.add(trees.get(i+j));
			}
			
			reducesTrees.add(tmp);
			
			for (String s : choicesString) {
				map.add(s);
			}
			
			i += j+1;
		}
		
		System.out.println(map);
		System.out.println(reducesTrees);
		
		ArrayList<Node> finalNodes = new ArrayList<Node>();
		
		System.out.println(reducesTrees.size());
		
		for (i = 0; i < reducesTrees.size(); i++) {
			System.out.println("\n----------------------\n");
			Node checked = checkParse(reducesTrees.get(i), map.get(i));
			
			if (checked == null) {
				return null;
			} else {
				finalNodes.add(checked);
				System.out.println("finalNiodes: " + finalNodes + "\n");
			}
		}
		
		System.out.println("wtf\n");
		
		return parse(finalNodes, "program");
	}
	
	public boolean evalStack(ArrayList<Node> check, String goal, int index) {
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
	
	public ArrayList<String> toArrayList(String[] arr) {
		ArrayList<String> out = new ArrayList<String>();
		
		for (int i = 0; i < arr.length; i++) {
			out.add(arr[i]);
		}
		
		return out;
	} 
	
	public String arraySubstring(ArrayList<Node> nodes, int start, int end) {
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
	
	public Node test(ArrayList<Node> nodes, String goal, boolean root) {
		/* attempt to reduce the stream of symbols into the goal */
		stack = new ArrayList<Node>();
		lockedStack = new LockedStack();
		Node semi = null;
		Node parent = null;
		
		System.out.println("nodes " + nodes + " wants " + goal);
		
		if (goal.equals("statement")) {
			System.out.println("statement ya");
			if (!nodes.get(nodes.size()-1).toString().equals("semi")) {
				return null;
			} else {
				semi = nodes.remove(nodes.size()-1);
				goal = "statementsemi";
			}			
		}
		
		if (goal.equals("statementsemi") && this.grammar.computePrevs("semi").contains(nodes.get(0).toString())) {
			parent = nodes.get(0);
			
			Node newChild = parent;
			parent = new Node(new Token(null,  "statement", null, null));
			parent.addChild(newChild);
			parent.addChild(new Node(new Token(null, "semi", null, null)));
			
			return parent;
		}
		
		//HashSet<Rule> choices = new HashSet<Rule>();
		Rule choice = null;
		
		System.out.println(nodes);
		
		if (nodes.get(0).toString().equals(goal)) 
			return nodes.get(0);
		
		/* there should exist a rule where at least the first or second terminal exist */
		
		for (int i = nodes.size() - 1; i >= 0; i--) {
			boolean stop = false;
			
			for (Rule rule : this.grammar.getRules()) {
				String rhs = getSpacedArray(rule.getRightSide());
				
				//System.out.println(nodes.size());
				//System.out.println(rhs);
				//System.out.println(nodes.size() == 1 && rhs.equals(nodes.get(0).toString()));
				
				if (nodes.size() == 1) {
					if (rhs.equals(nodes.get(0).toString())) {
						choice = rule;
						stop = true;
						break;
					} 					
				} else {
					if (i > 0 && rhs.contains(arraySubstring(nodes, 0, i+1))) {
						//choices.add(rule);
						choice = rule;
						stop = true;
						break;
					} else if (i == 0 && arrContains(rule.getRightSide(), nodes.get(i).toString())) {
						//choices.add(rule);
						choice = rule;
						stop = true;
						break;					
					}					
				}
			}
			
			if (choice == null) {
				for (Rule rule : this.grammar.getRules()) {
					String rhs = getSpacedArray(rule.getRightSide());
					
					if (rhs.contains(arraySubstring(nodes, 1, 2))) {
						choice = rule;
						stop = true;
						break;
					}
				}				
			}
			
			if (stop)
				break;
		} 
		
		System.out.println("choice " + choice);
		
		if (choice == null || choice.getLeftSide().equals("elifList"))
			return null;
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
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
		
		ArrayList<Node> next = new ArrayList<Node>();
		
		if (nodes.get(0).toString().equals(goal)) {
			return nodes.get(0);
		} else {			
			for (i = 0; i < choice.getRightSide().length; i++) {
				ArrayList<Node> tmp = new ArrayList<Node>();				
				if (choice.getRightSide().length > 1) {
					parent = new Node(new Token(null,  choice.getRightSide()[i], null, null));
					
					for (int j = 0; j < indexes.size(); j++) {
						if (indexes.get(j).equals(i)) {
							tmp.add(nodes.get(j));
						}
					}
					
					System.out.println("tmp: " + tmp);
					parent.addChild(test(tmp, choice.getRightSide()[i], false));
					next.add(parent);					
				} else {
					parent = new Node(new Token(null,  choice.getLeftSide(), null, null));
					parent.addChild(nodes.get(i));
					tmp.add(parent);
					next.add(test(tmp, goal, false));
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
			ArrayList<Node> finalTest = new ArrayList<Node>();
			finalTest.add(parent);
			return test(finalTest, goal, true);
		}
		
		//System.out.println("Final parent " + parent);
		
		if (parent.toString().equals(goal))
			System.out.println("Final parent " + parent);
		
		return parent;
	}
	
	public Node checkParse(ArrayList<ArrayList<Node>> nodes, String goal) {
		ArrayList<Node> checkStack = new ArrayList<Node>();
		
		System.out.println(nodes);
		
		List<Rule> options = this.grammar.getRules(goal);
		String lgoal = "";
		String rgoal = "";
		
		int i = 0;
		
		for (ArrayList<Node> nd : nodes) {
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
			//System.out.println(nd.get(0).toString().equals("l_paren"));
			//System.out.println(nd.get(1).toString().equals("r_paren"));
			
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
					
					Node parsed = parse(nd, newGoal);
					
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
					
					System.out.println("nd = " + nd);
					System.out.println("newGoal = " + newGoal);
					
					//Node parsed = test(nd, newGoal, true);
					Node parsed = parse(nd, "compoundStmt");
					
					//if (parsed.equals("statement")) {
						Node parent = new Node(new Token(null, "compoundStmt", null, null));
						//Node stmt = new Node(new Token(null, "statementList", null, null));
						//stmt.addChild(parsed);
						parent.addChild(left);
						//parent.addChild(stmt);
						parent.addChild(parsed);
						parent.addChild(right);
						parsed = parent;
					//}
					
					if (parsed != null && parsed.toString().equals("compoundStmt")) {
						checkStack.add(parsed);
					} else {
						return null;
					}
					
					System.out.println("stack est " + checkStack);
					
				} else {
					System.out.println("Missing closing parenthesis");
					return null;
				}				
			} else {
				System.out.println("nd = " + nd);
				parse(nd, goal);
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
		ArrayList<ArrayList<Node>> tmp = parseString(tokensToNodes());
		Node root = newParse(tmp);
		
		if (tmp == null || tmp.isEmpty() || root == null)
			return false;
		else
			return root.toString().equals("program");
	}
	
	public Node parse(ArrayList<Node> nodes, String goal) {
		Iterator<Node> tokenIt = nodes.iterator();	// used to iterate through list of tokens
		Node lookahead = tokenIt.next();						// looks ahead to next token to be read
		Node token = lookahead;
		stack = new ArrayList<Node>();
		lockedStack = new LockedStack();
		
		System.out.println(nodes);
		
		while (token != null) {
			/* establish token, lookahead, and lookbehind */
			token = lookahead;
			String lookbehind = lockedStack.peekString();
			String state = "";
			
			try {
				lookahead = tokenIt.next();
			} catch (Exception e) {
				lookahead = null;
			}
			
			if (token != null) {
				/* push token onto stack */
				//System.out.println("\n------------------------------------------------------------------------------------------------------");
				//System.out.println("Adding \"" + token + "\" to the stack\n");
				stack.add(token);
			}
			
			//System.out.println("Current stack: " + stack + "\n");
			//System.out.println("Current locked stack: " + lockedStack + "\n");
			
			/* set state of the stack */
			state = getSpacedArray(stack);
			
			//System.out.println("state = "  + state + "\n");
			
			/* 
			 * check if state can be reduced 
			 * if true, check if it should be reduced
			 * else, lock the stack
			 */
			if (canBeReduced(state)) {
				state = newReduce(state, lookahead == null ? null : lookahead.toString());
			} else {
				//System.out.println("Locking current stack\n");
				Node tmp = stack.remove(stack.size()-1);
				lockedStack.push(stack);
				stack = new ArrayList<Node>();
				stack.add(tmp);
				state = newReduce(tmp.toString(), lookahead == null ? null : lookahead.toString());
				//System.out.println("stack is now: " + stack);				
			}
			
			if (stack.size() == 1 && stackContains(goal) && lockedStack.isEmpty()) {
				System.out.println("Returning " + stack);
				return stack.get(0);
			}
		}
		
		return null;
	}
}
