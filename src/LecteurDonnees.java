import java.io.*;
import java.util.*;
import java.util.zip.DataFormatException;



/**
 * Lecteur de cartes au format spectifié dans le sujet.
 * Les données sur les cases, robots puis incendies sont lues dans le fichier,
 * puis construit une instance de la classe DonneesSimulation.
 * A noter: pas de vérification sémantique sur les valeurs numériques lues.
 *
 * Les différentes méthodes implémentées lisent puis retourne les données à une instance 
 * DonnéesSimulation.
 */
public class LecteurDonnees {

    /**
     * Lit et retourne les différentes donnes d'un fichier de donnees (cases,
     * robots et incendies).
     * Ceci est méthode de classe; utilisation:
     * LecteurDonnees.lire(fichierDonnees)
     * @param fichierDonnees nom du fichier à lire
     * @throws FileNotFoundException si le fichier ne peut être ouvert
     * @throws DataFormatException si le format du fichier est incorrect
     */
    public static void lire(String fichierDonnees)
        throws FileNotFoundException, DataFormatException {
        System.out.println("\n == Lecture du fichier" + fichierDonnees);
        LecteurDonnees lecteur = new LecteurDonnees(fichierDonnees);
        lecteur.lireCarte();
        lecteur.lireIncendies();
        lecteur.lireRobots();
        scanner.close();
        System.out.println("\n == Lecture terminee");
    }
/*
    public static void aaah(String fichierDonnees){
        if(fichierExiste(fichierDonnees)){
            System.out.println("\n == Lecture du fichier" + fichierDonnees);
            LecteurDonnees lecteur = new LecteurDonnees(fichierDonnees);
            lecteur.lireCarte();
            lecteur.lireIncendies();
            lecteur.lireRobots();
            scanner.close();
            System.out.println("\n == Lecture terminee");   
        }
    }*/

    /**
     * Retourne les données lues par le lecteur.
     * @return les données lues par le lecteur
     */
    public static DonneesSimulation getDonnees() {
        return donnees;
    }

    /**
     * Retourne le fichier lu par le lecteur
     * @return Le fichier lu par le lecteur
     */
    public static String getFichier(){
        return fichier;
    }

    // Tout le reste de la classe est prive!

    private static Scanner scanner;
    private static DonneesSimulation donnees = new DonneesSimulation();
    private static String fichier;

    /**
     * Constructeur prive; impossible d'instancier la classe depuis l'exterieur
     * @param fichierDonnees nom du fichier a lire
     */
    private LecteurDonnees(String fichierDonnees)
        throws FileNotFoundException {
        scanner = new Scanner(new File(fichierDonnees));
        scanner.useLocale(Locale.US);
        fichier=fichierDonnees;
    }

    /**
     * Lit et met les donnees de la carte dans une instance de DonneesSimulation.
     * @throws ExceptionFormatDonnees
     */
    private void lireCarte() throws DataFormatException {
        ignorerCommentaires();
        try {
            int nbLignes = scanner.nextInt();
            int nbColonnes = scanner.nextInt();
            int tailleCases = scanner.nextInt();	// en m
            System.out.println("Carte " + nbLignes + "x" + nbColonnes
                    + "; taille des cases = " + tailleCases);
            Carte carte = new Carte(nbLignes, nbColonnes, tailleCases);
            for (int lig = 0; lig < nbLignes; lig++) {
                for (int col = 0; col < nbColonnes; col++) {
                    Case newcase = lireCase(lig, col);
                    carte.setCase(lig, col, newcase);
                }
            }
            donnees.setCarte(carte);

        } catch (NoSuchElementException e) {
            throw new DataFormatException("Format invalide. "
                    + "Attendu: nbLignes nbColonnes tailleCases");
        }
        // une ExceptionFormat levee depuis lireCase est remontee telle quelle
    }




    /**
     * Lit et retourne les donnees d'une case.
     */
    private Case lireCase(int lig, int col) throws DataFormatException {
        ignorerCommentaires();
        //System.out.print("Case (" + lig + "," + col + "): ");
        String chaineNature = new String();
        //		NatureTerrain nature;

        try {
            chaineNature = scanner.next();
            // si NatureTerrain est un Enum, vous pouvez recuperer la valeur
            // de l'enum a partir d'une String avec
            NatureTerrain nature = NatureTerrain.valueOf(chaineNature);

            verifieLigneTerminee();

            //System.out.print("nature = " + chaineNature);
            Case newcase = new Case(lig, col, nature);
            return newcase;

        } catch (NoSuchElementException e) {
            throw new DataFormatException("format de case invalide. "
                    + "Attendu: nature altitude [valeur_specifique]");
        }

    }


    /**
     * Lit les donnees des incendies.
     */
    private void lireIncendies() throws DataFormatException {
        ignorerCommentaires();
        try {
            int nbIncendies = scanner.nextInt();
            System.out.println("Nb d'incendies = " + nbIncendies);
            donnees.initincendies(nbIncendies);
            for (int i = 0; i < nbIncendies; i++) {
                lireIncendie(i);
            }

        } catch (NoSuchElementException e) {
            throw new DataFormatException("Format invalide. "
                    + "Attendu: nbIncendies");
        }
    }


