package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.instructions.BRA;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;
    private static final Logger LOG = Logger.getLogger(Main.class);
    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst While: start");

        Label e_cond = compiler.getLabelManager().getLabel("E_Cond");
        Label e_debut = compiler.getLabelManager().getLabel("E_Debut");
        compiler.addInstruction(new BRA( e_cond )); //BRA E
        compiler.addLabel( e_debut );
        body.codeGenListInst(compiler);
        compiler.addLabel(e_cond);
        condition.codeGenCond(true, e_debut , compiler);
        LOG.debug("codeGenInst While: end");

    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
                LOG.debug("verifyInst While: start");

                // On retourne le type de la condition:
                Type typeCondition = this.condition.verifyExpr(compiler, localEnv, currentClass);
                // On retourne le symbol du type.
                Symbol symbolTypeCondition = typeCondition.getName();
                // On vérifie si le type est boolean.
                if (!symbolTypeCondition.getName().equals("boolean")){
                    throw new ContextualError("La condition doit étre de type boolean, mais pas de type " + symbolTypeCondition + ". " , getLocation());
                }
                // On vérifie la liste des instructions
                this.body.verifyListInst(compiler, localEnv, currentClass, returnType);
                LOG.debug("verifyInst While: end");
            }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }


}
