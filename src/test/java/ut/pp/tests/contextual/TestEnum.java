package ut.pp.tests.contextual;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;

import static ut.pp.tests.contextual.TestSimpleExpr.getParseTree;

public class TestEnum {

    private Scanner scanner = new Scanner();
    /**
     * Test if the program refuses enums having a
     * different type definition and different types
     * assigned.
     */
    @Test
    public void test_Enum1() throws Exception {
        try {
            ParseTree tree = getParseTree("bool enum fruits {\n" +
                    "        apple = 10,\n" +
                    "        banana = 25,\n" +
                    "        orange =39\n" +
                    "    };");
            scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(3, scanner.getErrors().size());
            Assert.assertEquals("Error: you are trying to assign an invalid type to a boolean enum at Line: 1 Character: 0", scanner.getErrors().get(0));
            Assert.assertEquals("Error: you are trying to assign an invalid type to a boolean enum at Line: 1 Character: 0", scanner.getErrors().get(1));
            Assert.assertEquals("Error: you are trying to assign an invalid type to a boolean enum at Line: 1 Character: 0", scanner.getErrors().get(2));
        }
    }
    /**
     * Test if the program accepts enums defined
     * correctly.
     */
    @Test
    public void test_Enum2() throws Exception {
        ParseTree tree = getParseTree("int enum fruits {\n" +
                "        apple = 10,\n" +
                "        banana = 25,\n" +
                "        orange =39\n" +
                "    };");
        scanner.check(tree);
        Assert.assertEquals(0, scanner.getErrors().size());
    }
}
