package fr.ensimag.deca.tree;

import java.util.List;

import fr.ensimag.deca.CLIException;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.BooleanTypeTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

/**
 * Boolean Lst literal
 *
 * @author gl47
 * @date 01/01/2025
 */
public class BooleanTableLiteral extends TableLiteral {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private List<BooleanLiteral> listLiteral = new ArrayList<>();
    private List<Boolean> listValue = new ArrayList<>();

    public void addLiteral(BooleanLiteral value){
        this.listLiteral.add(value);
        this.listValue.add(value.getValue());
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr BooleanTableLiteral: start");

                // ON vérifie tous les Nombres à l'intérieur
                for (BooleanLiteral element : listLiteral){
                    // On vérifie le type de l'expression
                    element.verifyExpr(compiler, localEnv, currentClass);
                }
                // le type du symbol à l'intérieur
                Symbol typeInSymbol;
                // On traite les cas
                if (getTaille().size() > 1){
                    typeInSymbol = compiler.createSymbol(getTaille().size()-1 + "D boolean");
                }else{
                    typeInSymbol = compiler.createSymbol("boolean");
                }
                // On construit le type int.
                BooleanTypeTable nombreTypeList = new BooleanTypeTable(compiler.symbolTable.create(getTaille().size()+"D boolean"), typeInSymbol);
                // On preserve le type de l'expression.
                setType(nombreTypeList);
                LOG.debug("verifyExpr BooleanTableLiteral: end");
                // On retourne le type
                return nombreTypeList;
    }

    @Override
    String prettyPrintNode() {
        return "TableBoolean [taille=" + getTaille() + "]";
    }

    @Override
    public void decompile(IndentPrintStream s) {
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException{
        GPRegister reg = compiler.getMemoryManager().getRegister(compiler);
        GPRegister regSize = Register.R1;
        compiler.addInstruction(new LOAD(1, regSize));
        for(AbstractExpr expr : this.getTaille().getList()){
            expr.codeGenInst(compiler);
            GPRegister registerInter = compiler.getMemoryManager().getLastRegister(compiler);
            compiler.addInstruction(new MUL(registerInter, regSize));
        }

        compiler.addInstruction(new NEW(regSize,reg));
        int idx = 0;
        for (BooleanLiteral literal : listLiteral) {
            compiler.addInstruction(new LOAD(literal.getValue()?1:0 , Register.R0));
            RegisterOffset regOff = new RegisterOffset(idx,reg);
            compiler.addInstruction(new STORE(Register.R0, regOff));
            idx++;
        }
    }
}