/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Bang
 */
import jess.Defrule;
import jess.Rete;

public class ActionAnalyzer {
    
    private Defrule targetRule;
    private ArrayList<String> intermediateActions;  // does not include the final action
    private int numActions;
    private ArrayList<String> finalActionClassifier;   // assert = 1, modify = 2, duplicate = 3
    private ArrayList<String> finalVariables;
    private ArrayList<String> finalAction;
    
    private Rete r;
    private QueryBuilder qb;
    

    public ActionAnalyzer(){
    }
    
    public ActionAnalyzer(Defrule inputRule, Rete r, QueryBuilder qb){
        targetRule = inputRule;
        this.r = r;
        this.qb = qb;
        numActions = targetRule.getNActions();
        intermediateActions = new ArrayList<>();
        finalAction = new ArrayList<>();
        finalActionClassifier = new ArrayList<>();
        
        
        for (int i = 0;i<numActions;i++){
            String currentString =  targetRule.getAction(i).toStringWithParens();
            //System.out.println(currentString);
            
            if (currentString.contains("assert")){
                finalAction.add(currentString.trim());
                finalActionClassifier.add("assert");
            } else if (currentString.contains("modify")){
                finalAction.add(currentString.trim());
                finalActionClassifier.add("modify");
            } else if (currentString.contains("duplicate")){
                finalAction.add(currentString.trim());
                finalActionClassifier.add("duplicate");
            }
            else {
                intermediateActions.add(currentString);
            }
            
        }
    }
    
    public ArrayList<String> getFinalActionClassifier(){
        return finalActionClassifier;
    }
    public ArrayList<String> getIntermediateActions(){
        return intermediateActions;
    }
    
    public String findActionClassifier(String action){
        String currentString = action;
        if (currentString.contains("assert")){
            return "assert";
            } else if (currentString.contains("modify")){
                return "modify";
            } else if (currentString.contains("duplicate")){
                return "duplicate";
            }
        return "not a fincal action";
    }
    

    public ArrayList<String> getTargetFact(){
        ArrayList<String> targetFacts = new ArrayList<>();
        int leng = finalAction.size();
        for (int i=0;i<leng;i++){
            if (finalActionClassifier.get(i).equals("assert")){
                targetFacts.add(getInsideParen(finalAction.get(i),2)); // for assert
            } else if (finalActionClassifier.get(i).equals("modify")){
                targetFacts.add(getInsideParen(finalAction.get(i),1).split(" ",2)[1]); // for modify
            } else if (finalActionClassifier.get(i).equals("duplicate")){
                targetFacts.add(getInsideParen(finalAction.get(i),1).split(" ",2)[1]); // for duplicate
            }
        }
        return targetFacts;
    }
    
    public String convertRHSTargetFactVariableToFactName(String factVariable){
        ConditionalElementAnalyzer cea = new ConditionalElementAnalyzer(targetRule,r,qb);
        int save = -1;
        for (int i = 0;i<cea.getNPattern();i++){
            if (cea.getPatternBoundNames().get(i).equals(factVariable)){
                save = i;
                break;
            }
        }
        return cea.getPattern(save).getName();
    }
    
    public ArrayList<String> getTargetFactName(){
        ArrayList<String> targetFactNames = new ArrayList<>();
        int leng = getTargetFact().size();
        for (int i=0;i<leng;i++){
            int[] loc = locateParen(getTargetFact().get(i),1);
            String targetFactName = getTargetFact().get(i).substring(0,loc[0]).trim();
            if (targetFactName.startsWith("?")){
                targetFactName = convertRHSTargetFactVariableToFactName(targetFactName);
            }
            targetFactNames.add(targetFactName);
        }
        return targetFactNames;
    }
    
    public HashMap <String,String> getSlotContentsOfFinalAction(int index){
        //get number of modifiedSlots
        int nSlots = getNumOfSlots(getTargetFact().get(index));
        //get slot names and contents
        HashMap <String,String> slotContents = new HashMap<>();
        for (int i =0;i<nSlots;i++){
            String currentSlot = getInsideParen(getTargetFact().get(index),i+1,1);
            String[] temp = currentSlot.split(" ",2);
            slotContents.put(temp[0], temp[1]); // (slotName,slotContent)
        }
        return slotContents;
    }
    
