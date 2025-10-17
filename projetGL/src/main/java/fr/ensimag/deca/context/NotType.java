package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;

/**
 *
 * @author equipe gl47
 * @date 20/12/2024
 */
public class NotType extends Type {

    public NotType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        // on va vérifie si otherType.getName() et name ont le méme String.
        // car égalité en String <==> égalité en Symbol <==> Égalité en Type
        if (otherType.getName().getName().equals(getName().getName())){
            return true;
        }
        return false;
    }
}
