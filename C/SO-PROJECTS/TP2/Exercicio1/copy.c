/*
 * Ver ficheiro lab.txt com indicações de ensaios
 */
#include <stdio.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>



void handleError (char *msg)
{
	fprintf(stderr, "ERROR: %s\n", msg);
	exit(EXIT_FAILURE);
}


void handleSystemError (char *msg)
{
	perror(msg);
	exit(EXIT_FAILURE);
}

int main (int argc, char *argv[]) 
{
    int           fsrc;
    int           fdst;
    unsigned long bufferDim;
    char *        buf;
    
    // validar argumentos do programa
    // fcopy <src file> <dst file> <dim buffer>
    if (argc != 4) 
       handleError("Use: fcopy <src file> <dst file> <dim buffer>");

    printf("Source     : %s\n", argv[1]);
    printf("Destination: %s\n", argv[2]);
    printf("Buffer     : %s\n", argv[3]); 
        
    bufferDim = atol(argv[3]);
    if ( bufferDim <= 0 ) handleError("Bad buffer size");
    
    buf = malloc(bufferDim);
    if ( buf == NULL) handleError("Buffer allocation error");
    
    
    if ( (fsrc = open(argv[1], O_RDONLY)) < 0 ) 
        handleSystemError("Open source File");

    if ( (fdst = open(argv[2], O_WRONLY | O_CREAT | O_TRUNC, 0664)) < 0 ) 
        handleSystemError("Open destination File");

    int cnt = 0;
    
    size_t nBytesRd;
    while ( (nBytesRd = read(fsrc, buf, bufferDim)) > 0 ) {
		size_t nBytesWr = write(fdst, buf, nBytesRd);
        if(nBytesWr < 0) handleSystemError("Write Error");
		if (nBytesWr < nBytesRd) printf("Write didn't write everything\n");
		cnt++;    
	}
	
	printf("Read calls %d\n", cnt + 1);
	
	if ( nBytesRd < 0 ) handleSystemError("Read error"); 
	
	if ( close(fsrc) < 0) handleSystemError("Closing source file"); 
	if ( close(fdst) < 0) handleSystemError("Closing destination file");
	
	return 0;
}

/*Registe os tempos (utilizando o comando time) que demora a copiar um ficheiro com os programas fcopy e
copy utilizando diferentes dimensões para o buffer, e.g., 1, 64, 128, 256, 512, 1024, etc. Compare os tempos
obtidos justificando os resultados e as diferenças de desempenho obtidas.*/

