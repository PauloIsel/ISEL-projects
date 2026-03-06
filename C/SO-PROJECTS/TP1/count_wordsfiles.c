/*Altere o programa anterior para que o número de palavras seja obtido pelo processo pai que irá realizar a
apresentação de resultados para o standard de output. Permita que o seu programa receba vários ficheiros pela
linha de argumentos sendo apresentado o total das palavras de todos os ficheiros. Pode testar o seu programa
executando-o da seguinte forma: $> ./count_words * */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

#define PIPE_RD 0
#define PIPE_WR 1
#define MAX_BUF 16

int main(int argc, char *argv[]) {
    
    if (argc < 2) {
        fprintf(stderr, "Uso: %s <ficheiro1> <ficheiro2> ... <ficheiroN>\n", argv[0]);
        return 1;
    }

    int total_palavras = 0;

    for (int i = 1; i < argc; i++) {

        int pipefd[2];
        if (pipe(pipefd) == -1) {
            perror("Erro ao criar pipe");
            exit(EXIT_FAILURE);
        }

        pid_t pid = fork();
        if (pid == -1) {
            perror("Erro ao criar processo filho");
            exit(EXIT_FAILURE);
        }

        if (pid == 0) {
            // Processo filho
            // Fecha o lado de leitura do pipe
            if ( close(pipefd[PIPE_RD]) < 0) {
                perror("Closing pipe read descriptor");
                exit(EXIT_FAILURE);           
            }   

            dup2(pipefd[PIPE_WR], STDOUT_FILENO); // Redireciona stdout para o pipe

            // Fecha o lado de escrita do pipe (já duplicado)
            if ( close(pipefd[PIPE_WR]) < 0) {
            perror("Closing pipe write descriptor");
            exit(EXIT_FAILURE);           
            } 

            // Executa o comando wc -w para o arquivo atual
            execlp("wc", "wc", "-w", argv[i], NULL);
            perror("Erro ao executar wc");
            exit(EXIT_FAILURE);

        } else {

            // Processo pai
            // Fecha o lado de escrita do pipe
            if ( close(pipefd[PIPE_WR]) < 0) {
                perror("Closing pipe write descriptor");
                exit(EXIT_FAILURE);           
            }  

            // Lê o resultado do pipe
            char buf[MAX_BUF + 1];
            ssize_t nBytesRd = read(pipefd[PIPE_RD], buf, MAX_BUF);
            if (nBytesRd > 0) {
                buf[nBytesRd] = '\0'; // Garante que a string está terminada
                total_palavras += atoi(buf); // Converte o número de palavras para inteiro e soma
            } else if (nBytesRd < 0) {
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
        }
    }

    printf("Total de palavras: %d\n", total_palavras);
    return 0;
}