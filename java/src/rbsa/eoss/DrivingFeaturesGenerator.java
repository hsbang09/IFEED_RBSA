

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rbsa.eoss;

//import be.ac.ulg.montefiore.run.jadti.AttributeSet;
//import be.ac.ulg.montefiore.run.jadti.AttributeValue;
//import be.ac.ulg.montefiore.run.jadti.DecisionTree;
//import be.ac.ulg.montefiore.run.jadti.Item;
//import be.ac.ulg.montefiore.run.jadti.ItemSet;
//import be.ac.ulg.montefiore.run.jadti.KnownSymbolicValue;
//import be.ac.ulg.montefiore.run.jadti.SymbolicAttribute;
//import be.ac.ulg.montefiore.run.jadti.SymbolicValue;
//import be.ac.ulg.montefiore.run.jadti.UnknownSymbolicValue;
import java.util.ArrayList;
//import weka.gui.treevisualizer.PlaceNode2;
//import weka.gui.treevisualizer.TreeVisualizer;
//import weka.core.converters.ConverterUtils.DataSink;
//import weka.core.converters.CSVSaver;
//import java.io.File;
//import java.awt.BorderLayout;
import java.util.Arrays;
//import javax.swing.JFrame;

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

    private ArrayList<int[][]> behavioral;
    private ArrayList<int[][]> non_behavioral;
    private int[][] dataFeatureMat;
    private double supp_threshold;
    private double confidence_threshold;
    private double lift_threshold;
    
    private double ninstr;
    private double norb;
    
    private ArrayList<DrivingFeature> drivingFeatures;
    private ArrayList<DrivingFeature> userDef;
    


    public DrivingFeaturesGenerator(){
    }
    
    
    public void initialize2(ArrayList<int[][]> behavioral, ArrayList<int[][]> non_behavioral, double supp, double conf, double lift){
        
//        this.focus = focus;
//        this.random = random;
        this.supp_threshold=supp;
        this.confidence_threshold=conf;
        this.lift_threshold=lift;
        
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;
        
        this.ninstr = behavioral.get(0)[0].length;
        this.norb = behavioral.get(0).length;
        
        userDef = new ArrayList<>();
        drivingFeatures = new ArrayList<>();
    }
    
  
    private double[] computeMetrics(Scheme s){

    	double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;
        
        for (int[][] a: behavioral) {
            if (s.compare(a) == 1) {
            	cnt_SF = cnt_SF+1.0;
            	cnt_F = cnt_F + 1.0;
            }
        }
        for (int[][] a: non_behavioral) {
            if (s.compare(a) == 1) cnt_F = cnt_F+1.0;
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
    

    

    

    public ArrayList<DrivingFeature> getDrivingFeatures (){

        Scheme scheme = new Scheme();

        scheme.setName("present");
        for (int i = 0; i < ninstr; ++i) {
            scheme.setInstrument (i);
            double[] metrics = computeMetrics(scheme);
            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                String[] param = new String[1];
                param[0] = Params.instrument_list[i];
                String featureName = "present[" + param[0] + "]";
                drivingFeatures.add(new DrivingFeature(featureName,"present", param, metrics));
            }
        }
        scheme.clearArgs();
        scheme.setName("absent");
        for (int i = 0; i < ninstr; ++i) {
            scheme.setInstrument (i);
            double[] metrics = computeMetrics(scheme);
            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                String [] param = new String[1];
                param[0] = Params.instrument_list[i];
                String featureName = "absent[" + param[0] + "]";
                drivingFeatures.add(new DrivingFeature(featureName,"absent", param, metrics));
            }
        }
        scheme.clearArgs();
        scheme.setName("inOrbit");
        for (int i = 0; i < norb; ++i) {
            for (int j = 0; j < ninstr; ++j) {
                scheme.setInstrument (j);
                scheme.setOrbit(i);
                double[] metrics = computeMetrics(scheme);
                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                    String[] param = new String[2];
                    param[0] = Params.orbit_list[i];
                    param[1] = Params.instrument_list[j];
                    String featureName = "inOrbit[" + param[0] + "," + param[1] + "]";
                    drivingFeatures.add(new DrivingFeature(featureName,"inOrbit", param, metrics));
                }
            }
        }
        scheme.clearArgs();
        scheme.setName("notInOrbit");
        for (int i = 0; i < norb; ++i) {
            for (int j = 0; j < ninstr; ++j) {
                scheme.setInstrument (j);
                scheme.setOrbit(i);
                double[] metrics = computeMetrics(scheme);
                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                    String[] param = new String[2];
                    param[0] = Params.orbit_list[i];
                    param[1] = Params.instrument_list[j];
                    String featureName = "notInOrbit[" + param[0] + "," + param[1] + "]";
                    drivingFeatures.add(new DrivingFeature(featureName,"notInOrbit", param, metrics));
                } 
            }
        }
        scheme.clearArgs();
        scheme.setName("together2");
        for (int i = 0; i < ninstr; ++i) {
            for (int j = 0; j < i; ++j) {
                scheme.setInstrument(i);
                scheme.setInstrument2(j);
                double[] metrics = computeMetrics(scheme);
                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                    String[] param = new String[2];
                    param[0] = Params.instrument_list[i];
                    param[1] = Params.instrument_list[j];
                    String featureName = "together2[" + param[0] + "," + param[1] + "]";
                    drivingFeatures.add(new DrivingFeature(featureName,"together2", param, metrics));
                }
            }
        }     
        scheme.clearArgs();
        scheme.setName("togetherInOrbit2");
        for (int i = 0; i < norb; ++i) {
            for (int j = 0; j < ninstr; ++j) {
                for (int k = 0; k < j; ++k) {
                    scheme.setInstrument(j);
                    scheme.setInstrument2(k);
                    scheme.setOrbit(i);
                    double[] metrics = computeMetrics(scheme);
                    if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                        String[] param = new String[3];
                        param[0] = Params.orbit_list[i];
                        param[1] = Params.instrument_list[j];
                        param[2] = Params.instrument_list[k];
                        String featureName = "togetherInOrbit2[" + param[0] + "," + param[1] + 
                                "," + param[2] + "]"; 
                        drivingFeatures.add(new DrivingFeature(featureName,"togetherInOrbit2", param,metrics));
                    }
                }
            }
        }
        scheme.clearArgs();
        scheme.setName("separate2");
        for (int i = 0; i < ninstr; ++i) {
            for (int j = 0; j < i; ++j) {
                scheme.setInstrument(i);
                scheme.setInstrument2(j);
                double[] metrics = computeMetrics(scheme);
                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                        String[] param = new String[2];
                        param[0] = Params.instrument_list[i];
                        param[1] = Params.instrument_list[j];
                        String featureName = "separate2[" + param[0] + "," + param[1] + "]";
                        drivingFeatures.add(new DrivingFeature(featureName,"separate2", param, metrics));
                    }
            }            
        }
        scheme.clearArgs();
        scheme.setName("together3");
        for (int i = 0; i < ninstr; ++i) {
            for (int j = 0; j < i; ++j) {
                for (int k = 0; k < j; ++k) {
                    scheme.setInstrument(i);
                    scheme.setInstrument2(j);
                    scheme.setInstrument3(k);
                    double[] metrics = computeMetrics(scheme);
                    if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                        String[] param = new String[3];
                        param[0] = Params.instrument_list[i];
                        param[1] = Params.instrument_list[j];
                        param[2] = Params.instrument_list[k];
                        String featureName = "together3[" + param[0] + "," + 
                                            param[1] + "," + param[2] + "]";
                        drivingFeatures.add(new DrivingFeature(featureName,"together3", param, metrics));
                    }
                }
            }            
        }
        scheme.clearArgs();
        scheme.setName("togetherInOrbit3");
        for (int i = 0; i < norb; ++i) {
            for (int j = 0; j < ninstr; ++j) {
                for (int k = 0; k < j; ++k) {
                    for (int l = 0; l < k; ++l) {
                        scheme.setName("togetherInOrbit3");
                        scheme.setInstrument(j);
                        scheme.setInstrument2(k);
                        scheme.setInstrument3(l);
                        scheme.setOrbit(i);
                        double[] metrics = computeMetrics(scheme);
                        if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                            String[] param = new String[4];
                            param[0] = Params.orbit_list[i];
                            param[1] = Params.instrument_list[j];
                            param[2] = Params.instrument_list[k];
                            param[3] = Params.instrument_list[l];
                            String featureName = "togetherInOrbit3[" + param[0] + "," + 
                                                param[1] + "," + param[2] + "," + param[3] + "]";
                            drivingFeatures.add(new DrivingFeature(featureName,"togetherInOrbit3", param, metrics));
                        }
                    }
                }
            }
        }
        scheme.clearArgs();
        scheme.setName("separate3");
        for (int i = 0; i < ninstr; ++i) {
            for (int j = 0; j < i; ++j) {
                for (int k = 0; k < j; ++k) {
                    scheme.setInstrument(i);
                    scheme.setInstrument2(j);
                    scheme.setInstrument3(k);
                    double[] metrics = computeMetrics(scheme);
                    if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                        String[] param = new String[3];
                        param[0] = Params.instrument_list[i];
                        param[1] = Params.instrument_list[j];
                        param[2] = Params.instrument_list[k];
                        String featureName = "separate3[" + param[0] + "," + 
                                            param[1] + "," + param[2] + "]";
                        drivingFeatures.add(new DrivingFeature(featureName,"separate3", param, metrics));
                    }
                }
            }
        }
        scheme.clearArgs();
        scheme.setName("emptyOrbit");
        for (int i = 0; i < norb; ++i) {
            scheme.setOrbit(i);
            double[] metrics = computeMetrics(scheme);
            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                String[] param = new String[1];
                param[0] = Params.orbit_list[i];
                String featureName = "emptyOrbit[" + param[0] + "]";
                drivingFeatures.add(new DrivingFeature(featureName,"emptyOrbit", param, metrics));
            }
        }
        scheme.clearArgs();
        scheme.setName("numOrbits");
        for (int i = 1; i < norb+1; i++) {
            scheme.setNumOrbits(i);
            double[] metrics = computeMetrics(scheme);
            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                String[] param = new String[1];
                param[0] = "" + i;
                String featureName = "numOrbits[" + param[0] + "]";
                drivingFeatures.add(new DrivingFeature(featureName,"numOrbits", param, metrics));
            }
        }
        scheme.clearArgs();
        scheme.setName("numOfInstruments");
        for (int i = 0; i < ninstr; i++) {
        	for(int j=0; j< norb + 1;j++){
                scheme.setInstrument(i);
                scheme.setNumInstruments(j);
                double[] metrics = computeMetrics(scheme);
                if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                    
                    String[] param = new String[2];
                    param[0] = Params.instrument_list[i];
                    param[1] = Integer.toString(j);
                    String featureName = "numOfInstruments[" + param[0] + "," + 
                                        param[1] + "]";
                    drivingFeatures.add(new DrivingFeature(featureName,"numOfInstruments", param, metrics));
                }
        	}
        }
        scheme.clearArgs();
        scheme.setName("numOfInstruments");
    	for(int i=1; i< 16;i++){
            scheme.setNumInstruments(i);
            double[] metrics = computeMetrics(scheme);
            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                
                String[] param = new String[1];
                param[0] = Params.instrument_list[i];
                String featureName = "numOfInstruments[" + param[0] +"]";
                drivingFeatures.add(new DrivingFeature(featureName,"numOfInstruments", param, metrics));
            }
    	}
        
        
        for (DrivingFeature userDef1:userDef){
//            System.out.println(userDef1.getName());
//            System.out.println(userDef1.getType());
            
            scheme.setName(userDef1.getType());
            double[] metrics = computeMetrics(scheme);
            if (metrics[0] > supp_threshold && metrics[1] > lift_threshold && metrics[2] > confidence_threshold && metrics[3] > confidence_threshold) {
                drivingFeatures.add(new DrivingFeature(userDef1.getName(),userDef1.getType(),metrics));
            }
        }
        
        getDataFeatureMat();
        
