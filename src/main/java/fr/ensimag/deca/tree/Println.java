package fr.ensimag.deca.tree;


import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.WNL;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Println extends AbstractPrint {
    private static final Logger LOG = Logger.getLogger(Main.class);

    /**
     * @param arguments arguments passed to the print(...) statement.
     * @param printHex if true, then float should be displayed as hexadecimal (printlnx)
     */
    public Println(boolean printHex, ListExpr arguments) {
        super(printHex, arguments);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst Println: start");
        super.codeGenInst(compiler);
        compiler.addInstruction(new WNL());
        LOG.debug("codeGenInst Println: end");

    }

    @Override
    String getSuffix() {
        return "ln";
    }
}
