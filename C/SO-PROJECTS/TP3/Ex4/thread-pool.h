#ifndef __THREAD_POOL_H__
#define __THREAD_POOL_H__

#include <pthread.h>

void handle_error(void* obj, const char *msg);

typedef void* (*wi_function_t)(void *);

typedef struct{
    wi_function_t func;
    void* args;
} work_item_t;

work_item_t* work_item_create(wi_function_t func, void *args);

typedef struct{
    void** buffer;
    int iPut;
    int iGet;
    int nElem;
    int maxCapacity;
    pthread_cond_t cEsperaEspacoLivre;
    pthread_cond_t cEsperaEspacoOcupado;
    pthread_mutex_t mutex;
} SharedBuffer;

void sharedBuffer_init(SharedBuffer *sb, int capacity);
void sharedBuffer_destroy(SharedBuffer *sb);
void SharedBuffer_Put(SharedBuffer *sb, void *item);
work_item_t* SharedBuffer_Get(SharedBuffer *sb);


typedef struct {
    SharedBuffer* sb;
    pthread_t* work_threads;
    pthread_t monitor_thread;
    int workers_min;
    int workers_max;
    int workers_current;
    int finished_worker_idx;
    pthread_mutex_t finished_worker_mutex;
    pthread_cond_t finished_worker_cond;
    int capacityIncreaseThreshold;
    int capacityDecreaseThreshold;
    int active;
} threadpool_t;

int threadpool_init(threadpool_t *tp, int queueDim, int nthreads_min, int nthreads_max, int cIncThreshold, int cDecThreshold);
int threadpool_destroy(threadpool_t *tp);
int threadpool_submit(threadpool_t *tp, wi_function_t func, void *args);
void threadpool_endqueue(threadpool_t *threadpool);

#endif
