#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <errno.h>
#include <libgen.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <pthread.h>

#include "client.h"

void print_usage(const char *program_name) {
    printf("Usage: %s [Options] <files>\n\n", program_name);
    printf("\t-s, --host <hostname or IP>  - server host [default: localhost]\n");
    printf("\t-p, --port <port number>     - server port [default: 8000]\n");
    printf("\t-u, --unix                   - use UNIX socket\n");
    printf("\t                                     [default: TCP socket]\n");
    printf("\t-r, --receive                - enable receive from server\n");
    printf("\t-n, --connections            - Set the amount of connections of the client\n");
    printf("\t                                     [default: only send files]\n");
    printf("\t-d, --directory              - folder to save received files\n");
    printf("\t                                     [default: ReceivedFiles]\n");
    printf("\t-c, --program <program>      - program to execute on server\n");
    printf("\t                                     [default: convert]\n");
    printf("\t-a, --args <args>            - arguments to program\n");
    printf("\t                                     [default: –rotate 45]\n");
    printf("\t<files>                      - files to send to server\n");
    printf("\t-h, --help                   - display this help and exit\n");
}

void parse_arguments(int argc, char *argv[], client_config_t *config) {
    static struct option long_options[] = {
        {"host",      required_argument, 0, 's'},
        {"port",      required_argument, 0, 'p'},
        {"unix",      no_argument,       0, 'u'},
        {"receive",   no_argument,       0, 'r'},
        {"conn",      no_argument,       0, 'n'},
        {"directory", required_argument, 0, 'd'},
        {"program",   required_argument, 0, 'c'},
        {"args",      required_argument, 0, 'a'},
        {"help",      no_argument,       0, 'h'},
        {0, 0, 0, 0}
    };

    // Set defaults
    strcpy(config->host, "localhost");
    config->port = 8000;
    config->is_unix = false;
    config->receive = false;
    config->connections = 1;
    strcpy(config->directory, "ReceivedFiles");
    strcpy(config->program, "convert");
    strcpy(config->args, "–rotate 45");
    config->files = NULL;
    config->num_files = 0;

    int opt;
    int option_index = 0;

    while ((opt = getopt_long(argc, argv, "s:p:n:urd:c:a:h", long_options, &option_index)) != -1) {
        switch (opt) {
            case 's':
                strncpy(config->host, optarg, ARGS_BUFFER - 1);
                break;
            case 'p': {
                char *endptr;
                errno = 0;
                long port = strtol(optarg, &endptr, 10);

                // Check for conversion errors
                if (errno != 0 || *endptr != '\0' || endptr == optarg) {
                    log_message(ERROR, "Invalid port number");
                    log_close();
                    print_usage(argv[0]);
                    exit(EXIT_FAILURE);
                }

                // Check port range
                if (port < 0 || port > 65535) {
                    log_message(ERROR, "Port number must be between 0 and 65535");
                    log_close();
                    print_usage(argv[0]);
                    exit(EXIT_FAILURE);
                }

                config->port = (int)port;
                break;
            }
            case 'u':
                config->is_unix = true;
                break;
            case 'r':
                config->receive = true;
                break;
            case 'n':
                config->connections = atoi(optarg);
                break;
            case 'd':
                strncpy(config->directory, optarg, MAX_PATH - 1);
                break;
            case 'c':
                strncpy(config->program, optarg, ARGS_BUFFER - 1);
                break;
            case 'a':
                strncpy(config->args, optarg, ARGS_BUFFER - 1);
                break;
            case 'h':
                print_usage(argv[0]);
                exit(EXIT_SUCCESS);
            default:
                print_usage(argv[0]);
                exit(EXIT_FAILURE);
        }
    }

    // Get the remaining arguments (files)
    config->num_files = argc - optind;
    if (config->num_files > 0) {
        config->files = &argv[optind];
    } else {
        log_message(ERROR, "No input files specified");
        log_close();
        print_usage(argv[0]);
        exit(EXIT_FAILURE);
    }
}

