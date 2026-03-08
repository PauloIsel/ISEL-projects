#ifndef __IO_H__
#define __IO_H__

#include "log.h"

#define MAX_BUFFER 256
#define ARGS_BUFFER 128
#define FILE_SOCKET_BUFFER 4096

int read_client_header(int clientSocket, char *buffer, long buffer_size);
int parse_client_header(const char *buffer, char *program, char *args, char *filename, long *file_size);
char* receive_file_content(int clientSocket, long file_size);
void send_program_output_to_client(char *header, int pipe_out_read, int clientSocket, char *header_args[]);
void header_to_client(char *header, char *program, char *args, char *filename, long file_size);
void build_header(char *program, char *args, char *filename, long file_size);
void send_content_in_chunks(int pipe_write_fd, const char *file_content, size_t file_size);
void setArgumentsToList(char* arg_list[], char program[], char args[]);
void freeArguments(char* arg_list[]);
char* get_output_extension(char *program);
char* get_output_file_with_extension(const char *program, const char *header_agr);

#endif