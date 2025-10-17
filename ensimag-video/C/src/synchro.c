#include "ensitheora.h"
#include "synchro.h"
#include <pthread.h>
#include <stdbool.h>

/* les variables pour la synchro, ici */
pthread_cond_t tailleFen= PTHREAD_COND_INITIALIZER;
pthread_cond_t textFen = PTHREAD_COND_INITIALIZER;
pthread_cond_t vide = PTHREAD_COND_INITIALIZER;
pthread_cond_t plein = PTHREAD_COND_INITIALIZER;


pthread_mutex_t mTaille= PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t  mText = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t  consproM = PTHREAD_MUTEX_INITIALIZER;
bool prete=false;

int reservoir=0;


/* l'implantation des fonctions de synchro ici */
void envoiTailleFenetre(th_ycbcr_buffer buffer) {
    pthread_mutex_lock(&mTaille);
    windowsx=buffer[0].width;
    windowsy=buffer[0].height;
    pthread_cond_signal(&tailleFen);
    pthread_mutex_unlock(&mTaille);
}

void attendreTailleFenetre() {
    pthread_mutex_lock(&mTaille);
    while ((windowsx==0)&&(windowsy==0)){
        pthread_cond_wait(&tailleFen,&mTaille);
    }
    pthread_mutex_unlock(&mTaille);
}

void signalerFenetreEtTexturePrete() {
    pthread_mutex_lock(&mText);
    prete=true;
    pthread_cond_signal(&textFen);
    pthread_mutex_unlock(&mText);
}

void attendreFenetreTexture() {
    pthread_mutex_lock(&mText);
    while (!prete){
        pthread_cond_wait(&textFen,&mText);
    }
    pthread_mutex_unlock(&mText);
}

void debutConsommerTexture() {
    pthread_mutex_lock(&consproM);
    while(reservoir==0){
        pthread_cond_wait(&vide,&consproM);
    }
    
    pthread_mutex_unlock(&consproM);
}

void finConsommerTexture() {
    pthread_mutex_lock(&consproM);
    reservoir--;
    pthread_cond_broadcast(&plein);
    pthread_mutex_unlock(&consproM);
}

void debutDeposerTexture() {
    pthread_mutex_lock(&consproM);
    while(reservoir==NBTEX){
        pthread_cond_wait(&plein,&consproM);
    }
    
    pthread_mutex_unlock(&consproM);
}

void finDeposerTexture() {
    pthread_mutex_lock(&consproM);
    reservoir++;
    pthread_cond_broadcast(&vide);
    pthread_mutex_unlock(&consproM);
}
