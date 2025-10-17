package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Full if/else if/else statement.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class IfThenElse extends AbstractInst {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private final AbstractExpr condition;
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }


    public void setElseBranch(ListInst elseBranch) {
        this.elseBranch = elseBranch;
    }

    public ListInst getElseBranch() {
        return this.elseBranch;
    }

    @Override
    public void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass, Type returnType)
            throws ContextualError {
                LOG.debug("verifyInst IfTheneElse: start");

                // On vérifie la construction + On retourne le type  de l'expression condition.
                Type typeCondition = this.condition.verifyExpr(compiler, localEnv, currentClass);
                // On retourne le symbol du type.
                Symbol symbolTypeCondition = typeCondition.getName();
                // On vérifie si le type est boolean.
                if (!symbolTypeCondition.getName().equals("boolean")){
                    throw new ContextualError("La condition doit étre de type boolean, mais pas de type " + symbolTypeCondition + ". " , getLocation());
                }
                // On vérifie la liste des instruction dans thenBranch
                this.thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
                // On vérifie la liste des instruction dans elseBranch
                this.elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
                LOG.debug("verifyInst IfTheneElse: end");

            }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst IfTheneElse: start");

        Label elseLabel = compiler.getLabelManager().getLabel("else");
        Label endLabel = compiler.getLabelManager().getLabel("end");

        condition.codeGenCond(false, elseLabel, compiler);
        thenBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(endLabel));
        compiler.addLabel(elseLabel);
        elseBranch.codeGenListInst(compiler);
        compiler.addLabel(endLabel);
        LOG.debug("codeGenInst IfTheneElse: end");

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if(");
        condition.decompile(s);
        s.println("){");
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.println("} else {");
        s.indent();
        elseBranch.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
