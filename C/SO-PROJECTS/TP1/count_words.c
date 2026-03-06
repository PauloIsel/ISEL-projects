/*Realize um programa (count_words) que recebe através da linha de argumentos o nome de um ficheiro e imprima
no standard de output o número de palavras desse ficheiro. A determinação do número de palavras é realizada
executando o programa externo wc com a opção -w (chamadas de sistema a usar: fork, pipe, dup/dup2, exec,
waitpid). */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

#define PIPE_RD 0
#define PIPE_WR 1

#define BUFFER_SIZE 16

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Uso: %s <nome_do_ficheiro>\n", argv[0]);
        return 1;
    }

    int pipefd[2];
    if (pipe(pipefd) == -1) {
        perror("Erro ao criar pipe");
        return 1;
    }

    pid_t pid = fork();
    if (pid == -1) {
        perror("Erro ao criar processo filho");
        return 1;
    }

    if (pid == 0) {
        // Processo filho
        // Fecha o lado de leitura do pipe
        if ( close(pipefd[PIPE_RD]) < 0) {
            perror("Closing pipe read descriptor");
            exit(EXIT_FAILURE);           
        }  

        dup2(pipefd[1], STDOUT_FILENO); // Redireciona stdout para o pipe

        // Fecha o lado de escrita do pipe (já duplicado)
        if ( close(pipefd[PIPE_WR]) < 0) {
            perror("Closing pipe write descriptor");
            exit(EXIT_FAILURE);           
        } 

        // Executa o comando wc -w
        execlp("wc", "wc", "-w", argv[1], NULL);
        perror("Erro ao executar wc");
        exit(1);

    } 
    
    // Processo pai    
    // Fecha o lado de escrita do pipe
    if ( close(pipefd[PIPE_WR]) < 0) {
        perror("Closing pipe write descriptor");
        exit(EXIT_FAILURE);           
    }  

    // Lê o resultado do pipe
    char buffer[BUFFER_SIZE];
    ssize_t bytesRead = read(pipefd[0], buffer, sizeof(buffer) - 1);

    if (bytesRead > 0) {
        buffer[bytesRead] = '\0'; // Garante que a string está terminada
        printf("Número de palavras: %s", buffer);
    } else {
        perror("Erro ao ler do pipe");
    }

    // Fecha o lado de leitura do pipe
    if ( close(pipefd[PIPE_RD]) < 0) {
        perror("Closing pipe read descriptor");
        exit(EXIT_FAILURE);           
    }  
    
    // Sincronizar com a terminação do processo filho obtendo:
    //     1 - a forma como terminou o processo filho
    //     2 - valor de terminação
    int status;
    pid_t w_pid = waitpid(pid, &status, 0);
    if (w_pid == -1) {
        perror("Calling waitpid");
        exit(EXIT_FAILURE);
    }

    printf("Process pid = %d", w_pid);
    if ( WIFEXITED(status) ) {
        printf(" has terminated normally with exit value %d", WEXITSTATUS(status));
    }
    else if ( WIFSIGNALED(status) ) {
        printf(" has terminated by signal %d", WTERMSIG(status));
    }
    printf("\n");

    printf("process %d terminating\n", getpid());
    return 0;
}
