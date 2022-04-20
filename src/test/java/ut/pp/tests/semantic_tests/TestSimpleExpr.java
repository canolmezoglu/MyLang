package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSimpleExpr {
    /**
     * Tests whether precedence works as expected.
     * @throws Exception
     */
    @Test
    public void testPrecedence() throws Exception {
        String input = "precedence";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 51" ,output.get(0));
        Assert.assertEquals("Sprockell 0 says 7" ,output.get(1));
        Assert.assertEquals("Sprockell 0 says -1" ,output.get(2));
        Assert.assertEquals("Sprockell 0 says 43" ,output.get(3));

    }
    /**
     * Tests if basic types are represented in the program.
     * @throws Exception
     */

    @Test
    public void testBasicTypes() throws Exception{
        String input = "311";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 7" ,output.get(0));
        Assert.assertEquals("Sprockell 0 says 1" ,output.get(1));
        Assert.assertEquals("Sprockell 0 says 0" ,output.get(2));
        Assert.assertEquals("Sprockell 0 says -11" ,output.get(3));
        Assert.assertEquals("Sprockell 0 says -2147483647" ,output.get(4));
        Assert.assertEquals("Sprockell 0 says 2147483647" ,output.get(5));
    }
    @Test
    public void testSimpleExpr() throws Exception{
        String input = "321";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 14" ,output.get(0));
        Assert.assertEquals("Sprockell 0 says 10" ,output.get(1));
        Assert.assertEquals("Sprockell 0 says 10" ,output.get(2));
        Assert.assertEquals("Sprockell 0 says 0" ,output.get(3));
        Assert.assertEquals("Sprockell 0 says 1" ,output.get(4));
        Assert.assertEquals("Sprockell 0 says 1" ,output.get(5));
        Assert.assertEquals("Sprockell 0 says 1" ,output.get(6));

    }
}
