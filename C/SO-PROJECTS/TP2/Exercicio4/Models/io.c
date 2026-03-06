#include <sys/stat.h>
#include <unistd.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include "io.h"

int read_client_header(int clientSocket, char *buffer, long buffer_size) {
    int total_bytes = 0;
    while (total_bytes < buffer_size) {
        int nbytes = read(clientSocket, buffer + total_bytes, 1);
        if (nbytes <= 0) {
            log_message(ERROR, "Error reading from socket");
            return -1; 
        }
        total_bytes += nbytes;

        buffer[total_bytes] = '\0';
        if (strstr(buffer, "\n\n") != NULL) {
            log_message(DEBUG, "Header successfully read");
            return 0;
        }
    }

    return -1;
}

int parse_client_header(const char *buffer, char *program, char *args, char *filename, long *file_size) {
    if (sscanf(buffer, "RUN: %255[^\n]\nARGS: %255[^\n]\nFILE: %255[^\n]\nDIM: %ld\n",
               program, args, filename, file_size) != 4) {
        log_message(ERROR, "Invalid header format");
        return -1;
    }
    return 0;
}

char* receive_file_content(int clientSocket, long file_size) {
    char *file_content = malloc(file_size);
    if (!file_content) {
        log_message(ERROR, "Error allocating memory for file content");
        return NULL;
    }

    long received = 0;
    int nbytes;
    while (received < file_size) {
        nbytes = read(clientSocket, file_content + received, file_size - received);
        // printf("Received %d bytes\n", nbytes);
        if (nbytes <= 0) {
            log_message(ERROR, "Error receiving file content");
            free(file_content);
            return NULL;
        }
        received += nbytes;
    }

    return file_content;
}

void send_program_output_to_client(char *header, int pipe_out_read, int clientSocket, char *header_args[]) {
    // Create a temporary file to store the content
    char temp_filename[] = "/tmp/tempFileXXXXXX";
    int temp_fd = mkstemp(temp_filename);
    if (temp_fd == -1) {
        log_message(ERROR, "Error creating temporary file");
        close(pipe_out_read);
        return;
    }
    
    // Write the content from the pipe to the temporary file
    char output_buffer[FILE_SOCKET_BUFFER];
    ssize_t nbytes;
    while ((nbytes = read(pipe_out_read, output_buffer, FILE_SOCKET_BUFFER)) > 0) {
        if (write(temp_fd, output_buffer, nbytes) < 0) {
            log_message(ERROR, "Error writing to temporary file");
            close(pipe_out_read);
            close(temp_fd);
            unlink(temp_filename);
            return;
        }
    }

    // Get the size of the content using fstat
    struct stat file_stat;
    if (fstat(temp_fd, &file_stat) == -1) {
        log_message(ERROR, "Error getting temporary file content for file size");
        close(pipe_out_read);
        close(temp_fd);
        unlink(temp_filename);
        return;
    }
    long content_size = file_stat.st_size;

    char *output_filename_with_extension = get_output_file_with_extension(header_args[0], header_args[2]);
    if (!output_filename_with_extension) {
        log_message(ERROR, "Error obtaining output filename with extension");
        close(pipe_out_read);
        close(temp_fd);
        unlink(temp_filename);
        return;
    }

    header_to_client(header, header_args[0], header_args[1], output_filename_with_extension, content_size);

    // Send the header to the client
    if (write(clientSocket, header, strlen(header)) < 0) {
        log_message(ERROR, "Error sending header to client");
        close(pipe_out_read);
        close(temp_fd);
        free(output_filename_with_extension);
        unlink(temp_filename);
        return;
    }

    // Send the content from the temporary file to the client
    lseek(temp_fd, 0, SEEK_SET); 
    while ((nbytes = read(temp_fd, output_buffer, FILE_SOCKET_BUFFER)) > 0) {
        ssize_t total_sent = 0;
        while (total_sent < nbytes) {
            ssize_t sent = write(clientSocket, output_buffer + total_sent, nbytes - total_sent);
            // printf("Sending %ld bytes\n", nbytes);
            if (sent < 0) {
                log_message(ERROR, "Error sending content to client");
                break;
            }
            total_sent += sent;
        }
    }

    // Clean up
    close(pipe_out_read);
    close(temp_fd);
    free(output_filename_with_extension);
    unlink(temp_filename);
}

void header_to_client(char *header, char *program, char *arguments, char *filename, long size) {
    snprintf(header, MAX_BUFFER,
        "RUN: %s\n"
        "ARGS: %s\n"
        "FILE: %s\n"
        "DIM: %ld\n\n",
        program, arguments, filename, size);
}

void build_header(char *program, char *args, char *filename, long file_size) {
    printf("========================================\n");
    printf("Program:   %s\n", program);
    printf("Arguments: %s\n", args);
    printf("Filename:  %s\n", filename);
    printf("Size:      %ld\n", file_size);
    printf("========================================\n");
}

void send_content_in_chunks(int pipe_write_fd, const char *file_content, size_t file_size) {
    size_t bytes_sent = 0;

    // Ignorar SIGPIPE para evitar interrupção do processo em caso de erro de escrita
    signal(SIGPIPE, SIG_IGN);

    while (bytes_sent < file_size) {
        size_t bytes_to_send = (file_size - bytes_sent) < FILE_SOCKET_BUFFER ? (file_size - bytes_sent) : FILE_SOCKET_BUFFER;
        ssize_t result = write(pipe_write_fd, file_content + bytes_sent, bytes_to_send);
        if (result == -1) {
            log_message(ERROR, "Error writing to pipe");
            break;
        }
        bytes_sent += result;
        //fprintf(stdout,"Enviados %zu bytes\n", bytes_sent);
    }

    close(pipe_write_fd);
}


void setArgumentsToList(char* arg_list[], char program[], char args[]) {
    arg_list[0] = strdup(program);
    int i = 1;

    char *token = strtok(args, " ");
    while (token != NULL && i < ARGS_BUFFER - 1) {
        arg_list[i++] = strdup(token);
        token = strtok(NULL, " ");
    }

    arg_list[i++] = strdup("-");
    arg_list[i++] = strdup("-");
    arg_list[i] = NULL;

    // printf("Executando programa: %s com os seguintes argumentos:\n", program);
    // for (int j = 0; j < i; j++) {
    //    fprintf(stdout, "arg_list[%d]: %s\n", j, arg_list[j]);
    // }
}

void freeArguments(char* arg_list[]) {
    for (int i = 0; arg_list[i] != NULL; i++) {
        free(arg_list[i]);
    }
}

char* get_output_extension(char *program) {
    if (strcmp(program, "pdftotext") == 0) {
        return ".txt";
    }

    return "";
}


char* get_output_file_with_extension(const char *program, const char *header_arg) {
    const char *output_extension = get_output_extension((char *)program);
    if (strcmp(output_extension, "") == 0) {
        return strdup(header_arg);
    }

    const char *dot_position = strrchr(header_arg, '.');
    size_t base_length = (dot_position != NULL) ? (size_t)(dot_position - header_arg) : strlen(header_arg);

    size_t total_length = base_length + strlen(output_extension) + 1; // +1 for null terminator
    char *output_filename_with_extension = malloc(total_length);
    if (!output_filename_with_extension) {
        log_message(ERROR, "Error allocating memory for output filename");
        return NULL;
    }

    snprintf(output_filename_with_extension, total_length, "%.*s%s", (int)base_length, header_arg, output_extension);
    return output_filename_with_extension;
}