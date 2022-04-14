package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;
import ut.pp.tests.TestChecker;
import ut.pp.Main.*;
public class TestSemantic {

    @Test
    public void test1() throws Exception {
        String input = "banking";
        Assert.assertEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input));
    }

}
