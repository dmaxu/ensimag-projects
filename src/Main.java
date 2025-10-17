import java.io.FileNotFoundException;
import java.util.zip.DataFormatException;

/**
 * Classe pour résoudre n'importe quel propositions d'incendies.
 */
public class Main{
    /**
     * Constructeur par défaut
     */
    public Main(){
        //Constructeur par défaut
    }
     /**
     * Méthode principale pour exécuter le Main.
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args){
        if (args.length < 2) {
            System.out.println("Syntaxe: ./run_main.sh <Carte> <nomDeStrategie>");
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
        String strategie = args[1];
        switch (strategie) {
            case "Simple":

            ChefSimple chef = new ChefSimple(LecteurDonnees.getDonnees().getCarte(),LecteurDonnees.getDonnees().getRobots(), LecteurDonnees.getDonnees().getIncendies(), affichage);
            affichage.ajouteEvenement(new EvenementChef(1, chef, 20, affichage));
                break;

            case "Evoluée":

            ChefEvolue chefEvolue = new ChefEvolue(LecteurDonnees.getDonnees().getCarte(),LecteurDonnees.getDonnees().getRobots(), LecteurDonnees.getDonnees().getIncendies(), affichage);
            affichage.ajouteEvenement(new EvenementChef(1, chefEvolue, 20, affichage));
                break;

            case "Collaboratif":
            
            ChefCollaboratif chefCollaboratif = new ChefCollaboratif(LecteurDonnees.getDonnees().getCarte(),LecteurDonnees.getDonnees().getRobots(), LecteurDonnees.getDonnees().getIncendies(), affichage);
            affichage.ajouteEvenement(new EvenementChef(1, chefCollaboratif, 20, affichage));
                break;
                
            default : 
            System.out.println("Mettez une stratégie mentionée dans la documentation.");
            System.exit(1);
        }
    }
}