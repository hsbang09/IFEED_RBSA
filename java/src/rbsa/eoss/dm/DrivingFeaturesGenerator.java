

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rbsa.eoss.dm;


import rbsa.eoss.*;
import java.util.ArrayList;
//import weka.gui.treevisualizer.PlaceNode2;
//import weka.gui.treevisualizer.TreeVisualizer;
//import weka.core.converters.ConverterUtils.DataSink;
//import weka.core.converters.CSVSaver;
//import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.BitSet;
import java.util.List;

import rbsa.eoss.local.Params;


import org.jblas.DoubleMatrix;

/**
 *
 * @author Bang
 */
public class DrivingFeaturesGenerator {

    private String scope;
    private DBQueryBuilder dbquery;
    private ArrayList<Integer> behavioral;
    private ArrayList<Integer> non_behavioral;
    private ArrayList<Integer> population;
    
    private double supp_threshold;
    private double conf_threshold;
    private double lift_threshold;    
        
    private FilterExpressionHandler feh;
    
    private double ninstr;
    private double norb;
    private ArrayList<DrivingFeature> drivingFeatures;
    private double[][] drivingFeaturesMatrix;
    private double[] labelArray;
    
    private int numIntervals=3;

    private String[] exclude_general_array = {"ArchID"}; 
    // _id, factName, factID, module, factHistory are filtered in DBQueryBuilder.getSlotNames(); 
    private String[] exclude_cost_array = {"payload-dimensions",
                    "payload-dimensions#","instruments","updated","updated2",
                    "ADCS-requirement","ADCS-type","worst-sun-angle","residual_dipole",
                    "satellite-dimensions","select-orbit","deorbiting-strategy","delta-V-deorbit",
                    "slew-angle","orbit-altitude","delta-V-injection","depth-of-discharge",
                    "drag-coefficient","fraction-sunlight","launch-vehicle",
                    "orbit-eccentricity","orbit-inclination","orbit-string","orbit-type"};
    private String[] exclude_aggregation_array ={"index","subobj-scores","weights","subobj-scores"};
    private String[] exclude_science_mission_array = {"Name","slew-angle","select-orbit",
                        "propellant-injection","orbit-type",
                        "payload-dimensions","payload-dimensions#","instruments"};
    private String[] exclude_science_measurement_array = {"taken-by","Id","Horizontal-Spatial-Resolution","orbit-string","Parameter"};
    private String[] exclude_science_capabilities_array = {"has-deployment-mechanism","High-lat-sensitivity",
                    "Geometry","flies-in","Day-Night","dimension-x#","dimension-z#","duty-cycle#","dimension-y#",
                    "developed-by","frequency#","duty-cycle#","Concept","num-of-plnaes#",
                    "num-of-sats-per-plane#",
                    "Name","mission-architecture","measurement-ids","lifetime","launch-date","Intent","inherited"};
    
    private ArrayList<String> exclude_general; 
    private ArrayList<String> exclude_cost;
    private ArrayList<String> exclude_aggregation;
    private ArrayList<String> exclude_science_mission;
    private ArrayList<String> exclude_science_measurement;
    private ArrayList<String> exclude_science_capabilities;
    
    private ArrayList<String> userDefFeatures;
    private ArrayList<Integer> removedFeatures;
    
    public DrivingFeaturesGenerator(){
        this.dbquery = new DBQueryBuilder();
    }
    public DrivingFeaturesGenerator(DBQueryBuilder dbq){
        this.dbquery = dbq;
    }


    
    public void initialize(String scope, ArrayList<Integer> behavioral, ArrayList<Integer> non_behavioral, 
                                double supp, double conf, double lift, int numIntervals){
        
        this.scope = scope;
        this.supp_threshold=supp;
        this.conf_threshold=conf;
        this.lift_threshold=lift;
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;
        this.population = new ArrayList<>();
        for(int i:behavioral){
            population.add(i);
        }
        for(int i:non_behavioral){
            population.add(i);
        }        
        drivingFeatures = new ArrayList<>();
        feh = new FilterExpressionHandler(this.dbquery);
        
        this.ninstr = Params.instrument_list.length;
        this.norb = Params.orbit_list.length;
        
        exclude_general = new ArrayList<String>(Arrays.asList(exclude_general_array));   
        exclude_cost = new ArrayList<String>(Arrays.asList(exclude_cost_array)); 
        exclude_science_mission = new ArrayList<String>(Arrays.asList(exclude_science_mission_array));
        exclude_science_measurement = new ArrayList<String>(Arrays.asList(exclude_science_measurement_array));
        exclude_science_capabilities = new ArrayList<String>(Arrays.asList(exclude_science_capabilities_array));
        
        userDefFeatures = new ArrayList<>();
        removedFeatures = new ArrayList<>();   
        this.numIntervals = numIntervals;
    }
    
 

