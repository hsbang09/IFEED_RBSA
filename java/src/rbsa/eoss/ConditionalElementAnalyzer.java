/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Bang
 */
import jess.Defrule;
import jess.Rete;
import jess.Test1;


public class ConditionalElementAnalyzer {
    private Rete r;
    private QueryBuilder qb;
    private Defrule targetRule;
    private jess.Group group;
    private int numOfPatterns;
    private ArrayList <String> patternBoundNames = new ArrayList<>();
    private ArrayList <jess.Pattern> patterns = new ArrayList<>();
    
    public ConditionalElementAnalyzer(){
    }
    public ConditionalElementAnalyzer(Defrule Rule,Rete r,QueryBuilder qb){
        targetRule = Rule;
        this.r = r;
        this.qb = qb;
        this.group =  (jess.Group) targetRule.getConditionalElements();
        int groupsize = group.getGroupSize();
        int cnt =0;
        for (int i = 0;i<groupsize;i++){
            if (!group.getConditionalElement(i).isGroup()){
                cnt++;
                patterns.add( (jess.Pattern) group.getConditionalElement(i));
                patternBoundNames.add(((jess.Pattern) group.getConditionalElement(i)).getBoundName());
            } 
        }
        numOfPatterns = cnt;
    }
    
    public jess.Pattern getPattern(int index){
        return patterns.get(index);
    }
    public int getNPattern(){
        return numOfPatterns;
    }
    public ArrayList<String> getPatternBoundNames(){
        return patternBoundNames;
    }
    
   public ArrayList<String> getTestSummary(jess.Pattern pat){
       Iterator testIter = pat.getTests();
       ArrayList<String> testSummary = new ArrayList<>();
       while(testIter.hasNext()){
           Test1 currentTest = (Test1) testIter.next();
           String tmpString = analyzeTest(currentTest);
           String[] tmpStringSplit = tmpString.split(" ;;;; ");
           if (!tmpStringSplit[2].equalsIgnoreCase("nil")){
           testSummary.add(tmpString);
           }
       }
       return testSummary;
   }
    public HashMap<String,String[]> getVariabletoPatternSlotMap(){
        HashMap<String,String[]> varSlotMap = new HashMap<>();
        for (int i =0;i<getNPattern();i++){
            jess.Pattern pat = getPattern(i);
            String patName = pat.getName();
            ArrayList<String> testSummary = getTestSummary(pat);
            
            for (String test1:testSummary){
                String[] testSplit = test1.split(" ;;;; ");
                String slotName = testSplit[0];
                String sign = testSplit[1];
                String var = testSplit[2];
                if (sign.equals("EQ")){
                    String[] tmp = new String[2];
                    tmp[0] = patName;
                    tmp[1] = slotName;
                    varSlotMap.put(var, tmp);
                }
            }
        }
        return varSlotMap;
    }
   public boolean checkSlotExistenceInPattern(jess.Pattern pat,String slotName){
       boolean usage = false;
       Iterator testIter = pat.getTests();
       while(testIter.hasNext()){
           Test1 currentTest = (Test1) testIter.next();
           if(currentTest.getSlotName().equalsIgnoreCase(slotName)){
               usage = true;
           }
       }
       return usage;
   }
    
   public HashMap<String,String> getVariableSlotPair(jess.Pattern pat){
       HashMap<String,String> varSlotMap = new HashMap<>();
       ArrayList<String> testSummary = getTestSummary(pat);
       for (String test1:testSummary){
           String[] testSplit = test1.split(" ;;;; ");
           String slotName = testSplit[0];
           String sign = testSplit[1];
           String var = testSplit[2];
           if (sign.equals("EQ")){
               varSlotMap.put(var, slotName);
           }
       }
       return varSlotMap;
   }
      public HashMap<String,String> getSlotVariablePair(jess.Pattern pat){
       HashMap<String,String> slotVarMap = new HashMap<>();
       ArrayList<String> testSummary = getTestSummary(pat);
       for (String test1:testSummary){
           String[] testSplit = test1.split(" ;;;; ");
           String slotName = testSplit[0];
           String sign = testSplit[1];
           String var = testSplit[2];
           if ((sign.equals("EQ")) && ((var.startsWith("?")) || (var.startsWith("$")))){
               slotVarMap.put(slotName, var);
           }
       }
       return slotVarMap;
   }
    public ArrayList<String> getAllSlotNames(jess.Pattern pat){
        ArrayList<String> slots = new ArrayList<>();

        ArrayList<String> testSummary = getTestSummary(pat);
        for (String test1:testSummary){
            String[] testSplit = test1.split(" ;;;; ");
            String slotName = testSplit[0];
            if(!slots.contains(slotName)){
                slots.add(slotName);
            }
        }
        return slots;
   }
   
   public ArrayList<String> getAllTestVariables(){
       ArrayList<String> variables = new ArrayList<>();
       for (int i =0;i<getNPattern();i++){
            jess.Pattern pat = getPattern(i);
            ArrayList<String> testSummary = getTestSummary(pat);
            for (String test1:testSummary){
                String[] testSplit = test1.split(" ;;;; ");
                String slotName = testSplit[0];
                String sign = testSplit[1];
                String var = testSplit[2];
                if (sign.equals("EQ")){
                    variables.add(var);
                }
            }
        }
        return variables;
   }
   
    public String analyzeTest(Test1 test){
        String testAnal = "";
        testAnal = testAnal.concat(test.getSlotName());
        
        String testString = test.toString(); //[Test1: test=EQ;sub_idx=-1;slot_value=?val2;conjunction=0]
        testString = testString.substring(8, testString.length()-1);
        String[] testSplit = testString.split(";");
        String[] tmp = testSplit[0].split("=");
        String matching = tmp[1];
        
        testAnal = testAnal.concat(" ;;;; " + matching);
        testAnal = testAnal.concat(" ;;;; " + test.getValue());
        return testAnal;
    }

    public int getNTests(jess.Pattern pat){
        return pat.getNTests();
    }

    public ArrayList<jess.Test1> getTest(jess.Pattern pat){
        Iterator testIter = pat.getTests();
        ArrayList<jess.Test1> testArray = new ArrayList<>();
        while(testIter.hasNext()){
            testArray.add((Test1) testIter.next());
        }
        return testArray;
    }
    public Test1 getTest(jess.Pattern pat, int index){
        return getTest(pat).get(index);
    }
    
    
    

}
