#include "dct.h"
#include <stdlib.h>
#include <math.h>
#include <stdint.h>

//mémoisation pour le calcul des cos (calculé avec cos.c)

float cos_tab[8][8] = {{1.000000,0.980785,0.923880,0.831470,0.707107,0.555570,0.382683,0.195090},{1.000000,0.831470,0.382683,-0.195090,-0.707107,-0.980785,-0.923880,-0.555570},{1.000000,0.555570,-0.382683,-0.980785,-0.707107,0.195090,0.923880,0.831470},{1.000000,0.195090,-0.923880,-0.555570,0.707107,0.831470,-0.382683,-0.980785},{1.000000,-0.195090,-0.923880,0.555570,0.707107,-0.831470,-0.382683,0.980785},{1.000000,-0.555570,-0.382683,0.980785,-0.707107,-0.195090,0.923880,-0.831470},{1.000000,-0.831470,0.382683,0.195090,-0.707107,0.980785,-0.923880,0.555570},{1.000000,-0.980785,0.923880,-0.831470,0.707107,-0.555570,0.382683,-0.195090}};

float c(int eps){
    if (eps == 0)
        return (1/sqrt(2));
    return 1;
}

int16_t** dct(int16_t** mat){
    int16_t** new_mat = malloc(8*sizeof(int16_t*));
    for (int k=0;k<=7;k++){
        new_mat[k] = malloc(8*sizeof(int16_t));
    }
    for (int i=0; i<=7;i++){
        for (int j=0; j<=7;j++){
            float somme_double = 0.0;
            for (int x=0; x<=7;x++){
                for (int y=0; y<=7;y++){
                    somme_double = somme_double + (float)(mat[x][y]-128)*cos_tab[x][i]*cos_tab[y][j];
                }
            }
            float resultat = (2./8) * c(i)*c(j) * somme_double;
            new_mat[i][j] = (int16_t) resultat;
        }
      
    }
    for (uint32_t i = 0 ; i < 8;i++) {
        free(mat[i]);
    }
    free(mat);
    return new_mat;
}