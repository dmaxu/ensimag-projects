import java.util.HashSet;

/**
 * Classe représentant le chef pompier simple qui gère les robots et les
 * incendies.
 */
public class ChefSimple extends Chef {

    /**
     * Constructeur de la classe ChefSimple.
     * 
     * @param carte      La carte.
     * @param robots     Le tableau des robots.
     * @param incendies  Le HashSet des incendies.
     * @param simulateur Le simulateur.
     */
    public ChefSimple(Carte carte, Robot[] robots, HashSet<Incendie> incendies, Simulateur simulateur) {
        super(carte, robots, incendies, simulateur);
    }

    /**
     * Méthode pour gérer les interventions des robots sur les incendies.
     */
    @Override
    public boolean gererInterventions() {

        for (Incendie incendie : incendies) {
            if (!incendie.estAffecte()) {
                for (Robot robot : robots) {
                    if ((robot.getDateDisponibilite() < simulateur.getDate()) && robot.getVolumeActuelle() > 0) {
                        //System.out.println("je suis dans chef simple, position incendie:"+incendie);
                        //System.out.println("je suis dans chef simple, position incendie:"+incendie.getPosition());
                        //System.out.println("je suis dans chef simple, position incendie:"+incendie.getPosition().getLigne()+" "+incendie.getPosition().getColonne());

                        //System.out.println("je suis dans chef simple, position robot:"+robot.getPosition().getLigne()+" "+robot.getPosition().getColonne());
                        Chemin chemin = CalculateurChemin.calculerChemin(super.carte,
                                robot, incendie.getPosition());
                        if (chemin != null) {
                            incendie.affecter(true);
                            chemin.toEvenementSecond(simulateur, robot);
                            simulateur.remplaceEvenement(new EvenementExtinction(super.simulateur, incendie, robot,false));
                            break;
                        }
                    }
                }
            }
        }
        return (incendies.size() > 0);
    }

}
