#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include "dec_mcu.h"
#include "matrice.h"
#include <stdio.h>
#include "ss_echant.h"


Cell* new_cel(struct Matrice mat,uint8_t Y_Cb_Cr) {
    Cell* new_cel = malloc(sizeof(Cell));
    new_cel->mat = mat;
    new_cel->suiv = NULL;
    new_cel->Y_Cb_Cr = Y_Cb_Cr;
    
    return new_cel;
}



struct Matrice* ss_echant420(int16_t** mat,int16_t n,int16_t m,char* ss_echant){

    struct Matrice mat_complete = define_matrix(8,8);
    int col=0,l=0,ligne=0,colonne=0;
    if (ss_echant[0]==ss_echant[2]||ss_echant[0]>ss_echant[2]){
        while (l<8){
            col=0;
            for (int i=0;i<m-1;i+=(m/8)){
                uint32_t somme_l1=0;
                uint32_t somme_l2=0;
                for (int j=0;j<(m/8);j++){
                    somme_l1+=mat[ligne][i+j];
                    somme_l2+=mat[ligne+1][i+j];
                }
                mat_complete.mat[l][col]=(somme_l1+somme_l2)/((m/8)*(n/8));
                col++;
            }
            ligne+=(n/8);
            l++;
        }
    }else {
        while (col<8){
            l=0;
            for (int i=0;i<n-1;i+=(n/8)){
                uint32_t somme_c1=0;
                uint32_t somme_c2=0;
                for (int j=0;j<(n/8);j++){
                    somme_c1+=mat[i+j][colonne];
                    somme_c2+=mat[i+j][colonne+1];
                }
                mat_complete.mat[l][col]=(somme_c1+somme_c2)/((m/8)*(n/8));
                l++;
            }
            colonne+=(m/8);
            col++;
        }
    }

    struct Matrice* tab=(struct Matrice*)malloc(sizeof(struct Matrice));
    tab[0]=mat_complete;

    return tab;
}


struct Matrice* ss_echant_hori(int16_t** mat,int16_t n,int16_t m,char* ss_echant){
    
    uint8_t diviser;
    if((uint8_t)(ss_echant[4]-'0')!=1){ diviser=(uint8_t)(ss_echant[4]-'0');}
    else{  diviser=(uint8_t)(ss_echant[0]-'0');}

    struct Matrice mat_complete=define_matrix(n,m/diviser);
    int col=0,l=0,somme=0;
    while (l<n){
        col=0;
        for (int i=0;i<m;i+=diviser){
            somme=0;
            for(int j=0;j<diviser;j++){
                somme+=mat[l][i+j];
            }
            mat_complete.mat[l][col]=somme/diviser;
            col++; 
        }
        l++;
    }
    struct Matrice* tab=MCU_dividing(&mat_complete,8,8);
    libere_matrice(mat_complete);
    return tab;
}   

struct Matrice* ss_echant_vert(int16_t** mat,int16_t n,int16_t m,char* ss_echant){
    uint8_t diviser;
    if((uint8_t)(ss_echant[6]-'0')!=1){ diviser=(uint8_t)(ss_echant[6]-'0');}
    else{  diviser=(uint8_t)(ss_echant[2]-'0');}

    struct Matrice mat_complete = define_matrix(n/diviser,m);
    int col=0,l=0,somme=0;
    while (col<m){
        l=0;
        for (int i=0;i<n;i+=diviser){
            somme=0;
            for(int j=0;j<diviser;j++){
                somme+=mat[i+j][col];
            }
            mat_complete.mat[l][col]=somme/diviser;
            l++; 
        }
        col++;
    }
    struct Matrice* tab=MCU_dividing(&mat_complete,8,8);
    libere_matrice(mat_complete);
    return tab;
}   


uint8_t detect_ssechant(char* ss_echant){
  
    /*boucle qui permet de stocker dans un tableau les valeurs du sous echant*/
    uint8_t tab[6];
    uint8_t cpt=0;
    for (int i=0;i<11;i+=2){
        tab[cpt++]=(uint8_t) (ss_echant[i]-'0');/*bien rajouter le 0 pour ne pas avoir le code ASCII*/
    }

    /*Vérification des conditions :*/
    
    /*La valeur de chaque facteur h ou v doit être comprise entre 1 et 4*/
    for (int i=0;i<6;i++){
        if (tab[i]<1||tab[i]>4){
            printf("Invalid value for the --sample argument : sampling factors should verify 1<h<4 and 1<v<4 \n");
            return 0;
        }
    }
    /*La somme des produits hi×vi doit être inférieure ou égale à 10*/
    uint16_t somme=0;
    for (int i=0;i<6;i+=2){
        
        somme+=tab[i]*tab[i+1];
    }
    if (somme>10){
        printf("Invalid value for the --sample argument : sum(hi*vi) should be <= 10 \n");
        return 0;
    }
    
    /*Les facteurs d'échantillonnage des chrominances doivent diviser parfaitement ceux de la luminance*/
    for (int i=0;i<2;i+=1){
        if (tab[i]%tab[i+2]>0||tab[i]%tab[i+4]>0){
            printf("Invalid value for the --sample argument : Y/Cb and Y/Cr sampling factors should divide perfectly \n");
            return 0;
        }
    }

    /*detection des sous echant*/
    /*1 = hori*/
    if ((tab[0]>tab[1]||tab[0]==tab[1])&&tab[0]>tab[2]&&tab[0]>tab[4]&&tab[1]==tab[3]&&tab[1]==tab[5]){
        return 1;
    }
    /*2=vert*/
    else if ((tab[1]>tab[0]||tab[1]==tab[0])&&tab[1]>tab[3]&&tab[1]>tab[5]&&tab[0]==tab[2]&&tab[0]==tab[4]){
        return 2;
    }
    /*3=vert et hori*/
    else if (tab[0]>tab[2]&&tab[0]>tab[4]&&tab[1]>tab[3]&&tab[1]>tab[5]){
        return 3;
    }
    /*pas de sous echant*/
    else if (tab[0]==tab[2]&&tab[0]==tab[4]&&tab[1]==tab[3]&&tab[1]==tab[5]){
        return 4;
    }
    else {
        return 0;
    }
}   

