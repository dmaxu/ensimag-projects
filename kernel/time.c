#include "cpu.h"
#include "string.h"
#include "stdbool.h"
#include "ecran.h"
#include "process.h"
#include "segment.h"

#define CLOCKFREQ 50 // fréquence de l'horloge (en Hz)
#define QUARTZ 0x1234DD // fréquence du circuit (~1,19 MHz)
#define IMPULSION QUARTZ / CLOCKFREQ

uint32_t time = 0;
uint8_t iter = 0;
uint32_t nbInterupFromStart = 0;

void tic_PIT(void)
{
    // signalement controleur interruption
    outb(0x20, 0x20);
    iter++;
    nbInterupFromStart++;
    if (iter >= CLOCKFREQ)
    {
        ordonnanceur();
        time++;
        char str_time[15];
        uint32_t hours = time / 3600;
        uint32_t minutes = (time % 3600) / 60;
        uint32_t seconds = time % 60;

        // Format the string
        sprintf(str_time, "%02d:%02d:%02d", hours, minutes, seconds);

        ecrit_temps(str_time, 8);
        iter = 0;
    }
}

void init_traitant_IT(uint32_t num_IT, void (*traitant)(void))
{
    uint32_t *idt1 = (uint32_t *)(0x1000 | 8 * num_IT);
    // 16 bits de poids faibles de l’adresse du traitant et les 16 bits du KERNEL_CS
    *idt1 = (uint32_t)(KERNEL_CS << 16) | ((uint32_t)traitant & 0x0000FFFF);
    // 16 bits de poids forts de l’adresse du traitant et la constante 0x8E00
    uint32_t *idt2 = (uint32_t *)(0x1000 | (8 * num_IT + 4));
    *idt2 = ((uint32_t)traitant & 0xFFFF0000) | (uint32_t)0x8E00;
}

void init_traitant_IT_user(uint32_t num_IT, void (*traitant)(void)) {
    uint32_t *idt1 = (uint32_t *)(0x1000 | 8 * num_IT);
    *idt1 = (KERNEL_CS << 16) | ((uint32_t)traitant & 0xFFFF);

    uint32_t *idt2 = (uint32_t *)(0x1000 | (8 * num_IT + 4));
    *idt2 = ((uint32_t)traitant & 0xFFFF0000) | 0xEE00;  // DPL = 3
}

void init_horloge()
{
    outb(0x34, 0x43);
    outb(IMPULSION % 256, 0x40);
    outb((IMPULSION >> 8), 0x40);
}

void masque_IRQ(uint32_t num_IRQ, bool masque)
{
    uint8_t tab = inb(0x21);
    if (masque)
    {
        // Masquer l'IRQ (mettre le bit correspondant à 1)
        tab |= (1 << num_IRQ);
    }
    else
    {
        // Démasquer l'IRQ (mettre le bit correspondant à 0)
        tab &= ~(1 << num_IRQ);
    }

    // Écrire le nouveau masque dans le port 0x21
    outb(tab, 0x21);
}

unsigned long current_clock(){
    return nbInterupFromStart;
}


void clock_settings(unsigned long *quartz, unsigned long *ticks) {
    if (quartz) {
        *quartz = QUARTZ;    
    }
    if (ticks) {
        *ticks = IMPULSION;  
    }
}


// uint32_t* clock_setting() {
//     // uint32_t* settings = (uint32_t*)malloc(2 * sizeof(uint32_t)); // Allocate memory for 2 uint32_t values
//     // if (!settings) {
//     //     return NULL; 
//     // }
//     // settings[0] = IMPULSION;
//     // settings[1] = CLOCKFREQ;
//     // return settings;
// }