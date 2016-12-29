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
import jess.Fact;
import jess.Rete;
import jess.ValueVector;
import rbsa.eoss.local.Params;


public class DialogueHistory {
    
    private String CurrentContext; // Examples of a context format: ATM, ATM-10, OCE-3-1, TER-14-2-3
    private ArrayList<String>  PreviousContexts;
    
    private Rete r;
    private QueryBuilder qb;
    
    
    public DialogueHistory(){
    }
    public DialogueHistory(Rete r, QueryBuilder qb){
        CurrentContext = "Initialization";
        PreviousContexts = new ArrayList<String>();

        this.r = r;
        this.qb = qb;
       
        
        
    }
    
    
    public void newWhyQuestion(String inquiredID){
        
        String ComparePreviousContext = getPreviousContext();
        if(!ComparePreviousContext.equalsIgnoreCase(CurrentContext)){
            PreviousContexts.add(CurrentContext);
        }
        String prevContext = getPreviousContext();

        
        String stkhldrID = "";
        String objID = "";
        String subobjID = "";
        String attrID = "";
        ArrayList<Fact> queriedFacts = new ArrayList<>();
        String level = "";
        String nextLevel = "";
        
        String [] cntxtSplit = prevContext.split("-");
        int length = cntxtSplit.length;
        if (prevContext.equalsIgnoreCase("Initialization")){
            length = -1;
        }
        if (prevContext.equalsIgnoreCase("general")){
            length = 0;
        }
        try{
            switch(length){
                case -1: level = "INITIALIZE"; // Asking about STAKEHOLDER satisfaction scores
                    queriedFacts = qb.makeQuery("AGGREGATION::STAKEHOLDER");
                    nextLevel = "STAKEHOLDER";
                    break;
                case 0: level = "STAKEHOLDER"; // Asking about OBJECTIVE scores of certain stakeholder
                    stkhldrID = inquiredID;
                    queriedFacts = qb.makeQuery("AGGREGATION::OBJECTIVE (parent " + stkhldrID + ")");
                    nextLevel = "OBJECTIVE";
                    break;    
                case 1: level = "OBJECTIVE"; // Asking about SUBOBJECTIVE scores of certain objective
                    stkhldrID = cntxtSplit[0];
                    objID = inquiredID;
                    queriedFacts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (parent " + stkhldrID + objID + ") (satisfied-by ~nil) ");
                    nextLevel = "SUBOBJECTIVE";
                    break;
                case 2: level = "SUBOBJECTIVE"; // Asking about ATTRIBUTE scores of certain subobjective
                    stkhldrID = cntxtSplit[0];
                    objID = cntxtSplit[1];
                    subobjID = inquiredID;
                    queriedFacts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id " + stkhldrID + objID + "-" + subobjID + ") (satisfied-by ~nil) ");
                    nextLevel = "ATTRIBUTE";
                    break;
                case 3: level = "ATTRIBUTE"; // Asking how ATTRIBUTE score was obtained
                    stkhldrID = cntxtSplit[0];
                    objID = cntxtSplit[1];
                    subobjID = cntxtSplit[2];
                    attrID = inquiredID;
                    break;
                default: level = "N/A";break;    
                    }
            printExplanations(nextLevel,queriedFacts,stkhldrID, objID, subobjID);

        }  catch (Exception e) {
            System.out.println( "EXC in asking WHY question " +e.getMessage() );
        }
    // Examples of a context format: ATM, ATM-10, OCE-3-1, TER-14-2-3
        CurrentContext = stkhldrID.concat("-" + objID.concat("-" + subobjID.concat("-" + attrID)));
        while(true){
            if (CurrentContext.endsWith("-")){
                CurrentContext = CurrentContext.substring(0, CurrentContext.length()-1);
            } else {
                break;
            }
        }
        if (CurrentContext.length() == 0 || CurrentContext.equalsIgnoreCase("Initialization")){
            CurrentContext = "general";
        }
        System.out.println("Current context: " + CurrentContext + "\n");
        PreviousContexts.add(CurrentContext);
    }
    
    public void newHowQuestion(String queriedAttrName){
        
        String explanation = "";
        String [] cntxtSplit = CurrentContext.split("-");
        String stkhldrID = cntxtSplit[0];
        String objID = cntxtSplit[1];
        String subobjID = cntxtSplit[2];
        String targetID = stkhldrID + objID + "-" + subobjID;
        
        ArrayList <Fact> queriedFacts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id " + targetID + ") (satisfied-by ~nil) ");
        Fact targetFact = queriedFacts.get(0);
        String targetSlotName = queriedAttrName;
        int size = queriedFacts.size();
        double maxScore = 0;
        try{
            Fact maxScoreFact = queriedFacts.get(0);
                for (int i = 0;i<size;i++){
                    Fact CurrentFact = queriedFacts.get(i);
                    double tempScore = CurrentFact.getSlotValue("satisfaction").floatValue(r.getGlobalContext());
                    if (tempScore > maxScore){
                        maxScore = tempScore;
                        maxScoreFact = CurrentFact;
                    }
                }
            targetFact = maxScoreFact;
            factHistoryAnalyzer fha = new factHistoryAnalyzer(targetFact,targetSlotName,r,qb);
            
            String inheritRuleName = "";
            if ((Params.req_mode.equalsIgnoreCase("FUZZY-CASES")) || (Params.req_mode.equalsIgnoreCase("FUZZY-ATTRIBUTES"))) {
                inheritRuleName = "FUZZY-REQUIREMENTS::" + targetID + "-attrib";
            }
            else if ((Params.req_mode.equalsIgnoreCase("CRISP-CASES")) || (Params.req_mode.equalsIgnoreCase("CRISP-ATTRIBUTES"))) {
                inheritRuleName = "REQUIREMENTS::" + targetID + "-attrib";
            }
            
            
            String[] tmp = fha.traceRelevantRule_skipOne(inheritRuleName, targetSlotName, fha.getFactHistory());
            String relRuleName = tmp[0];
            String newFactID = tmp[1];
            
            Fact tmpFact = r.findFactByID(Integer.parseInt(newFactID));
            String newHistory = tmpFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
            
            jess.Defrule relRule = Params.rules_defrule_map.get(relRuleName);
            ruleAnalyzer ra = new ruleAnalyzer(relRule,r,qb);
            if(ra.checkInheritance(targetSlotName)){
                fha.setTargetFact(targetFact);
                String[] tmp2 = fha.traceInheritance(relRuleName, targetSlotName, newHistory);
                relRuleName = tmp2[0];
                newFactID = tmp2[1];
                tmpFact = r.findFactByID(Integer.parseInt(newFactID));
                newHistory = tmpFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                relRule = Params.rules_defrule_map.get(relRuleName);
                ra = new ruleAnalyzer(relRule,r,qb);
            } 
            
            String description = "";
            String docString = relRule.getDocstring();
            if (!docString.isEmpty()){
                description = description + docString + "\n";
            }
            
            explanation = explanation + targetSlotName + " value is calculated in rule [" + relRuleName + "] "
                    + "(ruleID: R"+ Params.rules_NametoID_Map.get(relRuleName) +")\n"
                    + description
                    + "Type in ruleID to further view the rule";
            
            PreviousContexts.add(CurrentContext);
            CurrentContext = CurrentContext + " F" + newFactID +"~R" + Params.rules_NametoID_Map.get(relRuleName);
            System.out.println(explanation);
            System.out.println("Current context: " + CurrentContext + "\n");            

        } catch(Exception e) {
            System.out.println( "EXC in HOW explanation " +e.getMessage() );
        }
    }
    
