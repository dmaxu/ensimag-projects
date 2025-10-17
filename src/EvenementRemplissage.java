
/**
 * Cette classe implémente l'évènement de remplissage du réservoir d'eau.
 */
public class EvenementRemplissage extends Evenement {

    private Carte carte;
    private Robot robot;

    /**
     * Constructeur de la classe Remplissage
     * 
     * @param carte La carte sur laquelle se déroule la simulation
     * @param robot Le robot qui doit se remplir
     * @param simu  Le simulateur
     */
    public EvenementRemplissage(Carte carte, Robot robot, Simulateur simu) {
        // On ajoute le temps de remplissage du robot à la date de l'évènement pour
        // simuler le temps de remplissage
        super(0, simu);
        long dateRemplissage = Math.max(simu.getDate(), robot.getDateDisponibilite());
        dateRemplissage += (long) robot.getTempsRemplissage();
        super.setDate(dateRemplissage);
        this.carte = carte;
        this.robot = robot;
        robot.setDateDisponibilite(dateRemplissage);
    }

    @Override
    public void execute() {
        // on verifie que la position est correcte suivant le type de robot
        if (this.robot instanceof RobotDrone) {
            if (this.robot.getPosition().getNature() == NatureTerrain.EAU) {
                this.robot.remplirReservoir();
            } else {
                System.err.println("Le drone n'est pas à coté d'un point d'eau pour se remplir");
                return;
            }
            this.robot.remplirReservoir();
        } else {
            if (this.carte.voisinExiste(robot.getPosition(), Direction.NORD)&&this.carte.getVoisin(robot.getPosition(), Direction.NORD).getNature() == NatureTerrain.EAU) {
                this.robot.remplirReservoir();
            } else if (this.carte.voisinExiste(robot.getPosition(), Direction.SUD)&&this.carte.getVoisin(robot.getPosition(), Direction.SUD).getNature() == NatureTerrain.EAU) {
                this.robot.remplirReservoir();
            } else if (this.carte.voisinExiste(robot.getPosition(), Direction.EST)&&this.carte.getVoisin(robot.getPosition(), Direction.EST).getNature() == NatureTerrain.EAU) {
                this.robot.remplirReservoir();
            } else if (this.carte.voisinExiste(robot.getPosition(), Direction.OUEST)&&this.carte.getVoisin(robot.getPosition(), Direction.OUEST).getNature() == NatureTerrain.EAU) {
                this.robot.remplirReservoir();
            } else {
                System.err.println("Le robot n'est pas à coté d'un point d'eau pour se remplir");
                return;
            }
        }
        System.out.println("Rechargemennt en eau");
        this.getSim().afficheTout();
    }
    /**
     * retourne le robot qui se remplit
     * @return le robot qui doit remplir son reservoir
     */
    public Robot getRobot(){
        return robot;
    }
    /**
     * Set le robot qui doit remplir son reservoir
     * @param r le robot
     */
    public void setRobot(Robot r){
        this.robot=r;
    }
    /**
     * Set la carte dont on a besoin
     * @param carte la carte principale
     */
    public void setCarte(Carte carte){
        this.carte=carte;
    }
}