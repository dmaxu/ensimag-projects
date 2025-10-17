package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import java.util.List;

import fr.ensimag.ima.pseudocode.DAddr;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Initialization extends AbstractInitialization {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    public boolean isNoInitialization(){
        return false;
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t, ListExpr taille,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
                LOG.debug("verifyInitialization: start");

                EnvironmentType environmentType = compiler.environmentType;
                // la méthode compare entre le type de l'initialization et le type de la variable
                // la méthode enléve une erreur lorque les types de la variables et de l'initialisation sont différents.
                Type typeInit = getExpression().verifyExpr(compiler, localEnv, currentClass);
                // teste si typeInit est un sous type de t:
                if (!typeInit.isSousType(environmentType, t) && !(typeInit.isInt() && t.isFloat())){
                    throw new ContextualError("On ne peut pas affectée à " + t.getName() + " un type " + typeInit.getName() + ". " , getLocation());
                }
                // Vérification du type
                if (t.isTable()){
                    // On vérifie l'égalité des taille
                    if (getExpression().getTailleExpr(compiler, localEnv, currentClass).size() != taille.size()){
                        throw new ContextualError("Probléme dans les tailles des listes. ", getLocation());
                    }
                }
                // On vérifie si la variable est de type float et l'initialisation est de type int
                if (t.isFloat() && typeInit.isInt()){
                    // Construction de convfloat
                    ConvFloat newExpression = new ConvFloat(getExpression());
                    // On modéfie l'expression
                    setExpression(newExpression);
                    newExpression.verifyExpr(compiler, localEnv, currentClass);
                }
                LOG.debug("verifyInitialization: end");


    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" = ");
        expression.decompile(s);
    }

    protected void codeGenInit(DecacCompiler compiler, DAddr addr) throws CLIException {
        LOG.debug("codeGenInit: start");
        expression.codeGenInst(compiler);
        compiler.addInstruction(new STORE(compiler.getMemoryManager().getLastRegister(compiler), addr));
        LOG.debug("codeGenInit: end");

    }

    public void codeGenInitField(DecacCompiler compiler, Type type) throws CLIException {
        getExpression().codeGenInst(compiler);
    }


    @Override
    protected
    void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }
}