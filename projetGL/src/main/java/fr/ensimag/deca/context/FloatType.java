package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.ima.pseudocode.ImmediateFloat;

/**
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class FloatType extends Type {

    public FloatType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on utilise la méthode isFloat pour vérifier si otherType et this ont le méme type.
        return otherType.isFloat();
    }

    @Override
    public TypeTable createTableType(DecacCompiler compiler, int dimension) throws ContextualError{
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
        FloatTypeTable typeTable = new FloatTypeTable(listSymbol, typeInSymbol);
        // On construit la définition
        compiler.environmentType.addTypes(listSymbol, new TypeDefinition(typeTable, Location.BUILTIN));
        // On vérifie si les dimension inférieur existe dans l'environment
        for (int index = 1; index < dimension; index ++){
            // On construit le symbol
            listSymbol = compiler.createSymbol(index + "D " + getName());
            // On vérifie si le type existes
            if (compiler.environmentType.defOfType(listSymbol) == null){
                // On construit le type
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
