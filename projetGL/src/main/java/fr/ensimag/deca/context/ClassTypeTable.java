package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeTable;
/**
 * 
 * @author gl47
 * @date 12/01/2025
 * 
 */
public class ClassTypeTable extends TypeTable {
    
    public ClassTypeTable(SymbolTable.Symbol name, Symbol typeIn){
        super(name, typeIn);
    }

    @Override
    public boolean isClassTable(){
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on profite des méthode isTable() et isIntTable().
        if (otherType.isTable()){
            TypeTable otherTypeTable = (TypeTable) otherType;
            // On retourne le type in
            Symbol symbolTypeIn = otherTypeTable.getTypeIn();
            // retourne true au cas d'égalité, false si non.
            return getTypeIn().equals(symbolTypeIn);
        }
        return false;
    }
}