    public ArrayList<DrivingFeature> getPrimitiveDrivingFeatures(){
        
        // Assign unique id numbers for each driving feature
        int dfid = 0;
        
        // Get all archIDs
        ArrayList<Integer> allArchIDList = dbquery.getAllArchIDs(scope);          
        // Get unique set of IDs
                
        ArrayList<String> candidate_features = new ArrayList<>();        
        ArrayList<int[]> satList = new ArrayList<>();
        
        if(scope.equalsIgnoreCase("design_input")){
            // Input variables
            // present, absent, inOrbit, notInOrbit, together2, togetherInOrbit2
            // separate2, separate3, together3, togetherInOrbit3, emptyOrbit
            // numOrbits, numOfInstruments, subsetOfInstruments
            
            // Preset filter expression example:
            // {presetName[orbits;instruments;numbers]}    
                        
            for(int i=0;i<ninstr;i++){
                // present, absent
                candidate_features.add("{present[;"+i+";]}");
                candidate_features.add("{absent[;"+i+";]}");
                
                for(int j=0;j<norb+1;j++){
                    // numOfInstruments (number of specified instruments across all orbits)
                    candidate_features.add("{numOfInstruments[;"+i+";"+j+"]}");
                }                
                
                for(int j=0;j<i;j++){
                    // together2, separate2
                    candidate_features.add("{together[;"+i+","+j+";]}");
                    candidate_features.add("{separate[;"+i+","+j+";]}");
                    for(int k=0;k<j;k++){
                        // together3, separate3
                        candidate_features.add("{together[;"+i+","+j+","+k+";]}");
                        candidate_features.add("{separate[;"+i+","+j+","+k+";]}");
                    }
                }
            }
            for(int i=0;i<norb;i++){
                for(int j=1;j<9;j++){
                    // numOfInstruments (number of instruments in a given orbit)
                    candidate_features.add("{numOfInstruments["+i+";;"+j+"]}");
                }
                // emptyOrbit
                candidate_features.add("{emptyOrbit["+i+";;]}");
                // numOrbits
                candidate_features.add("{numOrbits[;;"+i+1+"]}");
                for(int j=0;j<ninstr;j++){
                    // inOrbit, notInOrbit
                    candidate_features.add("{inOrbit["+i+";"+j+";]}");
                    candidate_features.add("{notInOrbit["+i+";"+j+";]}");
                    for(int k=0;k<j;k++){
                        // togetherInOrbit2
                        candidate_features.add("{togetherInOrbit["+i+";"+j+","+k+";]}");
                        for(int l=0;l<k;l++){
                            // togetherInOrbit3
                            candidate_features.add("{togetherInOrbit["+i+";"+j+","+k+","+l+";]}");
                        }
                    }
                }
            }
            for(int i=0;i<16;i++){
                // numOfInstruments (across all orbits)
                candidate_features.add("{numOfInstruments[;;"+i+"]}");
            }
            
            for(String feature:candidate_features){
                
//                String feature_expression_inside = feature.substring(1,feature.length()-1);
//                String name = feature_expression_inside.split("\\[")[0];
//                
//                ArrayList<Integer> matchedArchIDs = feh.processSingleFilterExpression(feature_expression_inside,true);
//                double[] metrics = this.computeMetrics(matchedArchIDs);
//                
//                if(metrics[0]>supp_threshold && metrics[1] > lift_threshold && metrics[2] > conf_threshold && metrics[3] > conf_threshold){
//                    drivingFeatures.add(new DrivingFeature(dfid, name,feature,metrics,true));
//                    dfid++;
//                    int[] satArray = satisfactionArray(matchedArchIDs,population); 
//                    satList.add(satArray);
//                }
            }     

        }else{
            // Facts in database
            
            // How should candidate features defined here?
            // Each feature should contain some information about the number of instances of facts
            // and about the slot values. Numeric variables (continuous) need to be discretized first.
            
            // Number of instances of Facts
            // a) no occurance
            // b) at least 1
            // c) at least 2
            // d) at least 3
            // e) less than 2
            // f) less than 3
            // g) more than 6 (for aggregation facts only)
            // h) more than 9 (for aggregation facts only)
            
            // Examples of feature expressions
            // String: "{collectionName:gt[0],slotName:"String"}"
            // Double: "{collectionName:gt[0],slotName:[minVal,maxVal]}"
            // Double: "{collectionName:gt[0],slotName:[,maxVal]}"
            // Conditions put on multiple slots: "{collectionName:gt[0],slotName:[minVal;],slotName:String}"
                        
            
            if(scope.contains("AGGREGATION")){
                ArrayList<String> slots = new ArrayList<>();
                slots.add("id");
                if(!scope.contains("STAKEHOLDER")){
                    slots.add("parent");
                }
                for(String slot:slots){
                    ArrayList<String> validValues = dbquery.getValidValueList(scope, slot);
                    for(String val:validValues){
                        // Discretize continuous space (5 intervals)
                        ArrayList<double[]> thresholds = this.discretize_continuous_range(0.0,1.0,numIntervals);
                        for(int i=0;i<thresholds.size();i++){
                            String expression;
                            double[] th = thresholds.get(i);
                            if(i==0){
                                expression = "satisfaction:[;"+th[0]+"]";
                            }else if(i==thresholds.size()-1){
                                expression = "satisfaction:["+th[0]+";]";
                            }else{
                                expression = "satisfaction:["+th[0]+";"+th[1]+"]";
                            }
                            expression = expression +","+ slot+ ":" + val;
                            candidate_features.add(expression);
                        }
                    }        

                }
            }else{
                ArrayList<String> allSlots = dbquery.getSlotNames(scope);
                
                // Generate candidate features for all slots
                for(String slot:allSlots){

                    if(exclude_general.contains(slot)){
                        continue;
                    }else if(scope.equalsIgnoreCase("cost.MANIFEST.Mission") && exclude_cost.contains(slot)){
                        continue;
                    }else if(scope.equalsIgnoreCase("science.MANIFEST.Mission") && exclude_science_mission.contains(slot)){
                        continue;
                    }else if(scope.equalsIgnoreCase("science.REQUIREMENTS.Measurement")&& exclude_science_measurement.contains(slot)){
                        continue;
                    }else if(scope.equalsIgnoreCase("science.CAPABILITIES.Manifested_instrument")&& exclude_science_capabilities.contains(slot)){
                        continue;
                    }
                    String[] minmax = dbquery.getMinMaxValue(scope, slot);
                    if(minmax[0]==null){ // The class of a given slot value is java.lang.String   
                        ArrayList<String> validValues = dbquery.getValidValueList(scope, slot);


                        if(validValues.size()==1){
                            continue;
                        }else if(validValues.size() > 12){
                            continue;
                        }

                        for(String val:validValues){
                            String expression = slot+":"+val;
                            candidate_features.add(expression);
                        }                    
                    }else{ // The class of a given slot value is java.lang.Double
                        Double min = Double.parseDouble(minmax[0]);
                        Double max = Double.parseDouble(minmax[1]);

                        if(!Objects.equals(max, min)){
                        } else { // min and max equal
                            continue;
                        }
                        // Discretize continuous space (3 intervals)
                        ArrayList<double[]> thresholds = this.discretize_continuous_range(min, max, numIntervals);
                        for(int i=0;i<thresholds.size();i++){
                            
                            String expression;
                            double[] th = thresholds.get(i);
                            if(i==0){
                                double roundOff = Math.round(th[0] * 100.0) / 100.0;
                                expression = slot+":[;"+roundOff+"]";
                            }else if(i==thresholds.size()-1){
                                double roundOff = Math.round(th[0] * 100.0) / 100.0;
                                expression = slot+":["+roundOff+";]";
                            }else{
                                double roundOff1 = Math.round(th[0] * 100.0) / 100.0;
                                double roundOff2 = Math.round(th[1] * 100.0) / 100.0;
                                expression = slot+":["+roundOff1+";"+roundOff2+"]";
                            }
                            
                            candidate_features.add(expression);
                        }
                    }
                }            
            }

            
            // For each candidate_feature(defined for each slot), make a query and count the number of instances of Facts
            for(String slot_conditions:candidate_features){
                // Examples of slot_condition
                // slotName:String
                // slotName:[minVal;maxVal]
                // slotName:[;maxVal]
                // slotName:[minVal;]
                // slotName:[val], slotName:[val2]
                
                String[] slot_conditions_split = slot_conditions.split(",");

                ArrayList<String> slotNames = new ArrayList<>();
                ArrayList<String> conditions = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();
                ArrayList<String> valueTypes = new ArrayList<>();

                for(String slot_condition:slot_conditions_split){

                    String[] exp_split = slot_condition.split(":");
                    String slotName = exp_split[0];
                    if(exp_split.length==1){
                    }
                    String arguments = exp_split[1];

                    // Set up conditions to make a query
                    if(arguments.startsWith("[")){ // Numeric argument
                        // Remove square brackets
                        arguments = arguments.substring(1,arguments.length()-1); 

                        if(arguments.contains(";")){
                            // Range given
                            String[] argSplit = arguments.split(";");
                            if(argSplit[0].isEmpty()){ // Only max value specified
                                slotNames.add(slotName);
                                conditions.add("lt");
                                values.add(argSplit[1]);
                                valueTypes.add("Double");
                            }else if(argSplit.length==1){ // Only min value specified
                                slotNames.add(slotName);
                                conditions.add("gt");
                                values.add(argSplit[0]);
                                valueTypes.add("Double");
                            }else{ // Both min and max values specified
                                slotNames.add(slotName);
                                conditions.add("gt");
                                values.add(argSplit[0]);
                                valueTypes.add("Double");
                                slotNames.add(slotName);
                                conditions.add("lt");
                                values.add(argSplit[1]);
                                valueTypes.add("Double");                            
                            }
                        }else{
                            // Exact value given
                                slotNames.add(slotName);
                                conditions.add("eq");
                                values.add(arguments);
                                valueTypes.add("Double");
                        }
                    }else{ // String argument
                        slotNames.add(slotName);
                        conditions.add("eq");
                        values.add(arguments);
                        valueTypes.add("String");
                    }
                }

                
                // Make a query on Facts and get the ID's of architectures that contain those Facts
                ArrayList<Integer> matchedArchIDs = dbquery.makeQuery_ArchID(scope, slotNames, conditions, values, valueTypes);

                // FactCounter counts the number of Facts corresponding to each architecture
                HashMap<Integer,Integer> FactCounter = new HashMap<>();
                
                
                for(int id:population){
                    FactCounter.put(id,0);
                }
                for(int id:matchedArchIDs){
                    if(population.contains(id)){
                        // Keys are unique architecture ID's
                        int cnt = FactCounter.get(id);
                        FactCounter.put(id, cnt+1);
                    }
                }   

                // Generate conditions on the number of instances of jess Fact
                ArrayList<String> numOfInstance_conditions = new ArrayList<>();

                
                if(scope.contains("AGGREGATION")){
                    numOfInstance_conditions.add("eq[0]"); // No occurance
                    numOfInstance_conditions.add("gt[0]"); // At least 1
                    numOfInstance_conditions.add("gt[1]"); // At least 2
                    numOfInstance_conditions.add("gt[2]"); // At least 3
                    numOfInstance_conditions.add("lt[2]"); // less than 2
                    numOfInstance_conditions.add("gt[6]"); // more than 6
                    numOfInstance_conditions.add("gt[9]"); // more than 9   
                    numOfInstance_conditions.add("gt[12]");
                    //numOfInstance_conditions.add("lt[3]"); // less than 3
                }else{
                    numOfInstance_conditions.add("eq[0]");
                    numOfInstance_conditions.add("allBut1[]");
                    numOfInstance_conditions.add("all[]");
                }
                 
                // Generate driving features for each condition
                for(String cond:numOfInstance_conditions){
                    // Examples of feature expressions
                    // Variable in String: "{collectionName:gt[0],slotName:String}"
                    // Variable in Double: "{collectionName:gt[0],slotName:[minVal,maxVal]}"
                    // Variable in Double: "{collectionName:gt[0],slotName:[,maxVal]}"
                    String feature_expression = "{"+scope+":"+cond+","+slot_conditions+"}";
                    String feature_name = scope + ":" + slotNames.get(0);                    
                    
                    String inequalitySign = cond.split("\\[")[0];
                    int argument=0;
                    double[] metrics;
                    if(inequalitySign.contains("all")){
                        metrics = computeMetrics(FactCounter, allArchIDList, inequalitySign);
                    }else{
                        argument = Integer.parseInt(cond.substring(0,cond.length()-1).split("\\[")[1]);
                        metrics = computeMetrics(FactCounter,inequalitySign,argument);
                    }

                    if(metrics[0]>supp_threshold){
                        
                        if(inequalitySign.contains("all")){
                            matchedArchIDs = new ArrayList<>();
                            if(inequalitySign.equals("all")){                                
                                boolean pass = false;
                                for(int id:this.population){
                                    pass=FactCounter.get(id)==Collections.frequency(allArchIDList,id);
                                    if(pass){
                                        matchedArchIDs.add(id);
                                    }
                                }                                      
                            }else{
                                boolean pass = false;
                                for(int id:this.population){
                                    pass=FactCounter.get(id)==Collections.frequency(allArchIDList,id)-1;
                                    if(pass){
                                        matchedArchIDs.add(id);
                                    }
                                }                                      
                            }
                        }else{
                            boolean pass;
                            for(int id:this.population){
                                pass = feh.compare_number(FactCounter.get(id),inequalitySign,argument);
                                if(pass){
                                    matchedArchIDs.add(id);
                                }
                            }  
                        }                  
                        BitSet bs = satisfactionBitSet(matchedArchIDs,population);
                        drivingFeatures.add(new DrivingFeature(feature_name,bs));
                        dfid++;
                    }                       
                }
            }            
        }
        
//        // Test the user-defined features
//        if(!this.userDefFeatures.isEmpty()){
//            for(String exp:this.userDefFeatures){
//                if(exp.isEmpty()){
//                    continue;
//                }
//                ArrayList<Integer> matchedArchIDs = feh.processFilterExpression(exp, new ArrayList<Integer>(), "||");
//                double[] metrics = this.computeMetrics(matchedArchIDs);
//                if(metrics[0]>supp_threshold && metrics[1] > lift_threshold && metrics[2] > conf_threshold && metrics[3] > conf_threshold){
//                    drivingFeatures.add(new DrivingFeature(dfid,exp,exp,metrics,false));
//                    dfid++;
//                    int[] satArray = satisfactionArray(matchedArchIDs,population); 
//                    satList.add(satArray);
//                }             
//            }
//        }
        
        return this.drivingFeatures;
    }
    

    
    
