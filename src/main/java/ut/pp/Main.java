package ut.pp;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;

import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Programming Paradigms");

        String input = "// Line one\n"
            + "Hello\n"
            + "// Line two\n"
            + "Hello Hello\n";

        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.instruction();

        System.out.println("Children: " + tree.getChildCount() + ", parsed text: " + tree.getText());
    }
}
