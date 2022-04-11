package ut.pp.elaboration.haskell;

import java.io.*;

public class HaskellProcess {
    /**
     * Class to generate haskell code
     */
    private static final String path = "src/main/java/ut/pp/elaboration/haskell/output.hs";

    /**
     * Run sprockell code
     */
    public static void run_Sprockell(){
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
     * @param thread_count
     */
    public static  void build_Sprockell(String p, int thread_count,boolean debug){
        try {
            FileWriter fileWriter = new FileWriter(path);
            String runWithDebug = debug ? "runWithDebugger (debuggerSimplePrint myShow) " : "run";
            StringBuilder setup = new StringBuilder("import Sprockell\n" +
                    "prog :: [Instruction]\n" + "prog = ["+
                    p + "]"+"\n\n" + "main = "   +runWithDebug +"[prog");
            for(int i = 0; i<thread_count;i++){
                setup.append(",prog");
            }
            setup.append("]");
            fileWriter.write(setup.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
