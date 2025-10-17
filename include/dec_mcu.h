#include<stdlib.h>
#include<stdint.h>
#include "matrice.h"
/**
 * @brief Ouvre le fichier dont le nom est donné en paramètre, regarde si on a un ppm ou pgm
 * prend les dimensions de l'image, et va créer une matrice de pixels.
 *
 * @param input_filename le nom du fichier que l'on veut traiter
 * @return une matrice de pixel (int16_t) sous forme de tableau de tableau
 */
struct Matrice get_input_image(char* input_filename);

/**
 * @brief Fonction qui va créer un tableau de tableau (donc une matrice)
 * à partir des dimensions données en paramètres
 * (Cette fonction sera appelée par define matrice pour créer l'attribut .mat)
 *
 * @param hauteur la valeur de la hauteur de la matrice
 * @param largeur la valeur de la largeur de la matrice
 * @return Un pointeur vers un tableau de tableau (donc un pointeur vers une matrice)
 */
int16_t** initialise_matrix(uint16_t hauteur, uint16_t largeur);

/**
 * @brief Fonction qui va créer un tableau de tableau (donc une matrice)
 * à partir des dimensions données en paramètres contenant des structures Pixel
 * (Cette fonction sera appelée par define matrice_colored pour créer l'attribut .mat)
 *
 * @param hauteur la valeur de la hauteur de la matrice
 * @param largeur la valeur de la largeur de la matrice
 * @return Un pointeur vers un tableau de tableau de Pixel (donc un pointeur vers une matrice)
 */
struct Pixel** initialise_matrix_colored(uint16_t hauteur, uint16_t largeur);

/**
 * @brief Fonction qui va créer une structure matrice 
 *
 * @param hauteur un pointeur vers la valeur de la hauteur de la matrice
 * @param largeur un pointeur vers la valeur de la largeur de la matrice
 * @return une struct Matrice dont les attributs hauteur et largeur correspondent 
 * aux dimensions et l'attribut mat contient le tableau de tableau représentant la matrice
 */
struct Matrice define_matrix(uint16_t hauteur, uint16_t largeur);

/**
 * @brief Fonction qui va créer une structure matrice_Couleur 
 *
 * @param hauteur un pointeur vers la valeur de la hauteur de la matrice
 * @param largeur un pointeur vers la valeur de la largeur de la matrice
 * @return une struct Matrice dont les attributs hauteur et largeur correspondent 
 * aux dimensions et l'attribut mat contient le tableau de tableau de Pixel représentant la matrice
 */
struct Matrice_Couleur define_matrix_colored(uint16_t hauteur, uint16_t largeur);

/**
 * @brief Fonction qui va prendre en entrée une MCU et qui va renvoyer 
 * un tableau de structure Matrice de taille 3 contenant à l'indice 0 
 * la matrice Y, à l'indice 1 la matrice Cb et à l'indice 2 la matrice Cr
 *
 * @param matri une MCU de couleur (donc une matrice de structures Pixel)
 * @return un tableau de structure Matrice de taille 3 contenant les matrices 
 * Y, Cb et Cr
 */
struct Matrice* convert_to_YCbCr(struct Matrice_Couleur matri);

/**
 * @brief Fonction qui va ouvrir le fichier de données pgm ou ppm
 * et va regarder si il s'agit d'une image couleur ou non 
 *
 * @param input_filename un pointeur vers le nom du fichier à ouvrir
 * @return 5 si c'est une image noir et blanc, 6 si c'est une image couleur
 */
uint8_t check_color(char* input_filename);

/**
 * @brief Ouvre le fichier dont le nom est donné en paramètre, regarde si on a un ppm ou pgm
 * prend les dimensions de l'image, et va créer une matrice de structure Pixels.
 *
 * @param input_filename le nom du fichier que l'on veut traiter
 * @return une matrice de Pixel (triplé de int16_t) sous forme de tableau de tableau
 */
struct Matrice_Couleur get_input_image_colored(char* input_filename);

/**
 * @brief Fonction qui va compléter une matrice jusqu'à ce qu'elle
 * ait un nombre de ligne qui soit multiple de la hauteur d'une MCU
 *
 * @param matrix un pointeur vers un structure Matrice 
 * (donc vers un tableau de tableau de int16_t)
 * @return une matrice de pixel (int16_t) sous forme de tableau de tableau
 */
void add_line(struct Matrice* matrix);

/**
 * @brief Fonction qui va compléter une matrice couleur jusqu'à ce qu'elle
 * ait un nombre de ligne qui soit multiple de la hauteur d'une MCU
 *
 * @param matrix un pointeur vers un structure Matrice 
 * (donc vers un tableau de tableau de int16_t)
 * @param h_mcu la hauteur d'une MCU
 * @return une matrice de Pixel (triplé de int16_t) sous forme de tableau de tableau
 */
void add_line_colored(struct Matrice_Couleur* matrix,int h_mcu);

/**
 * @brief Fonction qui va compléter une matrice jusqu'à ce qu'elle
 * ait un nombre de colonne qui soit multiple de la largeur d'une MCU
 *
 * @param matrix un pointeur vers un structure Matrice 
 * (donc vers un tableau de tableau de int16_t)
 * @return une matrice de pixel (int16_t) sous forme de tableau de tableau
 */
void add_column(struct Matrice* matrix);

/**
 * @brief Fonction qui va compléter une matrice couleur jusqu'à ce qu'elle
 * ait un nombre de colonne qui soit multiple de la largeur d'une MCU
 *
 * @param matrix un pointeur vers un structure Matrice 
 * (donc vers un tableau de tableau de int16_t)
 * @param l_mcu la largeur d'une MCU
 * @return une matrice de Pixel (triplé de int16_t) sous forme de tableau de tableau
 */
void add_column_colored(struct Matrice_Couleur* matrix, int l_mcu);

/**
 * @brief Fonction qui va prendre en entré une matrice et va la découper en une succession
 * de matice dont les dimensions sont données en paramètres par h_mcu et l_mcu 
 *
 * @param matrix un pointeur vers un structure Matrice 
 * (donc vers un tableau de tableau de int16_t)
 * @param ptr_nb_mcu un pointeur qui pointe vers le nombre de MCU
 * @param h_mcu la hauteur d'une MCU
 * @param l_mcu la largeur d'une MCU
 * @return un tableau de structure Matrice qui sont en fait des MCU de la taille demandée
 */
struct Matrice* MCU_dividing(struct Matrice* matrix,int h_mcu, int l_mcu);

/**
 * @brief Fonction qui va prendre en entré une matrice en couleur et va la découper en une succession
 * de matice dont les dimensions sont données en paramètres par h_mcu et l_mcu 
 * (homologue de MCU_dividing mais pour les images couleurs)
 *
 * @param matrix un pointeur vers un structure Matrice 
 * (donc vers un tableau de tableau de int16_t)
 * @param ptr_nb_mcu un pointeur qui pointe vers le nombre de MCU
 * @param h_mcu la hauteur d'une MCU
 * @param l_mcu la largeur d'une MCU
 * @return un tableau de structure Matrice_Couleur qui sont en fait des MCU de la taille demandée
 */
struct Matrice_Couleur* MCU_dividing_colored(struct Matrice_Couleur* matrix,int h_mcu, int l_mcu);