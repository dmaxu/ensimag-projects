/** 
 * Implémentation abstraite de la classe robot.
 * Cette classe représente un robot avec des attributs de position, vitesse, volume d'eau, etc.
 */
public abstract class Robot {
    private Case position;
    private double vitesse;
    private int volumeActuelle;
    private int volumeMax;
    private int volumeIntervention;
    // Temps de remplissage du réservoir du robot en SECONDES !
    private int tempsRemplissage;
    // Temps d'intervention unitaire du robot en SECONDES !
    private int tempsIntervention;
    // Indique la prochaine date où le robot sera disponible
    private long dateDisponibilite = 0;

    /**
     * Constructeur de la classe Robot.
     * @param position La position initiale du robot.
     */
    public Robot(Case position) {
        this.position = position;
    }

    /**
     * Retourne la position actuelle du robot.
     * @return La position actuelle du robot.
     */
    public Case getPosition() {
        return this.position;
    } 

    /**
     * Définit la position du robot.
     * Cette méthode est surchargée dans les classes filles pour prendre en compte les spécificités de chaque robot.
     * @param position La nouvelle position du robot.
     */
    public void setPosition(Case position) {
        //TODO vérifier pour chaque type de robot si la position est bien compatible
        this.position = position;
    }

    /**
     * Retourne la vitesse du robot sur un terrain donné.
     * Cette méthode est surchargée dans les classes filles pour prendre en compte les spécificités de chaque robot.
     * @param nature Le type de terrain.
     * @return La vitesse du robot sur le terrain spécifié.
     */
    public double getVitesse(NatureTerrain nature) {
        //TODO ajuster la vitesse en fonction du terrain pour chaque type de robot
        return this.vitesse;
    }

    /**
     * Définit la vitesse du robot.
     * @param vitesse La nouvelle vitesse du robot.
     */
    public void setVitesse(double vitesse) {
        this.vitesse = vitesse;
    }

    /**
     * Retourne le volume actuel d'eau dans le réservoir du robot.
     * @return Le volume actuel d'eau.
     */
    public int getVolumeActuelle() {
        return this.volumeActuelle;
    }

    /**
     * Définit le volume actuel d'eau dans le réservoir du robot.
     * @param volumeActuelle Le nouveau volume actuel d'eau.
     */
    public void setVolumeActuelle(int volumeActuelle) {
        this.volumeActuelle = volumeActuelle;
    }
    

    /**
     * Retourne le volume maximum d'eau que le réservoir du robot peut contenir.
     * @return Le volume maximum d'eau.
     */
    public int getVolumeMax() {
        return this.volumeMax;
    }

    /**
     * Définit le volume maximum d'eau que le réservoir du robot peut contenir.
     * @param volumeMax Le nouveau volume maximum d'eau.
     */
    public void setVolumeMax(int volumeMax) {
        this.volumeMax = volumeMax;
    }

    /**
     * Définit le volume d'intervention du robot.
     * @param volumeIntervention Le volume d'intervention.
     */
    public void setVolumeIntervention(int volumeIntervention) {
        this.volumeIntervention = volumeIntervention;
    }

    /**
     * Retourne le volume d'intervention du robot.
     * @return Le volume d'intervention.
     */
    public int getVolumeIntervention() {
        return this.volumeIntervention;
    }

    /**
     * Définit le temps de remplissage du réservoir du robot.
     * @param tempsRemplissage Le temps de remplissage.
     */
    public void setTempsRemplissage(int tempsRemplissage) {
        this.tempsRemplissage = tempsRemplissage;
    }

    /**
     * Retourne le temps de remplissage du robot.
     * @return Le temps de remplissage.
     */
    public int getTempsRemplissage() {
        return this.tempsRemplissage;
    }

    /**
     * Définit le temps de intervention du réservoir du robot.
     * @param tempsIntervention Le temps de intervention.
     */
    public void setTempsIntervention(int tempsIntervention) {
        this.tempsIntervention = tempsIntervention;
    }

    /** 
     * Retourne le temps d'intervention du robot.
     * Cette méthode est surchargée dans les classes filles pour prendre en compte les spécificités de chaque robot.
     * @return Le temps d'intervention.
     */
    public int getTempsIntervention() {
        return this.tempsIntervention;
    }

    /**
     * Retourne la date de disponibilité du robot.
     * @return La date de disponibilité.
     */
    public long getDateDisponibilite() {
        return this.dateDisponibilite;
    }
    
    /**
     * Set la date de disponibilité du robot.
     * @param valeur la valeur 
     */
    public void setDateDisponibilite(long valeur) {
        this.dateDisponibilite = valeur;
    }

    /**
     * Déverse une quantité spécifiée d'eau du réservoir du robot.
     * @throws IllegalArgumentException si le volume à déverser est supérieur au volume actuel.
     */
    public void deverserEau() {
        try {
            if (volumeIntervention > this.volumeActuelle) {
                throw new IllegalArgumentException("Le volume d'eau à déverser est supérieur au volume du robot");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.volumeActuelle -= volumeIntervention;
    }

    /**
     * Remplit le réservoir du robot à sa capacité maximale.
     * Cette méthode est surchargée dans les classes filles pour prendre en compte les spécificités de chaque robot.
     */
    public void remplirReservoir() {
        //TODO vérifier si le robot est sur un terrain compatible pour se remplir
        this.volumeActuelle = this.volumeMax;
    }

    /**
     * Vérifie si le robot peut traverser une case donnée.
     * Cette méthode doit être implémentée par les sous-classes.
     * @param c La case à vérifier.
     * @return true si le robot peut traverser la case, false sinon.
     */
    public abstract boolean peutTraverser(Case c);

    /**
     * Retourne le temps de déplacement du robot sur une case donnée
     * @param c La case
     * @param carte La carte sur laquelle se déplace le robot.
     * @return Le temps de déplacement en secondes.
     */
    double tempsDeplacement(Case c, Carte carte) {
        return (carte.getTailleCases() / 100) / this.getVitesse(c.getNature());
    }

    /**
     * Set le robot
     * @param robot le robot
     */
    public void setRobot(Robot robot){
        robot=this;
    }
}
