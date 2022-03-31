package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;
import ut.pp.tests.TestChecker;

public class TestScope {

    Checker c = new Checker();

    @Test
    public void test1() throws Exception {
        String input = "int wait= 100;int money=120; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";

        c.check(TestChecker.getParseTree(input));
        assertEquals(0,c.getScopeErrors().size());
    }
    @Test
    public void test2() throws Exception {
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";
        try {
            c.check(TestChecker.getParseTree(input));
        }
        catch ( Exception e) {
            System.out.println(e.getMessage());

            assertEquals(1, c.getScopeErrors().size());

        }
    }
    @Test
    public void test3() throws Exception {
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        int c=0;\n" +
                "    } c=100;" ;
        try {
            c.check(TestChecker.getParseTree(input));
        }
        catch ( Exception e) {
            System.out.println(e.getMessage());
            assertEquals(1, c.getScopeErrors().size());

        }
    }
    @Test
    public void test4() throws Exception {
        String input = "int numberofiterations = 100;\n" +
                "\n" +
                "while (numberofiterations > 0) {\n" +
                "\n" +
                "  numberofiterations = numberofiterations - 1;\n" +
                "  bool numberofiterations = false;\n" +
                "\n" +
                "}";
        c.check(TestChecker.getParseTree(input));
        assertEquals(0,c.getScopeErrors().size());
    }


}
