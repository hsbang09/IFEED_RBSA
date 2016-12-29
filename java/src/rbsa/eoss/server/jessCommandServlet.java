/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;

import jess.Defrule;
import jess.Deftemplate;
import jess.Fact;
import jess.Rete;
import jess.Value;
import rbsa.eoss.ActionAnalyzer;
import rbsa.eoss.ConditionalElementAnalyzer;
import rbsa.eoss.QueryBuilder;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
import rbsa.eoss.factHistoryAnalyzer;
import rbsa.eoss.ruleAnalyzer;
//import madkitdemo3.AgentEvaluationCounter;
import rbsa.eoss.local.Params;


/**
 *
 * @author Bang
 */

@WebServlet(name = "jessCommandServlet", urlPatterns = {"/jessCommandServlet"})
public class jessCommandServlet extends HttpServlet {

    
    private ServletContext context;
    private Result resu;
    private ArchWebInterface ai;
    private Gson gson = new Gson();
    private PrintWriter pw;
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet jessCommandServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet jessCommandServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

 
    
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                
        String outputString = "";

        
        if (request.getParameter("ID").equalsIgnoreCase("getFactName")){
            String factName = "";
            try{
                String factID_string = request.getParameter("factID");
                int factID = Integer.parseInt(factID_string);
                Fact RequestedFact = resu.getRete().findFactByID(factID);
                factName = RequestedFact.getName();
            } catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            outputString = factName;
        }
        
        
        if (request.getParameter("ID").equalsIgnoreCase("getRuleName")){
            String ruleName = "";
            try{
                String ruleID_string = request.getParameter("ruleID");
                int ruleID = Integer.parseInt(ruleID_string);
                ruleName = Params.rules_IDtoName_map.get(ruleID);
            } catch(Exception e){
                System.out.println(e.getMessage()); 
                e.printStackTrace();
            }
            outputString = ruleName;
        }

        
        if (request.getParameter("ID").equalsIgnoreCase("requestppdefrule")){
            String ppdefrule = "";
            try{
                String ruleID_string = request.getParameter("ruleID");
                int ruleID = Integer.parseInt(ruleID_string);
                String ruleName = Params.rules_IDtoName_map.get(ruleID);
                
                jess.Value ppdefruleVal = resu.getRete().eval("(ppdefrule "+ruleName+")");
                ppdefrule = ppdefruleVal.stringValue(resu.getRete().getGlobalContext());
                
            } catch(Exception e){
                e.printStackTrace();
            }
            outputString = ppdefrule;
        }
        
