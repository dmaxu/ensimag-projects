import java.io.FileNotFoundException;
import java.util.zip.DataFormatException;

/**
 * Classe pour tester la classe ChefSimple.
 */


public class TestChef {
    /**
     * Constructeur par défaut de la classe TestChefSimple.
     */
    public TestChef() {
        // Constructeur par défaut
    }

    /**
     * Méthode principale pour exécuter le TestChefSimple.
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
        Simulateur affichage = new Simulateur(LecteurDonnees.getDonnees(),1000);
        ChefSimple chef = new ChefSimple(LecteurDonnees.getDonnees().getCarte(),LecteurDonnees.getDonnees().getRobots(), LecteurDonnees.getDonnees().getIncendies(), affichage);
        affichage.ajouteEvenement(new EvenementChef(1, chef, 20, affichage));
        // ChefEvolue chefEvolue = new ChefEvolue(LecteurDonnees.getDonnees().getCarte(),LecteurDonnees.getDonnees().getRobots(), LecteurDonnees.getDonnees().getIncendies(), affichage);
        // affichage.ajouteEvenement(new EvenementChef(1, chefEvolue, 20, affichage));
        // ChefCollaboratif chefCollaboratif = new ChefCollaboratif(LecteurDonnees.getDonnees().getCarte(),LecteurDonnees.getDonnees().getRobots(), LecteurDonnees.getDonnees().getIncendies(), affichage);
        // affichage.ajouteEvenement(new EvenementChef(1, chefCollaboratif, 20, affichage));
    }
}
