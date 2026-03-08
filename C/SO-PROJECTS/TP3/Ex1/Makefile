CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = philosopher

all: $(BINS)

philosopher: philosopher.o
philosopher.o: philosopher.c

clean:
	$(RM) $(BINS) *.o