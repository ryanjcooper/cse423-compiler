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