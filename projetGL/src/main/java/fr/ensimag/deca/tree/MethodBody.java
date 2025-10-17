package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.ListDeclVar;
import fr.ensimag.deca.tree.ListInst;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

public class MethodBody extends AbstractMethodBody{
    private static final Logger LOG = Logger.getLogger(Main.class);
    private ListDeclVar listVar;
    private ListInst listInst;

    public MethodBody(ListDeclVar listVar, ListInst listInst){
        this.listVar = listVar;
        this.listInst = listInst;
    }

    @Override
    public boolean isSimpleMethod(){
        return true;
    }

    public void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv ,
    ClassDefinition classDefinition, Type typeMethod) throws ContextualError{
        LOG.debug("verifyMethodBody: start");

        // Vérification des déclarations des variables
        try{
            this.listVar.verifyListDeclVariable(compiler, localEnv, classDefinition);
        }catch(DoubleDefException e){
            throw new ContextualError("Une variable a été définie plusieurs fois. " , listVar.getLocation());
        }
        // Vérification des instructions
        this.listInst.verifyListInst(compiler, localEnv, classDefinition, typeMethod);
        LOG.debug("verifyMethodBody: end");

    }

    public ListDeclVar getListDeclVar(){
        return this.listVar;
    }

    public ListInst getListInst(){
        return this.listInst;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // A FAIRE:
        listVar.decompile(s);
        listInst.decompile(s);

    }
    @Override
    protected void iterChildren(TreeFunction f) {
        // A FAIRE :
        this.listVar.iter(f);
        this.listInst.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (listVar != null) {
            listVar.prettyPrint(s, prefix, false);
        }
        if (listInst != null) {
            listInst.prettyPrint(s, prefix, true);
        }
    }

    protected void codegenMethBody(DecacCompiler compiler) throws CLIException {
        LOG.debug("codegenMethBody: start");
        listVar.codeGenDeclListVarLocal(compiler);
        listInst.codeGenListInst(compiler);
        LOG.debug("codegenMethBody: end");
    }

}
