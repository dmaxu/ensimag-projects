package fr.ensimag.deca.tree;


import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;

/**
 * read...() statement.
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class AbstractReadExpr extends AbstractExpr {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public AbstractReadExpr() {
        super();
    }



    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("CodeGen Read Inst: start");

        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(compiler.getReadError()));
        }
        LOG.debug("CodeGen Read Inst: end");

    }

}
