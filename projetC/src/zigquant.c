#include <stdlib.h>
#include <stdbool.h>
#include <stdint.h>
#include "qtables.h"
#include <stdio.h>

int16_t* zig_zag(int16_t** mat){
    int16_t* vect=malloc(64*sizeof(int16_t));
    int i=0, j=0;
    int cpt =1;
    vect[0]=mat[0][0];
        while(1){
            if (i==0){
                j++;
                vect[cpt++]=mat[i][j];
                while (j>0){
                    i++;
                    j--;
                    vect[cpt++]=mat[i][j];
                }
            }
            else if (j==0){
                i++;
                vect[cpt++]=mat[i][j];
                while (i>0){
                    i--;
                    j++;
                    vect[cpt++]=mat[i][j];
                }
            }
            if (j==0 && i==7){
                break;
            }
        }
        while(1){
        if (i==7){
            j++;
            vect[cpt++]=mat[i][j];
            while (j<7){
                j++;
                i--;
                vect[cpt++]=mat[i][j];
            }
        }
        else if (j==7){
            i++;
            vect[cpt++]=mat[i][j];
            while (i<7){
                i++;
                j--;
                vect[cpt++]=mat[i][j];                
            }
        }
        if (j==7 && i==7){
            break;
            }
        }
    for (uint8_t k = 0; k < 8; k++) {
        free(mat[k]);
    }
    free(mat);
    return vect;
}


int16_t* quant(int16_t* vect,bool chrom) {
    static uint8_t* tab;
    if (chrom) {
        tab = quantification_table_CbCr;
    } else {
        tab = quantification_table_Y;
    }
    int16_t* vect_quant=malloc(64*sizeof(int16_t));
    for (int i=0; i<64;i++) {
        vect_quant[i]=vect[i]/tab[i];
    }
    free(vect);
    return vect_quant;
}
