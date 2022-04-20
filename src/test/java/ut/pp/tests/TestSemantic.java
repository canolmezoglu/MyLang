package ut.pp.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSemantic {




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
