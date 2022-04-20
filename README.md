# PP Final Project:

## Prerequisites

Make sure you have installed:

- Maven. Preferably version 3.8 or later.
- A Java version. Preferably 11 or 17.

To test if you have these tools set up properly, run the following two commands in the command line:

- For maven: `mvn --version`
- For java: `javac --version`

If these commands print sensible outputs (e.g. "version so and so"), this boilerplate project should work fine. Otherwise, you will have to install these tools either manually or using your operating system's package manager.

## Compiling and Running all tests

Before compiling, In the terminal run:

```
mvn clean install
```

This is used because a ParseTree Visitor has been used for Code Generation. 
The command will compile the program and run all the tests.

## Compiling

In a terminal, run:
```
mvn compile
```
This first generates parsers for any grammars in `src/main/antlr4`. Then it compiles code in the `src/main/java` directory.

## Running
In a terminal, run:

```
mvn exec:java -Dexec.mainClass="ut.pp.Main"
```

This runs the `main` method of the class `Main`, located in the `ut.pp` package.

Note: `exec` does not invoke `compile`, so if changes were made to the code, `compile` needs to be invoked before `exec`. These commands can also be combined:

```
mvn compile exec:java -Dexec.mainClass="ut.pp.Main"
```

`mvn exec:java` is part of the maven-exec plugin. For more information about e.g. passing arguments to the main class, we refer you to the plugin documentation.

## Tests

In a terminal, run:

```
mvn test
```

This will run all test classes in the `src/test/java` directory which have names starting with "Test".

## Intellij Integration

The project can be imported into intellij by opening the `pom.xml` file in intellij. Then on the right there will be a "Maven" pane. If the "Maven" pane is not there, you might have opened the project directory; be sure to open _specifically the file_ with intellij.

Open the "Maven" pane, and open the tree structure there until you find the "Lifecycle" section. Here you can double-click "compile" and "test" to compile and test your code.

To run your main class, open the main class and click on the green arrow besides the main method. Note: you have to run the "compile" target in the maven pane before running the main class.

To run your a specific test, open the test class and click the green arrows beside the test methods to run the individual tests. Note: you have to run the "test" target in the maven pane before running the individual tests.

For more information on intellij's maven integration, we refer you to the intellij documentation.

##Using the compiler

To use the compiler, you can write code in the `src/sample/compiler.txt` which is run by the `Main` class.

If the code contains any errors, they are thrown as an exception and the error messages can be seen, otherwise the generated sprockell code can 
be found inside `ut/pp/compiler/haskell/output.hs` and the output can be seen in the Terminal.