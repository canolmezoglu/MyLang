package ut.pp.tests.contextual;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;
import ut.pp.compiler.Result;

import static ut.pp.tests.contextual.TestSimpleExpr.getParseTree;

public class TestArray {
    private Scanner scanner = new Scanner();
    /**
     * Test if the compiler rejects the
     * program if array lengths do not
     * match.
     */
    @Test
    public void test_Array1() throws Exception {
        try {
            ParseTree tree = getParseTree("int a[5] = {100,250,30,47,55,66};\n");
            Result result = scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Error: The size of the array does not match the number of elements you have listed at Line: 1 Character: 0", scanner.getErrors().get(0));

        }
    }
    /**
     * Test if the compiler accepts the
     * program if array lengths match.
     */
    @Test
    public void test_Array2() throws Exception {
        ParseTree tree = getParseTree("int a[5] = {100,250,30,47,55};\n");
        Result result = scanner.check(tree);
        Assert.assertEquals(0, scanner.getErrors().size());
    }

    /**
     * Test if the compiler rejects the
     * program if 2D array lengths do not
     * match.
     */
    @Test
    public void test_Array3() throws Exception {
        try {
            ParseTree tree = getParseTree("int a[3][5] = {{100,250,30,47,55},{100,-3,5,47,66,54},{100,250,30,47,55}};\n");
            Result result = scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Error: The number of columns does not match at Line: 1 Character: 0", scanner.getErrors().get(0));

        }
    }
    /**
     * Test if the compiler accepts the
     * program if 2D array lengths do
     * match.
     */
    @Test
    public void test_Array4() throws Exception {
        ParseTree tree = getParseTree("int a[3][5] = {{100,250,30,47,55},{100,-3,5,47,66},{100,250,30,47,55}};\n");
        Result result = scanner.check(tree);
        Assert.assertEquals(0, scanner.getErrors().size());

        }
    /**
     * Test if the compiler rejects
     * a simple expr to dynamically obtain
     * variables from an array.
     */
    @Test
    public void test_Array5() throws Exception {
        try {
            ParseTree tree = getParseTree("int a[3] = {451,42,-87}; int a%(1+1) = 5;\n");
            Result result = scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertTrue( scanner.getErrors().size() > 0);
            Assert.assertEquals("Error:a% array or array index not declared in this scope at Line: 1 Character: 29", scanner.getErrors().get(6));

        }

    }
    /**
     * Test if the compiler rejects
     * the code if an array name is
     * already declared.
     */
    @Test
    public void test_Array6() throws Exception {
        try {
            ParseTree tree = getParseTree("int a[3] = {451,42,-87}; int a = 5;\n");
            Result result = scanner.check(tree);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertTrue( scanner.getErrors().size() > 0);
            Assert.assertEquals("Error:a already declared in this scope at Line 1 Character:25", scanner.getErrors().get(0));
        }
    }
}

