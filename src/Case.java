/**
 * Cette classe représente la case qui est définie par deux coordonnées entières
 * (ligne et colonne)
 * et la nature du
 * terrain qu’elle représente (un attribut de type énuméré NatureTerrain).
 * Un ensemble de case forme une carte.
 */

public class Case {
    private int ligne;
    private int colonne;
    private NatureTerrain nature;

    /**
     * Constructeur de la classe Case
     * 
     * @param ligne   la ligne de la case
     * @param colonne la colonne de la case
     * @param nature  la nature de la case
     */
    public Case(int ligne, int colonne, NatureTerrain nature) {
        this.ligne = ligne;
        this.colonne = colonne;
        this.nature = nature;
    }

    /**
     * Retourne la ligne de la case
     * 
     * @return la ligne de la case
     */
    public int getLigne() {
        return ligne;
    }

    /**
     * Retourne la colonne de la case
     * 
     * @return la colonne de la case
     */
    public int getColonne() {
        return colonne;
    }

    /**
     * Retourne la nature de la case
     * 
     * @return la nature de la case
     */
    public NatureTerrain getNature() {
        return nature;
    }

    /**
     * Attribut à la case la nature specifiée
     * 
     * @param Nature nature de la case
     */
    public void setNature(NatureTerrain Nature) {
        this.nature = Nature;
    }
    /**
     * la copie de la case
     * @return la copie de la case
     */
    public Case copy(){
        Case c=new Case(ligne, colonne, nature);
        return c;
    }


}
