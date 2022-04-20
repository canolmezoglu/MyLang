package ut.pp.tests.checker;

import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;

import static ut.pp.tests.checker.TestBasicTypes.getParseTree;

public class TestFunction {
    private final Checker checker = new Checker();
    /**
     * Test if the function calls infer type
     * correctly by testing to do sum
     * operation with a function that returns an int.
     */
    @Test
    public void test_Function1() throws Exception{
            checker.check(
                    getParseTree(" function int gcd(int a, int b) {\n" +
                            "    if (a==b){\n" +
                            "        return a;\n" +
                            "    }\n" +
                            "    if (a > b){\n" +
                            "       return gcd( a - b, b);\n" +
                            "    }\n" +
                            "    return gcd(a, b -a);\n" +
                            "}\n" +
                            "print (5 + gcd(5,4));")
            );

            Assert.assertEquals(0,checker.getErrors().size());

        }
    /**
     * Test if the compiler accepts a void function
     * that does not have a return.
     */
    @Test
    public void test_Function11() throws Exception {
        checker.check(
                    getParseTree(
                            "function void gcd(int x, int y) {\n" +
                                    "        if (y == 0) {\n" +
                                    "print(x);" +
                                    "        }\n" +
                                    "}\n"
                    ));
        Assert.assertEquals(0,checker.getErrors().size());


    }
    /**
     * Test if the function calls infer type
     * correctly by testing to do a boolean
     * operation with a function that returns an int.
     * In a correct test run, this should be refused.
     */
    @Test
    public void test_Function2() {
        try {
            checker.check(
                    getParseTree(" function int gcd(int a, int b) {\n" +
                            "    if (a==b){\n" +
                            "        return a;\n" +
                            "    }\n" +
                            "    if (a > b){\n" +
                            "       return gcd( a - b, b);\n" +
                            "    }\n" +
                            "    return gcd(a, b -a);\n" +
                            "}\n" +
                            "print (true and gcd(5,4));")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("AND has type mismatch at Line: 10 Character: 7",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if a the checker refuses a
     * void function to work as a normal
     * expression.
     */
    @Test
    public void test_Function3() {
        try {
            checker.check(
                    getParseTree(" function void printNum(int a) {\n" +
                                        "print(a);"+
                            "}\n" +
                            "print (printNum(4) * 55);")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("Multiplication has type mismatch at Line: 3 Character: 7",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses
     * a function which returns a different type
     * than what is written on the function definition.
     */
    @Test
    public void test_Function4() {
        try {
            checker.check(
                    getParseTree(" function int addFive(int a) {\n" +
                            "return true;"+
                            "}\n"
            ));
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals(
                    "Error: A function claims to return NUM but actually returns BOOLEAN at Line: 2 Character: 0",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses
     * a void function which returns an basic value.
     */
    @Test
    public void test_Function5() {
        try {
            checker.check(
                    getParseTree(" function void addFive(int a) {\n" +
                            "return true;"+
                            "}\n"
                    ));
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals(
                    "Error: A function that is void is trying to return a BOOLEAN at Line: 2 Character: 0",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses
     * a void function which returns an basic value.
     */
    @Test
    public void test_Function6() {
        try {
            checker.check(
                    getParseTree(" int a =5;" +
                            "return 17;"
                    ));
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertEquals("Error: a return statement is called outside a function at Line: 1 Character: 10",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses
     * a void function which returns an basic value.
     */
    @Test
    public void test_Function7() {
        try {
            checker.check(
                    getParseTree("function int modulo (int a, int b) {\n" +
                            "    if (b > a){\n" +
                            "        return a;\n" +
                            "    }\n" +
                            "    int c = a / b ;\n" +
                            "    return a - c * b;\n" +
                            "}\n" +
                            "\n" +
                            "function int gcd(int x, int y) {\n" +
                            "        if (y == 0) {\n" +
                            "            return x;\n" +
                            "        }\n" +
                            "        return gcd(y, modulo( x , y ));\n" +
                            "}\n" +
                            "print(gcd(34 , 6 ));"
                    ));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(1, checker.getErrors().size());
            Assert.assertEquals("Error: Function calls cannot be made inside function calls at Line: 13 Character: 15",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses a call
     * to a function not declared.
     */
    @Test
    public void test_Function8() {
        try {
            checker.check(
                    getParseTree(
                            "print(gcd(34 , 6 ));" +
                            "function int gcd(int x, int y) {\n" +
                            "        if (y == 0) {\n" +
                            "            return x;\n" +
                            "        }\n" +
                            "        return gcd(y, x -y);\n" +
                            "}\n"
                    ));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(1, checker.getErrors().size());
            Assert.assertEquals("Error: you are calling a function that does not exist yet at Line: 1 Character: 6",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses
     * a void function which returns an basic value.
     */
    @Test
    public void test_Function9() {
        try {
            checker.check(
                    getParseTree(
                            "function int gcd(int x, int y) {\n" +
                                    "        if (y == 0) {\n" +
                                    "            return x;\n" +
                                    "        }\n" +
                                    "        return;\n" +
                                    "}\n"
                    ));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(1, checker.getErrors().size());
            Assert.assertEquals("Error: A function claims to return NUM but actually returns null at Line: 5 Character: 8",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses a function
     * with a definition to return a boolean
     * but returns nothing
     */
    @Test
    public void test_Function10() {
        try {
            checker.check(
                    getParseTree(
                            "function bool gcd(int x, int y) {\n" +
                                    "        if (y == 0) {\n" +
                                               "print(x);" +
                                    "        }\n" +
                                    "}\n"
                    ));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(1, checker.getErrors().size());
            Assert.assertEquals("Error: You cannot have an integer or boolean function that returns nothing at Line: 1 Character: 0",
                    checker.getErrors().get(0));
        }
    }
    /**
     * Test if the compiler refuses a function
     * with a definition to return a boolean
     * but returns nothing
     */
    @Test
    public void test_Function12() {
        try {
            checker.check(
                    getParseTree(
                            "function bool xor (bool x, bool y) {\n" +
                                        "if (y and x){" +
                                        "return false;" +
                                    "}" +
                                        "return x or y ;" +
                                    "}\n" +
                                    "print(xor(5,4));"
                    ));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(2, checker.getErrors().size());
            Assert.assertEquals("Error: the parameter type not equal to expected type at Line: 3 Character: 6" ,
                    checker.getErrors().get(0));
            Assert.assertEquals("Error: the parameter type not equal to expected type at Line: 3 Character: 6" ,
                    checker.getErrors().get(1));
        }
    }
}
