# Ensimag 2A POO - TP 2018/19
# ============================
#
# Ce Makefile permet de compiler le test de l'ihm en ligne de commande.
# Alternative (recommandee?): utiliser un IDE (eclipse, netbeans, ...)
# Le but est ici d'illustrer les notions de "classpath", a vous de l'adapter
# a votre projet.
#
# Organisation:
#  1) Les sources (*.java) se trouvent dans le repertoire src
#     Les classes d'un package toto sont dans src/toto
#     Les classes du package par defaut sont dans src
#
#  2) Les bytecodes (*.class) se trouvent dans le repertoire bin
#     La hierarchie des sources (par package) est conservee.
#     L'archive lib/gui.jar contient les classes de l'interface graphique
#
# Compilation:
#  Options de javac:
#   -d : repertoire dans lequel sont places les .class compiles
#   -classpath : repertoire dans lequel sont cherches les .class deja compiles
#   -sourcepath : repertoire dans lequel sont cherches les .java (dependances)

all: testInvader testLecture

testInvader:
	javac -d bin -classpath lib/gui.jar -sourcepath src src/TestInvader.java

testLecture:
	javac -d bin -classpath lib/gui.jar -sourcepath src src/TestLecteurDonnees.java

testChemin:
	javac -d bin -classpath lib/gui.jar -sourcepath src src/TestPlusCourtChmin.java

testChef:
	javac -g -d bin -classpath lib/gui.jar -sourcepath src src/TestChef.java

# Execution:
# on peut taper directement la ligne de commande :
#   > java -classpath bin:lib/gui.jar TestInvader
# ou bien lancer l'execution en passant par ce Makefile:
#   > make exeInvader
exeInvader: 
	java -classpath bin:lib/gui.jar TestInvader

exeLecture: 
	java -classpath bin:lib/gui.jar TestLecteurDonnees cartes/carteSujet.map

exeChemin: 
	java -classpath bin:lib/gui.jar TestPlusCourtChmin cartes/carteSujet.map

exeChef: 
	java -classpath bin:lib/gui.jar TestChef cartes/carteSujet.map

# Javadoc generation:
javadoc:
	javadoc -d doc -classpath lib/gui.jar -sourcepath src src/*.java

clean:
	rm -rf bin/*
