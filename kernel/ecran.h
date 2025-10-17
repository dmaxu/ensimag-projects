#include "stdint.h"

void efface_ecran(void);
void traite_car(char c);
void defilement(void);
void place_curseur(uint32_t lig, uint32_t col);
//affiche le temps en haut à droite de l'écran
void ecrit_temps(char *data, uint32_t taille);

uint32_t get_lig_globale();
uint32_t get_col_globale();
void set_lig_globale(uint32_t lig);
void set_col_globale(uint32_t col);

void console_putbytes(const char *s, int len);