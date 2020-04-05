package edu.nmt.optimizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;

/**
 * 
 * @author	mattadik123
 * @todo	implement loops (requires Jump), functions (requires Call), structs, switch statements, and goto
 *
 */
public class IR {
	ASTParser a;
	public static Map<String, Instruction> variableMap = new HashMap<String, Instruction>();
	private Boolean hasBreakOrGoto = false;
	private Integer instrCount = 1;
	private List<Instruction> instructionList;
	private Map<String, List<Instruction>> functionIRs;
	private List<String> ignoredLabels = new ArrayList<String>(Arrays.asList(
			"compoundStmt",
			"statementList",
			"exprStmt"
		));
	
	public IR() {
		this.instructionList = new ArrayList<Instruction>();
	}
	
	public IR(ASTParser a) {
		this(a.getRoot());
		this.a = a;
	}
	
	public IR(Node root) {
		this.instructionList = new ArrayList<Instruction>();
		this.functionIRs = new HashMap<String, List<Instruction>>();
		this.buildFunctionIRs(root);
	}

	public List<Instruction> getInstructionList() {
		return instructionList;
	}

	public void setInstructionList(List<Instruction> instructionList) {
		this.instructionList = instructionList;
	}

	public Map<String, List<Instruction>> getFunctionIRs() {
		return functionIRs;
	}

	public void setFunctionIRs(Map<String, List<Instruction>> functionIRs) {
		this.functionIRs = functionIRs;
	}
	
	private static Node convertAssignStmt(Node assignStmt) {
		String assignOp = assignStmt.getOp();
		String op = assignOp.replace("=", "");
		Node convert = null;
		Node newChild = null;
		if (op.isEmpty()) {
			return assignStmt;
		} else {
			convert = new Node(new Token(null, "assignStmt"));
			convert.setOp("=");
			String tokenString = null;
			if (op.contentEquals("+") || op.contentEquals("-")) {
				tokenString = "addExpression";
			} else if (op.contentEquals("*") || op.contentEquals("/")) {
				tokenString = "mulExpression";
			} else if(op.contentEquals("%")) {
				tokenString = "modExpression";
			} else {
				tokenString = "bitExpression";
			}

			newChild = new Node(new Token(null, tokenString));
			newChild.setOp(op);
			newChild.setChildren(assignStmt.getChildren());
			List<Node> newChildren = new ArrayList<Node>();
			newChildren.add(newChild);
			newChildren.add(new Node(assignStmt.getChildren().get(1)));
			convert.setChildren(newChildren);
		}
		
		return convert;
	}
	
	private static Node convertIncExpr(Node incExpr) {
		Node assignStmt = new Node(new Token(null, "assignStmt"));
		Node addExpression = new Node(new Token(null, "addExpression"));
		Node numConst = new Node(new Token("1"));
		Node idDuplicate = new Node(incExpr.getChildren().get(0));
		
		assignStmt.setOp("=");
		addExpression.setOp(Character.toString(incExpr.getOp().charAt(0)));
		numConst.setType("int");
		addExpression.addChild(numConst);
		addExpression.addChild(incExpr.getChildren().get(0));
		assignStmt.addChild(addExpression);
		assignStmt.addChild(idDuplicate);
		assignStmt.setParent(incExpr.getParent().getParent());
		
		return assignStmt;
	}
	
