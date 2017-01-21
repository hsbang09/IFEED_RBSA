/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import rbsa.eoss.DrivingFeature;
import rbsa.eoss.DrivingFeaturesGenerator;
import rbsa.eoss.ResultManager;
import rbsa.eoss.Scheme;
import rbsa.eoss.local.Params;
import rbsa.eoss.FilterExpressionHandler;

/**
 *
 * @author Bang
 */
@WebServlet(name = "DrivingFeatureServlet", urlPatterns = {"/DrivingFeatureServlet"})
public class DrivingFeatureServlet extends HttpServlet {

    
    private Gson gson = new Gson();
    ResultManager RM = ResultManager.getInstance();
//    Stack<Result> results;
//    ArchWebInterface ai;
//    Result currentResult = null;
    Scheme scheme;
    boolean init = false;
    int norb;
    int ninstr;
    String[] instrument_list;
    String[] orbit_list;
    String displayOutput;
    DrivingFeaturesGenerator dfsGen;
    ArrayList<DrivingFeature> DFs;
    ArrayList<DrivingFeature> sortedDFs;
    
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
            out.println("<title>Servlet drivingFeatureServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet drivingFeatureServlet at " + request.getContextPath() + "</h1>");
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

        if (init==false){
            norb = Params.orbit_list.length;
            ninstr = Params.instrument_list.length;
            instrument_list = Params.instrument_list;
            orbit_list = Params.orbit_list;
            scheme = new Scheme();
            init=true;

        }
        
