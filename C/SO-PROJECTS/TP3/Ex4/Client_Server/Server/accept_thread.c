#include "accept_thread.h"
#include "server.h"
#include <unistd.h>
#include <stdlib.h>

void* accept_thread_tcp(void *args) {
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, NULL);

    AcceptThreadArgs *acceptArgs = (AcceptThreadArgs *)args;

    while (server_running) {
        if(!server_running) break;
        int newSocket = tcp_server_socket_accept(*acceptArgs->socket);
        if (newSocket < 0) {
            if (!server_running) break;
            log_message(ERROR, "Error accepting TCP client");
            continue;
        }

        pthread_mutex_lock(&clients_mutex);
        tcp_clients++;
		log_message(INFO, "New TCP client connected");
        pthread_mutex_unlock(&clients_mutex);

        ThreadArgs *threadArgs = malloc(sizeof(ThreadArgs));
        if (!threadArgs) {
            log_message(ERROR, "Error allocating ThreadArgs");
            close(newSocket);
            continue;
        }
        threadArgs->socket = newSocket;
        threadArgs->is_unix = false;

		if (threadpool_submit(acceptArgs->threadpool, th_handle_client, threadArgs) != 0) {
			log_message(ERROR, "Error creating thread for TCP client");
			close(newSocket);
			free(threadArgs);
		}
    }
    log_message(DEBUG, "TCP accept thread terminated");
    return NULL;
}


void* accept_thread_unix(void *args) {
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, NULL);
    
    AcceptThreadArgs *acceptArgs = (AcceptThreadArgs *)args;

    while (server_running) {
        if(!server_running) break;
        int newSocket = un_server_socket_accept(*acceptArgs->socket);
        if (newSocket < 0) {
            if (!server_running) break;
            log_message(ERROR, "Error accepting UNIX client");
            continue;
        }

        pthread_mutex_lock(&clients_mutex);
        unix_clients++;
		log_message(INFO, "New UNIX client connected");
        pthread_mutex_unlock(&clients_mutex);

        ThreadArgs *threadArgs = malloc(sizeof(ThreadArgs));
        if (!threadArgs) {
            log_message(ERROR, "Error allocating ThreadArgs");
            close(newSocket);
            continue;
        }
        threadArgs->socket = newSocket;
        threadArgs->is_unix = true;

        if (threadpool_submit(acceptArgs->threadpool, th_handle_client, threadArgs) != 0) {
            log_message(ERROR, "Error creating thread for UNIX client");
            close(newSocket);
            free(threadArgs);
        }
    }
    log_message(DEBUG, "UNIX accept thread terminated");
    return NULL;
}