package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.CLIException;

public abstract class AbstractMethodBody extends Tree{
    

    /**
     * Cette méthode retourne true ou false sa dépend du type de le abstractMethod
     * @return
     * 
     */
    public boolean isSimpleMethod(){
        return false;
    }

    /**
     * Cette méthode génée le code de la méthode Body.
     * @param compiler
     * @throws CLIException
     * 
     */
    protected abstract void codegenMethBody(DecacCompiler compiler) throws CLIException;
}
