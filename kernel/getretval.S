    .text
    .globl getretval
# Structure de la pile en entree :
#   %esp + 4 : adresse du valeur de retour
getretval:
    ret

