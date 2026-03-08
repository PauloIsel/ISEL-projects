#ifndef __SERVER_H__
#define __SERVER_H__

#include "accept_thread.h"
#include "socket_utils.h"
#include "menu_thread.h"
#include "error.h"
#include "log.h"
#include <stdbool.h>
#include <pthread.h>

extern int tcp_clients;
extern int unix_clients;
extern bool server_running;
extern pthread_mutex_t clients_mutex;

typedef struct {
	int socket;
	bool is_unix;
} ThreadArgs;

void handle_client(int clientSocket);
void* th_handle_client(void *args);


#endif