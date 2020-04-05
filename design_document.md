## Compiler Design Document

### Authors
* Ryan Cooper (@coopss)
* Brandon Fleming (@RodianRomeo)
* Steven Aque (@mattadik123)
* Terence Brewer (@tbrewerDNM)

## Intended Feature List

### Required Features
| Feature     | Scanner            | Parser              | AST                 | IR                  | Backend             |
| :--------:  | :----------------: | :-----------------: | :-----------------: | :-----------------: | :-----------------: |
| Identifiers | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Variables   | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Functions   | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 | :x:                 |
| Keywords    | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Arithmetic  | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Assignment  | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Boolean     | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Goto        | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| If / Else   | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Unary       | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Return      | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Break       | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| While       | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
#### Optional Features
| Feature                 | Scanner            | Parser              | AST                 | IR                  | Backend             |
| :---------------------: | :----------------: | :-----------------: | :-----------------: | :-----------------: | :-----------------: |
| More Types              | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| ++, - , -=. +=, \*=, /= | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| For Loops               | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Binary Operators        | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 |
| Switch Statements       | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 | :x:                 |
#### Stretch-goal Features
| Feature                 | Scanner            | Parser              | AST                 | IR                  | Backend             |
| :---------------------: | :----------------: | :-----------------: | :-----------------: | :-----------------: | :-----------------: |
| Pointers                | :heavy_check_mark: | :heavy_check_mark:  | :heavy_check_mark:  | :x:                 | :x:                 |
| Arrays                  | :heavy_check_mark: | :heavy_check_mark:  | :x:                 | :x:                 | :x:                 |
| Strings                 | :heavy_check_mark: | :heavy_check_mark:  | :x:                 | :x:                 | :x:                 |
| Structs                 | :heavy_check_mark: | :heavy_check_mark:  | :x:                 | :x:                 | :x:                 |
| Enum                    | :x:                | :x:                 | :x:                 | :x:                 | :x:                 |
| Preprocessor Statements | :x:                | :x:                 | :x:                 | :x:                 | :x:                 |
| Type Casting            | :x:                | :x:                 | :x:                 | :x:                 | :x:                 |
| Type Promotion          | :x:                | :x:                 | :x:                 | :x:                 | :x:                 |
| Type Specs              | :x:                | :x:                 | :x:                 | :x:                 | :x:                 |

### Scanner Design
The Scanner class starts by reading a file as a string, identifying the string literals in the file, and converting them to a UUID. This is to allow for a space delimiting later in our Scanner and to label these string literals. We do the same in the next phase by converting character constants to a UUID to prevent altering the string. After this, comments are removed from the string by first using a greedy search on areas with a double slash, then in a non-greedy method for multi-line comments. Whitespace is then inserted around any punctuation characters to assist with tokenization. Double white space is then removed to prevent empty tokens and the scanner searches for double punctuation operators and removes their separating white space. The string is then passed to the tokenize function to build the token list. This is done by passing the string to a StringTokenizer with delimiting based on spaces. Each token is then passed individually to the token constructors, with the location data marked as null, which will be set later. The UUIDs for the strings and characters are then restored to their original content. Then the file string is processed to determine the positional data for each token so that error messages can later be printed.

We decided to make our own algorithm to scan so that we did not have to learn a new tool, and could reduce the number of dependencies of the project.

### Parser Design
The parser went through several iterations of design, but the final implementation is based on LR(1). This is not an implementation of LR(1), but it utilizes concepts such as lookahead, action, goto, and automata.

The parser takes in a stream of tokens and is given two objectives: check if this is a valid C file for our subset of C and if so create a parse tree. Another input used by the parser is the grammar, which is a slightly tweaked version of the C- grammar. Our Grammar class reads in the grammar file and converts each line to a useful data structure called a Rule, which contains left-hand-side and right-hand-side symbols.

The set of rules is used to create the Goto automata, named based on the Goto table from LR(1). This is a graph of the right-hand-side of each rule and the left-hand-side is the accept state.

The parser loops through a stream of tokens and bases what action to do based on the Action class, also named from the Action table from LR(1). The following five actions can occur:

* (1) SHIFT
This function checks if the current state of the automata can transition to the next state via the next token in the stream. This token acts as the lookahead to determine how the algorithm should proceed. The simplest case is the current state is able to directly transition to the next state using the next token symbol. The current state is then pushed onto the stack and the automata moves to the next state as its new current state. The other primary case, if the token stream is valid, is if the current state cannot directly transition to the next symbol, but is able to if either it or the next symbol need to be reduced. This is accomplished by pushing the non-terminal it can transition to onto the stack and the goals stack, which keeps the reduce function from reducing too much. The stack is then locked, essentially restarting the path with the next symbol as the new start state until it reaches the destination laid out in the goals stack.

* (2) REDUCE
This function is called when the last n elements on the stack need to be reduced to the state before the lock. This function is highly dependent on SHIFT working correctly as it assumes everything is correct during reduction. This function also handles the creation of the parse tree. The last n elements are popped off the stack, made children of the element before the lock, and that element becomes the new current state of the automata.

* (3) REPEAT
This calls the same function as SHIFT, however it does not proceed in the token stream but remains in-place. This is for cases where a state needs to be reduced before it can transition to the next state.

* (4) ACCEPT
This accepts the token stream as valid and produces a parse tree as a side effect.

* (5) REJECT
This rejects the token stream as invalid and produces an error message.

The parser supports everything labeled under the Features List that is not crossed out at a basic level. The file ParserTest.java contains every test .c file the parser is capable of handling. We are sure there are some oversights and this parser may not correctly parse every .c file even if it only contains only the features we have listed as working.

The following limitations include:
* arrays can be declared, but not specified statically i.e char string[] = "" works but not char string[] = {'a', 'b'}
* arrays can be referenced with an int i.e. a[1] but not without the int, so "a[] = b" will not work
* pointers can only be in the form \*variable
* for loops have to be in the "form for (i = 0; i < 1; i++) {  }" not precise, just similar format
* if statements, while loops, for loops all need braces around their interior


### Optimizer

### Intermediate Representation (IR)
Our IR is represented as a Map of a List of Instructions, in which keys are function names.
\
Static Single Assignment (SSA) is used.

Each Instruction contains the following information:
\
(1) Line Number \
(2) Instruction ID \
(3) Operation \
(4) Type \
(5) Operand 1 \
(6) Operand 1 Name \
(7) Operand 2 

All 7 parameters are needed, making the I/O IR different from the human readable version.
<br/><br/>
<b>(base.c)</b>
<pre>
int main() 
{ 
  return 1;
} 
</pre>
<b>(IR) </b>
<pre>
1: _1 = 1 type: int 
2: return _1 type: int 
</pre>
<b>(IR to file)</b> 
<pre>
#main 
1 _1 numeric_constant int null 1 null 
2 null return int 1 null null 
</pre>

### Optimizations

#### Optimization
| Item        | Status           |
| ------------- |-------------|
| Constant Propagation     | :x: |
| Constant Folding | :x: |
