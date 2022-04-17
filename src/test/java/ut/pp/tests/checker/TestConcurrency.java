package ut.pp.tests.checker;

import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;

import static ut.pp.tests.checker.SimpleExpr.getParseTree;

public class TestConcurrency {
    final Checker checker = new Checker();

    /**
     * Test if the system rejects more than 7 threads being defined.
     */
    @Test
    public void test_concurrency1() throws Exception{
        try {
            checker.check(
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
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
            Assert.assertTrue(checker.getErrors().contains(" more than 7 threads are defined, more shared memory locations are used than capacity"));

        }


    }
}
