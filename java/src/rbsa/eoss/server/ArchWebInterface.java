/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Cell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import madkit.kernel.Madkit;
import rbsa.eoss.ArchTradespaceExplorer;
import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
//import madkitdemo3.AgentEvaluationCounter;
import rbsa.eoss.local.Params;

/**
 *
 * @author Bang
 */
public class ArchWebInterface {
    
    private static ArchWebInterface instance = null;
    private Result resu;
    private ArchitectureEvaluator AE;
    private ArchTradespaceExplorer ATE;
    private ResultManager RM;
    private Params params = null;
    private ArchitectureGenerator AG;
    private ArrayList<String> panels;
    private ArrayList<String> objs;
    private ArrayList<String> subobjs;
    private HashMap<String, String> obj_descriptions;
    private HashMap<String, Double> obj_weights;
    private HashMap<String, String> subobj_descriptions;
    private HashMap<String, Double> subobj_weights;
    private boolean initialized;
    private boolean aggregation_initialized;
    
    private ArchWebInterface(){
    
        Madkit kernel;
        String search_clps = "";
        String path = "/Users/bang/workspace/IFEED_RBSA";
        params = new Params(path, "FUZZY-ATTRIBUTES", "test","normal",search_clps);//FUZZY or CRISP

        AE = ArchitectureEvaluator.getInstance();
        RM = ResultManager.getInstance();
        AG = ArchitectureGenerator.getInstance();
        
        initialized = false;
        aggregation_initialized = false;
    }
    
    public void saveRete(){
        AE.setSaveRete();
    }

    public Result evaluateArch(String bitString,int nSats){
        AE.setSaveRete();
        Architecture arch = new Architecture(bitString, nSats);
        resu = AE.evaluateArchitecture(arch, "Slow");
        return resu;
    }
    
    public Result get_firstTestArchResult(){
        AE.setSaveRete();
        Architecture test_arch = AG.getTestArch3();
        resu = AE.evaluateArchitecture(test_arch,"Slow");
        return resu;
    }
    public Result get_secondTestArchResult(){
        AE.setSaveRete();
        Architecture test_arch2 = AG.getTestArch4();
        resu = AE.evaluateArchitecture(test_arch2,"Slow");
        return resu;
    }
    
    public void initialize(){
        if (initialized == true){
            return;
        }
        AE.init(1);
        initialized = true;
    }
    

    public static ArchWebInterface getInstance()
    {
        if( instance == null ) 
        {
            instance = new ArchWebInterface();
        }
        return instance;
    }
    
    public rbsa.eoss.Result getResult(){
        return this.resu;
    }
    
    public ArrayList<String> getPanels(){
        return panels;
    }
    public ArrayList<String> getObjectives(){
        return objs;
    }
    public ArrayList<String> getSubobjectives(){
        return subobjs;
    }
    public HashMap<String,String> getObjDescriptions(){
        return obj_descriptions;
    }
    public HashMap<String,String> getSubobjDescriptions(){
        return subobj_descriptions;
    }
    public HashMap<String,Double> getObjWeights(){
        return obj_weights;
    }
    public HashMap<String,Double> getSubobjWeights(){
        return subobj_weights;
    }
            
    
    
