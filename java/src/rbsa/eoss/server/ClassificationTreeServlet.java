/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import rbsa.eoss.DrivingFeaturesGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
import rbsa.eoss.Scheme;
import rbsa.eoss.local.Params;

/**
 *
 * @author Bang
 */
@WebServlet(name = "ClassificationTreeServlet", urlPatterns = {"/ClassificationTreeServlet"})
public class ClassificationTreeServlet extends HttpServlet {

    private Gson gson = new Gson();
    ResultManager RM = ResultManager.getInstance();
    Stack<Result> results;
    ArchWebInterface ai = ArchWebInterface.getInstance();
    Scheme scheme;
    boolean init = false;
    int norb;
    int ninstr;
    String[] instrument_list;
    String[] orbit_list;
    DrivingFeaturesGenerator dfsGen;
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
            out.println("<title>Servlet classificationTreeServlet</title>");            
            out.println("</head>");
            out.println("<body>");
//            out.println("<h1>Servlet classificationTreeServlet at " + request.getContextPath() + "</h1>");
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
//        processRequest(request, response);
    
        String outputString="";
        String requestID = request.getParameter("ID");
        try {
        	
        if (requestID.equalsIgnoreCase("buildClassificationTree")){
        	

//            double support_threshold = Double.parseDouble(request.getParameter("supp"));
//            double confidence_threshold = Double.parseDouble(request.getParameter("conf"));
//            double lift_threshold = Double.parseDouble(request.getParameter("lift")); 
//             
//            String selectedArchs_raw = request.getParameter("selected");
//          //["000000001101010111010011010010000110001010100011101000001011","101011000010000010010011011010100110100000101000100101001011","001001001000110001010001110111000000111111100010000000110010"]
//            selectedArchs_raw = selectedArchs_raw.substring(1, selectedArchs_raw.length()-1);
//          //"000010011101011000110000110000100000110110000100100010011010","010000100001010000000111100110000100110111001101001110010100"
//            String nonSelectedArchs_raw = request.getParameter("nonSelected");
//            nonSelectedArchs_raw = nonSelectedArchs_raw.substring(1, nonSelectedArchs_raw.length()-1);
//            
//            String[] selectedArchs_split = selectedArchs_raw.split(",");
//            String[] nonSelectedArchs_split = nonSelectedArchs_raw.split(",");
//            
//            ArrayList<String> selectedArchs = new ArrayList<>();
//            ArrayList<String> allArchs = new ArrayList<>();
//
//            for (String selectedArchs_split1:selectedArchs_split) {
//                String bitString = selectedArchs_split1.substring(1, selectedArchs_split1.length() - 1);
//                selectedArchs.add(bitString);
//                allArchs.add(bitString);
//                if(bitString.length()!=60){
//                    System.out.println("something's wrong: bitString length not 60");
//                }
//            }
//            for (String nonSelectedArchs_split1:nonSelectedArchs_split) {
//                String bitString = nonSelectedArchs_split1.substring(1, nonSelectedArchs_split1.length() - 1);
//                allArchs.add(bitString);
//                if(bitString.length()!=60){
//                    System.out.println("something's wrong: bitString length not 60");
//                }
//            }
//            ArrayList<int[][]> selected_pop = new ArrayList<>();
//            ArrayList<int[][]> pop = new ArrayList<>();
//
//            for(String allArchs1:allArchs){
//                pop.add(bitString2IntMat(allArchs1));
//            }
//            for(String selectedArchs1:selectedArchs){
//                selected_pop.add(bitString2IntMat(selectedArchs1));
//            }
//            
//            dfsGen = new DrivingFeaturesGenerator();
//            dfsGen.initialize(selected_pop, pop, support_threshold,confidence_threshold,lift_threshold);
//            
//            
////            "[{"name":"thisName","expression":"present(ACE_ORCA)&&present(DESD_LID)"},{"name":"secondOne","expression":"present(DESD_LID)||numOrbitUsed(3)"}]"
//            String userDefFilters_raw = request.getParameter("userDefFilters");
//            System.out.println(userDefFilters_raw);
//            if (userDefFilters_raw==null){
//            }else{
////                userDefFilters_raw = userDefFilters_raw.substring(2, userDefFilters_raw.length()-2);
////              {"name":"thisName","expression":"present(ACE_ORCA)&&present(DESD_LID)"},{"name":"secondOne","expression":"present(DESD_LID)||numOrbitUsed(3)"}
//
//              while(true){
//              	
//              	if(!userDefFilters_raw.contains("},") && !userDefFilters_raw.contains("}]")){
//              		if(!userDefFilters_raw.endsWith("}")){
//              			break;
//              		}
//              	}
//              	
//              	int paren1 = userDefFilters_raw.indexOf("{");
//                int paren2;
//                
//                if(userDefFilters_raw.indexOf("},")!=-1){
//                	paren2 = userDefFilters_raw.indexOf("},");
//                }else if (userDefFilters_raw.indexOf("}]")!=-1){
//                	paren2 = userDefFilters_raw.indexOf("}]");
//                } else {
//                	paren2 = userDefFilters_raw.length()-1;
//                }
//                  
//                String thisFilter = userDefFilters_raw.substring(paren1+1,paren2);
//              	String thisFilterName = thisFilter.split(",",2)[0]; //"name":"thisName"
//              	thisFilterName = thisFilterName.split(":")[1]; // "thisName"
//              	thisFilterName = thisFilterName.substring(1, thisFilterName.length()-1); //thisName
//              	String thisFilterExp = thisFilter.split(",",2)[1];  //"expression":"present(ACE_ORCA)&&present(DESD_LID)"
//              	thisFilterExp = thisFilterExp.split(":")[1]; // "thisName"
//              	thisFilterExp = thisFilterExp.substring(1, thisFilterExp.length()-1); //thisName
//              	
//              	dfsGen.addUserDefFilter(thisFilterName,thisFilterExp);
//              	
//              	if(userDefFilters_raw.substring(paren2).length()==1){
//              		break;
//              	}else{
//              		userDefFilters_raw = userDefFilters_raw.substring(paren2+1);
//              	}
//              }
//            	
//            }
//            String graph = dfsGen.buildTree(true);
////            TreeNode root = parse_decisionTree(graph);
////            outputString = gson.toJson(root);
//            outputString = graph;
////            System.out.println(graph);
        }
        
//        else if(requestID.equalsIgnoreCase("getTreeSummary")){
//            
//
//
//
////            output = treeSummary + "\n==========\n" +
////                    evalSummary + "\n==========\n" + 
////                    confusion + "\n==========\n" +
////                    graph;
//
////Number of leaves: 21
////Size of the tree: 41
////Results
////Correctly Classified Instances        2550               97.3654 %
////Incorrectly Classified Instances        69                2.6346 %
////Kappa statistic                          0.9385
////Mean absolute error                      0.0418
////Root mean squared error                  0.1603
////Relative absolute error                  9.6708 %
////Root relative squared error             34.4579 %
////Total Number of Instances             2619
////=== Confusion Matrix ===
////    a    b   <-- classified as
//// 1771   19 |    a = false
////   50  779 |    b = true
//
//        }
//        

        }catch(Exception e){
            e.printStackTrace();
        }
        
