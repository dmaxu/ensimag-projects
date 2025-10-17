import java.util.HashSet;

/**
 * Cette classe est la classe principale regroupant toutes les données du problème.
 */
public class DonneesSimulation{

    private Robot[] robots;
    private HashSet<Incendie> incendies;
    private Carte carte;


    /** Constructeur par défaut de la classe DonneesSimulation */
    public DonneesSimulation(){
        this.robots = null;
        this.incendies = null;
        this.carte = null;
    }

    /** Retourne la liste des robots 
     * @return la liste des robots
    */
    public Robot[] getRobots(){
        return this.robots;
    }

    /** Initialise la liste des robots 
     * @param nbRobots le nombre de robots
    */
    public void initrobots(int nbRobots){
        this.robots = new Robot[nbRobots];
    }
    
    /** Retourne la Carte 
     * @return la ligne de la Carte
    */
    public Carte getCarte(){
        return this.carte;
    }

    /** Retourne la liste des incendies 
     * @return la liste des incendies
    */

    public HashSet<Incendie> getIncendies(){
        return this.incendies;
    }

    /** Initialise la liste des incendies 
     * @param nbIncendies le nombre d'incendies
    */
    public void initincendies(int nbIncendies){
        this.incendies = new HashSet<Incendie>(nbIncendies);
    }

    /** Ajoute un nouveau robot à la liste des robots 
     * @param robot le robot à ajouter
     * @param indice l'indice du robot
    */

    public void addrobot(Robot robot, int indice){
        this.robots[indice] = robot;
    }

    /** Ajoute un nouvel incendie à la liste des incendies
     * @param incendie l'incendie à ajouter
     * @param indice l'indice de l'incendie
    */

    public void addincendie(Incendie incendie, int indice){
        this.incendies.add(incendie);
        
    }

    /** Ajoute de la carte aux données 
     * @param carte la carte à ajouter
    */
    public void setCarte(Carte carte){
        this.carte = carte;
    }
}