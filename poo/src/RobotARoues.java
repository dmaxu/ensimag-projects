/** Implémentation du type robot à roues */

public class RobotARoues extends Robot {
    
    /**
     * Constructeur de la classe RobotARoues avec une vitesse par defaut
     * @param position La position initiale du robot
     */
    public RobotARoues( Case position) {
        super(position);
        super.setVitesse(80);
        super.setVolumeMax(5000);
        super.setVolumeActuelle(5000);
        super.setTempsRemplissage(10 * 60);
        super.setTempsIntervention(5);
        super.setVolumeIntervention(100);
    }

    /**
     * Constructeur de la classe RobotARoues.
     * @param position La position initiale du robot
     * @param vitesse La vitesse de déplacement du robot.
     */
    public RobotARoues(Case position, double vitesse) {
        super(position);
        super.setVitesse(vitesse);
        super.setVolumeMax(5000);
        super.setVolumeActuelle(5000);
        super.setTempsRemplissage(10 * 60);
        super.setTempsIntervention(5);
        super.setTempsIntervention(100);
    }

    @Override
    public void remplirReservoir(){
        super.remplirReservoir();
        // Ajouter le temps de remplissage du drone dans le simulateur ?
    }

    @Override
    public boolean peutTraverser(Case c){
        return (c.getNature()==NatureTerrain.TERRAIN_LIBRE||c.getNature()==NatureTerrain.HABITAT);
    }

}
