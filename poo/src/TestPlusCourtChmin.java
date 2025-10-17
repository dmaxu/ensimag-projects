import java.io.FileNotFoundException;
import java.util.zip.DataFormatException;

/**
 * Classe pour tester la classe CalculateurChemin.
 */

public class TestPlusCourtChmin {
    /**
     * Constructeur par défaut de la classe TestLecteurDonnees.
     */
    public TestPlusCourtChmin() {
        // Constructeur par défaut
    }

    /**
     * Méthode principale pour exécuter le TestLecteurDonnees.
     * 
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
        Simulateur affichage = new Simulateur(LecteurDonnees.getDonnees(),100);
        Robot robot = LecteurDonnees.getDonnees().getRobots()[1];
        Incendie incendie = (Incendie) LecteurDonnees.getDonnees().getIncendies().toArray()[4];
        Case destination = incendie.getPosition();
        Chemin chemin = CalculateurChemin.calculerChemin(LecteurDonnees.getDonnees().getCarte(), robot, destination);
        chemin.toEvenement(affichage, robot);
    }
}
