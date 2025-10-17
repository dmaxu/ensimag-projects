

/**
 * Implémentation de la classe Incendie  
 */

public class Incendie {
    private Case position;
    private int intensite;
    private int intensiteMax; //sert pour la bar d'affichage de l'intensité de l'incendie
    private boolean estAffecte= false;

    /**
     * Constructeur de la classe Incendie
     * @param position La position de l'incendie
     * @param intensite La quantité d'eau nécessaire pour éteindre l'incendie
     */
    public Incendie(Case position, int intensite){
        this.position= position;
        this.intensite= intensite;
        this.intensiteMax= intensite;
    }

    /** 
     * Retourne la position de l'incendie 
     * @return La position de l'incendie
    */
    public Case getPosition(){
        return this.position;
    }

    /**
     * Retourne l'intensité de l'incendie
     * @return L'intensité de l'incendie
    */
    public int getIntensite(){
        return this.intensite;
    }

    /**
     * Décrémente l'intensite de l'incendie en fonction de la quantité d'eau versée 
     * @param quantite La quantité d'eau a verser sur l'incendie
    */ 
    public void verser(int quantite){
        if (this.intensite- quantite <0){
            this.intensite=0;
            return;
        }
        this.intensite-=quantite;
    }


    /**
     * Retourne la proportion de l'intensité de l'incendie par rapport à l'intensité maximale
     * @return La proportion de l'intensité de l'incendie par rapport à l'intensité maximale
    */
    public boolean eteint(){
        return this.intensite==0;
    }

    /**
     * Retourne l'intensité maximale de l'incendie
     * @return L'intensité maximale de l'incendie
    */
    public int getIntensiteMax(){
        return this.intensiteMax;
    }

    /**
     * Retourne si l'incendie est affecté par un robot
     * @return Si l'incendie est affecté par un robot
    */
    public boolean estAffecte(){
        return this.estAffecte;
    }

    /**
     * Affecte ou désaffecte l'incendie par un robot
     * @param valeur La valeur de l'affectation
    */
    public void affecter(boolean valeur){
        this.estAffecte= valeur;
    }
    /**
     * Copie l'incendie
     * @return l'incendie
     */
    public Incendie copyIncendie(){
        Incendie I= new Incendie(position.copy() , intensite);
        return I;
    }
    /**
     * Set l'incendie
     * @param i incendie
     */
    public void setIncendie(Incendie i){
        i=this;
    }

}