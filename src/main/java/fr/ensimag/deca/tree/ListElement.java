package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.TypeTable;
import fr.ensimag.deca.tools.IndentPrintStream;

import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.log4j.Logger;


import java.io.PrintStream;

public class ListElement extends TableauElement {

    public void setGetAddress(Boolean getAddress) {
        this.getAddress = getAddress;
    }

    Boolean getAddress = false;

    public ListExpr getListIndex() {
        return listIndex;
    }

    private AbstractIdentifier listVar;
    private ListExpr listIndex;
    private static final Logger LOG = Logger.getLogger(Main.class);

    public ListElement(AbstractIdentifier listVar, ListExpr listIndex) {
        this.listVar = listVar;
        this.listIndex = listIndex;
    }


    private ListExpr tailleVar;
    // on doit faire List des exprs (taille)
    public AbstractIdentifier getListVar() {
        return this.listVar;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        LOG.debug("verifyExpr ListElement: start");

        // On vérifie le type de listeVar
        Type typeVar = getListVar().verifyExpr(compiler, localEnv, currentClass);
        // On vérifie que le type est une liste
        if (typeVar.isTable()) {
            TypeTable typeVarTableau = (TypeTable) typeVar;
            // On retourne le type à l'intérieyr
            Type typeIn = typeVarTableau;
            // Boucle.
            for (AbstractExpr index : listIndex.getList()) {
                // On vérifie le type
                Type typeIndex = index.verifyExpr(compiler, localEnv, currentClass);
                // On vérifie si il est un int
                if (typeIn.isTable()) {
                    if (typeIndex.isInt()) {
                        TypeTable typeIntermidiaire = (TypeTable) typeIn;
                        // On preserve le type à l'intérieur de la liste
                        typeIn = compiler.environmentType.defOfType(typeIntermidiaire.getTypeIn()).getType();
                    } else {
                        // On eléve l'erreur
                        throw new ContextualError("l'index doit étre de type int. ", index.getLocation());
                    }
                } else {
                    throw new ContextualError("Expression Invalide!, valable seulement pour les tableaux. ", getLocation());
                }
            }
            // On retourne la  taille
            ListExpr taille = getListVar().getTailleExpr(compiler, localEnv, currentClass);
            // On preserve la nouvelle taille
            ListExpr newTaille = new ListExpr();
            if (typeIn.isTable()) {
                setGetAddress(true);
                newTaille.setList(taille.getList().subList(listIndex.size(), taille.size()));
            }
            // la nouvele taille
            setTaille(newTaille);
            // On preserve le type
            setType(typeIn);
            LOG.debug("verifyExpr ListElement: end");
            // On retourne le type
            return typeIn;
        }
        // On eléve l'erreur
        throw new ContextualError("Expression Invalide!, valable seulement pour les tableaux. ", getLocation());
    }

    @Override
    String prettyPrintNode() {
        return "ListElement ";
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
        this.listVar.prettyPrint(s, prefix, false);
        this.listIndex.prettyPrint(s, prefix, true);
    }

    public boolean isListElement() {
        return true;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        this.listVar.getExpDefinition().codegenLoad(compiler);
        GPRegister finalReg = compiler.getMemoryManager().getLastRegister(compiler);
        compiler.getMemoryManager().markUsedRegister(finalReg.getNumber());
        int numDimensions = this.listVar.getExpDefinition().getTaille().getList().size()-getTaille().size();
        GPRegister idxReg = compiler.getMemoryManager().getRegister(compiler);
        GPRegister strideReg = compiler.getMemoryManager().getRegister(compiler);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), strideReg));
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), idxReg));
        // On boucle sur les indices des dimensions en sens inverse (du plus interne au plus externe)
        for (int i = numDimensions - 1; i >= 0; i--) {
            AbstractExpr indexExpr = listIndex.get(i);
            this.listVar.getExpDefinition().getTaille().get(i).codeGenInst(compiler);
            indexExpr.codeGenInst(compiler);
            GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);
            GPRegister regInter = compiler.getMemoryManager().getLastRegister(compiler);
            //compiler.addInstruction(new CMP(reg, regInter));
            //compiler.addInstruction(new BLE(compiler.getIndexProblem()));
            //compiler.addInstruction(new CMP(new ImmediateInteger(0), regInter));
            //compiler.addInstruction(new BLT(compiler.getNegativeIndexProblem()));
            compiler.addInstruction(new MUL(strideReg, reg));
            compiler.addInstruction(new ADD(reg, idxReg));
            compiler.addInstruction(new MUL(regInter, strideReg));
        }
        // Free the registers used for calculation
        compiler.getMemoryManager().markFreeRegister(strideReg.getNumber());



        Label begin = compiler.getLabelManager().getLabel("beginWhile");
        Label end = compiler.getLabelManager().getLabel("endWhile");
        compiler.addLabel(begin);
        compiler.addInstruction(new CMP(new ImmediateInteger(0), idxReg));
        compiler.addInstruction(new BEQ(end));
        compiler.addInstruction(new LEA(new RegisterOffset(1, finalReg), finalReg));
        compiler.addInstruction(new SUB(new ImmediateInteger(1), idxReg));
        compiler.addInstruction(new BRA(begin));
        compiler.addLabel(end);
        // Charge la valeur à l'adresse linéaire calculée

        if (getAddress) {
            compiler.addInstruction(new LOAD(finalReg, compiler.getMemoryManager().getRegister(compiler)));
        } else {

            compiler.addInstruction(new LOAD(new RegisterOffset(0, finalReg), compiler.getMemoryManager().getRegister(compiler)));
        }

        compiler.getMemoryManager().markFreeRegister(finalReg.getNumber());
        compiler.getMemoryManager().markFreeRegister(idxReg.getNumber());

    }


    @Override
    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        this.codeGenInst(compiler);
        compiler.addInstruction(new CMP(1, compiler.getMemoryManager().getLastRegister(compiler)));
        if (cond) {
            compiler.addInstruction(new BEQ(label));
        }

        if (!cond) {
            compiler.addInstruction(new BNE(label));
        }
    }


}
