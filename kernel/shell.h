#ifndef KERNEL_SHELL_H
#define KERNEL_SHELL_H

/* ça lance le mini‑shell */
int shell_proc(void *arg);

void cons_write(const char *str, unsigned long size);
int  cons_read(void);
void cons_echo(int on);
void reboot(void);

void cmd_pid(void);                      
void cmd_getprio(const char *arg);       
void cmd_help(void);                     
void cmd_clear(void);                   
void cmd_uptime(void);                   
void cmd_kill(const char *pidstr);    
void cmd_echo(const char *arg);          

#endif 
