#include "cpu.h"
#include "string.h"
#include "process.h"
#include "stdio.h"
#include "queue.h"
#include "file.h"
#include "physmem.h"
#include "userspace_apps.h"
#include "hash.h"

// External declaration for kernel page directory
extern unsigned int pgdir[];

// Constants for memory management
#define USER_STACK_BASE 0xC0000000  // Starting address for user stacks
#define PAGE_SIZE 4096
#define PAGE_TABLE_ENTRIES 1024
#define KERNEL_ENTRIES 64  // First 64 entries (256MB) for kernel

struct list_link list_process_activable;
struct list_link list_process_zombie;
struct list_link list_process_endormi;


process *processus_courant;
process **process_tab = NULL;
//int exit_quit = 0;

static hash_t apps_htable;
static int apps_htable_initialized = 0;
extern const struct uapps symbols_table[];

void free_process(process *proc)
{
    if (proc != NULL)
    {
        // Handle memory cleanup for user processes
        if (proc->user_mode && proc->cr3) {
            unsigned int* page_dir = (unsigned int*)proc->cr3;
            
            // Clean up user space entries (after KERNEL_ENTRIES)
            for (int i = KERNEL_ENTRIES; i < PAGE_TABLE_ENTRIES; i++) {
                if (page_dir[i] & 1) { // If present
                    unsigned int* page_table = (unsigned int*)(page_dir[i] & ~0xFFF);
                    
                    // Free all pages in this table
                    for (int j = 0; j < PAGE_TABLE_ENTRIES; j++) {
                        if (page_table[j] & 1) { // If present
                            void* page = (void*)(page_table[j] & ~0xFFF);
                            physmem_free_page(page);  // Use physmem_free_page instead of mem_free
                        }
                    }
                    
                    // Free the page table itself
                    physmem_free_page(page_table);  // Use physmem_free_page instead of mem_free
                }
            }
            
            // Free the page directory
            physmem_free_page(page_dir);  // Use physmem_free_page instead of mem_free
        } else {
            // Original kernel process cleanup
            mem_free(proc->pile_exec_process, sizeof(unsigned long) * proc->ssize);
        }
        
        process_tab[proc->pid] = NULL;
        mem_free(proc, sizeof(process));
    }
}

/**
 * Returns the first free index in process_tab
 * @returns index of first free element and -1 otherwise
 */
int find_free_spot_process_tab()
{
    for (int i = 0; i < NBPROC; i++)
    {
        if (process_tab[i] == NULL)
        {
            /*if(i==1){
                return 100;
            }*/
            return i;
        }
    }
    return -1;
}



int start(int (*pt_func)(void *), unsigned long ssize, int prio, const char *name, void *arg)
{

    // STEP 1 : GET A PID FOR THE CREATED PROCESS AND VERIFY INPUTS

    if (prio <= 0 || prio > MAXPRIO)
    {
        printf("priorié invalide");
        return -1;
    }

    int current_pid = find_free_spot_process_tab();
    if (current_pid == -1)
    {
        printf("Pas de place disponible pour créer un nouveau processus");
        return -1;
    }

    // STEP 2 : CREATE STRUCTURE FOR THE PROCESS


    // Verify ssize parameter
    if (ssize >= 0xffffffff - 3) {
        return -1; 
    }

    ssize = ssize + 179; // add size to store arg fin_processis pointer and pt_func and enough size so that there's no problem later in each process due to ordonnanceur calls for example

    process *created_process = mem_alloc(sizeof(process));

    if(!created_process){
        return -1;
    }
    
    created_process->pile_exec_process = mem_alloc(ssize * sizeof(unsigned long));

    if(!created_process->pile_exec_process){
        mem_free(created_process,sizeof(process));
        return -1;
    }

    strcpy(created_process->nom, name);
    created_process->pid = current_pid;

    created_process->ssize = ssize;
    created_process->tab_registres[1] = (int32_t)((int32_t *)created_process->pile_exec_process + ssize - 3);
    created_process->exit_quit = 0;

    created_process->reseted_message =0; // si 1 , donc la file de message a été reset 
    //created_process->deleted_message=0; 
    created_process->etat_bloque = -1; // 1 si il est bloqué sur file pleine, 0 s'il est bloqué sur file vide 
   // created_process ->fid_bloque =-1;

    created_process->etat = activable;
    created_process->pile_exec_process[ssize - 1] = (int32_t)arg;
    created_process->pile_exec_process[ssize - 2] = (int32_t)fin_processus;
    created_process->pile_exec_process[ssize - 3] = (int32_t)pt_func; 

    created_process->prio = prio;
    created_process->parent = processus_courant; // Initialiser le parent
    INIT_LIST_HEAD(&created_process->enfants);   // Initialiser la liste des enfants

    process_tab[current_pid] = created_process;

    created_process->user_mode = 0;

    // STEP 3 : ADD THE PROCESS TO THE LISTS

    // gestion de la filiation
    if (processus_courant)
    {
        queue_add(created_process, &processus_courant->enfants, struct process, linkEnfant, prio);
    }

    queue_add(created_process, &list_process_activable, struct process, lien, prio);


if (processus_courant && created_process->prio > processus_courant->prio)
    {
        ordonnanceur();
    }



    return created_process->pid;
}

