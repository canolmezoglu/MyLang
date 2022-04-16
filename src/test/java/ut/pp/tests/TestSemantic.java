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
        Assert.assertEquals("Sprockell 0 says -87",ut.pp.Main.runSprockell(input).get(0));
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


}
