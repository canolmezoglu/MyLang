package ut.pp.compiler.haskell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HaskellProcess {
    /**
     * Class to generate haskell code
     */
    private static final String path = "src/main/java/ut/pp/compiler/haskell/output.hs";
    private static final String sprockellPath = "src/main/java/ut/pp/elaboration/haskell/sprockell-master";
    /**
     * Run sprockell code
     */
    public static List<String> run_Sprockell(){
        try{
            StringBuilder sBuilder = new StringBuilder();
            ProcessBuilder b = new ProcessBuilder();
            // TODO WRITE FOR LINUX
//            b.command("cmd.exe ","/c","runhaskell ",path);
//            Process installSprockell = Runtime.getRuntime().exec("cabal install " +sprockellPath );
//            BufferedReader bReader2 = new BufferedReader(new InputStreamReader(installSprockell.getInputStream()));
//            String line2 = "";
//
//            while ((line2 = bReader2.readLine()) != null) {
//                System.out.println(line2);
//                output.add(line + "\n");

//            }
//            installSprockell.waitFor();
            Process process = Runtime.getRuntime().exec("runhaskell " + path);
//            Process process= b.start();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            List<String> output = new ArrayList<>();
            while ((line = bReader.readLine()) != null) {
                output.add(line );
//                output.add(line + "\n");

            }
            process.waitFor();
            return output;



        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Build haskell code
     * @param p Sprockell instructions
     * @param thread_count tnumber of threads in the program
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
