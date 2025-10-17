package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;

/**
 * Entry point for contextual verifications and code generation from outside the package.
 * 
 * @author gl47
 * @date 01/01/2025
 *
 */
public abstract class AbstractProgram extends Tree {
    public abstract void verifyProgram(DecacCompiler compiler) throws ContextualError, DoubleDefException;
    public abstract void codeGenProgram(DecacCompiler compiler) throws CLIException;

}