    public ArrayList<String> getSlotNamesOfFinalAction(int index){
        //get number of modifiedSlots
        int nSlots = getNumOfSlots(getTargetFact().get(index));
        //get slot names and contents
        ArrayList <String> slotNames = new ArrayList<>();
        for (int i =0;i<nSlots;i++){
            String currentSlot = getInsideParen(getTargetFact().get(index),i+1,1);
            String[] temp = currentSlot.split(" ",2);
            slotNames.add(temp[0]);
        }
        return slotNames;
    }
            
    public ArrayList<String> getFinalAction(){
        return finalAction;
    }
    
    public String getInsideParen(String inputString,int nth ,int level){
        
        if (checkParen(inputString) == false) return inputString; 
        int[] loc = locateParen(inputString, nth);
        String insideParen = inputString.substring(loc[0]+1, loc[1]);
        if (level == 1){
            return insideParen;
        } else {
            return getInsideParen(insideParen,1 ,level-1);
        }
    }
    public String getInsideParen(String inputString,int level){
        
        if (checkParen(inputString) == false) return inputString; 
        int[] loc = locateParen(inputString, 1);
        String insideParen = inputString.substring(loc[0]+1, loc[1]);
        if (level == 1){
            return insideParen;
        } else {
            return getInsideParen(insideParen,level-1);
        }
    }
    public int getNestedParenLevel(String inputString){
        int leng = inputString.length();
        int cnt = 0;
        int level = 0;
        int maxLevel = 0;
        
        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level > maxLevel) maxLevel = level;
            }
            if(inputString.charAt(i) == ')' ){
                level--;
            }
        }
        return maxLevel;
    }
    
    public int getNumOfSlots(String inputString){
        int leng = inputString.length();
        int cnt = 0;
        int level = 0;
        
        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level == 1) cnt++;
            }
            if(inputString.charAt(i) == ')' ){
                level--;
            }
        }
        return cnt;
    }
    
    public boolean checkParen(String inputString){
        int leng = inputString.length();
        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == '(') return true;
        }
        return false;
    }
   
    public int[] locateParen(String inputString,int index){ // locate (index)th parentheses
        
        int level = 0;
        int nth = 0;
        int leng = inputString.length();
        int[] parenLoc = new int[2];
        parenLoc[0] = 0;
        parenLoc[1] = 0;

        for (int i = 0; i<leng ;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level == 1) nth++;
                if ((nth == index) && (level == 1))  parenLoc[0] = i;
            }
            if(inputString.charAt(i) == ')' ){
                level--;
            }
            if((level == 0) && (nth == index)) {
                parenLoc[1] = i;
                break;
            }
        }
        return parenLoc;
    }
    public ArrayList<Integer> locateNestedParen(String inputString,int focusLevel){ // locate all parentheses at specified level
        
        int level = 0;
        int nth = 0;
        int leng = inputString.length();
        ArrayList<Integer> parenLoc = new ArrayList<>();

        for (int i = 0; i<leng ;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level == focusLevel)  parenLoc.add(i);
            }
            if(inputString.charAt(i) == ')' ){
                level--;
                if (level == focusLevel) parenLoc.add(i);
            }
        }
        return parenLoc;
    }
    
    public String collapseAllParenIntoSymbol(String inputExpression){
        
        if (checkParen(inputExpression) == false) return inputExpression; 
        int num = getNumOfSlots(inputExpression);
        String expression = inputExpression;
        
        for (int i = 0;i<num;i++){
            int[] loc = locateParen(expression,i+1);
            String s1 = expression.substring(0, loc[0]+1);
            String s2 = expression.substring(loc[1]);
            String symbol = "";
            for (int j = 0;j< loc[1]-loc[0]-1 ;j++) symbol = symbol.concat("X");
            expression = s1 + symbol + s2;
        }
        return expression;
    }
    
    public void addFinalVariable(String inputVariable){
        if(!finalVariables.contains(inputVariable))
            finalVariables.add(inputVariable);
    }
    public ArrayList<String> getFinalVariables(){
        return finalVariables;
    }
    
    public void setFinalVariables(){
        int NfinalActions = finalAction.size();
        ArrayList<String> slots;
        finalVariables = new ArrayList<>();
        
        for (int i=0;i<NfinalActions;i++){
             slots = getSlotNamesOfFinalAction(i);
            for (String slot : slots) {
                String slotContents = getSlotContentsOfFinalAction(i).get(slot);
                ArrayList<String> candidateFinalVars = findVariablesFromJessExpression(slotContents,finalVariables);
                for(String var:candidateFinalVars){
                	if(!finalVariables.contains(var)){
                		finalVariables.add(var);
                	}
                }
            }
        }
    }
    
    
    public ArrayList<String> getRelVarsFromSlotName(String slotName){
        
        ArrayList<String> relevantVariables = new ArrayList<>();
        int leng = finalAction.size();
        for(int i=0;i<leng;i++){ 
            String slotContent= getSlotContentsOfFinalAction(i).get(slotName);
            if(slotContent.length() == 0) continue;
            else{
                relevantVariables.addAll(findVariablesFromJessExpression(slotContent,new ArrayList<String>()));
            }
        }
        int leng2 = intermediateActions.size();
        for(int i=0;i<leng2;i++){
            String intermediateAction1 = intermediateActions.get(i);
            for (String var:relevantVariables){
                if(intermediateAction1.contains(var)){
                    relevantVariables.addAll(findVariablesFromJessExpression(intermediateAction1,new ArrayList<String>()));
                }
            }
        }
        for (String var:relevantVariables){
            System.out.println(var);
        }
        
        return relevantVariables;
    }
    
    
    
    public HashMap <String,String[]> getSlotName_VariablePair(int index){
        ArrayList<String> slotNames = getSlotNamesOfFinalAction(index);
        HashMap <String,String[]> slotName_VarMap = new HashMap<>();
        
        for(String slot1:slotNames){
            String slotContent = getSlotContentsOfFinalAction(index).get(slot1);
            ArrayList<String> slotVar =  findVariablesFromJessExpression(slotContent,new ArrayList<String>());
            int leng = slotVar.size();
            String [] slotVars = new String[leng];
            for (int i = 0;i<leng;i++){
                slotVars[i] = slotVar.get(i);
            }
            slotName_VarMap.put(slot1, slotVars);
        }
        
        return slotName_VarMap;
    }
    
    public boolean checkIF_THEN(String inputExpression){
        String expression = getInsideParen(inputExpression,1);
        expression = collapseAllParenIntoSymbol(expression);
        return (expression.contains("if")) && (expression.contains("then"));
    }
    public ArrayList<String> separateIF_THEN(String inputExpression){
        String expression = getInsideParen(inputExpression,1);
        ArrayList<String> separatedExpression = new ArrayList<>();
        int ifLoc = expression.indexOf("if");
        int thenLoc = expression.indexOf("then");
        String lhs = expression.substring(0, ifLoc);
        separatedExpression.add(lhs);
        String rhs = expression.substring(thenLoc);
        String rhsCopy = rhs;
        rhsCopy = collapseAllParenIntoSymbol(rhsCopy);
        int num = getNumOfSlots(rhsCopy);
        for (int i = 0;i<num;i++){
            int[] loc = locateParen(rhsCopy,i+1);
            String tmp = rhs.substring(loc[0],loc[1]+1);
            separatedExpression.add(tmp);
        }
        return separatedExpression;
    }
    
     public String rewriteJessExpression(String inputExpression){
    
        String function = "";
        String outputExpression = "";
        
        if(checkParen(inputExpression) == false) {
            return inputExpression;
        }
        
        String expression = getInsideParen(inputExpression,1,1);
        String[] functionExpressionPair = expression.split(" ",2);
        function = functionExpressionPair[0]; // separate function from the rest of the expression

        String restOfExpressionCollapsed = collapseAllParenIntoSymbol(functionExpressionPair[1]).trim();
        String[] tmp = restOfExpressionCollapsed.split(" ");
        
        if (tmp.length == 1){   // expression of the form: (function param1)
            String param = functionExpressionPair[1];
            outputExpression = function + " " + rewriteJessExpression(param) ;

        } else if(tmp.length ==2) { // common form of expression: (function param1 param2) 
            int param1Length = tmp[0].length();
            int param2Length = tmp[1].length();
            String param1 = functionExpressionPair[1].substring(0, param1Length);
            String param2 = functionExpressionPair[1].substring(param1Length + 1);
            outputExpression = rewriteJessExpression(param1) + " " + function + " " + rewriteJessExpression(param2) ;

        } else { // expression most likely related to handling lists: (function param1 param2 param3 ... paramN) 
            int numOfParams = tmp.length;
            String[] params = new String[numOfParams];
            int cumulativeLength = 0;
            outputExpression = function;
            for (int i=0;i<numOfParams;i++){
                int length = tmp[i].length();
                params[i] = functionExpressionPair[1].substring(cumulativeLength,cumulativeLength+length);
                cumulativeLength = cumulativeLength + length + 1;
                outputExpression =  outputExpression.concat(" " + rewriteJessExpression(params[i])) ;
            }
        }

        return "(" + outputExpression + ")";
    }
     public String rewriteJessExpression2(String inputExpression){
    
        String function = "";
        String outputExpression = "";
        
        if(checkParen(inputExpression) == false) {
            return inputExpression;
        }
        
        String expression = getInsideParen(inputExpression,1,1);
        String[] functionExpressionPair = expression.split(" ",2);
        function = functionExpressionPair[0]; // separate function from the rest of the expression

        String restOfExpressionCollapsed = collapseAllParenIntoSymbol(functionExpressionPair[1]).trim();
        String[] tmp = restOfExpressionCollapsed.split(" ");
        
        if (tmp.length == 1){   // expression of the form: (function param1)
            String param = functionExpressionPair[1];
            outputExpression = function + " applied to " + rewriteJessExpression2(param) ;

        } else if(tmp.length ==2) { // common form of expression: (function param1 param2) 
            int param1Length = tmp[0].length();
            int param2Length = tmp[1].length();
            String param1 = functionExpressionPair[1].substring(0, param1Length);
            String param2 = functionExpressionPair[1].substring(param1Length + 1);
            outputExpression = function + " applied to " + rewriteJessExpression2(param1) + " and " + rewriteJessExpression2(param2) ;

        } else { // expression most likely related to handling lists: (function param1 param2 param3 ... paramN) 
            int numOfParams = tmp.length;
            String[] params = new String[numOfParams];
            int cumulativeLength = 0;
            outputExpression = function + " applied to multiple variables: ";
            for (int i=0;i<numOfParams;i++){
                int length = tmp[i].length();
                params[i] = functionExpressionPair[1].substring(cumulativeLength,cumulativeLength+length);
                cumulativeLength = cumulativeLength + length + 1;
                outputExpression =  outputExpression.concat(" " + rewriteJessExpression2(params[i])) ;
            }
        }

        return "(" + outputExpression + ")";
    }
    
    public ArrayList<String> findVariablesFromJessExpression(String inputExpression, ArrayList<String> varList){
        
        String function = "";
        
        if(checkParen(inputExpression) == false) {
            ArrayList<String> newVarList = varList;
            if (inputExpression.startsWith("?")){
                String[] tmpExpressions = inputExpression.split(" ");
                for (String exp : tmpExpressions){
                    if (!newVarList.contains(exp)){
                    newVarList.add(inputExpression);
                    }
                }
            }

            return newVarList;
        }
        
        String expression = getInsideParen(inputExpression,1,1);
        String[] functionExpressionPair = expression.split(" ",2);
        function = functionExpressionPair[0]; // separate function from the rest of the expression

        String restOfExpressionCollapsed = collapseAllParenIntoSymbol(functionExpressionPair[1]).trim();
        String[] tmp = restOfExpressionCollapsed.split(" ");
        
        if (tmp.length == 1){   // expression of the form: (function param1)
            String param = functionExpressionPair[1];
            return findVariablesFromJessExpression(param,varList);

        } else if(tmp.length ==2) { // common form of expression: (function param1 param2) 
            int param1Length = tmp[0].length();
            int param2Length = tmp[1].length();
            String param1 = functionExpressionPair[1].substring(0, param1Length);
            String param2 = functionExpressionPair[1].substring(param1Length + 1);
            ArrayList<String> newVars = findVariablesFromJessExpression(param1,varList);
            ArrayList<String> newVars2 = findVariablesFromJessExpression(param2,newVars);
            return newVars2;
        } else { // expression most likely related to handling lists: (function param1 param2 param3 ... paramN) 
            int numOfParams = tmp.length;
            String[] params = new String[numOfParams];
            int cumulativeLength = 0;
            ArrayList<String> newVars = varList;
            for (int i=0;i<numOfParams;i++){
                int length = tmp[i].length();
                params[i] = functionExpressionPair[1].substring(cumulativeLength,cumulativeLength+length);
                cumulativeLength = cumulativeLength + length + 1;
                newVars = findVariablesFromJessExpression(params[i],newVars);
            }
            return newVars;
        }
    }
    
    
    
}



