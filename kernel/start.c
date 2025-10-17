#include "cpu.h"
#include "process.h"
#include "time.h"
#include "ecran.h"
#include "processTest.h"
#include "clavier.h"
#include "physmem.h"
#include "shell.h"

extern int test0(void*);
extern int test1(void *arg);
extern int test2(void *arg);
extern int test3(void *arg);
extern int test4(void *args);
extern int test5(void *arg);
extern int test6(void *arg);
extern int test7(void *arg);
extern int test8(void *arg);
extern int test9(void *arg);
extern int test10(void *arg);
extern int test12_msg(void *arg);
extern int test13_msg(void *arg);
extern int test14_msg(void *arg);
extern int test15_msg(void *arg);
extern int test16_msg(void *arg);
extern int test17_msg(void *arg);
extern int test18(void *arg);
extern int test19_crc(void *arg);
extern int test19_crl(void *arg);

extern int test22(void *arg);
extern int test21(void *arg);
//int procKill(void *args);
//extern int dummy1(void *arg);


int fact(int n)
{
	if (n < 2)
		return 1;

	return n * fact(n-1);
}

extern void traitant_IT_32(void);
extern void traitant_IT_33(void);

extern void traitant_syscall(void); 




void kernel_start(void)
{
	efface_ecran();

	// initialisation pour les interruptions horloges
	init_traitant_IT(32, traitant_IT_32);
	init_horloge();
	masque_IRQ(0, false);
	// initialisation pour les interruptions clavier
	init_traitant_IT(33, traitant_IT_33);
	masque_IRQ(1, false);
	physmem_init();

	// interruption pour syscall
	init_traitant_IT_user(0x49, traitant_syscall);  

	init_process_list();
	init_list_process_zombie();
	init_list_process_endormi();
	init_list_process_attente_clavier();
	//init_process_test();
	//start(test1, 512, 2, "test0", (void *)0);

	//init_process_test();

	//start(shell_proc, 4096, 128, "shell_", (void*)(0));
	//start_user("test_app",4000,128,0);

   	start(test0, 4000, 128, "test", (void*)(0));

	// Boucle infinie pour laisser tourner le kernel
	for (;;)
    {
        sti();
        hlt();
        cli();
    }
	return;
}


