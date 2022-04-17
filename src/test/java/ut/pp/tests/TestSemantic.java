package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSemantic {


    @Test
    public void testBanking() throws Exception {
        String input = "banking";
        Assert.assertEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input).get(0));
    }    @Test
    public void testBankingMaxThreads() throws Exception {
        String input = "bankingMaxThreads";
        Assert.assertEquals("Sprockell 0 says 183",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void testBankingNoLocks() throws Exception {
        String input = "bankingNoLocks";
        Assert.assertNotEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void testPeterson() throws Exception {
        String input = "peterson";
        Assert.assertEquals("Sprockell 0 says 3",ut.pp.Main.runSprockell(input).get(0));
    }
    @Test
    public void testNestedConcurrency() throws Exception {
        String input = "nestedConcurrency";
        Assert.assertEquals("Sprockell 0 says -287",ut.pp.Main.runSprockell(input).get(0));
    }

    @Test
    public void testThreadRunningOrder() throws Exception {
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

    @Test
    public void testWhile() throws Exception {
        String input = "while";
        List<String> output = ut.pp.Main.runSprockell(input);
        for (int i = 99; i >= 0;i--){
            Assert.assertEquals("Sprockell 0 says " + Integer.toString(i), output.get(99 - i));
        }

    }
    @Test
    public void testFib() throws Exception {
        String input = "fib";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 13", output.get(0));
    }
    @Test
    public void test1dArray() throws Exception {
        String input = "1darray";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 130, Sprockell 0 says 203, Sprockell 0 says 1, Sprockell 0 says 100, Sprockell 0 says 0]", output.toString());
    }
    @Test
    public void test2dArray() throws Exception {
        String input = "2darray";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 7, Sprockell 0 says 7, Sprockell 0 says 7, Sprockell 0 says 7, Sprockell 0 says 10, Sprockell 0 says 1]", output.toString());
    }
    @Test
    public void testDynamicArray() throws Exception {
        String input = "dynamic_array";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 100, Sprockell 0 says 250, Sprockell 0 says 30, Sprockell 0 says 47, Sprockell 0 says 55, Sprockell 0 says 1, Sprockell 0 says 4]", output.toString());
    }

    @Test
    public void testEnum() throws Exception {
        String input = "enum";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 1, Sprockell 0 says 39, Sprockell 0 says 2, Sprockell 0 says 1]", output.toString());
    }
    @Test
    public void testPointer() throws Exception {
        String input = "pointer";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 600, Sprockell 0 says 700, Sprockell 0 says 100]", output.toString());
    }


}
