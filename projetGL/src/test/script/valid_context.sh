#!/bin/bash

# Auteur : gl47
# Version : 01/01/2025

# Description :
# Ce script teste tous les fichiers .deca dans le répertoire spécifié.
# Il affiche "OK" en vert pour les tests réussis et "KO" en rouge pour les tests échoués.
# En cas d'échec, il affiche le message d'erreur associé.
# À la fin, il fournit un résumé des résultats.

# Définir les couleurs
GREEN="\033[0;32m"
RED="\033[0;31m"
NC="\033[0m" # No Color

# Variables pour le résumé
total=0
success=0
failure=0

# Vérifier si un argument a été fourni
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

# Parcourir tous les fichiers .deca dans le répertoire spécifié
for test_file in "$TEST_DIR"*.deca; do
    # Vérifier s'il y a des fichiers .deca
    if [ ! -e "$test_file" ]; then
        echo -e "${RED}Aucun fichier .deca trouvé dans $TEST_DIR.${NC}"
        break
    fi

    # Incrémenter le compteur total
    ((total++))

    # Exécuter le test_context et capturer le code de sortie et la sortie
    output=$(./src/test/script/launchers/test_context "$test_file" 2>&1)
    exit_code=$?

    # Vérifier le code de sortie
    if [ $exit_code -eq 0 ]; then
        echo -e "$test_file: ${GREEN}OK${NC}"
        ((success++))
    else
        echo -e "$test_file: ${RED}KO${NC}"
        echo -e "${RED}Erreur:${NC} $output"
        ((failure++))
    fi
done

# Afficher le résumé des tests
echo ""
echo "=== Résumé des Tests ==="
echo -e "Total : $total"
echo -e "${GREEN}Succès : $success${NC}"
echo -e "${RED}Échecs : $failure${NC}"

# Définir le code de sortie en fonction des échecs
if [ "$failure" -ne 0 ]; then
    exit 1
else
    exit 0
fi
