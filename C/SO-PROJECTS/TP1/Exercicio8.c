/*8. Considere o programa seguinte e responda com verdadeiro (V) ou falso (F) às seguintes afirmações.

Este programa executa o programa date, para imprimir a data
e hora no standard de output, em intervalos de 1 segundo
durante 10 segundos. Falso

São criados 10 processos auxiliares para executarem o
programa date ficando esses processos no estado zombie. Falso

Este código apenas executa o programa date 1 única vez. Verdadeiro

Este código deveria esperar pelos processos filho através da
utilização da chamada de sistema waitpid(). Falso
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

int main ()
{
    for (int i = 0; i < 10; ++i) {
        execlp("/bin/date", "date", NULL);
        sleep(1);
    }
    return 0;
} 