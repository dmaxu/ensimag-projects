package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.tree.Identifier;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * Cette classe nous permet de vérifier lors de la construction d'une variable sous la forme : Class var = new Class();
 *
 * @author gl47
 * @date 29/12/2024
 */

public class New extends AbstractExpr{
    private static final Logger LOG = Logger.getLogger(Main.class);
    private AbstractIdentifier identifier;
    /**
     * @param identifier
     */
    public New(AbstractIdentifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Cette méthode vérifie si la classe est déja déclaré avant dans l'environmentType.
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnvironmentExp
        , ClassDefinition currentClass) throws ContextualError {
        LOG.debug("verifyExpr NEW: start");
        // L'environmnent Type :
        EnvironmentType environmentType = compiler.environmentType;
        // Si typeDef est null : donc la classe n'est pas définie dans l'environmentType.
        Type type = this.identifier.verifyType(compiler);
        // Sinon, donc la classe est déja définie dans l'environmentType.
        if (type.isClass() || type.isTable()) {
            // on preserve le type
            setType(type);
            LOG.debug("verifyExpr NEW: end");
            // On retourne le type
            return type;
        }
        throw new ContextualError("Expression Invalide!, valable seulement pour les classes ou pour les tableaux. ", getLocation());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        this.identifier.decompile(s);
        s.print("(");
        s.print(")");
    }

    @Override
    String prettyPrintNode() {
        return "New :";
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        this.identifier.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        identifier.prettyPrint(s, prefix, true);
    }

    protected void codeGenInst(DecacCompiler compiler) {
        int sizeClass = this.identifier.getClassDefinition().getNumberOfFields() + 1;
        GPRegister reg = compiler.getMemoryManager().getRegister(compiler);
        compiler.addInstruction(new NEW(sizeClass, reg));
        DAddr addrMethodTable = this.identifier.getClassDefinition().getAddressInVtable();
        Label init = new Label("init." + identifier.getName());
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(compiler.getHeapOverflow()));
        }
        compiler.addInstruction(new LEA(addrMethodTable, Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, reg)));
        compiler.addInstruction(new PUSH(reg));
        compiler.addInstruction(new BSR(init));
        compiler.addInstruction(new POP(reg));
    }


}
