package fr.ensimag.deca.tree;

import java.util.List;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractInitialization;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

/**
 * List of DeclMethod.
 *
 * @author gl47
 * @date 26/12/2024
 */

public class ListDeclMethod extends TreeList<DeclMethod> {
    private static final Logger LOG = Logger.getLogger(Main.class);


    // A FAIRE
    @Override
    public void decompile(IndentPrintStream s) {
        for (DeclMethod declMethod : getList()) {
            declMethod.decompile(s);
            s.println();
        }
    }

    public List<Symbol> getListSymbolMethod(){
        List<Symbol> listeSymb = new ArrayList<>();
        for (DeclMethod declMethod : getList()){
            listeSymb.add(declMethod.getName().getName());
        }
        return listeSymb;
    }

    void verifyListDeclMethod(DecacCompiler compiler, EnvironmentType envType,
                              Symbol superClassName, ClassDefinition classDef ,EnvironmentExp localEnvClass) throws ContextualError {
        // A FAIRE :
        LOG.debug("verifyListDeclMethod: start");

        for (DeclMethod decl : getList()) {
            // On incrémente l'indice des méthodes.
            classDef.incNumberOfMethods();
            // on Construit un environment pour la méthode
            EnvironmentExp localEnvMethod = new EnvironmentExp(localEnvClass);
            // On vérifie sa déclaration
            decl.verifyDeclMethod(compiler, envType, superClassName, classDef.getType().getName(),localEnvMethod, localEnvClass, classDef.getNumberOfMethods());
        }
        LOG.debug("verifyListDeclMethod: end");

    }

    protected void codegenListMethod(DecacCompiler compiler, String className) throws CLIException {
        LOG.debug("codegenListMethod: start");
        for (DeclMethod decl : getList()) {
            decl.codegenMethod(compiler, className);
        }
        LOG.debug("codegenListMethod: end");

    }

}
