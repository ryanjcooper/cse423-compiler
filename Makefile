all: compiler

compiler:
	(cd test/ && make)
	mvn package
	mv target/c-compiler-0.0.1-SNAPSHOT-jar-with-dependencies.jar target/compiler.jar