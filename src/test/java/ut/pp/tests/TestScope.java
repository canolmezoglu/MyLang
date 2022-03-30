package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class TestScope {

    public ParseTree getTree(String input){
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.instruction();
        return tree;
    }

    @Test
    public void test1() {
        Checker c=new Checker();
        String input = "int wait=100;int money=120; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";
        assertEquals(0,c.checkScope(getTree(input)).size());
    }
    @Test
    public void test2() {
        Checker c=new Checker();
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";
        assertEquals(1,c.checkScope(getTree(input)).size());
    }
    @Test
    public void test3() {
        Checker c=new Checker();
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        int c=0;\n" +
                "    } c=100;" ;
        assertEquals(1,c.checkScope(getTree(input)).size());
    }
    @Test
    public void test4() {
        Checker c=new Checker();
        String input = "int numberofiterations = 100;\n" +
                "\n" +
                "while (numberofiterations > 0) {\n" +
                "\n" +
                "  numberofiterations = numberofiterations - 1;\n" +
                "  bool numberofiterations = false;\n" +
                "\n" +
                "}";
        assertEquals(0,c.checkScope(getTree(input)).size());
    }


}
