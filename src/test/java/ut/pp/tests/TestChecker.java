package ut.pp.tests;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.elaboration.Checker;
import ut.pp.elaboration.Result;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import static org.junit.Assert.assertEquals;

public class TestChecker {

    final Checker checker = new Checker();

    public static ParseTree getParseTree(String code){
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        return tree;
    }
    /**
     * Test regular integer assignment
     */
    @Test
    public void test_assStat1() throws Exception {
        checker.check (
             getParseTree("int fib = 5 ; ")
        );
        Assert.assertEquals(0,checker.getErrors().size());
    }
    /**
     * Test if prefix expression works correctly for boolean
     */
    @Test
    public void test_prfExpr1() throws Exception {
        checker.check (
                getParseTree("print(not true); ")
        );
        Assert.assertEquals(0,checker.getErrors().size());
    }
    /**
     * Test if prefix expression works correctly for int
     */
    @Test
    public void test_prfExpr2() throws Exception {
        checker.check (
                getParseTree("print(-5); ")
        );
        Assert.assertEquals(0,checker.getErrors().size());
    }
    /**
     * Test if prefix expression catches error when an
     * integer is assigned to a boolean prefix
     */
    @Test
    public void test_prfExpr3() throws Exception {
        try {
            checker.check (
                    getParseTree("print(not 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if prefix expression catches error when an
     * boolean is assigned to a integer prefix
     */

    @Test
    public void test_prfExpr4() throws Exception {
        try {
            checker.check(
                    getParseTree("print(-true); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if addition with plus works with correct types
     */
    @Test
    public void test_addExpr1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(5 + 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when addition with
     * plus works is given different types
     */
    @Test
    public void test_addExpr2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true + 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when addition with
     * plus works is given different types
     */
    @Test
    public void test_addExpr3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(6 + true); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if addition with minus works with correct types
     */
    @Test
    public void test_addExpr4() throws Exception {
        try {
            checker.check(
                    getParseTree("print(5 - 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when addition with
     * minus works is given different types
     */
    @Test
    public void test_addExpr5() throws Exception {
        try {
            checker.check(
                    getParseTree("print(false - 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when addition with
     * plus works is given different types
     */
    @Test
    public void test_addExpr6() throws Exception {
        try {
            checker.check(
                    getParseTree("print(618- false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if multiplication works with correct types
     */
    @Test
    public void test_multExpr1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(5 * 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when multiplication
     * is given different types
     */
    @Test
    public void test_multExpr2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true * 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when multiplication
     * is given different types
     */
    @Test
    public void test_multExpr3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(6 * false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }
    /**
     * Test if less than or equal operator accepts
     * the same correct type
     */

    @Test
    public void test_compLe1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(6 <= 2); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if less than or equal operator accepts
     * the wrong different types
     */

    @Test
    public void test_compLe2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(6 <= false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if less than or equal operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compLe3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true <= false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if less than operator accepts
     * the same correct type
     */

    @Test
    public void test_compLt1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(3721 < -2); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if less than operator accepts
     * the wrong different types
     */

    @Test
    public void test_compLt2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true < 7); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if less than operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compLt3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(not true <= false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if greater than or equal operator accepts
     * the same correct type
     */

    @Test
    public void test_compGe1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(-55 >= -22); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if greater than or equal operator accepts
     * the wrong different types
     */

    @Test
    public void test_compGe2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(not true >= 8282); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if greater than or equal operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compGe3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(not true >= not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if greater than operator accepts
     * the same correct type
     */

    @Test
    public void test_compGt1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(-535 > -22); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if greater than operator accepts
     * the wrong different types
     */

    @Test
    public void test_compGt2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(3232  > not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if greater than operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compGt3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true > not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if equal accepts
     * the same integer types
     */

    @Test
    public void test_compEq1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(-535  == -22); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if equal accepts
     * the same boolean types
     */

    @Test
    public void test_compEq2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true  == not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if equal operator rejects
     * the wrong different types
     */

    @Test
    public void test_compEq3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(3232  == not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if the not equal accepts
     * the same integer types
     */

    @Test
    public void test_compNeq1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(-535  != -22); ")
            );
        }
        catch (Exception e){
            checker.getErrors();
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if the not equal accepts
     * the same boolean types
     */

    @Test
    public void test_compNeq2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true  != not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }
    /**
     * Test if equal operator rejects
     * the wrong different types
     */


    @Test
    public void test_compNeq3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(3232  != not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }

    /**
     * Test if and operator rejects
     * the wrong different types
     */


    @Test
    public void test_bool1() throws Exception {
        try {
            checker.check(
                    getParseTree("print(3232  and not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }

    /**
     * Test if and operator accepts
     * the correct types
     */


    @Test
    public void test_bool2() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true  and not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }

    /**
     * Test if or operator rejects
     * the wrong different types
     */


    @Test
    public void test_bool3() throws Exception {
        try {
            checker.check(
                    getParseTree("print(3232  or not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }

    }
    /**
     * Test if or operator accepts
     * the correct types
     */


    @Test
    public void test_bool4() throws Exception {
        try {
            checker.check(
                    getParseTree("print(true  or not false); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }

    }

    /**
     * Test if change assignment accepts the correct type
     * with integers.
     */
    @Test
    public void test_changeAss1() throws Exception{
        try {
            checker.check(
                    getParseTree(" int a = 5; a = 16; ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }

    /**
     * Test if change assignment accepts the correct type
     * with booleans.
     */
    @Test
    public void test_changeAss2() throws Exception{
        try {
            checker.check(
                    getParseTree(" bool a = true; a = false; ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(0,checker.getErrors().size());
        }
    }
    /**
     * Test if change assignment rejects the incorrect type.
     */
    @Test
    public void test_changeAss3() throws Exception{
        try {
            checker.check(
                    getParseTree(" bool a = true; a = 12; ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1,checker.getErrors().size());
        }
    }









}
