#include <SDL2/SDL.h>
#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include "oggstream.h"
#include "stream_common.h"

int main(int argc, char *argv[]) {
  int res;
  pthread_t t1,t2;
  if (argc != 2) {
    fprintf(stderr, "Usage: %s FILE", argv[0]);
    exit(EXIT_FAILURE);
  }
  assert(argc == 2);

  // Initialisation de la SDL
  res = SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_EVENTS);
  atexit(SDL_Quit);
  assert(res == 0);

  // Your code HERE
  // start the two stream readers (theoraStreamReader and vorbisStreamReader)
  // each in a thread
  pthread_create(&t1,NULL,&theoraStreamReader,argv[1]);
  pthread_create(&t2,NULL,&vorbisStreamReader,argv[1]);



  // wait for vorbis thread
  pthread_join(t2,NULL);
  // 1 seconde of sound in advance, thus wait 1 seconde
  // before leaving
  sleep(1);
  pthread_cancel(t1);
  pthread_cancel(t_draw2SDL); //check si pas t2

  // Wait for theora and theora2sdl threads
  pthread_join(t1,NULL);//t_draw2SDL?
  pthread_join(t_draw2SDL,NULL);
  // TODO
  /* liberer des choses ? */

  exit(EXIT_SUCCESS);
}
