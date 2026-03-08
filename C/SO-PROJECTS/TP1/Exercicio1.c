/*Realize um programa para criar um número (NUMBER_OF_CHILDS) de processos filhos que se executam em
concorrência esperando, de seguida, que esses processos terminem.
Os processos filhos devem imprimir 5 vezes o seu pid e o pid do seu pai esperando 1 segundo entre cada impressão.
O processo pai deve escrever o pid de cada processo filho à medida que terminem (ver chamada de sistema wait).
Notas:
● O processo pai cria todos os processos filhos antes de esperar pela terminação deles
● utilize a função sleep para suspender a execução do processo durante período em segundos
● Use o comando man para estudar o comportamento das chamadas: fork, wait, waitpid, getpid,
getppid, sleep
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define NUMBER_OF_CHILDREN     5

int main ()
{
    printf("Parent process starts (pid = %d; ppid=%d)...\n",
                getpid(), getppid());

// Criar NUMBER_OF_CHILDREN processos filhos
    for (int i = 0; i < NUMBER_OF_CHILDREN; i++) {
        pid_t retfork = fork();

        if (retfork == -1) {
            perror("Creating Process");
            exit(EXIT_FAILURE);
        }

        if ( retfork == 0) {
        // novo processo
        for (int i = 0; i < 5; i++) {
            printf("Child Process with pid = %d; ppid = %d\n", getpid(), getppid());
            //Suspende durante 1 segundo
            sleep(1);
        }
        return 13;
        }

       // Processo ? -> processo pai
    printf("My pid = %d e create a process %d\n", getpid(), retfork);
    }

    // Esperar que NUMBERR_OF_CHILDREN filhos terminem
    for (int i=0; i< NUMBER_OF_CHILDREN; i++) {
        int status;
        pid_t pid = wait(&status);
        if (pid == -1) {
            perror("calling wait");
            exit(EXIT_FAILURE);
        }
        printf("Process %d", pid);
        if (WIFEXITED(status)) {
            printf(" has terminated normally with exit value %d\n", WEXITSTATUS(status));
        }
        else if (WIFSIGNALED(status)) {
            printf(" has terminated by signal %d\n", WTERMSIG(status));
        }

    printf("process %d terminating with child %d\n", getpid(), pid);
    }

    return 0;
}
