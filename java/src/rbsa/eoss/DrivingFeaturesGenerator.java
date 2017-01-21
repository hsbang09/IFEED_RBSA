

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rbsa.eoss;


import java.util.ArrayList;
//import weka.gui.treevisualizer.PlaceNode2;
//import weka.gui.treevisualizer.TreeVisualizer;
//import weka.core.converters.ConverterUtils.DataSink;
//import weka.core.converters.CSVSaver;
//import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import rbsa.eoss.local.Params;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Bang
 */
public class DrivingFeaturesGenerator {

    private String scope;
    private DBQueryBuilder dbquery;
    private ArrayList<Integer> behavioral;
    private ArrayList<Integer> non_behavioral;
    
    private double supp_threshold;
    private double confidence_threshold;
    private double lift_threshold;    
        
    private FilterExpressionHandler feh;
    
    private int[][] dataFeatureMat;
    private double ninstr;
    private double norb;
    private ArrayList<DrivingFeature> drivingFeatures;
    private ArrayList<DrivingFeature> userDef;
    

    public DrivingFeaturesGenerator(){
    }
    
    
    public void initialize(String scope, ArrayList<Integer> behavioral, ArrayList<Integer> non_behavioral, 
                                double supp, double conf, double lift){
        
        this.scope = scope;
        this.supp_threshold=supp;
        this.confidence_threshold=conf;
        this.lift_threshold=lift;
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;
        drivingFeatures = new ArrayList<>();
        this.dbquery = new DBQueryBuilder();
        feh = new FilterExpressionHandler(this.dbquery);
    }
    
 

