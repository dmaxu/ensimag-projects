package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractInitialization;


/**
 * List of Initializations (e.g. int x=0, float y = 1.01, ....).
 * 
 * @author gl47
 * @date 26/12/2024
 */
public class ListInitialization  extends TreeList<AbstractInitialization>{
    // A FAIRE
    @Override
    public void decompile(IndentPrintStream s) {
        // On l'utilise pour gérer la séparation des virgules.
        boolean first = true; 
        for (AbstractInitialization init : getList()) {
            if (!first) {
                // Ajouter une virgule avant chaque initialisation (sauf la première).
                s.print(", "); 
            }
            init.decompile(s);
            first = false;
        }
    }
}
