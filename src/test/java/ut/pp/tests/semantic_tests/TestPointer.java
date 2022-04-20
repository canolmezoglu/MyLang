package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestPointer {
    /**
     * Tests a basic pointer example.
     * @throws Exception
     */
    @Test
    public void testPointer() throws Exception {
        String input = "pointer";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 600, Sprockell 0 says 700, Sprockell 0 says 100]", output.toString());
    }


}
