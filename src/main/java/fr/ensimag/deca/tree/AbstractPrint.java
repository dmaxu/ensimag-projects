package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;


import java.io.PrintStream;
import java.nio.channels.UnsupportedAddressTypeException;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Print statement (print, println, ...).
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class AbstractPrint extends AbstractInst {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
            // boucle sur les axpressions
            LOG.debug("verify Print Inst: start");
            for (AbstractExpr expr : this.arguments.getList()) {
                // On vérifie la validité de chaque expression.
                Type argType = expr.verifyExpr(compiler, localEnv, currentClass);
                // On vérifie si l'expression est de type prédéfinie.
                if (argType.isInt()||argType.isString()||argType.isFloat()) {
                    // On preserve le type de l'expression
                    expr.setType(argType);
                }
                else{
                    throw new ContextualError("Print est valable seulement pour les types : int, float et string.", expr.getLocation());
                }
            }
            LOG.debug("verify Print Inst: end");

    }
    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("CodeGen Inst Print: start");

        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrint(compiler,printHex);
        }
        LOG.debug("CodeGen Inst Print: end");

    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        s.print("print" + getSuffix());
        if (getPrintHex()) {
            s.print("x");
        }
        s.print("(");
        getArguments().decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
