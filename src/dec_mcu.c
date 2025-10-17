#include<stdio.h>
#include<stdint.h>
#include<string.h>
#include<stdlib.h>
#include <math.h>
#include "matrice.h"

/* Va créer un tableau de tableau d'entier dont les dimensions sont spécifiées
en paramètres */
int16_t** initialise_matrix(uint16_t hauteur, uint16_t largeur) {
  
  int16_t** matrix = malloc(hauteur*sizeof(int16_t*));
  for (uint16_t i = 0 ; i < hauteur ; i++) {
    matrix[i] = malloc(largeur*sizeof(int16_t));
  }
  return matrix;
}

/* Va créer un tableau de tableau de Pixel */
struct Pixel** initialise_matrix_colored(uint16_t hauteur, uint16_t largeur) {
  
  //uint8_t** matrix = calloc(hauteur,largeur);
  struct Pixel** matrix = malloc(hauteur*sizeof(struct Pixel*));
  for (uint16_t i = 0 ; i < hauteur ; i++) {
    matrix[i] = malloc(largeur*sizeof(struct Pixel));
  }
  return matrix;
}

struct Matrice define_matrix(uint16_t hauteur, uint16_t largeur) {
  struct Matrice matrix;
  matrix.hauteur = hauteur;
  matrix.largeur = largeur;
  matrix.mat = initialise_matrix(matrix.hauteur,matrix.largeur);
  
  return matrix;
}

struct Matrice_Couleur define_matrix_colored(uint16_t hauteur, uint16_t largeur) {
  struct Matrice_Couleur matrix;
  matrix.hauteur = hauteur;
  matrix.largeur = largeur;
  matrix.mat = initialise_matrix_colored(matrix.hauteur,matrix.largeur);
  return matrix;
}

struct Matrice* convert_to_YCbCr(struct Matrice_Couleur matri) {
    /* On commence par créer trois structure Matrice de même taille
    que la Matrice Couleur donnée en paramètre, mais dont le tableau de tableau
    est rempli de coefficient nuls */
    struct Matrice Y = define_matrix(matri.hauteur,matri.largeur);
    struct Matrice Cb = define_matrix(matri.hauteur,matri.largeur);
    struct Matrice Cr = define_matrix(matri.hauteur,matri.largeur);
    for (uint16_t i = 0; i < matri.hauteur ;i++){
        for (uint16_t j = 0; j < matri.largeur; j++){
            Y.mat[i][j] = (int16_t)round(0.299*matri.mat[i][j].R + 0.587*matri.mat[i][j].G + 0.114*matri.mat[i][j].B);
            Cb.mat[i][j] = (int16_t)round(-0.1687*matri.mat[i][j].R - 0.3313*matri.mat[i][j].G + 0.5*matri.mat[i][j].B + 128);
            Cr.mat[i][j] = (int16_t)round(0.5*matri.mat[i][j].R - 0.4187*matri.mat[i][j].G - 0.0813*matri.mat[i][j].B + 128);
        }
    }
    struct Matrice* tab_YCbCr = (struct Matrice*)calloc(3,sizeof(struct Matrice));

    tab_YCbCr[0] = Y;
    tab_YCbCr[1] = Cb;
    tab_YCbCr[2] = Cr;

    libere_matrice_couleur(matri);

    return tab_YCbCr;
}

/* Fonction qui va regarder si on traite une image couleur ou noir et blanc */
uint8_t check_color(char* input_filename)
{
    FILE* image = fopen(input_filename, "r");

    uint8_t magic_number;
    uint8_t asc_ii_0 = 48;
    fgetc(image); //On sait ici qu'on va lire un P
    magic_number = fgetc(image) - asc_ii_0; // Ici on va avoir 5 ou 6

    fclose(image);
    return magic_number;
}

/**
 * @brief Ouvre le fichier dont le nom est donné en paramètre, regarde si on a un ppm ou pgm
 * prend les dimensions de l'image, et va créer une matrice de pixels.
 *
 * @param input_filename le nom du fichier que l'on veut traiter
 * @return une matrice de pixel (uint8_t) sous forme de tableau de tableau
 */
