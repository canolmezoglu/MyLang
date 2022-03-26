package ut.pp.elaboration;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ut.pp.parser.MyLangBaseListener;
import org.antlr.v4.runtime.tree.ParseTree;
import ut.pp.parser.MyLangParser;

import java.util.List;

public class Checker extends MyLangBaseListener {
    private List<String> errors;
    private Result result;
    public Result check (ParseTree tree) throws Exception {
        new ParseTreeWalker().walk(this, tree);
        if (!this.errors.isEmpty()){
            throw new Exception(this.errors.toString());
        }
        return this.result;
    }
    @Override
    public void ExitProgram(MyLangParser.InstructionContext ctx){


    }


}
