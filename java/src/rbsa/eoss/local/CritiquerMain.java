/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.local;

import madkit.kernel.Madkit;
import rbsa.eoss.ArchTradespaceExplorer;
import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
/**
 *
 * @author Bang
 */
public class CritiquerMain {
    
    public static void main(String[] args){
        System.out.println("running CritiquerMain");
        String path = "C:\\Users\\Bang\\Documents\\CritiquerProject";
        
        ArchitectureEvaluator AE = ArchitectureEvaluator.getInstance();
        ArchTradespaceExplorer ATE = ArchTradespaceExplorer.getInstance();
        ResultManager RM = ResultManager.getInstance();
//        AgentEvaluationCounter AEC = AgentEvaluationCounter.getInstance();
        Params params = null;
        Madkit kernel;
        String search_clps = "";

        
        params = new Params( path, "FUZZY-ATTRIBUTES", "test","normal",search_clps);//FUZZY or CRISP
        AE.init(1);
        
        ArchitectureGenerator AG = ArchitectureGenerator.getInstance();
        
        Architecture test_arch = AG.getTestArch3();
    //    Architecture test_arch = AG.getUserEnteredArch();
        
        Result result1 = AE.evaluateArchitecture(test_arch,"Slow");
        
        
        /*
        int inst_num = test_arch_anal.getTotalInstruments();
        System.out.println("Number of total Instruments:" + inst_num);
        try{
            Rete r = new Rete();
            r.eval("(watch facts)");
            r.eval("(reset)");
            r.eval("(defrule inst-num-check (arch ?inst-num&:(> ?inst-num 9)) => (printout t \" TOO MANY INSTRUMENTS \" crlf ))");
            r.eval("(assert (arch " +inst_num +"))");
            r.eval("(run)");
        }catch (Exception e) { System.out.println(e);
        }*/
        System.out.println("DONE");
    }
}
