/******************************************************
 * Copyright Grégory Mounié 2018                      *
 * This code is distributed under the GLPv3+ licence. *
 * Ce code est distribué sous la licence GPLv3+.      *
 ******************************************************/

#include <assert.h>
#include "mem.h"
#include "mem_internals.h"

void *
emalloc_small(unsigned long size)
{
    void* ptr;
    //arena.chunkpool
    if (arena.chunkpool==NULL)
    {
        unsigned long taille=mem_realloc_small();
        void ** curr=(void**)arena.chunkpool; //pointeur qui va aller sur la tete de liste
        void **next; //initialise un next (pointeur de pointeur)

        // Initialise la liste chaînée
        for (int i = 1; i < taille/96; i ++) {//on fait taille/96 bloc
            next = (void **)((char *)arena.chunkpool + 96 * i);//je crois que le cast en char* permet de bien avance d'octet en octet
            *curr=next;
            curr=next;
        }
        *curr=NULL;
    }
    unsigned long taille= 64 + 32;
    ptr = arena.chunkpool;
    arena.chunkpool=*(void**)arena.chunkpool;
    return mark_memarea_and_get_user_ptr(ptr, taille, SMALL_KIND);
}

void efree_small(Alloc a) {
    //chunk est un pointeur de pointeur vers la case à libérer
    void** chunk = (void**)a.ptr; //on dit que chunk = a.ptr

    *chunk = arena.chunkpool; 
    arena.chunkpool = chunk; 
}
