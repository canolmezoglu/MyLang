package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestArray {
    /**
     * Tests equality, dynamic access between 1D arrays.
     * @throws Exception
     */
    @Test
    public void test1dArray() throws Exception {
        String input = "1darray";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 130, Sprockell 0 says 203, Sprockell 0 says 1, Sprockell 0 says 100, Sprockell 0 says 0]", output.toString());
    }
    /**
     * Tests equality, dynamic access between 2D arrays.
     * @throws Exception
     */
    @Test
    public void test2dArray() throws Exception {
        String input = "2darray";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 7, Sprockell 0 says 7, Sprockell 0 says 7, Sprockell 0 says 7, Sprockell 0 says 10, Sprockell 0 says 1]", output.toString());
    }

    /**
     * Tests whether dynamic access to arrays work.
     * @throws Exception
     */
    @Test
    public void testDynamicArray() throws Exception {
        String input = "dynamic_array";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 100, Sprockell 0 says 250, Sprockell 0 says 30, Sprockell 0 says 47, Sprockell 0 says 55, Sprockell 0 says 1, Sprockell 0 says 4]", output.toString());
    }


}