        String outputString="";
        String requestID = request.getParameter("ID");
        try {
            

        
        if(requestID.equalsIgnoreCase("generateDrivingFeatures")){
        	
            double support_threshold = Double.parseDouble(request.getParameter("supp"));
            double confidence_threshold = Double.parseDouble(request.getParameter("conf"));
            double lift_threshold = Double.parseDouble(request.getParameter("lift")); 
             
            //[1,2,3,4,5]
            String selectedArchs_raw = request.getParameter("selected");
            String[] selectedArchs_split = selectedArchs_raw.substring(1, selectedArchs_raw.length()-1).split(",");
            String nonSelectedArchs_raw = request.getParameter("nonSelected");
            String[] nonSelectedArchs_split = nonSelectedArchs_raw.substring(1, nonSelectedArchs_raw.length()-1).split(",");
            
            ArrayList<Integer> behavioral = new ArrayList<>();
            ArrayList<Integer> non_behavioral = new ArrayList<>();

            for (String selectedArchs_split1:selectedArchs_split) {
                behavioral.add(Integer.parseInt(selectedArchs_split1));
            }
            for (String non_selectedArchs_split1:nonSelectedArchs_split) {
                non_behavioral.add(Integer.parseInt(non_selectedArchs_split1));
            }
            
            String scope = request.getParameter("scope");

            dfsGen = new DrivingFeaturesGenerator();
            dfsGen.initialize(scope,behavioral, non_behavioral, support_threshold,confidence_threshold,lift_threshold);
            
            
            
////            "[{"name":"thisName","expression":"present(ACE_ORCA)&&present(DESD_LID)"},{"name":"secondOne","expression":"present(DESD_LID)||numOrbitUsed(3)"}]"
//            String userDefFilters_raw = request.getParameter("userDefFilters");
//
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
            

            ArrayList<DrivingFeature> DFs;
            DFs = dfsGen.getDrivingFeatures();
            
            String sortingCriteria = request.getParameter("sortBy");
            ArrayList<DrivingFeature> sortedDFs;
            sortedDFs = new ArrayList<>();
            for (DrivingFeature df:DFs){
            
                double value = 0.0;
                double maxVal = 1000000.0;
                double minVal = -1.0;
                
                if (sortedDFs.isEmpty()){
                    sortedDFs.add(df);
                    continue;
                } 
                
                if(sortingCriteria.equalsIgnoreCase("lift")){
                    value = df.getMetrics()[1];
                    maxVal = sortedDFs.get(0).getMetrics()[1];
                    minVal = sortedDFs.get(sortedDFs.size()-1).getMetrics()[1];
                } else if(sortingCriteria.equalsIgnoreCase("supp")){
                    value = df.getMetrics()[0];
                    maxVal = sortedDFs.get(0).getMetrics()[0];
                    minVal = sortedDFs.get(sortedDFs.size()-1).getMetrics()[0];
                } else if(sortingCriteria.equalsIgnoreCase("confave")){
                    value = (double) (df.getMetrics()[2] + df.getMetrics()[3])/2;
                    maxVal = (double) (sortedDFs.get(0).getMetrics()[2] + sortedDFs.get(0).getMetrics()[3])/2;
                    minVal = (double) (sortedDFs.get(sortedDFs.size()-1).getMetrics()[2] + sortedDFs.get(sortedDFs.size()-1).getMetrics()[3])/2;
                } else if(sortingCriteria.equalsIgnoreCase("conf1")){
                    value = df.getMetrics()[2];
                    maxVal = sortedDFs.get(0).getMetrics()[2];
                    minVal = sortedDFs.get(sortedDFs.size()-1).getMetrics()[2];
                } else if(sortingCriteria.equalsIgnoreCase("conf2")){
                    value = df.getMetrics()[3];
                    maxVal = sortedDFs.get(0).getMetrics()[3];
                    minVal = sortedDFs.get(sortedDFs.size()-1).getMetrics()[3];
                }
                
                if (value >= maxVal){
                    sortedDFs.add(0,df);
                } else if(value <= minVal){
                    sortedDFs.add(df);
                } else {
                        for (int j=0;j<sortedDFs.size();j++){
                            
                            double refval = 0.0;
                            double refval2 = 0.0;
                            if(sortingCriteria.equalsIgnoreCase("lift")){
                                refval = sortedDFs.get(j).getMetrics()[1];
                                refval2 = sortedDFs.get(j+1).getMetrics()[1];
                            } else if(sortingCriteria.equalsIgnoreCase("supp")){
                                refval = sortedDFs.get(j).getMetrics()[0];
                                refval2 = sortedDFs.get(j+1).getMetrics()[0];
                            } else if(sortingCriteria.equalsIgnoreCase("confave")){
                                refval = (double) (sortedDFs.get(j).getMetrics()[2] + sortedDFs.get(j).getMetrics()[3])/2;
                                refval2 = (double) (sortedDFs.get(j+1).getMetrics()[2] + sortedDFs.get(j+1).getMetrics()[3])/2;
                            } else if(sortingCriteria.equalsIgnoreCase("conf1")){
                                refval = sortedDFs.get(j).getMetrics()[2];
                                refval2 = sortedDFs.get(j+1).getMetrics()[2];
                            } else if(sortingCriteria.equalsIgnoreCase("conf2")){
                                refval = sortedDFs.get(j).getMetrics()[3];
                                refval2 = sortedDFs.get(j+1).getMetrics()[3];
                            }
                            
                            if(value <= refval && value > refval2){
                                sortedDFs.add(j+1,df);
                                break;
                            }
                        } 
                }
            }
            String jsonObj = gson.toJson(sortedDFs);
            outputString = jsonObj;
   
        } 
        else if (requestID.equalsIgnoreCase("buildClassificationTree")){
//        	String graph = dfsGen.buildTree(false);
//        	outputString = graph;
        }

        
        
        
        else if(requestID.equalsIgnoreCase("applyFilter")){
            String filterExpression_raw = request.getParameter("filterExpression");
            String preset = request.getParameter("preset");

            filterExpression_raw = filterExpression_raw.substring(1,filterExpression_raw.length());
            
            System.out.println(filterExpression_raw);
            System.out.println(preset);
            
            
            FilterExpressionHandler feh = new FilterExpressionHandler();
            ArrayList<Integer> matchedArchIDs = feh.processSingleFilterExpression(filterExpression_raw, false);
            String jsonObj = gson.toJson(matchedArchIDs);
            outputString = jsonObj;            
        }

        else if(requestID.equalsIgnoreCase("applyComplexFilter")){
            String filterExpression_raw = request.getParameter("filterExpression");
            filterExpression_raw = filterExpression_raw.substring(1,filterExpression_raw.length());
            System.out.println(filterExpression_raw);
            FilterExpressionHandler feh = new FilterExpressionHandler();
            ArrayList<Integer> matchedArchIDs = feh.processFilterExpression(filterExpression_raw, new ArrayList<Integer>(), "||");
            String jsonObj = gson.toJson(matchedArchIDs);
            outputString = jsonObj;            
        }        
        
        
        
        

        }
        catch(Exception e){ e.printStackTrace();}
        
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
    
    public boolean[] bitString2booleanArray(String bitString){
        int norb = Params.orbit_list.length;
        int ninstr = Params.instrument_list.length;
        boolean[] bool = new boolean[norb*ninstr];
        
        for (int i=0;i<bitString.length();i++){
            if(bitString.substring(i, i+1).equalsIgnoreCase("1")){
                bool[i]=true;
            }else{
                bool[i]=false;
            }
        }
        return bool;
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
