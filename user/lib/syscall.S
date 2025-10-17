.section .text
.globl __syscall0
.globl __syscall1
.globl __syscall2
.globl __syscall3
.globl __syscall4

# Chaque wrapper sauve/restaure les registres callee‑saved (EBX, ESI, EDI)

__syscall0:
    pushl   %ebx
    pushl   %esi
    pushl   %edi

    # chargement du n° de syscall (après 3 pushes = +12)
    movl    16(%esp), %eax
    int     $0x49

    popl    %edi
    popl    %esi
    popl    %ebx
    ret

__syscall1:
    pushl   %ebx
    pushl   %esi
    pushl   %edi

    movl    16(%esp), %eax   # n° syscall
    movl    20(%esp), %ebx   # arg1
    int     $0x49

    popl    %edi
    popl    %esi
    popl    %ebx
    ret

__syscall2:
    pushl   %ebx
    pushl   %esi
    pushl   %edi

    movl    16(%esp), %eax   # n° syscall
    movl    20(%esp), %ebx   # arg1
    movl    24(%esp), %ecx   # arg2
    int     $0x49

    popl    %edi
    popl    %esi
    popl    %ebx
    ret

__syscall3:
    pushl   %ebx
    pushl   %esi
    pushl   %edi

    movl    16(%esp), %eax   # n° syscall
    movl    20(%esp), %ebx   # arg1
    movl    24(%esp), %ecx   # arg2
    movl    28(%esp), %edx   # arg3
    int     $0x49

    popl    %edi
    popl    %esi
    popl    %ebx
    ret

__syscall4:
    pushl   %ebx
    pushl   %esi
    pushl   %edi

    movl    16(%esp), %eax   # n° syscall
    movl    20(%esp), %ebx   # arg1
    movl    24(%esp), %ecx   # arg2
    movl    28(%esp), %edx   # arg3
    movl    32(%esp), %esi   # arg4
    int     $0x49

    popl    %edi
    popl    %esi
    popl    %ebx
    ret
