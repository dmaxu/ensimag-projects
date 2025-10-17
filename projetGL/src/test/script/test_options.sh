#!/bin/bash

# Auteur : gl47
# Version : 2025-04-15

# Couleurs pour la sortie
GREEN="\033[0;32m"
RED="\033[0;31m"
YELLOW="\033[1;33m"
NC="\033[0m" # No Color

# Variables pour le résumé des tests
success_count=0
fail_count=0

# Répertoires
TEST_DIR_VALID="src/test/deca/codegen/valid/ourTest"
TEST_DIR_INVALID="src/test/deca/context/invalid/provided/"

# Vérifier l'existence des répertoires
for DIR in "$TEST_DIR_VALID" "$TEST_DIR_INVALID"; do
    if [ ! -d "$DIR" ]; then
        echo -e "${RED}Erreur : Le répertoire $DIR n'existe pas.${NC}"
        exit 1
    fi
done

# Déterminer le répertoire du script pour les chemins relatifs
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../../.."

# Ajouter le chemin des exécutables au PATH si nécessaire
export PATH="$ROOT_DIR/src/main/bin:$PATH"

#####################################
# Fonction pour compiler un fichier .deca avec des options
# Arguments:
#   1. options: string contenant les options à passer à decac
#   2. file: fichier .deca à compiler
#   3. description: description du test
#   4. expected_result: "success" ou "fail"
#####################################
compile_deca() {
    local options="$1"
    local file="$2"
    local description="$3"
    local expected="$4"

    echo -e "${YELLOW}--- Compilation : $description ---${NC}"

    # Construire la commande decac
    cmd=("$ROOT_DIR/src/main/bin/decac")
    read -r -a opts <<< "$options"
    for opt in "${opts[@]}"; do
        cmd+=("$opt")
    done
    cmd+=("$file")

    # Exécuter la commande decac
    compilation_output=$("${cmd[@]}" 2>&1)
    exit_code=$?

    # Vérification du résultat selon expected_result
    if [ "$expected" == "success" ]; then
        # On s'attend à une compilation réussie
        if [ $exit_code -eq 0 ]; then
            echo -e "${GREEN}[PASS] $description${NC}"
            ((success_count++))
        else
            echo -e "${RED}[FAIL] $description${NC}"
            echo -e "${YELLOW}Sortie de la compilation :${NC}"
            echo "$compilation_output"
            ((fail_count++))
        fi
    else
        # expected == "fail"
        # On s'attend à une compilation qui échoue
        if [ $exit_code -ne 0 ]; then
            echo -e "${GREEN}[PASS] $description (Échec attendu)${NC}"
            ((success_count++))
        else
            echo -e "${RED}[FAIL] $description (La compilation a réussi alors qu'elle devait échouer)${NC}"
            echo -e "${YELLOW}Sortie de la compilation :${NC}"
            echo "$compilation_output"
            ((fail_count++))
        fi
    fi
}

#####################################
# Fonction pour tester l'option -b (banner)
# decac -b sans fichier -> doit réussir (affiche la bannière)
#####################################
test_banner() {
    echo -e "${YELLOW}--- Test de l'option -b (banner) ---${NC}"
    compilation_output=$("$ROOT_DIR/src/main/bin/decac" "-b" 2>&1)
    exit_code=$?

    # On considère qu'un simple exit_code = 0 est un succès
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}[PASS] decac -b a affiché la bannière correctement.${NC}"
        ((success_count++))
    else
        echo -e "${RED}[FAIL] decac -b a échoué.${NC}"
        echo -e "${YELLOW}Sortie obtenue :${NC}"
        echo "$compilation_output"
        ((fail_count++))
    fi
}

#####################################
# Fonction pour tester l'option -p (parse) sur un fichier valide
#####################################
test_parse_on_valid() {
    local file="$1"
    compile_deca "-p" "$file" "Compilation avec -p (valid) : $file" "success"
}

#####################################
# Fonction pour tester l'option -r X
#####################################
test_registers() {
    local X="$1"
    local file="$2"
    compile_deca "-r $X" "$file" "Compilation avec -r $X sur $file" "success"
}


#####################################
# Fonction pour tester un fichier invalid (compile-time)
#####################################
test_invalid_compile() {
    local file="$1"
    compile_deca "" "$file" "Compilation (invalid context) : $file" "fail"
}

#####################################
# Fonction pour tester des options incompatibles
# ex: decac -b -p
#####################################
test_incompatible_options() {
    local options="$1"
    echo -e "${YELLOW}--- Test des options incompatibles: $options ---${NC}"

    # Sélectionner un fichier valide (peu importe, c'est juste pour forcer l'échec)
    local test_file
    test_file=$(find "$TEST_DIR_VALID" -type f -name "*.deca" | head -n 1)

    # Construction de la commande
    cmd=("$ROOT_DIR/src/main/bin/decac")
    read -r -a opts <<< "$options"
    for opt in "${opts[@]}"; do
        cmd+=("$opt")
    done
    cmd+=("$test_file")

    compilation_output=$("${cmd[@]}" 2>&1)
    exit_code=$?

    # On s'attend à ce que decac échoue (exit_code != 0)
    if [[ $exit_code -ne 0 ]]; then
        echo -e "${GREEN}[PASS] Options incompatibles $options ont correctement échoué.${NC}"
        echo -e "${YELLOW}Détails de la compilation :${NC}"
        echo "$compilation_output"
        ((success_count++))
    else
        echo -e "${RED}[FAIL] Les options incompatibles $options n'ont PAS échoué comme attendu.${NC}"
        echo -e "${YELLOW}Sortie obtenue :${NC}"
        echo "$compilation_output"
        ((fail_count++))
    fi
}

