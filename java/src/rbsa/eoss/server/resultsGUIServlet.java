/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import jess.Defrule;
import jess.Fact;
import jess.Rete;
import jess.ValueVector;
import rbsa.eoss.ActionAnalyzer;
import rbsa.eoss.ConditionalElementAnalyzer;

import rbsa.eoss.QueryBuilder;
import rbsa.eoss.Result;
import rbsa.eoss.ResultCollection;
import rbsa.eoss.ResultManager;
import rbsa.eoss.local.Params;
import rbsa.eoss.ruleAnalyzer;


/**
 *
 * @author Bang
 */
@WebServlet(name = "resultsGUIServlet", urlPatterns = {"/resultsGUIServlet"})
public class resultsGUIServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1257649107469947355L;

    private Gson gson = new Gson();
    ResultManager RM = ResultManager.getInstance();
    private static resultsGUIServlet instance=null;
	ServletContext sctxt;
	ServletConfig sconfig;
    private int arch_id;
    private rbsa.eoss.Result resu;
    
    /**
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException{ 
    	instance = this;
    	
    	sctxt = this.getServletContext();
    	sconfig = this.getServletConfig();
    }
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
            out.println("<title>Servlet resultsGUIServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet resultsGUIServlet at " + request.getContextPath() + "</h1>");
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

//        processRequest(request, response);
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
    	

        String outputString="";
        String requestID = request.getParameter("ID");
        try {
            
        	
        	
        if (requestID.equalsIgnoreCase("getNorb")){
            int norb = Params.orbit_list.length;
            outputString = Integer.toString(norb);
        }
        else if (requestID.equalsIgnoreCase("getNinstr")){
            int ninstr = Params.instrument_list.length;
            outputString = Integer.toString(ninstr);
        }

        else if (requestID.equalsIgnoreCase("extractInfoFromBitString")){

            String bitString_string = request.getParameter("bitString");
            int cnt = bitString_string.length();
//            boolean[] bitString = new boolean[cnt];
            int norb = Params.orbit_list.length;
            int ninstr = Params.instrument_list.length;
            ArrayList<instrumentsInOrbit> architecture = new ArrayList<>();
            int b = 0;
            for (int i= 0;i<norb;i++) {
                String orbit = Params.orbit_list[i];
                instrumentsInOrbit thisOrbit = new instrumentsInOrbit(orbit);
                for (int j= 0;j<ninstr;j++) { 
                    if (bitString_string.substring(b,b+1).equalsIgnoreCase("1")){
                        String inst = Params.instrument_list[j];
                        thisOrbit.addToChildren(inst);
                    }
                    b++;
                }
                architecture.add(thisOrbit);
            }
            String jsonObj = gson.toJson(architecture);
            outputString = jsonObj;

        }
        
        
        
        
        else if (requestID.equalsIgnoreCase("resultFileURL_newData")){
            
            String resultPath = request.getParameter("filePath");
            InputStream file = sctxt.getResourceAsStream(resultPath);
            
            ResultCollection RC = RM.loadResultCollectionFromInputStream(file);
            Stack<Result> tmpResults = RC.getResults();
            Stack<Result> results = new Stack<Result>();
            for (Result tmpResult:tmpResults){
                if(tmpResult.getScience()>=0.001){
                    results.add(tmpResult);
                }
            }
            int nResults = results.size();

            ArrayList<Architecture> archArray = new ArrayList<>();
            this.arch_id=0;
            
            for (int i=0;i<nResults;i++){
                double sci = results.get(i).getScience();
                double cost = results.get(i).getCost();
                boolean[] bitString = results.get(i).getArch().getBitString();
                Architecture arch = new Architecture(this.arch_id,sci,cost,bitString);
                this.arch_id++;
                archArray.add(arch);
            }
            String jsonObj = gson.toJson(archArray);
            outputString = jsonObj;
        }
        
        else if (requestID.equalsIgnoreCase("resultFileURL_addData")){
            
        	String resultPath = request.getParameter("filePath");
        	InputStream file = sctxt.getResourceAsStream(resultPath);
        	ResultCollection RC = RM.loadResultCollectionFromInputStream(file);
        	
            Stack<Result> newResults = RC.getResults();
            int numNewResults = newResults.size();
            ArrayList<Architecture> archArray = new ArrayList<>();
            
            for (int i=0;i<numNewResults;i++){
                double sci = newResults.get(i).getScience();
                double cost = newResults.get(i).getCost();
                boolean[] bitString = newResults.get(i).getArch().getBitString();
                Architecture arch = new Architecture(this.arch_id,sci,cost,bitString);
                this.arch_id++;
                archArray.add(arch);
            }
            String jsonObj = gson.toJson(archArray);
            outputString = jsonObj;
        }
        
        

        
        else if (requestID.equalsIgnoreCase("getInstrumentList")){
            
            ArrayList<String> instrumentList = new ArrayList<>(); 
            String[] instruments = Params.instrument_list;
            for (String inst:instruments){
                instrumentList.add(inst);
            }
            String jsonObj = gson.toJson(instrumentList);
            outputString = jsonObj;

        }
        else if (requestID.equalsIgnoreCase("getOrbitList")){
            
            ArrayList<String> orbitList = new ArrayList<>(); 
            String[] orbits = Params.orbit_list;
            for (String orb:orbits){
                orbitList.add(orb);
            }
            String jsonObj = gson.toJson(orbitList);
            outputString = jsonObj;

        }
        
        else if(requestID.equalsIgnoreCase("generateEmptyFilterArch")){
            
            int norb = Params.orbit_list.length;
            int ninstr = Params.instrument_list.length;
            String filterArch = "";
            
            for (int i=0;i<(norb+2)*ninstr;i++){
                filterArch = filterArch + "0";
            }
            outputString = filterArch;
        }

        else if (requestID.equalsIgnoreCase("modifyBitString")){
            String bitString = request.getParameter("bitString");
            String instrument = request.getParameter("instrument");
            String orbit = request.getParameter("orbit");
            String modifiedBitString = bitString;
//            boolean[] bitString = new boolean[cnt];
            int norb = Params.orbit_list.length;
            int ninstr = Params.instrument_list.length;
            int nth_orb=0;
            int nth_inst=0;
            
            for (int i=0;i<norb;i++){
                if(Params.orbit_list[i].equalsIgnoreCase(orbit)){
                    nth_orb=i;
                    break;
                }
            }
            for (int i=0;i<ninstr;i++){
                if(Params.instrument_list[i].equalsIgnoreCase(instrument)){
                    nth_inst=i;
                    break;
                }
            }
            String bitString1 = bitString.substring(0, nth_orb*ninstr + nth_inst);
            String bitString2 = bitString.substring(nth_orb*ninstr + nth_inst+1);
            
            if (bitString.substring(nth_orb*ninstr + nth_inst,nth_orb*ninstr + nth_inst+1).equalsIgnoreCase("0")){
                modifiedBitString = bitString1 + "1" + bitString2;
            } else{
                modifiedBitString = bitString1 + "0" + bitString2;
            }
            outputString = modifiedBitString;
        }
        
        else if (requestID.equalsIgnoreCase("evalNewArch")){
            String bitString = request.getParameter("bitString");
            
//            ai = ArchWebInterface.getInstance();
//            ai.initialize();
//            Result resu = ai.evaluateArch(bitString, 1);
//            this.results.add(resu);
//            archEvalResults aer = new archEvalResults(resu.getScience(), resu.getCost(), resu.getArch().getBitString());
//            aer.setStatus("justAdded");
//            String jsonObj = gson.toJson(aer);
//            outputString = jsonObj;

        }
        
        
        
        
        else if (requestID.equalsIgnoreCase("satisfactionScoreSummaryRequest")){
            
            String bitString = request.getParameter("bitString");
            
            System.out.println(bitString);
            ArchWebInterface ai = ArchWebInterface.getInstance();
            ai.initialize();
                        
            this.resu = ai.evaluateArch(bitString,1);
                        
            ai.initialize_aggregation_structure();
            String bitString_string = request.getParameter("bitString");
            int cnt = bitString_string.length();
            
            ArrayList<String> subobjs = ai.getSubobjectives();
            ArrayList<String> objs = ai.getObjectives();
            ArrayList<String> panels = ai.getPanels();
            HashMap<String,String> objs_des = ai.getObjDescriptions();
            HashMap<String,String> subobjs_des = ai.getSubobjDescriptions();
            HashMap<String,Double> objs_weights = ai.getObjWeights();
            HashMap<String,Double> subobjs_weights = ai.getSubobjWeights();
            
            satisfactionScores value = new satisfactionScores("value");
            value.setScore(resu.getScience());
                        
            for (int i=0;i<panels.size();i++){ // iterate over stakeholders
                String panel = panels.get(i);
                satisfactionScores stakeholder = new satisfactionScores("stakeholder",panel);
                
                double panelScore = 0;
                int numOfObjs = 0;
                
                for (int j=0;j<objs.size();j++){  // iterate over objectives
                    String obj = objs.get(j);
                    if(!obj.contains(panel)) continue;
                    satisfactionScores objective = new satisfactionScores("objective",obj);
                    double objScore = 0;
                    double numOfSubobjs = 0;
                    
                    for(int k=0;k<subobjs.size();k++){ // iterate over subobjectives
                        String subobj = subobjs.get(k);
                        if(!subobj.split("-")[0].equalsIgnoreCase(obj)) continue;
                        double subobjScore = resu.getSubobjective_scores2().get(subobj);             
                        satisfactionScores subobjective = new satisfactionScores("subobjective",subobj,subobjScore);
                        subobjective.setDescription(subobjs_des.get(subobj));
                        subobjective.setWeight(subobjs_weights.get(subobj));
                        objective.addToChildren(subobjective);
                        objScore = objScore + subobjScore;
                        numOfSubobjs = numOfSubobjs+1;
                    }
                    objScore = objScore/numOfSubobjs;
                    objective.setScore(objScore);
                    objective.setDescription(objs_des.get(obj));
                    objective.setWeight(objs_weights.get(obj));
                    stakeholder.addToChildren(objective);
                    
                    panelScore = panelScore + objScore;
                    numOfObjs = numOfObjs+1;
                }
                panelScore = panelScore/numOfObjs;
                stakeholder.setScore(panelScore);
                value.addToChildren(stakeholder);
            }
            String jsonObj = gson.toJson(value);
            outputString = jsonObj;
            System.out.println("Satisfaction score summary sent back");
        }
       
        
        
        else if(requestID.equalsIgnoreCase("attributeScoreSummaryRequest")){ 
            
            String subobjID = request.getParameter("subobj");
            QueryBuilder qb = this.resu.getQueryBuilder();
            Rete r = this.resu.getRete();
            ArrayList<Fact> subobj_facts = qb.makeQuery("AGGREGATION::SUBOBJECTIVE (id " + subobjID + ")");
            
            Defrule targetRule = new Defrule("","",r);
            if ((Params.req_mode.equalsIgnoreCase("FUZZY-CASES")) || (Params.req_mode.equalsIgnoreCase("FUZZY-ATTRIBUTES"))) {  
                targetRule = (Defrule) Params.rules_defrule_map.get("FUZZY-REQUIREMENTS::" + subobjID + "-attrib");         
            }
            else if ((Params.req_mode.equalsIgnoreCase("CRISP-CASES")) || (Params.req_mode.equalsIgnoreCase("CRISP-ATTRIBUTES"))) {     
                targetRule = (Defrule) Params.rules_defrule_map.get("REQUIREMENTS::" + subobjID + "-attrib");
            }       
//            
            ruleAnalyzer ra = new ruleAnalyzer(targetRule,r,qb);            
            ActionAnalyzer aa = ra.getActionAnalyzer();
            ConditionalElementAnalyzer cea = ra.getConditionalElementAnalyzer();
            ArrayList subobjectiveFacts = new ArrayList();
//            
            int nil_fact = 0;
            for (int n = 0;n<subobj_facts.size();n++) {
                Fact f = subobj_facts.get(n);
                Double subobj_score = f.getSlotValue("satisfaction").floatValue(r.getGlobalContext());
                String satisfied_by = f.getSlotValue("satisfied-by").stringValue(r.getGlobalContext());
                if (satisfied_by.equalsIgnoreCase("nil")){
                    nil_fact++;
                    continue;
                }
                String factHistory = f.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                ValueVector attrList = f.getSlotValue("attributes").listValue(r.getGlobalContext());
                ValueVector attrScoreList = f.getSlotValue("attrib-scores").listValue(r.getGlobalContext());
                ValueVector attrReasonList = f.getSlotValue("reasons").listValue(r.getGlobalContext());
                int attrSize = attrList.size();
                String [] attrNames = new String[attrSize];
                double [] attrScores = new double[attrSize];
                String [] attrReasons = new String[attrSize];
                
                ArrayList<attributeScores> attributes = new ArrayList<>();
                int cnt = 0;
                String varName;
                for (int i=0;i<attrSize;i++){
                    attrNames[i] = attrList.get(i).stringValue(r.getGlobalContext());
                    attrScores[i] = attrScoreList.get(i).floatValue(r.getGlobalContext());
                    attrReasons[i] = attrReasonList.get(i).stringValue(r.getGlobalContext());
                    
                    int attrID = i+1;
                    if (attrID < attrList.size()-1){     
                        varName = "?x" + (attrID);  
                    } else if (attrID == attrList.size()-1){       
                        cnt++;
                        varName = "?dc";   
                        continue;
                    } else {                         
                        varName = "?pc";
                        cnt++;
                        continue;
                    } 
                    String action = ra.getActionContainingDesiredExpression("bind " + varName);
                    String thresholds = aa.getInsideParen(action, 4).split(" ",2)[1];
                    String refScores = aa.getInsideParen(aa.getInsideParen(action, 2),2,1).split(" ",2)[1];
                    String testVar = aa.getInsideParen(action, 3).split(" ", 3)[1];
                    String testSlot = cea.getVariableSlotPair(cea.getPattern(0)).get(testVar);
                    String parameter = cea.getSlotVariablePair(cea.getPattern(0)).get("Parameter");
                    ArrayList<Fact> capabilities = qb.makeQuery("REQUIREMENTS::Measurement (Parameter "+ Params.subobj_measurement_params.get(subobjID) +")");
                    Fact matchingCapability = capabilities.get(0);
                    for (Fact capa:capabilities){
                        int currentFactID = capa.getFactId();
                        if (factHistory.contains("A"+currentFactID)){
                            matchingCapability = capa;
                            break;
                        }
                    }
                    String actualVal = matchingCapability.getSlotValue(testSlot).stringValue(r.getGlobalContext());
                    attributeScores attr = new attributeScores(attrNames[i],attrScores[i],thresholds,refScores);
                    attr.setActualValue(actualVal);
                    attr.setInstrument(satisfied_by);
                    attributes.add(attr);
                    
                }
                
                if (cnt==attrSize){
                    System.out.println("No attribute other than dc & pc");
                }
                
                subobjectiveFacts.add(attributes);
            }
            
            if (subobj_facts.size()==nil_fact){
                System.out.println("Facts not found");
            }
            
            String jsonObj = gson.toJson(subobjectiveFacts);
            outputString = jsonObj;
            
        }
             


        
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        
        response.flushBuffer();
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(outputString);

        
//        processRequest(request, response);
    }
    
    
    public static resultsGUIServlet getInstance()
    {
        if( instance == null ) 
        {
            instance = new resultsGUIServlet();
        }
        return instance;
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
    
    public int[][] bitString2IntMat(String bitString){
        int norb = Params.orbit_list.length;
        int ninstr = Params.instrument_list.length;
        int[][] mat = new int[norb][ninstr];
        int cnt=0;
        for(int i=0;i<norb;i++){
            for(int j=0;j<ninstr;j++){
                if(bitString.substring(cnt, cnt+1).equalsIgnoreCase("1")){
                    mat[i][j]=1;
                }else{
                    mat[i][j]=0;
                }
                cnt++;
            }
        }
        return mat;
    }
    public int[][] boolArray2IntMat(boolean[] bool){
        int norb = Params.orbit_list.length;
        int ninstr = Params.instrument_list.length;
        int[][] mat = new int[norb][ninstr];
        int cnt=0;
        for(int i=0;i<norb;i++){
            for(int j=0;j<ninstr;j++){
                if(bool[cnt]==true){
                    mat[i][j]=1;
                }else{
                    mat[i][j]=0;
                }
                cnt++;
            }
        }
        return mat;
    }
    
    public boolean compareTwoBitStrings(String b1, boolean[] b2){
        for(int i=0;i<b2.length;i++){
            if(b1.substring(i,i+1).equalsIgnoreCase("0") && b2.equals(true)){
                return false;
            } else if (b1.substring(i,i+1).equalsIgnoreCase("1") && b2.equals(false)){
            	return false;
            }
        }
    	return true;
    }
    
    public boolean compareTwoBitStrings(boolean[] b1, boolean[] b2){
        for(int i=0;i<b1.length;i++){
            if(b1[i]!=b2[i]){
                return false;
            }
        }
        return true;
    }
    
    
    class instrumentsInOrbit{
        private String orbit;
        private ArrayList<String> children;
        private ArrayList<String> filterLogic;
        
        public instrumentsInOrbit(String orbit){
            this.orbit = orbit;
            children = new ArrayList<>();
            filterLogic = new ArrayList<>();
        }
       public void addToChildren(String instrument){
           children.add(instrument);
       }
       public void addToChildren(String instrument, String logic){
           children.add(instrument);
           filterLogic.add(logic);
       }
       
    }
    
    
    class Architecture{
        private int id;
        private boolean[] bitString;
        private double science;
        private double cost;
        private String status;
        
        public Architecture(){
        }
        
        public Architecture(int id, double science,double cost, boolean[] bitString){
            this.id = id;
            this.science = science;
            this.cost = cost;
            this.bitString = bitString;
        }
        public void setScience(double science){
            this.science = science;
        }
        public void setCost(double cost){
            this.cost = cost;
        }
    }
    
    
    
    
    
    
    
    
    
    class satisfactionScores{
        private String level;
        private String name;
        private double score;
        private String description;
        private double weight;
        private ArrayList<satisfactionScores> children = new ArrayList<>();
        
        public satisfactionScores(){    
        }
        public satisfactionScores(String level){    
            this.level = level;
        }
        public satisfactionScores(String level, String name){   
            this.level = level;
            this.name = name;
        }
        public satisfactionScores(String level, String name, double score){   
            this.level = level;
            this.name = name;
            this.score = score;
        }
        public void setDescription(String des){
            this.description = des;
        }
        public void setWeight(double weight){
            this.weight = weight;
        }
        
        public void addToChildren(satisfactionScores child){
            this.children.add(child);
        }
        public void setScore(double score){
            this.score  = score;
        }

    }
    
    
    
    class attributeScores{
        private String attrName;
        private double attrScore;
        private String thresholds;
        private String referenceScores;
        private String actualValue;
        private String instrument;
        private int archNum;
        
        
        public attributeScores(){
        }
        public attributeScores(String name, double score, String thres, String ref){
            this.attrName = name;
            this.attrScore = score;
            this.thresholds = thres;
            this.referenceScores = ref;
        }
        public void setName(String name){
            this.attrName = name;
        }
        public void setArchNum(int archNum){
            this.archNum = archNum;
        }
        public void setScore(double score){
            this.attrScore = score;
        }
        public void setThresholds(String thr){
            this.thresholds = thr;
        }
        public void setReferenceScores(String ref){
            this.referenceScores = ref;
        }
        public void setActualValue(String val){
            this.actualValue = val;
        }
        public void setInstrument(String inst){
            this.instrument = inst;
        }
        
    }
    
    
    
    
}


        



