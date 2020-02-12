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
* [] Struct, enum
* ~~Preprocessor statements~~
* ~~Casting, type promotion~~
* ~~Type specs~~


### Scanner Design
The Scanner class starts by reading a file as a string, identifying the string literals in the file, and converting them to a UUID. This is to allow for a space delimiting later in our Scanner and to label these string literals. We do the same in the next phase by converting character constants to a UUID to prevent altering the string. After this, comments are removed from the string by first using a greedy search on areas with a double slash, then in a non-greedy method for multi-line comments. Whitespace is then inserted around any punctuation characters to assist with tokenization. Double white space is then removed to prevent empty tokens and the scanner searches for double punctuation operators and removes their separating white space. The string is then passed to the tokenize function to build the token list. This is done by passing the string to a StringTokenizer with delimiting based on spaces. Each token is then passed individually to the token constructors, with the location data marked as null, which will be set later. The UUIDs for the strings and characters are then restoredt to their original content. Then the file string is processed to determine the positional data for each token so that error messages can later be printed.

### Parser Design
* TODO
