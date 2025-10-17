#include "fct_user.h"
#include "syscall.h"



int getpid(){
    return __syscall0(GETPID);
}

int console_putbytes(const char *chaine, unsigned long len) {
    return __syscall2(CONS_PUTBYTES, (int)chaine,(int)len);
}


int waitpid(int pid, int *retvalp){
    return __syscall2(WAITPID,pid,(int)retvalp );
}

int kill(int pid){
    return __syscall1(KILL,pid);
}

int exit2(int retval){
    __syscall1(EXIT,retval);
    return 1;
}

int chprio(int pid, int newprio){
    return __syscall2(CHPRIO,pid,newprio);
}



void wait_clock(unsigned long clock){
    __syscall1(WAIT_CLOCK,(int)clock);
}

void clock_settings(unsigned long *quartz, unsigned long *ticks){
    __syscall2(CLOCK_SETTING,(int)quartz,(int)ticks);
}

unsigned long current_clock(){
    return  (unsigned long)__syscall0(CURRENT_CLOCK);
}

void *shm_create(const char *key){
    return (void*) __syscall1(SHM_CREATE,(int)key);
}

void *shm_acquire(const char *key){
    return (void*) __syscall1(SHM_ACQUIRE,(int)key);
}

void shm_release(const char *key){
    __syscall1(SHM_RELEASE,(int)key);
}

void fin_processus(){
    __syscall0(FIN_PROCESSUS);
}


int start_user(const char *name, unsigned long ssize, int prio, void *arg){
    return __syscall4(START,(int)name,(int)ssize,(int)prio,(int)arg);
}