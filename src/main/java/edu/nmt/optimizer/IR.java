package edu.nmt.optimizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nmt.RuntimeSettings;
import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;

/**
 * Convert an abstract syntax tree into a linearized intermediate representation.
 * Iteratively traverse the uppermost statement list (which represent each individual line of code) of a function,
 * and then recursively convert each of these subtrees into its linearized form with temporary variables
 * @author	mattadik123
 * @dated	03/02/2020
 * @todo	implement switch statements
 *
 */
public class IR {
	ASTParser a;
	private Boolean hasBreakOrGoto = false;
	private Integer instrCount = 1;
	private String fileName;
	private ArrayList<String> functionOrder;
	public Map<String, Instruction> labelMap;
	private Map<String, List<Instruction>> functionIRs;
	private List<String> ignoredLabels = new ArrayList<String>(Arrays.asList(
			"compoundStmt",
			"statementList",
			"exprStmt"
		));
	
	public IR() {
		this.functionIRs = new HashMap<String, List<Instruction>>();
	}
	
	public IR(ASTParser a) {
		this(a.getRoot());
		this.a = a;
	}
	
	public IR(Node root) {
		this.functionIRs = new HashMap<String, List<Instruction>>();
		this.buildFunctionIRs(root);
	}

	public Map<String, List<Instruction>> getFunctionIRs() {
		return functionIRs;
	}

	public void setFunctionIRs(Map<String, List<Instruction>> functionIRs) {
		this.functionIRs = functionIRs;
	}
	
	private Node convertSwitchStmt(Node switchStmt) {
		Node topIfStmt = new Node(new Token(null, "ifStmt"));
		Node identifier = switchStmt.getChildren().get(0);
		Node caseList = switchStmt.getChildren().get(1);
		List<Node> switchCasesAsConditions = new ArrayList<Node>();
		Collections.reverse(caseList.getChildren());
		
		for (Node c : caseList.getChildren()) {
			Node switchLabel = c.getChildren().get(1);
			switchCasesAsConditions.add(this.buildSwitchCondition(identifier, switchLabel));
		}
		
		for (int i = 0; i < switchCasesAsConditions.size(); i++) {
			Node condition = switchCasesAsConditions.get(i);
			topIfStmt.getChildren().add(condition);
			condition.setParent(topIfStmt);
			Node compoundStmt = new Node(new Token(null, "compoundStmt"));
			Node compoundStmtBody = caseList.getChildren().get(i).getChildren().get(0);
//			List<Node> compoundStmtChildren = new ArrayList<Node>(compoundStmtBody.getChildren()); 
		}
		
		return null;
	}
	
	private Node buildSwitchCondition(Node identifier, Node switchLabel) {
		if (switchLabel.getChildren().isEmpty()) {
			return null;
		}
		
		Node condition = new Node(new Token(null, "condition"));
		Node boolExpr = new Node(new Token(null, "boolExpr"));
		Node testVal = new Node(switchLabel.getChildren().get(0));
		Node testID = new Node(identifier);
		
		condition.addChild(boolExpr);
		boolExpr.setParent(condition);
		boolExpr.setOp("==");
		boolExpr.addChild(testVal);
		boolExpr.addChild(testID);
		testVal.setParent(boolExpr);
		testID.setParent(boolExpr);

		return condition;
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
				operandList.add(returnInstr.get(returnInstr.size() - 1));
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
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		List<Instruction> argListInstr = new ArrayList<Instruction>();
		List<Instruction> paramList = new ArrayList<Instruction>();
		Node args = null;
		Node funcID = null;
		if (call.getChildren().size() > 1) {
			args = call.getChildren().get(0);
			funcID = call.getChildren().get(1);
		} else {
			funcID = call.getChildren().get(0);
		}
		
		if (args != null && args.getToken().getTokenLabel().contentEquals("argList")) {
			Collections.reverse(args.getChildren());
			for (Node c : args.getChildren()) {
				argListInstr.addAll(this.buildInstruction(c));
				paramList.add(argListInstr.get(argListInstr.size() - 1));
			}
		} else if (args != null) {
			argListInstr.addAll(this.buildInstruction(args));
			paramList.add(argListInstr.get(argListInstr.size() - 1));
		}
		
		Instruction callInstr = new CallInstruction(funcID, paramList, this.instrCount);
		this.instrCount++;
		
		returnInstr.addAll(argListInstr);
		returnInstr.add(callInstr);
		
		return returnInstr;
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
		Node paramList = null;
		if (node.getToken().getTokenLabel().contentEquals("funcDefinition") && node.getChildren().size() > 1) {
			paramList = node.getChildren().get(1);
		}
		
		if (paramList != null) {
			if (paramList.getToken().getTokenLabel().contentEquals("paramList")) {
				Collections.reverse(paramList.getChildren());
				for (Node c : paramList.getChildren()) {
					returnInstr.addAll(this.buildInstruction(c)); 
				}
			} else {
				returnInstr.addAll(this.buildInstruction(paramList));
			}
		}
		
		returnInstr.addAll(this.buildInstruction(node.getChildren().get(0)));
		
		return returnInstr;

	}
	