void init_process_list()
{
    process_tab = mem_alloc(NBPROC * sizeof(process *));
    if (!process_tab)
    {
        printf("Failed to allocate memory for process_tab\n");
        return;
    }
    INIT_LIST_HEAD(&list_process_activable);
    start(idle, 512, 1, "idle", NULL);
    processus_courant = queue_out(&list_process_activable, struct process, lien);
    processus_courant->etat = elu;
}

/**
 * Auxiliary function for ordonnanceur to wake any process depending on time
 */
void wake_process(process *tete_endormi)
{
    while (tete_endormi && tete_endormi->temps_reveil <= current_clock())
    {
        process *proc = queue_out(&list_process_endormi, struct process, lien);
        queue_add(proc, &list_process_activable, struct process, lien, prio);
        tete_endormi = queue_top(&list_process_endormi, struct process, lien);
    }
}


void ordonnanceur()
{
    process *old_processus_courant = processus_courant;

    // STEP 1: Wake process - existing code
    if (!queue_empty(&list_process_endormi))
    {
        process *tete_endormi = queue_top(&list_process_endormi, struct process, lien);
        wake_process(tete_endormi);
    }

    // STEP 2 & 3: Handle current process state - existing code
    if (processus_courant->etat == endormi)
    {
        queue_add(processus_courant, &list_process_endormi, struct process, lien, temps_reveil);
    }
    else if (processus_courant->etat != zombie && processus_courant->etat != to_delete && 
             processus_courant->etat != bloque_file && processus_courant->etat != waiting_child && 
             processus_courant->etat != bloque_clavier)
    {
        processus_courant->etat = activable;
        queue_add(processus_courant, &list_process_activable, struct process, lien, prio);
    }

    // STEP 4: Switch to next process
    process *next_processus_courant = queue_out(&list_process_activable, struct process, lien);
    next_processus_courant->etat = elu;

    if (old_processus_courant != next_processus_courant) // if it's same process, don't switch
    {
        // Save current process's CR3 if needed
        if (old_processus_courant->user_mode) {
            // Save CR3 value before switching
            __asm__ __volatile__("movl %%cr3, %0" : "=r" (old_processus_courant->cr3));
        }
        
        // Load new process's CR3 if it's a user process
        if (next_processus_courant->user_mode) {
            // Switch to the new page directory by updating CR3
            __asm__ __volatile__("movl %0, %%cr3" : : "r" (next_processus_courant->cr3) : "memory");
            processus_courant = next_processus_courant;
            int * tss_esp=(int*)(TSS+4);
            int * tss_ss=(int*)(TSS+8);

            *tss_esp=(int)next_processus_courant->pile_exec_process;
            *tss_ss=0x18;
            ctx_sw_user(old_processus_courant->tab_registres, next_processus_courant->tab_registres,(int *)0x40000000,(int *)next_processus_courant->tab_registres[1]);

        } else 
        {
            if (old_processus_courant->user_mode) {
            // If switching from user to kernel process, restore kernel page directory
            __asm__ __volatile__("movl %0, %%cr3" : : "r" (pgdir) : "memory");
        }  
            processus_courant = next_processus_courant;
            ctx_sw(old_processus_courant->tab_registres, next_processus_courant->tab_registres);
        }
        //processus_courant = next_processus_courant;
        //ctx_sw(old_processus_courant->tab_registres, next_processus_courant->tab_registres);
    }

    // STEP 5: Delete old process if needed - existing code
    if (processus_courant->etat == to_delete)
    {
        mem_free(old_processus_courant, sizeof(process));
    }
}