    /**
     * Runs Apriori and returns the top n features discovered from Apriori. Features are ordered by fconfidence in descending order.
     * @return 
     */
    public List<DrivingFeature> getDrivingFeatures() {

        BitSet labels = new BitSet(population.size());
        for (int i = 0; i < population.size(); i++) {
            if (behavioral.contains(population.get(i))) {
                labels.set(i, true);
            }
        }
        
        Apriori ap = new Apriori(population.size(), this.drivingFeatures);
                
        ap.run(labels, this.supp_threshold, this.conf_threshold, 2);

        return ap.getTopFeatures(1000, DrivingFeaturesParams.metric);
    }



    
    private ArrayList<double[]> discretize_continuous_range(double min, double max, int n){
        // Discretize continuous vars into discrete ranges 
        ArrayList<double[]> thresholds = new ArrayList<>();
        double interval = (max-min)/n;
        for(int i=0;i<n;i++){
            double[] range;
            if(i==0){
                range = new double[1];
                range[0]=min + interval*(i+1);
            }else if(i==n-1){
                range= new double[1];
                range[0]=min + interval*i;
            }else{
                range = new double[2];
                range[0] = min + interval * i;
                range[1] = min + interval * (i+1);
            }
            thresholds.add(range);
        }
        return thresholds;
    }
       
    
    
