#include "stdint.h"
#include "string.h"
#include "cpu.h"
#include "console.h"
#include "kbd.h"
#include "clavier.h"
#include "process.h"
#include "ecran.h"

char tampon_clavier[TAILLE_TAMPON_CLAVIER];
int position_tampon = 0;
int echo_active = 1; // pour savoir si l'echo est activé 1: Oui/ 0: Non
struct process* processus_en_attente;
struct list_link list_process_attente_clavier;

void init_list_process_attente_clavier(void){
    // initialisation de la liste des processus en attente du clavier
    INIT_LIST_HEAD(&list_process_attente_clavier);
}
void reception_clavier(void) {
    //acquittement de l'interruption
    outb(0x20, 0x20);
    uint8_t scancode = inb(0x60);
    // on l'interprete avec do_scancode
    do_scancode(scancode);

    //NB: doscnacode va appeler keyboard_data pour mettre dans le tampon
}

void keyboard_data(char *str){
    // si le tampon n'est pas plein, on ajoute tant qu'on peut
    int i = 0;
    while(str[i] != '\0'){
        if(position_tampon < TAILLE_TAMPON_CLAVIER){
            tampon_clavier[position_tampon] = str[i];
            if(echo_active == 1){
                if(str[i] != 127){

                    console_putbytes(&str[i], 1);
                }else{
                    position_tampon--;
                    uint32_t col = get_col_globale();
                    uint32_t lig = get_lig_globale();
                    if(col != 0){
                        set_col_globale(col-1);
                        console_putbytes(" ",1);
                        set_col_globale(col-1);
                        place_curseur(get_lig_globale(), get_col_globale());
                    }else if(col == 0 && lig != 0){
                        set_lig_globale(lig-1); 
                        set_col_globale(79);
                        console_putbytes(" ",1);
                        set_lig_globale(lig-1); 
                        set_col_globale(79);
                        place_curseur(get_lig_globale(), get_col_globale());
                    }
                }
            }
            position_tampon++;
        }else{
            break;
        }
        i++;
    }
    struct process* proc = queue_out(&list_process_attente_clavier, struct process, lien);
    if(proc){

        proc->etat = activable;
        queue_add(proc, &list_process_activable, struct process, lien, prio);
    }
    ordonnanceur();
}

void cons_echo(int on){
    echo_active = on;
}

// int cons_read(char *string, int length){
//     (void)string;
//     (void)length;
// }
int cons_read(){
    
    
    processus_courant->etat = bloque_clavier;
    queue_add(processus_courant, &list_process_attente_clavier, struct process, lien, prio);
    ordonnanceur();
    // Quand on est réveillé :
    if(position_tampon == 0){
        return 0;
    }
    //printf("\nTampon vide: %c \n",tampon_clavier[position_tampon] );
    return tampon_clavier[position_tampon-1];
}