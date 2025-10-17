package fr.ensimag.deca.context;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.deca.tree.ListExpr;

import java.util.List;

/**
 * Definition of a variable.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class VariableDefinition extends ExpDefinition {

    public VariableDefinition(Type type, Location location, ListExpr taille) {
        super(type, taille,location);
    }


    @Override
    public String getNature() {
        return "variable";
    }

    @Override
    public boolean isExpression() {
        return true;
    }
    @Override
    public boolean isVariable(){
        return true;
    }

    public void codegenLoad(DecacCompiler compiler) throws CLIException {
        // Obtenir l'adresse de l'identifiant
        DAddr address = this.getOperand();

        // Obtenir un registre pour stocker la valeur
        GPRegister reg = compiler.getMemoryManager().getRegister(compiler);

        // Charger la valeur de l'adresse dans le registre
        compiler.addInstruction(new LOAD(address, reg));
    }

}