package fr.ensimag.deca.context;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.util.List;

/**
 * Definition of a method parameter.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class ParamDefinition extends ExpDefinition {

    public ParamDefinition(Type type, ListExpr taille, Location location) {
        super(type, taille, location);
    }

    @Override
    public String getNature() {
        return "parameter";
    }

    @Override
    public boolean isExpression() {
        return true;
    }

    @Override
    public boolean isParam() {
        return true;
    }

    @Override
    public boolean isMethodParameter() {
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
