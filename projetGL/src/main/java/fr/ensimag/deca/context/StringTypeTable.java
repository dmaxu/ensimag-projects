package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 *
 * @author gl47
 * @date 11/01/2025
 */
public class StringTypeTable extends TypeTable {

    // par défault name = String
    public StringTypeTable(SymbolTable.Symbol name, Symbol typeIn) {
        super(name, typeIn);
    }

    @Override
    public boolean isStringTable(){
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on profite des méthode isTable() et isStringTable().
        if (otherType.isTable()){
            TypeTable otherTypeTable = (TypeTable) otherType;
            return otherTypeTable.isStringTable();
        }
        return false;
    }
}