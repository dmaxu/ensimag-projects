#include "queue.h"
#include <stdint.h>



#define PAGE_SIZE 4096
#define MAX_PHYS_PAGES 64000
#define PAGE_ENTRIES 1024

// Permissions (flags)
#define PAGE_PRESENT 0x1
#define PAGE_RW      0x2
#define PAGE_USER    0x4

typedef struct phys_page {
    link lfree; // Pour lier dans la free-list
    // Tu peux ajouter d'autres champs si besoin
} phys_page;

void physmem_init();
void *physmem_alloc_page();
void physmem_free_page(void *page);
int map_page(uint32_t *pgdir, unsigned long vaddr, unsigned long paddr, uint32_t perm);