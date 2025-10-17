import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Classe représentant le chef qui peut envoyer plusieurs robots sur le meme
 * incendie. Il choisit l'incendie le plus proche du robot
 */
public class ChefCollaboratif extends Chef {

    /**
     * Constructeur de la classe ChefCollaboratif.
     * 
     * @param carte      La carte.
     * @param robots     Le tableau des robots.
     * @param incendies  Le HashSet des incendies.
     * @param simulateur Le simulateur.
     */
    public ChefCollaboratif(Carte carte, Robot[] robots, HashSet<Incendie> incendies, Simulateur simulateur) {
        super(carte, robots, incendies, simulateur);
    }

    /**
     * Méthode pour gérer les interventions des robots sur les incendies.
     */
    @Override
    public boolean gererInterventions() {
        for (Robot robot : robots) {
            Map<Incendie, Chemin> chemins = new HashMap<>();
            // Cas où un robot n'est pas occupé et a de l'eau, il peut éteindre un incendie
            if ((robot.getDateDisponibilite() < simulateur.getDate()) && robot.getVolumeActuelle() > 0) {
                // Pour avoir un incendie à étiendre
                for (Incendie incendie : incendies) {
                    Chemin chemin = CalculateurChemin.calculerChemin(carte, robot, incendie.getPosition());
                    if (chemin != null) {
                        chemins.put(incendie, chemin);
                    }

                }
                if (!chemins.isEmpty()) {
                    Incendie incendieLePlusProche = null;
                    Chemin cheminLePlusCourt = null;
                    double tempsMin = Double.POSITIVE_INFINITY;

                    for (Map.Entry<Incendie, Chemin> entry : chemins.entrySet()) {
                        double temps = entry.getValue().getTempsTotale();
                        if (temps < tempsMin) {
                            tempsMin = temps;
                            incendieLePlusProche = entry.getKey();
                            cheminLePlusCourt = entry.getValue();
                        }
                    }

                    if (incendieLePlusProche != null) {
                        //System.err.println("j arrive ici ohh moi "+robot+ " sur "+incendieLePlusProche);
                        cheminLePlusCourt.toEvenementSecond(simulateur, robot);
                        simulateur.remplaceEvenement(
                                new EvenementExtinction(simulateur, incendieLePlusProche, robot, true));
                    }
                }

            }
        }
        //System.out.println("j arrive malheureseumen,t");
        return (incendies.size() > 0);

    }
}