//    public void newHowQuestion(String queriedAttrName){
//        
//        String [] cntxtSplit = CurrentContext.split("-");
//        String stkhldrID = cntxtSplit[0];
//        String objID = cntxtSplit[1];
//        String subobjID = cntxtSplit[2];
//        String targetID = stkhldrID + objID + "-" + subobjID;
//        ArrayList <Fact> queriedFacts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id " + targetID + ") (satisfied-by ~nil) ");
//        Fact targetFact = queriedFacts.get(0);
//        String targetSlotName = queriedAttrName;
//        int size = queriedFacts.size();
//        double maxScore = 0;
//        try{
//            Fact maxScoreFact = queriedFacts.get(0);
//                for (int i = 0;i<size;i++){
//                    Fact CurrentFact = queriedFacts.get(i);
//                    double tempScore = CurrentFact.getSlotValue("satisfaction").floatValue(r.getGlobalContext());
//                    if (tempScore > maxScore){
//                        maxScore = tempScore;
//                        maxScoreFact = CurrentFact;
//                    }
//                }
//            targetFact = maxScoreFact;
//            factHistoryAnalyzer fha = new factHistoryAnalyzer(targetFact,targetSlotName,r,qb);
//            
//            String relRuleName = fha.getRelevantRule();
//            ruleAnalyzer ra = new ruleAnalyzer(Params.rules_defrule_map.get(relRuleName),r,qb);
//            if(ra.checkInheritance(targetSlotName)){
//                String factBoundName = ra.getFactBoundNameFromFinalAction(ra.getActionAnalyzer().getFinalAction().get(0));
//            } else {
//                
//            }
//            
//                String tmpHistory = maxScoreFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
//                tmpHistory = tmpHistory.replace('{', '(');
//                tmpHistory = tmpHistory.replace('}',')');
////                String innermost = aa.getInsideParen(tmpHistory, aa.getNestedParenLevel(tmpHistory));
////                String targetFactID = innermost.split(" ")[1].substring(1);
////                Fact targetFact = r.findFactByID(Integer.parseInt(targetFactID));
////                factHistoryAnalyzer fha = new factHistoryAnalyzer(targetFact,targetSlot,r,qb);
////                String relRuleName = fha.findRelevantRule(fha.getFactHistory(),fha.getTargetSlot());
////                targetRule = (Defrule) Params.rules_defrule_map.get(relRuleName);
////                System.out.println(relRuleName);
////                System.out.println(fha.traceInheritance(relRuleName, targetSlot));
//                
//            
//
//        } catch(Exception e) {
//            System.out.println( "EXC in HOW explanation " +e.getMessage() );
//        }
//    }
    

    
    
    public String traceBackContextinHistory(int num){  
        ArrayList <String> history = getDialogueHistory();
        String temp = history.get(history.size()-1-num);
        return temp;
    }
    public String traceBackContextinHierarchy(){
        String saveCurrent = CurrentContext;
        while(true){
            if (saveCurrent.length() == 1){
                saveCurrent = "general";
                break;
            }
            saveCurrent = saveCurrent.substring(0, saveCurrent.length()-1);
            if ((saveCurrent.endsWith("~")) || (saveCurrent.endsWith("-")) || (saveCurrent.endsWith(" "))){
                saveCurrent = saveCurrent.substring(0, saveCurrent.length()-1);
                break;
            }
        }
        return saveCurrent;
    }
    
    public void getHigherLevel(){
        String temp = traceBackContextinHierarchy();
        CurrentContext = temp;
        System.out.println("Currnet context: " + CurrentContext + "\n");
    }
    
    
    public void queryFact(String factName){
        String context = CurrentContext;
        String newFactID = "";
        String[] cntxtSplit = context.split(" ");
        String[] prevFactAndRule = cntxtSplit[cntxtSplit.length-1].split("~");
        String prevFactID = "";
        String prevRuleID = "";
        if (prevFactAndRule.length > 1){
            prevFactID = prevFactAndRule[0];
            prevRuleID = prevFactAndRule[1];
        }
        try{
            newFactID = prevFactID;
            String prevRuleName = Params.rules_IDtoName_map.get(Integer.parseInt(prevRuleID.substring(1)));
            Defrule prevRule = Params.rules_defrule_map.get(prevRuleName);
            Fact prevFact = r.findFactByID(Integer.parseInt(prevFactID.substring(1)));
            String factHis = prevFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
            ruleAnalyzer ra = new ruleAnalyzer(prevRule,r,qb);
            ActionAnalyzer aa = ra.getActionAnalyzer();
            
            ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
            jess.Pattern pat = cea.getPattern(0); //initialize
            int np = cea.getNPattern();
            for (int i=0;i<np;i++){
                pat = cea.getPattern(i);
                if (pat.getName().equalsIgnoreCase(factName)){
                    break;
                }
            }
            ArrayList<String> allSlots = cea.getAllSlotNames(pat); // obtain slots that appear on the LHS of the rule
            
            
            factHistoryAnalyzer fha = new factHistoryAnalyzer(prevFact,r,qb);
            String newfactHis = fha.findRuleInHistory(prevRuleID, factHis); // returns only the LHS info within factHistory
            newfactHis = newfactHis.split(" ",2)[1];  // getting rid of the rule ID
            newfactHis = aa.collapseAllParenIntoSymbol(newfactHis);
            
            
            int n = newfactHis.split(" ").length;
            Fact myFact = prevFact;
            for (int i=0;i<n;i++){
                if (n==1){
                    break;
                }
                String LHSfactID = newfactHis.split(" ")[i];
                if (LHSfactID.startsWith("(")){
                    continue;
                }
                newFactID = LHSfactID;
                LHSfactID = LHSfactID.substring(1);
                myFact = r.findFactByID(Integer.parseInt(LHSfactID));
                if (myFact.getName().equalsIgnoreCase(factName)){
                    break;
                }
            }
            
            String print = factName + " ";
            for(String slot:allSlots){
                if (slot.equalsIgnoreCase("factHistory")){
                    continue;
                }
                print = print + " ("+ slot + " " + myFact.getSlotValue(slot).toString()+ ")";
            }
            System.out.println(print);
            
        }catch(Exception e){
            System.out.println( "EXC in queryFact " +e.getMessage() );
        }
        PreviousContexts.add(CurrentContext);
        CurrentContext = CurrentContext + "~" + newFactID; 
        System.out.println("Currnet context: " + CurrentContext + "\n");
    }
    
    public void queryRule(int ruleID){
        String context = CurrentContext;
        boolean haveFactHistory = false;
        String[] cntxtSplit = context.split(" ");
        String[] prevFactAndRule = cntxtSplit[cntxtSplit.length-1].split("~");
        
        String prevFactID = "";
        String prevRuleID = "";
        if (prevFactAndRule.length > 1){
            prevFactID = prevFactAndRule[0];
            prevRuleID = prevFactAndRule[1];
            if ((prevRuleID.startsWith("R"))&&(prevRuleID.substring(1).equalsIgnoreCase(Integer.toString(ruleID)))){
                 haveFactHistory = true;
            }
        }

        
        String ruleName = Params.rules_IDtoName_map.get(ruleID);
        Defrule rule = Params.rules_defrule_map.get(ruleName);
        ruleAnalyzer ra = new ruleAnalyzer(rule,r,qb);
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        ra.printDefrule(ruleName);
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.println("If you want to view left-hand-side fact, type in the name of the fact");
        System.out.println("Currnet context: " + CurrentContext + "\n");
    }
    
    
    public void printExplanations(String level,ArrayList<Fact> queriedFacts, String stkhldrID, String objID, String subobjID){

        try{
            double sumOfScores = 0;
            double score = 0;
            String explanation = "";
            int size = queriedFacts.size();
            ArrayList<String> lowScoreFactsID = new ArrayList<>();
            
            HashMap<String, String> descriptions = new HashMap<>();
            descriptions.putAll(Params.objective_descriptions);
            descriptions.putAll(Params.subobj_descriptions);
            
            if (size == 0){
                System.out.println("No fact returned from the query");
            } else {
                
                if ((level.equalsIgnoreCase("OBJECTIVE")) || (level.equalsIgnoreCase("SUBOBJECTIVE"))){
                    System.out.println("Following " + level + "s were not satisfied");
                }
            
                double highestAttrScore = 0;
                Fact AttrTemp = new Fact(queriedFacts.get(0));
            
                for (int i = 0; i < size; i++){
                    Fact currentFact = queriedFacts.get(i);
                    int index = currentFact.getSlotValue("index").intValue(r.getGlobalContext());
                    String ID = currentFact.getSlotValue("id").stringValue(r.getGlobalContext());
                    score = currentFact.getSlotValue("satisfaction").floatValue(r.getGlobalContext());
                
                    if (level.equalsIgnoreCase("STAKEHOLDER")){
                        explanation = explanation.concat(index + ". " + ID + " Panel satisfaction: " + score + "\n");
                    }
                    if ((lowScoreFactsID.contains(ID) == false) && (score == 0) && ((level.equalsIgnoreCase("OBJECTIVE")) || (level.equalsIgnoreCase("SUBOBJECTIVE")))){
                        lowScoreFactsID.add(ID);
                        explanation = explanation.concat(index + ". " + ID + " " + descriptions.get(ID) + "\n");
                    }
                    if ((level.equalsIgnoreCase("ATTRIBUTE")) && (score > highestAttrScore)){
                        highestAttrScore = score;
                        AttrTemp = currentFact;
                    }
                }
            
                if (level.equalsIgnoreCase("ATTRIBUTE")){
                    
                    String subobjective = AttrTemp.getSlotValue("id").stringValue(r.getGlobalContext());
                    ValueVector attrList = AttrTemp.getSlotValue("attributes").listValue(r.getGlobalContext());
                    ValueVector attrScoreList = AttrTemp.getSlotValue("attrib-scores").listValue(r.getGlobalContext());
                    ValueVector attrReasonList = AttrTemp.getSlotValue("reasons").listValue(r.getGlobalContext());
                    int attrSize = attrList.size();
                    String [] attrNames = new String[attrSize];
                    double [] attrScores = new double[attrSize];
                    String [] attrReasons = new String[attrSize];
                    String satisfiedBy = AttrTemp.getSlotValue("satisfied-by").stringValue(r.getGlobalContext());
                    String targetID = stkhldrID + objID + "-" + subobjID;
                    ArrayList<Integer> lowScoreAttr = new ArrayList<>();
                    explanation = explanation + "Following attributes of "+ subobjective+" are not satisfied \n";
                    
                    
                    
                    
                    for (int j = 0;j<attrList.size();j++) {
                        attrNames[j] = attrList.get(j).stringValue(r.getGlobalContext());
                        attrScores[j]= attrScoreList.get(j).floatValue(r.getGlobalContext());
                        attrReasons[j] = attrReasonList.get(j).stringValue(r.getGlobalContext());
                        int attrID = j+1;
                        if (attrScores[j] < 1){
                            
                            String varName;
                            if (attrID < attrList.size()-1){
                                 varName = "?x" + (attrID);
                            } else if (attrID == attrList.size()-1){
                                varName = "?dc";
                            } else {
                                varName = "?pc";
                            } 
                            
                            if((varName.equalsIgnoreCase("?dc")) || (varName.equalsIgnoreCase("?pc"))){
                                explanation = explanation.concat(j+1 + ". " + attrNames[j] + " attribute scores:" + attrScores[j] + "\n");
                                /*
                                String targetSlot = "";
                                if (varName.equalsIgnoreCase("?dc")) {
                                    targetSlot = "data-rate-duty-cycle#";
                                } else if (varName.equalsIgnoreCase("?pc")){
                                    targetSlot = "power-duty-cycle#";
                                }
                                ActionAnalyzer aa = new ActionAnalyzer();
                                String tmpHistory = AttrTemp.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                                tmpHistory = tmpHistory.replace('{', '(');
                                tmpHistory = tmpHistory.replace('}',')');
                                String innermost = aa.getInsideParen(tmpHistory, aa.getNestedParenLevel(tmpHistory));
                                String targetFactID = innermost.split(" ")[1].substring(1);
                                Fact targetFact = r.findFactByID(Integer.parseInt(targetFactID));
                                factHistoryAnalyzer fha = new factHistoryAnalyzer(targetFact,targetSlot,r,qb);
                                String relRuleName = fha.findRelevantRule(fha.getFactHistory(),fha.getTargetSlot());
                                Defrule targetRule = (Defrule) Params.rules_defrule_map.get(relRuleName);
                                System.out.println(relRuleName);
                                System.out.println(fha.traceInheritance(relRuleName, targetSlot));
                                */
                            }else if (attrNames[j].equalsIgnoreCase("Temporal-resolution#")){
                                Defrule targetRule = new Defrule("","",r);
                                if ((Params.req_mode.equalsIgnoreCase("FUZZY-CASES")) || (Params.req_mode.equalsIgnoreCase("FUZZY-ATTRIBUTES"))) {
                                    targetRule = (Defrule) Params.rules_defrule_map.get("FUZZY-REQUIREMENTS::" + targetID + "-attrib");
                                }
                                else if ((Params.req_mode.equalsIgnoreCase("CRISP-CASES")) || (Params.req_mode.equalsIgnoreCase("CRISP-ATTRIBUTES"))) {
                                    targetRule = (Defrule) Params.rules_defrule_map.get("REQUIREMENTS::" + targetID + "-attrib");
                                }
                                ruleAnalyzer ra = new ruleAnalyzer(targetRule,r,qb);
                                ActionAnalyzer aa = ra.getActionAnalyzer();
                                ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
                                String action = ra.getActionContainingDesiredExpression("bind " + varName);
                                String thresholds = aa.getInsideParen(action, 4).split(" ",2)[1];
                                String refScores = aa.getInsideParen(aa.getInsideParen(action, 2),2,1).split(" ",2)[1];
                            // Not implemented yet
                                explanation = explanation.concat(j+1 + ". " + attrNames[j] + " attribute scores:" + attrScores[j] + " \n"
                                + " Threshold: ["+ thresholds + "] Reference Scores: ["+ refScores + "] \n");
                            }else {
                            
                                Defrule targetRule = new Defrule("","",r);
                                if ((Params.req_mode.equalsIgnoreCase("FUZZY-CASES")) || (Params.req_mode.equalsIgnoreCase("FUZZY-ATTRIBUTES"))) {
                                    targetRule = (Defrule) Params.rules_defrule_map.get("FUZZY-REQUIREMENTS::" + targetID + "-attrib");
                                }
                                else if ((Params.req_mode.equalsIgnoreCase("CRISP-CASES")) || (Params.req_mode.equalsIgnoreCase("CRISP-ATTRIBUTES"))) {
                                    targetRule = (Defrule) Params.rules_defrule_map.get("REQUIREMENTS::" + targetID + "-attrib");
                                }
                                ruleAnalyzer ra = new ruleAnalyzer(targetRule,r,qb);
                                ActionAnalyzer aa = ra.getActionAnalyzer();
                                ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
                                String action = ra.getActionContainingDesiredExpression("bind " + varName);
                                String thresholds = aa.getInsideParen(action, 4).split(" ",2)[1];
                                String refScores = aa.getInsideParen(aa.getInsideParen(action, 2),2,1).split(" ",2)[1];
                                String testVar = aa.getInsideParen(action, 3).split(" ", 3)[1];
                                String testSlot = cea.getVariableSlotPair(cea.getPattern(0)).get(testVar);
                                String parameter = cea.getSlotVariablePair(cea.getPattern(0)).get("Parameter");
                                Fact lhsFact = qb.makeQuery("REQUIREMENTS::Measurement (Parameter "+ Params.subobj_measurement_params.get(targetID) +")(taken-by "+ satisfiedBy +")").get(0);
                                String actualVal = lhsFact.getSlotValue(testSlot).stringValue(r.getGlobalContext());
                                
                                // not accounting for synergy effects...     
                                lowScoreAttr.add(j);
                                explanation = explanation.concat(j+1 + ". " + attrNames[j] + " attribute scores:" + attrScores[j] + " \n"
                                    + " Threshold: ["+ thresholds + "] Reference Scores: ["+ refScores + "] Actual Value: ["+actualVal+"]\n");
                                
                            }
                        }
                    }        
                    if (lowScoreAttr.isEmpty()){
                        explanation = explanation.concat("no low-score attributes \n");
                    } else{
                        explanation = explanation + "Instrument: " + satisfiedBy + "\n";
                    }
                }
            }
                if(!(explanation.length() == 0)){
                    explanation = explanation.substring(0, explanation.length()-1);
                    System.out.println(explanation);
                }
        }catch (Exception e) {
            System.out.println( "EXC in WHY explanation " +e.getMessage() );
        }
    }  
    
    public String getCurrentContext(){
        return CurrentContext;
    }
    public int getContextLength(){
        return PreviousContexts.size();
    }
    public String getPreviousContext(){
        String tmp = "Initialization";
        if (getContextLength()>0){
            return PreviousContexts.get(getContextLength()-1);
        } else
            return tmp;
    }
    public ArrayList<String> getDialogueHistory(){
        return PreviousContexts;
    }
    public void printDialogueHistory(){
        System.out.println(PreviousContexts);
    }
   
}
