import java.util.List;

/**
 * Classe représentant un chemin calculé pour un robot.
 */
public class Chemin {
    private List<Case> cases;
    private double tempsTotale;

    /**
     * Constructeur de la classe Chemin.
     * @param cases La liste des cases constituant le chemin.
     * @param tempsTotale Le temps nécessaire pour parcourir le chemin.
     */
    public Chemin(List<Case> cases, double tempsTotale) {
        this.cases = cases;
        this.tempsTotale = tempsTotale;
    }

    /**
     * Retourne la liste des cases constituant le chemin.
     * @return La liste des cases.
     */
    public List<Case> getCases() {
        return cases;
    }

    /**
     * Retourne le temps nécessaire pour parcourir le chemin.
     * @return Le temps nécessaire pour parcourir le chemin.
     */
    public double getTempsTotale() {
        return tempsTotale;
    }

    /**
     * Transforme le chemin en événements de déplacement et les ajoute au simulateur.
     * @param simulateur Le simulateur auquel ajouter les événements.
     * @param robot Le robot à déplacer.
     */

    public void toEvenement(Simulateur simulateur, Robot robot) {
        Case previousCase = robot.getPosition();
        Carte carte = simulateur.getDonnees().getCarte();

        for (Case c : cases) {
            Direction dir = getDirection(previousCase, c);
            double tempsDeplacement = robot.tempsDeplacement(c, carte);
            simulateur.ajouteEvenement(new EvenementDeplacement((long) Math.ceil(tempsDeplacement), dir, robot, carte, simulateur));
            previousCase = c;
        }
    }

    /**
     * Fait pareil que toEvenement mais utilise remplaceEvenement au lieu de ajouteEvenement 
     * @param simulateur le simulateur
     * @param robot le robot
     * */
    public void toEvenementSecond(Simulateur simulateur, Robot robot) {
        Case previousCase = robot.getPosition();
        Carte carte = simulateur.getDonnees().getCarte();

        for (Case c : cases) {
            Direction dir = getDirection(previousCase, c);
            double tempsDeplacement = robot.tempsDeplacement(c, carte);
            simulateur.remplaceEvenement(new EvenementDeplacement((long) Math.ceil(tempsDeplacement), dir, robot, carte, simulateur));
            previousCase = c;
        }
    }

    


    /**
     * Détermine la direction entre deux cases adjacentes.
     * @param from La case de départ.
     * @param to La case d'arrivée.
     * @return La direction entre les deux cases.
     */
    private Direction getDirection(Case from, Case to) {
        if (to.getLigne() == from.getLigne() - 1 && to.getColonne() == from.getColonne()) {
            return Direction.NORD;
        } else if (to.getLigne() == from.getLigne() + 1 && to.getColonne() == from.getColonne()) {
            return Direction.SUD;
        } else if (to.getLigne() == from.getLigne() && to.getColonne() == from.getColonne() + 1) {
            return Direction.EST;
        } else if (to.getLigne() == from.getLigne() && to.getColonne() == from.getColonne() - 1) {
            return Direction.OUEST;
        }
        throw new IllegalArgumentException("Les cases ne sont pas adjacentes.");
    }
}