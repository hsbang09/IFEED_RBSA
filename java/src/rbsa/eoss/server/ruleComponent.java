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
class ruleComponent{
        
        private String type;  // rule, LHS (patterns(slot), tests), action (intermediate, final)
        private String name;
        private String variable;
        private String content;
        private ArrayList<ruleComponent> children = new ArrayList<>();
        
        public ruleComponent(String type){
            this.type = type;
        }
        public ruleComponent(String type, String name){
            this.type = type;
            this.name = name;
        }        
        public ruleComponent(String type, String name, String content){
            this.type = type;
            this.content = content;
            this.name = name;
        }     
        
        
        public void setContent(String content){
            this.content = content;
        }
        public void setVariable(String variable){
            this.variable = variable;
        }
        public void setName(String name){
            this.name = name;
        }
        
        public void addToChildren(ruleComponent rc){
            this.children.add(rc);
        }
    
    }