/*
 * Ver ficheiro lab.txt com indicações de ensaios
 */
#include <stdio.h>
#include <stdlib.h>


void handleError (char *msg)
{
	fprintf(stderr, "ERROR: %s\n", msg);
	exit(EXIT_FAILURE);
}


int main (int argc, char *argv[]) 
{
    FILE *        fsrc;
    FILE *        fdst;
    unsigned long bufferDim;
    char *        buf;
    
    // validar argumentos do programa
    // fcopy <src file> <dst file> <dim buffer>
    if (argc != 4) 
    handleError("Use: fcopy <src file> <dst file> <dim buffer>");

    printf("Source file     : %s\n", argv[1]);
    printf("Destination file: %s\n", argv[2]);
    printf("Buffer size     : %s\n", argv[3]); 
        
    bufferDim = atol(argv[3]);
    if ( bufferDim <= 0 ) handleError("Bad buffer size");
    
    buf = malloc(bufferDim);
    if ( buf == NULL ) handleError("Buffer allocation error");
    
    
    if ( (fsrc = fopen(argv[1], "r")) == NULL) handleError("Open source File");

    if ( (fdst = fopen(argv[2], "w")) == NULL) handleError("Open destination File");

    size_t nBytesRd;

    while ( (nBytesRd = fread(buf, 1, bufferDim, fsrc)) > 0 ) {
		size_t nBytesWr = fwrite(buf, 1, nBytesRd, fdst);
		if (nBytesWr < nBytesRd) handleError("Write error");    
	}
	
	if ( ferror(fsrc) ) handleError("Read error"); 
	
	fclose(fsrc); 
	fclose(fdst);
    
	return 0;
}

/*Registe os tempos (utilizando o comando time) que demora a copiar um ficheiro com os programas fcopy e
copy utilizando diferentes dimensões para o buffer, e.g., 1, 64, 128, 256, 512, 1024, etc. Compare os tempos
obtidos justificando os resultados e as diferenças de desempenho obtidas.*/

/*Ao visualizar os tempos e os traces podemos observar que o tempo independemente do buffer é relativamente constante 
com um pequeno desvio (entre 1,2 a 0.8 segundos). Outra situação a notar é que as chamadas de read e write vão ser sempre iguais,
independentemente do buffer também, sendo sempre no total 2603 calls. */

