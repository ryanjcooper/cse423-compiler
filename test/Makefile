CC=gcc
CFLAGS=-g -Wall
TOKIT=clang -O0 -Xclang -dump-tokens
list = base add subtract multiply divide while boolean string break assignment_arith assignment_bin function goto conditions types for binary switch struct enum pointer exhaustive

all: $(list)

# Compile all files in list, then set up <>.tokens
$(list):
	@$(CC) $(CFLAGS) $@.c -o $@ 2>/dev/null
	-$(TOKIT) $@.c > $@.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" $@.tokens
	@head -n -3 $@.tokens | tee $@.tokens > /dev/null

# deletes executeable and .tokens files
clean:
	rm $(list) *.tokens