	public Integer getInstrCount() {
		return instrCount;
	}

	public void setInstrCount(Integer instrCount) {
		this.instrCount = instrCount;
	}

	public void buildFunctionIRs(Node root) {
		
		this.functionOrder = new ArrayList<String>();
		
		if (this.functionIRs == null) {
			this.functionIRs = new HashMap<String, List<Instruction>>();
		}
		for (Node c : root.getChildren()) {
			if (c.getToken().getTokenLabel().contentEquals("funcDefinition")) {
				functionOrder.add(c.getName());
				this.functionIRs.put(c.getName(), this.buildInstructionList(c));
				if (this.hasBreakOrGoto) {
					this.fixJumpDestinations(functionIRs.get(c.getName()));
					this.hasBreakOrGoto = false;
				}
			}
		}
		
//		Collections.reverse(functionOrder);
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
	
	public String getFilename() {
		return this.fileName;
	}
	
	public boolean equals(IR ir2) {
		for (String key : this.functionIRs.keySet()) {
			if (!ir2.functionIRs.keySet().contains(key))
				return false;
			
			List<Instruction> il1 = this.functionIRs.get(key);
			List<Instruction> il2 = ir2.functionIRs.get(key);
			
			for (int i = 0; i < il1.size(); i++) {
				if (!il1.get(i).equals(il2.get(i)))
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * default outputToFile class, uses filename from parser
	 */
	public void outputToFile() {
		this.fileName = this.a.getFilename().split(".c")[0] + ".ir";
		outputToFile(this.fileName);
	}
	
	/**
	 * outputs IR to readable format for input
	 * @param filename is the name of file without extension
	 */
	public void outputToFile(String filename) {
		try {
			String file = "";
			this.fileName = filename;
			FileWriter writer = new FileWriter(filename);
			
			for (String key : this.functionOrder) {
				file += "#" + key + "\n";
				
				for (Instruction instr : this.functionIRs.get(key)) {
					file += instr.instrToStr() + "\n";
				}
			}
			
			writer.write(file);
			writer.close();
			
			System.out.println("Successfully wrote IR to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * intialize an IR from a file
	 * @param fileName is the file to init from, no extension (file needs to be .ir)
	 */
	public void initFromFile(String fileName) {
		java.util.Scanner irScanner = null;
		this.labelMap = new HashMap<String, Instruction>();
		this.functionOrder = new ArrayList<String>();
		Map <String, Integer> functionIndex = new HashMap<String, Integer>();
		
		ArrayList<Instruction> instructionList = new ArrayList<Instruction>();
		
		try {
			File irFile = new File(fileName);
			irScanner = new java.util.Scanner(irFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (irScanner.hasNextLine()) {
			String line = irScanner.nextLine();

			if (line.charAt(0) == '#') {
				functionOrder.add(line.substring(1, line.length()));
				functionIndex.put(line.substring(1, line.length()), instructionList.size());

			} else {
				instructionList.add(new Instruction());
				String[] lineSplit = line.split(" ");
				
				int currentIndex = Integer.parseInt(lineSplit[0]) - 1;
				Instruction op1 = null;  
				Instruction op2 = null;
				
				
				// special case for return
				if (lineSplit[2].equals("return")) {
					ReturnInstruction tmp = new ReturnInstruction();
					Instruction tmp2 = instructionList.get(currentIndex);
					
					
					tmp.setOperation("return");
					tmp.setOp1Name(lineSplit[5]);
					tmp.setInstrID("return");
					tmp.setLineNumber(Integer.parseInt(lineSplit[0]));
					tmp.setOperand1(instructionList.get((Integer.parseInt(lineSplit[4]) - 1)));					
					tmp.setType(lineSplit[3]);
					
					instructionList.add(currentIndex, tmp);
					instructionList.remove(tmp2);
					
					
				// special case for call
				} if (lineSplit[2].equals("call")) {
					
					ArrayList<String> params = new ArrayList<String>();
					ArrayList<Instruction> paramList = new ArrayList<Instruction>();
					
					for (int i = 7; i < lineSplit.length; i++) {
						params.add(lineSplit[i]);
					}
					
					CallInstruction tmp = new CallInstruction();
					Instruction tmp2 = instructionList.get(currentIndex);
	
					tmp.setOperation("call");
					
					tmp.setInstrID(lineSplit[1]);
					tmp.setOp1Name(lineSplit[5]);
					tmp.setLineNumber(Integer.parseInt(lineSplit[0]));
					
//					tmp.setOperand1(instructionList.get((Integer.parseInt(lineSplit[4]) - 1)));					
					tmp.setType(lineSplit[3]);
					
					for (String param : params) {
						for (Instruction i : instructionList) {
							if (i.getInstrID().equals(param)) {
								paramList.add(i);
								break;
							}
						}
					}
					
					tmp.setParamList(paramList);
					
//					
					instructionList.add(currentIndex, tmp);
					instructionList.remove(tmp2);
//					
				// special case for jump
				} else if (lineSplit[2].equals("jump")) {
					JumpInstruction tmp = new JumpInstruction();
					Instruction tmp2 = instructionList.get(currentIndex);
					
					
					tmp.setOperation("jump");
					
//					tmp.setInstrID("jump");
					tmp.setInstrID(lineSplit[6]);
					tmp.setLineNumber(Integer.parseInt(lineSplit[0]));
					
					try {
						tmp.setOperand1(instructionList.get((Integer.parseInt(lineSplit[4]) - 1)));	
					} catch (java.lang.NumberFormatException e) {
						tmp.setOperand1(null);
					}
					tmp.setOp1Name(lineSplit[5]);
					
					
									
					tmp.setType(lineSplit[3]);
					
					instructionList.add(currentIndex, tmp);
					instructionList.remove(tmp2);
						
				} else {					
					if (!lineSplit[4].equals("null")) {
						int op1Index = Integer.parseInt(lineSplit[4]) - 1;
						
						while (op1Index >= instructionList.size()) {
							instructionList.add(new Instruction());
						}
						
						op1 = instructionList.get(op1Index);
					}
					
					if (!lineSplit[6].equals("null")) {
						int op2Index = Integer.parseInt(lineSplit[6]) - 1;
						
						while (op2Index >= instructionList.size()) {
							instructionList.add(new Instruction());
						}
						
						op2 = instructionList.get(op2Index);
					}
					
					
					instructionList.get(currentIndex).copy(Instruction.strToInstr(line, op1, op2));
				}	
				
			}
		}
		
		if (instructionList != null) {
			for (Instruction inst: instructionList) {
				if (inst.getOperation() != null && inst.getOperation().equals("jump")) {
					String dest = inst.getInstrID();
					inst.setInstrID("jump");
					inst.setOperand2(instructionList.get(Integer.parseInt(dest) - 1));							
				}
			}
		}
		
		irScanner.close();
		
		
		// invert lookup
		Map<Integer, String> functionIndexInv = new HashMap<Integer, String>();
		for(Map.Entry<String, Integer> entry : functionIndex.entrySet()){
			functionIndexInv.put(entry.getValue(), entry.getKey());
		}
		
		// fix functions
		
		ArrayList<Integer> functionBounds = new ArrayList<Integer>();
		
		for (String funcName : functionIndex.keySet()) {
			functionBounds.add(functionIndex.get(funcName));
		}
		
		functionBounds.add(instructionList.size());
		Collections.sort(functionBounds);
		
		for (int i = 0; i < functionBounds.size() - 1; i++) {
			int curLine = functionBounds.get(i);
			int nextLine = functionBounds.get(i + 1);
			this.functionIRs.put(functionIndexInv.get(curLine), instructionList.subList(curLine, nextLine));
		}
		
	}
	
	public static void printIR(IR ir) {
		for (String funcName : ir.getFunctionOrder()) {
			System.out.println("# " + funcName);
			IR.printFunc(ir.getFunctionIRs(), funcName);
		}
	}
	
	private ArrayList<String> getFunctionOrder() {
		return this.functionOrder;
	}

	public static void printMain(Map<String, List<Instruction>> functionMap) {
		List<Instruction> instrList = functionMap.get("main");
		for (Instruction i : instrList) {
			System.out.println(i.getLineNumber() + ": " + i + " type: " + i.getType());
		}
	}
	
	public static void printFunc(Map<String, List<Instruction>> functionMap, String funcName) {
		List<Instruction> instrList = functionMap.get(funcName);
		for (Instruction i : instrList) {
			System.out.println(i.getLineNumber() + ": " + i + " type: " + i.getType());
		}
	}

	
	
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner("test/function.c");

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
		List<Instruction> foo = test.getFunctionIRs().get("foo");
//		System.out.println(mainList.get(0));
		IR.printMain(test.getFunctionIRs());
//		System.out.println("printing foo");
		IR.printFunc(test.getFunctionIRs(), "foo");
		
		//test.printIR();
		
		test.outputToFile();
		//IR tmp = new IR();
		//tmp.initFromFile(test.getFilename());
		//System.out.println(test.equals(tmp));
	}
}
