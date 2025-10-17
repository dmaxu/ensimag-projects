package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import java.util.List;

import org.apache.log4j.Logger;


import java.io.PrintStream;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl47
 * @date 01/01/2025
 */
public class NoInitialization extends AbstractInitialization {
    private static final Logger LOG = Logger.getLogger(Main.class);
    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t, ListExpr taille,
                                        EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        LOG.debug("verifyInitialization: start");

        // On vérife si le type t existe dans l'environmentType
        TypeDefinition typeDefinition = compiler.environmentType.defOfType(t.getName());
        // On vérifie si typeDefinition est null.
        if (typeDefinition == null) {
            throw new ContextualError("Le type " + t.getName() + " n'est pas définie dans l'environment des types. ", getLocation());
        }
        LOG.debug("verifyInitialization: end");
    }


    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    protected void codeGenInit(DecacCompiler compiler, DAddr addr) throws CLIException{

    }

    @Override
    public void codeGenInitField(DecacCompiler compiler, Type type) throws CLIException {
        LOG.debug("codeGenInitField: start");
        if (type.getName().getName().equals("int") || type.getName().getName().equals("boolean")) {
            compiler.addInstruction(new LOAD(0, compiler.getMemoryManager().getRegister(compiler)));
        }
        else if (type.getName().getName().equals("float")) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), compiler.getMemoryManager().getRegister(compiler)));
        }
        else {
            compiler.addInstruction(new LOAD(new NullOperand(), compiler.getMemoryManager().getRegister(compiler))); // Case Object
        }
        LOG.debug("codeGenInitField: end");
    }


}
