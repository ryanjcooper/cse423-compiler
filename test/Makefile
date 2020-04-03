CC=gcc
CFLAGS=-g -Wall
TOKIT=clang -O0 -Xclang -dump-tokens -fsyntax-only
AST=clang -O0 -w -Xclang -ast-dump -fsyntax-only
list = base base_semi base_semi2 add subtract multiply divide while boolean string break assignment_arith assignment_bin function goto conditions types for_semi2 for binary switch struct enum pointer exhaustive min badparenleft badparenredund badparenright badsemiconsec badsemimissing badsemimulti mod badbraceleft badbraceright badbraceredund badreturntype foldproptest

all: $(list)

# Compile all files in list, then set up <>.tokens
$(list):
	-@$(CC) $(CFLAGS) $@.c -o $@ 2>/dev/null
	-@$(TOKIT) $@.c > $@.tokens 2>&1
	-@head -n -1 $@.tokens | tee $@.tokens > /dev/null
	-@$(AST) $@.c > $@.astdump 2>&1

# deletes executeable and .tokens files
clean:
	rm $(list) *.tokens *.astdump
