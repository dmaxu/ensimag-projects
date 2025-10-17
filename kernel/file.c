////// file de message 
#include "process.h"
#include "file.h"
#include "queue.h"
#include "string.h"


int nbre_queue=0;
fileMessage liste_fileMessage[NBQUEUE] ;

int pcreate(int count) {
    if (count <= 0 || count >= 1073741827)
        return -1;


    int fid = -1;
    for (int i = 0; i < NBQUEUE; i++) {
        if (liste_fileMessage[i].message == NULL|| liste_fileMessage[i].deleted) {
            fid = i;
            break;
        }
    }
    if (fid < 0)
        return -1;


    if (liste_fileMessage[fid].message) {
        mem_free(liste_fileMessage[fid].message,sizeof(int) * liste_fileMessage[fid].capacite);
    }
    

    void *buf = mem_alloc(count * sizeof(int));
    if (!buf) {
        return -1;
    }
    liste_fileMessage[fid].message = buf;

    liste_fileMessage[fid].deleted= 0;
    liste_fileMessage[fid].indice_lecture= 0;
    liste_fileMessage[fid].indice_ecriture= 0;
    liste_fileMessage[fid].capacite = count;
    liste_fileMessage[fid].nbre_message = 0;
    liste_fileMessage[fid].nbre_processus_bloque_recieve = 0;
    liste_fileMessage[fid].nbre_processus_bloque_send    = 0;

    INIT_LIST_HEAD(&liste_fileMessage[fid].list_process_bloque_file);

    return fid;
}




int pdelete(int fid) {

    if (fid < 0 || fid >= NBQUEUE|| liste_fileMessage[fid].deleted)
        return -1;




    liste_fileMessage[fid].deleted=1;

    // liberer processus bloqués
    struct process *proc,*next;
    queue_for_each_safe(proc, next,&((liste_fileMessage[fid]).list_process_bloque_file), struct process, linkFileMessage)
    {
        proc->etat=activable;
        queue_del(proc,linkFileMessage);
        queue_add(proc,&list_process_activable,struct process ,lien , prio);
    }



    if (liste_fileMessage[fid].message) 
        {
            mem_free(liste_fileMessage[fid].message,sizeof(int) * liste_fileMessage[fid].capacite);
            liste_fileMessage[fid].message = NULL;
        }
    liste_fileMessage[fid].capacite = 0;
    liste_fileMessage[fid].indice_ecriture = 0;
    liste_fileMessage[fid].indice_lecture = 0;
    //
    liste_fileMessage[fid].nbre_message = 0;
    liste_fileMessage[fid].nbre_processus_bloque_recieve = 0;
    liste_fileMessage[fid].nbre_processus_bloque_send = 0;

    ordonnanceur();

    return 0;

}


int pcount(int fid, int *count){
    if (fid < 0 || fid >= NBQUEUE) {
        printf(" fid invalide , impossible de calculer pcount\n");
        return -1;
    }

    if (liste_fileMessage[fid].nbre_processus_bloque_recieve > 0) {
        *count = -liste_fileMessage[fid].nbre_processus_bloque_recieve;
    } else {
        *count = liste_fileMessage[fid].nbre_message + liste_fileMessage[fid].nbre_processus_bloque_send;
    }

    return 0;
}


