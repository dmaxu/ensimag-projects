/** 
 * Implémentation du type de robot drone.
 * Cette classe représente un drone avec des attributs de position, vitesse, volume d'eau, etc.
 */
public class RobotDrone extends Robot {

    /**
     * Constructeur de la classe Drone avec une vitesse par défaut.
     * @param position La position initiale du drone.
     */
    public RobotDrone(Case position) {
        super(position);
        super.setVitesse(100);
        super.setVolumeMax(10000);
        super.setPosition(position);
        super.setVolumeActuelle(10000);
        super.setTempsRemplissage(30 * 60);
        super.setTempsIntervention(30);
        super.setVolumeIntervention(10000);
    }

    /**
     * Constructeur de la classe Drone avec une position et une vitesse spécifiées.
     * @param position La position initiale du drone.
     * @param vitesse La vitesse initiale du drone.
     * @throws IllegalArgumentException si la vitesse dépasse 150 km/h.
     */
    public RobotDrone(Case position, double vitesse) {
        super(position);
        if (vitesse > 150) {
            throw new IllegalArgumentException("La vitesse maximale d'un drone ne peut pas dépasser 150 km/h.");
        }
        super.setVitesse(vitesse);
        super.setVolumeMax(10000);
        super.setPosition(position);
        super.setVolumeActuelle(10000);
        super.setTempsRemplissage(30 * 60);
        super.setTempsIntervention(30);
        super.setVolumeIntervention(10000);
    }

    /**
     * Remplit le réservoir du drone à sa capacité maximale.
     * @throws IllegalArgumentException si le drone n'est pas sur un terrain d'eau.
     */
    @Override
    public void remplirReservoir() {
        try {
            if (super.getPosition().getNature() == NatureTerrain.EAU) {
                super.setVolumeActuelle(super.getVolumeMax());
            } else {
                throw new IllegalArgumentException(
                        "Le drone ne peut pas se remplir sur un terrain autre que de l'eau.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ajouter le temps de remplissage du drone dans le simulateur ?
    }

    @Override
    public boolean peutTraverser(Case c){
        return true;
    }

}