                if (request.getParameter("ID").equalsIgnoreCase("requestppdefrule")){
            String ppdefrule = "";
            try{
                String ruleID_string = request.getParameter("ruleID");
                int ruleID = Integer.parseInt(ruleID_string);
                String ruleName = Params.rules_IDtoName_map.get(ruleID);
                
                jess.Value ppdefruleVal = resu.getRete().eval("(ppdefrule "+ruleName+")");
                ppdefrule = ppdefruleVal.stringValue(resu.getRete().getGlobalContext());
                
            } catch(Exception e){
                e.printStackTrace();
            }
            outputString = ppdefrule;
        }
        
                
        if (request.getParameter("ID").equalsIgnoreCase("getSlotNames")){
            String jsonObj = "";
            try{

                int factID = Integer.parseInt(request.getParameter("factID"));
                Fact RequestedFact = resu.getRete().findFactByID(factID);
                Deftemplate template = RequestedFact.getDeftemplate();
                int nSlots = template.getNSlots();
                ArrayList<String> slots = new ArrayList<>();
                for (int i=0;i<nSlots;i++){
                    slots.add(template.getSlotName(i));
                }
                jsonObj = gson.toJson(slots);
                
            } catch(Exception e){
                e.printStackTrace();
            }
            outputString = jsonObj;
      
        }      
        if (request.getParameter("ID").equalsIgnoreCase("getSlotValue")){
            String slotValString = "";
            try{

                int factID = Integer.parseInt(request.getParameter("factID"));
                String slotName = request.getParameter("slotName");
                Fact RequestedFact = resu.getRete().findFactByID(factID);
                
                Value slotVal = RequestedFact.getSlotValue(slotName);
                slotValString = slotVal.toString();
                
            } catch(Exception e){
                e.printStackTrace();
            }
            outputString = slotValString;
      
        }     
        if (request.getParameter("ID").equalsIgnoreCase("traceRelevantLHSFact")){
            String outputRuleID = "";
            try{
                    
                int factID = Integer.parseInt(request.getParameter("factID"));
                String slotName = request.getParameter("slotName");
                int ruleID = Integer.parseInt(request.getParameter("ruleID"));
                String ruleName = Params.rules_IDtoName_map.get(ruleID);
                Defrule rule = Params.rules_defrule_map.get(ruleName);
                
                Fact RequestedFact = resu.getRete().findFactByID(factID);
                String factHistory = RequestedFact.getSlotValue("factHistory").stringValue(resu.getRete().getGlobalContext());
                factHistoryAnalyzer fha = new factHistoryAnalyzer(RequestedFact, slotName, resu.getRete(), resu.getQueryBuilder());
                ruleAnalyzer ra = new ruleAnalyzer(rule,resu.getRete(),resu.getQueryBuilder());
                
                
                ArrayList<String> LHSFactIDs = fha.getLHSFacts(ruleID);
                ArrayList<String> LHSFactNames = ra.getRelevantLHSFacts(slotName);
                ArrayList<String> matchedFacts = new ArrayList<>();
                
                for (String ID:LHSFactIDs){
                    for (String name:LHSFactNames){
                        ID = ID.substring(1);
                        Fact thisFact = resu.getRete().findFactByID(Integer.parseInt(ID));
                        if(thisFact.getName().equalsIgnoreCase(name)){
                            matchedFacts.add(name);
                        }
                    }
                }
                
                
              
                outputRuleID = "" + ruleID;

            } catch(Exception e){
                e.printStackTrace();
            }
            outputString = outputRuleID;
        }  
        
        
        if (request.getParameter("ID").equalsIgnoreCase("getRelevantRule")){
            String outputRuleID = "";
            try{
                    
                int factID = Integer.parseInt(request.getParameter("factID"));
                String slotName = request.getParameter("slotName");
                
                Fact RequestedFact = resu.getRete().findFactByID(factID);
                String factHistory = RequestedFact.getSlotValue("factHistory").stringValue(resu.getRete().getGlobalContext());

                factHistoryAnalyzer fha = new factHistoryAnalyzer(RequestedFact, slotName, resu.getRete(), resu.getQueryBuilder());
                String relRuleName = fha.findRelevantRule(slotName);
                int ruleID = Params.rules_NametoID_Map.get(relRuleName);
              
                outputRuleID = "" + ruleID;
                System.out.println(relRuleName);
            } catch(Exception e){
                e.printStackTrace();
            }
            outputString = outputRuleID;
        }  
                
