#include "physmem.h"
#include <stddef.h>
#include <stdint.h>
#include "mem.h"






// Liste chaînée des pages libres
static link free_list_head;

// Tableau de structures représentant chaque page physique gérée
static phys_page *all_pages = NULL;
// Nombre total de pages gérées
static int total_pages = 0;
// Adresse physique de base de la zone de pages gérées
static void *base_addr = NULL;

// Fin du tas du noyau (définie dans le linker script)
extern char mem_heap_end[]; 



// Initialise l'allocateur de pages physiques
void physmem_init() {
    // On commence à gérer les pages juste après la fin du tas du noyau
    base_addr = (void*)mem_heap_end; 
    total_pages = MAX_PHYS_PAGES;
    // Allocation du tableau de structures de gestion (dans le tas du noyau)
    all_pages = (phys_page *)mem_alloc(sizeof(phys_page) * MAX_PHYS_PAGES);
    INIT_LIST_HEAD(&free_list_head);

    // Ajoute toutes les pages à la liste des pages libres
    for (int i = 0; i < MAX_PHYS_PAGES; ++i) {
        queue_add(&all_pages[i], &free_list_head, phys_page, lfree, lfree.next);
    }
}




// Alloue une page physique libre et retourne son adresse
void *physmem_alloc_page() {
    if (queue_empty(&free_list_head))
        return NULL; // Plus de pages libres
    phys_page *p = queue_out(&free_list_head, phys_page, lfree);
    int idx = p - all_pages;
    // Calcule l'adresse physique de la page allouée
    return (void *)((char *)base_addr + idx * PAGE_SIZE);
}




// Libère une page physique (la remet dans la free-list)
void physmem_free_page(void *page) {
    int idx = ((char *)page - (char *)base_addr) / PAGE_SIZE;
    queue_add(&all_pages[idx], &free_list_head, phys_page, lfree, lfree.next);
}




// Map une page virtuelle sur une page physique dans le page directory donné
int map_page(uint32_t *pgdir, unsigned long vaddr, unsigned long paddr, uint32_t perm) {
    size_t pd_idx = (vaddr >> 22) & 0x3FF;
    size_t pt_idx = (vaddr >> 12) & 0x3FF;

    // Aller chercher la page table, ou l’allouer si besoin
    if (!(pgdir[pd_idx] & PAGE_PRESENT)) {
        // Allouer une nouvelle page table physique
        uint32_t *new_pt = (uint32_t *)physmem_alloc_page();
        if (!new_pt)
            return -1;
        // Initialiser à zéro
        for (int i = 0; i < PAGE_ENTRIES; ++i)
            new_pt[i] = 0;
        // Insérer dans le page directory
        pgdir[pd_idx] = ((unsigned long)new_pt & 0xFFFFF000) | PAGE_PRESENT | PAGE_RW | PAGE_USER;
    }

    // Récupérer la page table
    uint32_t *pt = (uint32_t *)(pgdir[pd_idx] & 0xFFFFF000);

    // Mapper la page
    pt[pt_idx] = (paddr & 0xFFFFF000) | (perm & 0xFFF);

    return 0;
}