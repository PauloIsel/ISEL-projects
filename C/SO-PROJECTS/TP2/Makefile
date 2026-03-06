CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = executionTimes pi-threads

all: $(BINS)

executionTimes: executionTimes.o
executionTimes.o: executionTimes.c

pi-threads: pi-threads.o
pi-threads.o: pi-threads.c

clean: 
	$(RM) $(BINS) *.o    