//        System.out.println("----------mRMR-----------");
//        ArrayList<String> mRMR = minRedundancyMaxRelevance(40);
//        for(String mrmr:mRMR){
//            System.out.println(drivingFeatures.get(Integer.parseInt(mrmr)).getName());
//        }

        return drivingFeatures;
    }
    
    
    public int[][] booleanToInt(boolean[][] b) {
        int[][] intVector = new int[b.length][b[0].length]; 
        for(int i = 0; i < b.length; i++){
            for(int j = 0; j < b[0].length; ++j) intVector[i][j] = b[i][j] ? 1 : 0;
        }
        return intVector;
    }
    
//    public static DrivingFeaturesGenerator getInstance()
//    {
//        if( instance == null ) 
//        {
//            instance = new DrivingFeaturesGenerator();
//        }
//        return instance;
//    }

    public int[][] getDataFeatureMat(){
        
        int numData = behavioral.size() + non_behavioral.size();
        int numFeature = drivingFeatures.size() + 1; // add class label as a last feature
        int[][] dataMat = new int[numData][numFeature];
        
        for(int i=0;i<numData;i++){
        	int[][] d;
        	if(i<behavioral.size()){
        		d = behavioral.get(i);
        	}else{
        		d = non_behavioral.get(i-behavioral.size());
        	}
            Scheme s = new Scheme();

//            presetFilter(String filterName, int[][] data, ArrayList<String> params
            for(int j=0;j<numFeature-1;j++){
                DrivingFeature f = drivingFeatures.get(j);
                String name = f.getName();
                String type = f.getType();
                
                if(f.isPreset()){
                    String[] param_ = f.getParam();
                    ArrayList<String> param = new ArrayList<>();
                    param.addAll(Arrays.asList(param_));
                    if(s.presetFilter(type, d, param)){
                        dataMat[i][j]=1;
                    } else{
                        dataMat[i][j]=0;
                    }
                } else{
                    if(s.userDefFilter_eval(type, d)){
                        dataMat[i][j]=1;
                    } else{
                        dataMat[i][j]=0;
                    }
                }
            }
            
            boolean classLabel = false;
            for (int[][] compData : behavioral) {
                boolean match = true;
                for(int k=0;k<d.length;k++){
                    for(int l=0;l<d[0].length;l++){
                        if(d[k][l]!=compData[k][l]){
                            match = false;
                            break;
                        }
                    }
                    if(match==false) break;
                }
                if(match==true){
                    classLabel = true;
                    break;
                }
            }
            if(classLabel==true){
                dataMat[i][numFeature-1]=1;
            } else{
                dataMat[i][numFeature-1]=0;
            }
        }
        dataFeatureMat = dataMat;
        return dataMat;
    }
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
    


    public String buildTree(boolean recomputeDFs) {
    	  
        String graph="";
        if(recomputeDFs){
        	getDrivingFeatures();
        }
        int[][] mat = getDataFeatureMat();
        ClassificationTreeBuilder ctb = new ClassificationTreeBuilder(mat);
        
        try{
            ctb.setDrivingFeatures(drivingFeatures);
        	ctb.buildTree();
        	graph = ctb.printTree_json();
        	

        } catch(Exception e){
            e.printStackTrace();
        }
        
        return graph;
    }
    
    
    public String buildTree_Weka() { // using WEKA
  
        String graph="";
//        long t0 = System.currentTimeMillis();
        J48 tree = new J48();
        getDrivingFeatures();
        getDataFeatureMat();
        try{
            
            FastVector attributes = setDataFormat();
            Instances dataset = new Instances("Tree_dataset", attributes, 100000);
            dataset.setClassIndex(dataset.numAttributes()-1);
            dataset = addData(dataset);
            dataset.compactify();

//            // save as CSV
//            CSVSaver saver = new CSVSaver();
//            saver.setInstances(dataset);
//            saver.setFile(new File(Params.path + "\\tmp_treeData.clp"));
//            saver.writeBatch();
            
            System.out.println("numAttributes: " + dataset.numAttributes());
            System.out.println("num instances: " + dataset.numInstances());
            
            String [] options = new String[2];
            options[0] = "-C";
            options[1] = "0.05";
            tree.setOptions(options);
            
//            Evaluation eval = new Evaluation(dataset);
//            eval.crossValidateModel(tree, dataset, 10, new Random(1));
            tree.buildClassifier(dataset);
            
//            System.out.println(eval.toSummaryString("\nResults\n\n", false));
//            System.out.println(eval.toMatrixString());
//            System.out.println(tree.toSummaryString());
//            String summary = tree.toSummaryString();
//            String evalSummary = eval.toSummaryString("\nResults\n\n", false);
//            String confusion = eval.toMatrixString();
            graph = tree.graph();
            

            
//Number of leaves: 21
//Size of the tree: 41
//Results
//Correctly Classified Instances        2550               97.3654 %
//Incorrectly Classified Instances        69                2.6346 %
//Kappa statistic                          0.9385
//Mean absolute error                      0.0418
//Root mean squared error                  0.1603
//Relative absolute error                  9.6708 %
//Root relative squared error             34.4579 %
//Total Number of Instances             2619
//=== Confusion Matrix ===
//    a    b   <-- classified as
// 1771   19 |    a = false
//   50  779 |    b = true

            
            
//            System.out.println(graph);
            
//            TreeVisualizer tv = new TreeVisualizer(null, tree.graph(), new PlaceNode2());
//            JFrame jf = new JFrame("Weka Classifier Tree Visualizer: J48");
//            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            jf.setSize(800, 600);
//            jf.getContentPane().setLayout(new BorderLayout());
//            jf.getContentPane().add(tv, BorderLayout.CENTER);
//            jf.setVisible(true);
//            // adjust tree
//            tv.fitToScreen();
            
//            long t1 = System.currentTimeMillis();
//            System.out.println( "Tree building done in: " + String.valueOf(t1-t0) + " msec");
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return graph;
    }
    

    
    public void addUserDefFilter(String name, String expression){
        this.userDef.add(new DrivingFeature(name,expression));
    }
    
    

}
