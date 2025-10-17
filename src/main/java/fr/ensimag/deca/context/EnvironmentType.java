package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;

import java.util.HashMap;
import java.util.Map;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl47
 * @date 01/01/2025
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {
        envTypes = new HashMap<>();
        envExp = new EnvironmentExp(null); // Root environment for variables
        
        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol objectSymb = compiler.createSymbol("Object");
        OBJECT = new ClassType(objectSymb, Location.BUILTIN, null);
        envTypes.put(objectSymb, new ClassDefinition(OBJECT, Location.BUILTIN, null));
        // Not added to envTypes, as it's not visible for the user.;
    }

    private final Map<Symbol, TypeDefinition> envTypes;
    public final EnvironmentExp envExp; // Root environment for variables

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    /**
     * 
     * Add the definition typeDef associated to the symbol name int the environmenet.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throw ContextualError if the sumbol is in the current dictionnary (envTypes)
     * - or, hides the previous declaration otherwise.
     * @param name
     *            Name of the symbol to define
     * @param typeDef
     *            Definition of the symbol
     * @throws ContextualError
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    // cette méthode nous permet d'ajouté d'autre classe en plus des classes prédéfinie.
    public void addTypes(Symbol name, TypeDefinition typeDef) throws ContextualError{
        if (envTypes.containsKey(name)){
            // A FAIRE : change le type de l'exception utilié
            throw new ContextualError("la classe sous le nom " + name.toString() + " est déja définie.", typeDef.getLocation());
        }
        envTypes.put(name, typeDef);
    }

    // cette méthode nous permet d'enlévée au cas d'existence un couple (Symbole, TypeDefinition)
    // Essentielle pour les classes
    public void removeSymbol(Symbol symbol){
        // enléve au cas d'existence le couple (symbo, TypeDef)
        // sinon, rien.
        this.envTypes.remove(symbol);
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final BooleanType BOOLEAN;
    // Cette attribut est ajouté
    public final ClassType OBJECT;
}