int idle(void *)
{

    for (;;)
    {
        sti();
        hlt();
        cli();
    }
}

///// fin processus
void init_list_process_zombie()
{
    INIT_LIST_HEAD(&list_process_zombie);
}

void exit(int retval)
{
    processus_courant->retval = retval;
    processus_courant->exit_quit = 1;
    fin_processus();
    while (1)
        ; // La fonction ne retourne jamais
}

void fin_processus()
{
    int save_retVal = getretval(); // A NE PAS SUPPRIMER ! (SINON PERTE DE VALEUR DE RETOUR)
    if(processus_courant->exit_quit == 0 ){
        processus_courant->retval = save_retVal;
    }
    
    printf(" fin %s -->  %i \n", processus_courant->nom, processus_courant->retval);
    // Si un parent existe -> état zombie
    if (processus_courant->parent)
    {
        processus_courant->etat = zombie;
        queue_add(processus_courant, &list_process_zombie, struct process, lien, prio);

        // Informer le parent de la terminaison de l'enfant
        if (processus_courant->parent)
        {
            printf("Processus parent %s (pid: %d) informé de la terminaison de l'enfant %s (pid: %d)\n",
                   processus_courant->parent->nom, processus_courant->parent->pid,
                   processus_courant->nom, processus_courant->pid);
        }
            // ajout  pour test5
            if (processus_courant->parent->pid != 0 && processus_courant->parent->etat==waiting_child) {
                processus_courant->parent->etat = activable;
                queue_add( processus_courant->parent, &list_process_activable, struct process, lien, prio);
            }
            

    }
    else
    {
        processus_courant->etat = to_delete;
    }

    

    /*
    // Gérer la terminaison des processus enfants
    struct process *child, *next;
    queue_for_each_for_safe(child, &processus_courant->enfants, struct process, linkEnfant)
    {
        child->parent = NULL; // Détacher les enfants du processus courant
        // Détruire les fils zombies
        if (child->etat == zombie)
        {
            queue_del(child, linkEnfant);
            queue_del(child, lien);
            free_process(child);
        }
    }
    */

   struct process *child, *next;
    queue_for_each_safe(child, next, &processus_courant->enfants, struct process, linkEnfant)
    {
        child->parent = NULL;
        if (child->etat == zombie)
        {
            queue_del(child, linkEnfant);
            queue_del(child, lien);
            free_process(child);
        }
    }



    ordonnanceur();
}

//// dormir

void init_list_process_endormi()
{
    INIT_LIST_HEAD(&list_process_endormi);
}

void wait_clock(unsigned long clock)
{
    //printf(" le process pid : %i entrain de dormir ", processus_courant->pid);
    processus_courant->etat = endormi;
    // printf("  %d djsklfkjldsf",current_clock());
    processus_courant->temps_reveil = clock;
    ordonnanceur();
}
/*
static int wait_for_child(process *child, int *retvalp)
{   

   

    while (child->etat != zombie)
    {
        //processus_courant->etat = waiting_child;
        ordonnanceur();
    }

    if (retvalp)
    {
        *retvalp = child->retval;
    }

    queue_del(child, linkEnfant);
    int pid = child->pid;
    queue_del(child, lien);
    free_process(child);
    
    return pid;
}*/


