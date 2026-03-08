CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = Exercicio1 Exercicio2 Exercicio3 count_words count_wordsfiles pi pi-processes Exercicio7 Exercicio8

all: $(BINS)

Exercicio1: Exercicio1.o
Exercicio1.o: Exercicio1.c

Exercicio2: Exercicio2.o
Exercicio2.o: Exercicio2.c

Exercicio3: Exercicio3.o
Exercicio3.o: Exercicio3.c

count_words: count_words.o	
count_words.o: count_words.c

count_wordsfiles: count_wordsfiles.o
count_wordsfiles.o: count_wordsfiles.c

pi: pi.o
pi.o: pi.c

pi-processes: pi-processes.o
pi-processes.o: pi-processes.c

Exercicio7: Exercicio7.o
Exercicio7.o: Exercicio7.c

Exercicio8: Exercicio8.o
Exercicio8.o: Exercicio8.c

clean: 
	$(RM) $(BINS) *.o
	