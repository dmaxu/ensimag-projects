package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;

import java.util.List;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl47
 * @date 01/01/2025
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        List<AbstractExpr> exprList = getList();
        for (int i = 0; i < exprList.size(); i++) {
            if (i > 0) {
                s.print(", ");
            }
            exprList.get(i).decompile(s);
        }
    }
}
