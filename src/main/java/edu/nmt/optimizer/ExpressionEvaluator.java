package edu.nmt.optimizer;
import java.util.ArrayList;


// Taken from GitHub JakeWharton
/***
 * @name	Expression Evaluator
 * @author	Jake Wharton
 * @date	2005-11-07
 */
public class ExpressionEvaluator
{
	private static enum OPERATION
	{
		Subtraction, Addition, Modulus, Multiplication, Division, Power, LeftParenthesis, RightParenthesis, Equals
	}
	private static int Precedence(String Operation)
	{
		switch (Operation.charAt(0))
		{
			case '(':
			case ')':
				return 4;
			case '^':
				return 3;
			case '/':
			case '*':
			case '%':
				return 2;
			case '+':
			case '-':
				return 1;
			default:
				//throw new InvalidArgumentException();
				return -1;
		}
	}
	private static OPERATION GetOperation(int i)
	{
		switch (i)
		{
			case 0:
				return OPERATION.Subtraction;
			case 1:
				return OPERATION.Addition;
			case 2:
				return OPERATION.Modulus;
			case 3:
				return OPERATION.Multiplication;
			case 4:
				return OPERATION.Division;
			case 5:
				return OPERATION.Power;
			case 6:
				return OPERATION.LeftParenthesis;
			case 7:
				return OPERATION.RightParenthesis;
			default:
				return OPERATION.Equals;
		}
	}
	
	private static String OPERATIONS = "-+%*/^()";
	
	private ExpressionNode Root;
	
