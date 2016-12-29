/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;

import java.util.ArrayList;
import java.util.HashMap;

import jess.Defrule;
import jess.Rete;

/**
 *
 * @author Bang
 */
import rbsa.eoss.ActionAnalyzer;
import rbsa.eoss.ConditionalElementAnalyzer;
import rbsa.eoss.QueryBuilder;
import rbsa.eoss.ruleAnalyzer;
import rbsa.eoss.local.Params;

public class ruleWebInterface {
    
    ruleAnalyzer ra;
    ActionAnalyzer aa;
    ConditionalElementAnalyzer cea;
    Rete r;
    QueryBuilder qb;
    int numOfPat;
    ruleComponent rule;
    
    public ruleWebInterface(Rete r){
        this.r = r;
    }
    public ruleWebInterface(int ruleID, Rete r, QueryBuilder qb){
        this.r = r;
        this.qb = qb;
        String ruleName = Params.rules_IDtoName_map.get(ruleID);
        Defrule thisRule = Params.rules_defrule_map.get(ruleName);
        String ruleDoc = thisRule.getDocstring();
        
        ra = new ruleAnalyzer(thisRule,r,qb);
        aa = ra.getActionAnalyzer();
        cea = ra.getConditionalElementAnalyzer();
        
        numOfPat = ra.getNumberOfPatterns();
        
        rule = new ruleComponent("rule",ruleName,ruleDoc);
        ruleComponent LHS = new ruleComponent("LHS");
        ruleComponent RHS = new ruleComponent("RHS");
        
        for (int i=0;i<numOfPat;i++){
            String patternName = cea.getPattern(i).getName();
            String patternBoundVar = cea.getPattern(i).getBoundName();
            
            ruleComponent pattern = new ruleComponent("pattern",patternName);
            pattern.setVariable(patternBoundVar);
            
            ArrayList<String> slotNames = cea.getAllSlotNames(cea.getPattern(i));
            int numOfTests = cea.getNTests(cea.getPattern(i));
            for (String slotName:slotNames){
                if (slotName.equalsIgnoreCase("factHistory")) continue;
                
                ruleComponent slot = new ruleComponent("slot",slotName);
                String content = "";

                ArrayList<String> testSummary = cea.getTestSummary(cea.getPattern(i));
                for (String test1:testSummary){
//                    System.out.println(test1);
                    String[] testSplit = test1.split(" ;;;; ");
                    if((slotName.equalsIgnoreCase(testSplit[0])) && (testSplit[1].equals("EQ"))){
                        if (testSplit[2].startsWith("?")){
                            continue;
                        } else if(content.equalsIgnoreCase("")){
                            content = testSplit[2];
                        }else{
                            content = content + " " +testSplit[2];
                        } 
                    }
                }
                String variable = cea.getSlotVariablePair(cea.getPattern(i)).get(slotName);
                
                slot.setContent(content);
                slot.setVariable(variable);
                pattern.addToChildren(slot);
            }
            LHS.addToChildren(pattern);
        }
        
        ArrayList<String> finalActions = ra.getFinalActions();
        ArrayList<String> intermediateActions = ra.getIntermediateActions();        
        
        for (String action:intermediateActions){
            ruleComponent intermediateAction = new ruleComponent("intermediateAction");
            intermediateAction.setContent(action);
            RHS.addToChildren(intermediateAction);
        }
        for (int i = 0;i<finalActions.size();i++){

            String finalActionType = aa.getFinalActionClassifier().get(i);
            String variable = "";
            String name = "";
            String action = aa.getTargetFact().get(i);
            String[] actionSplit = action.split(" ", 2);
            
            ruleComponent finalAction = new ruleComponent("finalAction");
            finalAction.setContent(finalActionType);
            if (finalActionType.equalsIgnoreCase("modify")){
                variable = actionSplit[0];   
                finalAction.setVariable(variable);
            } else if(finalActionType.equalsIgnoreCase("duplicate")){
                variable = actionSplit[0];
                finalAction.setVariable(variable);
            } else {
                name = actionSplit[0];
                finalAction.setName(name);
            }
            int numOfSlots = aa.getNumOfSlots(actionSplit[1]);
            ArrayList<String> slotNames = aa.getSlotNamesOfFinalAction(i);
            HashMap<String,String> slotContents = aa.getSlotContentsOfFinalAction(i);
            for (int j=0;j<numOfSlots;j++){
                String slotName = slotNames.get(j);
                if (slotName.equalsIgnoreCase("factHistory")) continue;
                String slotContent = slotContents.get(slotName);
                ruleComponent slot = new ruleComponent("slot",slotName,slotContent);
                finalAction.addToChildren(slot);
            }
            
            RHS.addToChildren(finalAction);
        }
        
        rule.addToChildren(LHS);
        rule.addToChildren(RHS);
    }
    
    public ruleComponent getRuleObj(){
        return this.rule;
    }
    
 
    
}
