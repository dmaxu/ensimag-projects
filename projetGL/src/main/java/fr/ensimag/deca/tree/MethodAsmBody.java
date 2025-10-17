package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.StringLiteral;
import fr.ensimag.ima.pseudocode.InlinePortion;

import java.io.PrintStream;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class MethodAsmBody extends AbstractMethodBody{
    private StringLiteral mutiLineString;
    private static final Logger LOG = Logger.getLogger(Main.class);

    public MethodAsmBody(StringLiteral mutiLineString){
        this.mutiLineString = mutiLineString;
    }

    @Override
    public void decompile(IndentPrintStream s){
        s.print("ASM(");
        this.mutiLineString.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f){
        this.mutiLineString.iterChildren(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix){
        this.mutiLineString.prettyPrintChildren(s, prefix);
    }

    protected void codegenMethBody(DecacCompiler compiler) throws CLIException {
        LOG.debug("codegenMethBody: start");

        // Récupérer le code assembleur contenu dans le StringLiteral
        String asmCode = mutiLineString.getValue().replace("\\\"", "\""); // Récupère "ok" sans guillemets
    
        // Compiler ce code en utilisant le compilateur (ou l'ajouter directement à la sortie)
        compiler.add(new InlinePortion(asmCode));
        LOG.debug("codegenMethBody: end");

    }
}
