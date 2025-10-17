package fr.ensimag.deca.tree;


import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public class Lower extends AbstractOpIneq {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<";
    }


    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenCond Lower: start");
        GPRegister regRight;
        DVal adresse;
        // Disjonction des cas pour optimiser les calcules
        if (getRightOperand().isImmediate()){
            // Évaluer la première sous-expression
            getLeftOperand().codeGenInst(compiler);
            // On retourne le register
            regRight = compiler.getMemoryManager().getLastRegister(compiler);
            // Vérification
            if (getRightOperand().getType().isInt()){
                adresse = ((IntLiteral) getRightOperand()).getAdresse();
            }else{
                adresse = ((FloatLiteral) getRightOperand()).getAdresse();
            }
        }else if(getLeftOperand().isImmediate()){
            cond=!cond;
            // Évaluer la première sous-expression
            getLeftOperand().codeGenInst(compiler);
            // On retourne le register
            regRight = compiler.getMemoryManager().getLastRegister(compiler);
            // Vérification
            if (getLeftOperand().getType().isInt()){
                adresse = ((IntLiteral) getLeftOperand() ).getAdresse();
            }else{
                adresse = ((FloatLiteral) getLeftOperand() ).getAdresse();
            }
        }
        else{
            // Évaluer la première sous-expression
            getLeftOperand().codeGenInst(compiler);
            // Évaluter la deuxiéme expression
            getRightOperand().codeGenInst(compiler);
            adresse = compiler.getMemoryManager().getLastRegister(compiler);
            regRight = compiler.getMemoryManager().getLastRegister(compiler);
        }
        compiler.addInstruction(new CMP(adresse, regRight));
        if (cond) {
            compiler.addInstruction(new BLT(label));
        } else {
            compiler.addInstruction((new BGE(label)));
        }
        LOG.debug("codeGenCond Lower: end");
    }

//    // Case where it's an assign and we need to do boolean algebra (not a condition)
//    @Override
//    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
//        // Évaluer la première sous-expression
//        getLeftOperand().codeGenInst(compiler);
//
//
//        // Évaluer la deuxième sous-expression
//        getRightOperand().codeGenInst(compiler);
//
//
//        GPRegister regLeft = compiler.getMemoryManager().getLastRegister(compiler);
//        GPRegister regRight = compiler.getMemoryManager().getLastRegister(compiler);
//        compiler.addInstruction(new CMP(regLeft, regRight));
//        compiler.addInstruction(new SLT(compiler.getMemoryManager().getRegister(compiler)));
//
//    }

}
