#define _XOPEN_SOURCE 600
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <pthread.h>

#define DEFAULT_ITERATIONS 100
#define DEFAULT_N 1000000

void functionCall() {
    getpid();
    getppid();
}

void systemCall() {
    getpid();
    write(STDIN_FILENO, "", 0);
}

void forks() {
    pid_t retfork = fork();
    if (retfork == -1) {
        perror("Creating Process");
        exit(1);
    }
    if (retfork == 0) {
        exit(1);
    }
}


void executeProgram() {
    pid_t pid = fork();
    if (pid == -1) {
        perror("Creating Process");
        exit(1);
    }
    if (pid == 0) {
        execl("/bin/true", "true", NULL); // Execute a simple program
        perror("Executing Program");
        exit(1);
    }
    waitpid(pid, NULL, 0); // Wait for the child process to terminate
}

void *threadFunction(void *arg) {
    return NULL;
}

void createThread() {
    pthread_t thread;
    if (pthread_create(&thread, NULL, threadFunction, NULL) != 0) {
        perror("Creating Thread");
        exit(1);
    }
    pthread_join(thread, NULL); // Wait for the thread to terminate
}

void measure_time(void (*activity)(), const char *activity_name, long nTimes) {
    struct timespec t_start, t_end;

    clock_gettime(CLOCK_MONOTONIC, &t_start);

    for (unsigned long i = 0; i < nTimes; ++i) {
        activity();
    }

    clock_gettime(CLOCK_MONOTONIC, &t_end);

    double total_time = (t_end.tv_sec - t_start.tv_sec) + (t_end.tv_nsec - t_start.tv_nsec) / 1e9;
    double average_time = (total_time / nTimes) * 1e6;

    printf("%s:\n", activity_name);
    printf("  Total elapsed time = %9.6lf s\n", total_time);
    printf("  Average time per iteration = %7.2lf us\n", average_time);
}

int main (int argc, char *argv[]) {
    long nTimes = DEFAULT_ITERATIONS;

    printf("Measuring execution times for each activity (%ld iterations):\n", nTimes);

    measure_time(functionCall, "Function Call", nTimes);
    measure_time(systemCall, "System Call", nTimes);
    measure_time(forks, "Fork", nTimes);
    measure_time(executeProgram, "Execute Program", nTimes);
    measure_time(createThread, "Create Thread", nTimes);
}