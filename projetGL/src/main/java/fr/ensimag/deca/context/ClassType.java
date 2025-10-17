package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ClassTypeTable;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.context.TypeDefinition;
import org.apache.commons.lang.Validate;

/**
 * Type defined by a class.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class ClassType extends Type {
    
    protected ClassDefinition definition;
    
    public ClassDefinition getDefinition() {
        return this.definition;
    }
            
    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
        this.definition = new ClassDefinition(this, location, superClass);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }
    

    @Override
    public boolean sameType(Type otherType) {
        // on utilise la méthode isClass pour vérifier si otherType et this ont le méme type.
        return otherType.isClass();
    }

    /**
     * Return true if potentialSuperClass is a superclass of this class.
     */
    public boolean isSubClassOf(ClassType potentialSuperClass) {
        // A FAIRE: déja implémentée en classDefinition
        throw new UnsupportedOperationException("not yet implemented"); 
    }

    @Override
    public TypeTable createTableType(DecacCompiler compiler, int dimension) throws ContextualError {
        // On construit le symbol
        Symbol listSymbol = compiler.createSymbol(dimension + "D " + getName());
        // On retourne la définition
        TypeDefinition typeDefTable = compiler.environmentType.defOfType(listSymbol);
        // On vérifie si typedeftable est null
        if (typeDefTable != null){
            return (TypeTable) typeDefTable.getType();
        }
        // Type In
        Symbol typeInSymbol;
        if (dimension > 1){
            typeInSymbol = compiler.createSymbol(dimension-1 + "D " + getName());
        }else{
            typeInSymbol = getName();
        }
        // On construit le type
        ClassTypeTable typeTable = new ClassTypeTable(listSymbol, typeInSymbol);
        // On construit la définition
        compiler.environmentType.addTypes(listSymbol, new TypeDefinition(typeTable, Location.BUILTIN));
        // On vérifie si les dimensions inférieures existent dans l'environnement
        for (int index = 1; index < dimension; index++) {
            // On construit le symbol pour chaque dimension inférieure
            listSymbol = compiler.createSymbol(index + "D " + getName());
            // On vérifie si le type existe déjà dans l'environnement
            if (compiler.environmentType.defOfType(listSymbol) == null) {
                // On crée récursivement le type pour la dimension
                createTableType(compiler, index);
            }
            else{
                break;
            }
        }
        // On retourne le type
        return typeTable;
    }
    
}
