/**
 * Cette classe fournit l'implémentation de la carte (constituée de cases)
 * sur laquelle les robots évoluent.
 */
public class Carte {
    private int tailleCases;
    private int nbLignes;
    private int nbColonnes;
    private Case[][] cases;

    /**
     * Constructeur de la classe Carte.
     * 
     * @param nbLignes    Le nombre de lignes de la carte.
     * @param nbColonnes  Le nombre de colonnes de la carte.
     * @param tailleCases La taille des cases de la carte.
     */
    public Carte(int nbLignes, int nbColonnes, int tailleCases) {
        this.nbLignes = nbLignes;
        this.nbColonnes = nbColonnes;
        this.tailleCases = tailleCases;
        this.cases = new Case[nbLignes][nbColonnes];
    }

    /**
     * Retourne le nombre de lignes de la carte.
     * 
     * @return Le nombre de lignes.
     */
    public int getNbLignes() {
        return this.nbLignes;
    }

    /**
     * Retourne le nombre de colonnes de la carte.
     * 
     * @return Le nombre de colonnes.
     */
    public int getNbColonnes() {
        return this.nbColonnes;
    }

    /**
     * Retourne la taille des cases de la carte.
     * 
     * @return La taille des cases.
     */
    public int getTailleCases() {
        return this.tailleCases;
    }

    /**
     * Retourne la case située à la position spécifiée.
     * 
     * @param ligne   La ligne de la case.
     * @param colonne La colonne de la case.
     * @return La case à la position spécifiée.
     */
    public Case getCase(int ligne, int colonne) {
        return this.cases[ligne][colonne];
    }

    /**
     * Vérifie si une case voisine existe dans la direction spécifiée.
     * 
     * @param src La case source.
     * @param dir La direction dans laquelle vérifier la présence d'une case
     *            voisine.
     * @return true si une case voisine existe, false sinon.
     */
    public Boolean voisinExiste(Case src, Direction dir) {
        int ligne = src.getLigne();
        int colonne = src.getColonne();
        switch (dir) {
            case NORD:
                return ligne > 0;
            case SUD:
                return ligne < this.nbLignes - 1;
            case EST:
                return colonne < this.nbColonnes - 1;
            case OUEST:
                return colonne > 0;
            default:
                return false;
        }
    }

    /**
     * Retourne la case voisine dans la direction spécifiée.
     * 
     * @param src La case source.
     * @param dir La direction dans laquelle chercher la case voisine.
     * @throws IllegalArgumentException si la case voisine n'existe pas.
     * @return La case voisine dans la direction spécifiée.
     */
    public Case getVoisin(Case src, Direction dir) {
        try {
            if (!this.voisinExiste(src, dir)) {
                throw new IllegalArgumentException("La case voisine n'existe pas.");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        int ligne = src.getLigne();
        int colonne = src.getColonne();
        switch (dir) {
            case NORD:
                return this.cases[ligne - 1][colonne];
            case SUD:
                return this.cases[ligne + 1][colonne];
            case EST:
                return this.cases[ligne][colonne + 1];
            case OUEST:
                return this.cases[ligne][colonne - 1];
            default:
                return null;
        }
    }

    /**
     * Attribut à la case à la position (ligne, colonne) une nature spécifique
     * 
     * @param ligne   La ligne de la case
     * @param colonne La colonne de la case
     * @param newCase la case à mettre dans la carte
     */
    public void setCase(int ligne, int colonne, Case newCase) {
        this.cases[ligne][colonne] = newCase;
    }
}