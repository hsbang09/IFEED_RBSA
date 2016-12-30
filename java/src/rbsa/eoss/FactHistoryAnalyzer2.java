/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import jess.Rete;

/**
 *
 * @author bang
 */
public class FactHistoryAnalyzer2 extends JessExpressionAnalyzer{
    
    private String factHistory;
    private Rete r;
    private QueryBuilder qb;
    
    public FactHistoryAnalyzer2(String factHistory, Rete r, QueryBuilder qb){
        this.r = r;
        this.qb=qb;
        this.factHistory = factHistory.replace('{', '(').replace('}',')');
        if(!this.factHistory.startsWith("(")){
            this.factHistory = "(" + this.factHistory;
        }
        if(!this.factHistory.endsWith(")")){
            this.factHistory = this.factHistory + ")";
        }
    }
    

    public boolean findRule(String ruleID){
        return this.factHistory.contains("R" + ruleID);
    }
    
    public String getToSpecificRule(String ruleID){
        
        if(!findRule(ruleID)){
            System.out.println("Rule was not found from the factHistory");
            return null;
        }
        String his = factHistory;
        String ruleTmp = "";
        while (true){
            his = getInsideParen(his, 1);
            ruleTmp = his.split(" ",2)[0];
            if(ruleTmp.equalsIgnoreCase(ruleID)){
                break;
            }
        }
        return his;
    }
    
    
    
}
