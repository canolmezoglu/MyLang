package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSemantic {





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
