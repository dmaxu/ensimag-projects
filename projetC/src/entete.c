#include <stdint.h>
#include <stdio.h>
#include "entete.h"
#include "qtables.h"
#include "htables.h"
#include <string.h>
#include <stdlib.h>
#include "encod_RLE.h"


void affiche(char* str,FILE *file){
    uint8_t nb;
    char sub_str[3];
    sub_str[2] = '\0';
    int len = strlen(str);
    for(int i=0;i<len-1;i=i+2){
        sub_str[0] = str[i];
        sub_str[1] = str[i+1];
        nb = strtol(sub_str,NULL,16);
        fwrite(&nb,1,1,file);
    }

}



void debut_et_app(FILE *file){
    //affiche balise start of image et balise APPx
    char* str = "FFD8FFE000104A46494600010100000000000000";
    affiche(str,file);
}

void affiche_tab(uint8_t* tab,size_t len,FILE *file){
    //affiche table de quantification (taille 64 par défaut)
    for (size_t i=0;i<len;i++){
        fwrite(&tab[i],1,1,file);
    }
}

void entete_p5(uint16_t largeur, uint16_t hauteur,FILE *file){
    //i_C = 0 pour Y
    debut_et_app(file);
    //define quantization table (précision 8bit par défaut) et table d'indice 0
    affiche("FFFE00103C33206C652070726F6A65742043",file);//commentaire
    affiche("FFDB004300",file);
 
    affiche_tab(quantification_table_Y,64,file);
    //start of frame
    affiche("FFC0000B08",file);
    // fwrite(&hauteur,2,1,file);
    // fwrite(&largeur,2,1,file);
    char str_hauteur[5];
    char str_largeur[5];
    sprintf(str_hauteur,"%04x",hauteur);
    sprintf(str_largeur,"%04x",largeur);
    affiche(str_hauteur,file);
    affiche(str_largeur,file);
    affiche("01011100",file);
    //define huffman table DC
    affiche("FFC4001F00",file); 
    affiche_tab(htables_nb_symb_per_lengths[0][0],16,file);
    affiche_tab(htables_symbols[0][0],htables_nb_symbols[0][0],file);
    //define huffman table AC   
    affiche("FFC400B510",file);
    affiche_tab(htables_nb_symb_per_lengths[1][0],16,file);
    affiche_tab(htables_symbols[1][0],htables_nb_symbols[1][0],file);
    //define start of scan
    affiche("FFDA0008010100003F00",file);

}

void entete_p6(uint16_t largeur, uint16_t hauteur,FILE *file,char* fct_echant){
    debut_et_app(file);
    affiche("FFFE00103C33206C652070726F6A65742043",file);//commentaire
    //table de quantification de Y
    affiche("FFDB004300",file);
    affiche_tab(quantification_table_Y,64,file);
    //table de quantification de CbCr
    affiche("FFDB004301",file);
    affiche_tab(quantification_table_CbCr,64,file);
    //start of frame
    affiche("FFC0001108",file);
    char str_hauteur[5];
    char str_largeur[5];
    sprintf(str_hauteur,"%04x",hauteur);
    sprintf(str_largeur,"%04x",largeur);
    affiche(str_hauteur,file);
    affiche(str_largeur,file);
    affiche("03",file);
    affiche("01",file);
    char str [3];
    str[0] = fct_echant[0];
    str[1] = fct_echant[2];
    str[2] = '\0';
    affiche(str,file);
    affiche("00",file);
    affiche("02",file);
    str[0] = fct_echant[4];
    str[1] = fct_echant[6];
    str[2] = '\0';
    affiche(str,file);
    affiche("01",file);
    affiche("03",file);
    str[0] = fct_echant[8];
    str[1] = fct_echant[10];
    str[2] = '\0';
    affiche(str,file);
    affiche("01",file);
    //define huffman table Y DC
    affiche("FFC4001F00",file); 
    affiche_tab(htables_nb_symb_per_lengths[0][0],16,file);
    affiche_tab(htables_symbols[0][0],htables_nb_symbols[0][0],file);
    //define huffman table Y AC   
    affiche("FFC400B510",file);
    affiche_tab(htables_nb_symb_per_lengths[1][0],16,file);
    affiche_tab(htables_symbols[1][0],htables_nb_symbols[1][0],file);
    //define huffman table CbCr DC
    affiche("FFC4001F01",file); 
    affiche_tab(htables_nb_symb_per_lengths[0][1],16,file);
    affiche_tab(htables_symbols[0][1],htables_nb_symbols[0][1],file);
    //define huffman table CbCr AC
    affiche("FFC400B511",file);
    affiche_tab(htables_nb_symb_per_lengths[1][1],16,file);
    affiche_tab(htables_symbols[1][1],htables_nb_symbols[1][1],file);
    //define start of scan
    affiche("FFDA000C03010002110311003F00",file);
}

void balise_fin(FILE *file) {
    affiche("FFD9",file);
}
