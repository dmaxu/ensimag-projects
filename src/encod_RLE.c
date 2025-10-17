#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <math.h>
#include <string.h>
#include <stdbool.h>
#include "huffdic.h"
#include "entete.h"


int cherche_ind_magn(int16_t val,int16_t magn) {
    if (val>=0) {
        return val;
    }
    return pow(2,magn)+val-1;
}

char* dec_to_bin(int dec) {
    if (dec == 0) {
        char* bin = malloc(2);
        if (bin) {
            bin[0] = '0';
            bin[1] = '\0';
        }
        return bin;
    }

    int i=0;
    int k=0;
    char* bin=malloc(33);
    while(dec>0) {

        bin[i]='0'+dec%2; /* il faut mettre le '0' + car permet de transformer le chiffre en son carac ASCII*/
        dec=dec/2;
        i++;
    }
    bin[i]='\0';


    char* bin_rev=malloc(i+1);
    i--;
    while(i>=0) {
        bin_rev[k]=bin[i];
        i--;
        k++;
    }

    bin_rev[k]='\0';
    free(bin);
    return bin_rev;
}

char* dec_to_binmagn(int dec,int magn) {
    char* bin_magn=malloc(2*magn+1);
    char* bin1=dec_to_bin(dec);
    int nb_add_0 = magn -strlen(bin1);
    int index = 0;
    for (int i=0;i<nb_add_0;i++) {
        bin_magn[i]='0';
        index++;
    }
    int loop_max = fmin(strlen(bin1),magn);
    for (int i=0;i<loop_max;i++) {
        bin_magn[index++]=bin1[i];
    }
    bin_magn[index]='\0';
    free(bin1);
    return bin_magn;
}

char* concat(char* x, char* y) {
    char* res=malloc(strlen(x)+strlen(y)+1);
    strcpy(res,x); /*copie de x dans res*/
    strcat(res,y); /*puis concatenation*/
    return res;
}

char* RLE(int16_t mot,int cpt,bool dc,bool isY) {
    int a = mot;
    int k=mot;
    int magn=0;
    char** tab_AC;
    char** tab_DC;
    if (isY){
        tab_AC = symbols_AC_Y;
        tab_DC = symbols_DC_Y;
    }
    else {
        tab_AC = symbols_AC_CbCr;
        tab_DC = symbols_DC_CbCr;
    }
    while (k!=0) {
        k/=2;
        magn++;
    }
    int truc=cherche_ind_magn(a,magn);
    char* z=dec_to_binmagn(truc,magn);
    if (!dc){
        char* y=dec_to_binmagn(magn,4);
        char* x=dec_to_bin(cpt);
        char hex[10];
        char* hex_string = concat(x,y);
        sprintf(hex,"%lX",strtol(hex_string,NULL,2));
        free(hex_string);
        char* code_h_magn=tab_AC[strtol(hex,NULL,16)];
        free(x);
        free(y);
        char* result = concat(code_h_magn,z);
        free(z);
        return result;
    } else {
        char* code_h_magn=tab_DC[magn];
        char* result = concat(code_h_magn,z);
        free(z);
        return result;
    }
}

void copy(char* str,size_t start,char* buffer){
    size_t id_str = 0;
    size_t id_buffer;
    for(size_t i=start;i<=(start+strlen(str)-1);i++){
        id_buffer = i % 64;
        buffer[id_buffer] = str[id_str];
        id_str++;
    }
}

