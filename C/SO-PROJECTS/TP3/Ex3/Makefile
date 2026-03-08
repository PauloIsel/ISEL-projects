CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = vectorMinMaxClassification vectorMinMaxClassification2

all: $(BINS)

vectorMinMaxClassification: vectorMinMaxClassification.o
vectorMinMaxClassification.o: vectorMinMaxClassification.c

vectorMinMaxClassification2: vectorMinMaxClassification2.o
vectorMinMaxClassification2.o: vectorMinMaxClassification2.c

clean:
	$(RM) $(BINS) *.o