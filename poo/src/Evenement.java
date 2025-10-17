/**
 * Cette classe abstraite sert à decrire les differents évènements que pouront effectuer les robots 
 */

public abstract class Evenement implements Comparable<Evenement> {
    private long date; //La date à laquelle l'évènement à lieu.
    private Simulateur sim;

    /**
     * Constructeur de la classe Evenement
     * @param date La date à laquelle l'évènement doit se produire
     * @param sim Le simulateur
     */
    public Evenement(long date, Simulateur sim){
        this.date=date;
        this.sim=sim;
    }

    /**
     * Modifie la date de réalisation d'un évènement
     * @param date La nouvelle date de réalisation de l'évènement
     */
    public void setDate(long date){
        this.date=date;
    }

    /**
     * Retourne la date de réalisation d'un évènement 
     * @return Date de réalisation de l'évènement.
     */
    public long getDate(){
        return this.date;
    }

    /**
     * Méthode abstraite qui sera redéfinie dans les classes filles
     */

    public void execute(){}

    /**
     * Retourne le simulateur associé à l'évènement
     * @return Le simulateur associé à l'évènement
     */
    public Simulateur getSim(){
        return this.sim;
    }


    @Override
    public int compareTo(Evenement other) {
        return Long.compare(this.date, other.date);
    }
    
}
