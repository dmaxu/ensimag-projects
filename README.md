# Notre encodeur JPEG à nous

Bienvenue sur la page d'accueil de _votre_ projet JPEG, un grand espace de liberté, sous le regard bienveillant de vos enseignants préférés.
Le sujet sera disponible dès lundi à l'adresse suivante : [https://formationc.pages.ensimag.fr/projet/jpeg/jpeg/](https://formationc.pages.ensimag.fr/projet/jpeg/jpeg/).

Vous pouvez reprendre cette page d'accueil comme bon vous semble, mais elle devra au moins comporter les infos suivantes **avant la fin de la première semaine ** :

1. des informations sur le découpage des fonctionnalités du projet en modules, en spécifiant les données en entrée et sortie de chaque étape ;
2. (au moins) un dessin des structures de données de votre projet (format libre, ça peut être une photo d'un dessin manuscrit par exemple) ;
3. une répartition des tâches au sein de votre équipe de développement, comportant une estimation du temps consacré à chacune d'elle (là encore, format libre, du truc cracra fait à la main, au joli Gantt chart).

Rajouter **régulièrement** des informations sur l'avancement de votre projet est aussi **une très bonne idée** (prendre 10 min tous les trois chaque matin pour résumer ce qui a été fait la veille, établir un plan d'action pour la journée qui commence et reporter tout ça ici, par exemple).


# Contexte

Dans le cadre du module de première année Projet Logiciel en C nous avons choisi le sujet visant à créer un encodeur JPEG. Le but étant de pouvoir compresser des images initialement au format brut PPM (ou PGM) en fichier JPEG prenant moins de place en mémoire.

# Les structures utilisées

Avant de nous lancer dans le découpage en modules et d'expliquer le fonctionnemnt de chacun, il est important de définir les structures de données que nous avons utilisées.

### Structure Matrice
Tout d'abord, nous allons nous pencher sur les images en nuances de gris. Dans ce type d'image, pour stocker les pixels nous avons utilisé une structure Matrice, ayant trois attributs:
* la hauteur de l'image
* sa largeur
* un tableau de tableau dont les dimensions sont celles de l'image, rempli avec les pixels.

