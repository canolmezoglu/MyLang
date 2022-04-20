package ut.pp.tests.syntax_test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class TestSyntax {
    final Scanner c =new Scanner();

    public ParseTree getParseTree(String input){
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        return tree;
    }

    @Test
    public void testBasic() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("int a =100"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("int a =100;");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void test1dArray() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("int b[6] =  1,2,30,47,6,55;\n" +
                    "a%1 = a%2+a%0;\n" +
                    "print(a%1);"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("int b[6] =  {1,2,30,47,6,55};\n" +
                    "a%1 = a%2+a%0;\n" +
                    "print(a%1);");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void test2dArray() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("int a =100"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("int a =100;");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testPointer() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("int d = 200;\n" +
                    "pointer c = {1,2,3,4};"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("int d = 200;\n" +
                    "pointer c = d;");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testEnum() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("int enum {\n" +
                    "apple = 10,\n" +
                    "banana = 25,\n" +
                    "orange =39\n" +
                    "};\n" +
                    "\n" +
                    "print(orange);"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("int enum fruits {\n" +
                    "apple = 10,\n" +
                    "banana = 25,\n" +
                    "orange =39\n" +
                    "};\n" +
                    "\n" +
                    "print(fruits.orange);");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testIfElse() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("if  true { print(fine);} else {print(0);}"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("if ( not belowSpeedLimit(100,101) ) { print(fine);} else {print(0);}");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testWhile() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("int a = 100;\n" +
                    "while (a > 0):\n" +
                    "    a = a - 1;\n" +
                    "    print(a);\n" +
                    ""));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("int a = 100;\n" +
                    "while (a > 0){\n" +
                    "    a = a - 1;\n" +
                    "    print(a);\n" +
                    "}");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testLocks() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("shared int i = 65;\n" +
                    "lock {\n" +
                    "  lock {\n" +
                    "        i = 77;\n" +
                    "        print(i);\n" +
                    "  }\n" +
                    "} unlock"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("shared int i = 65;\n" +
                    "lock {\n" +
                    "  lock {\n" +
                    "        i = 77;\n" +
                    "        print(i);\n" +
                    "  } unlock\n" +
                    "} unlock");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testParallel() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("parallel {\n" +
                    "thread {   int wait = 100; while (wait > 0){\n" +
                    "        wait = wait - 1;\n" +
                    "        money = money + 1;\n" +
                    "    }\n" +
                    "}\n" +
                    "thread {\n" +
                    "    int wait = 100; while (wait > 0){\n" +
                    "        wait = wait - 1;\n" +
                    "        money = money - 2;\n" +
                    "    }\n" +
                    "}"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("parallel {\n" +
                    "thread {   int wait = 100; while (wait > 0){\n" +
                    "        wait = wait - 1;\n" +
                    "        money = money + 1;\n" +
                    "    }\n" +
                    "}\n" +
                    "thread {\n" +
                    "    int wait = 100; while (wait > 0){\n" +
                    "        wait = wait - 1;\n" +
                    "        money = money - 2;\n" +
                    "    }\n" +
                    "};");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testThread() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("thred {\n" +
                    "    int wait = 100; while (wait > 0){\n" +
                    "        wait = wait - 1;\n" +
                    "        lock {\n" +
                    "            money = money - 2;\n" +
                    "            } unlock\n" +
                    "    }\n" +
                    " }"));
        });
        Assert.assertTrue(exception.getMessage().contains("SyntaxError"));

        try{
            getParseTree("thread {\n" +
                    "    int wait = 100; while (wait > 0){\n" +
                    "        wait = wait - 1;\n" +
                    "        lock {\n" +
                    "            money = money - 2;\n" +
                    "            } unlock\n" +
                    "    }\n" +
                    "}");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
    @Test
    public void testFunction() throws Exception {
        Exception exception = Assert.assertThrows(Exception.class, () -> {
            c.check(getParseTree("function int ident() \n" +
                    "    if (a==b){\n" +
                    "        return a;\n" +
                    "    }\n" +
                    "    if (a > b){\n" +
                    "       return gcd( a - b, b);\n" +
                    "    }\n" +
                    "    return gcd(a, b -a);\n" +
                    "}\n" +
                    "print(gcd(1 , 2 ));\n"));
        });

        try{
            getParseTree("function gcd(int a, int b {\n" +
                    "    if (a==b){\n" +
                    "        return a;\n" +
                    "    }\n" +
                    "    if (a > b){\n" +
                    "       return gcd( a - b, b);\n" +
                    "    }\n" +
                    "    return gcd(a, b -a);\n" +
                    "}\n" +
                    "print(gcd(1 , 2 ));\n");
        }
        catch (Exception e){
            Assert.fail();
        }
    }
}
