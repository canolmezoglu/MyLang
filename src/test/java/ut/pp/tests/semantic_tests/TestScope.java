package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestScope {
    /**
     * Tests if variables can be renamed
     * in inner scopes and still accesssed.
     * Also tests if scopes are closed correctly.
     * @throws Exception
     */
    @Test
    public void testScope1() throws Exception {
        String input = "331";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 75", output.get(0));
        Assert.assertEquals("Sprockell 0 says 1", output.get(1));
        Assert.assertEquals("Sprockell 0 says 10", output.get(2));

    }
}
