package fr.ensimag.deca.context;

import java.util.Objects;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.TypeTable;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Deca Type (internal representation of the compiler)
 *
 * @author gl47
 * @date 01/01/2025
 */

public abstract class Type {


    /**
     * True if this and otherType represent the same type (in the case of
     * classes, this means they represent the same class).
     */
    public abstract boolean sameType(Type otherType);

    private final Symbol name;

    public Type(Symbol name) {
        this.name = name;
    }

    public Symbol getName() {
        return name;
    }

    public boolean isList(){
        return false;
    }

    public boolean isMatrix(){
        return false;
    }

    public boolean isTable(){
        return false;
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    public boolean isClass() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isClassOrNull() {
        return false;
    }
    /**
     * Returns the same object, as type ClassType, if possible. Throws
     * ContextualError(errorMessage, l) otherwise.
     *
     * Can be seen as a cast, but throws an explicit contextual error when the
     * cast fails.
     */
    public ClassType asClassType(String errorMessage, Location l)
            throws ContextualError {
        throw new ContextualError(errorMessage, l);
    }

    // Méthode qui retourne true lorsque this est un sous-type de type, false si non.
    public boolean isSousType(EnvironmentType environmentType, Type type){
        // TypeDefintion de l'expresion que on va retourner:
        TypeDefinition typeDef = environmentType.defOfType(getName());
        // typeDefReturnedion à retourner:
        TypeDefinition typeDefComp = environmentType.defOfType(type.getName());
        // Vérification si null
        if (type.isNull()){
            return true;
        }
        // ON vérifie si null
        if (typeDefComp.getType().isNull()){
            return true;
        }
        if (isClass() && type.isClass()) {
            ClassDefinition classDef = (ClassDefinition) typeDef;
            ClassDefinition classDefCmp = (ClassDefinition) typeDefComp;
            if (!classDef.isSubClass(classDefCmp)) {
                return false;
            }
            return true;
        }
        // Sinon donc les operandes ont des types prédéfinies.
        if (!isClass() && !type.isClass()){
            String nomType = getName().getName();
            String nomTypeCmp = type.getName().getName();
            if (!nomType.equals(nomTypeCmp)){
                return false;
            }
            return true;
        }
        return false;
    }
    // Comparaison des types basée sur le nom (Symbol)
    @Override
    public boolean equals(Object obj) {
        // On compare la référence.
        if (this == obj) {
            return true; 
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;  
        }
        Type other = (Type) obj;
        // Comparaison des symboles
        return this.name.getName().equals(other.name.getName());  
    }

    // Génération du hashCode basé sur le nom
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public TypeTable createTableType(DecacCompiler compiler, int dimension) throws ContextualError{
        throw new UnsupportedOperationException("n'est pas définie!");
    }
}
