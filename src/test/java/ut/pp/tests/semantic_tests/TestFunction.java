package ut.pp.tests.semantic_tests;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestFunction {
    /**
     * Testing to see whether recursive functions work.
     * For the standard memory of sprockell, only works
     * till fib(6).
     * @throws Exception
     */
    @Test
    public void testFib() throws Exception {
        String input = "fib";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 8", output.get(0));
    }

    /**
     * Testing pass by reference by a basic swap.
     * @throws Exception
     */
    @Test
    public void testSwapByReference() throws Exception {
        String input = "swapByReference";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 77", output.get(0));
        Assert.assertEquals("Sprockell 0 says 5", output.get(1));

    }
    /**
     * Testing if normal parameters and parameters passed by reference
     * can work together.
     * @throws Exception
     */
    @Test
    public void testReferenceSumAndChange() throws Exception {
        String input = "referenceSumAndChange";
        List<String> output = ut.pp.Main.runSprockell(input);
        Assert.assertEquals("Sprockell 0 says 18", output.get(0));
        Assert.assertEquals("Sprockell 0 says 60", output.get(1));
        Assert.assertEquals("Sprockell 0 says 2", output.get(2));
    }
    public boolean isPrime(int num){
        if (num==1 || num ==2){
            return true;
        }
        for (int i = 2; i <= num / 2; ++i) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests the isPrime function written in Sprockell
     * against an isPrime function written in Java.
     * @throws Exception
     */

    @Test
    public void testIsPrime() throws Exception {
        String input = "isPrime";
        String FILE_PATH = "src/main/sample/" + input + ".txt";
        List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(FILE_PATH), StandardCharsets.UTF_8));
        for (int i =1; i < 25; i++){
            fileContent.set(22,"print(isPrime("+ Integer.toString(i) +" ));");
            Files.write(Paths.get(FILE_PATH), fileContent, StandardCharsets.UTF_8);
            List<String> output = ut.pp.Main.runSprockell(input);
            if (isPrime(i)){
                Assert.assertEquals("Sprockell 0 says 1", output.get(0));
            }
            else {
                Assert.assertEquals("Sprockell 0 says 0", output.get(0));
            }
        }
    }
    int gcd(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        }
        return gcd(n2, n1 % n2);
    }
    /**
     * Tests the gcd function written in Sprockell
     * against an gcd function written in Java.
     * @throws Exception
     */
    @Test
    public void testGcd() throws Exception {
        String input = "euclidGcd";
        String FILE_PATH = "src/main/sample/" + input + ".txt";
        List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(FILE_PATH), StandardCharsets.UTF_8));
        for (int i =30; i < 35; i++){
            for (int x=1;x<7 ;x++){
                fileContent.set(15, "print(gcd(" + Integer.toString(i) + " , " + Integer.toString(x) + " ));");
                Files.write(Paths.get(FILE_PATH), fileContent, StandardCharsets.UTF_8);
                List<String> output = ut.pp.Main.runSprockell(input);
                Assert.assertEquals("Sprockell 0 says "+ Integer.toString(gcd(i,x)), output.get(0));

            }
        }
    }
}

