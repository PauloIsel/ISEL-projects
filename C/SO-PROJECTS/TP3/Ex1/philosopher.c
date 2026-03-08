#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
#include <semaphore.h>

#define Philosophers 5

pthread_mutex_t mutex;
sem_t sem[Philosophers];
int active = 1;

enum { THINKING, HUNGRY, EATING } state[Philosophers];

char *get_state(const int i) {
	switch (state[i]) {
		case THINKING: return "THINKING";
		case HUNGRY: return "HUNGRY";
		case EATING: return "EATING";
	}
	return "";
}

void check_state(int i) {
	if (state[i] == HUNGRY) {
		if(state[(i + 4) % Philosophers] != EATING &&
		state[(i + 1) % Philosophers] != EATING) {
			state[i] = EATING;
			sem_post(&sem[i]);
		}
	}
}

void take_forks(int i) {
    pthread_mutex_lock(&mutex);
    state[i] = HUNGRY;
    check_state(i);
    pthread_mutex_unlock(&mutex);
	sem_wait(&sem[i]);
}

void put_forks(int i) {
    pthread_mutex_lock(&mutex);
    state[i] = THINKING;
    check_state((i + 4) % Philosophers);
    check_state((i + 1) % Philosophers);
    pthread_mutex_unlock(&mutex);
}

void* Phil(void* number) {
    int i = *(int*) number;
    while(active) {
        printf("Philosopher %d is %s\n", i, get_state(i));
        sleep(1);

        take_forks(i);

        printf("Philosopher %d is %s\n", i, get_state(i));
        sleep(2);

        put_forks(i);
    }
	return NULL;
}

int main() {
	pthread_t philosophers[Philosophers];
	int phil_index[Philosophers];

	pthread_mutex_init(&mutex, NULL);
	for (int i = 0; i < Philosophers; i++) {
		state[i] = THINKING;
		phil_index[i] = i;
		sem_init(&sem[i], 0, 0);
		pthread_create(&philosophers[i], NULL, Phil, &phil_index[i]);
	}
	sleep(30);
	active = 0;

	for (int i = 0; i < Philosophers; i++) {
		pthread_join(philosophers[i], NULL);
	}
}
