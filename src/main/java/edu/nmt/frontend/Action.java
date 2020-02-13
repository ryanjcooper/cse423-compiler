package edu.nmt.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Action {

	private ActionType type;
	private Goto state;
	private Stack<Goto> stack;
	private Stack<Goto> goals;
	private Token token;
	private Token lookahead;
	
	Action(String goal) {
		this.type = ActionType.SHIFT;
		this.state = null;
		this.stack = new Stack<Goto>();
		this.goals = new Stack<Goto>();
	}
	
	public ActionType getType() {
		return this.type;
	}
	
	public void setType(ActionType type) {
		this.type = type;
	}
	
	private void lockState(Goto lstate) {
		System.out.println("Pushing " + lstate + " onto the stack!");
		this.stack.push(lstate);
		this.stack.push(Goto.getLock());
	}
	
	public void shift(Token token, Token lookahead) throws NullPointerException {
		
		this.token = token;
		this.lookahead = lookahead;
		
		System.out.println("shifting from " + this.state + " to " + token);
		System.out.println(stack);
		
		if (this.state == null) {
			// first call to sets the start state
			this.state = Goto.get(token.getTokenLabel());
			this.stack.push(Goto.getLock());
			this.stack.push(this.state);
			this.setType(ActionType.SHIFT);
			return;
		} else if (token == null) {
			// end of token stream
			this.setType(ActionType.REPLACE);
			return;			
		} else {
			// check if this state can directly transition to token
			System.out.println("Transition from " + this.state + " to " + token + "\n");
			
			System.out.println(this.state.getTransitions());
			
			Goto nextState = this.state.makeTransition(token.getTokenLabel());
	
			if (nextState == null) {
				// cannot transition to token
				
				if (this.state.terminateTo() != null) {
					// can transition to end state
					
					/*if (!Goto.canStart(token.getTokenLabel()) && ) {
						// if the lookahead is not a start symbol
						this.lockState(this.state.terminateTo());
						this.lockState(this.state);
						
						if (this.state.canTransition(token.getTokenLabel())) {
							nextState = this.state.makeTransition(token.getTokenLabel());
							this.lockState(nextState);
							this.state = Goto.get(token.getTokenLabel());
							this.setType(ActionType.SHIFT);
							return;							
						} else {
							this.setType(ActionType.REJECT);
							return;
						}
					} */
					
					System.out.println(this.state + " can transition to end state " + this.state.terminateTo());
					this.setType(ActionType.REPLACE);
					return;
				} else if (this.state.canRepeat()) {
					this.stack.push(Goto.getLock());
					this.goals.push(this.state);
					nextState = Goto.get(token.getTokenLabel());
					this.state = nextState;
					this.stack.push(this.state);
					this.setType(ActionType.SHIFT);
					return;
				} else {
					System.out.println("can transition to non terminal");
					// else push non-terminal to stack and goals stack and lock
					//this.stack.push(this.state);
					nextState = this.state.nextState(true);
					this.stack.push(nextState);
					//System.out.println(this.stack.peek());
					
					System.out.println("Pushing " + nextState + " to goals");
					
					System.out.println(nextState.getToken().getChildren());
					
					this.goals.push(nextState);
					this.stack.push(Goto.getLock());
					
					// this state will now begin at the token symbol start state
					this.state = Goto.get(token.getTokenLabel());
					
					// push this state to the stack
					this.stack.push(this.state);
					this.setType(ActionType.SHIFT);
					return;					
				}
			} else {
				// this state can transition to token, so add it to stack
				System.out.println("Adding " + nextState + " to the stack");
				this.state = nextState;
				this.stack.push(nextState);
				this.setType(ActionType.SHIFT);
				return;
			}
		}
	} 
	
	public Node reduce() {
		// this state can transition to an end state
		List<Goto> storage = new ArrayList<Goto>();
		
		System.out.println(stack);
		
		// pop all children off the stack
		while (!this.stack.peek().toString().equals("$")) {
			Goto tmp = this.stack.pop(); 
			storage.add(tmp);
		}
		
		System.out.println("transitions " + this.state.getTransitions());
		
		// push new parent onto the stack
		Goto tmp = this.state.terminateTo();
		if (tmp == null)
			tmp = this.state.getEpsilonTransition();
		
		if (tmp.toString().equals("program") && this.token != null) {
			this.stack.push(this.state);
			this.goals.push(this.state);
			this.stack.push(Goto.getLock());
			this.state = Goto.get(token.getTokenLabel());
			this.stack.push(this.state);
			this.setType(ActionType.SHIFT);
			return this.state.getToken();
		}
		
		System.out.println("tmp is " + tmp);
		
		System.out.println("goals " + this.goals);
		
		if (!this.goals.isEmpty() && tmp.toString().equals(this.goals.peek().toString())) {
			System.out.println("goals " + this.goals);
			// remove the lock
			this.stack.pop();
			this.stack.pop();
			
			Goto original = this.goals.pop();
			//original.setToken(tmp.getToken());
			
			System.out.println("original transitions " + original);
			
			//original.setToken(new Node(new Token(null, original.toString(), null, null)));
			
			System.out.println("original children should be empty " + original.getToken().getChildren());
			
			this.state = original;
			this.setType(ActionType.REPEAT);
		} else {
			// set the current state to the parent
			this.state = Goto.get(tmp.toString());
			
			//System.out.println("this should be empty " + this.state.getToken().getChildren());
			
			if (this.state == null)
				this.state = tmp;
			
			this.setType(ActionType.REPEAT);	
		}
		
		// push the children back on the stack
		for (int i = 0; i < storage.size(); i++) {
			System.out.println("Adding child " + storage.get(i).getToken() + " to " + this.state + "\n");
			this.state.getToken().addChild(storage.get(i).getToken());
		}
		
		System.out.println("parent " + this.state);
		//System.out.println(Node.printTree(this.state.getToken(), " ", false));
		
		this.stack.push(this.state);
		
		if (stack.get(1).toString().equals("program")) {
			this.setType(ActionType.ACCEPT);			
		}
		
		return stack.get(1).getToken();
	}
}
