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
import jess.Defrule;
import jess.Fact;
import jess.Rete;
import rbsa.eoss.local.Params;

public class factHistoryAnalyzer {
    
    private Fact FactInQuestion;
    private String targetSlot;
    private ruleAnalyzer ra;
    private ActionAnalyzer aa = new ActionAnalyzer();
    private Rete r;
    private QueryBuilder qb;
    private String factHistory;
    private String relevantRule;
    
    public factHistoryAnalyzer(){
    }
    public factHistoryAnalyzer(Rete r, QueryBuilder qb){
        this.r =r;
        this.qb=qb;
    }
    public factHistoryAnalyzer(Fact inputFact, Rete r, QueryBuilder qb){
        this.r =r;
        this.qb=qb;
        this.FactInQuestion = inputFact;
        
        initializeFactHistory();
    }
    public factHistoryAnalyzer(Fact inputFact, String slotName, Rete r, QueryBuilder qb){
        this.r =r;
        this.qb=qb;
        this.FactInQuestion = inputFact;
        this.targetSlot = slotName;
        initializeFactHistory();
    }
    
    
    
    
    private void initializeFactHistory(){
        try{
            factHistory = FactInQuestion.getSlotValue("factHistory").stringValue(r.getGlobalContext());
        } catch (Exception e) {
            System.out.println("EXC in initializing fact history "+ e.getMessage());
        }
        factHistory = factHistory.replace('{', '(');
        factHistory = factHistory.replace('}',')');
    }
    
    public String initializeFactHistory(String inputHistory){
        String history = inputHistory;
        history = history.replace('{', '(');
        history = history.replace('}',')');
        return history;
    }
    
//{R290 {R268 {R289 {R407 {R404 {R403 {R406 {R405 {R286 {R266 {R267 {R287 F731}}}}}}}}}}}}
    
    public String findRuleInHistory(String ruleID, String factHistory){
        String currentHistory = initializeFactHistory(factHistory);
        ActionAnalyzer aa = new ActionAnalyzer();
        String ruleTmp = "";
        while (true){
            currentHistory = aa.getInsideParen(currentHistory, 1);
            ruleTmp = currentHistory.split(" ",2)[0];
            if(ruleTmp.equalsIgnoreCase(ruleID)){
                break;
            }
        }
        return currentHistory;
    }
    
