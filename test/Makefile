CC=gcc
CFLAGS=-g -Wall
TOKIT=clang -Xclang -dump-tokens

targets = base hello_world fizzbuzz bubblesort bubblesort2 bubblesortline minimal tokenize

all: $(targets)

hello_world: hello_world.c
	$(CC) $(CFLAGS) $@.c -o $@

fizzbuzz: fizzbuzz.c
	$(CC) $(CFLAGS) $@.c -o $@

bubblesort: bubblesort.c
	$(CC) $(CFLAGS) $@.c -o $@

bubblesort2: bubblesort2.c
	$(CC) $(CFLAGS) $@.c -o $@

bubblesortline: bubblesortline.c
	$(CC) $(CFLAGS) $@.c -o $@

minimal: minimal.c
	$(CC) $(CFLAGS) $@.c -o $@
base: base.c
	$(CC) $(CFLAGS) $@.c -o $@

tokenize: hello_world.c fizzbuzz.c bubblesort.c bubblesort2.c bubblesortline.c
	-$(TOKIT) hello_world.c > hello_world.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" hello_world.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" hello_world.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" hello_world.tokens
	-$(TOKIT) fizzbuzz.c > fizzbuzz.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" fizzbuzz.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" fizzbuzz.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" fizzbuzz.tokens
	-$(TOKIT) base.c > base.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" base.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" base.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" base.tokens
	-$(TOKIT) bubblesort.c > bubblesort.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" bubblesort.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" bubblesort.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" bubblesort.tokens
	-$(TOKIT) bubblesort2.c > bubblesort2.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" bubblesort2.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" bubblesort2.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" bubblesort2.tokens
	-$(TOKIT) bubblesortline.c > bubblesortline.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" bubblesortline.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" bubblesortline.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" bubblesortline.tokens
	-$(TOKIT) minimal.c > minimal.tokens 2>&1
	@sed -i "s/^\(\S* '\S*'\).*/\1/" minimal.tokens
	@sed -i "/clang: error: linker command failed with exit code 1 (use -v to see invocation)/d" minimal.tokens
	@sed -i "/^\/usr\/bin\/ld: cannot find \/tmp.*/d" minimal.tokens

clean:
	rm $(targets) *.tokens
