#include "thread-pool.h"
#include "log.h"
#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>


// Error handling functions
void handle_tp_error(void *obj, const char *msg) {
    if (obj == NULL) {
        perror(msg);
        exit(EXIT_FAILURE);
    }
}

// Auxiliary functions
void mem_Allocation(threadpool_t *tp, int newSize) {
    pthread_t* thread = realloc(tp->work_threads, (tp->workers_current + newSize) * sizeof(pthread_t));
    handle_tp_error(thread, "monitor_thread memory allocation failed");
    tp->work_threads = thread;
}

int threadCreate(threadpool_t *tp, void *func, pthread_t *thread) {
    int th = pthread_create(thread, NULL, func, tp);
    if (th != 0) {
        fprintf(stderr, "Failed to create thread");
        return -1;
    }
    return 0;
}

//Work Item functions
work_item_t* work_item_create(wi_function_t func, void *args) {
    work_item_t *wi = malloc(sizeof(work_item_t));
    handle_tp_error(wi, "work_item_create memory allocation failed");
    wi->func = func;
    wi-> args = args;
    return wi;
}

//SharedBuffer functions
void sharedBuffer_init(SharedBuffer *sb, int capacity) {
    if (sb == NULL) return;
    sb->buffer = malloc(capacity * sizeof(void *));
    handle_tp_error(sb->buffer, "sharedBuffer_init memory allocation failed");
    sb->iGet = 0;
    sb->iPut = 0;
    sb->nElem = 0;
    sb->maxCapacity = capacity;
    pthread_cond_init(&sb->cEsperaEspacoLivre, NULL);
    pthread_cond_init(&sb->cEsperaEspacoOcupado, NULL);
    pthread_mutex_init(&sb->mutex, NULL);
}

void sharedBuffer_destroy (SharedBuffer *sb) {
    if (sb == NULL) return;
    free(sb->buffer);
    pthread_cond_destroy(&sb->cEsperaEspacoLivre);
    pthread_cond_destroy(&sb->cEsperaEspacoOcupado);
    pthread_mutex_destroy(&sb->mutex);
}

void SharedBuffer_Put(SharedBuffer *sb, void *item) {
    pthread_mutex_lock(&sb->mutex);

    while (sb->nElem == sb->maxCapacity) {
        pthread_cond_wait(&sb->cEsperaEspacoLivre, &sb->mutex);
    }

    sb->buffer[sb->iPut] = item;
    sb->iPut = (sb->iPut + 1) % sb->maxCapacity;
    sb->nElem++;

    pthread_cond_signal(&sb->cEsperaEspacoOcupado);
    pthread_mutex_unlock(&sb->mutex);
}

work_item_t* SharedBuffer_Get(SharedBuffer *sb) {
    pthread_mutex_lock(&sb->mutex);

    while (sb->nElem == 0){
        pthread_cond_wait(&sb->cEsperaEspacoOcupado, &sb->mutex);
    }

    work_item_t *wi = sb->buffer[sb->iGet];
    sb->iGet = (sb->iGet+1) % sb->maxCapacity;
    sb->nElem--;

    pthread_cond_signal(&sb->cEsperaEspacoLivre);
    pthread_mutex_unlock(&sb->mutex);
    return wi;
}

int getCurrentCapacity(SharedBuffer *sb) {
    pthread_mutex_lock(&sb->mutex);
    int currentCapacity = sb->nElem * 100 / sb->maxCapacity;
    pthread_mutex_unlock(&sb->mutex);
    return currentCapacity;
}

//worker thread function
void* worker_thread (void *args) {
    threadpool_t *tp = args;

    while(1) {
        work_item_t *wit = SharedBuffer_Get(tp->sb);
        if (wit == NULL) {
            if (tp->active) {
                pthread_mutex_lock(&tp->finished_worker_mutex);
                for (int i = 0; i < tp->workers_current; i++) {
                    if (tp->work_threads[i] == pthread_self()) {
                        tp->finished_worker_idx = i;
                        pthread_cond_signal(&tp->finished_worker_cond);
                        break;
                    }
                }
                pthread_mutex_unlock(&tp->finished_worker_mutex);
            }
            break;
        }
        wit->func(wit->args);
        free(wit);
    }
    return NULL;
}

