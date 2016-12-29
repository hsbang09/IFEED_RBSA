/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;



import java.util.ArrayList;
import java.util.HashMap;

import jess.Defrule;
import jess.Fact;
import jess.Rete;


public class ruleAnalyzer {
    private Rete r;
    private QueryBuilder qb;
    private Defrule targetRule;
    private int nop; //Number of patterns
    private ArrayList<String> patternBoundNames;
    private ArrayList<String> initialVariables;
    private ArrayList<String> finalVariables;
    private ArrayList<Integer> NTestsInPattern; //Number of tests for each pattern
    private ArrayList<String> intermediateActions;
    private ArrayList<String> finalActions;
    private ArrayList<String> finalActionClassifiers;
    private ConditionalElementAnalyzer cea;
    private ActionAnalyzer aa;
    
    
    public ruleAnalyzer(){
    }
    public ruleAnalyzer(Defrule inputRule,Rete r, QueryBuilder qb){
        this.r = r;
        this.qb = qb;
        this.targetRule = inputRule;
        ArrayList <String> conditionTests;
        NTestsInPattern = new ArrayList<>();
        initialVariables = new ArrayList<>();

        cea = new ConditionalElementAnalyzer(targetRule,r,qb);
        aa = new ActionAnalyzer(targetRule,r,qb);
        
        
        nop = cea.getNPattern();
        patternBoundNames = cea.getPatternBoundNames();
        for (int i = 0;i<nop;i++){
            jess.Pattern currentPattern = cea.getPattern(i);
        
            conditionTests = cea.getTestSummary(currentPattern);
            int leng = conditionTests.size();
            NTestsInPattern.add(leng);
            for (int j= 0;j<leng;j++){
                String tmp = conditionTests.get(j);
                String[] tmpSplit = tmp.split(" ;;;; ");
                initialVariables.add(tmpSplit[2]);
            }
        }
        finalActions = aa.getFinalAction();
        finalActionClassifiers = aa.getFinalActionClassifier();
        intermediateActions = aa.getIntermediateActions();
    }
    
  //  R290 {R268 {R289 {R407 {R404 {R403 {R406 {R405 {R286 {R266 {R267 {R287 F731}}}}}}}}}}}} {F234}
    

    
    public boolean checkModificationOnSlot(String slotName){
        int leng = finalActions.size();
        for (int i=0;i<leng;i++){
            if (aa.getSlotNamesOfFinalAction(i).contains(slotName))
                return true;
        }
        return false;
    }
    public String getRelevantFinalAction(String slotName){
        int leng = finalActions.size();
        for (int i=0;i<leng;i++){
            if (aa.getSlotNamesOfFinalAction(i).contains(slotName))
                return finalActions.get(i);
        }
        return "";
    }
    
