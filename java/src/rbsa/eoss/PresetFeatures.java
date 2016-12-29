/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import java.util.ArrayList;
/**
 *
 * @author Bang
 */
public class PresetFeatures {
    
    private String name;
    private int order;
    private ArrayList data;
    private ArrayList support;
    private ArrayList confidence; // {feature} -> {selection}
    private ArrayList confidence2;// {selection} -> {feature}
    private ArrayList lift;
    
    public PresetFeatures(String name,int order){
        this.name = name;
        this.order = order;
        data = new ArrayList<int[]>();
        support = new ArrayList<String>();
        confidence = new ArrayList<String>();
        confidence2 = new ArrayList<String>();
        lift = new ArrayList<String>();
    }
    
    public void addData(int[] input){
        data.add(input);
    }
    public void addData(int[] input,double lift){
        data.add(input);
        this.lift.add(Double.toString(lift));
    }
    public void addData(int[] input,double supp, double conf, double conf2, double lift){
        data.add(input);
        this.lift.add(Double.toString(lift));
        this.support.add(Double.toString(supp));
        this.confidence.add(Double.toString(conf));
        this.confidence2.add(Double.toString(conf2));
    }
    
    public String getName(){
        return name;
    }
    public int getOrder(){
        return order;
    }
    public ArrayList getData(){
        return data;
    }
    public ArrayList getSupport(){
        return support;
    }
    public ArrayList getConfidence(){
        return confidence;
    }
    public ArrayList getConfidence2(){
        return confidence2;
    }
    public ArrayList getLift(){
        return lift;
    }
}



