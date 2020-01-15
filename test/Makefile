CC=gcc
CFLAGS=-g -Wall

targets = hello_world fizzbuzz bubblesort

all: $(targets)

hello_world: hello_world.c
	$(CC) $(CFLAGS) $@.c -o $@

fizzbuzz: fizzbuzz.c
	$(CC) $(CFLAGS) $@.c -o $@

bubblesort: bubblesort.c
	$(CC) $(CFLAGS) $@.c -o $@

clean:
	rm $(targets)
