#include <pthread.h>
#include <stdio.h>

pthread_cond_t c = PTHREAD_COND_INITIALIZER;
pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;
int count = 0;

void* print_hello() {
    pthread_mutex_lock(&m);
    printf("Hello world !\n");
    count++;
    if (count == 10) {
        pthread_cond_signal(&c);
    }
    pthread_mutex_unlock(&m);
    return NULL;
}

void* print_done(){
    pthread_mutex_lock(&m);
    while (count < 10) {
        pthread_cond_wait(&c, &m);
    }
    printf("Done !\n");
    pthread_mutex_unlock(&m);
    return NULL;
}

int main() {
    pthread_mutex_init(&m, NULL);
    pthread_cond_init(&c, NULL);
    pthread_t threads[11];
    int i;

    for (i = 0; i < 10; i++) {
        pthread_create(&threads[i], NULL, print_hello, NULL);
    }

    pthread_create(&threads[10], NULL, print_done, NULL);

    for (i = 0; i < 11; i++) {
        pthread_join(threads[i], NULL);
    }
    pthread_mutex_destroy(&m);
    pthread_cond_destroy(&c);
}