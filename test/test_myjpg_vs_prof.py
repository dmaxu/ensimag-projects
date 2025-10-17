import sys,subprocess,os
from PIL import Image
from PIL import ImageChops
from imgcompare import is_equal
from imgcompare import image_diff_percent
import glob

def condition_ss_echant(h_y, v_y, h_cb, v_cb, h_cr, v_cr):
    # Vérification des valeurs de h et v comprises entre 1 et 4
    factors = [h_y, v_y, h_cb, v_cb, h_cr, v_cr]
    if any(f < 1 or f > 4 for f in factors):
        return False

    # La somme des produits hi*vi doit être inférieure ou égale à 10
    if (h_y * v_y + h_cb * v_cb + h_cr * v_cr) > 10:
        return False

    # Les facteurs d'échantillonnage des chrominances doivent diviser parfaitement ceux de la luminance
    if h_y % h_cb != 0 or h_y % h_cr != 0 or v_y % v_cb != 0 or v_y % v_cr != 0:
        return False

    if h_cb!=h_cr or v_cb!=v_cr:
        return False

    return True

def generateur_ss_echant():
    # valeurs possibles pour h et v
    h = [1, 2, 3, 4]
    v = [1, 2, 3, 4]
    liste_ss_echant = []

    for h_y in h:
        for v_y in v:
            for h_cb in h:
                for v_cb in v:
                    for h_cr in h:
                        for v_cr in v:
                            if condition_ss_echant(h_y, v_y, h_cb, v_cb, h_cr, v_cr):
                                ss_echant = f"{h_y}x{v_y},{h_cb}x{v_cb},{h_cr}x{v_cr}"
                                liste_ss_echant.append(ss_echant)

    return liste_ss_echant


def print_green(text):
    print(f"\033[92m{text}\033[0m",end='')

def print_red(text):
    print(f"\033[91m{text}\033[0m",end='')


def recup_pgm_ppm(directory,col):
    # Utiliser glob pour rechercher les fichiers .pgm et .ppm
    if col:
        files = glob.glob(os.path.join(directory, '*.ppm'))
    else:
        files = glob.glob(os.path.join(directory, '*.pgm'))

    return files

os.chdir(os.path.dirname(os.path.abspath(__file__)))

# Récupérer le répertoire de travail actuel
current_directory = os.getcwd()
color=False
if len(sys.argv)>2 and sys.argv[2]=="-test_col":
    color=True
    liste_ss_echant = generateur_ss_echant()
# Obtenir la liste des fichiers .pgm et .ppm
fichier_test=sys.argv[1]
l_fichier = recup_pgm_ppm(current_directory+"/"+fichier_test,color)



for file in l_fichier:
    out1=file[0:len(file)-4]+"_prof.jpg"
    out2=file[0:len(file)-4]+"_nous.jpg"
    
    if color:
        print(f"Pour l'image {file}")
        for ss_echant in liste_ss_echant:
            subprocess.check_call([r"./ppm2blabla",file ,f"--outfile={out1}", f"--sample={ss_echant}"]) #jpg des profs
            subprocess.check_call([r"./ppm2jpeg", file,f"--outfile={out2}", f"--sample={ss_echant}"]) #le notre
            if is_equal(out1,out2,tolerance=1):
                print_green("PASSED ")
            else:
                print_red("FAILED ")
            print(f"pour le sous-échantillonnage : {ss_echant}")
            print(f"Les images sont les mêmes à {round((1-image_diff_percent(out1,out2))*100,3)}%")

    else :
        subprocess.check_call([r"./ppm2blabla",file ,f"--outfile={out1}"]) #jpg des profs
        subprocess.check_call([r"./ppm2jpeg", file,f"--outfile={out2}"]) #le notre  
        if is_equal(out1, out2,tolerance=1):
            print_green("PASSED ")
        else:
            print_red("FAILED ")
        print(f"Pour l'image {file}, similaire à {round((1-image_diff_percent(out1,out2))*100,3)}%")

for file in glob.glob(os.path.join(current_directory+"/"+fichier_test,"*.jpg")):
    os.remove(file)

for file in glob.glob(os.path.join(current_directory+"/"+fichier_test,"*.bla")):
    os.remove(file)
