package ut.pp.tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestConcurrency {
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
        Assert.assertEquals("Sprockell 0 says 100",ut.pp.Main.runSprockell(input).get(0));
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
        Assert.assertEquals("Sprockell 3 says 3",output.get(3));
        Assert.assertEquals("Sprockell 0 says 4",output.get(4));
        Assert.assertEquals("Sprockell 4 says 5",output.get(5));
        Assert.assertEquals("Sprockell 5 says 6",output.get(6));
        Assert.assertEquals("Sprockell 0 says 7",output.get(7));
        Assert.assertEquals("Sprockell 6 says 8",output.get(8));
        Assert.assertEquals("Sprockell 7 says 9",output.get(9));
    }
    @Test
    public void testInterleave() throws Exception{
        String input = "interleave";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 1 says 1",output.get(0));
        Assert.assertEquals("Sprockell 2 says 2",output.get(1));
        int i = 1;
        while(i < output.size() - 3){
            Assert.assertEquals("Sprockell 1 says 1",output.get(++i));
            Assert.assertEquals("Sprockell 3 says 3",output.get(++i));
            Assert.assertEquals("Sprockell 2 says 2",output.get(++i));
            Assert.assertEquals("Sprockell 4 says 4",output.get(++i));
        }
        Assert.assertEquals("Sprockell 3 says 3",output.get(++i));
        Assert.assertEquals("Sprockell 4 says 4",output.get(++i));

    }
    @Test
    public void testVectorSum() throws Exception{
        String input = "vectorsum";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 427",output.get(0));
    }

    @Test
    public void testSharedEnum() throws Exception{
        String input = "shared_enum";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 1 says 3",output.get(0));
        Assert.assertEquals("Sprockell 2 says 1",output.get(1));
    }

    @Test
    public void testNestedConcurrencyInterleave() throws Exception {
        String input = "nestedConcurrencyInterleave";
        List<String> output = ut.pp.Main.runSprockell(input);
        int i = 0;
        while(i < 200){
            Assert.assertEquals("Sprockell 1 says 1",output.get(i++));
            Assert.assertEquals("Sprockell 4 says 2",output.get(i++));
        }
        Assert.assertEquals("Sprockell 4 says 2",output.get(i++));
        Assert.assertEquals("Sprockell 2 says 3",output.get(i++));
        while(i < 498){
            Assert.assertEquals("Sprockell 4 says 2",output.get(i++));
            Assert.assertEquals("Sprockell 3 says 4",output.get(i++));
            Assert.assertEquals("Sprockell 2 says 3",output.get(i++));
        }
        Assert.assertEquals("Sprockell 3 says 4",output.get(i++));

    }
//TODO ADD DEADLOCK TESTS

}
