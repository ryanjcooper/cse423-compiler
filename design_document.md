## Compiler Design Document

### Authors
* Ryan Cooper (@coopss)
* Brandon Fleming (@RodianRomeo)
* Steven Aque (@mattadik123)
* Terence Brewer (@tbrewerDNM)

## Intended Feature List
#### Required Features
* [] Identifiers, variables, functions
* [] Keywords
* [] Arithmetic expressions
* [] Assignment
* [] Boolean expressions
* [] Goto statements
* [] If / Else control flow
* [] Unary operators
* [] Return statements
* [] Break statements
* [] While loops
#### Optional Features
* [] Types other than integers
* [] ++, —, -=, +=, *=, /=
* [] For loops
* [] Binary operators
* [] Switch statements
#### Stretch-goal Features
* [] Pointers, ~~arrays~~, strings
* [] Struct, ~~enum~~
* ~~Preprocessor statements~~
* ~~Casting, type promotion~~
* ~~Type specs~~


### Scanner Design
* TODO

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
* 
