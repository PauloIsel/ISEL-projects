#ifndef __ERROR_H__
#define __ERROR_H__

void handle_error(const char *msg);
void check_system_and_exit(int ret, char *msg);
void check_pthread_and_exit(int ret, char *msg);
void handle_socket_error(int proc, const char *msg);

#endif