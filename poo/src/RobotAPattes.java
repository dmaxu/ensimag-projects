/**
 * Implementation de la classe Robot à pattes.
 */
public class RobotAPattes extends Robot {
    
    /**
     * Constructeur de la classe RobotAPattes avec une vitesse par defaut
     * @param position La position initiale du robot
     */
    public RobotAPattes( Case position) {
        super(position);
        super.setVitesse(30);
        super.setVolumeMax(Integer.MAX_VALUE); // Réservoir infini
        super.setVolumeActuelle(Integer.MAX_VALUE); // Réservoir infini
        super.setTempsRemplissage(0 * 60); //Car on le considère toujour rempli
        super.setTempsIntervention(1);
        super.setVolumeIntervention(10);
    }

    /**
     * Constructeur de la classe RobotAPattes 
     * @param position La position initiale du robot
     * @param vitesse La vitesse de déplacement du robot.
     */
    public RobotAPattes(Case position, double vitesse) {
        super(position);
        super.setVitesse(vitesse);
        super.setVolumeMax(Integer.MAX_VALUE); // Réservoir infini
        super.setVolumeActuelle(Integer.MAX_VALUE); // Réservoir infini
        super.setTempsRemplissage(0 * 60);
        super.setTempsIntervention(1);
        super.setVolumeIntervention(10);
    }

    @Override
    public void remplirReservoir(){
        //Le robot à pattes ne peut pas se remplir
        throw new UnsupportedOperationException("Le robot à pattes ne peut pas remplir son réservoir.");
    }

    @Override
    public double getVitesse ( NatureTerrain Nature){
        //En considerant que le RobotApatte ne se retrouvera jamais sur de l'eau
        // car ne pouvant pas y allez, on a: 
        if (Nature==NatureTerrain.FORET){
            return (double) 10;
        }
        return super.getVitesse(Nature);
    }

    @Override
    public boolean peutTraverser(Case c){
        return (c.getNature()!=NatureTerrain.EAU);
    }

    @Override
    public void deverserEau() {
        // Le robot à pattes peut toujours déverser de l'eau car son réservoir est infini
        // Ne pas modifier le volume actuel du réservoir
        super.setVolumeActuelle(Integer.MAX_VALUE);
    }
}