#define MAX_SHARED_PAGES 64
#define PAGE_SIZE 4096


typedef struct PagePartage {
    char key[32];
    void *adresse;           
    int count;         
} PagePartage;

void *shm_create(const char *key);
void *shm_acquire(const char *key);
void shm_release(const char *key);

