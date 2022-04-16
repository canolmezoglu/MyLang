package ut.pp.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.tests.checker.SimpleExpr;

import java.security.spec.ECField;

public class TestScope {

    Checker c = new Checker();

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

        c.check(SimpleExpr.getParseTree(input));
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
            c.check(SimpleExpr.getParseTree(input));
        }
        catch ( Exception e) {
            System.out.println(e.getMessage());

            assertEquals(1, c.getScopeErrors().size());
            assertTrue(c.getScopeErrors().contains("money not declared in this scope: 3"));

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
            c.check(SimpleExpr.getParseTree(input));
        }
        catch ( Exception e) {
            System.out.println(e.getMessage());
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

        c.check(SimpleExpr.getParseTree(input));
        assertEquals(0,c.getScopeErrors().size());
    }
    /**
     * Check if the type of the variable
     * is fixed throughout the scope it is in.
     * @throws Exception
     */
    @Test
    public void scope_test_5()  throws Exception {
        String input = "int numberofiterations = 100;\n" +
            "bool numberofiterations = true;";
        try {

            c.check(SimpleExpr.getParseTree(input));
        }
        catch (Exception e) {
            assertEquals(1, c.getScopeErrors().size());
            assertTrue(c.getScopeErrors().contains("numberofiterations already declared in this scope: 2"));
        }
    }


}