static int wait_for_child(process *child, int *retvalp)
{   


    processus_courant->parent->etat =waiting_child;

    while (child->etat != zombie)
    {
        processus_courant->etat = waiting_child;
        ordonnanceur();
    }
    
        

        

    if (retvalp)
        {
            *retvalp = child->retval;
        }

    queue_del(child, linkEnfant);
    int pid = child->pid;
    queue_del(child, lien);
    free_process(child);

    



    
    return pid;
}




int waitpid(int pid, int *retvalp)
{
    process *child;
    

    // Si pid est négatif, attendre n'importe quel fils
    if (pid < 0)
    {   
        int nbre_child =0;
        queue_for_each(child, &processus_courant->enfants, struct process, linkEnfant)
        {
            nbre_child++;
            if (child->etat == zombie)
            {
                return wait_for_child(child, retvalp);
            }
        }

        if(nbre_child==0){
            return -1;
        }

        // Attendre jusqu'à ce qu'un fils soit terminé

        processus_courant->etat = waiting_child;
        
        while (1)
        {
            queue_for_each(child, &processus_courant->enfants, struct process, linkEnfant)
            {
                if (child->etat == zombie)
                {
                    return wait_for_child(child, retvalp);
                }
            }

            ordonnanceur();
        }
    }
    else
    {
        // Si pid est positif, attendre le fils avec ce pid
        queue_for_each(child, &processus_courant->enfants, struct process, linkEnfant)
        {
            if (child->pid == pid)
            {
                return wait_for_child(child, retvalp);
            }
        }

        // Si le processus avec ce pid n'existe pas ou n'est pas un fils
        return -1;
    }
}


int getpid(void)
{
    return processus_courant->pid;
}

int kill(int pid)
{

    // printf("Je vais tuer le processus %s", process_tab[pid]->nom);
    // printf("Son etat est : %i", process_tab[pid]->etat);

    // Vérifier si le processus existe
    if (pid <= 0 || pid >= NBPROC || process_tab[pid] == NULL)
    {
        return -1; // Le processus n'existe pas
    }



    process *target_process = process_tab[pid];

    // Si le processus est déjà un zombie, le retirer et libérer ses ressources
    if (target_process->etat == zombie)
    {
        return -1;
    }

    /*
    pas besoin de ce bloc car on va l'executer en bas aussi 

    // Si le processus est bloqué dans une file d'attente, le retirer
    if (target_process->etat == endormi)
    {
        queue_del(target_process, lien);
    }

    */


    // Marquer le processus comme zombie si un parent existe
    if (target_process->parent)
    {
        // target_process->etat = zombie;
        /*
        */
       if(getpid()!=pid ){
            // si il est élu , il ne faut pas faire ceci
            if(target_process->etat == bloque_file){
                for(int i=0 ; i<NBQUEUE;i++){
                    if((liste_fileMessage[i]).capacite!= 0){
                        process * target_process;
                        queue_for_each(target_process, &((liste_fileMessage[i]).list_process_bloque_file), struct process, linkFileMessage)
                        {
                            if(target_process->pid == pid){
                                if(target_process->etat_bloque == 1){
                                    liste_fileMessage[i].nbre_processus_bloque_send--;
                                    target_process->etat_bloque = -1;
                                }else{
                                    liste_fileMessage[i].nbre_processus_bloque_recieve--;
                                    target_process->etat_bloque = -1;
                                }
                            }
                        }
                    }
                }
                queue_del(target_process, linkFileMessage);// supprimer target_process de la liste des endormis
            }
            else{
                queue_del(target_process, lien);// supprimer target_process de la liste des activables  !!!!!!(ATTENTON , ON POURRA SUPPRIMER AUSSI DE LA LISTE ENDORI , TEST7)
            }
            
       }
        target_process->etat = zombie;
        queue_add(target_process, &list_process_zombie, struct process, lien, prio);
    }
    else
    {
        // Si aucun parent, libérer immédiatement les ressources

        if(getpid()!=pid){
            //si il est élu , il ne faut pad faire ceci 
            if(target_process->etat == bloque_file){
                for(int i=0 ; i<NBQUEUE;i++){
                    if((liste_fileMessage[i]).capacite!= 0){
                        process * target_process;
                        queue_for_each(target_process, &((liste_fileMessage[i]).list_process_bloque_file), struct process, linkFileMessage)
                        {
                            if(target_process->pid == pid){
                                if(target_process->etat_bloque == 1){
                                    liste_fileMessage[i].nbre_processus_bloque_send--;
                                    target_process->etat_bloque = -1;
                                }else{
                                    liste_fileMessage[i].nbre_processus_bloque_recieve--;
                                    target_process->etat_bloque = -1;
                                }
                            }
                        }
                    }
                }
                queue_del(target_process, linkFileMessage);// supprimer target_process de la liste file
            }
            else{
                queue_del(target_process, lien);
            }
            //queue_del(target_process, lien);
            free_process(target_process); 
        }else{
            processus_courant->etat = to_delete;
        }
        
        //free_process(target_process); 
        
    }

    ordonnanceur();

    return 0; // Processus terminé avec succès
}