    /**
     * Lit et ajoute les donnees du i-eme incendie dans une instance de type DonneesSimulation.
     * @param i
     */
    private void lireIncendie(int i) throws DataFormatException {
        ignorerCommentaires();
        //System.out.print("Incendie " + i + ": ");

        try {
            int lig = scanner.nextInt();
            int col = scanner.nextInt();
            int intensite = scanner.nextInt();
            if (intensite <= 0) {
                throw new DataFormatException("incendie " + i
                        + "nb litres pour eteindre doit etre > 0");
            }
            verifieLigneTerminee();

            /**System.out.println("position = (" + lig + "," + col
                    + ");\t intensite = " + intensite);*/
            Carte carte = donnees.getCarte();
            Case position = carte.getCase(lig, col);
            Incendie incendie = new Incendie(position, intensite);
            donnees.addincendie(incendie, i);

        } catch (NoSuchElementException e) {
            throw new DataFormatException("format d'incendie invalide. "
                    + "Attendu: ligne colonne intensite");
        }
    }


    /**
     * Lit les donnees des robots.
     */
    private void lireRobots() throws DataFormatException {
        ignorerCommentaires();
        try {
            int nbRobots = scanner.nextInt();
            System.out.println("Nb de robots = " + nbRobots);
            donnees.initrobots(nbRobots);
            for (int i = 0; i < nbRobots; i++) {
                lireRobot(i);
            }

        } catch (NoSuchElementException e) {
            throw new DataFormatException("Format invalide. "
                    + "Attendu: nbRobots");
        }
    }


    /**
     * Lit et ajoute les donnees du i-eme robot dans une instance de types DonnesSimulation.
     * @param i
     */
    private void lireRobot(int i) throws DataFormatException {
        ignorerCommentaires();
        //System.out.print("Robot " + i + ": ");

        try {
            int lig = scanner.nextInt();
            int col = scanner.nextInt();
            //System.out.print("position = (" + lig + "," + col + ");");
            String type = scanner.next();

            //System.out.print("\t type = " + type);


            // lecture eventuelle d'une vitesse du robot (double)
            //System.out.print("; \t vitesse = ");
            String s = scanner.findInLine("(\\d+)");	// 1 or more digit(s) ?
            // pour lire un flottant:    ("(\\d+(\\.\\d+)?)");
            if (s == null) {
                //System.out.print("valeur par defaut");
                Case position = donnees.getCarte().getCase(lig, col);
                switch (type) {
                    case "ROUES" -> {
                        RobotARoues robotaroues = new RobotARoues(position);
                        donnees.addrobot(robotaroues, i);
                    }
                    case "PATTES" -> {
                        RobotAPattes robotapattes = new RobotAPattes(position);
                        donnees.addrobot(robotapattes, i);
                    }
                    case "CHENILLES" -> {
                        RobotChenille robotchenille = new RobotChenille(position);
                        donnees.addrobot(robotchenille, i);
                    }
                    case "DRONE" -> {
                        RobotDrone drone = new RobotDrone(position);
                        donnees.addrobot(drone, i);
                    }
                    default -> throw new DataFormatException ("Le type de ce robot n'est pas connu dans la base de donné");
                }
                
            } else {
                double vitesse;
                vitesse = Double.parseDouble(s);
                //System.out.print(vitesse);
                Case position = donnees.getCarte().getCase(lig, col);
                switch (type) {
                    case "ROUES" -> {
                        RobotARoues robotaroues = new RobotARoues(position, vitesse);
                        donnees.addrobot(robotaroues, i);
                    }
                    case "PATTES" -> {
                        RobotAPattes robotapattes = new RobotAPattes(position, vitesse);
                        donnees.addrobot(robotapattes, i);
                    }
                    case "CHENILLES" -> {
                        RobotChenille robotchenille = new RobotChenille(position, vitesse);
                        donnees.addrobot(robotchenille, i);
                    }
                    case "DRONE" -> {
                        RobotDrone drone = new RobotDrone(position, vitesse);
                        donnees.addrobot(drone, i);
                    }
                    default -> throw new DataFormatException ("Le type de ce robot n'est pas connu dans la base de donné");
                }
            }
            verifieLigneTerminee();
            
            System.out.println();

        } catch (NoSuchElementException e) {
            throw new DataFormatException("format de robot invalide. "
                    + "Attendu: ligne colonne type [valeur_specifique]");
        }
    }




    /** Ignore toute (fin de) ligne commencant par '#' */
    private void ignorerCommentaires() {
        while(scanner.hasNext("#.*")) {
            scanner.nextLine();
        }
    }

    /**
     * Verifie qu'il n'y a plus rien a lire sur cette ligne (int ou float).
     * @throws ExceptionFormatDonnees
     */
    private void verifieLigneTerminee() throws DataFormatException {
        if (scanner.findInLine("(\\d+)") != null) {
            throw new DataFormatException("format invalide, donnees en trop.");
        }
    }
}
