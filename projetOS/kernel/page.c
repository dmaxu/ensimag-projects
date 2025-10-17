#include "string.h"
#include "mem.h"  
#include "process.h"
#include "page.h"

static PagePartage pages[MAX_SHARED_PAGES];

void *shm_create(const char *key) {
    if (!key){
        return NULL;
    }
    for (int i = 0; i < MAX_SHARED_PAGES; i++) {
        if (strcmp(pages[i].key, key) == 0){
            return NULL;
        }
    }
    for (int i = 0; i < MAX_SHARED_PAGES; i++) {
        if (pages[i].key[0] == 0) {
            void *page = mem_alloc(PAGE_SIZE);
            strncpy(pages[i].key, key, sizeof(pages[i].key)-1);
            pages[i].adresse = page;
            pages[i].count = 1;
            return page;
        }
    }
    return NULL;
}

void *shm_acquire(const char *key) {
    if (!key){
        return NULL;
    }
    for (int i = 0; i < MAX_SHARED_PAGES; i++) {
        if (strcmp(pages[i].key, key) == 0) {
            pages[i].count++;
            return pages[i].adresse;
        }
    }
    return NULL; 
}

void shm_release(const char *key) {
    if (!key){
        return;
    }
    for (int i = 0; i < MAX_SHARED_PAGES; i++) {
        if (strcmp(pages[i].key, key) == 0) {
            pages[i].count--;
            if (pages[i].count == 0) {
                mem_free(pages[i].adresse, PAGE_SIZE);
                pages[i].key[0] = 0;
                pages[i].adresse = NULL;
            }
            return;
        }
    }
}
