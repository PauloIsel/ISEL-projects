#include "server.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include "io.h"

#define PIPE_READ 0
#define PIPE_WRITE 1

#define TP_QUEUE_DIM 20
#define TP_NTH_MIN 4
#define TP_NTH_MAX 8
#define TP_C_INC_THRESHOLD 80
#define TP_C_DEC_THRESHOLD 20

int tcp_clients = 0;
int unix_clients = 0;
bool server_running = true;
pthread_mutex_t clients_mutex = PTHREAD_MUTEX_INITIALIZER;


void handle_client(int clientSocket) {
    char buffer[MAX_BUFFER + 1];
    char program[ARGS_BUFFER], args[ARGS_BUFFER], filename[ARGS_BUFFER];
    long file_size;
    int pipe_in[2], pipe_out[2];
    pid_t pid;

    // Ler cabeçalho do protocolo
    if(read_client_header(clientSocket, buffer, MAX_BUFFER) < 0) {
        log_message(ERROR, "Error reading client header");
        log_close();
        close(clientSocket);
        return;
    }

    if (parse_client_header(buffer, program, args, filename, &file_size) < 0) {
        close(clientSocket);
        return;
    }   

    char *arg_list[ARGS_BUFFER];
    setArgumentsToList(arg_list, program, args);

    // Criar pipes para entrada/saída do programa
    if (pipe(pipe_in) < 0 || pipe(pipe_out) < 0) {
        log_message(ERROR, "Error creating pipes");
        log_close();
        freeArguments(arg_list);
        close(clientSocket);
        return;
    }

    // Criar processo filho para executar o programa
    pid = fork();
    if (pid < 0) {
        log_message(ERROR, "Error creating child process");
        log_close();
        close(pipe_in[PIPE_READ]); close(pipe_in[PIPE_WRITE]);
        close(pipe_out[PIPE_READ]); close(pipe_out[PIPE_WRITE]);
        freeArguments(arg_list);
        close(clientSocket);
        return;
    }

    if (pid == 0) {
        close(pipe_in[PIPE_WRITE]);
        close(pipe_out[PIPE_READ]); 
        
        // Redirect stdin and stdout
        dup2(pipe_in[PIPE_READ], STDIN_FILENO);
        dup2(pipe_out[PIPE_WRITE], STDOUT_FILENO);
        close(pipe_in[PIPE_READ]); 
        close(pipe_out[PIPE_WRITE]);
    
        // Execute the program
        execvp(program, arg_list);
        log_message(ERROR, "Error executing program");
        log_close();
        freeArguments(arg_list);
        close(clientSocket);
        exit(1);
    }

    // Pai: fechar extremidades desnecessárias
    close(pipe_in[PIPE_READ]);
    close(pipe_out[PIPE_WRITE]);

    // Receber conteúdo do ficheiro
    char *file_content = receive_file_content(clientSocket, file_size);
    if (!file_content) {
        close(pipe_in[PIPE_WRITE]);
        close(pipe_out[PIPE_READ]);
        freeArguments(arg_list);
        close(clientSocket);
        wait(NULL);
        return;
    }

    // Enviar conteúdo ao programa em blocos menores
    send_content_in_chunks(pipe_in[PIPE_WRITE], file_content, file_size);
    free(file_content);

    //Create the header to send to the client
    char header[MAX_BUFFER + 1];
    char *header_args[ARGS_BUFFER] = {program, args, filename};

    // Ler saída do programa e enviar ao cliente
    send_program_output_to_client(header, pipe_out[PIPE_READ], clientSocket, header_args);

    // Esperar pelo fim do processo
    wait(NULL);
    freeArguments(arg_list);
    log_message(DEBUG, "Client shutdown");
}

void* th_handle_client(void *args) {
	ThreadArgs *threadArgs = (ThreadArgs *)args;
	handle_client(threadArgs->socket);

    // Registar desconexão
    pthread_mutex_lock(&clients_mutex);
    if (threadArgs->is_unix) {
        unix_clients--;
        log_message(INFO, "UNIX Client disconnected");
    } else {
        tcp_clients--;
        log_message(INFO, "TCP Client disconnected");
    }
    pthread_mutex_unlock(&clients_mutex);

	close(threadArgs->socket);
	free(threadArgs);
    return NULL;
}


int main(int argc, char* argv[]) {
    if(argc > 2) handle_error("Invalid argument");

    int serverPort;
    
    if(argc == 1) {
        serverPort = 8000;
    } else if(argc == 2) {
        serverPort = atoi(argv[1]);
         if (serverPort < 0 || serverPort > 65535) {
            handle_error("Invalid port number");
            return 1;
        }
    }
    
    // printf("Port: %d\n", serverPort);

    const char *unix_endpoint = UNIX_ENDPOINT;

    if (log_init("server.log") != 0) {
        handle_error("Error initializing log file");
        return 1;
    }

    log_message(INFO, "Server started");

    int tcp_socket = tcp_server_socket_init(serverPort);
    check_system_and_exit(tcp_socket, "tcp_server_socket_init");

    int unix_socket = un_server_socket_init(unix_endpoint);
    check_system_and_exit(unix_socket, "un_server_socket_init");

    threadpool_t threadpool;
    int thInit = threadpool_init(&threadpool, TP_QUEUE_DIM, TP_NTH_MIN, TP_NTH_MAX, TP_C_INC_THRESHOLD, TP_C_DEC_THRESHOLD);
    if(thInit != 0) {
        printf("Error initializing thread pool\n");
        return -1;
    }

	pthread_t menu_thread;
    if (pthread_create(&menu_thread, NULL, menu_thread_function, NULL) != 0) {
        log_message(ERROR, "Error creating menu thread");
        log_close();
        close(tcp_socket);
        close(unix_socket);
        unlink(unix_endpoint);
        exit(1);
    }

    pthread_t tcp_accept_thread, unix_accept_thread;

    AcceptThreadArgs tcp_args = { .socket = &tcp_socket, .threadpool = &threadpool };
    AcceptThreadArgs unix_args = { .socket = &unix_socket, .threadpool = &threadpool };

    int tcp_thread = pthread_create(&tcp_accept_thread, NULL, accept_thread_tcp, &tcp_args);
    int unix_thread = pthread_create(&unix_accept_thread, NULL, accept_thread_unix, &unix_args);
    if (tcp_thread != 0 || unix_thread != 0) {
        log_message(ERROR, "Error creating accept threads");
        log_close();
        server_running = false;
        close(tcp_socket);
        close(unix_socket);
        unlink(unix_endpoint);
        pthread_cancel(menu_thread);
        exit(1);
    }

    // Esperar pelo thread do menu
    pthread_join(menu_thread, NULL);

    pthread_cancel(tcp_accept_thread);
    pthread_cancel(unix_accept_thread);

    pthread_join(tcp_accept_thread, NULL);
    pthread_join(unix_accept_thread, NULL);
    log_message(DEBUG, "Accept threads terminated");
    threadpool_destroy(&threadpool);

    if (tcp_socket >= 0) close(tcp_socket);
    if (unix_socket >= 0) close(unix_socket);
    unlink(unix_endpoint);
    log_message(DEBUG, "Closing sockets and unlinking unix_endpoint");

    log_message(INFO, "Server shutdown");
    log_close();
    return 0;
}