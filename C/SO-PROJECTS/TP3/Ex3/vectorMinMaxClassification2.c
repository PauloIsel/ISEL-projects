#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <limits.h>
#include <time.h>
#include <math.h>

typedef struct {
    pthread_mutex_t mutex;    // mutex para proteger dados compartilhados
    pthread_cond_t cond;       // Condição para barreira
    int nThreads;              // número total de threads
    int count;                 // contador de threads que chegaram à barreira
    int generation;  // nova variável para controlar "ciclos" da barreira
} sot_barrier_t;

typedef struct {
    int *v;                 // vetor original
    float *v_norm;          // vetor normalizado
    size_t v_sz;            // tamanho do vetor

    int min;                // mínimo global
    int max;                // máximo global
    double average;         // média dos valores normalizados

    sot_barrier_t barrier;     // barreira para sincronização

    double global_sum;        // soma dos valores normalizados para calcular média
} vector_shared_t;

typedef struct {
    vector_shared_t *shared;  // dados compartilhados
    int thread_id;            // id da thread
    size_t start;             // índice inicial da partição
    size_t end;               // índice final da partição
} thread_arg_t;

int sot_barrier_init (sot_barrier_t *barrier, int nThreads) {
    barrier->nThreads = nThreads;
    barrier->count = 0;
    pthread_mutex_init(&barrier->mutex, NULL);
    pthread_cond_init(&barrier->cond, NULL);
    return 0;
}

int sot_barrier_destroy (sot_barrier_t *barrier) {
    pthread_mutex_destroy(&barrier->mutex);
    pthread_cond_destroy(&barrier->cond);
    return 0;
}


int sot_barrier_wait(sot_barrier_t *barrier) {
    pthread_mutex_lock(&barrier->mutex);
    int my_generation = barrier->generation;

    barrier->count++;
    if (barrier->count == barrier->nThreads) {
        barrier->count = 0;
        barrier->generation++; // próxima rodada
        pthread_cond_broadcast(&barrier->cond);
    } else {
        while (my_generation == barrier->generation) {
            pthread_cond_wait(&barrier->cond, &barrier->mutex);
        }
    }

    pthread_mutex_unlock(&barrier->mutex);
    return 0;
}

void init_vector_shared(vector_shared_t *shared, int *v, size_t v_sz, int nThreads) {
    shared->v = v;
    shared->v_sz = v_sz;
    shared->v_norm = malloc(v_sz * sizeof(float));
    shared->min = INT_MAX;
    shared->max = INT_MIN;
    shared->average = 0.0;
    shared->global_sum = 0.0;
}

void init_thread(thread_arg_t *arg, vector_shared_t *shared, int thread_id, size_t start, size_t end) {
    arg->shared = shared;
    arg->thread_id = thread_id;
    arg->start = start;
    arg->end = end;
}

void destroy_vector_shared(vector_shared_t *shared) {
    free(shared->v_norm);
    sot_barrier_destroy(&shared->barrier);
}

void min_max(thread_arg_t *arg) {
    vector_shared_t *shared = arg->shared;
    int local_min = INT_MAX;
    int local_max = INT_MIN;

    for (size_t i = arg->start; i < arg->end; ++i) {
        if (shared->v[i] < local_min) local_min = shared->v[i];
        if (shared->v[i] > local_max) local_max = shared->v[i];
    }

    pthread_mutex_lock(&shared->barrier.mutex);
    if (local_min < shared->min) shared->min = local_min;
    if (local_max > shared->max) shared->max = local_max;
    pthread_mutex_unlock(&shared->barrier.mutex);
}

double normalize_and_sum(thread_arg_t *arg) {
    vector_shared_t *shared = arg->shared;
    int min = shared->min;
    int max = shared->max;
    double local_sum = 0.0;

    for (size_t i = arg->start; i < arg->end; ++i) {
        if (max == min) {
            shared->v_norm[i] = 0.0f;
        } else {
            shared->v_norm[i] = (shared->v[i] - min) * 100.0f / (max - min);
        }
        local_sum += shared->v_norm[i];
    }
    return local_sum;
}

void classify(thread_arg_t *arg) {
    vector_shared_t *shared = arg->shared;
    double avg = shared->average;

    for (size_t i = arg->start; i < arg->end; ++i) {
        shared->v[i] = (shared->v_norm[i] >= avg) ? 1 : 0;
    }
}

