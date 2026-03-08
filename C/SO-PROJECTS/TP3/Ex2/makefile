CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = accounts

all: $(BINS)

accounts: accounts.o
accounts.o: accounts.c

clean:
	$(RM) $(BINS) *.o
