#include "stdint.h"
#include "cpu.h"
#include "string.h"
#include "processTest.h"
#include "queue.h"


void init_process_test()
{
    start(proc1, 512, 2, "proc1", (void *)1);
    //start(testC, 512, 2, "testC", NULL);
    //start(testD, 512, 2, "testD", NULL);
    //start(testE, 512, 2, "testE", NULL);

    // struct process *ptr_elem;
    // queue_for_each(ptr_elem, &list_process_activable, struct process, lien)
    // {
    //     printf("%s (Priority: %d)\n", ptr_elem->nom, ptr_elem->prio);
    // }
}

int idle_test(void *)
{

    for (;;)
    {
        testB(NULL);
    }
}
int proc1(void * param)
{
    printf("--> param : %i \n",(int)param);
    int pidtestA=start(testA, 512, 2, "testA", NULL);
    int ret;
    waitpid(pidtestA,&ret);
    printf("--> ret : %i \n",ret);
    return 0;
    // hlt();
}

int testA(void *)
{
    int a=2;
    return a-9;
}

int testB(void *)
{
    unsigned long i;
    while (1)
    {
        printf("B");
        __asm__ __volatile__("sti"); /* demasquage des interruptions */
        /* une ou plusieurs it du timer peuvent survenir pendant cette
           boucle d'attente */
        for (i = 0; i < 5000000; i++)
            ;
        __asm__ __volatile__("cli"); /* masquage des interruptions */
    }
}

int testC(void *)
{
    unsigned long i;
    while (1)
    {
        printf("C");
        __asm__ __volatile__("sti"); /* demasquage des interruptions */
        /* une ou plusieurs it du timer peuvent survenir pendant cette
           boucle d'attente */
        for (i = 0; i < 5000000; i++)
            ;
        __asm__ __volatile__("cli"); /* masquage des interruptions */
    }
}

int testD(void *)
{
    for (int32_t i = 0; i < 55; i++)
    {
        printf("D");
        __asm__ __volatile__("sti"); /* demasquage des interruptions */
        /* une ou plusieurs it du timer peuvent survenir pendant cette
           boucle d'attente */
        for (i = 0; i < 5000000; i++)
            ;
        __asm__ __volatile__("cli"); /* masquage des interruptions */
    }
    return 1;
}

int testE(void *)
{
    for (int32_t i = 0; i < 3; i++)
    {
        printf("E");
        __asm__ __volatile__("sti"); /* demasquage des interruptions */
        /* une ou plusieurs it du timer peuvent survenir pendant cette
           boucle d'attente */
        for (i = 0; i < 5000000; i++)
            ;
        __asm__ __volatile__("cli"); /* masquage des interruptions */

        wait_clock(5);
    }

    for (int32_t i = 0; i < 10; i++)
    {
        printf("E");
        __asm__ __volatile__("sti"); /* demasquage des interruptions */
        /* une ou plusieurs it du timer peuvent survenir pendant cette
            boucle d'attente */
        for (i = 0; i < 5000000; i++)
            ;
        __asm__ __volatile__("cli"); /* masquage des interruptions */
    }

    for (int32_t i = 0; i < 10; i++)
    {
        printf("E");
        __asm__ __volatile__("sti"); /* demasquage des interruptions */
        /* une ou plusieurs it du timer peuvent survenir pendant cette
            boucle d'attente */
        for (i = 0; i < 5000000; i++)
            ;
        __asm__ __volatile__("cli"); /* masquage des interruptions */

        wait_clock(10);
    }

    return 1;
}