    private int[] satisfactionArray(ArrayList<Integer> matchedArchIDs, ArrayList<Integer> allArchIDs){
        int[] satArray = new int[allArchIDs.size()];
        for(int i=0;i<allArchIDs.size();i++){
            int id = allArchIDs.get(i);
            if(matchedArchIDs.contains(id)){
                satArray[i]=1;
            }else{
                satArray[i]=0;
            }
        }
        return satArray;
    }
    
    
    private BitSet satisfactionBitSet(ArrayList<Integer> matchedArchIDs, ArrayList<Integer> allArchIDs){
        
        BitSet bs = new BitSet(allArchIDs.size());
        for(int i=0;i<allArchIDs.size();i++){
            int id = allArchIDs.get(i);
            if(matchedArchIDs.contains(id)){
                bs.set(i);
            }
        }
        return bs;
    }
    
    
    
    private double[] computeMetrics(HashMap<Integer,Integer> FactCounter, ArrayList<Integer> allArchIDList, String type){
        
    	double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;
        
        boolean pass = false;
        for(int id:this.population){
                if(type.equals("all")){
                    pass=FactCounter.get(id)==Collections.frequency(allArchIDList,id);
                }else if(type.equals("allBut1")){
                    pass=FactCounter.get(id)==Collections.frequency(allArchIDList,id)-1;
                }
            if(pass){
                cnt_F++;
                if(behavioral.contains(id)){cnt_SF++;}
            }
        }        
        double cnt_NS = cnt_all-cnt_S;
        double cnt_NF = cnt_all-cnt_F;
        double cnt_S_NF = cnt_S-cnt_SF;
        double cnt_F_NS = cnt_F-cnt_SF;
        
    	double[] metrics = new double[4];
        double support = cnt_SF/cnt_all;
        double support_F = cnt_F/cnt_all;
        double support_S = cnt_S/cnt_all;
        double lift=0;
        double conf_given_F=0;
        if(cnt_F!=0){
            lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
            conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        }
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;
    }        
  
