/*Pretende-se um programa para determinar o valor aproximado de π, utilizando a série Leibniz apresentada abaixo.
O cálculo é realizado gerando um determinado número de termos da série e que neste exercício vamos considerara
um mínimo de 10 × 10⁹ termos. 

π = 4*nଽ(−1)^𝑘+1/2𝑘 − 1, k=1

Numa primeira fase realize um programa sequencial que recebe o número de termos a gerar através da linha de
comando. Pode avaliar, através do comando time, o tempo de execução necessário ao cálculo do valor de π
segundo este método:
$> time ./pi 10000000000 

A estratégia a seguir é a seguinte:
● A nova versão do programa (pi-processes) recebe mais um argumento, da linha de comando, que
indica o número de processos auxiliares a utilizar, por exemplo, para serem utilizados dois processos o
programa deve ser executado da seguinte forma:
$> ./pi-processes 10000000000 2
● O processo principal divide o cálculo do somatório pelos processos filhos, i.e., se utilizarmos dois
processos na determinação do π com 1000 termos, teríamos que o primeiro processo realiza o somatório
no intervalo entre 1 e 500 e o segundo processo entre 501 e 1000. Neste caso, deve tirar partido do facto
da chamada de sistema fork() criar um processo através de clonagem do processo pai incluindo todo o
seu espaço de endereçamento.
● Os processos filhos, no fim do processamento, enviam o resultado ao processo principal através do
mecanismo pipe para que este calcule o resultado final.
Avalie os tempos de execução, entre a versão sequencial e a versão baseada em múltiplos processos, variando o
limite superior do somatório e o número de processos. 
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define PIPE_RD 0
#define PIPE_WR 1

//Com o acréscimo do número de processos auxiliares, o tempo de execução diminui consideravelmente
//em relação à versão sequencial. 
//Para 10000000000 termos
//Versão sequencial: 4.611s
//Versão nova (com 4 processos auxiliares): 1.633s

int main(int argc,char* argv[]) {
    if(argc != 3) {
        fprintf(stderr, "Não tem argumentos suficientes\n");
        return -1;
    }

    printf("Parent process starts (pid = %d; ppid=%d)...\n",getpid(), getppid());

    double pi = 0.0;
    int num = atoi(argv[1]);
    if(num <= 0) {
        fprintf(stderr, "Número inválido\n");
        return -2;
    } 


    int forks = atoi(argv[2]);
    if(forks <= 0) {
        fprintf(stderr, "Número de processos auxiliares inválido\n");
        return -3;
    }

    int numsPerFork = num/forks;
    int pipefd[forks][2];
    
    //Child processes
    for (int i = 0; i < forks; i++) {
        //check if pipe was created
        if(pipe(pipefd[i]) == -1) {
            perror("pipe");
            exit(1);
        }

        pid_t retfork = fork();

        if(retfork == 0) {
            printf("Child Process with pid = %d; ppid = %d\n", getpid(), getppid());

            // Fecha o lado de leitura do pipe
            if ( close(pipefd[i][PIPE_RD]) < 0) {
                perror("Closing pipe read descriptor");
                exit(EXIT_FAILURE);           
            }  

            double piPartial = 0.0;
            int start = i * numsPerFork + 1;
            int end = (i == forks - 1) ? num : (i + 1) * numsPerFork;
            printf("start = %d, end = %d\n", start, end);

            for(int k = start; k <= end; k++) {
                double term = ((k % 2 == 0) ? -1.0 : 1.0) / (2 * k - 1);
                piPartial += term;
            }

            write(pipefd[i][PIPE_WR], &piPartial, sizeof(piPartial+1));

            // Fecha o lado de escrita do pipe
            if ( close(pipefd[i][PIPE_WR]) < 0) {
                perror("Closing pipe write descriptor");
                exit(EXIT_FAILURE);           
            } 

            exit(0);
        } else if (retfork == -1) {
            perror("fork");
            exit(1);
        }
    }

    //Wait time for each child process
    for (int i = 0; i < forks; i++) {
        wait(NULL);
    }

    //Parent process
    double partialPi;
    for(int i = 0; i < forks; i++) {
        // Fecha o lado de escrita do pipe
        if ( close(pipefd[i][PIPE_WR]) < 0) {
            perror("Closing pipe write descriptor");
            exit(EXIT_FAILURE);           
        } 

        ssize_t nBytesRd = read(pipefd[i][PIPE_RD], &partialPi, sizeof(partialPi));
        if (nBytesRd < 0) {
            perror("Erro ao ler do pipe");
        }
        
        pi += partialPi;

        // Fecha o lado de leitura do pipe
        if ( close(pipefd[i][PIPE_RD]) < 0) {
            perror("Closing pipe read descriptor");
            exit(EXIT_FAILURE);           
        }
    }



    pi *= 4;
    printf("%.15f\n", pi);
    return 0;
}