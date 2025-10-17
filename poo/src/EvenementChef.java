/**
 * Classe représentant un événement pour gérer les interventions du chef
 * pompier. On utilise un évenement pour la gestion du chef afin de pouvoir le rappeler à chaque intervalle de temps.
 */
public class EvenementChef extends Evenement {
    private Chef chef;
    private int interval;

    /**
     * Constructeur de la classe EvenementGererInterventions.
     * 
     * @param date       La date à laquelle l'événement doit se produire.
     * @param chef       Le chef pompier.
     * @param interval   L'intervalle de temps entre chaque intervention.
     * @param simulateur Le simulateur.
     */
    public EvenementChef(long date, Chef chef, int interval, Simulateur simulateur) {
        super(date, simulateur);
        this.chef = chef;
        this.interval = interval;
    }
    /**
     * retourne le chef pompier
     * @return le chef
     */
    public Chef getChef(){
        return this.chef;
    }
    /**
     * retourne un intervalle de temps
     * @return L'intervalle de temps entre chaque intervention.
     */
    public int getInterval(){
        return this.interval;
    }

    @Override
    public void execute() {
        boolean intervention = chef.gererInterventions();
        // Si il reste encore des feux on rappelle le chef
        if (intervention) {
            super.getSim().remplaceEvenement(
                    new EvenementChef(super.getDate() + this.interval, this.chef, this.interval, super.getSim()));
        }
    }
}