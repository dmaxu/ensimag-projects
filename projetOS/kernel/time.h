#include <stdint.h>
#include <stdbool.h>

extern uint32_t time;

// fonction traitant
void tic_PIT(void);

//fonction init
void init_traitant_IT(uint32_t num_IT, void (*traitant)(void));
void init_traitant_IT_user(uint32_t num_IT, void (*traitant)(void));

//envoie la fréquence de l'horloge
void init_horloge();

//masque ou demasque interruption
void masque_IRQ(uint32_t num_IRQ, bool masque);

//renvoie le nombre d'interruptions déjà passées depuis le démarrage du système
unsigned long current_clock();

//renvoie la fréquence du quartz et le nombre d'oscillations entre chaque interruption dans un tableau (Attention il faut free aprèes utilisation !)
uint32_t* clock_setting();

void clock_settings(unsigned long *quartz, unsigned long *ticks);