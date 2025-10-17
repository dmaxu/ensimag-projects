import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Classe pour calculer le plus court chemin d'un robot vers une destination.
 */
public class CalculateurChemin {

    /**
     * Constructeur par défaut de la classe CalculateurChemin.
     * Cette classe ne nécessite pas d'initialisation spécifique.
     */
    public CalculateurChemin() {
        // Constructeur par défaut
    }

    /**
     * Calcule le plus court chemin d'un robot vers une destination en utilisant
     * l'algorithme de Dijkstra.
     * 
     * @param carte       La carte de la simulation.
     * @param robot       Le robot pour lequel calculer le chemin.
     * @param destination La case de destination.
     * @return Le chemin calculé.
     */
    public static Chemin calculerChemin(Carte carte, Robot robot, Case destination) {
        Map<Case, Double> times = new HashMap<>();
        Map<Case, Case> predecesseurs = new HashMap<>();
        // Comparer les élements de la queue par le temps de parcours
        PriorityQueue<Case> queue = new PriorityQueue<>(Comparator.comparingDouble(times::get));

        for (int i = 0; i < carte.getNbLignes(); i++) {
            for (int j = 0; j < carte.getNbColonnes(); j++) {
                Case c = carte.getCase(i, j);
                times.put(c, Double.POSITIVE_INFINITY);
            }
        }

        Case source = robot.getPosition();
        times.put(source, 0.0);
        queue.add(source);

        while (!queue.isEmpty()) {
            Case u = queue.poll();

            for (Direction dir : Direction.values()) {
                if (carte.voisinExiste(u, dir) && robot.peutTraverser(carte.getVoisin(u, dir))) {
                    Case v = carte.getVoisin(u, dir);
                    double time = robot.tempsDeplacement(v, carte);

                    double alt = times.get(u) + time;

                    if (alt < times.get(v)) {
                        times.put(v, alt);
                        predecesseurs.put(v, u);
                        queue.add(v);
                    }
                }
            }
        }

        List<Case> chemin = new ArrayList<>();
        // on enlève la case actuelle du chemin
        Case step = destination;
        if (predecesseurs.containsKey(step) || step.equals(source)) {
            while (step != null) {
                chemin.add(step);
                step = predecesseurs.get(step);
            }
            chemin.remove(chemin.size()-1);
            Collections.reverse(chemin);
        }
        /*System.err.println("je suis dans calculchemin, position incendie:"+destination.getLigne()+" "+destination.getColonne());
        System.out.println("je suis dans calculchemin, position incendie:"+times.get(destination));
        System.out.println("je suis dans calculchemin, position incendie:"+destination);
        System.out.println("je suis dans calculchemin, position incendie:"+times);
        System.out.println("CArte"+carte);*/
        return new Chemin(chemin, times.get(destination));
    }

    /**
     * Calcule le plus court chemin (pas toujours) d'un robot vers une case de type
     * EAU en utilisant BFS(parcours en largeur).
     * 
     * @param carte La carte de la simulation.
     * @param robot Le robot pour lequel calculer le chemin.
     * @return Le chemin calculé vers une case de type EAU.
     */
    public static Chemin calculerCheminVersEau(Carte carte, Robot robot) {
        Map<Case, Case> predecesseurs = new HashMap<>();
        Queue<Case> queue = new LinkedList<>();
        Set<Case> visites = new HashSet<>();

        Case source = robot.getPosition();
        queue.add(source);
        visites.add(source);

        while (!queue.isEmpty()) {
            Case u = queue.poll();

            for (Direction dir : Direction.values()) {
                // Certanins robots ne peuvent pas traverser l'eau
                if (carte.voisinExiste(u, dir) && (robot.peutTraverser(carte.getVoisin(u, dir))
                        || carte.getVoisin(u, dir).getNature() == NatureTerrain.EAU)) {
                    Case v = carte.getVoisin(u, dir);
                    if (!visites.contains(v)) {
                        visites.add(v);
                        predecesseurs.put(v, u);
                        queue.add(v);

                        if (v.getNature() == NatureTerrain.EAU) {
                            List<Case> chemin = new ArrayList<>();
                            Case step = v;
                            double tempsTotal = 0.0;
                            while (step != null) {
                                chemin.add(step);
                                tempsTotal += robot.tempsDeplacement(step, carte);
                                step = predecesseurs.get(step);
                            }
                            Collections.reverse(chemin);
                            // Si le robot est un robot à roues ou un robot chenille, on s'arrête à coté de
                            // la case d'eau
                            if (robot instanceof RobotARoues || robot instanceof RobotChenille) {
                                chemin.remove(chemin.size() - 1);
                                tempsTotal -= robot.tempsDeplacement(v, carte);
                            }
                            // on enlève la case actuelle du chemin
                            chemin.remove(0);

                            return new Chemin(chemin, tempsTotal);
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Aucune case d'eau n'a été trouvée.");
    }
}