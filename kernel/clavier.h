
#ifndef __CLAVIER_H__
#define __CLAVIER_H__


#include "stdint.h"

#define TAILLE_TAMPON_CLAVIER 100

extern  int echo_active; // pour savoir si l'echo est activé 1: Oui/ 0: Non
extern char tampon_clavier[TAILLE_TAMPON_CLAVIER];
extern struct list_link list_process_attente_clavier;

void reception_clavier(void);
void init_list_process_attente_clavier(void);

#endif