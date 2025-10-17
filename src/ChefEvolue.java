import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Classe représentant le chef pompier évolué qui gère les robots et les
 * incendies.
 */
public class ChefEvolue extends Chef {

    /**
     * Constructeur de la classe ChefEvolue.
     * 
     * @param carte      La carte.
     * @param robots     La liste des robots.
     * @param incendies  La liste des incendies.
     * @param simulateur Le simulateur.
     */
    public ChefEvolue(Carte carte, Robot[] robots, HashSet<Incendie> incendies, Simulateur simulateur) {
        super(carte, robots, incendies, simulateur);
    }

    @Override
    public boolean gererInterventions() {
        for (Incendie incendie : incendies) {
            if (!incendie.estAffecte()) {
                Map<Robot, Chemin> chemins = new HashMap<>();
                for (Robot robot : robots) {
                    if ((robot.getDateDisponibilite() < simulateur.getDate()) && robot.getVolumeActuelle() > 0) {
                        Chemin chemin = CalculateurChemin.calculerChemin(carte, robot, incendie.getPosition());
                        if (chemin != null) {
                            chemins.put(robot, chemin);
                        }
                    }
                }

                if (!chemins.isEmpty()) {
                    Robot robotLePlusProche = null;
                    Chemin cheminLePlusCourt = null;
                    double tempsMin = Double.POSITIVE_INFINITY;

                    for (Map.Entry<Robot, Chemin> entry : chemins.entrySet()) {
                        double temps = entry.getValue().getTempsTotale();
                        if (temps < tempsMin) {
                            tempsMin = temps;
                            robotLePlusProche = entry.getKey();
                            cheminLePlusCourt = entry.getValue();
                        }
                    }

                    if (robotLePlusProche != null) {
                        incendie.affecter(true);
                        cheminLePlusCourt.toEvenementSecond(simulateur, robotLePlusProche);
                        simulateur.remplaceEvenement(
                                new EvenementExtinction(simulateur, incendie, robotLePlusProche, true));
                    }
                }
            }
        }
        return (incendies.size() > 0);
    }
}
