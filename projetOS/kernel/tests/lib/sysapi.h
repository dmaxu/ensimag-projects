/*
 * Ensimag - Projet système
 * Copyright (C) 2012 - Damien Dejean <dam.dejean@gmail.com>
 *
 * Unique header of the standalone test standard library.
 */

#ifndef _SYSAPI_H_
#define _SYSAPI_H_

#define NULL ((void*)0)

#include <stdio.h>

/*******************************************************************************
 * Printf macros
 ******************************************************************************/
#define PRINTF_LEFT_JUSTIFY 1
#define PRINTF_SHOW_SIGN 2
#define PRINTF_SPACE_PLUS 4
#define PRINTF_ALTERNATE 8
#define PRINTF_PAD0 16
#define PRINTF_CAPITAL_X 32

#define PRINTF_BUF_LEN 512

/*******************************************************************************
 * Assert : check a condition or fail
 ******************************************************************************/
#define __STRING(x) #x


#define DUMMY_VAL 78

#define TSC_SHIFT 8

#define FREQ_PREC 50

#define NBSEMS 10000

#define TRUE 1
#define FALSE 0

#define NR_PHILO 5

#define WITH_MSG 0
#define CONS_READ_CHAR 0
// Prototype des appels systeme de la spec
int chprio(int pid, int newprio);
void cons_write(const char *str, unsigned long size);
#if defined CONS_READ_LINE
int cons_read(char *string, int length);
#elif defined CONS_READ_CHAR
int cons_read();
#endif
void cons_echo(int on);
void exit(int retval);
int getpid(void);
int getprio(int pid);
int kill(int pid);



/* Wrapper sur les verrous basés sur les sémaphores ou les files de messages */
union sem {
    int fid;
    int sem;
};
void xwait(union sem *s);
void xsignal(union sem *s);
void xsdelete(union sem *s);
void xscreate(union sem *s);


int start(int (*pt_func)(void*), unsigned long ssize, int prio, const char *name, void *arg);
//int start(const char *process_name, unsigned long ssize, int prio, void *arg);
int waitpid(int pid, int *retval);
void test_it(void);
#if defined WITH_SEM
/*
 * Pour la soutenance, devrait afficher la liste des processus actifs, des
 * semaphores utilises et toute autre info utile sur le noyau.
 */
#elif defined WITH_MSG
/*
 * Pour la soutenance, devrait afficher la liste des processus actifs, des
 * files de messages utilisees et toute autre info utile sur le noyau.
 */
int pcount(int fid, int *count);
int pcreate(int count);
int pdelete(int fid);
int preceive(int fid,int *message);
int preset(int fid);
int psend(int fid, int message);
#endif
void sys_info(void);

/* Available from our standard library */
#ifndef __SIZE_TYPE__
#error __SIZE_TYPE__ not defined
#endif

typedef __SIZE_TYPE__ size_t;

int strcmp(const char *str1, const char *str2);
size_t strlen(const char *s);
char *strncpy(char *dst, const char *src, unsigned n);
void *memset(void *dst, int c, size_t n);




#ifndef TELECOM_TST
unsigned long current_clock(void);
void wait_clock(unsigned long wakeup);
void clock_settings(unsigned long *quartz, unsigned long *ticks);
void *shm_create(const char *key);
void *shm_acquire(const char *key);
void shm_release(const char *key);
#endif


#define DIVISION_FOIREUSE 0

/* math.h */
typedef unsigned long long uint_fast64_t;
typedef unsigned long uint_fast32_t;
short randShort(void);
void setSeed(uint_fast64_t _s);
unsigned long rand();
unsigned long long div64(unsigned long long num, unsigned long long div, unsigned long long *rem);


#endif /* _SYSAPI_H_ */