int getprio(int pid)
{
    // Vérifier si le pid est valide
    if (pid < 0 || pid >= NBPROC || process_tab[pid] == NULL)
    {
        return -1; // Le processus n'existe pas ou le pid est invalide
    }

    // Récupérer le processus correspondant
    process *target_process = process_tab[pid];

    // Retourner la priorité du processus
    return target_process->prio;
}

int chprio(int pid, int newprio)
{
    // Vérifier si le pid est valide
    if (pid < 0 || pid >= NBPROC || process_tab[pid] == NULL)
    {
        return -1; // Le processus n'existe pas ou le pid est invalide
    }

    // Vérifier si la nouvelle priorité est valide
    if (newprio <= 0 || newprio > MAXPRIO)
    {
        return -1; // La priorité est hors borne
    }

    // Récupérer le processus correspondant
    process *target_process = process_tab[pid];

    // Vérifier que le processus n'est pas zombie
    if (target_process->etat == zombie){
        return -1;
    }

    // Sauvegarder l'ancienne priorité
    int oldprio = target_process->prio;

    // Mettre à jour la priorité
    target_process->prio = newprio;

    // Si le processus est dans une file d'attente, réorganiser la file
    if (target_process->etat == activable || target_process->etat == endormi )
    {
        queue_del(target_process, lien);
        queue_add(target_process, &list_process_activable, struct process, lien, prio);
    }
   

    if(target_process->etat == bloque_file){

        for (int i = 0; i < NBQUEUE; i++) {
            fileMessage *f = &liste_fileMessage[i];
            if (f->capacite == 0) continue;

            /* Le PCB est‑il dans cette liste ? */
            if (!queue_member(target_process,
                              &f->list_process_bloque_file,
                              struct process, linkFileMessage))
                continue;

            queue_del(target_process, linkFileMessage);
            queue_add(target_process,
                      &f->list_process_bloque_file,
                      struct process, linkFileMessage, prio);
            break;              /* surtout NE PAS boucler davantage */
        }
        
    }

    ordonnanceur();
    // Retourner l'ancienne priorité
    return oldprio;
}

static unsigned int* create_user_page_directory() {
    unsigned int* page_dir = physmem_alloc_page();
    if (!page_dir) return NULL;
    
    memset(page_dir, 0, PAGE_SIZE);
    
    // Copy kernel mappings but make them user-accessible
    for (int i = 0; i < KERNEL_ENTRIES; i++) {
        page_dir[i] = pgdir[i] | 0x4;  // Add user access bit to kernel pages
    }
    
    return page_dir;
}

