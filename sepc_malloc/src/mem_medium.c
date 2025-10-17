/******************************************************
 * Copyright Grégory Mounié 2018                      *
 * This code is distributed under the GLPv3+ licence. *
 * Ce code est distribué sous la licence GPLv3+.      *
 ******************************************************/

#include <stdint.h>
#include <assert.h>
#include "mem.h"
#include "mem_internals.h"

unsigned int puiss2(unsigned long size) {
    unsigned int p=0;
    size = size -1; // allocation start in 0
    while(size) {  // get the largest bit
	p++;
	size >>= 1;
    }
    if (size > (1 << p))
	p++;
    return p;
}


void *emalloc_medium(unsigned long size) {
    assert(size < LARGEALLOC);
    assert(size > SMALLALLOC);
    size = size + 32;
    unsigned int tzl_size = puiss2(size);

    // Si un bloc existe dans la liste
    if (arena.TZL[tzl_size] != NULL) {
        void* ptr = arena.TZL[tzl_size];
        arena.TZL[tzl_size] = *(void**)ptr;
        return mark_memarea_and_get_user_ptr(ptr, 1 << tzl_size, 1);
    }

    // Cherche un bloc plus grand
    unsigned int larger_tzl_size = tzl_size;
    while (larger_tzl_size < TZL_SIZE && arena.TZL[larger_tzl_size] == NULL) {
        larger_tzl_size++;
    }

    // Si aucun bloc plus grand n'a été trouvé
    if (larger_tzl_size >= (FIRST_ALLOC_MEDIUM_EXPOSANT + arena.medium_next_exponant)) {
        mem_realloc_medium();
        return emalloc_medium(size - 32);
    }

    // Un bloc plus grand a été trouvé, on va le découper
    void* large_block = arena.TZL[larger_tzl_size];
    arena.TZL[larger_tzl_size] = *(void**)large_block;

    // Découper le bloc plus grand jusqu'à obtenir la bonne taille
    while (larger_tzl_size > tzl_size) {
        larger_tzl_size--;
        void* buddy = (void*)((unsigned long)large_block ^ (1 << larger_tzl_size));

        // Ajouter le buddy à la liste libre
        *(void**)buddy = arena.TZL[larger_tzl_size];
        arena.TZL[larger_tzl_size] = buddy;
    }

    return mark_memarea_and_get_user_ptr(large_block, 1 << tzl_size, 1);
}



void efree_medium(Alloc a) {
    //on recherche le buddy dans la liste
    unsigned int tzl_size = puiss2(a.size);
    void* bloc = a.ptr;
    void* buddy = (void*) ((unsigned long)bloc^(1<<tzl_size));
    void* current = arena.TZL[tzl_size];
    while (current!=NULL && current!=buddy){
        current = *(void**)current;
    }
    //si le buddy n'est pas dans la liste alors il est utilisé donc on ajoute le bloc
    if (current==NULL){
        *(void**)bloc = arena.TZL[tzl_size];
        arena.TZL[tzl_size] = bloc;
    }
    //sinon on fusionne les deux blocs
    else {
        if (arena.TZL[tzl_size]==buddy){
            arena.TZL[tzl_size] = *(void**)buddy;
        }
        else {
            void* current = arena.TZL[tzl_size];
            while (*(void**)current!=buddy){
                current = *(void**)current;
            }
            *(void**)current = *(void**)buddy;
        }
        //vérifier le bon ordre des blocs
        void* merged_bloc = (bloc < buddy) ? bloc : buddy;

        efree_medium((Alloc){merged_bloc,1,a.size*2});
    }
}


