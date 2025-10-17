#!/bin/bash

# Auteur : gl47
# Version : 01/01/2025

# Description :
# Ce script compile et teste tous les fichiers .deca dans un répertoire donné.
# Il affiche "OK" en vert pour les tests réussis et "KO" en rouge pour les tests échoués.
# En cas d'échec, il affiche le message d'erreur associé.
# À la fin, il fournit un résumé des résultats.

# Définir les couleurs pour les messages
GREEN="\033[0;32m"
RED="\033[0;31m"
YELLOW="\033[0;33m"
NC="\033[0m" # No Color

# Variables pour le résumé des tests
success_count_ima=0
fail_count_ima=0

# Vérifier si un argument a été fourni (répertoire contenant les fichiers .deca)
if [ $# -ne 1 ]; then
    echo -e "${RED}Usage: $0 <répertoire_contenant_les_fichiers_de_tests>${NC}"
    exit 1
fi

TEST_DIR="$1"

# Vérifier si le répertoire existe
if [ ! -d "$TEST_DIR" ]; then
    echo -e "${RED}Erreur : Le répertoire $TEST_DIR n'existe pas.${NC}"
    exit 1
fi

# Déterminer le répertoire du script pour les chemins relatifs
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../../.."

# Ajouter le chemin des exécutables au PATH
export PATH="$ROOT_DIR/src/test/script/launchers:$PATH"

# Compilation des fichiers .deca
compilation_output=$("$ROOT_DIR/src/main/bin/decac" "$TEST_DIR"/*.deca 2>&1)

if [[ $? -ne 0 ]]; then
    echo -e "${RED}La compilation avec decac -P a échoué !${NC}"
    echo -e "${YELLOW}Détails de la compilation :${NC}"
    echo "$compilation_output"
    exit 1
else
    echo -e "${GREEN}Compilation réussie pour tous les fichiers valides.${NC}"
fi

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

# Début des tests IMA valides
echo -e "${YELLOW}--- Début des tests IMA valides ---${NC}"

# Boucle sur tous les fichiers .ass générés
for cas_de_test in "$TEST_DIR"/*.ass; do
    test_ima_valid "$cas_de_test"
done

# Résumé final des tests
echo ""
echo -e "${YELLOW}--- Résumé des tests IMA ---${NC}"
echo -e "${GREEN}Tests réussis : $success_count_ima${NC}"
echo -e "${RED}Tests échoués : $fail_count_ima${NC}"

# Définir le code de sortie en fonction des échecs
if [ "$fail_count_ima" -ne 0 ]; then
    exit 1
else
    exit 0
fi

