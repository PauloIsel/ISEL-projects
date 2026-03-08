#include "error.h"
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>

void handle_error(const char *msg) {
	perror(msg);
	exit(EXIT_FAILURE);
}

void check_system_and_exit(int ret, char *msg) {
	if (ret == -1) handle_error(msg);
}

void check_pthread_and_exit(int ret, char *msg) {
	if (ret != 0) {
		errno = ret;
		handle_error(msg);
	}
}

void handle_socket_error(int proc, const char *msg) {
	if(proc < 0) handle_error(msg);
}
