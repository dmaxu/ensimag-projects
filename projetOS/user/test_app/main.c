#include "fct_user.h"
#include <stdio.h>

int main(void) {
        register unsigned reg1 = 1u;
        register unsigned reg2 = 0xFFFFFFFFu;
        register unsigned reg3 = 0xBADB00B5u;
        register unsigned reg4 = 0xDEADBEEFu;

        printf("I'm a simple process running ...");

        unsigned i;
        for (i = 0; i < 10; i++) {
             if (reg1 != 1u || reg2 != 0xFFFFFFFFu || reg3 != 0xBADB00B5u || reg4 != 0xDEADBEEFu) {
                printf(" and I feel bad. Bybye ...\n");
             }
        }

        printf(" and I'm healthy. Leaving.\n");
        exit2(0); 
        return 0;
}