/*
Source file     : filesrc
Destination file: filedst
Buffer size     : 1

real    0m1,239s
user    0m0,125s
sys     0m0,427s

Source file     : filesrc
Destination file: filedst
Buffer size     : 1
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 53,11    0,124739          97      1283           write
 46,44    0,109072          85      1282           read
  0,37    0,000859         214         4           openat
  0,07    0,000173          43         4           close
  0,00    0,000011           2         5           fstat
  0,00    0,000000           0         8           mmap
  0,00    0,000000           0         3           mprotect
  0,00    0,000000           0         1           munmap
  0,00    0,000000           0         3           brk
  0,00    0,000000           0         2           pread64
  0,00    0,000000           0         1         1 access
  0,00    0,000000           0         1           execve
  0,00    0,000000           0         1           arch_prctl
  0,00    0,000000           0         1           set_tid_address
  0,00    0,000000           0         1           set_robust_list
  0,00    0,000000           0         1           prlimit64
  0,00    0,000000           0         1           getrandom
  0,00    0,000000           0         1           rseq
------ ----------- ----------- --------- --------- ----------------
100,00    0,234854          90      2603         1 total

-------------------------------------------------------------------------------------------------------------
Source file     : filesrc
Destination file: filedst
Buffer size     : 64

real    0m0,983s
user    0m0,014s
sys     0m0,314s

Source file     : filesrc
Destination file: filedst
Buffer size     : 64
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 55,72    0,137822         107      1283           write
 42,97    0,106284          82      1282           read
  0,53    0,001300         325         4           openat
  0,21    0,000515          64         8           mmap
  0,11    0,000263          65         4           close
  0,10    0,000249          49         5           fstat
  0,09    0,000219          73         3           mprotect
  0,07    0,000164         164         1           munmap
  0,03    0,000084          84         1           set_tid_address
  0,03    0,000065          21         3           brk
  0,03    0,000065          65         1           prlimit64
  0,03    0,000064          64         1           arch_prctl
  0,02    0,000058          29         2           pread64
  0,02    0,000047          47         1           rseq
  0,02    0,000046          46         1         1 access
  0,02    0,000046          46         1           set_robust_list
  0,02    0,000046          46         1           getrandom
  0,00    0,000000           0         1           execve
------ ----------- ----------- --------- --------- ----------------
100,00    0,247337          95      2603         1 total

-----------------------------------------------------------------------------------------------------------
Source file     : filesrc
Destination file: filedst
Buffer size     : 128

real    0m1,102s
user    0m0,025s
sys     0m0,363s

Source file     : filesrc
Destination file: filedst
Buffer size     : 128
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 52,90    0,154975         120      1283           write
 45,87    0,134390         104      1282           read
  0,26    0,000769         769         1           execve
  0,15    0,000445         148         3           mprotect
  0,14    0,000415         103         4           close
  0,14    0,000408          51         8           mmap
  0,11    0,000319          79         4           openat
  0,10    0,000285          95         3           brk
  0,10    0,000284         284         1           set_robust_list
  0,06    0,000186          37         5           fstat
  0,03    0,000099          49         2           pread64
  0,03    0,000094          94         1           arch_prctl
  0,03    0,000078          78         1           munmap
  0,02    0,000053          53         1           prlimit64
  0,02    0,000053          53         1           rseq
  0,02    0,000052          52         1           set_tid_address
  0,01    0,000042          42         1           getrandom
  0,01    0,000022          22         1         1 access
------ ----------- ----------- --------- --------- ----------------
100,00    0,292969         112      2603         1 total

-----------------------------------------------------------------------------------------------------------

Source file     : filesrc
Destination file: filedst
Buffer size     : 256

real    0m0,800s
user    0m0,007s
sys     0m0,244s

Buffer size     : 256
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 50,86    0,187154         145      1283           write
 49,03    0,180432         140      1282           read
  0,11    0,000418         104         4           close
  0,00    0,000005           1         5           fstat
  0,00    0,000000           0         8           mmap
  0,00    0,000000           0         3           mprotect
  0,00    0,000000           0         1           munmap
  0,00    0,000000           0         3           brk
  0,00    0,000000           0         2           pread64
  0,00    0,000000           0         1         1 access
  0,00    0,000000           0         1           execve
  0,00    0,000000           0         1           arch_prctl
  0,00    0,000000           0         1           set_tid_address
  0,00    0,000000           0         4           openat
  0,00    0,000000           0         1           set_robust_list
  0,00    0,000000           0         1           prlimit64
  0,00    0,000000           0         1           getrandom
  0,00    0,000000           0         1           rseq
------ ----------- ----------- --------- --------- ----------------
100,00    0,368009         141      2603         1 total

------------------------------------------------------------------------------------------------------------
Source file     : filesrc
Destination file: filedst
Buffer size     : 512

real    0m1,132s
user    0m0,011s
sys     0m0,312s

Buffer size     : 512
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 55,53    0,125798          98      1283           write
 43,99    0,099669          77      1282           read
  0,34    0,000771         771         1           execve
  0,06    0,000147          36         4           close
  0,05    0,000108          27         4           openat
  0,01    0,000024           3         8           mmap
  0,01    0,000013           4         3           mprotect
  0,00    0,000009           1         5           fstat
  0,00    0,000005           5         1           munmap
  0,00    0,000004           1         3           brk
  0,00    0,000003           3         1         1 access
  0,00    0,000002           1         2           pread64
  0,00    0,000001           1         1           set_tid_address
  0,00    0,000001           1         1           set_robust_list
  0,00    0,000001           1         1           rseq
  0,00    0,000000           0         1           arch_prctl
  0,00    0,000000           0         1           prlimit64
  0,00    0,000000           0         1           getrandom
------ ----------- ----------- --------- --------- ----------------
100,00    0,226556          87      2603         1 total

----------------------------------------------------------------------------------------
Source file     : filesrc
Destination file: filedst
Buffer size     : 1024

real    0m0,961s
user    0m0,019s
sys     0m0,341s

Source file     : filesrc
Destination file: filedst
Buffer size     : 1024
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 53,58    0,197331         153      1283           write
 45,92    0,169120         131      1282           read
  0,34    0,001248        1248         1           execve
  0,09    0,000315          78         4           openat
  0,04    0,000152          38         4           close
  0,02    0,000067           8         8           mmap
  0,01    0,000025           8         3           mprotect
  0,01    0,000022           4         5           fstat
  0,00    0,000015          15         1           munmap
  0,00    0,000007           2         3           brk
  0,00    0,000007           3         2           pread64
  0,00    0,000004           4         1           arch_prctl
  0,00    0,000003           3         1           prlimit64
  0,00    0,000003           3         1           getrandom
  0,00    0,000002           2         1           set_tid_address
  0,00    0,000002           2         1           set_robust_list
  0,00    0,000002           2         1           rseq
  0,00    0,000000           0         1         1 access
------ ----------- ----------- --------- --------- ----------------
100,00    0,368325         141      2603         1 total
*/