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




int main(int argc,char* argv[]) {
    if(argc != 2){
        fprintf(stderr,"Não tem argumentos suficientes");
        return -1;
    }

    double pi = 0.0;
    int num = atoi(argv[1]);
    if (num <= 0 ){
        fprintf(stderr, "Tem que ser maior que zero");
        return -2;
    }

    for(int k = 1; k <= num; k++) {
        double term = ((k % 2 == 0) ? -1.0 : 1.0) / (2 * k - 1);
        pi += term;
    }

    pi *= 4;
    printf("%.15f\n", pi);
    return 0;
}