#!/bin/bash

# Auteur : gl47
# Version initiale : 01/01/2025

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

# Couleurs pour les messages
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
RESET="\033[0m"

# Variables pour le résumé
success_count_synt=0
fail_count_synt=0

# Fonction pour tester les fichiers syntaxiques invalides
test_synt_invalide() {
    local file="$1"
    local output

    # Exécuter la commande et capturer la sortie
    output=$(test_synt "$file" 2>&1)

    # Vérifier si l'erreur correspond à ce qui est attendu
    if echo "$output" | grep -q -e "$file:[0-9][0-9]*:"; then
        echo -e "${GREEN}[PASS] Échec attendu détecté pour $file.${RESET}"
        ((success_count_synt++))
    elif echo "$output" | grep -q -e "literal values cannot be infinite"; then
        echo -e "${GREEN}[PASS] Échec attendu détecté pour $file (valeur flottante infinie).${RESET}"
        ((success_count_synt++))
    elif echo "$output" | grep -q -e "For input string: \"2147483648\""; then
        echo -e "${GREEN}[PASS] Échec attendu détecté pour $file (entier hors limite).${RESET}"
        ((success_count_synt++))
    else
        echo -e "${RED}[FAIL] Succès inattendu pour $file.${RESET}"
        echo -e "${YELLOW}Détails de la commande :${RESET}"
        echo "$output"
        ((fail_count_synt++))
    fi
}

# Fonction pour tester les fichiers valides
test_synt_valid() {
    # $1 = chemin du fichier
    if test_synt "$1" 2>&1 | grep -q -e ":[0-9][0-9]*:"; then
        echo -e "${RED}[FAIL] Erreur détectée dans un fichier valide : $1.${RESET}"
        test_synt "$1" 2>&1
        ((fail_count_synt++))
    else
        echo -e "${GREEN}[PASS] Aucun problème détecté pour $1.${RESET}"
        ((success_count_synt++))
    fi
}

# En-tête des tests syntaxiques invalides
echo ""
echo -e "${YELLOW}--- Début des tests syntaxiques invalides ---${RESET}"

# Boucle sur tous les fichiers de test invalides
for cas_de_test in src/test/deca/syntax/invalid/ourTests/*.deca; do
    test_synt_invalide "$cas_de_test"
done


# En-tête des tests synt valides
echo ""
echo -e "${YELLOW}--- Début des tests syntaxiques valides ---${RESET}"

# Boucle sur tous les fichiers de test synt valides
for cas_de_test in src/test/deca/syntax/valid/ourTests/*.deca; do
    test_synt_valid "$cas_de_test"
done
echo ""
echo -e "${YELLOW}--- Résumé des tests synt ---${RESET}"
echo -e "${GREEN}Tests réussis synt: $success_count_synt${RESET}"
echo -e "${RED}Tests échoués synt: $fail_count_synt${RESET}"