    public ArrayList<DrivingFeature> getDrivingFeatures (){
        
        ArrayList<String> candidate_features = new ArrayList<>();
        
        if(scope.equalsIgnoreCase("input_variables")){
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
                String feature_expression_inside = feature.substring(1,feature.length()-1);
                String name = feature_expression_inside.split("\\[")[0];
                ArrayList<Integer> matchedArchIDs = feh.processSingleFilterExpression(feature,true);
                double[] metrics = this.computeMetrics(matchedArchIDs);
                drivingFeatures.add(new DrivingFeature(name,feature, metrics,false));
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
            // {presetName[orbits;instruments;numbers]}    
            
            
            ArrayList<String> allSlots = dbquery.getSlotNames(scope);
            
            // Generate candidate features for all slots
            for(String slot:allSlots){
                String[] minmax = dbquery.getMinMaxValue(scope, slot);
                
                if(minmax[0]==null){ // The class of a given slot value is java.lang.String   
                    ArrayList<String> validValues = dbquery.getValidValueList(scope, slot);
                    for(String val:validValues){
                        String expression = slot+":"+val;
                        candidate_features.add(expression);
                    }                    
                }else{ // The class of a given slot value is java.lang.Double
                    Double min = Double.parseDouble(minmax[0]);
                    Double max = Double.parseDouble(minmax[1]);
                    
                    // Discretize continuous vars into three ranges: low, mid, high
                    Double threshold1 = (max-min)/3 + min;
                    Double threshold2 = (max-min)/3*2 + min;
                    
                    String low = slot+":[,"+threshold1+"]";
                    String mid = slot+":["+threshold1+","+threshold2+"]";
                    String high = slot+":["+threshold2+",]";
                    candidate_features.add(low);
                    candidate_features.add(mid);
                    candidate_features.add(high);
                }
            }
            
            
            // For each candidate_feature(defined for each slot), make a query and count the number of instances of Facts
            for(String slot_condition:candidate_features){
                // Examples of slot_condition
                // slotName:String
                // slotName:[minVal,maxVal]
                // slotName:[,maxVal]
                // slotName:[val]
                
                ArrayList<String> slotNames = new ArrayList<>();
                ArrayList<String> conditions = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();
                ArrayList<String> valueTypes = new ArrayList<>();

                String[] exp_split = slot_condition.split(":");
                String slotName = exp_split[0];
                String arguments = exp_split[1];
                
                // Set up conditions to make a query
                if(arguments.startsWith("[")){ // Numeric argument
                    // Remove square brackets
                    arguments = arguments.substring(1,arguments.length()-1); 
                    
                    if(arguments.contains(",")){
                        // Range given
                        String[] argSplit = arguments.split(",");
                        if(argSplit[0]==null){ // Only max value specified
                            slotNames.add(slotName);
                            conditions.add("lt");
                            values.add(argSplit[1]);
                            valueTypes.add("Double");
                        }else if(argSplit[1]==null){ // Only min value specified
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
                // Make a query on Facts and get the ID's of architectures that contain those Facts
                ArrayList<Integer> matchedArchIDs = dbquery.makeQuery_ArchID(scope, slotNames, conditions, values, valueTypes);
                
                // FactCounter counts the number of Facts corresponding to each architecture
                HashMap<Integer,Integer> FactCounter = new HashMap<>();
                for(int id:matchedArchIDs){
                    // Keys are unique architecture ID's
                    if(FactCounter.containsKey(id)){
                        int cnt = FactCounter.get(id);
                        FactCounter.put(id, cnt+1);
                    }else{
                        FactCounter.put(id, 1);
                    }
                }

                // Generate conditions on the number of instances of jess Fact
                ArrayList<String> numOfInstance_conditions = new ArrayList<>();
                numOfInstance_conditions.add("eq[0]"); // No occurance
                numOfInstance_conditions.add("gt[0]"); // At least 1
                numOfInstance_conditions.add("gt[1]"); // At least 2
                numOfInstance_conditions.add("gt[2]"); // At least 3
                numOfInstance_conditions.add("lt[2]"); // less than 2
                numOfInstance_conditions.add("lt[3]"); // less than 3
                numOfInstance_conditions.add("gt[6]"); // more than 6
                numOfInstance_conditions.add("gt[9]"); // more than 9                    

                // Generate driving features for each condition
                for(String cond:numOfInstance_conditions){
                    String inequalitySign = cond.split("\\[")[0];
                    int argument = Integer.parseInt(cond.substring(0,cond.length()-1).split("\\[")[1]);
                    
                    double[] metrics = computeMetrics(FactCounter,inequalitySign,argument);
                    // Examples of feature expressions
                    // Variable in String: "{collectionName:gt[0],slotName:String}"
                    // Variable in Double: "{collectionName:gt[0],slotName:[minVal,maxVal]}"
                    // Variable in Double: "{collectionName:gt[0],slotName:[,maxVal]}"
                    String feature_expression = "{"+scope+":"+cond+","+slot_condition+"}";
                    String feature_name = feature_expression.substring(1,feature_expression.length()-1);
                    drivingFeatures.add(new DrivingFeature(feature_name,feature_expression, metrics,false));
                }
            }            
        }
        return this.drivingFeatures;
    }
        
  
  
    private double[] computeMetrics(HashMap<Integer,Integer> FactCounter, String inequalitySign, int argument){
        
    	double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;
        
        boolean pass;
        for(int uniqueArchID:FactCounter.keySet()){
            pass = feh.compare_number(FactCounter.get(uniqueArchID),inequalitySign,argument);
            if(pass){
                cnt_F++;
                if(behavioral.contains(uniqueArchID)){cnt_SF++;}
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
        double lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
        double conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;
    }    

    private double[] computeMetrics(ArrayList<Integer> matchedArchIDs){
        
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
        double lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
        double conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;
    }  
    


    
    
    
    

    
        
//        Scheme scheme = new Scheme();
//        scheme.setName("present");
//        for (int i = 0; i < ninstr; ++i) {
//            scheme.setInstrument (i);
//            double[] metrics = computeMetrics(scheme);
//            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                String[] param = new String[1];
//                param[0] = Params.instrument_list[i];
//                String featureName = "present[" + param[0] + "]";
//                drivingFeatures.add(new DrivingFeature(featureName,"present", param, metrics));
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("absent");
//        for (int i = 0; i < ninstr; ++i) {
//            scheme.setInstrument (i);
//            double[] metrics = computeMetrics(scheme);
//            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                String [] param = new String[1];
//                param[0] = Params.instrument_list[i];
//                String featureName = "absent[" + param[0] + "]";
//                drivingFeatures.add(new DrivingFeature(featureName,"absent", param, metrics));
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("inOrbit");
//        for (int i = 0; i < norb; ++i) {
//            for (int j = 0; j < ninstr; ++j) {
//                scheme.setInstrument (j);
//                scheme.setOrbit(i);
//                double[] metrics = computeMetrics(scheme);
//                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                    String[] param = new String[2];
//                    param[0] = Params.orbit_list[i];
//                    param[1] = Params.instrument_list[j];
//                    String featureName = "inOrbit[" + param[0] + "," + param[1] + "]";
//                    drivingFeatures.add(new DrivingFeature(featureName,"inOrbit", param, metrics));
//                }
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("notInOrbit");
//        for (int i = 0; i < norb; ++i) {
//            for (int j = 0; j < ninstr; ++j) {
//                scheme.setInstrument (j);
//                scheme.setOrbit(i);
//                double[] metrics = computeMetrics(scheme);
//                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                    String[] param = new String[2];
//                    param[0] = Params.orbit_list[i];
//                    param[1] = Params.instrument_list[j];
//                    String featureName = "notInOrbit[" + param[0] + "," + param[1] + "]";
//                    drivingFeatures.add(new DrivingFeature(featureName,"notInOrbit", param, metrics));
//                } 
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("together2");
//        for (int i = 0; i < ninstr; ++i) {
//            for (int j = 0; j < i; ++j) {
//                scheme.setInstrument(i);
//                scheme.setInstrument2(j);
//                double[] metrics = computeMetrics(scheme);
//                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                    String[] param = new String[2];
//                    param[0] = Params.instrument_list[i];
//                    param[1] = Params.instrument_list[j];
//                    String featureName = "together2[" + param[0] + "," + param[1] + "]";
//                    drivingFeatures.add(new DrivingFeature(featureName,"together2", param, metrics));
//                }
//            }
//        }     
//        scheme.clearArgs();
//        scheme.setName("togetherInOrbit2");
//        for (int i = 0; i < norb; ++i) {
//            for (int j = 0; j < ninstr; ++j) {
//                for (int k = 0; k < j; ++k) {
//                    scheme.setInstrument(j);
//                    scheme.setInstrument2(k);
//                    scheme.setOrbit(i);
//                    double[] metrics = computeMetrics(scheme);
//                    if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                        String[] param = new String[3];
//                        param[0] = Params.orbit_list[i];
//                        param[1] = Params.instrument_list[j];
//                        param[2] = Params.instrument_list[k];
//                        String featureName = "togetherInOrbit2[" + param[0] + "," + param[1] + 
//                                "," + param[2] + "]"; 
//                        drivingFeatures.add(new DrivingFeature(featureName,"togetherInOrbit2", param,metrics));
//                    }
//                }
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("separate2");
//        for (int i = 0; i < ninstr; ++i) {
//            for (int j = 0; j < i; ++j) {
//                scheme.setInstrument(i);
//                scheme.setInstrument2(j);
//                double[] metrics = computeMetrics(scheme);
//                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                        String[] param = new String[2];
//                        param[0] = Params.instrument_list[i];
//                        param[1] = Params.instrument_list[j];
//                        String featureName = "separate2[" + param[0] + "," + param[1] + "]";
//                        drivingFeatures.add(new DrivingFeature(featureName,"separate2", param, metrics));
//                    }
//            }            
//        }
//        scheme.clearArgs();
//        scheme.setName("together3");
//        for (int i = 0; i < ninstr; ++i) {
//            for (int j = 0; j < i; ++j) {
//                for (int k = 0; k < j; ++k) {
//                    scheme.setInstrument(i);
//                    scheme.setInstrument2(j);
//                    scheme.setInstrument3(k);
//                    double[] metrics = computeMetrics(scheme);
//                    if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                        String[] param = new String[3];
//                        param[0] = Params.instrument_list[i];
//                        param[1] = Params.instrument_list[j];
//                        param[2] = Params.instrument_list[k];
//                        String featureName = "together3[" + param[0] + "," + 
//                                            param[1] + "," + param[2] + "]";
//                        drivingFeatures.add(new DrivingFeature(featureName,"together3", param, metrics));
//                    }
//                }
//            }            
//        }
//        scheme.clearArgs();
//        scheme.setName("togetherInOrbit3");
//        for (int i = 0; i < norb; ++i) {
//            for (int j = 0; j < ninstr; ++j) {
//                for (int k = 0; k < j; ++k) {
//                    for (int l = 0; l < k; ++l) {
//                        scheme.setName("togetherInOrbit3");
//                        scheme.setInstrument(j);
//                        scheme.setInstrument2(k);
//                        scheme.setInstrument3(l);
//                        scheme.setOrbit(i);
//                        double[] metrics = computeMetrics(scheme);
//                        if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                            String[] param = new String[4];
//                            param[0] = Params.orbit_list[i];
//                            param[1] = Params.instrument_list[j];
//                            param[2] = Params.instrument_list[k];
//                            param[3] = Params.instrument_list[l];
//                            String featureName = "togetherInOrbit3[" + param[0] + "," + 
//                                                param[1] + "," + param[2] + "," + param[3] + "]";
//                            drivingFeatures.add(new DrivingFeature(featureName,"togetherInOrbit3", param, metrics));
//                        }
//                    }
//                }
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("separate3");
//        for (int i = 0; i < ninstr; ++i) {
//            for (int j = 0; j < i; ++j) {
//                for (int k = 0; k < j; ++k) {
//                    scheme.setInstrument(i);
//                    scheme.setInstrument2(j);
//                    scheme.setInstrument3(k);
//                    double[] metrics = computeMetrics(scheme);
//                    if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                        String[] param = new String[3];
//                        param[0] = Params.instrument_list[i];
//                        param[1] = Params.instrument_list[j];
//                        param[2] = Params.instrument_list[k];
//                        String featureName = "separate3[" + param[0] + "," + 
//                                            param[1] + "," + param[2] + "]";
//                        drivingFeatures.add(new DrivingFeature(featureName,"separate3", param, metrics));
//                    }
//                }
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("emptyOrbit");
//        for (int i = 0; i < norb; ++i) {
//            scheme.setOrbit(i);
//            double[] metrics = computeMetrics(scheme);
//            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                String[] param = new String[1];
//                param[0] = Params.orbit_list[i];
//                String featureName = "emptyOrbit[" + param[0] + "]";
//                drivingFeatures.add(new DrivingFeature(featureName,"emptyOrbit", param, metrics));
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("numOrbits");
//        for (int i = 1; i < norb+1; i++) {
//            scheme.setNumOrbits(i);
//            double[] metrics = computeMetrics(scheme);
//            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                String[] param = new String[1];
//                param[0] = "" + i;
//                String featureName = "numOrbits[" + param[0] + "]";
//                drivingFeatures.add(new DrivingFeature(featureName,"numOrbits", param, metrics));
//            }
//        }
//        scheme.clearArgs();
//        scheme.setName("numOfInstruments");
//        for (int i = 0; i < ninstr; i++) {
//        	for(int j=0; j< norb + 1;j++){
//                scheme.setInstrument(i);
//                scheme.setNumInstruments(j);
//                double[] metrics = computeMetrics(scheme);
//                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                    
//                    String[] param = new String[2];
//                    param[0] = Params.instrument_list[i];
//                    param[1] = Integer.toString(j);
//                    String featureName = "numOfInstruments[" + param[0] + "," + 
//                                        param[1] + "]";
//                    drivingFeatures.add(new DrivingFeature(featureName,"numOfInstruments", param, metrics));
//                }
//        	}
//        }
//        scheme.clearArgs();
//        scheme.setName("numOfInstruments");
//    	for(int i=1; i< 16;i++){
//            scheme.setNumInstruments(i);
//            double[] metrics = computeMetrics(scheme);
//            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                
//                String[] param = new String[1];
//                param[0] = Params.instrument_list[i];
//                String featureName = "numOfInstruments[" + param[0] +"]";
//                drivingFeatures.add(new DrivingFeature(featureName,"numOfInstruments", param, metrics));
//            }
//    	}        


//        
//        
//        for (DrivingFeature userDef1:userDef){
////            System.out.println(userDef1.getName());
////            System.out.println(userDef1.getType());
//            
//            scheme.setName(userDef1.getType());
//            double[] metrics = computeMetrics(scheme);
//            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
//                drivingFeatures.add(new DrivingFeature(userDef1.getName(),userDef1.getType(),metrics));
//            }
//        }
//        
//        getDataFeatureMat();
        
//        System.out.println("----------mRMR-----------");
//        ArrayList<String> mRMR = minRedundancyMaxRelevance(40);
//        for(String mrmr:mRMR){
//            System.out.println(drivingFeatures.get(Integer.parseInt(mrmr)).getName());
//        }

//        return drivingFeatures;
//    }
    
    
    public int[][] booleanToInt(boolean[][] b) {
        int[][] intVector = new int[b.length][b[0].length]; 
        for(int i = 0; i < b.length; i++){
            for(int j = 0; j < b[0].length; ++j) intVector[i][j] = b[i][j] ? 1 : 0;
        }
        return intVector;
    }

    
    

//    public int[][] getDataFeatureMat(){
//        
//        int numData = behavioral.size() + non_behavioral.size();
//        int numFeature = drivingFeatures.size() + 1; // add class label as a last feature
//        int[][] dataMat = new int[numData][numFeature];
//        
//        for(int i=0;i<numData;i++){
//        	int[][] d;
//        	if(i<behavioral.size()){
//        		d = behavioral.get(i);
//        	}else{
//        		d = non_behavioral.get(i-behavioral.size());
//        	}
//            Scheme s = new Scheme();
//
////            presetFilter(String filterName, int[][] data, ArrayList<String> params
//            for(int j=0;j<numFeature-1;j++){
//                DrivingFeature f = drivingFeatures.get(j);
//                String name = f.getName();
//                String type = f.getType();
//                
//                if(f.isPreset()){
//                    String[] param_ = f.getParam();
//                    ArrayList<String> param = new ArrayList<>();
//                    param.addAll(Arrays.asList(param_));
//                    if(s.presetFilter(type, d, param)){
//                        dataMat[i][j]=1;
//                    } else{
//                        dataMat[i][j]=0;
//                    }
//                } else{
//                    if(s.userDefFilter_eval(type, d)){
//                        dataMat[i][j]=1;
//                    } else{
//                        dataMat[i][j]=0;
//                    }
//                }
//            }
//            
//            boolean classLabel = false;
//            for (int[][] compData : behavioral) {
//                boolean match = true;
//                for(int k=0;k<d.length;k++){
//                    for(int l=0;l<d[0].length;l++){
//                        if(d[k][l]!=compData[k][l]){
//                            match = false;
//                            break;
//                        }
//                    }
//                    if(match==false) break;
//                }
//                if(match==true){
//                    classLabel = true;
//                    break;
//                }
//            }
//            if(classLabel==true){
//                dataMat[i][numFeature-1]=1;
//            } else{
//                dataMat[i][numFeature-1]=0;
//            }
//        }
//        dataFeatureMat = dataMat;
//        return dataMat;
//    }
    public ArrayList<String> minRedundancyMaxRelevance(int numSelectedFeatures){
        
        int[][] m = dataFeatureMat;
        int numFeatures = m[0].length;
        int numData = m.length;
        ArrayList<String> selected = new ArrayList<>();
        
        while(selected.size() < numSelectedFeatures){
            double phi = -10000;
            int save=0;
            for(int i=0;i<numFeatures-1;i++){
                if(selected.contains(""+i)){
                    continue;
                }

                double D = getMutualInformation(i,numFeatures-1);
                double R = 0;

                for (String selected1 : selected) {
                    R = R + getMutualInformation(i, Integer.parseInt(selected1));
                }
                if(!selected.isEmpty()){
                   R = (double) R/selected.size();
                }
                
//                System.out.println(D-R);
                
                if(D-R > phi){
                    phi = D-R;
                    save = i;
                }
            }
//            System.out.println(save);
            selected.add(""+save);
        }
        return selected;
    }  
    public double getMutualInformation(int feature1, int feature2){
        
        int[][] m = dataFeatureMat;
        int numFeatures = m[0].length;
        int numData = m.length;
        double I;
        
        int x1=0,x2=0;
        int x1x2=0,nx1x2=0,x1nx2=0,nx1nx2=0;      

        for(int k=0;k<numData;k++){
            if(m[k][feature1]==1){ // x1==1
                x1++;
                if(m[k][feature2]==1){ // x2==1
                    x2++;
                    x1x2++;
                } else{ // x2!=1
                    x1nx2++;
                }
            } else{ // x1!=1
                if(m[k][feature2]==1){ // x2==1 
                    x2++;
                    nx1x2++;
                }else{ // x2!=1
                    nx1nx2++;
                }
            }
        }
        double p_x1 =(double) x1/numData;
        double p_nx1 = (double) 1-p_x1;
        double p_x2 = (double) x2/numData;
        double p_nx2 = (double) 1-p_x2;
        double p_x1x2 = (double) x1x2/numData;
        double p_nx1x2 = (double) nx1x2/numData;
        double p_x1nx2 = (double) x1nx2/numData;
        double p_nx1nx2 = (double) nx1nx2/numData;
        
        if(p_x1==0){p_x1 = 0.0001;}
        if(p_nx1==0){p_nx1=0.0001;}
        if(p_x2==0){p_x2=0.0001;}
        if(p_nx2==0){p_nx2=0.0001;}
        if(p_x1x2==0){p_x1x2=0.0001;}
        if(p_nx1x2==0){p_nx1x2=0.0001;}
        if(p_x1nx2==0){p_x1nx2=0.0001;}
        if(p_nx1nx2==0){p_nx1nx2=0.0001;}
        
        double i1 = p_x1x2*Math.log(p_x1x2/(p_x1*p_x2));
        double i2 = p_x1nx2*Math.log(p_x1nx2/(p_x1*p_nx2));
        double i3 = p_nx1x2*Math.log(p_nx1x2/(p_nx1*p_x2));
        double i4 = p_nx1nx2*Math.log(p_nx1nx2/(p_nx1*p_nx2));

        I = i1 + i2 + i3 + i4;
        return I;
    }
    
    
    public FastVector setDataFormat(){
        
            FastVector bool = new FastVector();
            bool.addElement("false");
            bool.addElement("true");
            FastVector attributes = new FastVector();

            for(DrivingFeature df:drivingFeatures){
                String name = df.getName();
                attributes.addElement(new Attribute(name,bool));
            }
            
            FastVector bool2 = new FastVector();
            bool2.addElement("not selected");
            bool2.addElement("selected ");
            
            attributes.addElement(new Attribute("class",bool2));
            
            return attributes;
    }
    
    public Instances addData(Instances dataset){
        
        for(int i=0;i<behavioral.size()+non_behavioral.size();i++){
            double[] values = new double[drivingFeatures.size()+1];
            for(int j=0;j<drivingFeatures.size()+1;j++){
                values[j] = (double) dataFeatureMat[i][j];
            }
            Instance thisInstance = new Instance(1.0,values);
            dataset.add(thisInstance);
        }
        return dataset;
    }
    


//    public String buildTree(boolean recomputeDFs) {
//    	  
//        String graph="";
//        if(recomputeDFs){
//        	getDrivingFeatures();
//        }
//        int[][] mat = getDataFeatureMat();
//        ClassificationTreeBuilder ctb = new ClassificationTreeBuilder(mat);
//        
//        try{
//            ctb.setDrivingFeatures(drivingFeatures);
//        	ctb.buildTree();
//        	graph = ctb.printTree_json();
//        	
//
//        } catch(Exception e){
//            e.printStackTrace();
//        }
//        
//        return graph;
//    }
//    
//    
//    public String buildTree_Weka() { // using WEKA
//  
//        String graph="";
////        long t0 = System.currentTimeMillis();
//        J48 tree = new J48();
//        getDrivingFeatures();
//        getDataFeatureMat();
//        try{
//            
//            FastVector attributes = setDataFormat();
//            Instances dataset = new Instances("Tree_dataset", attributes, 100000);
//            dataset.setClassIndex(dataset.numAttributes()-1);
//            dataset = addData(dataset);
//            dataset.compactify();
//
////            // save as CSV
////            CSVSaver saver = new CSVSaver();
////            saver.setInstances(dataset);
////            saver.setFile(new File(Params.path + "\\tmp_treeData.clp"));
////            saver.writeBatch();
//            
//            System.out.println("numAttributes: " + dataset.numAttributes());
//            System.out.println("num instances: " + dataset.numInstances());
//            
//            String [] options = new String[2];
//            options[0] = "-C";
//            options[1] = "0.05";
//            tree.setOptions(options);
//            
////            Evaluation eval = new Evaluation(dataset);
////            eval.crossValidateModel(tree, dataset, 10, new Random(1));
//            tree.buildClassifier(dataset);
//            
////            System.out.println(eval.toSummaryString("\nResults\n\n", false));
////            System.out.println(eval.toMatrixString());
////            System.out.println(tree.toSummaryString());
////            String summary = tree.toSummaryString();
////            String evalSummary = eval.toSummaryString("\nResults\n\n", false);
////            String confusion = eval.toMatrixString();
//            graph = tree.graph();
//            
//
//            
////Number of leaves: 21
////Size of the tree: 41
////Results
////Correctly Classified Instances        2550               97.3654 %
////Incorrectly Classified Instances        69                2.6346 %
////Kappa statistic                          0.9385
////Mean absolute error                      0.0418
////Root mean squared error                  0.1603
////Relative absolute error                  9.6708 %
////Root relative squared error             34.4579 %
////Total Number of Instances             2619
////=== Confusion Matrix ===
////    a    b   <-- classified as
//// 1771   19 |    a = false
////   50  779 |    b = true
//
//            
//            
////            System.out.println(graph);
//            
////            TreeVisualizer tv = new TreeVisualizer(null, tree.graph(), new PlaceNode2());
////            JFrame jf = new JFrame("Weka Classifier Tree Visualizer: J48");
////            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
////            jf.setSize(800, 600);
////            jf.getContentPane().setLayout(new BorderLayout());
////            jf.getContentPane().add(tv, BorderLayout.CENTER);
////            jf.setVisible(true);
////            // adjust tree
////            tv.fitToScreen();
//            
////            long t1 = System.currentTimeMillis();
////            System.out.println( "Tree building done in: " + String.valueOf(t1-t0) + " msec");
//        } catch(Exception e){
//            e.printStackTrace();
//        }
//        
//        return graph;
//    }
    

    
    public void addUserDefFilter(String name, String expression){
        this.userDef.add(new DrivingFeature(name,expression));
    }
    
    

}
