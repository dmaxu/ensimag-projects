

/**
 * Cette classe est une sous-classe de Evenement qui a pour but de faire déplacer les robots
 */

public class EvenementDeplacement extends Evenement { 
    private Carte carte; // La carte du jeu.
    private Robot robot; // Le robot qui doit effectuer les déplacements.
    private Direction dir; //La direction que doi prendre le robot

    /**
     * Constructeur de la classe EvenementDeplacement
     * @param tempsDeplacement Le temps de déplacement du robot
     * @param dir La position à atteindre apres le déplacement
     * @param robot Le robot qui subit l'evenement
     * @param carte La carte du jeu
     * @param sim Le simulateur
     */
    public EvenementDeplacement(long tempsDeplacement, Direction dir, Robot robot, Carte carte, Simulateur sim) {
        super(sim.getDate(), sim);
        this.dir = dir;
        this.robot = robot;
        this.carte = carte;
        // La date d'execution de l'évènement est la date de disponibilité du robot ou la date acutelle du simulateur si le robot est disponible
        long dateExecution = Math.max(sim.getDate(), robot.getDateDisponibilite());
        dateExecution += (long) Math.ceil(tempsDeplacement);
        super.setDate(dateExecution);
        robot.setDateDisponibilite(dateExecution);
    }

    @Override
    public void execute() {
        
        if (!this.carte.voisinExiste( this.robot.getPosition(), dir) ){
            System.err.println("!!! La case voisine n'est pas sur la carte !!!");
            return; //chercher comment arreter directement le programme a parir de ce point.
        }
        System.out.println(" Click!... Déplacement");
        Case voisinCase = this.carte.getVoisin(this.robot.getPosition(), this.dir);
        this.robot.setPosition(voisinCase);
        //Reaffichage
        this.getSim().afficheTout();
        //System.out.println("Position du "+robot+" :"+robot.getPosition().getLigne()+" "+robot.getPosition().getColonne());
    }
    /**
     * retourne la direction que prend le robot
     * @return la direction que doit prendre 
     */
    public Direction getDirection(){
        return this.dir;
    }
    /**
     * retourne le robot à deplacer
     * @return le robot à déplacer
     */
    public Robot getRobot(){
        return this.robot;
    }
    /**
     * set le robot à déplacer
     * @param r le robot
     */
    public void setRobot(Robot r){
        this.robot=r;
    }
    /**
     * Set la carte sur laquelle il y'a un déplacement
     * @param carte la carte 
     */
    public void setCarte(Carte carte ){
        this.carte=carte;
    }
}
