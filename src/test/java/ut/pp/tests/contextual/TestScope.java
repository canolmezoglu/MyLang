package ut.pp.tests.contextual;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;

public class TestScope {

    Scanner c = new Scanner();

    /**
     * Check if a normally constructed statement with
     * nested scopes is accepted.
     * @throws Exception
     */
    @Test
    public void scope_test_1() throws Exception {
        String input = "int wait= 100;int money=120; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";

        c.check(TestSimpleExpr.getParseTree(input));
        assertEquals(0,c.getScopeErrors().size());
    }

    /**
     * Check if the compiler will reject the use of
     * a variable before it is initialized, namely
     * the variable money.
     * @throws Exception
     */
    @Test
    public void scope_test_2()  throws Exception {
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";
        try {
            c.check(TestSimpleExpr.getParseTree(input));
            Assert.fail();
        }
        catch ( Exception e) {
            assertEquals(2, c.getScopeErrors().size());
            assertTrue(c.getScopeErrors().contains("Error:money not declared in this scope at Line: 3 Character: 8"));

        }
    }
    /**
     * Test if the program will reject access to a outer scope
     * that was exited.
     * @throws Exception
     */
    @Test
    public void scope_test_3()  throws Exception {
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        int c=0;\n" +
                "    } c=100;" ;
        try {
            c.check(TestSimpleExpr.getParseTree(input));
        }
        catch ( Exception e) {
            assertEquals(1, c.getScopeErrors().size());

        }
    }

    /**
     * Checks if the compiler will accept a
     * program that reuses variable names in nested
     * scopes.
     * @throws Exception
     */
    @Test
    public void scope_test_4()  throws Exception {
        String input = "int numberofiterations = 100;\n" +
                "\n" +
                "while (numberofiterations > 0) {\n" +
                "\n" +
                "  numberofiterations = numberofiterations - 1;\n" +
                "  bool numberofiterations = false;\n" +
                "\n" +
                "}";

        c.check(TestSimpleExpr.getParseTree(input));
        assertEquals(0,c.getScopeErrors().size());
    }
    /**
     * Check if the type of the variable
     * is fixed throughout the scope it is in
     * @throws Exception
     */
    @Test
    public void scope_test_5()  throws Exception {
        String input = "shared int numberofiterations = 100;\n" +
                "shared bool numberofiterations = true;";
        try {

            c.check(TestSimpleExpr.getParseTree(input));
            Assert.fail();

        }
        catch (Exception e) {
            assertEquals(1, c.getScopeErrors().size());
            assertTrue(c.getScopeErrors().contains("Error:numberofiterations already declared in this scope at Line 2 Character:0"));
        }
    }

    /**
     * Check if the type of the variable
     * is fixed throughout the scope it is in
     * for global variables.
     * @throws Exception
     */
    @Test
    public void scope_test_6()  throws Exception {
        String input = "shared int numberofiterations = 100;\n" +
                "shared bool numberofiterations = true;";
        try {

            c.check(TestSimpleExpr.getParseTree(input));
            Assert.fail();

        }
        catch (Exception e) {
            assertEquals(1, c.getScopeErrors().size());
            assertTrue(c.getScopeErrors().contains("Error:numberofiterations already declared in this scope at Line 2 Character:0"));
        }
    }
    /**
     * Check if a variable cannot be
     * called without initilization
     * @throws Exception
     */
    @Test
    public void scope_test_7()  throws Exception {
        String input = "bool test;\n" +
                "if(test){" +
                "print(6);" +
                "}";
        try {

            c.check(TestSimpleExpr.getParseTree(input));
            Assert.fail();

        }
        catch (Exception e) {
            assertEquals(2, c.getScopeErrors().size());
            assertEquals(c.getErrors().get(2),"Error: this variable you are changing does not exist at Line: 1 Character: 5");
        }
    }



}
