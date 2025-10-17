#include <stdlib.h>
#include <stdbool.h>
/**
 * @brief modifie une matrice donné en format zig-zag
 *
 * @param mat matrice passé en paramètre
 * @return vecteur 1x64
 */

int16_t* zig_zag(int16_t** mat);

/**
 * @brief divise chaque coefficient par un coefficient du tableau de quantification
 *
 * @param  vect,chrom vect : vecteur format zigzag passé en paramètre / chrom : bool qui permet de choisir si quantification sur table chrominance ou non
 * @return vecteur 1x64
 */

int16_t* quant(int16_t* vect,bool chrom);