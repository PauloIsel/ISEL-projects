#ifndef __CLIENT_H__
#define __CLIENT_H__

#include "error.h"
#include "socket_utils.h"
#include "io.h"
#include <stdbool.h>

#define MAX_PATH 256

typedef struct {
    char host[ARGS_BUFFER];
    int port;
    bool is_unix;
    bool receive;
    int connections;
    char directory[MAX_PATH];
    char program[ARGS_BUFFER];
    char args[ARGS_BUFFER];
    char **files;
    int num_files;
} client_config_t;

void print_usage(const char *program_name);
void parse_arguments(int argc, char *argv[], client_config_t *config);

#endif