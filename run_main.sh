#!/bin/bash

# Compile the Main class
javac -d bin -classpath lib/gui.jar -sourcepath src src/Main.java

# Run the Main class with arguments
java -classpath bin:lib/gui.jar Main "$@"