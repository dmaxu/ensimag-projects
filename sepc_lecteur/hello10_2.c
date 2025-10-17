#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>


typedef struct{
    pthread_cond_t c;
    pthread_mutex_t m;
    int count;
}Data;


void init_data(Data* don){
    pthread_mutex_init(&don->m,NULL);
    pthread_cond_init(&don->c,NULL);
    don->count=0;

}


void* print_hello(void* arg) {
    Data* args=(Data*)arg;
    pthread_mutex_lock(&args->m);
    printf("Hello world !\n");
    args->count++;
    if (args->count == 10) {
        pthread_cond_signal(&args->c);
    }
    pthread_mutex_unlock(&args->m);
    return NULL;
}

void* print_done(void* arg){
    Data* args=(Data*)arg;
    pthread_mutex_lock(&args->m);
    while (args->count < 10) {
        pthread_cond_wait(&args->c, &args->m);
    }
    printf("Done !\n");
    pthread_mutex_unlock(&args->m);
    return NULL;
}

int main() {
    Data* donnee=(Data*) malloc(sizeof(Data));
    init_data(donnee);
    pthread_t threads[11];
    int i;

    for (i = 0; i < 10; i++) {
        pthread_create(&threads[i], NULL, print_hello, donnee);
    }

    pthread_create(&threads[10], NULL, print_done, donnee);

    for (i = 0; i < 11; i++) {
        pthread_join(threads[i], NULL);
    }

    pthread_mutex_destroy(&donnee->m);
    pthread_cond_destroy(&donnee->c);
    free(donnee);
}