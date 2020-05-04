### Build Instructions
1. Ensure `clang`, `mvn`, `javac`, and `java` are in the path.
   - Java 1.8 or newer
2. `cd` into `test/` and run `make`
3. `cd` back to the root directory of the project
4. run `mvn package`
5. A `.jar` file will be produced to `target/c-compiler-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
6. `mv target/c-compiler-0.0.1-SNAPSHOT-jar-with-dependencies.jar target/compiler.jar`
7. To run, use `java -jar target/compiler.jar`


or, you can just call `make` from the project root.

### Arguments
| Argument | Description | Currently Supported |
| -------- | ----------- | ------------------- |
| -help | Prints usage instructions | :white_check_mark: |
| -pt | Prints tokens to console | :white_check_mark: |
| -o | Destination for executable | :white_check_mark: |
| -ap | Prints the abstract syntax tree | :white_check_mark: |
| -stp | Prints the symbol table | :white_check_mark: |
| -ap | Print the abstract syntax tree | :white_check_mark: |
| -irn | read in ir from file, must have .ir extension | :white_check_mark: |
| -iro | write ir to file | :white_check_mark: |
| -o1 | Add optimizations (basic blocks only) | :white_check_mark: |
| -pir | print ir to console | :white_check_mark: |
| -pp |  print the parse tree | :white_check_mark: |
| -wp | write parse tree to file | :white_check_mark: |
