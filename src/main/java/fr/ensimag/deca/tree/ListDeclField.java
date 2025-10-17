package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.List;

import org.apache.log4j.Logger;

public class ListDeclField extends TreeList<DeclField> {
    private static final Logger LOG = Logger.getLogger(Main.class);

    @Override
    public void decompile(IndentPrintStream s) {
        // Parcourt tous les champs déclarés dans la liste
        for (DeclField field : getList()) {
            // Appelle la méthode decompile de chaque champ
            field.decompile(s);
            // Ajoute un point virgule après chaque déclaration de champ pour lisibilité
            s.print(";");
        }
    }


    public void verifyListDeclField(DecacCompiler compiler,
                                    EnvironmentExp localEnv, ClassDefinition currentClass, Type type, Visibility visbility, ListExpr taille)
            throws ContextualError, DoubleDefException {
        // On boucle sur les éléments de la liste
        LOG.debug("verifyListDeclField: start");

        for (DeclField field : getList()) {
            // on vérifie l'attribut
            field.verifyDeclField(compiler, localEnv, currentClass, type, taille, visbility);
        }
        LOG.debug("verifyListDeclField: end");

    }

    /**
     * Cette méthode vérifie l'initalisation des attributs
     * @param compiler
     * @param typeName
     * @param taille
     * @param localEnv
     * @param currentClass
     * @throws ContextualError
     * 
     */
    public void verifyListDeclFieldInit(DecacCompiler compiler, Type typeName, ListExpr taille, EnvironmentExp localEnv, 
        ClassDefinition currentClass) throws ContextualError{
        // On boucle sur les éléments de la liste
        LOG.debug("verifyListDeclFieldInit: start");

        for (DeclField field : getList()) {
            // On vérifie les initialization
            field.verifyDeclFieldInit(compiler, typeName, taille, localEnv, currentClass);
        }
        LOG.debug("verifyListDeclFieldInit: end");

    }

    protected void codeGenListField(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenListField: start");

        for (DeclField field : this.getList()) {
            field.codeGenField(compiler);
            compiler.getMemoryManager().releaseRegisters(compiler);
        }
        LOG.debug("codeGenListField: end");

    }


}