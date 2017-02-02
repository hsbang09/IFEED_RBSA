

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.List;
import java.util.Set;

import rbsa.eoss.local.Params;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

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

    private String[] exclude_general_array = {"ArchID"}; 
    // _id, factName, factID, module, factHistory are filtered in DBQueryBuilder.getSlotNames(); 
    private String[] exclude_cost_array = {"payload-dimensions","payload-dimensions#","instruments","updated","updated2"};
    private String[] exclude_aggregation_array ={"index","subobj-scores","weights","subobj-scores"};
    private String[] exclude_science_mission_array = {"Name","payload-dimensions","payload-dimensions#","instruments"};
    private String[] exclude_science_measurement_array = {"taken-by","Id","Horizontal-Spatial-Resolution","orbit-string","Parameter"};
    
    private ArrayList<String> exclude_general; 
    private ArrayList<String> exclude_cost;
    private ArrayList<String> exclude_aggregation;
    private ArrayList<String> exclude_science_mission;
    private ArrayList<String> exclude_science_measurement;
    
    private ArrayList<String> userDefFeatures;
    private ArrayList<Integer> removedFeatures;
    
    public DrivingFeaturesGenerator(){
        this.dbquery = new DBQueryBuilder();
    }
    public DrivingFeaturesGenerator(DBQueryBuilder dbq){
        this.dbquery = dbq;
    }


    
    public void initialize(String scope, ArrayList<Integer> behavioral, ArrayList<Integer> non_behavioral, 
                                double supp, double conf, double lift){
        
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
        
        userDefFeatures = new ArrayList<>();
        removedFeatures = new ArrayList<>();        
    }
    
 

    public ArrayList<DrivingFeature> getDrivingFeatures (){
        
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
                String feature_expression_inside = feature.substring(1,feature.length()-1);
                String name = feature_expression_inside.split("\\[")[0];
                ArrayList<Integer> matchedArchIDs = feh.processSingleFilterExpression(feature_expression_inside,true);
                double[] metrics = this.computeMetrics(matchedArchIDs);
                if(metrics[0]>supp_threshold && metrics[1] > lift_threshold && metrics[2] > conf_threshold && metrics[3] > conf_threshold){
                    drivingFeatures.add(new DrivingFeature(dfid, name,feature,metrics,true));
                    dfid++;
                    int[] satArray = satisfactionArray(matchedArchIDs,population); 
                    satList.add(satArray);
                }
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
                        ArrayList<double[]> thresholds = this.discretize_continuous_range(0.0,1.0,5);
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
                        ArrayList<double[]> thresholds = this.discretize_continuous_range(min, max, 3);
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
                numOfInstance_conditions.add("eq[0]"); // No occurance
                numOfInstance_conditions.add("gt[0]"); // At least 1
                numOfInstance_conditions.add("gt[1]"); // At least 2
                numOfInstance_conditions.add("gt[2]"); // At least 3
                numOfInstance_conditions.add("lt[2]"); // less than 2
                //numOfInstance_conditions.add("lt[3]"); // less than 3
                numOfInstance_conditions.add("all[]");
                if(scope.contains("AGGREGATION")){
                    numOfInstance_conditions.add("gt[6]"); // more than 6
                    numOfInstance_conditions.add("gt[9]"); // more than 9   
                    numOfInstance_conditions.add("gt[12]");
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
                    int argument;
                    double[] metrics;
                    if(inequalitySign.equalsIgnoreCase("all")){
                        metrics = computeMetrics(FactCounter, allArchIDList);
                    }else{
                        argument = Integer.parseInt(cond.substring(0,cond.length()-1).split("\\[")[1]);
                        metrics = computeMetrics(FactCounter,inequalitySign,argument);
                    }

                    if(metrics[0]>supp_threshold && metrics[1] > lift_threshold && metrics[2] > conf_threshold && metrics[3] > conf_threshold){
                        drivingFeatures.add(new DrivingFeature(dfid,feature_name,feature_expression, metrics,false));
                        dfid++;
                        int[] satArray = satisfactionArray(matchedArchIDs,population); 
                        satList.add(satArray);
                    }                       
                }
            }            
        }
        
        // Test the user-defined features
        if(!this.userDefFeatures.isEmpty()){
            for(String exp:this.userDefFeatures){
                if(exp.isEmpty()){
                    continue;
                }
                ArrayList<Integer> matchedArchIDs = feh.processFilterExpression(exp, new ArrayList<Integer>(), "||");
                double[] metrics = this.computeMetrics(matchedArchIDs);
                if(metrics[0]>supp_threshold && metrics[1] > lift_threshold && metrics[2] > conf_threshold && metrics[3] > conf_threshold){
                    drivingFeatures.add(new DrivingFeature(dfid,exp,exp,metrics,true));
                    dfid++;
                    int[] satArray = satisfactionArray(matchedArchIDs,population); 
                    satList.add(satArray);
                }             
            }
        }

        // Get feature satisfaction matrix
        this.drivingFeaturesMatrix = new double[population.size()][drivingFeatures.size()];
        for(int i=0;i<population.size();i++){
            for(int j=0;j<drivingFeatures.size();j++){
                this.drivingFeaturesMatrix[i][j] = (double) satList.get(j)[i];
            }
        }       
        
        return this.drivingFeatures;
    }
    
    
    public ArrayList<DrivingFeature> runFeatureSelection(int numFeatures){

        ArrayList<DrivingFeature> newDrivingFeatures = new ArrayList<>();
        
        // Find indices of removed features 
        //(Note: index and IDs are different!)
        ArrayList<Integer> removedFeatureIndices = new ArrayList<>();
        for(int rf:this.removedFeatures){
            for(int i=0;i<this.drivingFeatures.size();i++){
                if(this.drivingFeatures.get(i).getID()==rf){
                    removedFeatureIndices.add(i);
                    break;
                }
            }
        }
        
        // Create an array containing labels
        double[] labels = new double[population.size()];
        for(int i=0;i<population.size();i++){
            int id = population.get(i);
            if(behavioral.contains(id)){
                labels[i] = 1.0;
            }else{
                labels[i] = 0.0;
            }
        }
        
        
        Apriori tempAp = new Apriori();
        ArrayList<Apriori.Feature> S = new ArrayList<>();
        // Generate an ArrayList containing instances of Apriori.Feature
        for(int i=0;i<this.drivingFeaturesMatrix[0].length;i++){
            if(removedFeatureIndices.contains(i)){continue;}
            // Create a new Apriori.Feature instance
            Apriori.Feature newFeat = tempAp.new Feature(i);
            double[] metrics = drivingFeatures.get(i).getMetrics();
            // Copy over the metrics
            newFeat.setMetrics(metrics);
            S.add(newFeat);
        }              
        // Sort features based on confidence(feature->selection)
        // Changing the order of features here is okay because the feature indices are saved as 'elements' in Apriori.Feature
        S = tempAp.sortFeatures(S);
        MRMR mRMR = new MRMR();
        // Run feature selection algorithm
        S = mRMR.minRedundancyMaxRelevance(new DoubleMatrix(this.drivingFeaturesMatrix),labels, S ,numFeatures);
        
        // Create a new list of driving featues and assign new IDs
        ArrayList<Integer> included_columns=new ArrayList<>();
        int id=0;
        for(Apriori.Feature feat:S){
            int featureIndex = feat.getElements().get(0);
            DrivingFeature df = this.drivingFeatures.get(featureIndex);
            included_columns.add(featureIndex);
            DrivingFeature newdf = new DrivingFeature(id,df.getName(),df.getExpression(), df.getMetrics(), df.isPreset());
            id++;
            newDrivingFeatures.add(df);
        }    
        
        DoubleMatrix old_sat_matrix = new DoubleMatrix(this.drivingFeaturesMatrix);
        DoubleMatrix new_sat_matrix = DoubleMatrix.zeros(this.drivingFeaturesMatrix.length,S.size());
        for(int i=0;i<included_columns.size();i++){
            DoubleMatrix col = old_sat_matrix.getColumn(included_columns.get(i));
            new_sat_matrix.putColumn(i,col);
        }
        this.drivingFeatures=newDrivingFeatures;
        this.drivingFeaturesMatrix=new_sat_matrix.toArray2();
        
        return newDrivingFeatures;      
    }
    
    
    public ArrayList<DrivingFeature> getHigherOrderDrivingFeautures(double supp, double conf, double lift){
        
        ArrayList<DrivingFeature> newDrivingFeatures = new ArrayList<>();
        
        // Find indices of removed features 
        //(Note: index and IDs are different!)
        ArrayList<Integer> removedFeatureIndices = new ArrayList<>();
        for(int rf:this.removedFeatures){
            for(int i=0;i<this.drivingFeatures.size();i++){
                if(this.drivingFeatures.get(i).getID()==rf){
                    removedFeatureIndices.add(i);
                    break;
                }
            }
        }
        
        // Create an array containing labels
        double[] labels = new double[population.size()];
        for(int i=0;i<population.size();i++){
            int id = population.get(i);
            if(behavioral.contains(id)){
                labels[i] = 1.0;
            }else{
                labels[i] = 0.0;
            }
        }
        
        // Set threshold values
        this.setThresholds(supp, conf, lift);
        double[] thresholds = {this.supp_threshold, this.lift_threshold, this.conf_threshold};
        
        // Create a new instance of Apriori
        Apriori ap = new Apriori(drivingFeatures, this.drivingFeaturesMatrix, labels, thresholds);
        // Set list of feature indices to be not used
        ap.setSkip(removedFeatureIndices);
        
        // Run Apriori algorithm
        ArrayList<Apriori.Feature> new_features = ap.runApriori(2,false,100);

        // Create a new drivingFeaturesMatrix. 
        // Row represents each sample and each column represents newly defined features
        int number_of_old_features = this.drivingFeatures.size();
        // Generate a matrix whose size is [num_old_features, num_new_features]
        DoubleMatrix mapping_old_and_new_feature_indices = DoubleMatrix.zeros(number_of_old_features,new_features.size());
        int[] save_feature_length = new int[new_features.size()];
        
        // Create a new list of driving features (assign new IDs)
        int id=0;
        for(int f=0;f<new_features.size();f++){
            
            Apriori.Feature feat = new_features.get(f);
            String expression="";
            String name="";
            ArrayList<Integer> featureIndices = feat.getElements();
            
            int[] indices_array = new int[featureIndices.size()];
            for(int i=0;i<featureIndices.size();i++){
                indices_array[i] = featureIndices.get(i);
            }
        	
            // Update mapping between old and new feature indices
            mapping_old_and_new_feature_indices.put(indices_array, f, 1.0);
            save_feature_length[f] = featureIndices.size();
                
            boolean first = true;
            for(int index:featureIndices){
                if(first){
                    first = false;
                }
                else{
                    expression = expression + "&&";
                    name = name + "&&";
                }
                DrivingFeature thisDF = this.drivingFeatures.get(index);
                expression = expression + thisDF.getExpression();
                name = name + thisDF.getName();
            }
            double[] metrics = feat.getMetrics();
            DrivingFeature df = new DrivingFeature(id,name,expression, metrics, false);
            id++;
            newDrivingFeatures.add(df);
        }
        
        // Define the new feature satisfaction matrix       
        DoubleMatrix prev_sat_matrix = new DoubleMatrix(this.drivingFeaturesMatrix);
        DoubleMatrix new_sat_matrix = prev_sat_matrix.mmul(mapping_old_and_new_feature_indices);        
        
        DoubleMatrix newDrivingFeaturesMatrix = DoubleMatrix.zeros(new_sat_matrix.getRows(), new_sat_matrix.getColumns());
        for(int i=0;i<new_sat_matrix.getColumns();i++){
            DoubleMatrix col = new_sat_matrix.getColumn(i);
            col = col.eq(save_feature_length[i]);
            newDrivingFeaturesMatrix.putColumn(i, col);
        }
        
        this.drivingFeaturesMatrix = newDrivingFeaturesMatrix.toArray2();
        this.drivingFeatures = newDrivingFeatures;
        return newDrivingFeatures;
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
    
    
    private double[] computeMetrics(HashMap<Integer,Integer> FactCounter, ArrayList<Integer> allArchIDList){
        
    	double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;
        
        boolean pass;
        for(int id:this.population){
                pass=FactCounter.get(id)==Collections.frequency(allArchIDList,id);
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
    
    

    
    
//    public FastVector setDataFormat(){
//        
//            FastVector bool = new FastVector();
//            bool.addElement("false");
//            bool.addElement("true");
//            FastVector attributes = new FastVector();
//
//            for(DrivingFeature df:drivingFeatures){
//                String name = df.getName();
//                attributes.addElement(new Attribute(name,bool));
//            }
//            
//            FastVector bool2 = new FastVector();
//            bool2.addElement("not selected");
//            bool2.addElement("selected ");
//            
//            attributes.addElement(new Attribute("class",bool2));
//            
//            return attributes;
//    }
//    
//    public Instances addData(Instances dataset){
//        
//        for(int i=0;i<behavioral.size()+non_behavioral.size();i++){
//            double[] values = new double[drivingFeatures.size()+1];
//            for(int j=0;j<drivingFeatures.size()+1;j++){
//                values[j] = (double) dataFeatureMat[i][j];
//            }
//            Instance thisInstance = new Instance(1.0,values);
//            dataset.add(thisInstance);
//        }
//        return dataset;
//    }
    


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

    
    

}