/*Para o buffer igual a 1 e 64, no teste que foi feito na máquina virtual este teve que ser terminado após 4 min, assim,
 foi considerado indefinido o tempo

Ao aumentar o buffer no copy, menos é calls são feitas para o sistema operativo e menos tempo demora, aproxima-se
cada vez mais da perfomance do fcopy, que é o ideal. Assim, o tempo de execução do copy com buffer 4096 é 1.638s, enquanto 
que o fcopyé entre 1.2 a 0.8 segundos.
 
 Source     : filesrc
Destination: filedst
Buffer     : 128
Read calls 81921

real    0m52,114s
user    0m0,290s
sys     0m27,582s

Source     : filesrc
Destination: filedst
Buffer     : 128
Read calls 81921
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 50,95   14,299809         174     81922           read
 49,04   13,762380         167     81924           write
  0,00    0,000900         900         1           execve
  0,00    0,000399          99         4           close
  0,00    0,000180          45         4           openat
  0,00    0,000031           3         8           mmap
  0,00    0,000013           4         3           mprotect
  0,00    0,000007           7         1           munmap
  0,00    0,000006           2         3           fstat
  0,00    0,000005           1         3           brk
  0,00    0,000003           3         1         1 access
  0,00    0,000002           1         2           pread64
  0,00    0,000002           2         1           set_tid_address
  0,00    0,000001           1         1           arch_prctl
  0,00    0,000001           1         1           prlimit64
  0,00    0,000001           1         1           getrandom
  0,00    0,000001           1         1           rseq
  0,00    0,000000           0         1           set_robust_list
------ ----------- ----------- --------- --------- ----------------
100,00   28,063741         171    163882         1 total
 
 Source     : filesrc
Destination: filedst
Buffer     : 256
Read calls 40961

real    0m26,051s
user    0m0,141s
sys     0m11,587s

Source     : filesrc
Destination: filedst
Buffer     : 256
Read calls 40961
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 50,78    6,469721         157     40964           write
 49,21    6,268495         153     40962           read
  0,01    0,000862         862         1           execve
  0,00    0,000294          73         4           close
  0,00    0,000111          27         4           openat
  0,00    0,000020           2         8           mmap
  0,00    0,000007           2         3           mprotect
  0,00    0,000004           4         1           munmap
  0,00    0,000003           1         3           fstat
  0,00    0,000003           1         3           brk
  0,00    0,000002           1         2           pread64
  0,00    0,000002           2         1         1 access
  0,00    0,000001           1         1           arch_prctl
  0,00    0,000001           1         1           set_tid_address
  0,00    0,000001           1         1           set_robust_list
  0,00    0,000001           1         1           getrandom
  0,00    0,000001           1         1           rseq
  0,00    0,000000           0         1           prlimit64
------ ----------- ----------- --------- --------- ----------------
100,00   12,739529         155     81962         1 total
 
Source     : filesrc
Destination: filedst
Buffer     : 512
Read calls 20481

real    0m10,930s
user    0m0,055s
sys     0m5,329s

Source     : filesrc
Destination: filedst
Buffer     : 512
Read calls 20481
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 50,27    2,931169         143     20484           write
 49,72    2,899085         141     20482           read
  0,01    0,000603         150         4           openat
  0,01    0,000320          80         4           close
  0,00    0,000037           4         8           mmap
  0,00    0,000016           5         3           mprotect
  0,00    0,000007           7         1           munmap
  0,00    0,000006           2         3           fstat
  0,00    0,000003           3         1         1 access
  0,00    0,000002           0         3           brk
  0,00    0,000002           1         2           pread64
  0,00    0,000001           1         1           arch_prctl
  0,00    0,000001           1         1           set_robust_list
  0,00    0,000001           1         1           prlimit64
  0,00    0,000001           1         1           getrandom
  0,00    0,000001           1         1           rseq
  0,00    0,000000           0         1           execve
  0,00    0,000000           0         1           set_tid_address
------ ----------- ----------- --------- --------- ----------------
100,00    5,831255         142     41002         1 total

Source     : filesrc
Destination: filedst
Buffer     : 1024
Read calls 10241

real    0m5,697s
user    0m0,025s
sys     0m2,414s

Source     : filesrc
Destination: filedst
Buffer     : 1024
Read calls 10241
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 54,01    0,971139          94     10244           write
 45,95    0,826175          80     10242           read
  0,03    0,000530         132         4           openat
  0,01    0,000183          45         4           close
  0,00    0,000000           0         3           fstat
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
100,00    1,798027          87     20522         1 total

Source     : filesrc
Destination: filedst
Buffer     : 4096
Read calls 2561

real    0m1,638s
user    0m0,009s
sys     0m0,475s

Source     : filesrc
Destination: filedst
Buffer     : 4096
Read calls 2561
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 52,11    0,342586         133      2564           write
 47,66    0,313316         122      2562           read
  0,14    0,000935         935         1           execve
  0,04    0,000282          70         4           openat
  0,03    0,000204          51         4           close
  0,00    0,000030           3         8           mmap
  0,00    0,000014           4         3           mprotect
  0,00    0,000009           9         1           munmap
  0,00    0,000007           2         3           brk
  0,00    0,000006           2         3           fstat
  0,00    0,000004           4         1         1 access
  0,00    0,000002           1         2           pread64
  0,00    0,000001           1         1           set_tid_address
  0,00    0,000001           1         1           set_robust_list
  0,00    0,000001           1         1           prlimit64
  0,00    0,000001           1         1           getrandom
  0,00    0,000001           1         1           rseq
  0,00    0,000000           0         1           arch_prctl
------ ----------- ----------- --------- --------- ----------------
100,00    0,657400         127      5162         1 total
 */