int preceive(int fid, int *message) {
    if (fid < 0 || fid >= NBQUEUE) {
        printf("fid invalide, impossible de faire preceive\n");
        return -1;
    }

     if(liste_fileMessage[fid].deleted==1){
        return -1;
    }




    fileMessage *file = &liste_fileMessage[fid];

    // Si des messages dispo  : on lit
    if (file->nbre_message > 0) {
        file->indice_lecture = file->indice_lecture % file->capacite; 
        *message = file->message[file->indice_lecture]; 
        file->indice_lecture++; 
        file->nbre_message--;

        // S’il y avait un psend bloque : le débloquer
        if (file->nbre_processus_bloque_send > 0) {
            struct process *proc_send = queue_out(&(file->list_process_bloque_file), struct process, linkFileMessage);
            if (proc_send) {
                int msg = proc_send->messageEnvoye;
                //
                file->indice_ecriture = file->indice_ecriture % file->capacite;
                file->message[file->indice_ecriture] = msg;
                file->indice_ecriture++; //pour suivre les indices d'écritures de la file
                //
                //file->message[file->nbre_message] = msg;
                file->nbre_message++;
                file->nbre_processus_bloque_send--;

                proc_send->etat = activable;
                queue_add(proc_send, &list_process_activable, struct process, lien, prio);

                ordonnanceur();
            }
            
        }



        return 0;
    }

    // Si file vide : bloquer le processus
    processus_courant->etat = bloque_file;
    processus_courant->etat_bloque = 0;
    file->nbre_processus_bloque_recieve++;
    queue_add(processus_courant, &(file->list_process_bloque_file), struct process, linkFileMessage,prio);
    ordonnanceur();


    *message = processus_courant->messageReçu;


     if(liste_fileMessage[fid].deleted==1){
        return -1;
    }

    if(processus_courant->reseted_message==1){
        processus_courant->reseted_message=0;
        return -1;
    }


    return 0;
}


int psend(int fid, int message){
    if (fid < 0 || fid >= NBQUEUE) {
        printf(" fid invalide , impossible de calculer psend\n");
        return -1;
    }

    if(liste_fileMessage[fid].deleted==1){
        return -1;
    }




    processus_courant-> messageEnvoye = message;

    if(liste_fileMessage[fid].nbre_message < liste_fileMessage[fid].capacite){
        if((liste_fileMessage[fid]).nbre_processus_bloque_recieve>0){

            struct process* proc_recieve = queue_out(&(liste_fileMessage[fid].list_process_bloque_file), struct process, linkFileMessage);
            proc_recieve->etat = activable;
            queue_add(proc_recieve, &list_process_activable, struct process, lien, prio);
            proc_recieve->messageReçu = message;

            (liste_fileMessage[fid]).nbre_processus_bloque_recieve--;

            ordonnanceur();
        }else{
            //
            fileMessage *file = &liste_fileMessage[fid];
            //
            file->indice_ecriture = file->indice_ecriture % file->capacite;
            liste_fileMessage[fid].message[file->indice_ecriture] = message;
            file->indice_ecriture++;
            liste_fileMessage[fid].nbre_message++;
        }
    }else{
        processus_courant->etat = bloque_file;
        processus_courant->etat_bloque = 1;
        (liste_fileMessage[fid]).nbre_processus_bloque_send++;
        queue_add(processus_courant, &((liste_fileMessage[fid]).list_process_bloque_file) , struct process, linkFileMessage, prio);
        ordonnanceur();



         if(liste_fileMessage[fid].deleted==1){
        return -1;
        }

        if(processus_courant->reseted_message==1){
            processus_courant->reseted_message=0;
            return -1;
        }


    }




    return 0;
}


int preset(int fid){
    if (fid < 0 || fid >= NBQUEUE) {
        printf("fid invalide, impossible de faire preset\n");
        return -1;
    }

     if(liste_fileMessage[fid].deleted==1){
        return -1;
    }







    

    liste_fileMessage[fid].indice_lecture = 0;
    liste_fileMessage[fid].indice_ecriture = 0;
    //
    liste_fileMessage[fid].nbre_message = 0;
    liste_fileMessage[fid].nbre_processus_bloque_recieve = 0;
    liste_fileMessage[fid].nbre_processus_bloque_send = 0;
    memset(liste_fileMessage[fid].message,0,liste_fileMessage[fid].capacite*sizeof(int));  // A VERIFIER SI C'EST LA BONNE TAILLE capacite*sizeof(int) !!!!!!
    

    struct process *proc,*next;
    queue_for_each_safe(proc,next, &((liste_fileMessage[fid]).list_process_bloque_file), struct process, linkFileMessage)
    {
        printf("  nom : %s  \n",proc->nom);
        proc->reseted_message =1;
        proc->etat=activable;
        queue_del(proc,linkFileMessage);
        queue_add(proc,&list_process_activable,struct process ,lien , prio);
    }


    ordonnanceur();

    return 0;
}
