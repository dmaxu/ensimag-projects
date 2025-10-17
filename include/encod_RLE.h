#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
/**
 * @brief cherche l'index d'une valeur donnée correspondant à sa magnitude
 *
 * @param val,magn
 * @return l'index
 */

int cherche_ind_magn(int val,int magn);

/**
 * @brief convertie un decimal en binaire (str)
 *
 * @param dec, valeur à convertir
 * @return chaine de charactère binaire
 */

char* dec_to_bin(int dec);

/**
 * @brief converti un décimal en binaire avec un taille de bit fixé
 *
 * @param dec,magn dec : décimal à convertir, magn : taille de la chaine de charac
 * @return chaine de charac binaire taille fixé
 */

char* dec_to_binmagn(int dec,int magn);

/**
 * @brief concatene 2 chaines de charactères
 *
 * @param x,y
 * @return chaine de charactère
 */

char* concat(char* x, char* y);

/**
 * @brief calcule le code RLE d'un mot donnée
 *
 * @param mot,cpt,dc mot: le mot à coder, cpt: compteur de '0', dc : permet de différencier si nous codons le 1er du vecteur(true) ou non.
 * @return chaine de charactère correspondant au codage RLE d'un mot
 */

char* RLE(int16_t mot,int cpt,bool dc,bool isY) ;


/**
 * @brief affiche sur le flux de bit le codage RLE d'un vecteur
 *
 * @param vecteur_quant vecteur d'entier à encoder
 * @param file pointeur vers le fichier d'écriture
 * @param buffer pour stocker temporerement les bits (taille 64 nécessaire)
 * @param end pour indiquer si c'est la dernière MCU au cas où il faut compléter par des 0
 * @param capacity_ptr pointeur vers la capacity du buffer
 * @param start_ptr pointeur vers le début du buffer
 * @param capacity_ptr pointeur vers la capacity du buffer
 * @param start_ptr pointeur vers la première case vide du buffer
 * @param forgotten_char pointeur vers un charactère qui n'a pas été traité pendant la RLE de la dernière MCU
 * @param isY pour indiquer si c'est une MCU de la chrominance
 * @return NULL
 */

void parcours_RLE(int16_t* vecteur_quant,FILE *file,char* buffer,bool end,size_t* capacity_ptr,size_t* start_ptr,size_t* empty_case_ptr,char* forgotten_char,bool isY);

