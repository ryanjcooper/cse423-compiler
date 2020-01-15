CC=gcc
CFLAGS=-g -Wall

targets = hello_world fizzbuzz bubblesort bubblesort2 bubblesortline

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

clean:
	rm $(targets)