    public String[] traceInheritance(String relRuleName, String slotName, String history){
        String fh = initializeFactHistory(history);
        String newSlotName = "";
        String newFactHis = "";
        String[] outputRuleAndHistory = new String[2];
        String newFactID = "";
        
        try {
            
            Defrule relRule = Params.rules_defrule_map.get(relRuleName);
            ruleAnalyzer ra = new ruleAnalyzer(relRule,r,qb);
            int ruleID = Params.rules_NametoID_Map.get(relRuleName);
            
            if (!ra.checkInheritance(slotName)){
                outputRuleAndHistory[0] = findRelevantRule(history,slotName);
                outputRuleAndHistory[1] = newFactID;
                return outputRuleAndHistory;
            }
            ActionAnalyzer aa = ra.getActionAnalyzer();
            ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
            ArrayList<Fact> refFacts = new ArrayList<>();
            
            int level = 0;
            while(true){ // finding where relRule is in factHistory
                fh = aa.getInsideParen(fh,1);
                if (fh.split(" ",2)[0].equalsIgnoreCase("R"+ruleID)){
                    break;
                }
            }
            fh = aa.collapseAllParenIntoSymbol(fh);
            String[] fhSplit = fh.split(" ");
            for (String fa:fhSplit){
                if  (fa.startsWith("S")){
                    String fid = fa.substring(1);
                    refFacts.add(r.findFactByID(Integer.parseInt(fid)));
                }
            }
            
           
            ArrayList<String> finalActions = aa.getFinalAction();
            int nof = finalActions.size();
            String slotVar = "";
            for(int i=0;i<nof;i++){
                slotVar = aa.getSlotContentsOfFinalAction(i).get(slotName); 
                if (!slotVar.equalsIgnoreCase("")) break;
            }
            
            Fact relFact = refFacts.get(0);
            String fa = ra.getRelevantFinalAction(slotName);
            String rhsBoundName = ra.getFactBoundNameFromFinalAction(fa);
            int npat = cea.getNPattern();
            jess.Pattern pat = cea.getPattern(0);
            for (int i = 0;i<npat;i++){
                pat = cea.getPattern(i);
                String lhsBoundName = "?" + pat.getBoundName();
                newSlotName = cea.getVariableSlotPair(pat).get(slotVar);
                if ((!newSlotName.equalsIgnoreCase(""))&&(!lhsBoundName.equalsIgnoreCase(rhsBoundName))) break;
            } // the slot value on the RHS is copied from different fact on the LHS, hence having different bound nanmes for facts
            
//            ArrayList<Fact> facts = ra.getRelevantLHSFacts(slotName);

            for (Fact currentFact:refFacts){
                String currentName = currentFact.getName();
                if (currentFact.getName().equalsIgnoreCase(pat.getName())){
                    relFact = currentFact;
                    break;
                }
            }
            newFactHis = relFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
            newFactHis = initializeFactHistory(newFactHis);
            newFactID = Integer.toString(relFact.getFactId());

        } catch (Exception e){
            System.out.println("EXC in tracing inheritance rule "+ e.getMessage());
        }
        outputRuleAndHistory[0] = findRelevantRule(newFactHis,newSlotName);
        outputRuleAndHistory[1] = newFactID;
        return outputRuleAndHistory;
    }
    
    
    public String[] traceRelevantRule_skipOne(String knownRuleName, String slotName, String history){
        String fh = initializeFactHistory(history);
        String newFactHis = "";
        String newFactID = "";
        try {
            
            Defrule relRule = Params.rules_defrule_map.get(knownRuleName);
            ruleAnalyzer ra = new ruleAnalyzer(relRule,r,qb);
            int ruleID = Params.rules_NametoID_Map.get(knownRuleName);
            
            ActionAnalyzer aa = ra.getActionAnalyzer();
            ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
            ArrayList<Fact> refFacts = new ArrayList<>();
            
            int npat = cea.getNPattern();
            ArrayList<jess.Pattern> relPats = new ArrayList<>();
            for (int i =0;i<npat;i++){
                if(cea.checkSlotExistenceInPattern(cea.getPattern(i), slotName)){
                    relPats.add(cea.getPattern(i));
                }
            }
            if (relPats.size() > 1){
                // make the user choose among multiple facts
            }
            jess.Pattern relPat = relPats.get(0);
            
            int level = 0;
            while(true){ // finding where relRule is in factHistory
                fh = aa.getInsideParen(fh,1);
                if (fh.split(" ",2)[0].equalsIgnoreCase("R"+ruleID)){
                    break;
                }
            }
            fh = aa.collapseAllParenIntoSymbol(fh);
            String[] fhSplit = fh.split(" ");
            for (String fa:fhSplit){
                String fid = fa.substring(1);
                refFacts.add(r.findFactByID(Integer.parseInt(fid)));
            }
            
            Fact relFact = refFacts.get(0);
            
            for (Fact currentFact:refFacts){
                String currentName = currentFact.getName();
                if (currentFact.getName().equalsIgnoreCase(relPat.getName())){
                    relFact = currentFact;
                    break;
                }
            }
            newFactHis = relFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
            newFactHis = initializeFactHistory(newFactHis);
            newFactID = Integer.toString(relFact.getFactId());
        } catch (Exception e){
            System.out.println("EXC in findRelevantRule_skipOne "+ e.getMessage());
        }
        String[] outputRuleAndHistory = new String[2]; 
        outputRuleAndHistory[0] = findRelevantRule(newFactHis,slotName);
        outputRuleAndHistory[1] = newFactID;
        return outputRuleAndHistory;
    }    