static unsigned long map_user_stack(unsigned int* page_dir, unsigned long size) {
    // Calculate necessary pages
    int pages = (size + PAGE_SIZE - 1) / PAGE_SIZE;
    unsigned long stack_top = USER_STACK_BASE + (pages * PAGE_SIZE);
    
    // For each page needed
    for (int i = 0; i < pages; i++) {
        unsigned long vaddr = USER_STACK_BASE + (i * PAGE_SIZE);
        
        // Allocate physical page for stack
        void* phys_page = physmem_alloc_page();
        if (!phys_page) return 0;
        
        // Clear the page
        memset(phys_page, 0, PAGE_SIZE);
        
        // Map the page using map_page function
        // Use PAGE_PRESENT | PAGE_RW | PAGE_USER (0x7) for user writable pages
        if (map_page(page_dir, vaddr, (unsigned long)phys_page, PAGE_PRESENT | PAGE_RW | PAGE_USER) < 0) {
            // Handle error - free allocated page
            physmem_free_page(phys_page);
            return 0;
        }
    }
    
    return stack_top;
}

static void init_apps_htable(void) {
    if (apps_htable_initialized) return;
    
    // Use hash_init_string instead of htable_init
    hash_init_string(&apps_htable);
    
    // Add all applications to the hash table
    for (int i = 0; symbols_table[i].name != NULL; i++) {
        // Use hash_set instead of htable_add
        hash_set(&apps_htable, (void*)symbols_table[i].name, (void*)&symbols_table[i]);
    }
    
    apps_htable_initialized = 1;
}

static int map_user_application(unsigned int* page_dir, const char* name, unsigned long base_addr) {
    // Find the application in the hash table or symbols table
    struct uapps* app = NULL;
    
    // Try hash table first if initialized
    if (apps_htable_initialized) {
        // Use hash_get instead of htable_lookup
        app = (struct uapps*)hash_get(&apps_htable, (void*)name, NULL);
    } else {
        // Fall back to linear search if hash table not initialized
        for (int i = 0; symbols_table[i].name != NULL; i++) {
            if (strcmp(symbols_table[i].name, name) == 0) {
                app = (struct uapps*)&symbols_table[i];
                break;
            }
        }
    }
    
    if (!app) {
        printf("Application '%s' not found\n", name);
        return -1;
    }
    
    // Calculate size of application
    unsigned long app_size = (unsigned long)app->end - (unsigned long)app->start;
    unsigned long pages_needed = (app_size + PAGE_SIZE - 1) / PAGE_SIZE;
    
    printf("Mapping application '%s' (%lu bytes, %lu pages) to 0x%lx\n", 
           name, app_size, pages_needed, base_addr);
    
    // Map each page of the application
    for (unsigned long i = 0; i < pages_needed; i++) {
        // Calculate source and destination addresses
        unsigned long src_offset = i * PAGE_SIZE;
        unsigned long dest_vaddr = base_addr + src_offset;
        
        // Don't go beyond app size
        if (src_offset >= app_size) break;
        
        // Allocate a physical page
        void* phys_page = physmem_alloc_page();
        if (!phys_page) {
            printf("Failed to allocate page for application\n");
            return -1;
        }
        
        // Copy app data to the physical page
        unsigned long bytes_to_copy = PAGE_SIZE;
        if (src_offset + bytes_to_copy > app_size) {
            bytes_to_copy = app_size - src_offset;
        }
        
        memcpy(phys_page, (char*)app->start + src_offset, bytes_to_copy);
        
        // Map the page into user space
        if (map_page(page_dir, dest_vaddr, (unsigned long)phys_page, 
                    PAGE_PRESENT | PAGE_USER | PAGE_RW) < 0) {
            printf("Failed to map page for application\n");
            physmem_free_page(phys_page);
            return -1;
        }
    }
    
    return 0;
}

/**
 * Start a user process with memory protection
 */