    public boolean checkInheritance(String slotName){
        int leng = finalActions.size();
        for (int i=0;i<leng;i++){
            String finalf = finalActions.get(i);
            if (finalf.contains(slotName)){
                int i1 = finalf.indexOf(slotName);
                String contents = finalf.substring(i1+slotName.length()+1).trim();
                contents = contents.substring(0,contents.indexOf(")")).trim();
                if ((contents.startsWith("?")) && cea.getAllTestVariables().contains(contents)){
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public HashMap<String, String> getRHSFactVariableToLHSFactName(){
        HashMap<String,String> rhsVarTolhsVar = new HashMap<>();
        
        int leng = finalActions.size();
        for (int i=0;i<leng;i++){
            if((finalActionClassifiers.get(i).equalsIgnoreCase("modify")) || (finalActionClassifiers.get(i).equalsIgnoreCase("duplicate"))){
                String rhsBoundName = aa.getInsideParen(finalActions.get(i), 1).split(" ")[1];
//                System.out.println("rhsboundName: " + rhsBoundName);
                int leng2 = cea.getNPattern();
                for (int j=0;j<leng2;j++){
                    String lhsBoundName = "?"+cea.getPattern(j).getBoundName();
//                    System.out.println("lhsboundName: " + lhsBoundName);
                    if (lhsBoundName.equalsIgnoreCase(rhsBoundName)){
                        rhsVarTolhsVar.put(rhsBoundName,cea.getPattern(j).getName());
                    }
                }
            }
        }
        return rhsVarTolhsVar;
    }
    
    public String getFactBoundNameFromFinalAction(String finalAction){
        String boundName="";
        if ((finalAction.contains("modify"))||(finalAction.contains("duplicate"))){
            boundName = aa.getInsideParen(finalAction, 1).split(" ")[1];
        }
        return boundName;
    }
    
    
    public ArrayList<String> getActionsRelatedToFinalFactSlot(String slotName){
        //find intermediate actions that build up to the final variable
        int leng = finalActions.size();
        ArrayList<String> relatedActions = new ArrayList<>();
        for (int i=0;i<leng;i++){
            String[] vars = aa.getSlotName_VariablePair(i).get(slotName);
            for (String var:vars){
                relatedActions.addAll(getActionsRelatedtoVariable(var,intermediateActions,relatedActions));
            }
        }
        return relatedActions;
    }
    
    private ArrayList<String> getActionsRelatedtoVariable(String var, ArrayList<String> inputActions, ArrayList<String> RelatedActions){
       
        ArrayList<String> ActionsToConsider = inputActions;
         
        // remove last action
        if (ActionsToConsider.size() == intermediateActions.size()){
            ActionsToConsider.remove(ActionsToConsider.size()-1);
        }
        ArrayList<String> newActionsToConsider = ActionsToConsider;
        ArrayList<String> newVarsToConsider = new ArrayList<>();
        ArrayList<String> RelatedActions1 = RelatedActions;
        
       int leng = ActionsToConsider.size();
        for (int j = 0;j<leng-1;j++){
            String action = ActionsToConsider.get(j);
            if((action.contains(var)) && !RelatedActions1.contains(action)){
                
                if (aa.checkIF_THEN(action)){  // Consider this action relevant when the variable is on the RHS
                    boolean rhsContainsVar = false;
                    ArrayList<String> separatedActions = aa.separateIF_THEN(action);
                    int num = separatedActions.size();
                    for (int i = 1;i<num;i++){
                        if(separatedActions.get(i).contains(var)){
                            rhsContainsVar = true;
                        }
                    }
                    if (rhsContainsVar == false){
                        continue;
                    }
                }
                RelatedActions1.add(action);
                newActionsToConsider.remove(action);
                newVarsToConsider.addAll(aa.findVariablesFromJessExpression(action, new ArrayList<String>()));
            } 
        }
        for (String newVar:newVarsToConsider){
            if (!newVar.equalsIgnoreCase(var)){
                RelatedActions1 = getActionsRelatedtoVariable(newVar,newActionsToConsider,RelatedActions1);
            }
        }
        return RelatedActions1;
       
    }
    public String getActionContainingDesiredExpression(String expression){
        for (String action:intermediateActions){
            if (action.contains(expression)){
                return action;
            }
        }
        return "";
    }
    
    public HashMap<String,ArrayList<String>> getRelevantLHSFactsToSlots(String slotName){
      
        HashMap<String,ArrayList<String>> relevantFactsAndSlots = new HashMap<>();
        ArrayList<String> relevantVariables = aa.getRelVarsFromSlotName(slotName);
        
        int NPatterns = cea.getNPattern();
        for (int i=0;i<NPatterns;i++){
            jess.Pattern pat = cea.getPattern(i);
            String patternName = pat.getName();
            ArrayList<String> testSummary = cea.getTestSummary(pat);
            ArrayList<String> relevantSlots = new ArrayList<>();
            
            for (String test:testSummary){
                boolean flag=false;
                for(String var:relevantVariables) {
                    if (test.contains(var)){
                        flag=true;
                        break;
                    }
                }
                if(flag==true){
                    String[] testSplit = test.split(" ;;;; ");
                    relevantSlots.add(testSplit[0]);
                }
            }
            relevantFactsAndSlots.put(patternName,relevantSlots);
        }
        return relevantFactsAndSlots;
    }
    public ArrayList<String> getRelevantLHSFacts(String slotName){

        ArrayList<String> relevantVariables = aa.getRelVarsFromSlotName(slotName);
        ArrayList<String> relevantFacts = new ArrayList<>();
        
        
        int NPatterns = cea.getNPattern();
        for (int i=0;i<NPatterns;i++){
            boolean flag=false;
            jess.Pattern pat = cea.getPattern(i);
            String patternName = pat.getName();
            ArrayList<String> testSummary = cea.getTestSummary(pat);
            ArrayList<String> relevantSlots = new ArrayList<>();
            
            for (String test:testSummary){
                for(String var:relevantVariables) {
                    if (test.contains(var)){
                        flag=true;
                        break;
                    }
                }
                if(flag==true){
                    break;
                }
            }
            if (flag==true){
                relevantFacts.add(patternName);
            }
        }
        return relevantFacts;
    }
    
//    public ArrayList<Fact> getRelevantLHSFacts(String slotName){   //--> old ver
//        
//        ArrayList<String> testVars = cea.getAllTestVariables();
//        ArrayList<Fact> relFacts = new ArrayList<>();
//            
//        int NPatterns = cea.getNPattern();
//        for (int i=0;i<NPatterns;i++){
//            jess.Pattern pat = cea.getPattern(i);
//            String patternName = pat.getName();
//            String tmp = "";
//            for (String var:testVars){
//                if (!var.startsWith("?")){
//                    String testSlot = cea.getVariableSlotPair(pat).get(var);
//                    if (!testSlot.equalsIgnoreCase("")){
//                        tmp = tmp + " (" + testSlot + " " + var + ")";
//                    }
//                }
//            }
//            relFacts.addAll(qb.makeQuery(patternName + tmp));
//        }
//        return relFacts;
//    }
    
    public void printDefrule(String ruleName){
        try{
        r.eval("(printout t (ppdefrule "+ ruleName +") crlf)");
        // 
        }catch (Exception e) {
            System.out.println( "EXC in printing defrule " +e.getMessage() );
        }
    }
    public void printFactWithSlots(Fact fact){
 
    }
    
    
    public String generateExplanation(String slotName){
        
        String outputExpression = slotName + " is obtained from ... \n";
        int save=0;
        for (int i =0; i<finalActions.size();i++){
            if (finalActions.get(i).contains(slotName)){
                save = i;
            }
        }
        String[] relVars = aa.getSlotName_VariablePair(save).get(slotName);
        String slotContent = aa.getSlotContentsOfFinalAction(save).get(slotName);
        
        if (slotContent.startsWith("?")){
            outputExpression = slotName + " represented by a variable "+ slotContent +" is obtained from ... \n";
            ArrayList<String> relActions = getActionsRelatedtoVariable(slotContent,intermediateActions,new ArrayList<String>());
            for(String action:relActions){
                outputExpression = outputExpression + aa.rewriteJessExpression2(action) + "\n";
            }
            for (String testVar:cea.getAllTestVariables()){
                if (outputExpression.contains(testVar)){
                    String[] tmp = cea.getVariabletoPatternSlotMap().get(testVar);
                    outputExpression = outputExpression + testVar + " is a slot value of " + tmp[1] + " in Fact " + tmp[0] + "\n" ;
                }
            }
        } else{
            for (String var:relVars){
                outputExpression = outputExpression + slotName + ": " + aa.rewriteJessExpression2(slotContent) + "\n";
                ArrayList<String> relActions = getActionsRelatedtoVariable(var,intermediateActions,new ArrayList<String>());
                for (String action:relActions){
                    outputExpression=outputExpression + aa.rewriteJessExpression2(action) + "\n";
                }
            }
            for (String testVar:cea.getAllTestVariables()){
                if (outputExpression.contains(testVar)){
                    String[] tmp = cea.getVariabletoPatternSlotMap().get(testVar);
                    outputExpression = outputExpression + testVar + " is the slot value of " + tmp[1] + " in Fact " + tmp[0] + "\n" ;
                }
            }
        }
        
        return outputExpression;
    }
    
    public String generateExplanationForVariable(String var){
        
        String outputExpression = "Variable " + var + " is obtained from ... \n";
        ArrayList<String> relActions = getActionsRelatedtoVariable(var,intermediateActions,new ArrayList<String>());
        
        for(String action:relActions){
            outputExpression = outputExpression + aa.rewriteJessExpression2(action) + "\n";
        }
        for (String testVar:cea.getAllTestVariables()){
            if (outputExpression.contains(testVar)){
                String[] tmp = cea.getVariabletoPatternSlotMap().get(testVar);
                outputExpression = outputExpression + testVar + " is a slot value of " + tmp[1] + " in Fact " + tmp[0] + "\n" ;
            }
        }
        
        return outputExpression;
    }
    
    public ConditionalElementAnalyzer getConditionalElementAnalyzer(){
        return cea;
    }
    public ActionAnalyzer getActionAnalyzer(){
        return aa;
    }
    public ArrayList<String> getInitialVariables(){
        return initialVariables;
    }
    public ArrayList<String> getPatternBoundNames(){
        return patternBoundNames;
    }
    public ArrayList<Integer> getNTestsInPattern(){
        return NTestsInPattern;
    }
    public int getNumberOfPatterns(){
        return nop;
    }
    public ArrayList<String> getIntermediateActions(){
        return intermediateActions;
    }
    public ArrayList<String> getFinalActions(){
        return finalActions;
    }
    public ArrayList<String> getFinalActionTypes(){
        return finalActionClassifiers;
    }
    
}