    public String findRelevantRule(String slotName){
        String factHistory1 = this.factHistory;
        if (aa.checkParen(factHistory1) == false){
            return "Invalid history input";
        }
        int level = aa.getNestedParenLevel(factHistory1);
        for (int i = 0;i<level;i++){
            factHistory1 = aa.getInsideParen(factHistory1, 1, 1);
            String[] historySplit = factHistory1.split(" ",2);
            int targetRuleNumber = Integer.parseInt(historySplit[0].substring(1));
            String restOfHistory = historySplit[1].trim();
            String targetRuleName = Params.rules_IDtoName_map.get(targetRuleNumber);
            Defrule targetRule = Params.rules_defrule_map.get(targetRuleName);
            ruleAnalyzer tempRA = new ruleAnalyzer(targetRule,r,qb);    
            if(tempRA.checkModificationOnSlot(slotName)){
                ra = tempRA;
                return targetRuleName;
            }
        }
        return "No rule found to be relevant";
    }
    public String findRelevantRule(String inputHistory, String slotName){
        String factHistory1 = initializeFactHistory(inputHistory);
        if (aa.checkParen(factHistory1) == false){
            return "Invalid history input";
        }
        int level = aa.getNestedParenLevel(factHistory1);
        for (int i = 0;i<level;i++){
            factHistory1 = aa.getInsideParen(factHistory1, 1, 1);
            String[] historySplit = factHistory1.split(" ",2);
            int targetRuleNumber = Integer.parseInt(historySplit[0].substring(1));
            String restOfHistory = historySplit[1].trim();
            String targetRuleName = Params.rules_IDtoName_map.get(targetRuleNumber);
            Defrule targetRule = Params.rules_defrule_map.get(targetRuleName);
            ruleAnalyzer tempRA = new ruleAnalyzer(targetRule,r,qb);    
            if(tempRA.checkModificationOnSlot(slotName)){
                ra = tempRA;
                return targetRuleName;
            }
        }
        try{
        String lastFact = factHistory1.split(" ")[1];
        if (lastFact.startsWith("A")){
            
        } else if (lastFact.startsWith("D")){
            Fact newTargetFact = r.findFactByID(Integer.parseInt(lastFact.substring(1)));
            String newFactHistory = newTargetFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
            newFactHistory = initializeFactHistory(newFactHistory);
            return findRelevantRule(newFactHistory,slotName);
            
        } else if (lastFact.startsWith("J")){
            return "Fact asserted in java";
        }
        
        if (factHistory1.split(" ").length > 2){
            String[] subf = factHistory1.split(" ");
            int leng = subf.length;
            for (int f = 2;f<leng;f++){
                String subName = subf[f];
                Fact newTargetFact = r.findFactByID(Integer.parseInt(subName.substring(1)));
                String newFactHistory = newTargetFact.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                newFactHistory = initializeFactHistory(newFactHistory);
                return findRelevantRule(newFactHistory,slotName);
            }
        }
        
        } catch (Exception e){
            System.out.println("EXC in getRelevant Rule "+ e.getMessage());
        }
        return "No rule found to be relevant";
    }
    
    
    //{R290 {R268 {R289 {R407 {R404 {R403 {R406 {R405 {R286 {R266 {R267 {R287 F731}}}}}}}}}}}}
    public ArrayList<String> getLHSFacts(int ruleID){
        
        ArrayList<String> LHSFactIDs = new ArrayList<>();
        
        String factHistory1 = factHistory;
        int level = aa.getNestedParenLevel(factHistory1);
        for (int i = 0;i<level;i++){
            factHistory1 = aa.getInsideParen(factHistory1, 1, 1);
            String[] historySplit = factHistory1.split(" ",2);
            int thisRuleID = Integer.parseInt(historySplit[0].substring(1));
            String restOfHistory = historySplit[1].trim();
            
            if (ruleID == thisRuleID){
                int[] paren = aa.locateParen(restOfHistory, 1);
                String[] LHSFacts_ = restOfHistory.substring(paren[1]+1).split(" ");
                for (String thisFact:LHSFacts_){
                    LHSFactIDs.add(thisFact);
                }
                break;
            }  
        }
        return LHSFactIDs;
    }
    
    
    
    public void setTargetSlot(String slotName){
        this.targetSlot = slotName;
    }
    
    public void setTargetFact(Fact fa){
        this.FactInQuestion = fa;
        initializeFactHistory();
    }

    public String getTargetSlot(){
        return targetSlot;
    }
    public ruleAnalyzer getRuleAnalyzer(){
        return ra;
    }
    public String getFactHistory(){
        return factHistory;
    }
    
           // {290 {268 {289 {407 {404 {403 {406 {405 {286 {266 {267 {287 731}}}}}}}}}}}}
}
