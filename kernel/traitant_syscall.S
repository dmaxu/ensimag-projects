    .globl traitant_syscall
traitant_syscall:
    cli
    pushfl                 
    pushal                 

    push %ebx
    mov     $0x18, %bx
    mov     %bx, %ds
    mov     %bx, %es
    mov     %bx, %fs
    mov     %bx, %gs
    pop %ebx

    pushl   %edi           
    pushl   %esi          
    pushl   %edx         
    pushl   %ecx            
    pushl   %ebx        
    pushl   %eax         

    call    syscall_main
    addl    $24, %esp      

    movl    %eax, (%esp)

    popal                   
    popfl                 
    sti                  

    #mov     $0x23, %bx
    #mov     %bx, %ds
    #mov     %bx, %es
    #mov     %bx, %fs
    #mov     %bx, %gs

    iret                 
