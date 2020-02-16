package edu.nmt.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.nmt.util.Debugger;

/**
 * handles interactions with the automata
 * @author Terence
 *
 */
public class Action {

	private ActionType type;
	private Goto state;
	private Stack<Goto> stack;
	private Stack<Goto> goals;
	private Token lookahead;
	private Debugger debugger;
	private Node root;
	private int errorCode;
	private Token expected;
	
	Action() {
		this.type = ActionType.SHIFT;
		this.debugger = new Debugger(false);
		this.stack = new Stack<Goto>();
		this.goals = new Stack<Goto>();
		this.state = null;
		this.root = null;
		this.errorCode = 0;
	}
	
	Action(Debugger db) {
		this.type = ActionType.SHIFT;
		this.debugger = db;
		this.stack = new Stack<Goto>();
		this.goals = new Stack<Goto>();
		this.state = null;
		this.root = null;
		this.errorCode = 0;
	}
	
	/**
	 * push lstate onto the stack, push a lock behind it, and 
	 * push lstate to the goals stack
	 * @param lstate is the state to push
	 */
	private void lockState(Goto lstate) {
		debugger.print("Pushing " + lstate + " onto the stack!");
		
		this.stack.push(lstate);
		this.stack.push(Goto.getLock());
		this.goals.push(lstate);
	}
	
	/**
	 * attempt to transition current state to lookahead
	 * @param lookahead is the next token to transition to
	 * @return the type of the next action phase
	 * @throws NullPointerException
	 */
	public ActionType shift(Token lookahead) throws NullPointerException {
		
		this.lookahead = lookahead;
		
		debugger.print("shifting from " + this.state + " to " + lookahead);
		debugger.print(stack);
		
		if (this.state == null) {
			// first call to sets the start state
			this.state = Goto.get(lookahead.getTokenLabel());
			this.stack.push(Goto.getLock());
			this.stack.push(this.state);
			return ActionType.SHIFT;
		} else if (lookahead == null) {
			// end of token stream
			return ActionType.REDUCE;			
		} else {
			// check if this state can directly transition to token
			debugger.print("Transition from " + this.state + " to " + lookahead + "\n");
			
			Goto nextState = this.state.makeTransition(lookahead.getTokenLabel());
	
			if (nextState == null) {
				// cannot transition to token
				
				if (this.state.terminateTo() != null) {
					// can transition to end state
					debugger.print(this.state + " can transition to end state " + this.state.terminateTo());
					
					return ActionType.REDUCE;
				} else if (this.state.canRepeat()) {
					// is a repeat state
					
					this.stack.push(Goto.getLock());
					this.goals.push(this.state);
					this.state = this.stack.push(Goto.get(lookahead.getTokenLabel()));
					
					return ActionType.SHIFT;
				} else {
					// push non-terminal to stack and goals stack and lock
					nextState = this.state.nextState(true);
					
					// if the next state is a terminal, reject and print error
					if (Goto.grammar.isTerminal(nextState.toString())) {
						this.setError(1);
						return ActionType.REJECT;
					}
					
					debugger.print(this.state + " can transition to non terminal " + nextState);
					
					this.lockState(nextState);
					
					// this state will now begin at the token symbol start state
					this.state = this.stack.push(Goto.get(lookahead.getTokenLabel()));
					
					return ActionType.SHIFT;					
				}
			} else {
				// this state can transition to lookahead, so add it to stack
				debugger.print("Adding " + nextState + " to the stack");
				debugger.print(this.stack);
				
				this.state = this.stack.push(nextState);
				
				return ActionType.SHIFT;
			}
		}
	} 
	
	/**
	 * reduce items on stack until empty or lock symbol
	 * into non-terminal end state
	 * update root node of parse tree as side effect
	 * @return the type of the next action phase
	 */
	public ActionType reduce() {
		List<Goto> storage = new ArrayList<Goto>();
		
		debugger.print(stack);
		
		// pop all children off the stack until lock symbol
		while (!this.stack.peek().toString().equals("$")) {
			Goto tmp = this.stack.pop(); 
			storage.add(tmp);
		}
		
		// get the non-terminal end state
		Goto nt = this.state.terminateTo();
	
		if (nt.toString().equals("program") && this.lookahead != null) {
			// prevent the first declarationList from becoming a program
			this.lockState(this.state);
			this.state = Goto.get(lookahead.getTokenLabel());
			this.stack.push(this.state);
			
			return ActionType.SHIFT;
		}
		
		
		if (!this.goals.isEmpty() && nt.toString().equals(this.goals.peek().toString())) {
			// remove the lock
			this.stack.pop();
			this.stack.pop();
			this.state = this.goals.pop();
		} else {
			// set the current state to the parent
			this.state = Goto.get(nt.toString());
			
			if (this.state == null)
				this.state = nt;
		}
		
		// push the children back on the stack
		for (int i = 0; i < storage.size(); i++) {
			this.state.getToken().addChild(storage.get(i).getToken());
		}
		
		this.stack.push(this.state);
		
		if (this.stack.peek().toString().equals("program")) {
			// check if top of the stack is our goal
			this.root = stack.get(1).getToken();
			return ActionType.ACCEPT;			
		}
		
		return ActionType.REPEAT;
	}
	
	public Node getRoot() {
		return this.root;
	}
	
	public ActionType getType() {
		return this.type;
	}
	
	public void setType(ActionType type) {
		this.type = type;
	}
	
	public int getError() {
		return this.errorCode;
	}
	
	public void setError(int err) {
		this.errorCode = err;
	}
}
