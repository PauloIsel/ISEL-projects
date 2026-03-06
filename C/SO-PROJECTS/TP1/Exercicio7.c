/*7. Considere o programa seguinte e responda com verdadeiro (V) ou falso (F) às seguintes afirmações.
A execução deste código irá originar além do processo
principal mais 6 processos. Falso

A execução deste código irá originar além do processo
principal mais 4 processos. Falso

Este código pode originar processos órfãos, mas não processos
zombies (defuncts). Falso

Este código pode originar processos órfãos e processos
zombies (defuncts). Verdadeiro
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

int main ()
{
    printf("MSG 1\n");
    for (int i = 0; i < 3; ++i) {
        if (fork() == 0) {
        printf("MSG 2\n");
        }
    }
    printf("MSG 3\n");
    return 0;
} 