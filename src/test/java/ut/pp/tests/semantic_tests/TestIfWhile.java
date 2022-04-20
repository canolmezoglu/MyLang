package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.security.spec.ECField;
import java.util.List;

public class TestIfWhile {
    /**
     * A basic while loop to see if while loops work
     * as expected.
     * @throws Exception
     */
    @Test
    public void testWhile() throws Exception {
        String input = "while";
        List<String> output = ut.pp.Main.runSprockell(input);
        for (int i = 99; i >= 0;i--){
            Assert.assertEquals("Sprockell 0 says " + Integer.toString(i), output.get(99 - i));
        }

    }
    /**
     * A basic if statement to see if while loops work
     * as expected.
     * @throws Exception
     */
    @Test
    public void testIf() throws Exception {
        String input = "ifElse";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 500",output.get(0));
        Assert.assertEquals("Sprockell 0 says 0",output.get(1));
    }

    /**
     * Tests a rosetta example.
     * http://rosettacode.org/wiki/Loops/While
     * @throws Exception
     */
    @Test
    public void TestRosettaWhile() throws Exception{
        String input = "rosettaWhile";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 1024",output.get(0));
        Assert.assertEquals("Sprockell 0 says 512",output.get(1));
        Assert.assertEquals("Sprockell 0 says 256",output.get(2));
        Assert.assertEquals("Sprockell 0 says 128",output.get(3));
        Assert.assertEquals("Sprockell 0 says 64",output.get(4));
        Assert.assertEquals("Sprockell 0 says 32",output.get(5));
        Assert.assertEquals("Sprockell 0 says 16",output.get(6));
        Assert.assertEquals("Sprockell 0 says 8",output.get(7));
        Assert.assertEquals("Sprockell 0 says 4",output.get(8));
        Assert.assertEquals("Sprockell 0 says 2",output.get(9));
        Assert.assertEquals("Sprockell 0 says 1",output.get(10));
    }

    /**
     * Tests an implementation of insertion sort made in Sprockell.
     * @throws Exception
     */
    @Test
    public void TestInsertionSort() throws Exception{
        String input = "insertion_sort";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 1",output.get(0));
        Assert.assertEquals("Sprockell 0 says 2",output.get(1));
        Assert.assertEquals("Sprockell 0 says 4",output.get(2));
        Assert.assertEquals("Sprockell 0 says 7",output.get(3));
        Assert.assertEquals("Sprockell 0 says 8",output.get(4));
        Assert.assertEquals("Sprockell 0 says 9",output.get(5));
        Assert.assertEquals("Sprockell 0 says 16",output.get(6));
    }
    /**
     * Tests a changed version of:
     * http://rosettacode.org/wiki/Loops/Nested
     */
    @Test
    public void TestNestedWhile() throws Exception{
        String input = "findTrueFrom2dArray";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 3",output.get(0));
        Assert.assertEquals("Sprockell 0 says 2",output.get(1));
    }


}
