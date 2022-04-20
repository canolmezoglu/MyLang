package ut.pp.tests.contextual;

import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;

import static ut.pp.tests.contextual.TestSimpleExpr.getParseTree;

public class TestConcurrency {
    final Scanner scanner = new Scanner();

    /**
     * Test if the system rejects more than 7 threads being defined.
     */
    @Test
    public void test_concurrency1() throws Exception{
        try {
            scanner.check(
                    getParseTree(" print(0);\n" +
                            "parallel{\n" +
                            "    thread {print(1);\n" +
                            "              parallel{\n" +
                            "                 thread {print(2);}\n" +
                            "                 thread {print(3);}\n" +
                            "                 }\n" +
                            "              }\n" +
                            "    }\n" +
                            "print (4);\n" +
                            "parallel{\n" +
                            "     thread {print(5);}\n" +
                            "     thread {print(6);}\n" +
                            "    }\n" +
                            "print (7);\n" +
                            "parallel{\n" +
                            "     thread {print(8);}\n" +
                            "     thread {print(9);}\n" +
                            "     thread {print(10);}\n" +
                            "    }\n")
            );
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertTrue(scanner.getErrors().contains(" Error: More than 7 threads are defined, more shared memory locations are used than capacity"));
        }

    }
    /**
     * Test if the system rejects more than 7 memory locations being used with arrays.
     */
    @Test
    public void test_concurrency2() throws Exception{
        try {
            scanner.check(
                    getParseTree(" print(0);\n" +
                            "shared int arr[6] = {1,2,3,4,5,6};" +
                            "parallel{\n" +
                            "    thread {print(1);\n" +
                            "              parallel{\n" +
                            "                 thread {print(2);}\n" +
                            "                 thread {print(3);}\n" +
                            "                 }\n" +
                            "              }\n" +
                            "    }\n"
            ));
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertTrue(scanner.getErrors().contains("Error: More than 7 shared memory locations are used at line: 2"));
        }
    }
    /**
     * Test if the system rejects more than 7 memory locations being used with variables.
     */
    @Test
    public void test_concurrency3() throws Exception{
        try {
            scanner.check(
                    getParseTree(
                            "shared int var1 = 5;" +
                            "shared int var2 = 5;" +
                            "shared int var3 = 5;" +
                            "shared int var4 = 5;" +
                            "shared int var5 = 5;" +
                            "parallel{\n" +
                            "    thread {print(1);\n" +
                            "              parallel{\n" +
                            "                 thread {print(2);}\n" +
                            "                 thread {print(3);}\n" +
                            "                 }\n" +
                            "              }\n" +
                            "    }\n"
                    ));
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertTrue(scanner.getErrors().contains("Error: More than 7 shared memory locations are used at line: 1"));
        }

    }
    /**
     * Test if the system accepts less than 7 memory locations being used with variables.
     */
    @Test
    public void test_concurrency4() throws Exception{
            scanner.check(
                    getParseTree(
                            "shared int var1 = 5;" +
                                    "shared int var2 = 5;" +
                                    "parallel{\n" +
                                    "    thread {print(1);\n" +
                                    "              parallel{\n" +
                                    "                 thread {print(2);}\n" +
                                    "                 thread {print(3);}\n" +
                                    "                 }\n" +
                                    "              }\n" +
                                    "    }\n"
                    ));
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if the system accepts less than 7 memory locations being used with arrays.
     */
    @Test
    public void test_concurrency5() throws Exception{
        scanner.check(
                getParseTree(
                        "shared int arr[3] = {1,2,3};" +
                                "parallel{\n" +
                                "    thread {print(1);\n" +
                                "              parallel{\n" +
                                "                 thread {print(2);}\n" +
                                "                 thread {print(3);}\n" +
                                "                 }\n" +
                                "              }\n" +
                                "    }\n"
                ));
        Assert.assertEquals(0, scanner.getErrors().size());
    }


    }