//monitor thread function
void* monitor_thread(void *args) {
    threadpool_t *tp = args;

    while (tp->active) {
        int currentCapacity = getCurrentCapacity(tp->sb);
        if(currentCapacity > tp->capacityIncreaseThreshold && tp->workers_current < tp->workers_max) {
            mem_Allocation(tp, 1);

            if (threadCreate(tp, worker_thread, &tp->work_threads[tp->workers_current]) != 0) {
                break;
            }
            tp->workers_current++;
            log_message(INFO, "Increased number of worker threads");
        } else if(currentCapacity < tp->capacityDecreaseThreshold && tp->workers_current > tp->workers_min) {
            SharedBuffer_Put(tp->sb, NULL);

            pthread_mutex_lock(&tp->finished_worker_mutex);
            while (tp->finished_worker_idx == -1) pthread_cond_wait(&tp->finished_worker_cond, &tp->finished_worker_mutex);
            pthread_mutex_unlock(&tp->finished_worker_mutex);

            pthread_join(tp->work_threads[tp->finished_worker_idx], NULL);
            tp->workers_current--;
            if (tp->finished_worker_idx != tp->workers_current) {
                tp->work_threads[tp->finished_worker_idx] = tp->work_threads[tp->workers_current];
            }
            pthread_mutex_lock(&tp->finished_worker_mutex);
            tp->finished_worker_idx = -1;
            pthread_mutex_unlock(&tp->finished_worker_mutex);
            mem_Allocation(tp, 0);
            log_message(INFO,"Decreased number of worker threads");
        }
    }
    return NULL;
}

//Thread-Pool functions
int threadpool_init(threadpool_t *tp, int queueDim, int nthreads_min, int nthreads_max, int cIncThreshold, int cDecThreshold) {
    tp->sb = malloc(sizeof(SharedBuffer));
    if (tp->sb == NULL) return -1;
    handle_tp_error(tp->sb, "threadpool_init memory allocation failed");
    sharedBuffer_init(tp->sb, queueDim);
    tp->work_threads = malloc(sizeof(pthread_t) * nthreads_min);
    if (tp->work_threads == NULL) return -1;
    handle_tp_error(tp->work_threads, "threadpool_init memory allocation failed");

    tp->active = 1;
    tp->workers_min = nthreads_min;
    tp->workers_max = nthreads_max;
    tp->workers_current = nthreads_min;
    tp->capacityIncreaseThreshold = cIncThreshold;
    tp->capacityDecreaseThreshold = cDecThreshold;
    tp->finished_worker_idx = -1;
    pthread_mutex_init(&tp->finished_worker_mutex, NULL);
    pthread_cond_init(&tp->finished_worker_cond, NULL);

    for (int i = 0; i < tp->workers_current; i++) {
        if (threadCreate(tp, worker_thread, &tp->work_threads[i]) != 0) {
            return -1;
        }
    }

    if (threadCreate(tp,monitor_thread, &tp->monitor_thread) != 0) {
        return -1;
    }

    return 0;
}

int threadpool_destroy(threadpool_t *tp) {
    tp->active = 0;
    pthread_join(tp->monitor_thread, NULL);
    pthread_cond_destroy(&tp->finished_worker_cond);

    threadpool_endqueue(tp);

    for(int i = 0; i < tp->workers_current; i++) {
        pthread_join(tp->work_threads[i], NULL);
    }

    if (tp->sb->nElem != 0) {
        printf("Warning: There are still %d items in the buffer\n", tp->sb->nElem);
    }

    sharedBuffer_destroy(tp->sb);
    free(tp->sb);
    tp->sb = NULL;

    free(tp->work_threads);
    tp->work_threads = NULL;

    return 0;
}

int threadpool_submit(threadpool_t *tp, wi_function_t func, void *args) {
    if(tp->sb == NULL) return -1;
    work_item_t *wit = work_item_create(func, args);
    SharedBuffer_Put(tp->sb, wit);
    return 0;
}

void threadpool_endqueue(threadpool_t *threadpool) {
    for (int i = 0; i < threadpool->workers_current; i++) {
        SharedBuffer_Put(threadpool->sb, NULL);
    }
}
