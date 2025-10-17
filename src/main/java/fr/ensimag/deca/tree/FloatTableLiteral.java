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
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatTypeTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.IndentPrintStream;


import java.io.PrintStream;

/**
 * Integer literal
 *
 * @author gl47
 * @date 01/01/2025
 */
public class FloatTableLiteral extends TableLiteral {

    private List<FloatLiteral> listLiteral = new ArrayList<>();
    private List<Float> listValue = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger(Main.class);

    public void addLiteral(FloatLiteral value){
        this.listLiteral.add(value);
        this.listValue.add(value.getValue());
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr FloatTableLiteral: start");

                // ON vérifie tous les Nombres à l'intérieur
                for (FloatLiteral element : listLiteral){
                    // On vérifie le type de l'expression
                    element.verifyExpr(compiler, localEnv, currentClass);
                }
                Symbol typeInSymbol;
                // On traite le cas
                if (getTaille().size() > 1){
                    typeInSymbol = compiler.createSymbol(getTaille().size()-1 + "D float");
                }else{
                    typeInSymbol = compiler.createSymbol("float");
                }
                // On construit le type int.
                FloatTypeTable nombreTypeList = new FloatTypeTable(compiler.symbolTable.create(getTaille().size()+"D float"), typeInSymbol);
                // On preserve le type de l'expression.
                setType(nombreTypeList);
                // On retourne le type
                LOG.debug("verifyExpr FloatTableLiteral: end");
                return nombreTypeList;
    }

    @Override
    String prettyPrintNode() {
        return "TableFloat [taille=" + getTaille() + "]";
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
        for (FloatLiteral literal : listLiteral) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(literal.getValue()), Register.R0));
            RegisterOffset regOff = new RegisterOffset(idx,reg);
            compiler.addInstruction(new STORE(Register.R0, regOff));
            idx++;
        }
    }
}