package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 *
 * @author gl47
 * @date 11/01/2025
 */
public class BooleanTypeTable extends TypeTable {

    // par défault name = Boolean
    public BooleanTypeTable(SymbolTable.Symbol name, Symbol typeIn) {
        super(name, typeIn);
    }

    @Override
    public boolean isBooleanTable(){
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on profite des méthode isTable() et isBooleanTable().
        if (otherType.isTable()){
            TypeTable otherTypeTable = (TypeTable) otherType;
            return otherTypeTable.isBooleanTable();
        }
        return false;
    }
}