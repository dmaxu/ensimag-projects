package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DVal;

/**
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class StringType extends Type {

    public StringType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on utilise la méthode isString pour vérifier si otherType et this ont le méme type.
        return otherType.isString();
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
        StringTypeTable typeTable = new StringTypeTable(listSymbol, typeInSymbol);
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