struct Matrice get_input_image(char* input_filename)
{
    FILE* image = fopen(input_filename, "r");

    char car;
    uint16_t cpt = 0;
    uint16_t hauteur = 0;
    uint16_t largeur = 0;
    uint16_t asc_ii_0 = 48;
    /*On va entrer dans cette boucle tant qu'on est dans les données de l'en-tête*/
    while (cpt < 3){
        /* On sait qu'un fichier commence soit par 50 36 0a soit par 50 35 0a
        pour : P6 espace ou P5 espace */
        if (cpt == 0) {
            car = fgetc(image); //On sait qu'on va lire un P
            fgetc(image);
            car = fgetc(image); //On sait que car vaut espace
            cpt++;
        }
        
        /*Ici on va récupérer les dimensions de l'image*/
        if (cpt == 1) {
            car = fgetc(image);
            //gestion commentaire
            if (car==35){
                while(car != 10){
                    car = fgetc(image);
                }
                car = fgetc(image); 
            }

            /*Alors on est dans la section des dimensions*/

            while (car != 32) //Tant qu'on ne voit pas d'espace
            {
                largeur = 10*largeur + (car - asc_ii_0);
                car = fgetc(image);
            }
            car = fgetc(image);
            while (car != 10) //Tant qu'on ne voit pas d'espace
            {
                hauteur = 10*hauteur + (car-asc_ii_0);
                car = fgetc(image);
            }
            cpt++;
        }
        /*Ici cette section ne nous intéresse pas vraiment 
        car c'est elle ou est ecrit 255 tout le temps */
        else
        {
            do
            {
                car = fgetc(image);
            } while (car != 10);
            cpt++;
        }
    }

    /* Ici on va gérer les pixels du fichiers pgm */
    struct Matrice matrix = define_matrix(hauteur,largeur);

    char pix;
    for (uint16_t i = 0; i < hauteur; i++){
        for (uint16_t j = 0; j < largeur;j++) {

            pix = fgetc(image);
            (matrix.mat)[i][j] = (uint8_t)pix;

        }
    }

    fclose(image);

    return matrix;
}

struct Matrice_Couleur get_input_image_colored(char* input_filename)
{
    FILE* image = fopen(input_filename, "r");

    char car;
    uint16_t cpt = 0;
    uint16_t hauteur = 0;
    uint16_t largeur = 0;
    uint16_t asc_ii_0 = 48;
    /*On va entrer dans cette boucle tant qu'on est dans les données de l'en-tête*/
    while (cpt < 3){
        /* On sait qu'un fichier commence soit par 50 36 0a soit par 50 35 0a
        pour : P6 espace ou P5 espace */
        if (cpt == 0) {
            car = fgetc(image); //On sait qu'on va lire un P
            fgetc(image);
            car = fgetc(image); //On sait que car vaut espace
            cpt++;
        }
        
        /*Ici on va récupérer les dimensions de l'image*/
        if (cpt == 1) {
            car = fgetc(image);
            //gestion commentaire
            if (car==35){
                while(car != 10){
                    car = fgetc(image);
                }
                car = fgetc(image); 
            }

            /*Alors on est dans la section des dimensions*/

            while (car != 32) //Tant qu'on ne voit pas d'espace
            {
                largeur = 10*largeur + (car - asc_ii_0);
                car = fgetc(image);
            }
            car = fgetc(image);
            while (car != 10) //Tant qu'on ne voit pas d'espace
            {
                hauteur = 10*hauteur + (car-asc_ii_0);
                car = fgetc(image);
            }
            cpt++;
        }
        /*Ici cette section ne nous intéresse pas vraiemnt*/
        else
        {
            do
            {
                car = fgetc(image);
            } while (car != 10);
            cpt++;
        }
    }

    /* Ici on va gérer les pixels du fichiers pgm */
    struct Matrice_Couleur matrix = define_matrix_colored(hauteur,largeur);

    char pix;
    
