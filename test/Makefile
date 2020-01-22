CC=gcc
CFLAGS=-g -Wall
TOKFLG=-E

targets = hello_world fizzbuzz bubblesort bubblesort2 bubblesortline minimal tokenize

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

tokenize: hello_world.c fizzbuzz.c bubblesort.c bubblesort2.c bubblesortline.c
	$(CC) $(TOKFLG) hello_world.c -o hello_world.tokens
	$(CC) $(TOKFLG) fizzbuzz.c -o fizzbuzz.tokens
	$(CC) $(TOKFLG) bubblesort.c -o bubblesort.tokens
	$(CC) $(TOKFLG) bubblesort2.c -o bubblesort2.tokens
	$(CC) $(TOKFLG) bubblesortline.c -o bubblesortline.tokens
	$(CC) $(TOKFLG) minimal.c -o minimal.tokens

clean:
	rm $(targets) *.tokens
