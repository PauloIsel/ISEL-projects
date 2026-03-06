#include "socket_utils.h"
#include "error.h"
#include <netdb.h>
#include <sys/un.h>
#include <arpa/inet.h>
#include <unistd.h>

#define QUEUE_TIME 5


int tcp_server_socket_init (int serverPort) {
    int socketfd = socket(AF_INET, SOCK_STREAM, 0);
    handle_socket_error(socketfd, "Error asking for socket descriptor");

    int opt = 1;
    if (setsockopt(socketfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        handle_error("Error setting socket options");
    }

    struct sockaddr_in serverAddr;
    memset((char*)&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    serverAddr.sin_port = htons(serverPort);

    int binding = bind(socketfd, (struct sockaddr *)&serverAddr, sizeof(serverAddr));
    handle_socket_error(binding, "Error binding socket");

    int listening = listen(socketfd, QUEUE_TIME);
    handle_socket_error(listening, "Error listening on socket");

    return socketfd;
}


int tcp_server_socket_accept (int serverSocket) {
    struct sockaddr_in clientAddr;
    socklen_t clientAddrLen = sizeof(clientAddr);
    
    int newSocketfd = accept(serverSocket, (struct sockaddr*) &clientAddr, &clientAddrLen);
    handle_socket_error(newSocketfd, "Error accepting connection");

    return newSocketfd;
}


int tcp_client_socket_init (const char *host, int port) {
    struct hostent* hostEntry = gethostbyname(host);
    in_addr_t serverAddress;

    if(hostEntry != NULL) {
        memcpy(&serverAddress, hostEntry->h_addr_list[0], hostEntry->h_length);
    } else if((serverAddress = inet_addr(host)) == INADDR_NONE) {
        handle_error("Impossible to resolve host"); 
    }

    if((port < 1) || (port > 65535)) {
        handle_error("Port must be between 1 and 65535");
    }

    int socketfd = socket(AF_INET, SOCK_STREAM, 0);
    handle_socket_error(socketfd, "Error asking for socket descriptor");

    struct sockaddr_in serverAddr;
    memset((char*)&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = serverAddress;
    serverAddr.sin_port = htons(port);

    int connection = connect(socketfd, (struct sockaddr *)&serverAddr, sizeof(serverAddr));
    handle_socket_error(connection, "Connection failed");

    return socketfd;
}


int un_server_socket_init (const char *serverEndPoint) {
    unlink(serverEndPoint);

    int socketfd = socket(AF_UNIX, SOCK_STREAM, 0);
    handle_socket_error(socketfd, "Error asking for socket descriptor");

    struct sockaddr_un serverAddr;
    serverAddr.sun_family = AF_UNIX;
    strcpy(serverAddr.sun_path, serverEndPoint);

    int binding = bind(socketfd, (struct sockaddr *)&serverAddr, sizeof(serverAddr));
    handle_socket_error(binding, "Error binding socket");

    int listening = listen(socketfd, QUEUE_TIME);
    handle_socket_error(listening, "Error listening on socket");
    
    return socketfd;
}

int un_server_socket_accept (int serverSocket) {
    struct sockaddr_un clientAddr;
    socklen_t clientAddrLen = sizeof(clientAddr);
    
    int newSocketfd = accept(serverSocket, (struct sockaddr*) &clientAddr, &clientAddrLen);
    handle_socket_error(newSocketfd, "Error accepting connection");

    return newSocketfd;
}


int un_client_socket_init (const char *serverEndPoint) {
    int socketfd = socket(AF_UNIX, SOCK_STREAM, 0);
    handle_socket_error(socketfd, "Error asking for socket descriptor");

    struct sockaddr_un serverAddr;
    memset((char*)&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sun_family = AF_UNIX;
    strcpy(serverAddr.sun_path, serverEndPoint);

    int connection = connect(socketfd, (struct sockaddr *)&serverAddr, sizeof(serverAddr));
    handle_socket_error(connection, "Connection failed");

    return socketfd;
}
