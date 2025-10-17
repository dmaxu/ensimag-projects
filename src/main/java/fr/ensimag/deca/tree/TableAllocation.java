package fr.ensimag.deca.tree;

import java.util.List;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

import java.io.PrintStream;
import java.util.List;

import fr.ensimag.deca.context.Type;

public class TableAllocation extends AbstractExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);
    private AbstractIdentifier type;
    private ListExpr dimension;

    public TableAllocation(AbstractIdentifier type, ListExpr dimension) {
        this.type = type;
        this.dimension = dimension;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler,
                           EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        LOG.debug("verifyExpr TableAllocation: start");
        // On vérifie que dimension est de type (Int)^N
        for (AbstractExpr expr : this.dimension.getList()) {
            // On retourne le type de l'expression
            Type typeExpr = expr.verifyExpr(compiler, localEnv, currentClass);
            // Vérification
            if (!typeExpr.isInt()) {
                throw new ContextualError("L'allocation doit étre de type Int. ", expr.getLocation());
            }
        }
        // On retourne la définition du type
        TypeDefinition typeDefinition = compiler.environmentType.defOfType(this.type.getName());
        // Vérification du type
        if (typeDefinition != null) {
            // On retourne le type
            Type typeAllocation = typeDefinition.getType();
            // On construit les type
            typeAllocation.createTableType(compiler, dimension.getList().size());
            // on vérifie la dimension
            Symbol symbolTable = compiler.createSymbol(dimension.getList().size() + "D " + typeAllocation.getName());
            // On retourne la définition du type
            TypeDefinition typeDefinitionTable = compiler.environmentType.defOfType(symbolTable);
            // On preserve la définition
            this.type.setDefinition(typeDefinitionTable);
            // On preserve le type
            this.type.setType(typeDefinitionTable.getType());
            // on vérifie le type
            if (typeDefinitionTable != null) {
                // on preserve le type
                setType(typeDefinitionTable.getType());
                LOG.debug("verifyExpr TableAllocation: start");
                // on retourne le type
                return typeDefinitionTable.getType();
            }
            throw new ContextualError("le type " + symbolTable + " est pas inconnue. ", getLocation());
        }
        throw new ContextualError("le type " + this.type.getName() + " est pas inconnue. ", getLocation());
    }

    @Override
    String prettyPrintNode() {
        return "TableAllocation";
    }

    // A FAIRE :
    //Allocation en utilisant listExpr

    @Override
    protected ListExpr getTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                                     ClassDefinition currentClass) throws ContextualError {
        // On retourne la taille
        return this.dimension;
    }

    @Override
    protected void setTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                                 ClassDefinition currentClass, ListExpr newTaille) throws ContextualError {
        // On retourne la taille
        this.dimension = newTaille;
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
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        TypeDefinition def = compiler.environmentType.defOfType(this.type.getName());
        GPRegister regForSize = Register.R1;
        compiler.addInstruction(new LOAD(1, regForSize));
        for (AbstractExpr number : this.dimension.getList()) {
            number.codeGenInst(compiler);
            GPRegister registerNumber = compiler.getMemoryManager().getLastRegister(compiler);
            compiler.addInstruction(new CMP(new ImmediateInteger(0), registerNumber));
            compiler.addInstruction(new BLE(compiler.getAllocationProblem()));
            compiler.addInstruction(new MUL(registerNumber, regForSize));
        }

        GPRegister reg = compiler.getMemoryManager().getRegister(compiler);

        compiler.addInstruction(new NEW(regForSize, reg));
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(compiler.getHeapOverflow()));
        }

        //Init with default value :
        GPRegister iReg = compiler.getMemoryManager().getRegister(compiler);
        compiler.addInstruction(new LOAD(0, iReg));
        GPRegister tempReg = compiler.getMemoryManager().getRegister(compiler); // to store temp address
        compiler.addInstruction(new LOAD(reg, tempReg));
        // For loop in assembly
        Label forLabel = compiler.getLabelManager().getLabel("forLabel");
        Label endFor = compiler.getLabelManager().getLabel("endForLabel");
        compiler.addLabel(forLabel);

        compiler.addInstruction(new CMP(iReg, regForSize));
        compiler.addInstruction(new BLE(endFor));



        if (this.type.getName().getName().equals("int") || this.type.getName().getName().equals("boolean")) {
            compiler.addInstruction(new LOAD(0, Register.R0));
        } else if (this.type.getName().getName().equals("float")) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), Register.R0));
        } else {
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        }

        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0,tempReg)));
        compiler.addInstruction(new LEA(new RegisterOffset(1,tempReg),tempReg)); //increase address by ONE
        compiler.addInstruction(new ADD(new ImmediateInteger(1),iReg));
        compiler.addInstruction(new BRA(forLabel));

        compiler.addLabel(endFor);

        compiler.getMemoryManager().getLastRegister(compiler); //Get rid of i register
        compiler.getMemoryManager().getLastRegister(compiler);//Get rid of tempReg
    }
}
