.text
    .globl ctx_sw_user

# Entrée sur la pile de l’ISR avant appel :
#   [esp+0] = retour vers le kernel
#   [esp+4] = &old_ctx
#   [esp+8] = &new_ctx
#   [esp+12] = user_eip
#   [esp+16] = user_esp

ctx_sw_user:
    # 1) Sauvegarde du contexte KERNEL
    movl    4(%esp), %eax      # eax = &old_ctx
    movl    %ebx,   (%eax)
    movl    %esp,   4(%eax)
    movl    %ebp,   8(%eax)
    movl    %esi,  12(%eax)
    movl    %edi,  16(%eax)

    # 2) Récupère new_ctx, user_eip et user_esp dans des regs temporaires
    movl    8(%esp),  %eax     # ebx = &new_ctx
    movl   12(%esp),  %ecx     # ecx = user_eip
    movl   16(%esp),  %edx     # edx = user_esp

    # 3) Restauration du contexte KERNEL du nouveau processus
    movl   (%eax),   %ebx      # ebx = new_ctx->ebx
    movl   4(%eax),   %esp     # esp = new_ctx->esp
    movl   8(%eax),   %ebp     # ebp = new_ctx->ebp
    movl  12(%eax),   %esi     # esi = new_ctx->esi
    movl  16(%eax),   %edi     # edi = new_ctx->edi

    # 4) Charge les bons sélecteurs USER (RPL=3)
    mov     $0x4B, %ax         # user DS/ES/FS/GS
    mov     %ax, %ds
    mov     %ax, %es
    mov     %ax, %fs
    mov     %ax, %gs

    # 5) Prépare le frame pour iret en mode UTILISATEUR
    pushl   $0x4B               # user SS = 0x4B
    pushl   %edx               # user ESP
    pushfl                     # EFLAGS
    pushl   $0x43             # CS
    pushl   %ecx               # EIP


    iret