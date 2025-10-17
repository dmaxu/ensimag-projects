/*****************************************************
 * Copyright Grégory Mounié 2008-2015                *
 *           Simon Nieuviarts 2002-2009              *
 * This code is distributed under the GLPv3 licence. *
 * Ce code est distribué sous la licence GPLv3+.     *
 *****************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "variante.h"
#include "readcmd.h"
#include <fcntl.h>
#include <signal.h>
#include <sys/signal.h>
#include <sys/time.h>
#include <time.h>
#ifndef VARIANTE
#error "Variante non défini !!"
#endif

/* Guile (1.8 and 2.0) is auto-detected by cmake */
/* To disable Scheme interpreter (Guile support), comment the
 * following lines.  You may also have to comment related pkg-config
 * lines in CMakeLists.txt.
 */

#if USE_GUILE == 1
#include <libguile.h>

int question6_executer(char *line)
{
	/* Question 6: Insert your code to execute the command line
	 * identically to the standard execution scheme:
	 * parsecmd, then fork+execvp, for a single command.
	 * pipe and i/o redirection are not required.
	 */
	
	struct cmdline *l;
	l = parsecmd(&line);
	int pid = fork(); /*processus fils*/
		if (!pid)		  /*si processus fils*/
		{
			execvp(l->seq[0][0], l->seq[0]);
		}
		else
		{
			wait(pid);
		}

	return 0;

	return 0;
}

SCM executer_wrapper(SCM x)
{
	return scm_from_int(question6_executer(scm_to_locale_stringn(x, 0)));
}
#endif

void terminate(char *line)
{
#if USE_GNU_READLINE == 1
	/* rl_clear_history() does not exist yet in centOS 6 */
	clear_history();
#endif
	if (line)
		free(line);
	printf("exit\n");
	exit(0);
}

void affiche_process(L_c_process *tete)
{
	tete = tete->suiv;
	int idx = 1;

	while (tete != NULL)
	{
		int status = waitpid(tete->pid, NULL, WNOHANG);
		if (!status)
		{
			printf("[%d]\t\t", idx);
			printf("PID:%d\t", tete->pid);
			printf("%s", tete->nom_proc);
			printf("\n");
		}
		tete = tete->suiv;
		idx++;
	}
}
L_c_process *tete_process; //on a besoin de la mettre en globale pour sichld_handler puisse y acceder
//gestion de SIGCHLD
void sigchld_handler(int signal){
	struct timeval end_process;
	int status;
	int pid;

	while ((pid = waitpid(-1,&status,WNOHANG))>0){ //on boucle dessus SANS BLOQUER tant que le processus n'est pas fini
		gettimeofday(&end_process, NULL); //on prend recupere date de fin
		L_c_process* curr=tete_process;

		while (curr != NULL) { //on va chercher quel processus correspond à celui qui s'est terminé
            if (curr->pid == pid) {
                // Calculer la durée d'exécution
                long seconds = end_process.tv_sec - curr->start_time.tv_sec;
                long microseconds = end_process.tv_usec - curr->start_time.tv_usec;
                if (microseconds < 0) {
                    seconds--;
                    microseconds += 1000000;
                }

                // Afficher le temps d'exécution
                printf("Processus %d (%s) terminé. Temps de calcul : %ld.%06lds\n", pid, curr->nom_proc, seconds, microseconds);
				break;
            }
            curr = curr->suiv;  // Passer au suivant
        }
    }	

}

