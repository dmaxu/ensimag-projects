#ifndef MATRICE_H
#define MATRICE_H

#include <stdint.h>

struct Matrice
{
    uint16_t hauteur;
    uint16_t largeur;
    int16_t** mat;
};

struct Pixel
{
    int16_t R;
    int16_t G;
    int16_t B;
};

struct Matrice_Couleur
{
    uint16_t hauteur;
    uint16_t largeur;
    struct Pixel** mat;
    uint8_t col;
};

/**
 * @brief Fonction permettant d'afficher le contenu d'une matrice
 *
 * @param struct Matrice* matrix ; une Structure Matrice
 */
void affiche_matrice(struct Matrice matrix);

/**
 * @brief Fonction permettant d'afficher le contenu d'une matrice couleur
 *
 * @param matrix ; une Structure Matrice_Couleur
 */
void affiche_matrice_couleur(struct Matrice_Couleur matrix);

/**
 * @brief Fonction permettant de libérer l'espace mémoire aloué
 * pour la matrice 
 *
 * @param struct Matrice* matrix
 */
void libere_matrice(struct Matrice matrix);

/**
 * @brief Fonction permettant de libérer l'espace mémoire aloué
 * pour la matrice 
 *
 * @param struct Matrice* matrix
 */
void libere_matrice_couleur(struct Matrice_Couleur matrix);

#endif // MATRICE_H