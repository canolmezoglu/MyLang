package ut.pp.elaboration.haskell;
import ut.pp.elaboration.model.Sprockell;

import java.io.*;

public class HaskellProcess {
    /**
     * Class to generate haskell code
     */
    private static final String sourcePath = "src/haskell/sprockelConverter.hs";
    private static final String path = "src/haskell/output.hs";

    public static void runSprockel(){
        try{
            ProcessBuilder b = new ProcessBuilder();
            b.command("cmd.exe ","/c","runhaskell ",path);
            b.inheritIO();
            b.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build haskell code
     * @param p
     */
    public static  void build (Sprockell p){
        try {
            FileWriter fileWriter = new FileWriter(path);
            StringBuilder setup = new StringBuilder("import Sprockell\n" +
                    "prog :: [Instruction]\n" +
                    p + "\n\n" + "main = run[prog ");
            setup.append("]");
            fileWriter.write(setup.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
