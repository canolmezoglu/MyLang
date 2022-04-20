package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSoftDivision {
    /**
     * Tests a basic division example.
     * Tests negative number division as well.
     * @throws Exception
     */
    @Test
    public void testDivision() throws Exception {
        String input = "division";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 9, " +
                "Sprockell 0 says 8, Sprockell 0 says 0, Sprockell 0 says -5, " +
                "Sprockell 0 says 5, Sprockell 0 says -5]", output.toString());
    }

}
