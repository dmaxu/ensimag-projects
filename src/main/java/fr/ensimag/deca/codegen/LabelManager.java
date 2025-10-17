package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.DecacMain;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

import java.util.HashMap;

public class LabelManager {
    private final HashMap<String, Integer> labelNb = new HashMap<>();



    /**
     * Method that returns the number after to put after the label name to avoid having labels with the same name
     *
     * @param name
     */
    public Label getLabel(String name) {
        Label label;
        int nb;
        if (labelNb.containsKey(name)) {
            nb = labelNb.get(name);
            nb++;
        } else {
            label = new Label(name);
            nb = 0;
            labelNb.put(name, nb);
        }
        label = new Label(name + "." + nb);
        labelNb.replace(name, nb);
        return label;
    }


    /**
     * Adds an error label in the program
     *
     * @param label
     * @param compiler
     * @param message
     */
    public void addErrorLabel(Label label, DecacCompiler compiler, String message) {

        compiler.addLabel(label);
        compiler.addInstruction(new WSTR(message));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());
    }




}
