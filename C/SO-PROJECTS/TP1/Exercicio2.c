//Crie um programa que execute simultaneamente (em paralelo) os comandos /bin/date e /bin/ping -c 4
//www.google.com. O programa deve esperar pelo fim da execução dos dois programas indicando o estado de
//terminação de cada um deles (chamadas de sistema a usar: família de funções fork, exec, waitpid). 

#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

int main ()
{
    printf("Parent process starts (pid = %d; ppid=%d)...\n",
                getpid(), getppid());

// Criar processo filho para a realização do exec de /bin/ping -c 4 www.google.com
    pid_t ping_pid = fork();
    if (ping_pid == -1) {
        perror("Creating Process");
        exit(EXIT_FAILURE);
    }

    if (ping_pid == 0) {
// novo processo
    printf("Child Process with pid = %d; ppid = %d\n", getpid(), getppid());
    execl("/bin/ping", "ping", "-c", "4", "www.google.com", NULL);
    perror("calling exec");
    exit(EXIT_FAILURE);
    }

// Criar processo filho para a realização do exec de /bin/date
    pid_t date_pid = fork();
    if (date_pid == -1) {
        perror("Creating Process");
        exit(EXIT_FAILURE);
    }
    
    if (date_pid == 0) {
// novo processo
        printf("Child Process (date) with pid = %d; ppid = %d\n", getpid(), getppid());
        execl("/bin/date", "date", NULL);
        perror("calling exec");
        exit(EXIT_FAILURE);
    }

// processo pai
    printf("My pid = %d and create a process %d\n", getpid(), ping_pid);
    printf("My pid = %d and create a process %d\n", getpid(), date_pid);

// Esperar que os dois filhos terminem
    for (int i = 0; i < 2; i++) {
        int status;
        pid_t pid = waitpid(-1, &status, 0);
        if (pid == -1) {
            perror("calling waitpid");
            exit(EXIT_FAILURE);
        }
        printf("Process %d", pid);
        if (WIFEXITED(status)) {
            printf(" has terminated normally with exit value %d\n", WEXITSTATUS(status));
        } else if (WIFSIGNALED(status)) {
            printf(" has terminated by signal %d\n", WTERMSIG(status));
        }
    }
    return 0;
}