    public void initialize_aggregation_structure () {
         try {
             
            if (aggregation_initialized == true){
                return;
            }
             
            Workbook aggregation_xls = Workbook.getWorkbook( new File( Params.aggregation_xls ) );
  
            
            panels = new ArrayList<String>();
            objs = new ArrayList<String>();
            subobjs = new ArrayList<String>();
            obj_descriptions = new HashMap<>();
            obj_weights = new HashMap<String,Double>();
            subobj_descriptions = new HashMap<>();
            subobj_weights = new HashMap<String,Double>();

            
             Sheet meas = aggregation_xls.getSheet("Aggregation rules");
             
             //Stakeholders
             Cell[] col = meas.getColumn(1);
             int npanels = col.length-3;
             ArrayList<String> panel_names = new ArrayList(npanels);
             ArrayList panel_weights = new ArrayList(npanels);
             ArrayList<Integer> num_objectives_per_panel = new ArrayList<>();
             HashMap subobj_weights_map = new HashMap();
             for(int i = 0;i<npanels;i++) {
                 panel_names.add(meas.getCell(1, i+2).getContents());
                 //Params.panel_weights.add(Double.parseDouble(meas.getCell(3, i+2).getContents()));
                 NumberCell nc = (NumberCell)meas.getCell(3, i+2);
                 panel_weights.add(nc.getValue());
             }
 
             // Objectives
             Cell[] obj_w = meas.getColumn(8);
             Cell[] obj_n = meas.getColumn(6);
             Cell[] obj_d = meas.getColumn(7);
             int i = 3;
             int p = 0;
             //ArrayList<String> obj_descriptions = new ArrayList<>();
             
             while(p<npanels) {            
                 Boolean new_panel = false;
                 ArrayList<Double> obj_weights_p = new ArrayList<>();
                 while(!new_panel) {             
                     //Double weight = Double.parseDouble(obj_w[i].getContents());
                     NumberCell nc2 = (NumberCell) obj_w[i];
                     obj_weights_p.add(nc2.getValue());
                     String obj = obj_n[i].getContents();
                     if(!objs.contains(obj)){
                        objs.add(obj);
                     }
                     obj_descriptions.put(obj,obj_d[i].getContents());
                     obj_weights.put(obj,nc2.getValue());
                     new_panel = obj_d[i+1].getContents().equalsIgnoreCase("");
                     i++;
                     
                 }
//                 obj_weights.add(obj_weights_p);
                 num_objectives_per_panel.add(obj_weights_p.size());

                 p++;
                 i = i + 4;
             }
             panels = panel_names;
             
             //Subobjectives
             p = 0;
             HashMap<String,String> subobjDes = new HashMap<>();
             while(p<npanels) {  
                 Cell[] subobj_w = meas.getColumn(13+p*5);
                 Cell[] subobj_n = meas.getColumn(11+p*5);
                 Cell[] subobj_d = meas.getColumn(12+p*5);
                 //Cell[] subobj_d = meas.getColumn(12*p*5);
                 ArrayList<ArrayList> subobj_weights_p = new ArrayList<ArrayList>();
                 ArrayList subobj_p = new ArrayList(num_objectives_per_panel.get(p));
                 i = 4;
                 int o = 0;
                 while(o<num_objectives_per_panel.get(p)) {
                     Boolean new_obj = false;
                     ArrayList<Double> subobj_weights_o = new ArrayList<Double>();
                     ArrayList subobj_o = new ArrayList();
                     int so = 1;
                     while(!new_obj) {             
                         //Double weight = Double.parseDouble(subobj_w[i].getContents());
                         NumberCell nc3 = (NumberCell) subobj_w[i];
                         double weight = nc3.getValue();
                         subobj_weights_o.add(weight);
                         String subobj_name = panel_names.get(p) + (o+1) + "-" + so;
                         if (!subobjs.contains(subobj_name)){
                             subobjs.add(subobj_name);
                         }
//                         System.out.println(subobj_name + ": " + subobj_d[i].getContents());
                         subobjDes.put(subobj_name, subobj_d[i].getContents());
                         subobj_descriptions.put(subobj_name, subobj_d[i].getContents());
                         subobj_weights.put(subobj_name,weight);
                         subobj_weights_map.put(subobj_name,weight);
                         subobj_o.add(subobj_name);
                         i++;so++;
                         if (i>= subobj_n.length) {
                             new_obj = true;
                         } else {
                             String subobj = subobj_n[i].getContents();
                             new_obj = subobj.equalsIgnoreCase("");
                         }
                         
                     }
                     subobj_weights_p.add(subobj_weights_o);
                     subobj_p.add(subobj_o);
                     o++;
                     i = i + 4;
                 }
                 p++;
             }
      
         } catch (Exception e) {
            System.out.println( "EXC in loadAggregationRules " +e.getMessage() );
        }
         aggregation_initialized = true;
     }
    
    
    
    
}