int start_user(const char *name, unsigned long ssize, int prio, void *arg) {
    // Input validation
    if (prio <= 0 || prio > MAXPRIO) {
        printf("priorité invalide");
        return -1;
    }
    
    int current_pid = find_free_spot_process_tab();
    if (current_pid == -1) {
        printf("Pas de place disponible pour créer un nouveau processus");
        return -1;
    }
    
    // Verify stack size
    if (ssize >= 0xFFFFFFFF - 3) {
        return -1;
    }
    
    ssize = ssize + 1024;
    
    // Create process structure
    process *created_process = mem_alloc(sizeof(process));
    if (!created_process) {
        return -1;
    }
    
    // Initialize basic fields
    strcpy(created_process->nom, name);
    created_process->pid = current_pid;
    created_process->ssize = ssize;
    created_process->user_mode = 1;  // Mark as user process
    created_process->exit_quit = 0;
    created_process->reseted_message = 0;
    created_process->etat_bloque = -1;
    created_process->etat = activable;
    
    // Create page directory for this process
    unsigned int* page_dir = create_user_page_directory();
    if (!page_dir) {
        mem_free(created_process, sizeof(process));
        return -1;
    }
    
    // Store CR3 in the process structure
    created_process->cr3 = (unsigned int)page_dir;
    
    // Make sure hash table is initialized
    if (!apps_htable_initialized) {
        init_apps_htable();
    }
    
    // Map the user application at 1GB boundary (0x40000000)
    if (map_user_application(page_dir, name, 0x40000000) < 0) {
        printf("Failed to load application '%s'\n", name);
        physmem_free_page(page_dir);
        mem_free(created_process, sizeof(process));
        return -1;
    }
    
    // Use the application's entry point at 0x40000000 as the pt_func
    void* pt_func = (void*)0x40000000;
    
    // Map user stack
    unsigned long stack_top = map_user_stack(page_dir, ssize * sizeof(unsigned long));
    if (!stack_top) {
        // Cleanup 
        physmem_free_page(page_dir);
        mem_free(created_process, sizeof(process));
        return -1;
    }
    
    
    
    // Calculate virtual address for the stack
    unsigned long stack_virt_addr = stack_top - (3 * sizeof(unsigned long));
    created_process->user_stack = stack_virt_addr;
    // Store the virtual address of the stack
    created_process->pile_exec_process = mem_alloc(ssize * sizeof(unsigned long));
    
    // Calculate physical address to initialize stack content
    unsigned int pd_idx = stack_virt_addr >> 22;
    unsigned int pt_idx = (stack_virt_addr >> 12) & 0x3FF;
    unsigned int* page_table = (unsigned int*)(page_dir[pd_idx] & ~0xFFF);
    void* phys_page = (void*)(page_table[pt_idx] & ~0xFFF);
    unsigned long offset = stack_virt_addr & 0xFFF;
    unsigned long* stack_phys_addr = (unsigned long*)((char*)phys_page + offset);
    
    // Initialize stack in correct order for return sequence
    stack_phys_addr[2] = (unsigned long)arg;           // Argument
    stack_phys_addr[1] = (unsigned long)fin_processus; // Return address
    stack_phys_addr[0] = (unsigned long)pt_func;       // Entry point (will be popped to EIP by ret)
    
    // ESP points to argument position on the stack
    created_process->tab_registres[1] = stack_virt_addr ;
    
    // Store process in process table
    process_tab[current_pid] = created_process;
    
    // Set up process relationships
    created_process->prio = prio;
    created_process->parent = processus_courant;
    INIT_LIST_HEAD(&created_process->enfants);
    
    // Add to scheduler queues
    if (processus_courant) {
        queue_add(created_process, &processus_courant->enfants, struct process, linkEnfant, prio);
    }
    
    queue_add(created_process, &list_process_activable, struct process, lien, prio);
    
 
    
    if (processus_courant && created_process->prio > processus_courant->prio) {
        ordonnanceur();
    }
    
    return created_process->pid;
}

