CC=gcc
CFLAGS=-g -Wall
TOKIT=clang -O0 -Xclang -dump-tokens

targets = base hello_world fizzbuzz bubblesort bubblesort2 bubblesortline minimal tokenize exhaustive

all: $(targets)

hello_world: hello_world.c
	$(CC) $(CFLAGS) $@.c -o $@

exhaustive: exhaustive.c
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
	@sed -i "s/^\(\S* '.*'\).*/\1/" hello_world.tokens
	@head -n -3 hello_world.tokens | tee hello_world.tokens > /dev/null
	-$(TOKIT) fizzbuzz.c > fizzbuzz.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" fizzbuzz.tokens
	-$(TOKIT) base.c > base.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" base.tokens
	@head -n -3 base.tokens | tee base.tokens > /dev/null
	-$(TOKIT) bubblesort.c > bubblesort.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" bubblesort.tokens
	-$(TOKIT) bubblesort2.c > bubblesort2.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" bubblesort2.tokens
	-$(TOKIT) bubblesortline.c > bubblesortline.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" bubblesortline.tokens
	-$(TOKIT) minimal.c > minimal.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" minimal.tokens
	@head -n -3 minimal.tokens | tee minimal.tokens > /dev/null
	-$(TOKIT) exhaustive.c > exhaustive.tokens 2>&1
	@sed -i "s/^\(\S* '.*'\).*/\1/" exhaustive.tokens
	@head -n -3 exhaustive.tokens | tee exhaustive.tokens > /dev/null

clean:
	rm $(targets) *.tokens