uint8_t* taille_Y_Cb_Cr(char* ss_echant){
    uint8_t tab[6];
    uint8_t cpt=0;
    for (int i=0;i<11;i+=2){
        tab[cpt++]=(uint8_t) (ss_echant[i]-'0');/*bien rajouter le 0 pour ne pas avoir le code ASCII*/
    }
    uint8_t nb_Y=tab[0]*tab[1];
    uint8_t nb_Cb=tab[2]*tab[3];
    uint8_t nb_Cr=tab[4]*tab[5];

    uint8_t* res=malloc(3*sizeof(uint8_t));

    res[0]=nb_Y;
    res[1]=nb_Cb;
    res[2]=nb_Cr;
    
    return res;
}

Cell* lc_ssechant(struct Matrice* tab,char* ss_echant){

    /*tab est un tableau de structure matrice [Y,Cb,Cr]*/
    Cell* lc = malloc(sizeof(Cell));
    /*curr qui va permettre de parcourir la liste chainée*/
    Cell* curr=lc;
    /*besoin de la taille pour init is_Y*/
    uint8_t* taille_mat=taille_Y_Cb_Cr(ss_echant);
    uint8_t type_ss_echant=detect_ssechant(ss_echant); /*rajouter erreur si = 0*/
    struct Matrice* tab_88;
    if (type_ss_echant==4) {
        for (int i=0;i<3;i++){
            tab_88=MCU_dividing(&tab[i],8,8);
            
            for(int j=0;j<taille_mat[i];j++){
            //     /*on connait la taille grâce au ssechant donc on peut bien parcourir*/
                curr->suiv=new_cel(tab_88[j],i);
                curr=curr->suiv;
            }

            free(tab_88);
        }
        free(taille_mat);
        Cell* ptr_to_head=lc->suiv;
        free(lc);
        return ptr_to_head;
    }
    /*1 = hori*/
    else if (type_ss_echant==1){
        for (int i=0;i<3;i++){
            if (i==0){
                tab_88=MCU_dividing(&tab[i],8,8);
            } 
            else{
                tab_88=ss_echant_hori(tab[i].mat,tab[i].hauteur,tab[i].largeur,ss_echant);
            }

            for(int j=0;j<taille_mat[i];j++){
                /*on connait la taille grâce au ssechant donc on peut bien parcourir*/
                curr->suiv=new_cel(tab_88[j],i);
                curr=curr->suiv;
            }

            free(tab_88);
        }
        free(taille_mat);
        Cell* ptr_to_head=lc->suiv;
        free(lc);
        return ptr_to_head;
    }
    /*2 = vert*/
    else if (type_ss_echant==2){
         for (int i=0;i<3;i++){
            if (i==0){
                tab_88=MCU_dividing(&tab[i],8,8);
            } 
            else{
                tab_88=ss_echant_vert(tab[i].mat,tab[i].hauteur,tab[i].largeur,ss_echant);
            }

            for(int j=0;j<taille_mat[i];j++){
                /*on connait la taille grâce au ssechant donc on peut bien parcourir*/
                curr->suiv=new_cel(tab_88[j],i);
                curr=curr->suiv;
            }

            free(tab_88);
        }
        free(taille_mat);
        Cell* ptr_to_head=lc->suiv;
        free(lc);
        return ptr_to_head;
    }
    /*3 = hori et vert*/
    else if (type_ss_echant==3){
         for (int i=0;i<3;i++){
            if (i==0){
                tab_88=MCU_dividing(&tab[i],8,8);
            } 
            else{
                tab_88=ss_echant420(tab[i].mat,tab[i].hauteur,tab[i].largeur,ss_echant);
            }

            for(int j=0;j<taille_mat[i];j++){
                /*on connait la taille grâce au ssechant donc on peut bien parcourir*/
                curr->suiv=new_cel(tab_88[j],i);
                curr=curr->suiv;

            }

            free(tab_88);
        }
        free(taille_mat);
        Cell* ptr_to_head=lc->suiv;
        free(lc);
        return ptr_to_head;
    }

    /*error mauvais sous echant*/
    else {
        return lc;
    }
}

uint8_t* taille_dec_mat(char* ss_echant){
    /*donne les dim [n,m] nécessaire pour un sous échant donné*/
    uint8_t* tab=malloc(2*sizeof(uint8_t));
    tab[0]=(uint8_t) (ss_echant[2]-'0')*8;
    tab[1]=(uint8_t) (ss_echant[0]-'0')*8;
    return tab;
} 

