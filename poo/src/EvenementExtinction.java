/**
 * Cette classe est une sous-classe de Evenement qui a pour objectif d'éteindre
 * les incendies.
 */
public class EvenementExtinction extends Evenement {
    private Incendie incendie; // L'incendie dont il faut verser de l'eau sur le feu pour l'éteindre
    private Robot robot; // Le robot chargé de verser de l'eau sur l'incendie
    private boolean remplissageAuto; // Indique si le robot se remplit automatiquement

    /**
     * Contructeur de la classe EvenmentExtinction
     * 
     * @param sim             Le simulateur
     * @param incendie        L'incendie qui subit l'évènement
     * @param robot           Le robot chargé de verser de l'eau sur l'incendie
     * @param remplissageAuto Indique si le robot se remplit automatiquement
     */
    public EvenementExtinction(Simulateur sim, Incendie incendie, Robot robot, boolean remplissageAuto) {
        super(1, sim);
        //la date d'execution est soit la date actuelle si le robot est disponible, soit la date de disponibilité du robot
        long dataExecution = Math.max(sim.getDate(), robot.getDateDisponibilite());
        dataExecution += (long) robot.getTempsIntervention();
        super.setDate(dataExecution);
        this.incendie = incendie;
        this.robot = robot;
        this.remplissageAuto = remplissageAuto;
        robot.setDateDisponibilite(dataExecution);
    }

    @Override
    public void execute() {
        // Si le remplissage automatique est activé et que le réservoir du robot est vide
        if (this.remplissageAuto && robot.getVolumeActuelle() == 0) {
            // Calculer le chemin vers le point d'eau le plus proche
            Chemin cheminEau = CalculateurChemin.calculerCheminVersEau(this.getSim().getDonnees().getCarte(), robot);
            // Ajouter les événements de déplacement vers le point d'eau
            cheminEau.toEvenementSecond(this.getSim(), robot);
            // Ajouter l'événement de remplissage
            this.getSim().remplaceEvenement(
                    new EvenementRemplissage(this.getSim().getDonnees().getCarte(), this.robot, this.getSim()));
            incendie.affecter(false);
        }

        // Si l'incendie est déjà éteint
        if (incendie.getIntensite() == 0) {
            System.out.println("Click ... Incendie éteint"+incendie);
            // Retirer l'incendie de la liste des incendies
            this.getSim().getDonnees().getIncendies().remove(incendie);
            //System.out.println("je suis dans evetextinction tab incendie avt attribution2:"+this.getSim().getDonnees().getIncendies());
            // Réafficher tous les éléments graphiques
            this.getSim().afficheTout();
            return;
        }

        // Si le robot n'est pas sur la position de l'incendie
        if ( (incendie.getPosition().getLigne()!=robot.getPosition().getLigne()) || (incendie.getPosition().getColonne()!=robot.getPosition().getColonne())) {
            System.out.println("Click ... Robot pas sur l'incendie");
            //System.out.println("Position de l incendie:"+incendie.getPosition().getLigne()+" "+incendie.getPosition().getColonne());
            return;
        }

        // Si le robot a encore de l'eau dans son réservoir
        if (robot.getVolumeActuelle() > 0) {
            // Déverser de l'eau sur l'incendie
            this.robot.deverserEau();
            // Réduire l'intensité de l'incendie
            this.incendie.verser(this.robot.getVolumeIntervention());
            System.out.println("Click ... Intervention unitaire effectuée  ");
            // Ajouter un nouvel événement d'extinction pour continuer l'extinction
            this.getSim().remplaceEvenement(new EvenementExtinction(this.getSim(), this.incendie, this.robot, this.remplissageAuto));
        }
        // Réafficher tous les éléments graphiques
        this.getSim().afficheTout();
    }
    /**
     * retourne l'incendie qui doit être eteinte
     * @return l'incendie à eteindre
     */
    public Incendie getIncendie(){
        return this.incendie;
    }
    /**
     * retourne le robot qui eteint l'incendie
     * @return le robot qui eteint l'incendie
     */
    public Robot getRobot(){
        return this.robot;
    }
    /**
     * retourne le temps de remplissage
     * @return le temps de remplissage necessaire
     */
    public boolean getRemplissageAuto(){
        return this.remplissageAuto;
    }
    /**
     * set le robot qui eteint
     * @param r le robot
     */
    public void setRobot(Robot r){
        this.robot=r;
    }
    /**
     * Set l'incendie à eteindre
     * @param i l'incendie
     */
    public void setIncendie(Incendie i){
        this.incendie=i;
    }
}
