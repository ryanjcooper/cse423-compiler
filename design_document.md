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
* [] Pointers, arrays, strings
* ~~Struct, enum~~
* ~~Preprocessor statements~~
* ~~Casting, type promotion~~
* ~~Type specs~~


### Scanner Design
* TODO

### Parser Design
The parser went through several iterations of design, but the final implementation is based on the LR(1). This is not an implementation of LR(1), but it utilizes concepts such as lookahead, action, goto, and automata.

The parser takes in a stream of tokens and is given two objectives: check if this is a valid C file for our subset of C and if so create a parse tree. Another input used by the parser is the grammar, which is a slightly tweaked version of the C- grammar. Our Grammar class reads in the grammar file and converts each line to a useful data structure called a Rule, which contains left-hand-side and right-hand-side symbols.