int main()
{
	//sigaction
	struct sigaction sign;
	bzero(&sign,sizeof(sign));
	sign.sa_handler=&sigchld_handler;
	sigaction(SIGCHLD,&sign,NULL);


	tete_process = malloc(sizeof(L_c_process));
	tete_process->status = 0;
	L_c_process *current = tete_process;

	printf("Variante %d: %s\n", VARIANTE, VARIANTE_STRING);

#if USE_GUILE == 1
	scm_init_guile();
	/* register "executer" function in scheme */
	scm_c_define_gsubr("executer", 1, 0, 0, executer_wrapper);
#endif

	while (1)
	{
		struct cmdline *l;
		char *line = 0;
		int i, j;
		char *prompt = "ensishell>";

		/* Readline use some internal memory structure that
		   can not be cleaned at the end of the program. Thus
		   one memory leak per command seems unavoidable yet */
		line = readline(prompt);
		
		char line_cpy[1000];
		strcpy(line_cpy, line);
		if (line == 0 || !strncmp(line, "exit", 4))
		{
			terminate(line);
		}

#if USE_GNU_READLINE == 1
		add_history(line);
#endif

#if USE_GUILE == 1
		/* The line is a scheme command */
		if (line[0] == '(')
		{
			char catchligne[strlen(line) + 256];
			sprintf(catchligne, "(catch #t (lambda () %s) (lambda (key . parameters) (display \"mauvaise expression/bug en scheme\n\")))", line);
			scm_eval_string(scm_from_locale_string(catchligne));
			free(line);
			continue;
		}
#endif

		/* parsecmd free line and set it up to 0 */
		l = parsecmd(&line);

		/* If input stream closed, normal termination */
		if (!l)
		{

			terminate(0);
		}

		if (l->err)
		{
			/* Syntax error, read another command */
			printf("error: %s\n", l->err);
			continue;
		}

		if (l->in)
			printf("in: %s\n", l->in);
		if (l->out)
			printf("out: %s\n", l->out);
		if (l->bg)
			printf("background (&)\n");

		/* Display each command of the pipe */
		for (i = 0; l->seq[i] != 0; i++)
		{
			char **cmd = l->seq[i];
			printf("seq[%d]: ", i);
			for (j = 0; cmd[j] != 0; j++)
			{
				printf("'%s' ", cmd[j]);
			}
			printf("\n");
		}

		/*question 4 */
		if (!strcmp(l->seq[0][0], "jobs"))
		{
			affiche_process(tete_process);
			continue;
		}

		L_c_process *new;
		/*question 1*/
		/*l->seq[0][0] : première chaine de charac dans la commande*/
		/*l->seq[0][1] : ce qu'esL_c_process* t après l'espace de [0][0] (les args)*/
		int pid = fork(); /*processus fils*/
		
		if (!pid)		  /*si processus fils*/
		{
			/*pour les < et >*/
			if (l->in!=NULL){
				int fichier=open(l->in,O_RDONLY);
				dup2(fichier, STDIN_FILENO);
				close(fichier);
			}

			if (l->out!=NULL){
				int fichier=open(l->out,O_WRONLY | O_CREAT | O_TRUNC, 0644);
				dup2(fichier, STDOUT_FILENO);
				close(fichier);
			}
			/*fin < et >*/
			if (l->seq[1] != NULL)/*pipe*/
			{
				
				int pipefd[2];
				if (pipe(pipefd) == -1)
				{
					perror("pipe");
					exit(EXIT_FAILURE);
				}
				int pid2 = fork(); /*creation d'un autre processus fils de fils pour la 2e commande après le pipe*/
				if (!pid2)
				{ /*on est dans le fils du fils*/
					/**/
					dup2(pipefd[1], STDOUT_FILENO);
					close(pipefd[0]);
					close(pipefd[1]);
					execvp(l->seq[0][0], l->seq[0]);
				}
				else /*on est dans le fils du parent ex : ls | grep, on est dans grep (terminal -> grep -> ls)*/
				{
					dup2(pipefd[0], STDIN_FILENO);
					close(pipefd[0]);
					close(pipefd[1]);
					execvp(l->seq[1][0], l->seq[1]);
				}
			}
			else
			{
				execvp(l->seq[0][0], l->seq[0]);
			}
		}

		/*ici on fait attendre le parent*/
		if (!l->bg)
		{

			wait(pid);
		}
		else
		{
			//on est dans un process bg donc on recupere le start time
			struct timeval start_process;
			gettimeofday(&start_process,NULL);

			new = malloc(sizeof(L_c_process));
			new->pid = pid;
			strcpy(new->nom_proc, line_cpy);
			new->start_time=start_process;
			current->suiv = new;

			current = new;
		}
	}
}