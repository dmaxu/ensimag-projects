#ifndef PROCESS_H
#define PROCESS_H

#include "stdint.h"
#include "cpu.h"
#include "queue.h"
#include "mem.h"


//#define NBPROC 100000
#define NBPROC 1000
#define MAXPRIO 256
#define TSS 0x20000
typedef enum
{
    activable = 0,
    elu = 1,
    non_activable = -1,
    zombie = 2,
    endormi = 3,
    to_delete = 4,
    bloque_file = 5,
    waiting_child = 6,
    bloque_clavier=7
} etat_processus;

extern void ctx_sw(int *ancien, int *nouveau);
extern void ctx_sw_user(int *ancien, int *nouveau,int * eip_user,int *esp_user);

extern int32_t getretval();

typedef struct process
{
    int pid;
    char nom[128];
    etat_processus etat;
    int tab_registres[5];
    int *pile_exec_process;
    struct process *suiv;
    int prio;
    uint32_t temps_reveil;
    int32_t retval; // valeur de retour du process
    struct process *parent;
    link lien;
    struct list_link enfants;
    link linkEnfant;
    link linkFileMessage;
    int messageReçu;
    int messageEnvoye;
    int etat_bloque;
    int ssize;
    int exit_quit;
    int reseted_message;
    
    // New fields for memory protection
    unsigned int cr3;             // Physical address of page directory
    int user_mode;                // Flag to indicate if running in user mode
    unsigned long user_stack;     // Virtual address of user stack
} process;

extern process *actif;

extern process* actif;
extern struct list_link list_process_activable;
extern struct list_link list_process_zombie;
extern struct list_link list_process_endormi;

extern process *processus_courant;
//extern int exit_quit;


int idle(void*);

int proc1(void*);
void init_process_list();
void init_list_process_zombie();
void init_list_process_endormi();

void ordonnanceur();
void fin_processus(void);

unsigned long current_clock();

void wait_clock(unsigned long clock);

//  PRIMTIVES

/**
 * crée un processus dans l'état activable.
 */
int start(int (*pt_func)(void *), unsigned long ssize, int prio, const char *name, void *arg);

/**
 * attend la terminaison d'un processus fils et récupère sa valeur de retour
 */
int waitpid(int pid, int *retvalp);

/**
 * récupére le pid du processus actif.
 */
int getpid(void);

/**
 * La primitive kill termine le processus identifié par la valeur pid. Si ce processus était bloqué dans une file d'attente pour 
 * un quelconque élément système, alors il en est retiré.
 * Si la valeur de pid est invalide, la valeur de retour est strictement négative. En cas de succès, la valeur de retour est nulle.
*/
int kill(int pid);

/**
 * Si la valeur de pid est invalide, la valeur de retour est strictement négative,
 *  sinon elle est égale à la priorité du processus identifié par la valeur pid.
 */
int getprio(int pid);

/**
 * La primitive chprio donne la priorité newprio au processus identifié par la valeur de pid.
 * Si la priorité du processus change et qu'il était en attente dans une file, il doit y être replacé selon sa nouvelle priorité.
 * Si la valeur de newprio ou de pid est invalide, la valeur de retour de chprio est strictement négative,
 * sinon elle est égale à l'ancienne priorité du processus pid.
 */
int chprio(int pid, int newprio);


int start_user(const char *name, unsigned long ssize, int prio, void *arg);

void exit(int retval);

#endif // PROCESS_H