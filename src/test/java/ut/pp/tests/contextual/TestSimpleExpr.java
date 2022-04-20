package ut.pp.tests.contextual;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import ut.pp.compiler.Scanner;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

public class TestSimpleExpr {

    final Scanner scanner = new Scanner();

    public static ParseTree getParseTree(String code){
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        return parser.program();
    }

    /**
     * Test if prefix expression works correctly for boolean
     */
    @Test
    public void test_prfExpr1() throws Exception {

        scanner.check (getParseTree("print(not true); "));
        Assert.assertEquals(0, scanner.getErrors().size());

    }
    /**
     * Test if prefix expression works correctly for int
     */
    @Test
    public void test_prfExpr2() throws Exception {
        scanner.check (getParseTree("print(-5); "));
        Assert.assertEquals(0, scanner.getErrors().size());

    }
    /**
     * Test if prefix expression catches error when an
     * integer is assigned to a boolean prefix
     */
    @Test
    public void test_prfExpr3() {
        try {
            scanner.check (
                    getParseTree("print(not 5); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals(e.getMessage(),"Prefix operation has type mismatch, expected bool, got int at Line: 1 Character: 6");
        }

    }
    /**
     * Test if prefix expression catches error when an
     * boolean is assigned to a integer prefix
     */

    @Test
    public void test_prfExpr4() {
        try {
            scanner.check(
                    getParseTree("print(-true); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals(e.getMessage(),"Prefix operation has type mismatch, expected int, got bool at Line: 1 Character: 6");

        }
    }
    /**
     * Test if addition with plus works with correct types
     */
    @Test
    public void test_addExpr1() throws Exception {

            scanner.check(
                    getParseTree("print(5 + 5); ")
            );


        Assert.assertEquals(0, scanner.getErrors().size());

    }
    /**
     * Test if error is thrown when addition with
     * plus works is given different types
     */
    @Test
    public void test_addExpr2() {
        try {
            scanner.check(
                    getParseTree("print(true + 5); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when addition with
     * plus works is given different types
     */
    @Test
    public void test_addExpr3(){
        try {
            scanner.check(
                    getParseTree("print(6 + true); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test if addition with minus works with correct types
     */
    @Test
    public void test_addExpr4() throws Exception {

            scanner.check(
                    getParseTree("print(5 - 5); ")
            );


            Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if error is thrown when addition with
     * minus works is given different types
     */
    @Test
    public void test_addExpr5() {
        try {
            scanner.check(
                    getParseTree("print(false - 5); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when addition with
     * plus works is given different types
     */
    @Test
    public void test_addExpr6()  {
        try {
            scanner.check(
                    getParseTree("print(618- false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test if multiplication works with correct types
     */
    @Test
    public void test_multExpr1() throws Exception {
        scanner.check(
                    getParseTree("print(5 * 5); ")
            );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if error is thrown when multiplication
     * is given different types
     */
    @Test
    public void test_multExpr2()  {
        try {
            scanner.check(
                    getParseTree("print(true * 5); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test if error is thrown when multiplication
     * is given different types
     */
    @Test
    public void test_multExpr3() {
        try {
            scanner.check(
                    getParseTree("print(6 * false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }
    }
    /**
     * Test if less than or equal operator accepts
     * the same correct type
     */

    @Test
    public void test_compLe1() throws Exception {
        scanner.check(
                    getParseTree("print(6 <= 2); ")
            );

        Assert.assertEquals(0, scanner.getErrors().size());


    }
    /**
     * Test if less than or equal operator accepts
     * the wrong different types
     */

    @Test
    public void test_compLe2()  {
        try {
            scanner.check(
                    getParseTree("print(6 <= false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if less than or equal operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compLe3()  {
        try {
            scanner.check(
                    getParseTree("print(true <= false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if less than operator accepts
     * the same correct type
     */

    @Test
    public void test_compLt1() throws Exception {
        scanner.check(
                    getParseTree("print(3721 < -2); ")
            );
        Assert.assertEquals(0, scanner.getErrors().size());

    }
    /**
     * Test if less than operator accepts
     * the wrong different types
     */

    @Test
    public void test_compLt2()  {
        try {
            scanner.check(
                    getParseTree("print(true < 7); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if less than operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compLt3() {
        try {
            scanner.check(
                    getParseTree("print(not true <= false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if greater than or equal operator accepts
     * the same correct type
     */

    @Test
    public void test_compGe1() throws Exception {
        scanner.check(
                    getParseTree("print(-55 >= -22); ")
            );
        Assert.assertEquals(0, scanner.getErrors().size());


    }
    /**
     * Test if greater than or equal operator accepts
     * the wrong different types
     */

    @Test
    public void test_compGe2()  {
        try {
            scanner.check(
                    getParseTree("print(not true >= 8282); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if greater than or equal operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compGe3()  {
        try {
            scanner.check(
                    getParseTree("print(not true >= not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if greater than operator accepts
     * the same correct type
     */

    @Test
    public void test_compGt1() throws Exception {
        scanner.check(
                    getParseTree("print(-535 > -22); ")
            );

        Assert.assertEquals(0, scanner.getErrors().size());


    }
    /**
     * Test if greater than operator accepts
     * the wrong different types
     */

    @Test
    public void test_compGt2() {
        try {
            scanner.check(
                    getParseTree("print(3232  > not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if greater than operator accepts
     * the wrong types that are same
     */

    @Test
    public void test_compGt3()  {
        try {
            scanner.check(
                    getParseTree("print(true > not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if equal accepts
     * the same integer types
     */

    @Test
    public void test_compEq1() throws Exception {
        scanner.check(
                    getParseTree("print(-535  == -22); ")
            );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if equal accepts
     * the same boolean types
     */

    @Test
    public void test_compEq2() throws Exception {
            scanner.check(
                    getParseTree("print(true  == not false); ")
            );

            Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if equal operator rejects
     * the wrong different types
     */

    @Test
    public void test_compEq3()  {
        try {
            scanner.check(
                    getParseTree("print(3232  == not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if the not equal accepts
     * the same integer types
     */

    @Test
    public void test_compNeq1() throws Exception {
        scanner.check(
                    getParseTree("print(-535  != -22); ")
            );


        Assert.assertEquals(0, scanner.getErrors().size());


    }
    /**
     * Test if the not equal accepts
     * the same boolean types
     */

    @Test
    public void test_compNeq2() throws Exception {
        scanner.check(
                    getParseTree("print(true  != not false); ")
            );
        Assert.assertEquals(0, scanner.getErrors().size());

    }
    /**
     * Test if equal operator rejects
     * the wrong different types
     */


    @Test
    public void test_compNeq3() {
        try {
            scanner.check(
                    getParseTree("print(3232  != not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }

    /**
     * Test if and operator rejects
     * the wrong different types
     */


    @Test
    public void test_bool1() {
        try {
            scanner.check(
                    getParseTree("print(3232  and not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }

    /**
     * Test if and operator accepts
     * the correct types
     */


    @Test
    public void test_bool2() throws Exception {
            scanner.check(
                    getParseTree("print(true  and not false); ")
            );
            Assert.assertEquals(0, scanner.getErrors().size());


    }

    /**
     * Test if or operator rejects
     * the wrong different types
     */


    @Test
    public void test_bool3() {
        try {
            scanner.check(
                    getParseTree("print(3232 or not false); ")
            );
            Assert.fail();

        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
        }

    }
    /**
     * Test if or operator accepts
     * the correct types
     */


    @Test
    public void test_bool4() throws Exception {
        scanner.check(
                    getParseTree("print(true  or not false); ")
            );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if and operator accepts
     * the correct types
     */


    @Test
    public void test_and1() throws Exception {
        scanner.check(
                getParseTree("print(true  and not false); ")
        );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if and operator rejects
     * the correct types
     */
    @Test
    public void test_and2() throws Exception {
        try {
            scanner.check(
                    getParseTree("print(true  and 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("AND has type mismatch at Line: 1 Character: 6", scanner.getErrors().get(0));

        }
    }
    /**
     * Test if mult operator accepts
     * the correct types
     */


    @Test
    public void test_mult1() throws Exception {
        scanner.check(
                getParseTree("print(6 * 5); ")
        );
        Assert.assertEquals(0, scanner.getErrors().size());
    }
    /**
     * Test if mult operator rejects
     * the correct types
     */
    @Test
    public void test_mult2() throws Exception {
        try {
            scanner.check(
                    getParseTree("print(true * 5); ")
            );
        }
        catch (Exception e){
            Assert.assertEquals(1, scanner.getErrors().size());
            Assert.assertEquals("Multiplication has type mismatch at Line: 1 Character: 6", scanner.getErrors().get(0));

        }
    }









}
