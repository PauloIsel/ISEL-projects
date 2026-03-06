#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <pthread.h>

typedef struct {
    long start;
    long end;
    double piPartial;
} ThreadArgs;

void handleErrorInputs(int value, char *msg) {
    if(value <= 0) {
        fprintf(stderr, "ERROR: %s\n", msg);
        exit(EXIT_FAILURE);
    }
}

void handleError(char *msg) {
    fprintf(stderr, "ERROR: %s\n", msg);
    exit(EXIT_FAILURE);
}

void* piCalc(void *args) {
    ThreadArgs *threadArgs = (ThreadArgs *)args;
    double pi = 0.0;

    for(long k = threadArgs->start; k <= threadArgs->end; k++) {
        double term = ((k % 2 == 0) ? -1.0 : 1.0) / (2 * k - 1);
        pi += term;
    }

    threadArgs->piPartial = pi;
    return NULL;
}


int main(int argc,char* argv[]) {
    if(argc != 3) handleError("Não tem argumentos suficientes");

    long num = atoi(argv[1]);
    handleErrorInputs(num, "Número inválido");
    
    int threads = atoi(argv[2]);
    handleErrorInputs(threads, "Número de threads inválido");

    if (threads > num) handleError("Número de threads maior que o número de termos");
    
    double pi = 0.0;
    pthread_t th[threads]; 
    ThreadArgs args[threads];
    long numsPerthread = num/threads;
    

    for (int i = 0; i < threads; i++) {
        args[i].start = i * numsPerthread + 1;
        args[i].end = (i == threads - 1) ? num : (i + 1) * numsPerthread;
        printf("Thread %d: start = %ld, end = %ld\n", i, args[i].start, args[i].end);

        int err = pthread_create(&th[i], NULL, piCalc, &args[i]);
        if (err != 0) handleError("Error creating thread");
    }

    for (int i = 0; i < threads; i++) {
        pthread_join(th[i], NULL);
    }
    
    for (int i = 0; i < threads; i++) {
        pi += args[i].piPartial;
    }

    pi *= 4;
    printf("Valor do pi: %.15lf\n", pi);
    return 0;
}