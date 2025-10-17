package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of declarations (e.g. int x; float y,z).
 *
 * @author gl47
 * @date 01/01/2025
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {
    private static final Logger LOG = Logger.getLogger(Main.class);

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclVar declVar : getList()) {
            declVar.decompile(s);
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     contains the "env_types" attribute
     * @param localEnv     its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *                     in precondition, its "current" dictionary corresponds to
     *                     the "env_exp" attribute
     *                     in postcondition, its "current" dictionary corresponds to
     *                     the "env_exp_r" attribute
     * @param currentClass corresponds to "class" attribute (null in the main bloc).
     */
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
                                ClassDefinition currentClass) throws ContextualError, DoubleDefException {
        // A FAIRE :
        LOG.debug("verifyListDeclVariable: start");

        // On vérifie que chaqu'une des variables est bien déclarée.
        for (AbstractDeclVar decl : getList()) {
            decl.verifyDeclVar(compiler, localEnv, currentClass);
        }
        LOG.debug("verifyListDeclVariable: end");

    }


    public void codeGenProgram(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenProgram: start");

        for (AbstractDeclVar decl : getList()) {
            decl.codeGenDeclVar(compiler);
        }
        LOG.debug("codeGenProgram: end");

    }

    public void codeGenDeclListVarLocal(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenDeclVarLocal: start");
        int idx = 0;
        for (AbstractDeclVar decl : getList()) {
            idx++;
            decl.codeGenDeclVarLocal(compiler, idx);
            compiler.getMemoryManager().incrementTempInMethod();
        }
        LOG.debug("codeGenDeclVarLocal: end");

    }


}
