package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;

/**
 *
 * @author equipe gl47
 * @date 20/12/2024
 */
public class UnaryMinusType extends Type {

    public UnaryMinusType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on utilise la méthode isInt pour vérifier si otherType et this ont le méme type.
        return otherType.isInt();
    }


}
