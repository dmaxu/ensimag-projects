#include<stdio.h>
#include<stdint.h>
#include<string.h>
#include<stdlib.h>

#include "matrice.h"

/* Fonction qui permet d'afficher le contenu d'une matrice */
void affiche_matrice(struct Matrice matrix) {
    for (uint16_t i = 0; i <matrix.hauteur ; i++){
        for (uint16_t j = 0; j < matrix.largeur; j++){
            printf("%02x",matrix.mat[i][j]);
            printf(" ");
        }
        printf("\n");
    }
}

/* Fonction qui permet d'afficher le contenu d'une matrice couleur */
void affiche_matrice_couleur(struct Matrice_Couleur matrix) {
  for (uint16_t i = 0; i < (matrix).hauteur ; i++) {
      for (uint16_t j = 0; j < (matrix).largeur; j++){
          printf("%02x",(matrix).mat[i][j].R);
          printf("%02x",(matrix).mat[i][j].G);
          printf("%02x",(matrix).mat[i][j].B);
          printf(" ");
      }
      printf("\n");
    }
}

/* Fonction qui permet de libérer l'espace mémoire alloué pour la création 
de notre matrice */
void libere_matrice(struct Matrice matrix) {
  for (uint16_t i = 0; i < matrix.hauteur; i++) {
    free(matrix.mat[i]);
  }
  free(matrix.mat);
}

/* Fonction qui permet de libérer l'espace mémoire alloué pour la création 
de notre matrice */
void libere_matrice_couleur(struct Matrice_Couleur matrix) {
  for (uint16_t i = 0; i < matrix.hauteur; i++) {
    free(matrix.mat[i]);
  }
  free(matrix.mat);
}