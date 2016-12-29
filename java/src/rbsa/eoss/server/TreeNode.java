/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;

import java.util.ArrayList;

/**
 *
 * @author Bang
 */
    public class TreeNode{
        
        private String ID;
        private String name;
        private boolean cond;
        private int weight;   
        private int classifiedAsSelected;  // number of data points classified as selected
        private ArrayList<TreeNode> children;
        
        public TreeNode(){
            children = new ArrayList<>();
        }
        public TreeNode(String ID){
            this.ID=ID;
            weight=-1;
            classifiedAsSelected=-1;
            children = new ArrayList<>();
        }

        public TreeNode(String ID, String name){
            this.ID=ID;
            this.name=name;
            weight=-1;
            classifiedAsSelected=-1;
            children = new ArrayList<>();
        } 
        
        
        public void setName(String name){
            this.name = name;
        }
        public void setCond(boolean cond){
            this.cond = cond;
        }
        public void setWeight(int weight){
            this.weight=weight;
        }
        public void setClassifiedAsSelected(int n){
            this.classifiedAsSelected=n;
        }
        public void addChild(TreeNode c){
            if(!children.isEmpty()){
                if(!children.get(0).getCond()){  // true first
                    children.add(0,c);
                }else{
                    children.add(c);
                }
            }else{
                children.add(c);
            }
        }
        public ArrayList getChildren(){
            return children;
        }
        public String getID(){
            return ID;
        }
        public String getName(){
            return name;
        }
        public boolean getCond(){
            return cond;
        }
        public int getWeight(){
            return weight;
        }
        public int getClassifiedAsSelected(){
            return classifiedAsSelected;
        }
        
        
//    public treeNode makeTree(){
//        treeNode n1 = new treeNode("1");
//        n1.addChild(new treeNode("2"));
//        n1.addChild(new treeNode("3"));
//        treeNode n2 = (treeNode) n1.getChildren().get(0);
//        n2.addChild(new treeNode("4"));
//        n2.addChild(new treeNode("5"));
//        treeNode n3 = (treeNode) n1.getChildren().get(1);
//        n3.addChild(new treeNode("6"));
//        n3.addChild(new treeNode("7"));
//        return n1;
//    }
    
    public TreeNode findDescendent(TreeNode parent, String targetID){
        TreeNode tmp = new TreeNode("-1");
        if(parent.getID().equalsIgnoreCase(targetID)){
            return parent;
        }
        
        if(findChild(parent,targetID).getID().equalsIgnoreCase("-1")){
            if(parent.getChildren().isEmpty()){
            }else{
                if(parent.getChildren().size()==1){
                    TreeNode c1=findDescendent((TreeNode) parent.getChildren().get(0),targetID);
                    return c1;
                }else{
                    TreeNode c1=findDescendent((TreeNode) parent.getChildren().get(0),targetID);
                    TreeNode c2=findDescendent((TreeNode) parent.getChildren().get(1),targetID);
                    if(c1.getID().equalsIgnoreCase(targetID)){
                        return c1;
                    } else if(c2.getID().equalsIgnoreCase(targetID)){
                        return c2;
                    }
                }
            }
        } else {
           return findChild(parent,targetID);
        }
        return tmp;
    }
    
    public TreeNode findChild(TreeNode parent, String childID){
        TreeNode noMatch = new TreeNode("-1");
        if(parent.getChildren().isEmpty()){
            return noMatch;
        }else{
            for(TreeNode tn: (ArrayList<TreeNode>)parent.getChildren()){
                if(tn.getID().equalsIgnoreCase(childID)){
                    return tn;
                }
            }
        }
        return noMatch;
    }
    
    public void updateAllWeights(TreeNode rootNode){
        addWeightsFromChildren(rootNode);
    }
    
    public int addWeightsFromChildren(TreeNode parent){
        if(parent.getChildren().isEmpty()){
            return parent.getWeight();
        }else{
            int addedWeight=0;
            for(TreeNode tn:(ArrayList<TreeNode>) parent.getChildren()){
                if(tn.getWeight() < 0){
                    addedWeight += addWeightsFromChildren(tn);
                } else{
                    addedWeight += tn.getWeight();
                }
            }
            parent.setWeight(addedWeight);
            return addedWeight;
        }
    }
    
    public void updateAllClassifiedAsSelected(TreeNode rootNode){
        addClassifiedAsSelectedFromChildren(rootNode);
    }
    
    public int addClassifiedAsSelectedFromChildren(TreeNode parent){
        if(parent.getChildren().isEmpty()){
            return parent.getClassifiedAsSelected();
        }else{
            int addedvalue=0;
            for(TreeNode tn:(ArrayList<TreeNode>) parent.getChildren()){
                if(tn.getClassifiedAsSelected() < 0){
                    addedvalue += addClassifiedAsSelectedFromChildren(tn);
                } else{
                    addedvalue += tn.getClassifiedAsSelected();
                }
            }
            parent.setClassifiedAsSelected(addedvalue);
            return addedvalue;
        }
    }
}
