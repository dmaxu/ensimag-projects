#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <stdint.h>
#include "entete.h"
#include "dct.h"
#include "dec_mcu.h"
#include "matrice.h"
#include "zigquant.h"
#include "encod_RLE.h"
#include "ss_echant.h"
#include <unistd.h>    
#include <sys/stat.h>
#include <errno.h>

char* creation_dossier(char* PATH){
    char* ptr = strchr(PATH,'/');
    char sub_str[100];
    int dir;
    if (ptr == PATH)
        ptr = strchr(PATH+1,'/');
    while(ptr!=NULL){
        strncpy(sub_str, PATH, ptr-PATH);
        sub_str[ptr-PATH] = '\0';
        mkdir(sub_str,0755);
        if ( errno!=EEXIST){
            mkdir(sub_str,0755);
            dir = chdir(sub_str);
            if (dir == -1)
                return NULL;
            PATH = ptr + sizeof(char);
        }
        else {
            dir = chdir(sub_str);
            if (dir == -1)
                return NULL ;
            PATH = ptr + sizeof(char);
        }
        ptr = strchr(PATH,'/');
    }
    return (PATH );
}

int main(int argc, char **argv)
{   
    //gestion options
    if (argc == 1) {
        printf("Erreur, vous n'avez pas spécifié d'input\n");
        return EXIT_FAILURE;
    }

    char entree[100] = "";
    char sortie[100] = "";
    char sous_echant[12] = "1x1,1x1,1x1";

    if (argc > 2) {
        for (int i = 1; i < argc; i++) {
            if (strncmp(argv[i], "--outfile=", 10) == 0) {
                // Utilisation de strncpy pour copier la sortie
                strncpy(sortie, argv[i] + 10, sizeof(sortie) - 1);
                sortie[sizeof(sortie) - 1] = '\0'; // Assurer la terminaison nulle
            } else if (strncmp(argv[i], "--sample=", 9) == 0) {
                // Utilisation de strncpy pour copier le sous-échantillonnage
                strncpy(sous_echant, argv[i] + 9, sizeof(sous_echant) - 1);
                sous_echant[sizeof(sous_echant) - 1] = '\0'; // Assurer la terminaison nulle
            } else if (strlen(argv[i]) >= 4 && (strcmp(argv[i] + strlen(argv[i]) - 4, ".ppm") == 0 || strcmp(argv[i] + strlen(argv[i]) - 4, ".pgm") == 0)) {
                // Utilisation de strncpy pour copier le fichier d'entrée
                strncpy(entree, argv[i], sizeof(entree) - 1);
                entree[sizeof(entree) - 1] = '\0'; // Assurer la terminaison nulle
            } else {
                printf("Erreur dans la spécification des paramètres\n");
                return EXIT_FAILURE;
            }
        }
        
    } else {
        if (strncmp(argv[1], "--help", 10) == 0) {
            printf("Usage: ./ppm2jpeg [options] image.{pgm, ppm}\n");
            printf("Options list:\n");
            printf("      --outfile=filename : sets the output file name to filename\n");
            printf("      --sample=h1xv1,h2xv2,h3xv3 : sets the horizontal and vertical sampling factors for components 1..3\n");
            printf("      --help : prints this help\n");
            return EXIT_SUCCESS;
        }
        else if (strlen(argv[1]) >= 4 && (strcmp(argv[1] + strlen(argv[1]) - 4, ".ppm") == 0 || strcmp(argv[1] + strlen(argv[1]) - 4, ".pgm") == 0)) {
            // Utilisation de strncpy pour copier le fichier d'entrée
            strncpy(entree, argv[1], sizeof(entree) - 1);
            entree[sizeof(entree) - 1] = '\0'; // Assurer la terminaison nulle

            // Copie de l'entrée vers la sortie
            strncpy(sortie, argv[1], sizeof(sortie) - 1);
            sortie[sizeof(sortie) - 1] = '\0'; // Assurer la terminaison nulle

            // Remplacement de l'extension par ".jpg" avec snprintf
            if (strlen(sortie) >= 4) {
                snprintf(sortie + strlen(sortie) - 4, 5, ".jpg");
            } else {
                printf("Erreur : nom de fichier de sortie trop court\n");
                return EXIT_FAILURE;
            }
        } else {
            printf("Erreur : le fichier passé en entrée n'est pas un .ppm ou .pgm\n");
            return EXIT_FAILURE;
        }
    }
    if (sortie[0] == '\0'){
        strncpy(sortie,entree, strlen(entree));
        sortie[sizeof(sortie) - 1] = '\0'; // Assurer la terminaison nulle
        // Remplacement de l'extension par ".jpg" avec snprintf
        if (strlen(sortie) >= 4) {
            snprintf(sortie + strlen(sortie) - 4, 5, ".jpg");
        } else {
            printf("Erreur : nom de fichier de sortie trop court\n");
            return EXIT_FAILURE;
        }
    }


    FILE *fptr;
    //Creation path
    char chemin[1000] ;
    char* wd = getcwd(chemin,100);
    if (wd == NULL)
        return EXIT_FAILURE;
    char* nom_image;
    //verifier s'il faut effectuer un traitement de dossier
    if (strrchr(sortie,'/')!=NULL){
         nom_image= creation_dossier(sortie);
    }
    else {
        nom_image = sortie;
    }
    fptr = fopen(nom_image, "wb");
    // uint32_t nbMCU; //le nombre de MCU à traiter pour la suite
    //dimension de l'image de base (obtenu ensuite avec la fonction get_input)
    uint32_t largeurMCU = 8;  
    uint32_t hauteurMCU = 8;
    int diro = chdir(chemin);
    if (diro == -1)
        return EXIT_FAILURE;
    uint8_t col = check_color(entree);
    //variable pour la fonction encodage RLE
    char forgotten_char[1];
    forgotten_char[0] = ' ';
    char buffer[64];
    size_t capacity = 0;
    size_t empty_case = 0;
    size_t start = 0;
    bool end = false;
    //pour le calcul de difference de DC
    int16_t last_dc_Y = 0;
    int16_t copy_dc_Y;
    int16_t last_dc[] = {0,0,0};
    int16_t copy_dc[] = {0,0,0};


    if (col == 5) {
        
        //mat_pix contient la liste des mcu(dimension des mcu à definier en fct du ss echantillonage)
        struct Matrice mat_pix = get_input_image(entree);
        // printf("Dimension %u x %u",mcus.largeur,mcus.hauteur);
        // affiche_matrice(mat_pix);
        entete_p5(mat_pix.largeur,mat_pix.hauteur,fptr);
        struct Matrice* tab_MCU = MCU_dividing(&mat_pix,hauteurMCU,largeurMCU);
        uint32_t nbMCU = (mat_pix.hauteur)/hauteurMCU * (mat_pix.largeur)/largeurMCU;
        // printf("J'ai découpé");
        // // affiche_matrice(&mat_pix);
        libere_matrice(mat_pix);
        // struct Matrice mat_freq = define_matrix(hauteurMCU,largeurMCU);
        for (uint32_t i=0;i<nbMCU;i++){
            end = (i==(nbMCU-1));
            struct Matrice mat_freq = define_matrix(hauteurMCU,largeurMCU);
            // mat_freq.hauteur = hauteurMCU;
            // mat_freq.largeur = largeurMCU;
            for (uint8_t l = 0 ; l < 8; l++) {
                free(mat_freq.mat[l]);
            }
            free(mat_freq.mat);
            mat_freq.mat = dct(tab_MCU[i].mat);

            /*mat_ferq est donc une matrice d'entiers signés sur 16 bits */
            int16_t* vecteur_quant = quant(zig_zag(mat_freq.mat),false);
            // libere_matrice(mat_freq);

            copy_dc_Y = vecteur_quant[0];
            vecteur_quant[0] = vecteur_quant[0] - last_dc_Y;
            parcours_RLE(vecteur_quant,fptr,buffer,end,&capacity,&start,&empty_case,forgotten_char,true);
            free(vecteur_quant);
            last_dc_Y = copy_dc_Y;    
                     
        }
        free(tab_MCU);
        balise_fin(fptr);             
        
    }
    
    else if (col == 6)
    {
        struct Matrice_Couleur mat_pix = get_input_image_colored(entree);
        entete_p6(mat_pix.largeur,mat_pix.hauteur,fptr,sous_echant);
        uint8_t* tab_dim = taille_dec_mat(sous_echant);
        hauteurMCU = tab_dim[0];
        largeurMCU = tab_dim[1];
        free(tab_dim);
        struct Matrice_Couleur* tab = MCU_dividing_colored(&mat_pix,hauteurMCU,largeurMCU);
        uint32_t nbMCU = (mat_pix.hauteur)/hauteurMCU * (mat_pix.largeur)/largeurMCU;
        libere_matrice_couleur(mat_pix);
        // free(tab_dim);
        // struct Matrice mat_freq = define_matrix(8,8);
        for (uint32_t i=0;i<nbMCU;i++) {
            
            /* conversion  */
            struct Matrice* tab_YCbCr = convert_to_YCbCr(tab[i]);

            Cell* lc_Mcu = lc_ssechant(tab_YCbCr,sous_echant);
            if (lc_Mcu == NULL) {
                return EXIT_FAILURE;
            }
            Cell* head = lc_Mcu;
            // for (uint16_t k = 0 ; k<3;k++) {
            //     libere_matrice(tab_YCbCr[k]);
            // }
            // free(tab_YCbCr);

            while (lc_Mcu != NULL){
                struct Matrice mat_freq = define_matrix(8,8);
                for (uint8_t l = 0 ; l < 8; l++) {
                    free(mat_freq.mat[l]);
                }
                free(mat_freq.mat);
                // printf("Verif\n");
                // affiche_matrice(lc_Mcu->mat);
                mat_freq.mat = dct((lc_Mcu->mat).mat);
                // affiche_matrice(mat_freq);

                bool chrom = (lc_Mcu->Y_Cb_Cr!=0);
                int16_t* vecteur_quant = quant(zig_zag(mat_freq.mat),chrom);

                uint8_t idx = lc_Mcu->Y_Cb_Cr;
                end = (i==(nbMCU-1)&&(idx == 2));
                copy_dc[idx] = vecteur_quant[0];
                vecteur_quant[0] = vecteur_quant[0] - last_dc[idx];
                parcours_RLE(vecteur_quant,fptr,buffer,end,&capacity,&start,&empty_case,forgotten_char,!chrom);
                free(vecteur_quant);
                last_dc[idx] = copy_dc[idx];
                lc_Mcu=lc_Mcu->suiv;
            }
            for (uint8_t k = 0 ; k < 3 ; k++) {
                libere_matrice(tab_YCbCr[k]);
            }
            free(tab_YCbCr);
            // free_lc(head);
            lc_Mcu = head;
            Cell* next = lc_Mcu->suiv;
            while (next != NULL)
            {
                // libere_matrice(lc_Mcu->mat);
                free(lc_Mcu);
                lc_Mcu = next;
                next = next->suiv;
            }
            // free(head);
            free(lc_Mcu);
            
        }
        // for (uint16_t i = 0; i < nbMCU; i++)
        // {
        //     free(tab->mat[i]);
        //     // libere_matrice(tab[i]);
        // }
        free(tab);
    balise_fin(fptr);
    }
    
    fclose(fptr);

    return EXIT_SUCCESS;
}