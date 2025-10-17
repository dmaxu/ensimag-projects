#include "stdint.h"
#include "string.h"
#include "cpu.h"
#include "console.h"

#define adresse_memoire_video 0xB8000

uint32_t lig_globale = 0;
uint32_t col_globale = 0;

uint32_t get_lig_globale()
{
    return lig_globale;
}
uint32_t get_col_globale()
{
    return col_globale;
}
void set_lig_globale(uint32_t lig)
{
    lig_globale = lig;
}
void set_col_globale(uint32_t col)
{
    col_globale = col;
}


uint16_t *ptr_mem(uint32_t lig, uint32_t col)
{
    return (uint16_t *)(0xB8000 + 2 * (lig * 80 + col));
}

void ecrit_car(uint32_t lig, uint32_t col, char c, uint8_t couleur_c, uint8_t couleur_fond, uint8_t clignote)
{
    // on peut ajouter des conditions if sur les parametre pour eviter les erreurs
    uint16_t *dest = ptr_mem(lig, col);
    // memset((void *)dest, c, 1);
    uint8_t deuxieme_octet_couleur = (clignote << 7) | (couleur_fond << 4) | couleur_c;
    dest[0] = c | deuxieme_octet_couleur << 8;
    // memset((void *)(dest+1), deuxieme_octet_couleur, 1);
}

/*
void efface_ecran(void)
{
    uint16_t *tableau_memoire = (uint16_t *)0xB8000;
    for (int i = 0; i < 2 * (25 * 80); i += 2)
    {
        tableau_memoire[i] = ' ';
        tableau_memoire[i + 1] = 0;
    }
    lig_globale = 0;
    col_globale = 0;
}
*/

void place_curseur(uint32_t lig, uint32_t col)
{
    uint16_t pos = (uint16_t)(lig * 80 + col);
    outb(0x0F, 0x3D4);
    outb((uint8_t)pos, 0x3D5);
    outb(0x0E, 0x3D4);
    outb((uint8_t)(pos >> 8), 0x3D5);
}



void efface_ecran(void)
{
    uint16_t *tableau_memoire = (uint16_t *)0xB8000;
    for (int i = 0; i < (25 * 80); i++)
    {
        tableau_memoire[i] = (0x07 << 8) | ' '; 
    }
    lig_globale = 0;
    col_globale = 0;
    place_curseur(0, 0);
}





void defilement()
{
    memmove((void *)0xB8000, (const void *)(0xB8000 + (80 * 2)), 8 * 25 * 79);
}
void traite_car(char c)
{
    if (c >= 0)
    {
        switch (c)
        {
        case '\b':
            if (col_globale != 0)
            {
                col_globale -= 1;
            }
            break;
        case '\n':
            // done

            col_globale = 0;
            if (lig_globale == 24)
            {
                defilement();
            }
            else
            {
                lig_globale += 1;
            }
            // place_curseur(lig_globale, col_globale-1);

            break;
        case '\f':
            // done
            col_globale = 0;
            lig_globale = 0;
            break;
        case '\r':
            // done
            col_globale = 0;
            break;
        case '\t':
            col_globale = ((col_globale / 8) + 1) * 8;
            break;
        default:
            // done
            ecrit_car(lig_globale, col_globale, c, 15, 0, 0);
            if (col_globale == 79)
            {
                col_globale = 0;
                if (lig_globale == 24)
                {
                    defilement();
                }
                else
                {
                    lig_globale += 1;
                }
            }
            else
            {
                col_globale++;
            }
            break;
        }
        place_curseur(lig_globale, col_globale);
    }
}

void console_putbytes(const char *s, int len)
{
    // efface_ecran();

    // lig_globale=0;
    // col_globale=0;
    for (int i = 0; i < len; i++)
    {
        traite_car(s[i]);
    }
}

// pareil que console_putbytes
void cons_write(const char *str, unsigned long size){
    console_putbytes(str, size);
}

void ecrit_temps(char *data, uint32_t taille)
{
    for (int i = 80 - taille - 1; i < 80; i++)
    {
        ecrit_car(0, i, data[i - 80 + taille + 1], 15, 0, 0);
    }
}