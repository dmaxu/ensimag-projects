package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractInitialization;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.util.List;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * List of DeclFieldSet.
 *
 * @author gl47
 * @date 26/12/2024
 */

public class ListDeclFieldSet extends TreeList<DeclFieldSet> {
    // A FAIRE
    private static final Logger LOG = Logger.getLogger(Main.class);

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Not yet implemented");
        for (DeclFieldSet declField : getList()) {
            declField.decompile(s);
            s.println();
        }
    }

    EnvironmentExp verifyListDeclFieldSet(DecacCompiler compiler, EnvironmentType envType,
                                          EnvironmentExp localEnvParent, Symbol superClassName, Symbol className) throws ContextualError, DoubleDefException {
    LOG.debug("verifyListDeclFieldSet: start");

        // on vérifie que chaqu'une des attributs est bien définie
        EnvironmentExp localEnvExp = new EnvironmentExp(localEnvParent);
        for (DeclFieldSet decl : getList()) {
            // On vérifie l'indice
            decl.verifyDeclFieldSet(compiler, envType, superClassName, localEnvExp, className);
        }
        LOG.debug("verifyListDeclFieldSet: end");

        return localEnvExp;
    }

    /**
     * Cette méthode vérifie une liste des attributs
     * @param compiler
     * @param localEnv
     * @param currentClass
     * @throws ContextualError
     * 
     */
    void verifyListDeclFieldSetInit(DecacCompiler compiler,  EnvironmentExp localEnv, 
        ClassDefinition currentClass) throws ContextualError{
        // On vérifie les intialisations des attributs
        LOG.debug("verifyListDeclFieldSetInit: start");

        for (DeclFieldSet decl : getList()) {
            // On vérifie l'indice
            decl.verifyDeclFieldSetInit(compiler, localEnv, currentClass);
        }
        LOG.debug("verifyListDeclFieldSetInit: end");

    }

    protected void codegenFieldSet(DecacCompiler compiler) throws CLIException {
        //TODO: visibility not treated yet
        LOG.debug("codegenFieldSet: start");

        for (DeclFieldSet decl : getList()) {
            decl.getListDeclField().codeGenListField(compiler);
        }
        LOG.debug("codegenFieldSet: end");

    }

}