	private List<Instruction> buildInstruction(Node node) {
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		List<Instruction> operandList = new ArrayList<Instruction>();
		Instruction add = null;
		String label = node.getToken().getTokenLabel();
		
		if (this.ignoredLabels.contains(label)) {
			for (Node c : node.getChildren()) {
				returnInstr.addAll(this.buildInstruction(c));
			}
			return returnInstr;
		}
		
		if (label.contentEquals("ifStmt")) {
			return this.buildConditional(node, null);
		} else if (label.contains("Loop")) {
			returnInstr.addAll(this.buildLoop(node));
			Instruction endOfBlock = new Instruction(null, new Node(new Token("endOfFullLoopBlock", "label")), new ArrayList<Instruction>(), this.instrCount);
			this.instrCount++;
			returnInstr.add(endOfBlock);
			return returnInstr;
		}
		
		if (!node.getChildren().isEmpty()) {
			if (label.contains("IncExpr")) {
				// converts the AST representation of unary incrementation into its full formatting (e.g. a++ becomes a = a + 1)
				// then replaces the increment expression node with the new representation (which is an assignStmt)
				Node replace = IR.convertIncExpr(node);
				int nodeIndex = node.getParent().getParent().getChildren().indexOf(node.getParent());
				node.getParent().getParent().getChildren().set(nodeIndex, replace);
				node = replace;
				label = "assignStmt";
			}
			
			if (label.contentEquals("assignStmt")) {
				// converts the AST representation of combination assignment statements (e.g. x += y) into its full formatting (e.g. x = x + y)
				Node replace = IR.convertAssignStmt(node);
				// node == replace when the assignment statement is not a combination assignment
				// when node != replace, node is replaced in its parent's child list by replace
				if (node != replace) {
					int nodeIndex = node.getParent().getChildren().indexOf(node);
					node.getParent().getChildren().set(nodeIndex, replace);
					node = replace;
				}
				// index 1 contains the left-hand side of the assignment, so there is no need to recursively create instructions
				// this is not true for index 0, which is the right-hand side
				// the returned value will be a linearized instruction list of all the sub nodes of this statement in the AST
				returnInstr.addAll(this.buildInstruction(replace.getChildren().get(0)));
				
				// final member of tmp list is the root node of this node's subtree
				operandList.add(returnInstr.get(returnInstr.size() - 1));

			} else if (label.contentEquals("call")) {
				returnInstr.addAll(this.buildCall(node));
				add = returnInstr.get(returnInstr.size() - 1);
			} else {
				for (Node c : node.getChildren()) {
					returnInstr.addAll(this.buildInstruction(c));
					operandList.add(returnInstr.get(returnInstr.size() - 1));
				}
			}
		}
		
		if (label.contentEquals("returnStmt")) {
			add = new ReturnInstruction(node, operandList, this.instrCount);
		} else if(label.contentEquals("goto")) {
			Instruction jumpLabel = new Instruction(null, new Node(new Token(node.getName(), "label")), new ArrayList<Instruction>(), this.instrCount);
			add = new JumpInstruction(node, Arrays.asList(jumpLabel), this.instrCount, node.getName() + "TEMPORARYLABEL");
			this.hasBreakOrGoto = true;
		} else if(label.contentEquals("break")) {
			Node breakNode = node;
			String breakLabel = node.getToken().getTokenLabel().toLowerCase();
			while (!breakLabel.contains("switch") && !breakLabel.contains("loop")) {
				breakNode = breakNode.getParent();
				breakLabel = breakNode.getToken().getTokenLabel().toLowerCase();
			}
			String jumpLabelString = null;
			if (breakNode.getToken().getTokenLabel().toLowerCase().contains("loop")) {
				jumpLabelString = "endOfFullLoopBlock";
			} else {
				jumpLabelString = "endOfSwitchBlock";
			}
			Instruction jumpLabel = new Instruction(null, new Node(new Token(jumpLabelString, "label")), new ArrayList<Instruction>(), this.instrCount);
			add = new JumpInstruction(node, Arrays.asList(jumpLabel), this.instrCount, jumpLabelString + "TEMPORARYLABEL");
			this.hasBreakOrGoto = true;

		} else {
			add = new Instruction(null, node, operandList, this.instrCount);
		}
		
		this.instrCount++;
		if (add != null) {
			returnInstr.add(add);
		}
		return returnInstr;
	}
	
