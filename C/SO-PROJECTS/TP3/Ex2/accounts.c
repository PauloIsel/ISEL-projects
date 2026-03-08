#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <time.h>
#include <unistd.h>

typedef struct {
    int id;
    double balance;
    pthread_mutex_t mutex;
} account_t;

typedef struct {
    account_t *accounts;
    int n_accounts;
    int num_transfers;
    unsigned int seed;
} thread_arg_t;

void transfer(account_t *a, account_t *b, double amount) {
    //Bloquear as threads para evitar deadlock
    account_t *first = a->id < b->id ? a : b;
    account_t *second = a->id < b->id ? b : a;

    pthread_mutex_lock(&first->mutex);
    pthread_mutex_lock(&second->mutex);

    if (a->balance >= amount) {
        a->balance -= amount;
        b->balance += amount;

        // printf("Transferência de %.2f€ da conta %d para a conta %d | Novo saldo: [de %d: %.2f€, para %d: %.2f€]\n",
        //        amount, a->id, b->id, a->id, a->balance, b->id, b->balance);

    // } else {

        // printf("Transferência FALHOU (saldo insuficiente): %.2f€ da conta %d para a conta %d (saldo atual: %.2f€)\n",
        //        amount, a->id, b->id, a->balance);

    }
    pthread_mutex_unlock(&second->mutex);
    pthread_mutex_unlock(&first->mutex);
}

void *thread_func(void *arg) {
    thread_arg_t *targ = (thread_arg_t *)arg;

    for (int i = 0; i < targ->num_transfers; ++i) {
        int from = rand_r(&targ->seed) % targ->n_accounts;
        int to = rand_r(&targ->seed) % (targ->n_accounts - 1);
        if (to >= from) {
            to++;
        }

        double amount = 1 + rand_r(&targ->seed) % 100;
        transfer(&targ->accounts[from], &targ->accounts[to], amount);
    }
    return NULL;
}

void accounts_init(account_t *accounts, int n, double balance) {
    for (int i = 0; i < n; ++i) {
        accounts[i].id = i;
        accounts[i].balance = balance;
        pthread_mutex_init(&accounts[i].mutex, NULL);
    }
}

void threads_init(pthread_t *threads, thread_arg_t *args, account_t *accounts, int n_threads, int n_accounts, int num_transfers) {
    for (int i = 0; i < n_threads; ++i) {
        args[i].accounts = accounts;
        args[i].n_accounts = n_accounts;
        args[i].num_transfers = num_transfers;
        args[i].seed = (unsigned int)time(NULL) + i; //Garantir que cada thread tem a sua própria sequência
        pthread_create(&threads[i], NULL, thread_func, &args[i]);
    }
}

void join_threads(pthread_t *threads, int n_threads) {
    for (int i = 0; i < n_threads; ++i) {
        pthread_join(threads[i], NULL);
    }
}

// Exemplo de função de teste
void run_test(int n_accounts, double balance, int n_threads, int n_transfers) {
    account_t accounts[n_accounts];
    pthread_t threads[n_threads];
    thread_arg_t args[n_threads];

    accounts_init(accounts, n_accounts, balance);

    threads_init(threads, args, accounts, n_threads, n_accounts, n_transfers);
    join_threads(threads, n_threads);

    double total = 0;
    for (int i = 0; i < n_accounts; ++i) {
        total += accounts[i].balance;
        pthread_mutex_destroy(&accounts[i].mutex);
    }
    printf("Teste: contas=%d, saldo=%.2f, threads=%d, transfers=%d | Total=%.2f (esperado: %.2f)\n",
           n_accounts, balance, n_threads, n_transfers, total, n_accounts * balance);
}

int main() {
    srand((unsigned int) time(NULL) + getpid());
    for (int i = 0; i < 5; ++i) {
        int n_accounts = 5 + rand() % 16; // 5 a 20 contas
        double balance = 500.0 + rand() % 4501; // 500 a 5000 saldo
        int n_threads = 2 + rand() % 9; // 2 a 10 threads
        int n_transfers = 100 + rand() % 9901; // 100 a 10000 transferências

        run_test(n_accounts, balance, n_threads, n_transfers);
    }
    return 0;
}
