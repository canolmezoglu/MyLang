package ut.pp.tests.checker;

import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;

import java.util.ArrayList;

import static ut.pp.tests.checker.TestBasicTypes.getParseTree;

public class TestIfWhile {
    private final Checker checker = new Checker();

    /**
     * Test if a correct use of an if
     * statement is accepted.
     * @throws Exception
     */
    @Test
    public void test_If1() throws Exception{
        checker.check(
                getParseTree("if (false or true){" +
                        "   print(1);" +
                        "}")
        );

        Assert.assertEquals(0,checker.getErrors().size());

    }
    /**
     * Test if an correct array
     * type  used as condition in an if
     * statement is rejected.
     * @throws Exception
     */
    @Test
    public void test_If4() throws Exception{
            checker.check(
                    getParseTree(
                            "bool arr[2] = {true,false};" +
                                    "if ( arr%1 ){" +
                                    "   print(1);" +
                                    "}")
            );
            Assert.assertEquals(0,checker.getErrors().size());
    }
    /**
     * Test if an incorrect type in used
     * as condition in an if
     * statement is rejected.
     * @throws Exception
     */
    @Test
    public void test_If2() throws Exception{
        try {
            checker.check(
                    getParseTree("if (5+4){" +
                            "   print(1);" +
                            "}")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("Error: if statement can only check a boolean at Line: 1 Character: 0",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if an incorrect array
     * type  used as condition in an if
     * statement is rejected.
     * @throws Exception
     */
    @Test
    public void test_If3() throws Exception{
        try {
            checker.check(
                    getParseTree(
                            "int arr[2] = {1,2};" +
                            "if ( arr%1 ){" +
                            "   print(1);" +
                            "}")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("Error: if statement can only check a boolean at Line: 1 Character: 19",
                    checker.getErrors().get(0));
        }
        }
    /**
     * Test if a correct use of an while
     * statement is accepted.
     * @throws Exception
     */
    @Test
    public void test_While1() throws Exception{
        checker.check(
                getParseTree("while (false or true){" +
                        "   print(1);" +
                        "}")
        );

        Assert.assertEquals(0,checker.getErrors().size());

    }
    /**
     * Test if an correct array
     * type used as condition in a while
     * statement is rejected.
     * @throws Exception
     */
    @Test
    public void test_While4() throws Exception{
        checker.check(
                getParseTree(
                        "bool arr[2] = {true,false};" +
                                "while ( arr%1 ){" +
                                "   print(1);" +
                                "}")
        );
        Assert.assertEquals(0,checker.getErrors().size());
    }
    /**
     * Test if an incorrect type in used
     * as condition in an while
     * statement is rejected.
     * @throws Exception
     */
    @Test
    public void test_While2() throws Exception{
        try {
            checker.check(
                    getParseTree("while (5+4){" +
                            "   print(1);" +
                            "}")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("Error: while statement can only check a boolean at Line: 1 Character: 0",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if an incorrect array
     * type  used as condition in an while
     * statement is rejected.
     * @throws Exception
     */
    @Test
    public void test_While3() throws Exception{
        try {
            checker.check(
                    getParseTree(
                            "int arr[2] = {1,2};" +
                                    "while ( arr%1 ){" +
                                    "   print(1);" +
                                    "}")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("Error: while statement can only check a boolean at Line: 1 Character: 19",
                    checker.getErrors().get(0));
        }
    }
}