        if (request.getParameter("ID").equalsIgnoreCase("getRuleObjJson")){
            String jsonObj = "";
            String ppdefrule_LHS = "";
            String ppdefrule_RHS = "";
            ArrayList<String> outputStrings = new ArrayList<>();
            try{
                
                int ruleID = Integer.parseInt(request.getParameter("ruleID"));  
                Rete r = resu.getRete();
                QueryBuilder qb = resu.getQueryBuilder();
                
                String ruleName = Params.rules_IDtoName_map.get(ruleID);
                Defrule thisRule = Params.rules_defrule_map.get(ruleName);
                String ruleDoc = thisRule.getDocstring();
                
                ruleAnalyzer ra = new ruleAnalyzer(thisRule,r,qb);
                ActionAnalyzer aa = ra.getActionAnalyzer();
                ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
        
                int numOfPat = ra.getNumberOfPatterns();
                
                outputStrings.add("RuleName: " + ruleName);
                
                for (int i=0;i<numOfPat;i++){
                    
                    String patternName = cea.getPattern(i).getName();
                    String patternBoundVar = cea.getPattern(i).getBoundName();
                    
                    outputStrings.add("FactName: " + patternName);
                    String slot_temp = "     Slots: ";                    
//                    ppdefrule_LHS = ppdefrule_LHS + "FactName: " + patternName + "\n"
////                            + "Bound Variable: " + patternBoundVar + "\n"
//                            + "     Slots: ";
                      
                    ArrayList<String> slotNames = cea.getAllSlotNames(cea.getPattern(i));
                    int numOfTests = cea.getNTests(cea.getPattern(i));
                    for (String slotName:slotNames){
                        if (slotName.equalsIgnoreCase("factHistory")) continue;
                       
                        ArrayList<String> testSummary = cea.getTestSummary(cea.getPattern(i));
                        for (String test1:testSummary){
                            String[] testSplit = test1.split(" ;;;; ");
                            if(slotName.equalsIgnoreCase(testSplit[0])){
                                slot_temp = slot_temp + "(" + slotName + " " + testSplit[2] +") ";
//                                ppdefrule_LHS = ppdefrule_LHS + "(" + slotName + " " + testSplit[2] +") ";
                            }
                        }
                    } 
//                    ppdefrule_LHS = ppdefrule_LHS + "\n";
                    outputStrings.add(slot_temp);
                }
        
                outputStrings.add("=>");
                ArrayList<String> finalActions = ra.getFinalActions();
                ArrayList<String> intermediateActions = ra.getIntermediateActions();        
                 
                for (String action:intermediateActions){
                    outputStrings.add(action);
//                    ppdefrule_RHS = ppdefrule_RHS + action + "\n";
                }
                 
//                ppdefrule_RHS = ppdefrule_RHS + "Actions on Facts"  + "\n";
                outputStrings.add("Actions on Facts");
                
                for (int i = 0;i<finalActions.size();i++){
                  
                    String finalActionType = ra.getFinalActionTypes().get(i);
                    String variable = "";
                    String name = "";
                    String action = aa.getTargetFact().get(i);
                    String[] actionSplit = action.split(" ", 2);
                   
                    if (finalActionType.equalsIgnoreCase("modify")){
                        variable = actionSplit[0];  
                   
                        name = ra.getRHSFactVariableToLHSFactName().get(variable);
                    } else if(finalActionType.equalsIgnoreCase("duplicate")){
                        variable = actionSplit[0];
                   
                        name = ra.getRHSFactVariableToLHSFactName().get(variable);
                    } else {
                        name = actionSplit[0];
                    }
                    outputStrings.add("Action Type: " + finalActionType);
                    outputStrings.add("     Target Fact: " + name);
                    String slot_temp ="     Slots: ";
//                    ppdefrule_RHS = ppdefrule_RHS + "Action Type: " + finalActionType + "\n"
//                            + "     Target Fact: " + name + "\n"
//                            + "     Slots: ";
                  
                    ArrayList<String> slotNames = aa.getSlotNamesOfFinalAction(i);
                    HashMap<String,String> slotContents = aa.getSlotContentsOfFinalAction(i);
                    
                    for (String slotName:slotNames){
                        
                        if (slotName.equalsIgnoreCase("factHistory")) {continue;}
                        String slotContent = slotContents.get(slotName);
//                        ppdefrule_RHS = ppdefrule_RHS + "(" + slotName + " " + slotContent + ") ";
                        slot_temp = slot_temp + "("+ slotName + " " + slotContent + ") ";
                    }
                    outputStrings.add(slot_temp);
//                    ppdefrule_RHS = ppdefrule_RHS + "\n";
                }
        
//                int ruleID = Integer.parseInt(request.getParameter("ruleID"));
//                ruleWebInterface rwi = new ruleWebInterface(ruleID,resu.getRete(),resu.getQueryBuilder());
//                
//                ruleComponent ruleObj = rwi.getRuleObj();
//                jsonObj = gson.toJson(ruleObj);
                
            } catch(Exception e){
                e.printStackTrace();
            }
            
//            outputString = jsonObj;
            outputString = gson.toJson(outputStrings);
//            outputString = ppdefrule_LHS + "=> \n" + ppdefrule_RHS;
        }
        