![Structure Matrice](https://gitlab.ensimag.fr/formationc/projet/jpeg/2024/team28/-/blob/master/Schema_struct/Struct_matrice.png)


### Structure Pixel
Ensuite, pour les images en couleurs, nous avons utilisé une structure légèrement différente. Pour la définir nous avons du créer une structure Pixel, possèdant trois arguments:
* R (pour red)
* G (pour green)
* B (pour blue)

### Structure Matrice_Couleur
Ensuite on a alors pu définir une structure Matrice_Couleur dont les deux premiers arguments sont la hauteur et la largeur de l'image, et le dernier qui est un tableau de tableau de structure Pixel. C'est-à-dire que chaque coefficient de la matrice ainsi crée est une structure Pixel (un peu comme un tuple de taille 3 entier).

![Structure Matrice_Couleur](https://gitlab.ensimag.fr/formationc/projet/jpeg/2024/team28/-/blob/master/Schema_struct/Struct_matrice_couleur.png)

### Structure cell
Cette structure est utilisée dans le module de sous-échantillonage, et va nous permettre de créer des listes chaînées de matrices 8x8. Elle possède 3 attributs : 
* un qui correspond à la structure Matrice 
* un qui est un pointeur qui va pointer vers la prochaine structure matrice de la chaîne ou NULL le cas échéant
* un est un entier (uint8_t) qui va nous permettre de savoir si la matrice sur laquelle on pointe est une matrice Y (attribut = 0), Cb (attribut = 1) ou Cr ( attribut = 2).

Voici un schéma de ce à quoi ressemble notre liste chaînée de structure Cell qui correspond à une liste chaînée de MCU 8x8

![Liste chaînée de Cell](https://gitlab.ensimag.fr/formationc/projet/jpeg/2024/team28/-/blob/master/Schema_struct/Liste_chainee.png)


# Découpage en modules

Pour avoir un projet des plus clair possible et afin de pouvoir se répartir les tâches nous avons découpé notre projet en plusieurs modules. 

## Module Matrice 
Tout d'abord nous avons crée un module matrice.h dans lequel nous définissons certaines des structures mentionnées au-dessus. De plus ce module nous a servi lors des débogages et lorsqu'il fallait vérifier nos résultats car on y a aussi définie des fonctions pour afficher des matrices sur la sortie standard. 

## Récupération des données, découpage en MCU, et conversion YCbCr

La première étape est de récupérer le fichier PPM passé sur la ligne de commande et plus particulièrement les pixels contenus dans ce fichier mais pas seulement. En effet, notre premier module va prendre le nom du fichier, l'ouvrir et commencer à aller chercher les informations importantes relatives au fichier telles que le type (PPM pour les images couleurs ou PGM pour les images aux nuances de gris) ainsi que ses dimensions. Ensuite il nous faut récupérer les données brutes de l'images c'est-à-dire les pixels de couleurs. Pour les stocker nous avons choisi de créer une matrice, sous forme de tableau de tableau d'entier signés sur 16 bits. Ainsi on parcour le fichier en stockant chaque pixel dans notre matrice. Enfin, pour pouvoir passer d'un module à l'autre toutes les informations récupérées, nous utilisons notre structure Matrice. Cela facilitera la manipulation de notre tableau de tableau par la suite.

Ensuite, il est nécessaires de découper notre image en MCU, qui sont des portions de l'image de base. Ainsi, une fois la matrice de pixels crées il nous faut donc la découper en plusieurs sous-matrices dont la taille va dépendre du sous-échantillonage choisi. A noté que pour des images en noir et blanc nous avons décidé de n'utiliser des MCU de tailles 8x8. Les sous-matrices obtenues (MCU) sont stockées dans un tableau, qui est donc un tableau de stuctures Matrice. Ce choix à été motivé par le fait qu'il serait surement plus facile d'itérer sur le tableau afin de réaliser les taĉhes nécessaires à la compression.

Enfin, dans le cas des images couleurs on va convertir notre MCU de pixels R G B en trois MCU Y, Cb et Cr. On prend alors en entrée une structure Matrice_Couleur qui est une MCU de pixels R G B, et on renvoit un tableau de taille 3 de structure Matrice contenant la matrice Y à l'indice 0, Cb à l'indice 1 et puis Cr à l'indice 2. 

## Sous-échantillonage

Le module suivant est le premier pas de la compression de notre image. En effet il vise à réduire le nombre de pixels en en prennant plusieurs à la fois et en faisant la moyenne. Cette compression se fait uniquement sur les composantes de chrominances Cb et Cr car l'oeil humain est moins sensible à la chrominance qu'à la luminance. Ainsi on va pouvoir réduire la taille de notre image sans pour autant beaucoup en affecter la qualité.
Dans le cas d'une image en noir et blanc, ce module va prendre en entrée un tableau de structure Matrice toutes de taille 8x8, et renvoit une liste chaînée de structure Matrice de taille 8x8 également (Car on ne fait pas de sous-échantillonage pour les images en noir et blanc).
Dans le cas d'une image couleur, ce module prend en entrée un tableau de taille trois contenant trois structure Matrice, une pour Y, une pour Cb et une pour Cr. La matrice Y n'est pas modifiée pour les raisons mentionnées plus haut. Par contre les matrices Cb et Cr vont elles être sous-échantionné selon les facteurs pouvant être passés comme options en lançant le programme '''ppm2jpg --sample=h1xv1,h2xv2,h3xv3'''. Nous avons décidé que si cette option ne figurait pas au lancement de la commande, par défault aucun sous-échantillonage ne serait appliqué. 

## DCT (Transformée en cosinus dicrète)

Ce module va traiter du changement de domaine de nos MCU dans le domaine fréquentiel grâce à une transformée en cosinus discrète.
Pour réaliser cette tâche, le module prend en entrée une structure Matrice, et renvoit une structure Matrice mais dont les coefficients sont des fréquences. 
Afin d'optimiser le temps de calcul de ce module nous avonc utiliser une technique de mémoïsation qui a consisté à calculer les différentes valeurs de cosinus au préalable et à les stocker dans un tableau. Ainsi nous avons simplement à aller les chercher au besoins et nous n'avons pas besoin de re-calculer ces cosinus à chaque fois.

## Zig-Zag et quantification

Le prochain module est en deux parties. La première est la phase zig-zag, qui permet de transformer une matrice 8x8 en un vecteur de 64 composantes. Elle prend donc en entrée un tableau de tableau d'entier, et renvoie un tableau de int16_t.
Ensuite, vient la phase de quantification lors de laquelle on va diviser toutes les composantes du vecteur par des coefficients trouvés dans les tables de quantification qui nous sont données. Ainsi on obtient en sortie un tableau de int16_t. 

## Codage de Huffman

Le programme huffman.c va construire les arbres de huffman données dans le module htables.h puis à partir de ces arbres il va constuire des tableaux qui contiendront les codages de huffman de chaque symboles. Ces tables seront stockés dans le modules huffdic et utilisés dans pour l'encodage RLE.

## Codage RLE

Ce module s'occupe de l'encodage RLE de chaque MCU qu'il prend en entré et il écrit cet encodage sur le flux. L'écriture dans le flux se fait octet par octet à l'aide d'un buffer ce qui permet d'éviter de stocker le codage entier de chaque MCU.

## Ecriture de l'entête

Ce module va prendre en entrée le fichier .jpg dans lequel on veut écrire, et va écrire les données nécessaires à l'entête JPEG.
De plus il va afficher les balises de début et fin d'image.

# Test

Nous avons pu faire un dossier test permettant de comparer nos résultats avec ceux des professeurs. Dans ce dossier doit s'y trouver notre exécutable et le leur. Il doit aussi s'y trouver des dossiers d'images .ppm et .pgm pour bien sûr tester dessus. Le fichier test a été codé en python. Pour exécuter des tests il faudra se placer dans le dossier test et exécuter le code python en passant bien en **1er argument le dossier d'image** à tester. Lorsqu'il n'y a pas -test_col en 2e argument, les tests se feront que sur les images .pgm. Si l'argument -**test_col est passé en 2e paramètre, les tests se feront sur les .ppm** et testeront le résultat pour chaque sous-échantillonnage possible. Il faudra donner tous les droits (chmod 777) aux exécutables et installer les bibliothèques nécessaires (pip install imgcompare).

# Répartition du travail

* Récuperation des données : Benjamin
* Découpage en MCU + conversion YCbCr : Benjamin
* Sous-échantillonage : Ryan
* DCT : Mehdi
* Zig-zag + quantification : Ryan 
* Codage de Huffman : Mehdi
* RLE : Mehdi et Ryan
* Ecriture de l'entête : Mehdi
* Ecriture du main : tous les 3
* Test : Ryan

# Liens utiles

- Bien former ses messages de commits : [https://www.conventionalcommits.org/en/v1.0.0/](https://www.conventionalcommits.org/en/v1.0.0/) ;
- Problème relationnel au sein du groupe ? Contactez [Pascal](https://fr.wikipedia.org/wiki/Pascal,_le_grand_fr%C3%A8re) !
- Besoin de prendre l'air ? Le [Mont Rachais](https://fr.wikipedia.org/wiki/Mont_Rachais) est accessible à pieds depuis la salle E301 !
- Un peu juste sur le projet à quelques heures de la deadline ? Le [Montrachet](https://www.vinatis.com/achat-vin-puligny-montrachet) peut faire passer l'envie à vos profs de vous mettre une tôle !