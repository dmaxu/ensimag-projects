#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include "matrice.h"

typedef struct Cell {
    struct Matrice mat;
    struct Cell* suiv;
    uint8_t Y_Cb_Cr;
}Cell;
/**
 * @brief permet d'inserer une cellule
 *
 * @param mat ; structure matrice
 * @param Y_Cb_Cr ; int qui permet de reconnaitre si c'est Y, Cb et Cr
 * @return ptr vers une cellule
 */

Cell* new_cel(struct Matrice mat,uint8_t Y_Cb_Cr);

/**
 * @brief sous-échantillonnage 4:2:0
 *
 * @param mat ; matrice passé en paramètre 
 * @param n ; nb ligne 
 * @param m ; m: nb colonne
 * @return tableau de structure matrice 8x8 
 */

struct Matrice* ss_echant420(int16_t** mat,int16_t n,int16_t m,char* ss_echant);

/**
 * @brief sous-échantillonnage horizontal
 *
 * @param mat ; matrice passé en paramètre 
 * @param n ; nb ligne 
 * @param m ; m: nb colonne
 * @return tableau de structure matrice 8x8 
 */

struct Matrice* ss_echant_hori(int16_t** mat,int16_t n,int16_t m,char* ss_echant);

/**
 * @brief sous-échantillonnage vertical
 *
 * @param mat ; matrice passé en paramètre 
 * @param n ; nb ligne 
 * @param m ; m: nb colonne
 * @return tableau de structure matrice 8x8 
 */

struct Matrice* ss_echant_vert(int16_t** mat,int16_t n,int16_t m,char* ss_echant);

/**
 * @brief détection de type et vérification des conditions de sous-échantillonnage
 *  
 * la fonction va renvoyer un int qui correspond à un certain ssechant
    elle va aussi vérifier si les conditions de ssechant sont respecté
    renvoie 0 si le c'est un ss echant qui ne respecte pas les conditions et écrit une erreur
    1 = ss_echant hori, 2= ss_echant vert, 3=ss_echant hori et vert, 4=pas de sous echant, 0 = error
 * @param ss_echant ; chaine de charactère de type h1xv1,h2xv2,h3xv3
 * @return un entier correspondant au type de sous-échantillonnage 
 */

uint8_t detect_ssechant(char* ss_echant);

/**
 * @brief permet de trouver le nombre de matrice 8x8 par composant en fonction du sous-échantillonnage
 *
 * @param ss_echant ; sous-échantillonnage
 * @return tableau de valeur : [nombre de matrice 8x8 pour Y,nombre de matrice 8x8 pour Cb,nombre de matrice 8x8 pour Cr] 
 */

uint8_t* taille_Y_Cb_Cr(char* ss_echant);

/**
 * @brief donne les dim nécessaire du découpage pour un sous-échantillonnage donnée
 *
 * @param ss_echant ; sous-échantillonnage
 * @return tableau de valeur : [nb ligne,nb colonne] 
 */

uint8_t* taille_dec_mat(char* ss_echant);


/**
 * @brief créer une liste chainée de struct matrice pour un MCU donnée Y0->..->Yn->Cb0->..->Cbm->Cr0->..->Crm->NULL
 *
 * @param tab ; tableau de structure matrice passé en paramètre 
 * @param ss_echant ; sous-échantillonnage
 * @return liste chainée de structure matrice. 
 */

Cell* lc_ssechant(struct Matrice* tab,char* ss_echant);

