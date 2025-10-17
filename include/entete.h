#include <stdint.h>



/**
 * @brief ecrit sur un fichier le code binaire représenté par un string représentant un hex(ATTENTION IL FAUT QUE LE HEX SOIT PAIRE SINON LE DERNIER CARACTERE NE SERA PAS AFFICHÉ)
 *          
 * @param file fichier
 * @param str la chaine de caratère représentant le code hex
 * @return NULL
 */
void affiche(char* str,FILE *file);

/**
 * @brief affiche sur le flux de bit l'entete JPEG pour une image noir et blanc(PRÉCISION 8 BIT POUR LA TABLE DE QUANT)
 *          
 * @param largeur largeur de l'image
 * @param hauteur hauteur de l'image
 * @param file fichier d'écriture
 * @return NULL
 */
void entete_p5(uint16_t largeur, uint16_t hauteur,FILE *file);  

/**
 * @brief affiche sur le flux de bit l'entete JPEG pour une image en couleur(PRÉCISION 8 BIT POUR LA TABLE DE QUANT)
 *          
 * @param largeur largeur de l'image
 * @param hauteur hauteur de l'image
 * @param file fichier d'écriture
 * @param fct_echant echantillage horiz concat à echant verticale
 * @return NULL
 */
void entete_p6(uint16_t largeur, uint16_t hauteur,FILE *file,char* fct_echant);  

/**
 * @brief affiche balise fin d'image
 *          
 * @return NULL
 */
void balise_fin(FILE *file); 