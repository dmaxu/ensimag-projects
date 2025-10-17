/******************************************************
 * Copyright Grégory Mounié 2018-2022                 *
 * This code is distributed under the GLPv3+ licence. *
 * Ce code est distribué sous la licence GPLv3+.      *
 ******************************************************/

#include <sys/mman.h>
#include <assert.h>
#include <stdint.h>
#include "mem.h"
#include "mem_internals.h"

unsigned long knuth_mmix_one_round(unsigned long in)
{
    return in * 6364136223846793005UL % 1442695040888963407UL;
}

void *mark_memarea_and_get_user_ptr(void *ptr, unsigned long size, MemKind k)
{
    *(unsigned long *)ptr = size;
    unsigned long magic = knuth_mmix_one_round((unsigned long )ptr);
    magic = magic & ~0b11; 
    magic |= k;
    *(unsigned long *)(ptr+8*sizeof(char)) = magic;
    *(unsigned long *)(ptr+(size-16)*sizeof(char)) = magic;
    *(unsigned long *)(ptr+(size-8)*sizeof(char)) = size;
    return (void *) (ptr+16*sizeof(char));
}

Alloc mark_check_and_get_alloc(void *ptr)
{   
    unsigned long size = *(unsigned long *)(ptr-16*sizeof(char));
    unsigned long magic = *(unsigned long *)(ptr-8*sizeof(char));
    unsigned long correct_magic = knuth_mmix_one_round((unsigned long )(ptr-16*sizeof(char)));
    assert(correct_magic>>2==magic>>2);
    unsigned long end_magic = *(unsigned long *)(ptr+(size-32)*sizeof(char));
    unsigned long end_size = *(unsigned long *)(ptr+(size-24)*sizeof(char));
    assert(size==end_size);
    assert(magic==end_magic);
    Alloc a = {};
    a.ptr = ptr - 16*sizeof(char);
    a.size = size;
    a.kind = (magic&0b11);
    return a;
}


unsigned long
mem_realloc_small() {
    assert(arena.chunkpool == 0);
    unsigned long size = (FIRST_ALLOC_SMALL << arena.small_next_exponant);
    arena.chunkpool = mmap(0,
			   size,
			   PROT_READ | PROT_WRITE | PROT_EXEC,
			   MAP_PRIVATE | MAP_ANONYMOUS,
			   -1,
			   0);
    if (arena.chunkpool == MAP_FAILED)
	handle_fatalError("small realloc");
    arena.small_next_exponant++;
    return size;
}

unsigned long
mem_realloc_medium() {
    uint32_t indice = FIRST_ALLOC_MEDIUM_EXPOSANT + arena.medium_next_exponant;
    assert(arena.TZL[indice] == 0);
    unsigned long size = (FIRST_ALLOC_MEDIUM << arena.medium_next_exponant);
    assert( size == (1UL << indice));
    arena.TZL[indice] = mmap(0,
			     size*2, // twice the size to allign
			     PROT_READ | PROT_WRITE | PROT_EXEC,
			     MAP_PRIVATE | MAP_ANONYMOUS,
			     -1,
			     0);
    if (arena.TZL[indice] == MAP_FAILED)
	handle_fatalError("medium realloc");
    // align allocation to a multiple of the size
    // for buddy algo
    arena.TZL[indice] += (size - (((intptr_t)arena.TZL[indice]) % size));
    arena.medium_next_exponant++;
    return size; // lie on allocation size, but never free
}


// used for test in buddy algo
unsigned int
nb_TZL_entries() {
    int nb = 0;
    
    for(int i=0; i < TZL_SIZE; i++)
	if ( arena.TZL[i] )
	    nb ++;

    return nb;
}