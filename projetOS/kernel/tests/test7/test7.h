#ifndef TEST7_H
#define TEST7_H

// Déclaration de toutes les fonctions utilisées dans test7.c
void *shm_create(const char *key);
void *shm_acquire(const char *key);
void shm_release(const char *key);

unsigned long current_clock(void);
void wait_clock(unsigned long wakeup);
void clock_settings(unsigned long *quartz, unsigned long *ticks);
void test_it(void);

#endif // TEST7_H
