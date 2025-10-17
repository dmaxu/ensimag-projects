import java.util.HashSet;

/**
 * Classe abstraite représentant un chef pompier qui gère les robots et les
 * incendies.
 */
public abstract class Chef {
    /**
     * Le tableau des robots gérés par le chef pompier.
     */
    protected Robot[] robots;

    /**
     * Le HashSet des incendies à éteindre.
     */
    protected HashSet<Incendie> incendies;

    /**
     * Le simulateur de la simulation.
     */
    protected Simulateur simulateur;

    /**
     * La carte de la simulation.
     */
    protected Carte carte;

    /**
     * Constructeur de la classe Chef.
     * 
     * @param carte      La carte.
     * @param robots     Le tableau des robots.
     * @param incendies  Le HashSet des incendies.
     * @param simulateur Le simulateur.
     */
    public Chef(Carte carte, Robot[] robots, HashSet<Incendie> incendies, Simulateur simulateur) {
        this.robots = robots;
        this.incendies = incendies;
        this.simulateur = simulateur;
        this.carte = carte;
    }

    /**
     * Méthode abstraite pour gérer les interventions des robots sur les incendies.
     * 
     * @return true si il reste des incendies à éteindre, false sinon.
     */
    public abstract boolean gererInterventions();
    /**
     * retourne la liste des robots
     * @return la liste des robots
     */
    public Robot[] getRobots(){
        return this.robots;
    }
    /**
     * La Hashset des incendies
     * @return La Hashset des incendies
     */
    public HashSet<Incendie> getIncendies(){
        return this.incendies;
    }
    /**
     * Ajoute un robot à la liste
     * @param i son indice
     * @param robot le robot
     */
    public void setRobot(int i, Robot robot){
        this.robots[i]=robot;
    }
    /**
     * Attribue la carte à l'objet Chef
     * @param carte la carte principale
     */
    public void setCarte(Carte carte){
        this.carte=carte;
    }
    /**
     * Set incendies
     * @param incendies la liste des incendies
     */
    public void setIncendies(HashSet<Incendie> incendies){
        this.incendies=incendies;
    }

}