	public ExpressionEvaluator(String Expression)
	{
		this.Root = this.ConstructInfixExpressionTree(Expression);
	}
	private ExpressionNode ConstructInfixExpressionTree(String InfixExpression)
	{
		//TODO: unary operators
		
		//un-suck infix with spacing and check parenthesis
		int j = 0;
		for (int i = 0; i < InfixExpression.length(); ++i)
		{
			if (ExpressionEvaluator.OPERATIONS.indexOf(InfixExpression.charAt(i)) != -1)
			{
				if (InfixExpression.charAt(i) == '(') ++j;
				if (InfixExpression.charAt(i) == ')') --j;
				if (j < 0)
				{
					return null;
					//throw new Exception... Mismatched parenthesis at character i
				}
				if ((i > 0) && (InfixExpression.charAt(i - 1) != ' '))
				{
					InfixExpression = InfixExpression.substring(0, i) + " " + InfixExpression.substring(i);
					++i; //adjust for insertion
				}
				if ((i < InfixExpression.length() - 1) && (InfixExpression.charAt(i + 1) != ' '))
				{
					InfixExpression = InfixExpression.substring(0, i + 1) + " " + InfixExpression.substring(i + 1);
					++i; //adjust for insertion
				}
			}
		}
		if (j > 0)
		{
			return null;
			//throw new Exception... Mismatched parenthesis
		}
		
		String[] Terms = InfixExpression.split(" ");
		ArrayList<ExpressionNode> Nodes = new ArrayList<ExpressionNode>();
		
		//create leaf node for each expression term
		for (String Term : Terms)
		{
			if (ExpressionEvaluator.OPERATIONS.indexOf(Term) == -1)
			{
				try
				{
					Nodes.add(new ExpressionNode(Double.valueOf(Term)));
				}
				catch (NumberFormatException e)
				{
					this.Root = null;
					return null;
				}
			}
			else
			{
				Nodes.add(new ExpressionNode(ExpressionEvaluator.GetOperation(ExpressionEvaluator.OPERATIONS.indexOf(Term))));
			}
		}
		
		//temporary stack space
		ArrayList<ExpressionNode> OperationStack = new ArrayList<ExpressionNode>();
		ArrayList<ExpressionNode> ValueStack = new ArrayList<ExpressionNode>();
		
		try
		{
			//iterate terms and construct tree
			for (int i = 0; i < Nodes.size(); ++i)
			{
				switch (Nodes.get(i).toString().charAt(0))
				{
					case '(':
						OperationStack.add(0, Nodes.get(i));
						break;
					case ')':
						//assign operations children until left parenthesis is encountered
						while (OperationStack.get(0).toString().equals("(") == false)
						{
							ValueStack.add(0, OperationStack.remove(0));
							ValueStack.get(0).SetChildren(ValueStack.remove(2), ValueStack.remove(1));
						}
						OperationStack.remove(0); //remove left parenthesis
						break;
					case '-':
					case '+':
					case '%':
					case '*':
					case '/':
					case '^':
						//assign operations children until operation stack is empty, a left parenthesis is encountered, or a lower precedence operation is encountered
						while ((OperationStack.size() > 0) && (OperationStack.get(0).toString().equals("(") == false) && (ExpressionEvaluator.Precedence(Nodes.get(i).toString()) <= ExpressionEvaluator.Precedence(OperationStack.get(0).toString())))
						{
							ValueStack.add(0, OperationStack.remove(0));
							ValueStack.get(0).SetChildren(ValueStack.remove(2), ValueStack.remove(1));
						}
						OperationStack.add(0, Nodes.get(i));
						break;
					default: //case number:
						ValueStack.add(0, Nodes.get(i));
				}
			}
			//assign the rest of the operations children
			while (OperationStack.size() > 0)
			{
				ValueStack.add(0, OperationStack.remove(0));
				ValueStack.get(0).SetChildren(ValueStack.remove(2), ValueStack.remove(1));
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			ValueStack.clear();
			this.Root = null;
		}
		catch (Exception e)
		{
			ValueStack.clear();
			this.Root = null;
		}
		
		//last node, root node
		return ValueStack.get(0);
	}

	public String GetInfixExpression()
	{
		return this.Root.GetInfixExpression();
	}
	public Integer GetValue()
	{
		try {
			return this.Root.GetValue().intValue();
		} catch (NullPointerException e) { 
			return null;
		}
	}
	
	private class ExpressionNode
	{
		private double Value;
		private OPERATION Operation;
		private ExpressionNode Left;
		private ExpressionNode Right;
		
		public ExpressionNode(double Value)
		{
			this.Value = Value;
			this.Operation = OPERATION.Equals;
			this.Left = null;
			this.Right = null;
		}
		public ExpressionNode(OPERATION Operation)
		{
			this(Operation, null, null);
		}
		public ExpressionNode(OPERATION Operation, ExpressionNode Left, ExpressionNode Right)
		{
			this.Value = 0;
			if (Operation == OPERATION.Equals)
			{
				System.exit(1);
				//throw new Exception("You cannot explicitly define an Equals node. To define a value-only node, passing in a double");
			}
			this.Operation = Operation;
			this.Left = Left;
			this.Right = Right;
		}
		
		public void SetChildren(ExpressionNode Left, ExpressionNode Right)
		{
			this.Left = Left;
			this.Right = Right;
		}
		public Double GetValue()
		{
			//determine and perform operation on children
			switch (this.Operation)
			{
				case Equals:
					break;
				case Power:
					this.Value = Math.pow(this.Left.GetValue(), this.Right.GetValue());
					break;
				case Division:
					this.Value = this.Left.GetValue() / this.Right.GetValue();
					break;
				case Multiplication:
					this.Value = this.Left.GetValue() * this.Right.GetValue();
					break;
				case Modulus:
					this.Value = this.Left.GetValue().intValue() % this.Right.GetValue().intValue();
					break;
				case Addition:
					this.Value = this.Left.GetValue() + this.Right.GetValue();
					break;
				case Subtraction:
					this.Value = this.Left.GetValue() - this.Right.GetValue();
					break;
				default:
					return null;
					//error
			}
			return this.Value;
		}
		private String GetOperation()
		{
			return String.valueOf(ExpressionEvaluator.OPERATIONS.charAt(this.Operation.ordinal()));
		}
		public String GetInfixExpression()
		{
			if (this.Operation == OPERATION.Equals)
			{
				return String.valueOf(this.Value);
			}
			else
			{
				//fully parenthesized
				return "(" + this.Left.GetInfixExpression()  + " " + this.GetOperation() + " " + this.Right.GetInfixExpression() + ")";
			}
		}
		public String toString()
		{
			return (this.Operation != OPERATION.Equals) ? this.GetOperation() : String.valueOf(this.Value);
		}
	}
}