        response.flushBuffer();
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(outputString);
    
    
    }
    
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
    
    
    
    
    
    
    
    public TreeNode parse_decisionTree_weka(String graph){
        
//        String g = graph.substring(graph.indexOf("{")+2 , graph.indexOf("}")-1);
        String g = graph;
        ArrayList<String> existingNodes = new ArrayList<>();
//        ArrayList<ArrayList<String>> branches;
        TreeNode root = new TreeNode();
        while(g.length()!=0){
            if(!g.contains("\n")){
                break;
            }
            String line = g.substring(0, g.indexOf("\n"));
            String rest = g.substring(g.indexOf("\n")+1);
            if(!line.contains("[")){
                g = rest;
                continue;
            }
            String id;
            if(!line.contains("->")){ // node
                id = line.substring(line.indexOf("N")+1,line.indexOf("[")-1);
                if(id.equalsIgnoreCase("0")){
                    root = new TreeNode("0");
//                    String name = line.substring(line.indexOf("[")+1,line.indexOf("]",line.length()-1));
                    String tmp;
                    String name;
                    if(line.contains("'")){
                        tmp = line.substring(line.indexOf("\"")+2);
                        name = tmp.substring(0,tmp.indexOf("\"")-1);
                    } else{
                        tmp = line.substring(line.indexOf("\"")+1);
                        name = tmp.substring(0,tmp.indexOf("\""));
                    }
                    root.setName(name);
                    existingNodes.add(id);
                }else{
                    if(existingNodes.contains(id)){
                        TreeNode thisNode = root.findDescendent(root,id);
                        String tmp;
                        String name;
                        if(line.contains("'")){
                            tmp = line.substring(line.indexOf("\"")+2);
                            name = tmp.substring(0,tmp.indexOf("\"")-1);
                        } else{
                            tmp = line.substring(line.indexOf("\"")+1);
                            name = tmp.substring(0,tmp.indexOf("\""));
                        }
                        
                        if(name.contains("selected")){
//                            System.out.println(name);
                            String insideParen = name.substring(name.indexOf("(")+1,name.indexOf(")"));
                            double weight;
                            double incorrect=0.0;
                            if(insideParen.indexOf("/")==-1){
                                weight = Double.parseDouble(insideParen);
                            } else{
                                weight = Double.parseDouble(insideParen.substring(0,insideParen.indexOf("/")));
                                incorrect = Double.parseDouble(insideParen.substring(insideParen.indexOf("/")+1));
                            }
                            name = name.substring(0,name.indexOf("(")-1);
                            thisNode.setWeight((int) weight);
                            if(!name.contains("not")){ // selected
                                thisNode.setClassifiedAsSelected((int) (weight-incorrect));
                            } else{    // not selected
                                thisNode.setClassifiedAsSelected((int) incorrect);
                            }
                        }
                        thisNode.setName(name);
                    } else{
                        System.out.println("sth went wrong");
                    }
                }
            }
            else{ // link
                String ids = line.substring(0,line.indexOf("[")-1);
                id = ids.substring(ids.indexOf("N")+1,ids.indexOf("->"));
                String newid = ids.substring(ids.indexOf("->")+3);
                TreeNode thisNode = root.findDescendent(root, id);
                TreeNode newNode = new TreeNode(newid);
                if(line.contains("true")){
                    newNode.setCond(true);
                }else if(line.contains("false")){
                    newNode.setCond(false);
                }else {
                    System.out.println("link without condition");
                }
                thisNode.addChild(newNode);
                existingNodes.add(newid);
            }
            g = rest;
        }
        root.updateAllClassifiedAsSelected(root);
        root.updateAllWeights(root);
        return root;
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

}
