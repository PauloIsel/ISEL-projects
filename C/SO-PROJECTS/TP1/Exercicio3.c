/* Realize um programa constituído por um processo principal (pai) e por um processo auxiliar (filho). O processo
principal irá ler, do standard de input, uma sequência de números inteiros e para cada valor lido envia-o, através de
um pipe, para o processo filho. O processo filho imprime, no standard de output, o quadrado de cada valor
recebido. A leitura do pai é realizada através da função scanf até esta retornar o valor EOF (ctrl + D). O filho termina
quando o pipe for fechado pelo processo pai (chamadas de sistema a usar: fork, pipe, waitpid). 
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

#define PIPE_RD 0
#define PIPE_WR 1

int main ()
{
    printf("Parent process starts (pid = %d; ppid=%d)...\n", getpid(), getppid());

    int pipefd[2];

    if (pipe(pipefd) == -1) {
        perror("Calling pipe");
        exit(EXIT_FAILURE);   
    }
            
    pid_t retfork = fork();
    if (retfork == -1) {
        perror("Calling fork");
        exit(EXIT_FAILURE);
    }

    if (retfork == 0) {
        // Processo filho
        printf("Child Process with pid = %d; ppid = %d\n", getpid(), getppid());

        if (close(pipefd[PIPE_WR]) < 0) {
            perror("Closing pipe write descriptor");
            exit(EXIT_FAILURE);           
        }

        int num;
        while (read(pipefd[PIPE_RD], &num, sizeof(int)) > 0) {
            printf("Received: %d, Squared: %d\n", num, num * num);
        }

        if (close(pipefd[PIPE_RD]) < 0) {
            perror("Closing pipe read descriptor");
            exit(EXIT_FAILURE);            
        }

        printf("Child Process terminating with pid = %d; ppid = %d\n", getpid(), getppid());
        exit(EXIT_SUCCESS);
    }

    // Processo pai
    if (close(pipefd[PIPE_RD]) < 0) {
        perror("Closing pipe read descriptor");
        exit(EXIT_FAILURE);            
    }

    int num;
    while (scanf("%d", &num) != EOF) {
        if (write(pipefd[PIPE_WR], &num, sizeof(int)) == -1) {
            perror("Writing to pipe");
            exit(EXIT_FAILURE);   
        }
    }

    if (close(pipefd[PIPE_WR]) < 0) {
        perror("Closing pipe write descriptor");
        exit(EXIT_FAILURE);           
    }

    // Sincronizar com a terminação do processo filho
    int status;
    pid_t pid = waitpid(retfork, &status, 0);
    if (pid == -1) {
        perror("Calling waitpid");
        exit(EXIT_FAILURE);
    }

    printf("Process pid = %d", pid);
    if (WIFEXITED(status)) {
        printf(" has terminated normally with exit value %d", WEXITSTATUS(status));
    } else if (WIFSIGNALED(status)) {
        printf(" has terminated by signal %d", WTERMSIG(status));
    }
    printf("\n");

    printf("process %d terminating\n", getpid());
    return 0;
}