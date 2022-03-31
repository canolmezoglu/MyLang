package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class TestParser {
    @Test
    public void oneHello() {
        String input = "int wait=100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        assertEquals(3, tree.getChildCount()); // 1 for Hello, 1 for EOF
    }

    @Test
    public void helloNewlineHello() {
         String input = "int wait= 100;int money=120; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                "        money = money - 1;\n" +
                "    }";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        assertEquals(3, tree.getChildCount()); // 2 for Hello, 1 for EOF
    }
//
//    @Test
//    public void helloWorld() {
//        // Fails by design, "World" is not allowed
//        String input = "shared int turn = 0;";
//        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
//        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
//        MyLangParser parser = new MyLangParser(tokens);
//        ParseTree tree = parser.program();
//        assertEquals(6, tree.getChildCount());
//    }
}
