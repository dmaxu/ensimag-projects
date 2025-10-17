#!/bin/bash

# Auteur : gl47
# Version initiale : 01/01/2025

GREEN="\033[0;32m"
RED="\033[0;31m"
YELLOW="\033[0;33m"
NC="\033[0m" # No Color

# Variables pour le résumé des tests
success_count_ima=0
fail_count_ima=0

# Répertoire des tests
TEST_DIR_VALID="src/test/deca/codegen/valid/ourTest"
TEST_DIR_INVALID="src/test/deca/codegen/invalid"
TEST_DIR_INTERACTIVE="src/test/deca/codegen/interactive"

# Vérifier si le répertoire existe
if [ ! -d "$TEST_DIR_VALID" ]; then
    echo -e "${RED}Erreur : Le répertoire $TEST_DIR_VALID n'existe pas.${NC}"
    exit 1
fi
if [ ! -d "$TEST_DIR_INVALID" ]; then
    echo -e "${RED}Erreur : Le répertoire $TEST_DIR_INVALID n'existe pas.${NC}"
    exit 1
fi
if [ ! -d "$TEST_DIR_INTERACTIVE" ]; then
    echo -e "${RED}Erreur : Le répertoire $TEST_DIR_INTERACTIVE n'existe pas.${NC}"
    exit 1
fi

# Déterminer le répertoire du script pour les chemins relatifs
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../../.."

# Ajouter le chemin des exécutables au PATH
export PATH="$ROOT_DIR/src/test/script/launchers:$PATH"

# Compilation des fichiers .deca
compilation_output=$("$ROOT_DIR/src/main/bin/decac" "$TEST_DIR_VALID"/*.deca 2>&1)

if [[ $? -ne 0 ]]; then
    echo -e "${RED}La compilation avec decac -P a échoué !${NC}"
    echo -e "${YELLOW}Détails de la compilation :${NC}"
    echo "$compilation_output"
    exit 1
else
    echo -e "${GREEN}Compilation réussie pour tous les fichiers valides.${NC}"
fi
# Compilation des fichiers .deca
compilation_output=$("$ROOT_DIR/src/main/bin/decac" "$TEST_DIR_INVALID"/*.deca 2>&1)

if [[ $? -ne 0 ]]; then
    echo -e "${RED}La compilation avec decac -P a échoué !${NC}"
    echo -e "${YELLOW}Détails de la compilation :${NC}"
    echo "$compilation_output"
    exit 1
else
    echo -e "${GREEN}Compilation réussie pour tous les fichiers invalides.${NC}"
fi

# Compilation des fichiers .deca
compilation_output=$("$ROOT_DIR/src/main/bin/decac" "$TEST_DIR_INTERACTIVE"/*.deca 2>&1)

if [[ $? -ne 0 ]]; then
    echo -e "${RED}La compilation avec decac -P a échoué !${NC}"
    echo -e "${YELLOW}Détails de la compilation :${NC}"
    echo "$compilation_output"
    exit 1
else
    echo -e "${GREEN}Compilation réussie pour tous les fichiers interactives.${NC}"
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

# Fonction pour tester les fichiers IMA invalides
test_ima_invalid() {
    local ass_file="$1"
    local output
    local deca_file="${ass_file%.ass}.deca" # Trouver le fichier .deca correspondant

    # Exécuter le programme avec IMA et capturer la sortie
    output=$(ima "$ass_file" 2>&1)

    # Vérifier si une erreur IMA est produite
    if echo "$output" | grep -q -e "Erreur"; then
        echo -e "${GREEN}[PASS] Erreur attendue pour un fichier invalide : $ass_file.${NC}"
        echo -e "${YELLOW}Sortie de la commande :${NC}"
        echo "$output"
        echo -e "${YELLOW}Résultats attendus dans le fichier .deca :${NC}"
        extract_result "$deca_file"
        ((success_count_ima++))
    else
        echo -e "${RED}[FAIL] Aucune erreur détectée pour un fichier invalide : $ass_file.${NC}"
        echo -e "${YELLOW}Sortie de la commande :${NC}"
        echo "$output"
        ((fail_count_ima++))
    fi
}

# Fonction pour tester les tests interactifs
test_interactive() {
    local test_name="$1"
    local ass_file="$2"
    local input="$3"
    local expected_output="$4"

    echo -e "${YELLOW}--- Test interactif : $test_name ---${NC}"
    echo -e "${GREEN}Exécution avec entrée : $input ---${NC}"

    # Exécuter le test avec l'entrée spécifiée
    output=$(echo "$input" | ima "$ass_file" 2>&1)

    # Affichage des résultats et vérification si la sortie correspond aux attentes
    if [[ "$output" == *"$expected_output"* ]]; then
        echo -e "${GREEN}[PASS] Résultat attendu trouvé pour $test_name.${NC}"
        echo -e "${YELLOW}Sortie obtenue :${NC}"
        echo "$output"
        ((success_count_ima++))
    else
        echo -e "${RED}[FAIL] Résultat attendu NON trouvé pour $test_name.${NC}"
        echo -e "${YELLOW}Sortie obtenue :${NC}"
        echo "$output"
        ((fail_count_ima++))
    fi
}

# Début des tests IMA valides
echo -e "${YELLOW}--- Début des tests IMA valides ---${NC}"

# Boucle sur tous les fichiers .ass générés
for cas_de_test in "$TEST_DIR_VALID"/*.ass; do
    test_ima_valid "$cas_de_test"
done

# Début des tests IMA invalides
echo -e "${YELLOW}--- Début des tests IMA invalides ---${NC}"

# Boucle sur tous les fichiers .ass générés
for cas_de_test in "$TEST_DIR_INVALID"/*.ass; do
    test_ima_invalid "$cas_de_test"
done


# Début des tests interactifs
echo -e "${YELLOW}--- Début des tests IMA interactives ---${NC}"

# Test de readInt avec valeur valide (2)
test_interactive "Test de readInt avec valeur valide" "$TEST_DIR_INTERACTIVE/test_readInt.ass" "2" "4"

# Test de readFloat avec valeur valide (2.0)
test_interactive "Test de readFloat avec valeur valide" "$TEST_DIR_INTERACTIVE/test_readFloat.ass" "2.0" "4.0"

# Test de fibbo avec valeur valide (10)
test_interactive "Test de fibbo avec valeur valide" "$TEST_DIR_INTERACTIVE/test_fibo.ass" "10" "55"

# Test de guess avec valeurs valides (0.0 -10.0 10.0)
test_interactive "Test de guess avec valeurs valides" "$TEST_DIR_INTERACTIVE/test_guess.ass" "0.0 -10.0 10.0" "la valeur juste est 10.0,reessaie
la valeur juste est 10.0,reessaie
Bravo,c est bien 10"

# Test de readInt avec valeur invalide (2.0)
test_interactive "Test de readInt avec valeur invalide" "$TEST_DIR_INTERACTIVE/test_readInt.ass" "2.0" "Erreur"

# Test de readFloat avec valeur invalide (2)
test_interactive "Test de readFloat avec valeur invalide" "$TEST_DIR_INTERACTIVE/test_readFloat.ass" "2" "Erreur"




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
