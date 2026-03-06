#include "../Models/log.h"
#include "../Models/error.h"
#include "../Models/socket_utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>

int main() {
    if (log_init("logfile.txt") != 0) {
        handle_error("Falha ao iniciar o módulo de logging");
        return -1;
    }

    log_message(INFO, "Servidor iniciado");
    log_message(DEBUG, "A testar o módulo de logging...");
    log_message(ERROR, "Algo correu mal (mas só no teste)");

    // Inicializa um servidor TCP na porta 0 (porta escolhida pelo sistema)
    int server_fd = tcp_server_socket_init(0);
    if (server_fd < 0) {
        log_message(ERROR, "Falha ao inicializar o servidor TCP");
        log_close();
        return -1;
    }

    // Obtém a porta atribuída pelo sistema
    struct sockaddr_in addr;
    socklen_t len = sizeof(addr);
    getsockname(server_fd, (struct sockaddr*)&addr, &len);
    int port = ntohs(addr.sin_port);

    // Inicializa um cliente TCP e conecta ao servidor
    int client_fd = tcp_client_socket_init("127.0.0.1", port);
    if (client_fd < 0) {
        log_message(ERROR, "Falha ao inicializar o cliente TCP");
        close(server_fd);
        log_close();
        return -1;
    }

    // Aceita a conexão no lado do servidor
    int sockfd = tcp_server_socket_accept(server_fd);
    if (sockfd >= 0) {
        log_message_with_end_point(INFO, "Ligação simulada recebida", sockfd);
        close(sockfd);
    } else {
        log_message(ERROR, "Falha ao aceitar ligação");
    }

    close(client_fd);
    close(server_fd);

    log_message(INFO, "Servidor a terminar...");
    log_close();
    return 0;
}