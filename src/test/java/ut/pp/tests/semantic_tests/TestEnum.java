package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestEnum {
    /**
     * Tests a basic enum example.
     * @throws Exception
     */
    @Test
    public void testEnum() throws Exception {
        String input = "enum";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("[Sprockell 0 says 1, Sprockell 0 says 39, Sprockell 0 says 2, Sprockell 0 says 1]", output.toString());
    }

}