    for (uint16_t i = 0; i < hauteur; i++){
        for (uint16_t j = 0 ; j < largeur ; j++){
            /* On initialise d'abord le pixel rouge, puis le vert, puis le bleu */
            pix = fgetc(image);
            (matrix.mat)[i][j].R = (uint8_t)pix;

            pix = fgetc(image);
            (matrix.mat)[i][j].G = (uint8_t)pix;

            pix = fgetc(image);
            (matrix.mat)[i][j].B = (uint8_t)pix;
        }
    }

    fclose(image);

    return matrix;
}

/* Permet d'ajouter des ligne à une matrice */
void add_line(struct Matrice* matrix){
    uint16_t to_add = (*matrix).hauteur;
    uint16_t old_haut = (*matrix).hauteur;
    while (to_add % 8 != 0) {
        to_add++;
    }
    /* On modifie la valeuleur de la hauteur de la matrice */
    matrix->hauteur = to_add;
    /* On re-alloue de la mémoire */
    (*matrix).mat = realloc((*matrix).mat,to_add*sizeof(int16_t*));
    
    for (uint16_t n = old_haut;n < to_add;n++){
        (*matrix).mat[n] = calloc((*matrix).largeur,sizeof(int16_t));
        for (uint16_t j = 0;j < matrix->largeur;j++) {
            (*matrix).mat[n][j] = (*matrix).mat[n-1][j];
        }
    }
}

void add_line_colored(struct Matrice_Couleur* matrix,int h_mcu){
    uint16_t to_add = (*matrix).hauteur;
    uint16_t old_haut = (*matrix).hauteur;
    while (to_add % h_mcu != 0) {
        to_add++;
    }
    /* On modifie la valeuleur de la hauteur de la matrice */
    matrix->hauteur = to_add;
    /* On re-alloue de la mémoire */
    (*matrix).mat = realloc((*matrix).mat,to_add*sizeof(struct Pixel*));
    
    for (uint16_t n = old_haut;n < to_add;n++){
        (*matrix).mat[n] = calloc((*matrix).largeur,sizeof(struct Pixel));
        for (uint16_t j = 0;j < matrix->largeur;j++) {
            (*matrix).mat[n][j].R = (*matrix).mat[n-1][j].R;
            (*matrix).mat[n][j].G = (*matrix).mat[n-1][j].G;
            (*matrix).mat[n][j].B = (*matrix).mat[n-1][j].B;
        }
    }
}

/* Permet d'ajouter des colonnes à une matrice */
void add_column(struct Matrice* matrix) {
    uint16_t to_add = (*matrix).largeur;
    uint16_t old_lar = (*matrix).largeur;
    while (to_add % 8 != 0) {
        to_add++;
    }
    (*matrix).largeur = to_add;
    for (uint16_t i = 0;i<(*matrix).hauteur;i++){
        (*matrix).mat[i] = realloc((*matrix).mat[i],to_add*sizeof(int16_t));
        for (uint16_t n = old_lar;n < to_add;n++){
            (*matrix).mat[i][n] = (*matrix).mat[i][n-1];
        }
    }
}

void add_column_colored(struct Matrice_Couleur* matrix, int l_mcu) {
    uint16_t to_add = (*matrix).largeur;
    uint16_t old_lar = (*matrix).largeur;
    while (to_add % l_mcu != 0) {
        to_add++;
    }
    (*matrix).largeur = to_add;
    for (uint16_t i = 0;i<(*matrix).hauteur;i++){
        (*matrix).mat[i] = realloc((*matrix).mat[i],to_add*sizeof(struct Pixel));
        for (uint16_t n = old_lar;n < to_add;n++){
            (*matrix).mat[i][n].R = (*matrix).mat[i][n-1].R;
            (*matrix).mat[i][n].G = (*matrix).mat[i][n-1].G;
            (*matrix).mat[i][n].B = (*matrix).mat[i][n-1].B;
        }
    }
}

/*Fonction qui va prendre une matrice de pixels en entrée (tous les pixels d'une image)
et qui va découper cette image en MCU.
Ainsi cette fonction doit renvoyer un tableau de structures Matrice correspondant aux MCU.*/
struct Matrice* MCU_dividing(struct Matrice* matrix,int h_mcu,int l_mcu) {

