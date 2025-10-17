/*******************************************************************************
 * Test 0
 *
 * A simple test that probes a classic system call.
 ******************************************************************************/

#include <stdio.h>
#include "cpu.h"


int test0(void*)
{
        //(void)arg;
        register unsigned reg1 = 1u;
        register unsigned reg2 = 0xFFFFFFFFu;
        register unsigned reg3 = 0xBADB00B5u;
        register unsigned reg4 = 0xDEADBEEFu;


        printf("I'm a simple process running ...");
        sti();
        unsigned i;
        for (i = 0; i < 10000000; i++) {
             if (reg1 != 1u || reg2 != 0xFFFFFFFFu || reg3 != 0xBADB00B5u || reg4 != 0xDEADBEEFu) {
                printf(" and I feel bad. Bybye ...\n");
                assert(0);
             }
        }
        cli();
        printf(" and I'm healthy. Leaving.\n");

        return 1;
}
