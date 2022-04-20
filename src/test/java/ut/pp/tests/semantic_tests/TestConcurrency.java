package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestConcurrency {
    /**
     * Tests the classic banking example.
     * The result is the sum of the all values.
     * @throws Exception
     */
    @Test
    public void testBanking() throws Exception {
        String input = "banking";
        Assert.assertEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input).get(0));
    }
    /**
     * Tests the banking with the maximum amount of available threads.
     * The result is the sum of the all values.
     * @throws Exception
     */
    @Test
    public void testBankingMaxThreads() throws Exception {
        String input = "bankingMaxThreads";
        Assert.assertEquals("Sprockell 0 says 183",ut.pp.Main.runSprockell(input).get(0));
    }
    /**
     * Tests the banking example without locks to see
     * if locks actually make an impact.
     * @throws Exception
     */
    @Test
    public void testBankingNoLocks() throws Exception {
        String input = "bankingNoLocks";
        Assert.assertNotEquals("Sprockell 0 says -95",ut.pp.Main.runSprockell(input).get(0));
    }

    /**
     * Tests the peterson example as requested.
     * The sum of values if critical sections are accessed properly
     * will be 100.
     * @throws Exception
     */
    @Test
    public void testPeterson() throws Exception {
        String input = "peterson";
        Assert.assertEquals("Sprockell 0 says 100",ut.pp.Main.runSprockell(input).get(0));
    }
    /**
     * Tests the peterson example with shared arrays.
     */
    @Test
    public void testPetersonWithSharedArray() throws Exception {
        String input = "petersonWithSharedArray";
        Assert.assertEquals("Sprockell 0 says 100",ut.pp.Main.runSprockell(input).get(0));
    }
    /**
     * Tests nested concurrency by a banking example to see
     * if all threads get to run, including the threads spawned
     * by the first thread of the parallel block.
     */
    @Test
    public void testNestedConcurrency() throws Exception {
        String input = "nestedConcurrency";
        Assert.assertEquals("Sprockell 0 says -287",ut.pp.Main.runSprockell(input).get(0));
    }
    /**
     * Tests thread running order to make sure the programs adhere to the
     * parend/parbegin.
     */
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

    /**
     * Tests if the tests actually work concurrently by making them print
     * a number. As they take turns printing the number, and not write a
     * whole block at once it can be seen that they are concurrent.
     * @throws Exception
     */
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

    /**
     * Test whether two threads can take the sum by getting different
     * indices of a shared array.
     * @throws Exception
     */
    @Test
    public void testVectorSum() throws Exception{
        String input = "vectorsum";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 427",output.get(0));
    }
    /**
     * Test if shared enums work.
     */
    @Test
    public void testSharedEnum() throws Exception{
        String input = "shared_enum";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 1 says 3",output.get(0));
        Assert.assertEquals("Sprockell 2 says 1",output.get(1));
    }

    /**
     * Test if nested threads also work concurrently by making the
     * the second thread of the first parallel block
     * run for a longer time, thereby they are all supposed
     * to run simultaneously.
     * @throws Exception
     */
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
    /**
     * Expected to fail by timing out.
     * Test to show that threads can deadlock.
     */
    @Test(timeout = 10000)
    public void testDeadlock() throws Exception{
        String input = "deadlockwiththreads";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.fail();
    }

}
