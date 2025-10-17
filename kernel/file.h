#define NBQUEUE 10


typedef struct fileMessage {
    int* message;
    int nbre_message;
    int capacite;
    int indice_ecriture;
    int indice_lecture;
    int nbre_processus_bloque_recieve;
    int nbre_processus_bloque_send;
    struct list_link list_process_bloque_file; //l'initial
    int deleted;
    //int reseted;
} fileMessage;

extern fileMessage liste_fileMessage[NBQUEUE] ;

int pcreate(int count);
int pdelete(int fid);
int pcount(int fid, int *count);
int preceive(int fid,int *message);
int psend(int fid, int message);
int preset(int fid);
