/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.io.File;

/**
 *
 * @author bang
 */
public class JAISTest {
    
    public static void main(String[] args){
        
    	// To generate driving features, first initialize DrivingFeaturesGenerator
    	// All the configurations can be checked and modified from DrivingFeaturesParams. The paths to input and output files can be modified here.
    	// Running getDrivingFeatures() will result in generating a file containing driving features, but they will be written in an arbitrary order.
    	// Running sortDrivingFeatures(int index) will result in generating a file with driving features ordered by the metric specified by the index.
    	// (0: support, 1:lift)
        
        String labledDataFile = "/Users/bang/workspace/JAIS/data/dataset_normal.txt";
        String featureDataFile = "/Users/bang/workspace/JAIS/data/extracted_features-mrmr.csv";
        
        //DrivingFeaturesGenerator dfg = new DrivingFeaturesGenerator(60);
        
        
        
        // Find driving features
        // Sort driving features based on the metric of your choice (0: support, 1: lift, 2: confidence)
//        dfg.getDrivingFeatures(labledDataFile, featureDataFile, 4);

    
    }
    

    
    
}