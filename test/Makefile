CC=gcc
CFLAGS=-g -Wall
TOKIT=clang -O0 -Xclang -dump-tokens -fsyntax-only
AST=clang -O0 -w -Xclang -ast-dump -fsyntax-only
list = base add subtract multiply divide while boolean string break assignment_arith assignment_bin function goto conditions types for binary switch struct enum pointer exhaustive

all: $(list)

# Compile all files in list, then set up <>.tokens
$(list):
	@$(CC) $(CFLAGS) $@.c -o $@ 2>/dev/null
	-@$(TOKIT) $@.c > $@.tokens 2>&1
	-@$(AST) $@.c > $@.astdump 2>&1

# deletes executeable and .tokens files
clean:
	rm $(list) *.tokens
