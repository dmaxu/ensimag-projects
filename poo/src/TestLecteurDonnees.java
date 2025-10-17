import java.io.FileNotFoundException;
import java.util.zip.DataFormatException;

/**
 * Classe pour tester la fonctionnalité LecteurDonnees.
 */
public class TestLecteurDonnees {
    /**
     * Constructeur par défaut de la classe TestLecteurDonnees.
     */
    public TestLecteurDonnees() {
        // Constructeur par défaut
    }

    /**
     * Méthode principale pour exécuter le TestLecteurDonnees.
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntaxe: java TestLecteurDonnees <nomDeFichier>");
            System.exit(1);
        }

        try {
            LecteurDonnees.lire(args[0]);
        } catch (FileNotFoundException e) {
            System.out.println("fichier " + args[0] + " inconnu ou illisible");
        } catch (DataFormatException e) {
            System.out.println("\n\t**format du fichier " + args[0] + " invalide: " + e.getMessage());
        }
        /**Scenario 1 
        int Nbr=4;
        Simulateur affichage = new Simulateur(LecteurDonnees.getDonnees(),Nbr );

        for (int i = 1; i < 5; i++) {
            affichage.ajouteEvenement( new EvenementDeplacement(i, Direction.NORD, LecteurDonnees.getDonnees().getRobots()[0], LecteurDonnees.getDonnees().getcarte(), affichage));    
        }*/

        /*Scenario 2 */
        int Nbr = 100;
        Simulateur affichage = new Simulateur(LecteurDonnees.getDonnees(),Nbr );
        affichage.ajouteEvenement(new EvenementDeplacement(1, Direction.NORD, LecteurDonnees.getDonnees().getRobots()[1],LecteurDonnees.getDonnees().getCarte(),affichage));
        affichage.ajouteEvenement(new EvenementExtinction(affichage, (Incendie)LecteurDonnees.getDonnees().getIncendies().toArray()[0], LecteurDonnees.getDonnees().getRobots()[1],true));
        affichage.ajouteEvenement(new EvenementDeplacement(3, Direction.OUEST, LecteurDonnees.getDonnees().getRobots()[1],LecteurDonnees.getDonnees().getCarte(),affichage));
        affichage.ajouteEvenement(new EvenementDeplacement(4, Direction.OUEST, LecteurDonnees.getDonnees().getRobots()[1],LecteurDonnees.getDonnees().getCarte(),affichage));
        affichage.ajouteEvenement(new EvenementRemplissage(LecteurDonnees.getDonnees().getCarte(), LecteurDonnees.getDonnees().getRobots()[1],  affichage));
        affichage.ajouteEvenement(new EvenementDeplacement(6, Direction.EST, LecteurDonnees.getDonnees().getRobots()[1],LecteurDonnees.getDonnees().getCarte(),affichage));
        affichage.ajouteEvenement(new EvenementDeplacement(7, Direction.EST, LecteurDonnees.getDonnees().getRobots()[1],LecteurDonnees.getDonnees().getCarte(),affichage));
    }
}

