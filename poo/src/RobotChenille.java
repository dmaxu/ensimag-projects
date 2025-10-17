/**
 * Implémentation du type de robot chenille.
 * Cette classe représente un robot chenille avec des attributs de position, vitesse, volume d'eau, etc.
 */
public class RobotChenille extends Robot {

    /**
     * Constructeur de la classe Chenille avec une vitesse par défaut.
     * @param position La position initiale du robot chenille.
     */
    public RobotChenille(Case position) {
        super(position);
        super.setVitesse(60);
        super.setVolumeMax(2000);
        super.setPosition(position);
        super.setVolumeActuelle(2000);
        super.setTempsRemplissage(5 * 60);
        super.setTempsIntervention(8);
        super.setVolumeIntervention(100);
    }

    /**
     * Constructeur de la classe Chenille avec une position et une vitesse spécifiées.
     * @param position La position initiale du robot chenille.
     * @param vitesse La vitesse initiale du robot chenille.
     * @throws IllegalArgumentException si la vitesse dépasse 80 km/h.
     */
    public RobotChenille(Case position, double vitesse) {
        super(position);
        if (vitesse > 80) {
            throw new IllegalArgumentException(
                    "La vitesse maximale d'un robot à chenilles ne peut pas dépasser 80 km/h.");
        }
        super.setVitesse(vitesse);
        super.setVolumeMax(2000);
        super.setPosition(position);
        super.setVolumeActuelle(2000);
        super.setTempsRemplissage(5 * 60);
        super.setTempsIntervention(8);
        super.setVolumeIntervention(100);
    }

    /**
     * Retourne la vitesse du robot chenille en fonction du type de terrain.
     * @param nature Le type de terrain.
     * @return La vitesse du robot chenille sur le terrain spécifié.
     */
    @Override
    public double getVitesse(NatureTerrain nature) {
        double vitesse = super.getVitesse(NatureTerrain.TERRAIN_LIBRE);
        switch (nature) {
            case EAU:
                return 0;
            case FORET:
                return (0.5 * vitesse);
            case ROCHE:
                return 0;
            case TERRAIN_LIBRE:
                return vitesse;
            default:
                return 0;
        }
    }

    /**
     * Remplit le réservoir du robot chenille à sa capacité maximale.
     * La méthode ne vérifie pas la compatibilité de la nature de la case avec le robot.
     */
    @Override
    public void remplirReservoir() {
        super.remplirReservoir();
    }

    @Override
    public boolean peutTraverser(Case c) {
        return (c.getNature() != NatureTerrain.EAU&& c.getNature() != NatureTerrain.ROCHE);
    }
}