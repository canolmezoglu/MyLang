package ut.pp.tests.checker;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class VariableChecker {
    final Checker checker = new Checker();

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
        try {
            checker.check(
                    getParseTree(" int a = 5; a = 16; ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }

    /**
     * Test if change assignment accepts the correct type
     * with booleans.
     */
    @Test
    public void test_changeAss2() throws Exception{
        try {
            checker.check(
                    getParseTree(" bool a = true; a = false; ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }
    /**
     * Test if change assignment rejects the incorrect type.
     */
    @Test
    public void test_changeAss3() throws Exception{
        try {
            checker.check(
                    getParseTree(" bool a = true; a = 12; ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test regular integer assignment
     */
    @Test
    public void test_assStat1() throws Exception {
        checker.check (
                getParseTree("int fib = 5 ; ")
        );
        Assert.assertEquals(0,checker.getErrors().size());
    }




}
