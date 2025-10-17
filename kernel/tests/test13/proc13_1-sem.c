/*
#include "sysapi.h"


int proc13_1(void *arg)
{
        int sem = (int)arg;
        assert(try_wait(sem) == 0);
        assert(try_wait(sem) == -3);
        printf("1");
        assert(wait(sem) == -4);
        printf(" 9");
        assert(wait(sem) == -3);
        printf(" 13");
        assert(wait(sem) == -1);

        exit(1);
}
*/
