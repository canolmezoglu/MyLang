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

import java.util.List;

public class TestSemantic {

    @Test
    public void test1() throws Exception {
        String input = "banking";
        Assert.assertEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void test2() throws Exception {
        String input = "peterson";
        Assert.assertEquals("Sprockell 0 says 3",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void test3() throws Exception {
        String input = "nestedConcurrency";
        Assert.assertEquals("Sprockell 0 says -87",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void test4() throws Exception {
        String input = "bankingNoLocks";
        // TODO write something logical here
        Assert.assertNotEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void test5() throws Exception {
        String input = "threadRunningOrder";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 0",output.get(0));
        Assert.assertEquals("Sprockell 1 says 1",output.get(1));
        Assert.assertEquals("Sprockell 2 says 2",output.get(2));
        Assert.assertEquals("Sprockell 0 says 3",output.get(3));
        Assert.assertEquals("Sprockell 3 says 4",output.get(4));
        Assert.assertEquals("Sprockell 4 says 5",output.get(5));
        Assert.assertEquals("Sprockell 0 says 6",output.get(6));


    }


}