#include "thread-pool.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

// Function for the thread-pool to execute
void * printMSG (void *args) {
    char * str = args;
    for (int i = 0; i < 2; i++) {
        printf("%s\n" , str);
    }
    return NULL;
}

// Program main
int main() {
    threadpool_t threadpool;
    int nthmin = 4, nthmax = 8, cIncThreshold = 80, cDecThreshold = 20;
    int thInit = threadpool_init(&threadpool, 20, nthmin, nthmax, cIncThreshold, cDecThreshold);
    if(thInit != 0) {
        printf("Error initializing thread pool\n");
        return -1;
    }

    for(int i= 0; i < 100; i++) {
        threadpool_submit(&threadpool, printMSG, "OLA");
    }

    // sleep(2);
    threadpool_destroy(&threadpool);

    int err = threadpool_submit(&threadpool, printMSG, "OLA");
    if(err == -1) {
        printf("Success: Couldn't submit work item to thread pool after destroying threadpool\n");
    }

    return 0;
}