#####################################
# Collecte des fichiers
#####################################
valid_files=("$TEST_DIR_VALID"/*.deca)
invalid_files=("$TEST_DIR_INVALID"/*.deca)

# Vérifier qu'il y a au moins un fichier valide et un fichier invalid context
if [ ${#valid_files[@]} -eq 0 ]; then
    echo -e "${RED}Aucun fichier .deca valide trouvé dans $TEST_DIR_VALID.${NC}"
    exit 1
fi
if [ ${#invalid_files[@]} -eq 0 ]; then
    echo -e "${RED}Aucun fichier .deca invalide trouvé dans $TEST_DIR_INVALID.${NC}"
    exit 1
fi

# Choisir un fichier valide pour tester -r X
valid_test_file="${valid_files[0]}"
#####################################
# Fonction pour tester un fichier qui n'existe pas
#####################################
test_missing_file() {
    local missing_file="non_existent_file.deca"
    echo -e "${YELLOW}--- Test sur un fichier inexistant : $missing_file ---${NC}"

    # Construction de la commande
    local cmd=("$ROOT_DIR/src/main/bin/decac" "$missing_file")
    compilation_output=$("${cmd[@]}" 2>&1)
    exit_code=$?

    # On s'attend à ce que decac échoue (exit_code != 0)
    if [[ $exit_code -ne 0 ]]; then
        echo -e "${GREEN}[PASS] Le compilateur a correctement échoué sur un fichier inexistant.${NC}"
        echo -e "${YELLOW}Détails de l'erreur :${NC}"
        echo "$compilation_output"
        ((success_count++))
    else
        echo -e "${RED}[FAIL] Le compilateur n'a PAS échoué sur un fichier inexistant.${NC}"
        echo -e "${YELLOW}Sortie obtenue :${NC}"
        echo "$compilation_output"
        ((fail_count++))
    fi
}



#####################################
# Fonction pour tester un fichier avec des erreurs sémantiques spécifiques
#####################################
test_semantic_errors() {
    local semantic_file="$TEST_DIR_INVALID/semantic_error.deca"
    echo -e "${YELLOW}--- Test sur un fichier avec des erreurs sémantiques : $semantic_file ---${NC}"

    # Création d'un fichier avec des erreurs sémantiques spécifiques
    cat > "$semantic_file" <<EOL
class Test {
    int a;
    int a; // Erreur : variable redéfinie
}
main {}
EOL

    # Test de compilation
    compile_deca "" "$semantic_file" "Fichier avec erreur sémantique" "fail"

    # Nettoyage
    rm -f "$semantic_file"
}

#####################################
# Fonction pour tester un fichier non lisible
#####################################
test_unreadable_file() {
    local unreadable_file="src/test/deca/unreadable_file.deca"
    echo -e "${YELLOW}--- Test sur un fichier non lisible : $unreadable_file ---${NC}"

    # Création d'un fichier temporaire et suppression des permissions de lecture
    echo "main {}" > "$unreadable_file"
    chmod -r "$unreadable_file"

    # Test de compilation
    compile_deca "" "$unreadable_file" "Fichier non lisible" "fail"

    # Restauration des permissions et nettoyage
    chmod +r "$unreadable_file"
    rm -f "$unreadable_file"
}

#########################################################
# DÉBUT DES TESTS
#########################################################
echo -e "${YELLOW}=== Début des tests sur les options de decac ===${NC}"

# 1) Tester decac -b (banner)
test_banner

# 2) Tester decac -p (parse) sur chaque fichier valide
for file in "${valid_files[@]}"; do
    test_parse_on_valid "$file"
done

# 3) Tester decac -r X pour X de 4 à 16, sur un fichier valide
for X in {4..16}; do
    test_registers "$X" "$valid_test_file"
done

#) Tester decac avec un depassement d nombre des registres
test_registers 5 "src/test/deca/codegen/perf/provided/ln2_fct.deca"

# 5) Tester options incompatibles (ex. -b -p)
test_incompatible_options "-b -p"

# 6) Compiler les fichiers invalid context : on attend un échec
echo -e "${YELLOW}=== Tests de compilation sur les invalides (context/invalid) ===${NC}"
for inv_file in "${invalid_files[@]}"; do
    test_invalid_compile "$inv_file"
done
# 7) Tester decac sur un fichier qui n'existe pas
test_missing_file


# 8) Tester un fichier avec des erreurs sémantiques
test_semantic_errors

# 9) Tester un fichier non lisible
test_unreadable_file

#########################################################
# RÉSUMÉ FINAL
#########################################################
echo ""
echo -e "${YELLOW}=== Résumé des tests decac ===${NC}"
echo -e "${GREEN}Tests réussis : $success_count${NC}"
echo -e "${RED}Tests échoués : $fail_count${NC}"

if [ "$fail_count" -ne 0 ]; then
    echo -e "${RED}[ERROR] Certains tests ont échoué.${NC}"
    exit 1
else
    echo -e "${GREEN}[SUCCESS] Tous les tests ont réussi.${NC}"
    exit 0
fi
