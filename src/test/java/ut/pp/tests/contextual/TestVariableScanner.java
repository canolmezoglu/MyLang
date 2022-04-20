package ut.pp.tests.contextual;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class TestVariableScanner {
    final Scanner scanner = new Scanner();

    public static ParseTree getParseTree(String code) {
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        return tree;
    }
    /**
     * Test if change assignment accepts the correct type
     * with integers.
     */
    @Test
    public void test_changeAss1() throws Exception{
            scanner.check(
                    getParseTree(" int a = 5; a = 16; ")
            );
    }

    /**
     * Test if change assignment accepts the correct type
     * with booleans.
     */
    @Test
    public void test_changeAss2() throws Exception{
            scanner.check(
                    getParseTree(" bool a = true; a = false; ")
            );

            Assert.assertEquals(0, scanner.getErrors().size());

    }
    /**
     * Test if change assignment rejects the incorrect type.
     */
    @Test
    public void test_changeAss3() {
        try {
            scanner.check(
                    getParseTree(" bool a = true; a = 12; ")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test regular integer declaration
     */
    @Test
    public void test_declStat() throws Exception {
        scanner.check (
                getParseTree("int fib = 5 ; ")
        );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test regular boolean declaration
     */
    @Test
    public void test_declStat2() throws Exception {
        scanner.check (
                getParseTree("bool a = true ; ")
        );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test declaring without an expression
     * is rejected.
     */
    @Test
    public void test_declStat3() throws Exception {
        try {
            scanner.check(
                    getParseTree("bool a; ")
            );
            Assert.fail();
        }catch (Exception e){
            Assert.assertEquals(4, scanner.getErrors().size());
        }
        }





}
