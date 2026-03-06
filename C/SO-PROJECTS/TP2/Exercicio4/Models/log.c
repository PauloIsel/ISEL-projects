#include "log.h"
#include "error.h"
#include <stdio.h>
#include <time.h>
#include <netdb.h>
#include <sys/socket.h>
#include <arpa/inet.h>


static FILE *log_file = NULL;

const char* level_to_string(LOG_LEVEL level) {
    switch (level) {
        case INFO: return "INFO";
        case ERROR: return "ERROR";
        case DEBUG: return "DEBUG";
        default: return "UNKNOWN";
    }
}

void get_time_string(char *buffer, size_t size) {
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    strftime(buffer, size, "%Y-%m-%d %H:%M:%S", t);
}

int get_ip_and_port(int sock, char *ip, size_t ip_size, unsigned short *port) {
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);

    if (getpeername(sock, (struct sockaddr *)&addr, &addr_len) == -1) {
        perror("Failed to get peer name");
        return -1;
    }

    if (inet_ntop(AF_INET, &addr.sin_addr, ip, ip_size) == NULL) {
        perror("Failed to convert IP address");
        return -2;
    }

    *port = ntohs(addr.sin_port);

    return 0; 
}

//Inicia o ficheiro onde se vai guardar os logs
int log_init(const char *pathname){
    if(pathname == NULL) {
        handle_error("Invalid pathname");
        return -1; 
    }

    log_file = fopen(pathname, "a");
    if (log_file == NULL) {
        handle_error("Failed to open log file");
        return -2; 
    }

    return 0;
}

//Crie e escreve no ficheiro um registo com o seguinte formato:
//[INFO] 2025-04-03 14:15:00 - TCP server shutting down...
int log_message(LOG_LEVEL level, const char *msg){    
    if (log_file == NULL) {
        handle_error("Log file not initialized");
        return -1;
    }

    if (msg == NULL) {
        handle_error("Invalid message");
        return -2;
    }

    char time_str[20];
    get_time_string(time_str, sizeof(time_str));

    fprintf(log_file, "[%s] %s - %s\n", level_to_string(level), time_str, msg);
    fflush(log_file);
    return 0;
}

//Crie e escreve no ficheiro um registo com o seguinte formato:
//[INFO] 2025-04-03 14:06:10 - New connection established: IP=127.0.0.1, Port=53412. 
int log_message_with_end_point(LOG_LEVEL level, const char *msg, int sock) {
    if (log_file == NULL) {
        handle_error("Log file not initialized");
        return -1;
    }

    if (msg == NULL) {
        handle_error("Invalid message");
        return -2;
    }

    char time_str[20];
    get_time_string(time_str, sizeof(time_str));

    char ip[INET_ADDRSTRLEN];
    unsigned short port;

    if (get_ip_and_port(sock, ip, sizeof(ip), &port) != 0) {
        handle_error("Failed to get IP and port");
        return -3;
    }

    fprintf(log_file, "[%s] %s - %s: IP=%s, Port=%u\n", level_to_string(level), time_str, msg, ip, port);
    fflush(log_file);

    return 0;
}

//Fecha o ficheiro onde se guardam os logs
int log_close() {
    if (log_file == NULL) return -1;
    fclose(log_file);
    log_file = NULL;
    return 0;
}