	/**
	 * first child is parameter(s) unless no parameters
	 * if first child is argList, then multiple parameters
	 * second child is function identifier
	 * @param call
	 * @return
	 */
	private List<Instruction> buildCall(Node call) {
		Node args = call.getChildren().get(0);
		return null;
	}
	
	private List<Instruction> buildLoop(Node loopNode) {
		boolean isForLoop = loopNode.getToken().getTokenLabel().contains("for");
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		Node condition = loopNode.getChildren().get(0);
		Node init = null;
		Node increment = null;
		Node body = null;
		if (isForLoop) {
			condition = condition.getChildren().get(0);
			init = loopNode.getChildren().get(1);
			increment = loopNode.getChildren().get(2);
			body = loopNode.getChildren().get(3);
		} else {
			body = loopNode.getChildren().get(1);
		}

		
		List<Instruction> initInstr = null;
		if (isForLoop) {
			initInstr = this.buildInstruction(init);
		}
		
		Instruction endOfBody = new Instruction(null, new Node(new Token("endOfLoopBody", "label")), new ArrayList<Instruction>(), this.instrCount);
		Instruction jumpToCondition = new JumpInstruction(null, Arrays.asList(endOfBody), this.instrCount, null);
		this.instrCount++;
		
		Instruction startOfBody = new Instruction(null, new Node(new Token("startOfLoopBody", "label")), new ArrayList<Instruction>(),this.instrCount);
		this.instrCount++;
		
		List<Instruction> bodyInstr = new ArrayList<Instruction>();
		if (!body.getChildren().isEmpty()) {
			for (Node c : body.getChildren()) {
				bodyInstr.addAll(this.buildInstruction(c));
			}
		}
		
		List<Instruction> incrementInstr = null;
		if (isForLoop) {
			incrementInstr = this.buildInstruction(increment);
		}
		
		endOfBody.setLineNumber(this.instrCount);
		this.instrCount++;

		List<Instruction> operandList = new ArrayList<Instruction>();
		List<Instruction> condInstr = this.buildInstruction(condition);
		operandList.add(condInstr.get(condInstr.size() - 1));
		operandList.add(startOfBody);
		Instruction jumpToBody = new JumpInstruction(new Node(new Token(null, "loopBody")), operandList, this.instrCount, "true");
		this.instrCount++;
		
		if (isForLoop) {
			returnInstr.addAll(initInstr);
		}
		returnInstr.add(jumpToCondition);
		returnInstr.add(startOfBody);
		returnInstr.addAll(bodyInstr);
		if (isForLoop) {
			returnInstr.addAll(incrementInstr);
		}
		returnInstr.add(endOfBody);
		returnInstr.addAll(condInstr);
		returnInstr.add(jumpToBody);
		return returnInstr;
	}
	
	private List<Instruction> buildConditional(Node ifStmt, Instruction finalDestination) {
		if (finalDestination == null) {
			finalDestination = new Instruction(null, new Node(new Token("endOfFullConditionalBlock", "label")), new ArrayList<Instruction>(), this.instrCount);
		}
		Instruction endOfBlock = new Instruction(null, new Node(new Token("endOfConditionalBlock", "label")), new ArrayList<Instruction>(), this.instrCount);
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		List<Instruction> operandList = new ArrayList<Instruction>(); // operand list for conditional jump (if statement)
		Node condition = null;
		Node body = null;
		Node elseStmt = null;
		if (ifStmt.getChildren().size() == 1) {
			body = ifStmt.getChildren().get(0);
		} else {
			condition = ifStmt.getChildren().get(0);
			body = ifStmt.getChildren().get(1);
		}
		
		if (ifStmt.getChildren().size() == 3) {
			elseStmt = ifStmt.getChildren().get(2);
		}
		
		// create boolean expression and add to operandList of JumpInstruction to be constructed
		if (condition != null) {
			returnInstr.addAll(this.buildInstruction(condition.getChildren().get(0)));
			operandList.add(returnInstr.get(returnInstr.size() - 1));
			// add end of conditional block to operandList of JumpInstruction to be constructed
			operandList.add(endOfBlock);
			returnInstr.add(new JumpInstruction(ifStmt, operandList, this.instrCount, "false"));
			this.instrCount++;
		}
		// convert body nodes into instructions
		if (!body.getChildren().isEmpty()) {
			returnInstr.addAll(this.buildInstruction(body));
		}
		// unconditional jump to end of full conditional block
		if (condition != null) {
			returnInstr.add(new JumpInstruction(null, Arrays.asList(finalDestination), this.instrCount, null));
			this.instrCount++;
		}
		
		endOfBlock.setLineNumber(this.instrCount);
		this.instrCount++;
		returnInstr.add(endOfBlock);
		
		if (elseStmt != null) {
			returnInstr.addAll(this.buildConditional(elseStmt, finalDestination));
		}
		
		if (!ifStmt.getParent().getToken().getTokenLabel().contentEquals("ifStmt")) {
			finalDestination.setLineNumber(this.instrCount);
			returnInstr.add(finalDestination);
			this.instrCount++;
		}
		
		return returnInstr;
	}
	
