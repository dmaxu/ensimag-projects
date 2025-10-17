import subprocess
import pandas as pd
import matplotlib.pyplot as plt
import re

taille_l_cache=64/8

nom_test=input("Test pour quel algo ? (recu ,itera, cache_aware, cache_obv) : ")
while nom_test not in ["recu","itera", "cache_aware", "cache_obv"]:
    print("La test choisi n'existe pas, veuillez saisir une bonne valeur")
    nom_test=input("Test pour quel algo ? (itera, cache_aware, cache_obv) : ")


def f_itera(x:tuple):
    return str(int((3*x[0]*x[1])/taille_l_cache))

def f_cache_aware(x:tuple):
    return str(int((x[0]*x[1])/taille_l_cache))

def f_cache_obv(x:tuple):
    return str(int((x[0]*x[1])/taille_l_cache))



def f_cout_theo(x:tuple):
    if nom_test=="itera":
        return f_itera(x)
    elif nom_test=="cache_aware":
        return f_cache_aware(x)
    else :
        return f_cache_obv(x)


l_test=[(1000,1000),(2000,1000),(4000,1000),(2000,2000),(4000,4000),(6000,6000),(8000,8000)]
#l_test=[(10000,1000)]
res=""
l_def_cache_d1=[]
l_val_theo=[]

for i in l_test :
    command = "valgrind --tool=cachegrind --D1=4096,4,64 ./distanceEdition ../tests/ba52_recent_omicron.fasta 153 "+str(i[0])+" ../tests/wuhan_hu_1.fasta 116 "+str(i[1])+" 2>&1 | grep -E 'I\\s+refs|D\\s+refs|D1\\s+misses'"
    command_def_cache="valgrind --tool=cachegrind --D1=4096,4,64 ./distanceEdition ../tests/ba52_recent_omicron.fasta 153 "+str(i[0])+" ../tests/wuhan_hu_1.fasta 116 "+str(i[1])+" 2>&1 | grep 'D1  misses' | sed -E 's/.*D1  misses:\s+([0-9,]+).*/\1/'"
    res+=f"Pour le test {i} : \n"
    res+=subprocess.run(command, shell=True, capture_output=True, text=True,check=False).stdout
    if nom_test!="recu":
        res+="La valeur théorique est "+f_cout_theo(i)+"\n\n"
        l_val_theo.append(int(f_cout_theo(i)))
        cache_misses = re.search(r'D1\s+misses:\s+([\d,]+)', subprocess.run(command, shell=True, capture_output=True, text=True,check=False).stdout)  # Adapter l'expression selon la sortie réelle
        
        # Afficher le résultat de l'expression régulière pour diagnostic
        print("Cache Misses Regex Match:", cache_misses)
        
        # Si un match est trouvé, extraire et ajouter la valeur
        if cache_misses:
            l_def_cache_d1.append(int(cache_misses.group(1).replace(",", "")))


if l_def_cache_d1:
    x_labels_str = [str(tup) for tup in l_test]
    # Création du graphique avec les deux courbes
    plt.plot(x_labels_str, l_def_cache_d1, label="Valgrind", marker='o')
    plt.plot(x_labels_str, l_val_theo, label="Théorique", marker='x')

    # Configuration des axes et titre
    plt.xlabel("Test pour le couple (M,N)")
    plt.ylabel("Défauts de cache")
    plt.title("Comparaison des défauts de caches réelle vs théorique\n"+nom_test)
    plt.legend()

    # Rotation des étiquettes de l'axe X pour plus de lisibilité
    plt.xticks(rotation=15)

    # Affichage du graphique
    plt.savefig(f"./plot_{nom_test}.png")
    print(f"Graphique sauvegardé sous 'plot_{nom_test}.png'")
    plt.close()

subprocess.run("rm *.out.*", shell=True)

with open("test_"+nom_test,"w") as file:

    file.write(res)
    file.close()
