#include "shell.h"

#include "process.h"  
#include "string.h"
#include "stdio.h"     
#include "ecran.h"     
#include "clavier.h"   

#include "time.h"


static const char *help_txt =
    "\nCommands:\n"
    "  ps               – list processes\n"
    "  getpid              – print current pid\n"
    "  prio <pid>       – print priority\n"
    "  uptime           – show time since boot\n"
    "  kill <pid>       – kill process\n"
    "  clear            – clear screen\n"
    "  exit             – reboot\n";

void cmd_help(void) { cons_write(help_txt, strlen(help_txt)); }

void debut_line(void) {
    cons_write("\n", 1);
    cons_write("> ", 2); 
}

void read_line(char *buf, unsigned max)
{
    unsigned i = 0;
    for (;;) {
        int c = cons_read();

        if (c == '\n' || c == '\r') break;

        if (c == 8 || c == 127) {
            if (i > 0) {
                --i;
                buf[i] = '\0';        
            }
            continue;
        }

        if (c < 32 || c > 126) continue;    /* on ignore le reste */

        if (i < max - 1) buf[i++] = (char)c;
    }
    buf[i] = '\0';

    /* on retire les espaces de fin de ligne */
    while (i && buf[i-1] == ' ') buf[--i] = '\0';
}

/* ---------- commandes ------------------------------------------------ */

void cmd_ps(void)
{
    cons_write("\n", 1);
    cons_write("PID  PRIO  ETAT  NOM\n", 20);

    struct process *p1;
    struct process *p3;
    cons_write("\n", 1);
    char linee[250];                      /* assez grand  */
        int n12 = snprintf(linee, sizeof linee,
                         "%3d  %4d  %3d   %s\n",
                         processus_courant->pid, processus_courant->prio, processus_courant->etat, processus_courant->nom);
        cons_write(linee, (unsigned)n12);
    queue_for_each(p1, &list_process_activable, struct process, lien) {
        char line[250];                      /* assez grand */
        int n1 = snprintf(line, sizeof line,
                         "%3d  %4d  %3d   %s\n",
                         p1->pid, p1->prio, p1->etat, p1->nom);
        cons_write(line, (unsigned)n1);
    }
    
    
    struct process *p2;
    queue_for_each(p2, &list_process_zombie, struct process, lien) {
        char line2[250];                     
        int n2 = snprintf(line2, sizeof line2,
                         "%3d  %4d  %3d   %s\n",
                         p2->pid, p2->prio, p2->etat, p2->nom);
        cons_write(line2, (unsigned)n2);
    }
    queue_for_each(p3, &list_process_endormi, struct process, lien) {
        char line3[250];                    
        int n3 = snprintf(line3, sizeof line3,
                         "%3d  %4d  %3d   %s\n",
                         p3->pid, p3->prio, p3->etat, p3->nom);
        cons_write(line3, (unsigned)n3);
    }
}


void cmd_echo(const char *arg)
{
    if (!arg) {
        
        cons_write("\n", 1);
        const char *msg = "no arg here\n";
        cons_write(msg, 13);   
        return;
    }

    if (!strcmp(arg, "on"))  {cons_write("\n", 1); cons_echo(1); }
    else if (!strcmp(arg, "off")) {cons_write("\n", 1); cons_echo(0); }
    else {
        cons_write("\n", 1);
        const char *help = "echo on|off";
        cons_write(help, 12);
    }
}

void cmd_kill(const char *pidstr)
{
    if (!pidstr) {
        cons_write("\nusage : kill <pid>\n", 20);
        return;
    }


    int pid = 0;
    for (const char *p = pidstr; *p; ++p) {
        if (*p < '0' || *p > '9') {
            cons_write("\nkill : pid invalide\n", 23);
            return;
        }
        pid = pid * 10 + (*p - '0');
    }

    int ret = kill(pid);
    if (ret < 0) {
        cons_write("\nkill : échec ou pid inconnu\n", 30);
    } else {
        char buf[32];
        int n = snprintf(buf, sizeof buf, "\nprocessus %d tué\n", pid);
        cons_write(buf, n);
    }
}

void cmd_pid(void)
{
    char buf[32];
    int n = snprintf(buf, sizeof buf, "\npid = %d\n", getpid());
    cons_write(buf, n);
}

void cmd_getprio(const char *pidstr)
{
    if (!pidstr) {                
        cons_write("\nusage : prio <pid>\n", 20);
        return;
    }

    int pid = 0;
    for (const char *p = pidstr; *p; ++p) {
        if (*p < '0' || *p > '9') {
            cons_write("\nprio : pid invalide\n", 22);
            return;
        }
        pid = pid * 10 + (*p - '0');
    }

    int pr = getprio(pid);
    if (pr < 0) {
        cons_write("\nprio : pid inconnu\n", 22);
    } else {
        char buf[32];
        int n = snprintf(buf, sizeof buf, "\nprio(%d) = %d\n", pid, pr);
        cons_write(buf, n);
    }
}

void cmd_clear(void) { efface_ecran(); }

void cmd_uptime(void)
{
    unsigned long quartz, ticks;
    clock_settings(&quartz, &ticks);   
    unsigned long n_ticks = current_clock();


    double seconds = n_ticks * (double)ticks / quartz;

    char buf[64];
    unsigned long s = (unsigned long)seconds;
    unsigned h = s / 3600;
    unsigned m = (s % 3600) / 60;
    unsigned sec = s % 60;
    int n = snprintf(buf, sizeof buf,
                    "\nuptime : %02u:%02u:%02u (%lu ticks)\n",
                    h, m, sec, n_ticks);
    cons_write(buf, n);
}



int shell_proc(void *arg)
{
    (void)arg;
    cons_write("Mini-shell.\n", 13);
    char line[128];
    for (;;) {
    debut_line();
    read_line(line, sizeof line);
    if (!line[0]) continue;

    // on ignore les espaces en tête
    char *input = line;
    while (*input == ' ') ++input;

    // découpe la commande et ses arguments
    char *cmd  = strtok(input, " ");
    char *rest = strtok( (void*)(0), " ");

    if (!cmd) continue;

        if (!strcmp(cmd, "ps"))         cmd_ps();
        else if (!strcmp(cmd, "echo"))  cmd_echo(rest);
        else if (!strcmp(cmd, "kill"))   cmd_kill(rest);
        else if (!strcmp(cmd, "getpid"))   cmd_pid();
        else if (!strcmp(cmd, "clear")) cmd_clear();
        else if (!strcmp(cmd, "prio"))  cmd_getprio(rest);
        else if (!strcmp(cmd, "uptime"))    cmd_uptime();  
        else if (!strcmp(cmd,"help")) cmd_help();
        else if (!strcmp(cmd, "exit")) {  cons_write("\n", 1); exit(1);  }
        else                            {cons_write("\n", 1); cons_write("cmd inconnue", 13);}
    }

    return 0;
}