void parcours_RLE(int16_t* vecteur_quant,FILE *file,char* buffer,bool end,size_t* capacity_ptr,size_t* start_ptr,size_t* empty_case_ptr,char* forgotten_char,bool isY) {

    size_t capacity = *capacity_ptr;
    size_t empty_case = *empty_case_ptr;
    size_t start = *start_ptr;
    size_t cpt = 0;
    char hexChars[] = "0123456789ABCDEF";
    char last_hex = ' ';
    char current_hex = ' ';
    char hexcode[1000] = {'\0'};
    size_t hex_id = 0;
    if (forgotten_char[0] != ' '){
        hexcode[0] = forgotten_char[0];
        hex_id++;
    }
    char* stock_t = RLE(vecteur_quant[0],0,true,isY);
    copy(stock_t,empty_case,buffer);
    size_t len = strlen(stock_t);
    free(stock_t);
    capacity = capacity + len;
    empty_case = (empty_case + len)%64;
    for(int i=1;i<64;i++){
        if (vecteur_quant[i]==0){
            cpt++;
            
        }
        else {
            // 16 0 à la suite F0
            while (cpt >= 16) {
                int16_t value = 0x00F0;
                if (isY){
                    copy("11111111001",empty_case,buffer);
                    len = strlen("11111111001"); 
                }
                else{
                    copy(symbols_AC_CbCr[value],empty_case,buffer);
                    len = strlen(symbols_AC_CbCr[value]); 
                }

                capacity = capacity + len;
                empty_case = (empty_case + len)%64;
                cpt = cpt - 16;
            }
            char* stock_temp = RLE(vecteur_quant[i],cpt,false,isY);
            copy(stock_temp,empty_case,buffer);
            len = strlen(stock_temp);
            free(stock_temp);
            capacity = capacity + len;
            empty_case = (empty_case + len)%64;
            cpt = 0;
            while(capacity>=4){
                int value = 0;
                for (size_t k = start; k <(start+4); k++) {
                    k = k % 64;
                    value = value*2+(int)(buffer[k]-'0'); 
                }
                current_hex = hexChars[value];
                hexcode[hex_id] = current_hex;
                hex_id = hex_id + 1;
                //Byte stuffing
                if ((current_hex=='F')&&(last_hex=='F')&&(hex_id%2==0)) {
                    hexcode[hex_id] = '0';
                    hexcode[hex_id+1] = '0';
                    last_hex = '0';
                    hex_id = hex_id + 2;
                }
                else
                    last_hex = current_hex;
                capacity = capacity - 4;
                start = (start + 4)%64; 
            }
        }
    }
    //End of block
    if (cpt>0){
        char* stock_temp = RLE(0,0,false,isY);
        copy(stock_temp,empty_case,buffer);
        // len = strlen(RLE(0,0,false,isY));
        len = strlen(stock_temp);
        free(stock_temp);
        capacity = capacity + len;
        empty_case = (empty_case + len)%64;
        while(capacity>=4){
            int value = 0;
            for (size_t k = start; k <(start+4); k++) {
                k = k % 64;
                value = value*2+(int)(buffer[k]-'0'); 
            }
            current_hex = hexChars[value];
            hexcode[hex_id] = current_hex;
            hex_id = hex_id + 1;
            //Byte stuffing
            if ((current_hex=='F')&&(last_hex=='F')&&(hex_id%2==0)) {
                    hexcode[hex_id] = '0';
                    hexcode[hex_id+1] = '0';
                    last_hex = '0';
                    hex_id = hex_id + 2;
                }
                else
                    last_hex = current_hex;
            capacity = capacity - 4;
            start = (start + 4)%64; 

            
        }
    }
    if (end){
        //compléter la fin avec des 0
        size_t locked_capacity = capacity ;
        for(size_t i=locked_capacity;i<locked_capacity+3;i++){
            size_t idx = (start+i)%64;
            buffer[idx] = '0';
            capacity++;
        }
        while(capacity>=4){
            int value = 0;
            for (size_t k = start; k <(start+4); k++) {
                k = k % 64;
                value = value*2+(int)(buffer[k]-'0'); 
            }
            current_hex = hexChars[value];
            hexcode[hex_id] = current_hex;
            hex_id = hex_id + 1;
            capacity = capacity - 4;
            start = (start + 4)%64; 
        }
        size_t hexcode_len = strlen(hexcode);
        if (hexcode_len%2 == 1){
            //extension avec 0 si pas paire
            hexcode[hexcode_len] = '0';
        }
    }  
    //cas pour octet seul à la fin
    size_t hexcode_len = strlen(hexcode);
    if (hexcode_len%2 == 1){
        forgotten_char[0] = hexcode[hexcode_len-1];
    }
    else {
        forgotten_char[0] = ' ';
    }
    affiche(hexcode,file);
    *capacity_ptr = capacity;
    *empty_case_ptr = empty_case;
    *start_ptr = start;
}