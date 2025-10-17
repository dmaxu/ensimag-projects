package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.log4j.Logger;

import static fr.ensimag.ima.pseudocode.Register.*;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        // A FAIRE : ce commentaire nous permet de retourner à cette méthode au cas d'un probléme (grep -r "A FAIRE")
        // Lors de la passe 1, on vérifie le nom des classes et la hiérarchie de classes.
        LOG.debug("verify listClass: start");
        //  on traverse tous les classes définies , et on vérifie si elles sont bien définies.
        for (AbstractDeclClass classes : getList()) {
            classes.verifyClass(compiler);
        }
        LOG.debug("verify listClass: end");
        // la deuxiéme passe
        verifyListClassMembers(compiler);
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        // A FAIRE : ce commentaire nous permet de retourner à cette méthode au cas d'un probléme (grep -r "A FAIRE")
        //  on traverse tous les classes définies , et on vérifie si elles sont bien définies.
        // Lors de la passe 2, on vérifie les champs et la signature des méthodes des différentes classes.
        for (AbstractDeclClass classes : getList()) {
            classes.verifyClassMembers(compiler);
        }
        // la troisiéme passe
        verifyListClassBody(compiler);
    }

    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        // A FAIRE : ce commentaire nous permet de retourner à cette méthode au cas d'un probléme (grep -r "A FAIRE")
        // On traverse tous les classes définies , et on vérifie si elles sont bien définies.
        // Lors de la passe 3, on vérifie les blocs, les instructions, les expressions et les initialisations.
        for (AbstractDeclClass classes : getList()) {
            classes.verifyClassBody(compiler);
        }
    }

    public void genVTable(DecacCompiler compiler) {
        compiler.addComment(" --------------------------------------------------");
        compiler.addComment("       Construction des tables des methodes        ");
        compiler.addComment(" --------------------------------------------------");

        //Generating table for Object class
        compiler.addComment("Construction de la table des methodes de Object");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), Register.GB)));
        compiler.getMemoryManager().incrementGlobalVar();
        Label label = new Label("code.Object.equals");
        LabelOperand labelOperand = new LabelOperand(label);
        compiler.addInstruction(new LOAD(labelOperand, Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), Register.GB)));
        compiler.getMemoryManager().incrementGlobalVar();
        compiler.getMemoryManager().setLastTablePos(1);

        //Generating table for other classes
        for (AbstractDeclClass decl : getList()) {
            decl.genClassTable(compiler, labelOperand);
        }

    }

    public void codegenClasses(DecacCompiler compiler) throws CLIException {
        for (AbstractDeclClass decl : getList()) {
            decl.codegenClass(compiler);
        }
    }

    protected void codeGenObject(DecacCompiler compiler){
        compiler.addComment("-----------------------------------------------");
        compiler.addComment("                   Classe Object               ");
        compiler.addComment("-----------------------------------------------");
        compiler.addLabel(new Label("code.object.equals"));
        DAddr thisAddr = new RegisterOffset(-2 , LB);
        DAddr toCprAdr = new RegisterOffset(-3 , LB);
        compiler.addInstruction(new LOAD(thisAddr, R0));
        compiler.addInstruction(new LOAD(new RegisterOffset(0,R0), R0));
        compiler.addInstruction(new LOAD(toCprAdr, R1));
        compiler.addInstruction(new LOAD(new RegisterOffset(0,R1), R1));
        compiler.addInstruction(new CMP(R0, R1));
        compiler.addInstruction(new SEQ(R0));
        compiler.addInstruction(new RTS());
    }


}
