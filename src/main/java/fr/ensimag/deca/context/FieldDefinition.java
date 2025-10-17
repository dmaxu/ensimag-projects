package fr.ensimag.deca.context;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ClassDefinition;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.util.List;

/**
 * Definition of a field (data member of a class).
 *
 * @author gl47
 * @date 01/01/2025
 */
public class FieldDefinition extends ExpDefinition {
    public int getIndex() {
        return index;
    }

    private int index;
    
    @Override
    public boolean isField() {
        return true;
    }

    private final Visibility visibility;
    private final ClassDefinition containingClass;
    
    public FieldDefinition(Type type, Location location, Visibility visibility,
            ClassDefinition memberOf, int index, ListExpr taille) {
        super(type, taille,location);
        this.visibility = visibility;
        this.containingClass = memberOf;
        this.index = index;
    }
    
    @Override
    public FieldDefinition asFieldDefinition(String errorMessage, Location l)
            throws ContextualError {
        return this;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public ClassDefinition getContainingClass() {
        return containingClass;
    }

    @Override
    public String getNature() {
        return "field";
    }

    @Override
    public boolean isExpression() {
        return true;
    }

    public void codegenLoad(DecacCompiler compiler) throws CLIException {
        DAddr addr = this.getOperand();
        compiler.addInstruction(new LOAD(addr, compiler.getMemoryManager().getRegister(compiler)));
        GPRegister lastReg = compiler.getMemoryManager().getLastRegister(compiler);
        DAddr fieldAddr = new RegisterOffset(index, lastReg);

        compiler.addInstruction(new LOAD(fieldAddr, compiler.getMemoryManager().getRegister(compiler)));
    }

}