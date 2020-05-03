package edu.nmt.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nmt.RuntimeSettings;
import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;
import edu.nmt.optimizer.IR;
import edu.nmt.optimizer.Instruction;

public class Translator {

	private Map<String, Integer> typeSizes = new HashMap<String, Integer>();
 	
	{
		typeSizes.put("int", 4);		
	}
	
	private IR ir;
	private ASTParser a;

	
	public Translator(IR ir, ASTParser a) {
		this.ir = ir;
		this.a = a;
//		this.asm = new ArrayList<String>();
	}
	
	public Integer getNextBaseOffset(Map<String, Integer> variableOffsets) {
		
		try {
			// Max offset.
			return Collections.min(variableOffsets.values());
		} catch (java.util.NoSuchElementException e) {
			return 0;
		}		
	}
	
	public String getSizeModifier(Integer size) {
		switch (size) {
		case 4: 
			return "l";
		case 8:
			return "q";
		}
		return null;
	}
	
	public String getRegisterModifier(Integer size) {
		switch (size) {
		case 4: 
			return "e";
		case 8:
			return "r";
		}
		return null;
		
	}
	
	
	public void translate() {
		
		Integer funcIndex = 0;
		Map<String, Integer> variableOffsets;
		Map<String, Integer> variableSizes;
		Map<String, Node> funcScope;
		List<String> asm;
		Map<String, List<String>> funcAsm = new HashMap<String, List<String>>();
		List<String> fileAsm = new ArrayList<String>();
		
		// file header
		fileAsm.add("\t.file\t\"" + RuntimeSettings.sourceFilename + "\"\n");
		fileAsm.add("\t.text\n");
		fileAsm.add("\t.globl	main\n");
		fileAsm.add("\t.type	main, @function\n");		
	
		
		// translate each function
		for (String funcName : ir.getFunctionIRs().keySet()) {		
			asm = new ArrayList<String>();
			funcScope = a.getFunctionSymbolTable(funcName);
			
			variableOffsets = new HashMap<String, Integer>();
			variableSizes = new HashMap<String, Integer>();
			
			// function label
			asm.add(funcName + ":\n");
			
			// function preamble
			asm.add(".LFB" + funcIndex + ":\n");
			asm.add("\t.cfi_startproc\n");
			asm.add("\tpushq	%rbp\n");
			asm.add("\t.cfi_def_cfa_offset 16\n");
			asm.add("\t.cfi_offset 6, -16\n");
			asm.add("\tmovq	%rsp, %rbp\n");
			asm.add("\t.cfi_def_cfa_register 6\n");
			
			// adjust stack pointer for necessary locals
			
			
//			asm.add("\tsubq	$16, %rsp\n");
//			asm.add("\tmovl	$7, -4(%rbp)\n");
//			asm.add("\tmovl	-4(%rbp), %eax\n");

			
			
			List<Instruction> funcInstr = ir.getFunctionIRs().get(funcName);
			
			for (Instruction inst : funcInstr) {
			
				System.out.println(inst.getOperation() + " goes to: " + inst);
				
				// since this is already linearized, just simply translate Instruction object to corresponding assembly command(s)
				if (inst.getOperation() == null) {
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					String instrValue = inst.getOperand1().getInstrID();
					
					System.out.println(instrValue);
					System.out.println(variableOffsets.get(instrValue));
					
					asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t" + variableOffsets.get(instrValue) + "(%rbp), " + offset + "(%rbp)\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
					
				} else if (inst.getOperation().equals("numeric_constant")) {
					System.out.println("Constant: "+inst);
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);

					asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t$" + inst.getOp1Name() + ", " + offset + "(%rbp)\n");
//					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
					
					
//					System.out.println("HERE: " + inst.getInstrID());
					
//					System.out.println(inst.getType());
//					System.out.println(typeSizes.get(inst.getType()));

					
				} else if (inst.getOperation().equals("return")) {					
					String returnValueName = inst.getOp1Name();
					Integer returnValueSize = variableSizes.get(returnValueName);
					Integer offset = variableOffsets.get(returnValueName);
					
					System.out.println(returnValueSize);
					
					
					asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t" + offset + "(%rbp), %" + getRegisterModifier(returnValueSize) + "ax\n");					
				} else if (inst.getOperation().equals("+") || inst.getOperation().equals("-")) {	
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					/* move first operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					offset = variableOffsets.get(instrValue2);
					
					if (inst.getOperation().equals("+"))
						/* add second operand to register */
						asm.add("\tadd" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else
						/* sub second operand to register */
						asm.add("\tsub" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);;
					
					/* move register value to stack */
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp), \n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("identifier")) {
					/* assigning the value of an identifier to a variable */
					System.out.println(inst.getOp1Name());
					String id = inst.getOp1Name();
					String instrValue = inst.getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(id));
					Integer idOffset = variableOffsets.get(id);
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					/* move register value to stack */
					asm.add("\tmov" + sizeModifier + "\t" + idOffset + "(%rbp), " + offset + "(%rbp), \n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("*")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					/* move first operand into ax */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "ax\n");
					
					offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					/* empty rdx */
					asm.add("\tmov" + sizeModifier + "\t$0, " + regModifier + "dx\n");
					
					/* multiply*/
					asm.add("\timul\t%" + regModifier + "bx\n");
					
					/* move result into dest */
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "ax, " + offset + "(%rbp), \n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("/")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					/* move first operand into ax */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "ax\n");
					
					offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					/* empty rdx */
					asm.add("\tmov" + sizeModifier + "\t$0, " + regModifier + "dx\n");
					
					/* divide */
					asm.add("\tidiv\t%" + regModifier + "bx\n");
					
					/* move result into dest */
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "ax, " + offset + "(%rbp), \n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if(inst.getType().equals("conditionalJump")) {
					// Build jump statement
					asm.add("\tJMP CONDITIONAL" + "\t" + "\n");
				} else if(inst.getType().equals("unconditionalJump")) {
					String splitres[];
					splitres = inst.toString().split(" ");
					asm.add("\tJMP" + "\t" +splitres[1] + "\n");
				} else if(inst.getOperation().equals("label")) {
					String splitres[];
					splitres = inst.toString().split("=");
					
					asm.add(splitres[0].replace(" ", "") + ": \n");
				} else if(inst.getType().equals("boolean")) {
					String splitres[];
					
					// Process statement
					splitres = inst.toString().split(" ");
					
					asm.add("\tCOMPARISON" + "\n");
				}
			}
			
			// function footer
			asm.add("\tpopq	%rbp\n");
			asm.add("\t.cfi_def_cfa 7, 8\n");
			asm.add("\tret\n");
			asm.add("\t.cfi_endproc\n");			
		

			funcAsm.put(funcName, asm);
			fileAsm.addAll(asm);
		}
		
		System.out.println("\n");
		for(String s : fileAsm) {
			System.out.print(s);
		}
		
		
		
	}
	
	
	public static void main(String argv[]) throws IOException {
		Scanner s = new Scanner("test/conditions.c");
    	s.scan();
    
//		s.printTokens();

    	// Initialize grammar
    	Grammar grammar = new Grammar(RuntimeSettings.grammarFile);
    	grammar.loadGrammar();
    	
    	// Start parser
    	Parser p = new Parser(grammar, s);  

    	if (p.parse()) {
    		
//    		p.printParseTree();
        	        	
        	// Start AST Parser
    		ASTParser a = new ASTParser(p);
    		
    		if (a.parse()) {
//   			a.printAST();
    			
	    		a.printSymbolTable();
    		}
    		
			IR ir = new IR(a);
			IR.printIR(ir);
			
			Translator t = new Translator(ir, a);
			t.translate();
    	}
	
		
	}
	
}
