#include "process.h"
#include "syscall_main.h"
#include "ecran.h"
#include "time.h"
#include "page.h"




int syscall_main(int syscall_number, int arg1, int arg2, int arg3, int arg4, int arg5) {
    
    arg5+= 1;
    switch (syscall_number) {
        case 0: 
            return getpid();
        case 1:
            console_putbytes((const char *)arg1 , arg2);
            return 0;
        case 2:
            return start_user((const char*)arg1,(unsigned long)arg2,arg3,(void*)arg4);
        case 3:
            return waitpid(arg1,(int*)arg2);
        case 4:
            return kill(arg1);
        case 5:
            exit(arg1);
        case 6:
            return chprio(arg1,arg2);
        case 7:
            wait_clock((unsigned long)arg1);
            return 0;
        case 8:
            clock_settings((unsigned long*)arg1 ,(unsigned long*)arg2 );
            return 0;
        case 9:
            return (int) current_clock();
        case 10:
            return (int) shm_create((const char*)arg1);
        case 11:
            return (int) shm_acquire((const char*)arg1);
        case 12:
            shm_release((const char*)arg1);
            return 0;
        case 13:
            fin_processus();
            return 0;    

        default:
            printf(" problem dans syscall main , numero indefini : %d\n", syscall_number);
            return -1;
    }
}
