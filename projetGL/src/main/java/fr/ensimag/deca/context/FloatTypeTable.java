package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 *
 * @author gl47
 * @date 11/01/2025
 */
public class FloatTypeTable extends TypeTable {

    // par défault name = Float
    public FloatTypeTable(SymbolTable.Symbol name, Symbol typeIn) {
        super(name, typeIn);
    }

    @Override
    public boolean isFloatTable(){
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on profite des méthode isTable() et isFloatTable().
        if (otherType.isTable()){
            TypeTable otherTypeTable = (TypeTable) otherType;
            return otherTypeTable.isFloatTable();
        }
        return false;
    }
}