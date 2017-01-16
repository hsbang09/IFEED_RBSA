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
class RuleComponent{
        
        private String type;  // rule, LHS (patterns(slot), tests), action (intermediate, final)
        private String name;
        private String variable;
        private String content;
        private ArrayList<RuleComponent> children = new ArrayList<>();
        
        public RuleComponent(String type){
            this.type = type;
        }
        public RuleComponent(String type, String name){
            this.type = type;
            this.name = name;
        }        
        public RuleComponent(String type, String name, String content){
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
        
        public void addToChildren(RuleComponent rc){
            this.children.add(rc);
        }
    
    }