package ut.pp.tests.contextual;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;

import static ut.pp.tests.contextual.TestSimpleExpr.getParseTree;

public class TestPointer {
    private Scanner scanner = new Scanner();
    /**
     * Test if the program accepts correctly
     * defined pointers.
     */
    @Test
    public void test_Pointer1() throws Exception {
        ParseTree tree = getParseTree("int d = 200;\n" + "pointer c = d;");
        scanner.check(tree);
        Assert.assertEquals(0, scanner.getErrors().size());
    }

    /**
     * Test if the program refuses incorrectly
     * defined pointers as pointers cannot
     * point to simple expressions.
     */
    @Test
    public void test_Pointer2() {
        try {
            ParseTree tree = getParseTree("pointer c = 17;");
            scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Error:Pointers cannot point to something different than variables.1 Character: 0", scanner.getErrors().get(0));
        }
    }
    /**
     * Test if the program refuses incorrectly
     * defined pointers as pointers cannot
     * point to undefined variables.
     */
    @Test
    public void test_Pointer3() {
        try {
            ParseTree tree = getParseTree("pointer c = a;");
            scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(3, scanner.getErrors().size());
            Assert.assertEquals("Error:Pointer is pointing to an undefined variable 1 Character: 0", scanner.getErrors().get(0));
            Assert.assertEquals("Error:a not declared in this scope at Line: 1 Character: 0", scanner.getErrors().get(1));
        }
    }
    /**
     * Test if the program does not allow
     * basic types to have the variable name as
     * pointers.
     */
    @Test
    public void test_Pointer4() {
        try {
            ParseTree tree =
                    getParseTree("bool a = true;" +
                            "pointer c = a ;" +
                            "int c = 44;");
            scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Error:c already declared in this scope at Line 1 Character:29", scanner.getErrors().get(0));
        }
    }
}