// Função executada por cada thread
void *thread_func(void *argp) {
    thread_arg_t *arg = (thread_arg_t *)argp;
    vector_shared_t *shared = arg->shared;

    min_max(arg);           //Fase1
    sot_barrier_wait(&shared->barrier); // Sincronização após Fase1

    double local_sum = normalize_and_sum(arg);  //Fase2

    // Atualizar soma global
    pthread_mutex_lock(&shared->barrier.mutex);
    shared->global_sum += local_sum;
    pthread_mutex_unlock(&shared->barrier.mutex);

    sot_barrier_wait(&shared->barrier); // Sincronização após Fase2

    if (arg->thread_id == 0) {
        shared->average = shared->global_sum / shared->v_sz;
    }

    sot_barrier_wait(&shared->barrier); // Sincronização após Soma global
    classify(arg);              //Fase3
    return NULL;
}

void divide_vector_for_threads(thread_arg_t *args, vector_shared_t *shared, pthread_t *threads, size_t v_sz, int nThreads) {
    size_t chunk_size = v_sz / nThreads;
    size_t remainder = v_sz % nThreads;
    size_t start = 0;
    for (int i = 0; i < nThreads; i++) {
        size_t end = start + chunk_size + (i < remainder ? 1 : 0);
        init_thread(&args[i], shared, i, start, end);
        pthread_create(&threads[i], NULL, thread_func, &args[i]);
        start = end;
    }
}

void verificar_resultados(int *v_original, int *v_classificado, size_t v_sz, int min_p, int max_p, double avg_p) {
    int min = v_original[0], max = v_original[0];
    double soma = 0.0;
    float *v_norm = malloc(v_sz * sizeof(float));

    for (size_t i = 0; i < v_sz; i++) {
        if (v_original[i] < min) min = v_original[i];
        if (v_original[i] > max) max = v_original[i];
    }

    for (size_t i = 0; i < v_sz; i++) {
        v_norm[i] = (max == min) ? 0.0f : (v_original[i] - min) * 100.0f / (max - min);
        soma += v_norm[i];
    }
    double media = soma / v_sz;

    printf("Min %s: %d\n", (min == min_p) ? "correto" : "diferente", min);
    printf("Max %s: %d\n", (max == max_p) ? "correto" : "diferente", max);
    printf("Média %s: %.6f\n", (fabs(media - avg_p) <= 1e-6) ? "correta" : "diferente", media);

    int ok = 1;
    for (size_t i = 0; i < v_sz; i++) {
        int esperado = (v_norm[i] >= media) ? 1 : 0;
        if (v_classificado[i] != esperado) {
            ok = 0;
            printf("Classificação diferente no índice %zu! Seq: %d, Par: %d\n", i, esperado, v_classificado[i]);
            break;
        }
    }
    if (ok) printf("Classificação está correta!\n");

    free(v_norm);
}

void norm_min_max_and_classify_parallel(int v[], size_t v_sz, int nThreads) {
    pthread_t *threads = malloc(nThreads * sizeof(pthread_t));
    thread_arg_t *args = malloc(nThreads * sizeof(thread_arg_t));

    vector_shared_t shared;
    init_vector_shared(&shared, v, v_sz, nThreads);
    sot_barrier_init(&shared.barrier, nThreads);

    // Cópia do vetor original para verificação
    int *v_copia = malloc(v_sz * sizeof(int));
    for (size_t i = 0; i < v_sz; i++) v_copia[i] = v[i];

    // Dividir o vetor entre as threads
    divide_vector_for_threads(args, &shared, threads, v_sz, nThreads);

    for (int i = 0; i < nThreads; i++) {
        pthread_join(threads[i], NULL);
    }

    verificar_resultados(v_copia, v, v_sz, shared.min, shared.max, shared.average);

    free(v_copia);
    destroy_vector_shared(&shared);
    free(threads);
    free(args);
}

int main() {
    srand((unsigned int)(time(NULL) ^ getpid()));
    int v_sz = 10 + rand() % 99999; // Tamanho do vetor entre 10 e 100000
    int *v = malloc(v_sz * sizeof(int));
    for (int i = 0; i < v_sz; i++) {
        v[i] = rand() % 100; // Valores entre 0 e 999
    }

    int nThreads = 100;

    norm_min_max_and_classify_parallel(v, v_sz, nThreads);
    free(v);
    return 0;
}