void *client_accept(void  *arg) {
    client_config_t *config = arg;

    int socket_fd;

    if(config->is_unix) {
        const char *unix_endpoint = UNIX_ENDPOINT;
        socket_fd = un_client_socket_init(unix_endpoint);
        log_message(INFO ,"Connecting to UNIX socket...");
    } else {
        socket_fd = tcp_client_socket_init(config->host, config->port);
        log_message(INFO ,"Connecting to TCP socket...");
    }

    if (socket_fd != -1) {
        log_message(INFO ,"Connection established...");
    }


    for (int j = 0; j < config->num_files; j++) {
        log_message(INFO, "Sending file");

        // Open the file
        FILE* file = fopen(config->files[j], "rb");
        if (file == NULL) {
            log_message(ERROR, "Error opening file");
            continue;
        }

        // Get file size
        fseek(file, 0, SEEK_END);
        size_t file_size = (size_t)ftell(file);
        fseek(file, 0, SEEK_SET);

        // Prepare header
        char header[MAX_BUFFER + 1];
        char* filename = basename(config->files[j]);
        header_to_client(header, config->program, config->args, filename, file_size);

        // Print header info
        build_header(config->program, config->args, filename, file_size);

        // Send header
        if (write(socket_fd, header, strlen(header)) < 0) {
            log_message(ERROR, "Error sending header");
            fclose(file);
            continue;
        }

        // Read and send file content in blocks
        char buffer[FILE_SOCKET_BUFFER];
        size_t bytes_read;
        while ((bytes_read = fread(buffer, 1, FILE_SOCKET_BUFFER, file)) > 0) {
            if (write(socket_fd, buffer, bytes_read) < 0) {
                log_message(ERROR, "Error sending file content");
                fclose(file);
                break;
            }
        }
        log_message(INFO, "Read and sent file content in blocks successfully");

        fclose(file);

        // If receive mode is enabled, receive the processed file
        if (config->receive) {
            log_message(INFO, "Receiving file...");

            // Receive header
            char recv_header[MAX_BUFFER + 1];
            if (read_client_header(socket_fd, recv_header, MAX_BUFFER) < 0) {
                log_message(ERROR, "Error receiving response header");
                continue;
            }

            // Parse header to get file size
            char program[ARGS_BUFFER], args[ARGS_BUFFER], Filename[ARGS_BUFFER];
            long response_file_size;
            if (parse_client_header(recv_header, program, args, Filename, &response_file_size) < 0) {
                continue;
            }

            // Print received header info
            build_header(program, args, Filename, response_file_size);

            // Receive file content
            char* response_content = receive_file_content(socket_fd, response_file_size);
            if (response_content == NULL) {
                continue;
            }

            // Create output directory if it doesn't exist
            mkdir(config->directory, 0755);

            // Save received file
            char output_path[MAX_PATH];
            if (snprintf(output_path, MAX_PATH, "%s/%s", config->directory, Filename) >= MAX_PATH) {
                log_message(ERROR, "Output path too long");
                free(response_content);
                continue;
            }

            FILE* output_file = fopen(output_path, "wb");
            if (output_file == NULL) {
                log_message(ERROR, "Error Creating output file");
                free(response_content);
                continue;
            }

            fwrite(response_content, 1, response_file_size, output_file);
            fclose(output_file);
            free(response_content);
        }

        // Close socket and exit loop
        close(socket_fd);
        log_message(INFO, "Client close connection");
        break;
    }

    return NULL;
}

int main(int argc, char* argv[]) {
    client_config_t config;
    threadpool_t tp;
    int thInit = threadpool_init(&tp, 20, 4, 50, 80, 20);
    if(thInit != 0) {
        printf("Error initializing thread pool\n");
        return -1;
    }

    if (log_init("client.log") != 0) {
        handle_error("Error initializing log file");
        return 1;
    }

    // Parse command line arguments
    parse_arguments(argc, argv, &config);

    if (config.receive) {
        log_message(INFO ,"Receive Folder");
        struct stat st = {0};
        if (stat(config.directory, &st) == 0) {
            log_message(DEBUG, "Receive folder already exist");
        }
    }

    for (int i = 0; i < config.connections; i++) {
        threadpool_submit(&tp, client_accept, &config);
    }

    threadpool_destroy(&tp);
    log_close();
    return 0;
}