    /* Il faut maintenant vérifier que l'image possède bien un multiple de 8 de ligne et colonne */
    if (matrix->hauteur % h_mcu != 0) {
        add_line(matrix);
    }
    if (matrix->largeur % l_mcu != 0) {
        add_column(matrix);
    }

    /* On calcule le nombre de MCU nécessaire au découpage de cette image */
    uint32_t nb_mcu = ((*matrix).hauteur)/h_mcu * ((*matrix).largeur)/l_mcu;
    /* On peut alors créer notre tableau de Matrice qui seront de la taille des MCU */
    struct Matrice* tab_mcu = (struct Matrice*)calloc(nb_mcu,sizeof(struct Matrice));

    uint16_t cpt_c = 0;
    uint16_t cpt_l = 0;
    uint32_t ind = 0;
    for (uint16_t k = 0 ; k < ((*matrix).hauteur)/h_mcu ; k++) {
        for (uint16_t p = 0 ; p < ((*matrix).largeur)/l_mcu;p++) {
            /* On commence par créer notre mcu avec que des zeros à l'intérieur */
            struct Matrice mcu = define_matrix(h_mcu,l_mcu);
            for (uint16_t i = 0 + cpt_l; i < h_mcu+cpt_l;i++) {
                for (uint16_t j = 0 + cpt_c; j < l_mcu + cpt_c;j++) {
                    /* On rempli la mcu avec les pixels contenus dans la matrice de base */
                    mcu.mat[i-cpt_l][j-cpt_c] = matrix->mat[i][j];
                }
            }
            cpt_c += l_mcu;
            tab_mcu[ind] = mcu;
            ind++;
        }
        cpt_l += h_mcu;
        cpt_c = 0;
    }

    return tab_mcu;
}

/* Renvoie un tableau de structure Matrice_Couleur */
struct Matrice_Couleur* MCU_dividing_colored(struct Matrice_Couleur* matrix,int h_mcu, int l_mcu) {

    /* Il faut maintenant vérifier que l'image possède bien un multiple de 8 de ligne et colonne */
    if (matrix->hauteur % h_mcu != 0) {
        //Il faut rajouter des lignes
        add_line_colored(matrix,h_mcu);
    }
    if (matrix->largeur % l_mcu != 0) {
        //il faut rajouter des colonnes
        add_column_colored(matrix,l_mcu);
    }

    /* On calcule le nombre de MCU nécessaire au découpage de cette image */
    uint32_t nb_mcu = ((*matrix).hauteur)/h_mcu * ((*matrix).largeur)/l_mcu;
    /* On peut alors créer notre tableau de Matrice qui seront de la taille des MCU */
    struct Matrice_Couleur* tab_mcu = (struct Matrice_Couleur*)calloc(nb_mcu,sizeof(struct Matrice_Couleur));
    uint16_t cpt_c = 0;
    uint16_t cpt_l = 0;
    uint32_t ind = 0;
    for (uint16_t k = 0 ; k < ((*matrix).hauteur)/h_mcu ; k++) {
        for (uint16_t p = 0 ; p < ((*matrix).largeur)/l_mcu;p++) {
            /* On commence par créer notre mcu avec que des zeros à l'intérieur */
            struct Matrice_Couleur mcu = define_matrix_colored(h_mcu,l_mcu);
            for (uint16_t i = 0 + cpt_l; i < h_mcu+cpt_l;i++) {
                for (uint16_t j = 0 + cpt_c; j < l_mcu + cpt_c;j++) {
                    /* On rempli notre mcu couleur */
                    mcu.mat[i-cpt_l][j-cpt_c].R = matrix->mat[i][j].R;
                    mcu.mat[i-cpt_l][j-cpt_c].G = matrix->mat[i][j].G;
                    mcu.mat[i-cpt_l][j-cpt_c].B = matrix->mat[i][j].B;
                }
            }
            cpt_c += l_mcu;
            tab_mcu[ind] = mcu;
            ind++;
        }
        cpt_l += h_mcu;
        cpt_c = 0;
    }

    return tab_mcu;
}
