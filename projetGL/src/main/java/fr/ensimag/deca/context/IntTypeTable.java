package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 *
 * @author gl47
 * @date 11/01/2025
 */
public class IntTypeTable extends TypeTable {

    // par défault name = Int
    public IntTypeTable(SymbolTable.Symbol name, Symbol typeIn) {
        super(name, typeIn);
    }

    @Override
    public boolean isIntTable(){
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on profite des méthode isTable() et isINtTable().
        if (otherType.isTable()){
            TypeTable otherTypeTable = (TypeTable) otherType;
            return otherTypeTable.isIntTable();
        }
        return false;
    }
}