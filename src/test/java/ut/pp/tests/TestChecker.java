package ut.pp.tests;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.elaboration.Result;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import static org.junit.Assert.assertEquals;

public class TestChecker {

    final Checker checker = new Checker();

    public ParseTree getParseTree(String code){
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.instruction();
        return tree;
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
