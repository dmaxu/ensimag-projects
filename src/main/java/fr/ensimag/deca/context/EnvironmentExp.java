package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.Map;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 *
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 *
 * The dictionary at the head of this list thus corresponds to the "current" 
 * block (eg class).
 *
 * Searching a definition (through method get) is done in the "current" 
 * dictionary and in the parentEnvironment if it fails. 
 *
 * Insertion (through method declare) is always done in the "current" dictionary.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class EnvironmentExp {
    // A FAIRE : implémenter la structure de donnée représentant un
    // environnement (association nom -> définition, avec possibilité
    // d'empilement).
    EnvironmentExp parentEnvironment;

    public HashMap<Symbol, ExpDefinition> getDictionnaireDef() {
        return dictionnaireDef;
    }

    // pour ce moment on utilise un dictionnaire
    // aprés on vas remplacéer String par symbole, mais en faite on a String name <==> Symbol
    HashMap<Symbol, ExpDefinition> dictionnaireDef = new HashMap<>();

    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public ExpDefinition get(Symbol key) {
        //  par défault, cette commande retourne null au cas ou key n'existe pas dans KeySet de l'env ou bien au parentEnv (récursive).
        // on doit içi faire un Override pour la méthode .equals() dans la classe Symbol
        if (!this.dictionnaireDef.containsKey(key) && parentEnvironment != null){
            // empilement
            return parentEnvironment.get(key);
        }
        return this.dictionnaireDef.get(key);
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     *
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary
     * - or, hides the previous declaration otherwise.
     *
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        // A FAIRE: définir x comme attribut + variable à l'intérieur d'une méthode.
        // On vérifie si le Symbole a déja une définition.
        if (this.dictionnaireDef.containsKey(name)){
            throw new DoubleDefException();
        }
        this.dictionnaireDef.put(name, def);
    }

    /**
     *  Cette méthode retourne true si l'identificatuer existedans l'env local.
     * @param name
     * @return
     *
     */
    public boolean itExistLocal(Symbol name){
        return this.dictionnaireDef.containsKey(name);
    }

    /**
     *
     * Cette méthode nous permet d'enléver un élément du dictionnaire.
     *
     * @param name
     *
     */

    public void remove(Symbol name){
        this.dictionnaireDef.remove(name);
    }

    // Hypothése : les environments que on fusionne ont le méme environement parent
    // cette méthode nous permet de fusionner les environements
    // Nécessaire pour la partie des classes.
    public void fusionner(EnvironmentExp autreEnv) throws  ContextualError{
        // On parcourt les définitions du dictionnaire de l'autre environnement
        for (Map.Entry<Symbol, ExpDefinition> entry : autreEnv.dictionnaireDef.entrySet()) {
            Symbol symbole = entry.getKey();
            ExpDefinition definition = entry.getValue();
            // Ajoute la définition uniquement si elle n'existe pas dans le dictionnaire actuel
            if (!this.dictionnaireDef.containsKey(symbole)) {
                this.dictionnaireDef.put(symbole, definition);
                // Sinon, donc la condition que les deux environements n'ont pas des symbols commun n'est pas vérifie
            }
            else {
                throw new UnsupportedOperationException("ses deux environement ont en communs la clé : " + symbole.toString());
            }
        }
    }

    public void Affiche(){
        System.out.println(this.dictionnaireDef);
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined WITHOUT LOOKING AT PARENT ENVIRONMENT
     */
    public ExpDefinition getWithoutParent(Symbol key) {
        //  par défault, cette commande retourne null au cas ou key n'existe pas dans KeySet de l'env
        if (!this.dictionnaireDef.containsKey(key)){
            // empilement
            return null;
        }
        return this.dictionnaireDef.get(key);
    }
}
