
#define GETPID 0
#define CONS_PUTBYTES 1
#define START 2
#define WAITPID 3
#define KILL 4
#define EXIT 5
#define CHPRIO 6
#define WAIT_CLOCK 7
#define CLOCK_SETTING 8
#define CURRENT_CLOCK 9
#define SHM_CREATE 10
#define SHM_ACQUIRE 11
#define SHM_RELEASE 12
#define FIN_PROCESSUS 13




int getpid();
int console_putbytes(const char *chaine, unsigned long len);
int start_user(const char *name, unsigned long ssize, int prio, void *arg);

int waitpid(int pid, int *retvalp);
int kill(int pid);
int exit2(int retval);
int chprio(int pid, int newprio);

void wait_clock(unsigned long clock);
void clock_settings(unsigned long *quartz, unsigned long *ticks);
unsigned long current_clock();

void *shm_create(const char *key);
void *shm_acquire(const char *key);
void shm_release(const char *key);