	private List<Instruction> buildInstructionList(Node node) {
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		returnInstr.addAll(this.buildInstruction(node));
		
		return returnInstr;

	}
	
	public void buildFunctionIRs(Node root) {
		if (this.functionIRs == null) {
			this.functionIRs = new HashMap<String, List<Instruction>>();
		}
		for (Node c : root.getChildren()) {
			if (c.getToken().getTokenLabel().contentEquals("funcDefinition")) {
				this.functionIRs.put(c.getName(), this.buildInstructionList(c.getChildren().get(0)));
				if (this.hasBreakOrGoto) {
					this.fixJumpDestinations(functionIRs.get(c.getName()));
					this.hasBreakOrGoto = false;
				}
			}
		}
	}
	
	private void fixJumpDestinations(List<Instruction> functionIR) {
		for (int i = 0; i < functionIR.size(); i++) {
			Instruction x = functionIR.get(i);
			if (x.getType().contentEquals("unconditionalJump") && x.getOp1Name() != null && x.getOp1Name().contains("TEMPORARYLABEL")) {
				String trueDestination = x.getOp1Name().replace("TEMPORARYLABEL", "");
				for (int j = i + 1; j < functionIR.size(); j++) {
					Instruction y = functionIR.get(j);
					if (y.getType().contentEquals("label") && y.getOp1Name() != null && y.getOp1Name().contentEquals(trueDestination)) {
						x.setOperand2(y);
						break;
					}
				}
			}
		}
	}
	
	public void fileToIR(String fileName) {
		java.util.Scanner irScanner = null;
		
		try {
			File irFile = new File(fileName);
			irScanner = new java.util.Scanner(irFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (irScanner.hasNext()) {
			String line = irScanner.next();
			this.instructionList.add(Instruction.strToInstr(line));
		}
		
		irScanner.close();
	}
	
	public static void printMain(Map<String, List<Instruction>> functionMap) {
		List<Instruction> instrList = functionMap.get("main");
		for (Instruction i : instrList) {
			System.out.println(i.getLineNumber() + ": " + i + " type: " + i.getType());
		}
	}

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner("test/break.c");

		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		if (p.parse()) {
//			p.printParseTree();
		}
		
		ASTParser a = new ASTParser(p);
		if (a.parse()) {
			a.printAST();	
		}
		
		a.printSymbolTable();
		Node root = a.getRoot();
		root.recursiveSetDepth();
//		Node mainAST = root.getChildren().get(0).getChildren().get(0).getChildren().get(0);
		IR test = new IR(a);
		List<Instruction> mainList = test.getFunctionIRs().get("main");
//		System.out.println(mainList.get(0));
		IR.printMain(test.getFunctionIRs());
		
	}
}