    private double[] computeMetrics(HashMap<Integer,Integer> FactCounter, String inequalitySign, int argument){
        
    	double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;
        
        boolean pass;
        for(int id:this.population){
            pass = feh.compare_number(FactCounter.get(id),inequalitySign,argument);
            if(pass){
                cnt_F++;
                if(behavioral.contains(id)){cnt_SF++;}
            }
        }        
        double cnt_NS = cnt_all-cnt_S;
        double cnt_NF = cnt_all-cnt_F;
        double cnt_S_NF = cnt_S-cnt_SF;
        double cnt_F_NS = cnt_F-cnt_SF;
        
    	double[] metrics = new double[4];
        double support = cnt_SF/cnt_all;
        double support_F = cnt_F/cnt_all;
        double support_S = cnt_S/cnt_all;
        double lift=0;
        double conf_given_F=0;
        if(cnt_F!=0){
            lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
            conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        }
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;
    }    

    private double[] computeMetrics(ArrayList<Integer> matchedArchIDs){
        
        if (matchedArchIDs.isEmpty()){
            double[] metrics = {0,0,0,0};
            return metrics;
        }

        double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;

        // Need to count cnt_SF and cnt_F
        for(int id:matchedArchIDs){
            cnt_F++;
            if(behavioral.contains(id)){
                cnt_SF++;
            }
        }
        double cnt_NS = cnt_all-cnt_S;
        double cnt_NF = cnt_all-cnt_F;
        double cnt_S_NF = cnt_S-cnt_SF;
        double cnt_F_NS = cnt_F-cnt_SF;
        
    	double[] metrics = new double[4];
    	
        double support = cnt_SF/cnt_all;
        double support_F = cnt_F/cnt_all;
        double support_S = cnt_S/cnt_all;
        
        double lift=0;
        double conf_given_F=0;
        if(cnt_F!=0){
            lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
            conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        }
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;
    }  
    



    
    
    public int[][] booleanToInt(boolean[][] b) {
        int[][] intVector = new int[b.length][b[0].length]; 
        for(int i = 0; i < b.length; i++){
            for(int j = 0; j < b[0].length; ++j) intVector[i][j] = b[i][j] ? 1 : 0;
        }
        return intVector;
    }

    
    public void setRemovedFeatures(int[] removed){
        removedFeatures = new ArrayList<>();
        for(int r:removed){
            removedFeatures.add(r);
        }
    }
    public void setUserDefFeatures(String[] userdef){
        userDefFeatures = new ArrayList<>();
        for(String s:userdef){
            if(!s.isEmpty()){
                userDefFeatures.add(s);
            }
        }
    }
    
    public void setThresholds(double supp, double conf, double lift){
        this.supp_threshold=supp;
        this.conf_threshold=conf;
        this.lift_threshold=lift;
    }
    

}