        if (request.getParameter("ID").equalsIgnoreCase("getFactIDFromSubobj")){
            int archNum = Integer.parseInt(request.getParameter("archNum"));
            
            QueryBuilder qb = resu.getQueryBuilder();
            Rete r = resu.getRete();

            
            String subobjName = request.getParameter("subobj");

            Fact currentFact = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id "+ subobjName +")").get(0);
            Double current_subobj_score = 0.0;
            try{

                ArrayList<Fact> subobj_facts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id "+ subobjName +")");
                for (int n = 0;n<subobj_facts.size();n++) {
                   Fact f = subobj_facts.get(n);
                   Double subobj_score = f.getSlotValue("satisfaction").floatValue(r.getGlobalContext());
                   
                   if(subobj_score > current_subobj_score){
                       current_subobj_score = subobj_score;
                       currentFact = f;
                   }
//                        explanations.put(subobj, qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id " + subobj + ")"));   
                }
                
            } catch (Exception e){
                e.printStackTrace();
            }
            int factID = currentFact.getFactId();
            outputString = "" + factID;
        }
        
        
        if (request.getParameter("ID").equalsIgnoreCase("factHistoryFigureRequest")){
            
            ai = ArchWebInterface.getInstance();
            this.resu = ai.getResult();
            Rete r = resu.getRete();
            QueryBuilder qb = resu.getQueryBuilder();
            
            String subobj = request.getParameter("subobj");
            String factHistory = "";
            String factName = "";
            String jsonObj = "";
            try{
                
                ArrayList<Fact> facts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id "+ subobj +")");
                double max_sat = -1;
                Fact f = facts.get(0);
                for(int i=0;i<facts.size();i++){
                    double temp = facts.get(i).getSlotValue("satisfaction").floatValue(r.getGlobalContext());
                    if(temp >= max_sat){
                        f = facts.get(i);
                    }
                }
                
                Fact RequestedFact = f;
                int factID = f.getFactId();
                factHistory = RequestedFact.getSlotValue("factHistory").stringValue(resu.getRete().getGlobalContext());
                factName = RequestedFact.getName();
                factAndRuleNode farn = new factAndRuleNode(factID,factHistory);
                jsonObj = gson.toJson(farn);
                
//                System.out.println(factName + " factHistory request: "+jsonObj);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
            outputString = jsonObj;
//            response.sendRedirect("/CritiquerWebApplication/executionTrace.html");
        }

        response.flushBuffer();
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(outputString);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    
    
    class factAndRuleNode{
        private String type;
        private int ID;
        private ArrayList<factAndRuleNode> children;
        
        public factAndRuleNode(int mainFactID, String factHistoryInput){ // initialization
            
            try{
            
            this.ID = mainFactID;
            this.type = "fact";
            children = new ArrayList<>();
            String factHistory = factHistoryInput.replace('{', '(');
            factHistory = factHistory.replace('}',')');
            ActionAnalyzer aa = new ActionAnalyzer();
            int level = aa.getNestedParenLevel(factHistory);
            
            for (int i=0; i < level; i++){
                String inside = aa.getInsideParen(factHistory,i+1);
                String[] insideSplit = inside.split(" ", 2);
                if (insideSplit[0].substring(1).equalsIgnoreCase("nil")){
                    System.out.println("nil factID found");
                    continue;
                }
                int ruleID = Integer.parseInt(insideSplit[0].substring(1));
                String rest = insideSplit[1];
                rest = aa.collapseAllParenIntoSymbol(rest);
                String[] restSplit = rest.split(" ");
                ArrayList<factAndRuleNode> childrenOfRule = new ArrayList<>();
                ArrayList<factAndRuleNode> emptyList = new ArrayList<>();
                for (String tmp:restSplit){
                    if((tmp.startsWith("A")) || (tmp.startsWith("F")) || (tmp.startsWith("S")) || (tmp.startsWith("D")) || (tmp.startsWith("J"))){
                        int factID = Integer.parseInt(tmp.substring(1));
                        childrenOfRule.add(new factAndRuleNode(factID,"fact",emptyList));
                    }
                }
                this.children.add(new factAndRuleNode(ruleID, "rule" ,childrenOfRule));
                
            }}
            catch(Exception e){e.printStackTrace();}
            
        }
        public factAndRuleNode(int ID, String type, ArrayList<factAndRuleNode> children){
            this.ID = ID;
            this.type = type;
            this.children = children;
        }
        
    }
    
   
    
}
