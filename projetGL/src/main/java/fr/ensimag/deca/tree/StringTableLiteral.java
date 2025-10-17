package fr.ensimag.deca.tree;

import java.util.List;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.StringTypeTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;

/**
 * Integer literal
 *
 * @author gl47
 * @date 01/01/2025
 */
public class StringTableLiteral extends TableLiteral {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private List<StringLiteral> listLiteral = new ArrayList<>();
    private List<String> listValue = new ArrayList<>();

    public void addLiteral(StringLiteral value){
        this.listLiteral.add(value);
        this.listValue.add(value.getValue());
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr StringTableLiteral: start");

                // ON vérifie tous les Nombres à l'intérieur
                for (StringLiteral element : listLiteral){
                    // On vérifie le type de l'expression
                    element.verifyExpr(compiler, localEnv, currentClass);
                }
                Symbol typeInSymbol;
                if (getTaille().size() > 1){
                    typeInSymbol = compiler.createSymbol(getTaille().size()-1 + "D string");
                }else{
                    typeInSymbol = compiler.createSymbol("string");
                }
                // On construit le type string.
                StringTypeTable nombreTypeList = new StringTypeTable(compiler.symbolTable.create(getTaille().size() + "D string"), typeInSymbol);
                // On preserve le type de l'expression.
                setType(nombreTypeList);
                LOG.debug("verifyExpr StringTableLiteral: end");
                // On retourne le type
                return nombreTypeList;
    }

    @Override
    String prettyPrintNode() {
        return "TableString [taille=" + getTaille() + "]";
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
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        
    }
}