#include "menu_thread.h"
#include <stdio.h>

void* menu_thread_function() {
    int option;
    char input_buffer[IN_BUFFER];

    while (server_running) {
        printf("\n--- Server Menu ---\n");
        printf("1. List Clients\n");
        printf("2. Exit\n");
        printf("Choose an option: ");

        if (fgets(input_buffer, sizeof(input_buffer), stdin) == NULL) {
            log_message(ERROR, "Error reading input");
            continue;
        }

        if (sscanf(input_buffer, "%d", &option) != 1) {
            printf("Invalid option. Input a number.\n");
            log_message(DEBUG, "Invalid option input");
            continue;
        }

        switch (option) {
            case 1:
                pthread_mutex_lock(&clients_mutex);
                printf("Connected clients:\n");
                printf("  - TCP: %d\n", tcp_clients);
                printf("  - UNIX: %d\n", unix_clients);
                printf("  - Total: %d\n", tcp_clients + unix_clients);
                char log_msg[LOG_BUFFER];
                snprintf(log_msg, sizeof(log_msg), "Client List: TCP=%d, UNIX=%d", tcp_clients, unix_clients);
                log_message(INFO, log_msg);
                pthread_mutex_unlock(&clients_mutex);
                break;

            case 2:
                log_message(INFO, "Server shutting down from menu...");
                pthread_mutex_lock(&clients_mutex);
                server_running = false;
                pthread_mutex_unlock(&clients_mutex);
                return NULL;

            default:
                printf("Invalid option. Try again.\n");
                log_message(DEBUG, "Invalid menu option");
        }
    }

    return NULL;
}