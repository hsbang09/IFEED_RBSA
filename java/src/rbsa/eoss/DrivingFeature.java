/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

/**
 *
 * @author Bang
 */
 
public class DrivingFeature{
        
        private int DFID; // id number
        private String name; // specific names
        private String expression; // inOrbit, together, separate, present, absent, etc.
        private boolean preset;
        private double[] metrics;
        

        public DrivingFeature(int id, String name, String expression){
            this.DFID=id;
            this.name = name;
            this.expression=expression;
            this.preset = false;
        }
        public DrivingFeature(int id, String name, String expression, double[] metrics, boolean preset){
            this.DFID=id;
            this.name = name;
            this.expression = expression;
            this.metrics = metrics;
            this.preset = preset;
        }

        public String getExpression(){return expression;}
        public String getName(){return name;}
        public double[] getMetrics(){return metrics;}
        public boolean isPreset(){return preset;}
        public int getID(){return this.DFID;}
        
    }
