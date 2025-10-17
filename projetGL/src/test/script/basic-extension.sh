#!/bin/bash

# Auteur : gl47
# Version initiale : 01/01/2025


cd "$(dirname "$0")"/../../.. || exit 1
TEST_DIR_VALID="src/test/deca/extension/codegen/valid"
# Vérifier si le répertoire existe
if [ ! -d "$TEST_DIR_VALID" ]; then
    echo -e "${RED}Erreur : Le répertoire $TEST_DIR_VALID n'existe pas.${NC}"
    exit 1
fi

# Déterminer le répertoire du script pour les chemins relatifs
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../../.."

PATH=./src/test/script/launchers:"$PATH"

# Couleurs pour les messages
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
RESET="\033[0m"

# Variables pour le résumé
success_count_cont=0
fail_count_cont=0
# Variables pour le résumé
success_count_synt=0
fail_count_synt=0

compilation_output=$("$ROOT_DIR/src/main/bin/decac" -P "$TEST_DIR_VALID"/*.deca 2>&1)

if [[ $? -ne 0 ]]; then
    echo -e "${RED}La compilation avec decac -P a échoué !${NC}"
    echo -e "${YELLOW}Détails de la compilation :${NC}"
    echo "$compilation_output"
    exit 1
else
    echo -e "${GREEN}Compilation réussie pour tous les fichiers valides.${NC}"
fi


# Fonction pour tester les fichiers invalides
test_context_invalide() {
    # $1 = chemin du fichier
    if test_context "$1" 2>&1 | grep -q -e "$1:[0-9][0-9]*:"; then
        echo -e "${GREEN}[PASS] Échec attendu détecté pour $1.${RESET}"
        ((success_count_cont++))
    else
        echo -e "${RED}[FAIL] Succès inattendu pour $1.${RESET}"
        ((fail_count_cont++))
    fi
}

# Fonction pour tester les fichiers valides
test_context_valid() {
    # $1 = chemin du fichier
    if test_context "$1" 2>&1 | grep -q -e ":[0-9][0-9]*:"; then
        echo -e "${RED}[FAIL] Erreur détectée dans un fichier valide : $1.${RESET}"
        test_context "$1" 2>&1
        ((fail_count_cont++))
    else
        echo -e "${GREEN}[PASS] Aucun problème détecté pour $1.${RESET}"
        ((success_count_cont++))
    fi
}
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
    elif echo "$output" | grep -q -e "On ne peut pas affectée à 1D int un type 1D float"; then
        echo -e "${GREEN}[PASS] Échec attendu détecté pour $file (erreur de type).${RESET}"
        ((success_count_cont++))
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
# Fonction pour extraire les résultats attendus depuis un fichier .deca
extract_result() {
    local deca_file="$1"
    local result_line
    result_line=$(grep -A1 "// Resultats:" "$deca_file")
    echo "$result_line"
}
# Fonction pour tester les fichiers IMA valides
test_ima_valid() {
    local ass_file="$1"
    local output
    local deca_file="${ass_file%.ass}.deca" # Trouver le fichier .deca correspondant

    # Exécuter le programme avec IMA et capturer la sortie
    output=$(ima "$ass_file" 2>&1)

    # Vérifier si une erreur IMA est produite
    if echo "$output" | grep -q -e "ERREUR"; then
        echo -e "${RED}[FAIL] Erreur inattendue dans un fichier valide : $ass_file.${NC}"
        echo -e "${YELLOW}Détails de la commande :${NC}"
        echo "$output"
        ((fail_count_ima++))
    else
        echo -e "${GREEN}[PASS] Aucune erreur détectée pour $ass_file.${NC}"
        echo -e "${YELLOW}Sortie de la commande :${NC}"
        echo "$output"
        echo -e "${YELLOW}Résultats attendus dans le fichier .deca :${NC}"
        extract_result "$deca_file"
        ((success_count_ima++))
    fi
}

# En-tête des tests syntaxiques invalides
echo ""
echo -e "${YELLOW}--- Début des tests syntaxiques invalides de l'extension---${RESET}"

# Boucle sur tous les fichiers de test invalides
for cas_de_test in src/test/deca/extension/synt/invalid/*.deca; do
    test_synt_invalide "$cas_de_test"
done


# En-tête des tests synt valides
echo ""
echo -e "${YELLOW}--- Début des tests syntaxiques valides de l'extension---${RESET}"

# Boucle sur tous les fichiers de test synt valides
for cas_de_test in src/test/deca/extension/synt/valid/*.deca; do
    test_synt_valid "$cas_de_test"
done
echo ""
echo -e "${YELLOW}--- Résumé des tests synt ---${RESET}"
echo -e "${GREEN}Tests réussis synt: $success_count_synt${RESET}"
echo -e "${RED}Tests échoués synt: $fail_count_synt${RESET}"



# En-tête des tests invalides
echo ""
echo -e "${YELLOW}--- Début des tests contextuels invalides de l'extension---${RESET}"

# Boucle sur tous les fichiers de test invalides
for cas_de_test in src/test/deca/extension/context/invalid/*.deca; do
    test_context_invalide "$cas_de_test"
done

# En-tête des tests valides
echo ""
echo -e "${YELLOW}--- Début des tests contextuels valides de l'extension---${RESET}"

# Boucle sur tous les fichiers de test valides
for cas_de_test in src/test/deca/extension/context/valid/*.deca; do
    test_context_valid "$cas_de_test"
done
# Début des tests IMA valides
echo -e "${YELLOW}--- Début des tests IMA valides ---${NC}"

# Boucle sur tous les fichiers .ass générés
for cas_de_test in "$TEST_DIR_VALID"/*.ass; do
    test_ima_valid "$cas_de_test"
done


# Résumé final
echo ""
echo -e "${YELLOW}--- Résumé des tests contextuels ---${RESET}"
echo -e "${GREEN}Tests réussis context: $success_count_cont${RESET}"
echo -e "${RED}Tests échoués context: $fail_count_cont${RESET}"
echo ""



