package ut.pp.tests.contextual;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;
import ut.pp.compiler.model.enums.MyType;
import ut.pp.compiler.Result;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class TestBasicTypes {
    final Scanner scanner = new Scanner();

    public static ParseTree getParseTree(String code) {
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        return tree;
    }

    /**
     * Test if type is assigned correctly for booleans
     */
    @Test
    public void test_boolean1() throws Exception {
        ParseTree tree = getParseTree("print(not true);");
        Result result = scanner.check(tree);
        ParseTree expr = tree.getChild(0).getChild(0).getChild(2);
        Assert.assertEquals(result.getType(expr), MyType.BOOLEAN);
        Assert.assertEquals(0, scanner.getErrors().size());
    }

    /**
     * Test if type is assigned correctly for integers
     */
    @Test
    public void test_integer1() throws Exception {
        ParseTree tree = getParseTree("print(5);");
        Result result = scanner.check(tree);
        ParseTree expr = tree.getChild(0).getChild(0).getChild(2);
        Assert.assertEquals(result.getType(expr), MyType.NUM);
        Assert.assertEquals(0, scanner.getErrors().size());
    }

    /**
     * Test if numbers higher than expected size are handled correctly
     */
    @Test
    public void test_integer2() throws Exception {
        ParseTree tree = getParseTree("print(-2147483648);");
        try {
            scanner.check(
                    tree);
            Assert.fail();

        } catch (Exception e) {
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Error: This int defined is larger than the limits at Line: 1 Character: 7"
            , e.getMessage());
        }


    }

    /**
     * Test if numbers higher than expected size are handled correctly
     */
    @Test
    public void test_integer3() throws Exception {
        ParseTree tree = getParseTree("print(2147483648);");
        try {
            scanner.check(
                    tree);
            Assert.fail();

        } catch (Exception e) {
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Error: This int defined is larger than the limits at Line: 1 Character: 6",
                    e.getMessage());
        }
    }
    /**
     * Test if operator precedence
     * parses the and first, thereby leading
     * the program to be rejected
     */
    @Test
    public void test_boolean2() {
        try {
            scanner.check(
                    getParseTree("print( true and 5 > 6 );" )
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("AND has type mismatch at Line: 1 Character: 7",
                    scanner.getErrors().get(0));
        }
    }
}
