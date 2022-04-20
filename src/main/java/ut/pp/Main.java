package ut.pp;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;

import ut.pp.compiler.Scanner;
import ut.pp.compiler.CodeGeneration;
import ut.pp.compiler.haskell.HaskellProcess;
import ut.pp.compiler.model.Instruction;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        /**
         * Parsing, Code generation and running sprockell code
         * Raise exception and show errors in code if there are any (excluding syntax errors) , in that case - no code generation
         * Generated Sprockell code can be seen in elaboration/haskell/output.hs
         */
        String path ="src/main/sample/compiler.txt";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromPath(new File(path).toPath()));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        CodeGeneration c = new CodeGeneration();
        Scanner scanner = new Scanner();
        scanner.check(tree);
        int thread_count = Scanner.getNumberOfThreads(tree);
        if(scanner.getErrors().size()==0) {
            List<Instruction> instructions = c.genCode(tree);
            String sprockell_code = "";
            for (int i = 0; i < instructions.size(); i++) {
                if (i == 0) {
                    sprockell_code = instructions.get(i).toString();
                } else {
                    sprockell_code = sprockell_code + ',' + instructions.get(i).toString();
                }
            }
            HaskellProcess.build_Sprockell(sprockell_code,thread_count,false
            );
            String can  = HaskellProcess.run_Sprockell().toString();
            System.out.println(can);
        }


    }
    public static List<String> runSprockell(String filename) throws Exception {
        /**
         * Parsing, Code generation and running sprockell code
         * Raise exception and show errors in code if there are any (excluding syntax errors) , in that case - no code generation
         * Generated Sprockell code can be seen in elaboration/haskell/output.hs
         */
        String path ="src/main/sample/"+ filename +".txt";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromPath(new File(path).toPath()));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        CodeGeneration c = new CodeGeneration();
        Scanner scanner = new Scanner();
        scanner.check(tree);
        int thread_count = Scanner.getNumberOfThreads(tree);
        if(scanner.getErrors().size()==0) {
            List<Instruction> instructions = c.genCode(tree);
            String sprockell_code = "";
            for (int i = 0; i < instructions.size(); i++) {
                if (i == 0) {
                    sprockell_code = instructions.get(i).toString();
                } else {
                    sprockell_code = sprockell_code + ',' + instructions.get(i).toString();
                }
            }
            HaskellProcess.build_Sprockell(sprockell_code, thread_count, false);
            return  HaskellProcess.run_Sprockell();
        }
        return null;

    }
}
