package fr.ensimag.deca.codegen;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.util.*;

public class MemoryManager {


    private int maxRegistersTemp = 0;
    private int globalVar = 1;
    private int maxRegisters = 15;
    private int registersInTemp = 0;
    private final Stack<Integer> availableReg = new Stack<>();
    private final Deque<Integer> usedRegister = new ArrayDeque<>();


    {
        for (int i = 2; i <= maxRegisters; i++) {
            availableReg.add(i);
        }
    }


    public synchronized int getGlobalVar() {
        return globalVar;
    }

    public synchronized void incrementGlobalVar() {
        globalVar++;
    }

    public void markFreeRegister(int i) {
        if (usedRegister.contains(i)) {
            usedRegister.remove(i);
            availableReg.add(i);
        }
    }


    /**
     * @param compiler
     * @return The number of the next register to use
     */
    public synchronized GPRegister getRegister(DecacCompiler compiler) {
        if (!availableReg.isEmpty()) {
            int reg = availableReg.pop();
            usedRegister.add(reg);
            return Register.getR(reg);
        } else {
            int reg = usedRegister.poll();
            compiler.addInstruction(new PUSH(Register.getR(reg)));
            registersInTemp++;
            if (registersInTemp > maxRegistersTemp) {
                maxRegistersTemp = registersInTemp;
            }
            usedRegister.add(reg);
            return Register.getR(reg);
        }
    }

    /**
     * Returns the last used register with a saved value in int and frees it
     *
     * @param compiler
     * @return Id of the last used register
     */
    public synchronized GPRegister getLastRegister(DecacCompiler compiler) throws CLIException {
        if (!usedRegister.isEmpty()) {
            int reg = usedRegister.pollLast();
            availableReg.add(reg);
            return Register.getR(reg);
        } else if (registersInTemp > 0) {
            int reg = availableReg.pop();
            usedRegister.add(reg);
            compiler.addInstruction(new PUSH(Register.getR(reg)));
            registersInTemp--;
            return Register.getR(reg);
        } else {
            throw new CLIException("No values saved in registers");
        }

    }


    public synchronized void releaseRegisters(DecacCompiler compiler) {
        availableReg.clear();
        for (int i = 2; i <= maxRegisters; i++) {
            availableReg.add(i);
        }
        usedRegister.clear();
    }

    public synchronized int getMaxRegisters() {
        return maxRegisters;
    }

    public synchronized void setMaxRegisters(int maxRegisters) {
        this.maxRegisters = maxRegisters;
        releaseRegisters(null);
    }

    public synchronized int getRegistersInTemp() {
        return registersInTemp;
    }

    /**
     * manually marks reg as used
     *
     * @param reg
     */
    public synchronized void markUsedRegister(int reg) {
        if (!usedRegister.contains(reg)) {
            usedRegister.add(reg);
            availableReg.removeElement(reg);
        }
    }

    public synchronized int getMaxRegistersTemp() {
        return maxRegistersTemp;
    }

    //Used to keep track of stack position when building Vtable
    private int lastTablePos = 0;

    public synchronized int getLastTablePos() {
        return lastTablePos;
    }

    public synchronized void setLastTablePos(int lastTablePosValue) {
        lastTablePos = lastTablePosValue;

    }

    public void saveRegisters(DecacCompiler compiler) {
        compiler.addComment("Début Sauvegarde de registres");
        for (int i = 2; i <= this.maxRegisters; i++) {
            compiler.addInstruction(new PUSH(Register.getR(i)));
        }
        compiler.addComment("Fin Sauvegarde de registres");
    }

    public void restoreRegisters(DecacCompiler compiler) {
        compiler.addComment("Début Restauration de registres");
        for (int i = maxRegisters; i >= 2; i--) {
            compiler.addInstruction(new POP(Register.getR(i)));
        }
        compiler.addComment("Fin Restauration de registres");
    }

    public void initTempInMethod() {
        maxRegistersTemp = maxRegisters;
    }

    public void incrementTempInMethod() {
        maxRegistersTemp++;
    }



}
