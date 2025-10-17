package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.NullType;

import java.io.PrintStream;

import org.apache.log4j.Logger;

public class NullLiteral extends AbstractExpr{
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Object getValue(){
        return null;
    }

    public NullLiteral(){

    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError {
            LOG.debug("verifyExpr NullLiteral: start");
            // On construit le type NUll.
            NullType nullType = new NullType(compiler.symbolTable.create("null"));
            // On preserve le type
            setType(nullType);
            // On retourne le type
            LOG.debug("verifyExpr NullLiteral: end");
            return nullType;
    }
   
    @Override
    String prettyPrintNode() {
        return "Null (" +  ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }
        
}
