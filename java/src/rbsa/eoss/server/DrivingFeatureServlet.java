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

import rbsa.eoss.dm.DrivingFeature;
import rbsa.eoss.dm.DrivingFeaturesGenerator;
import rbsa.eoss.ResultManager;
import rbsa.eoss.Scheme;
import rbsa.eoss.local.Params;
import rbsa.eoss.FilterExpressionHandler;
import rbsa.eoss.DBQueryBuilder;

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
    FilterExpressionHandler feh;
    DBQueryBuilder dbq;

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
            dbq = new DBQueryBuilder();
            feh = new FilterExpressionHandler(dbq);
        }
        
        String outputString="";
        String requestID = request.getParameter("ID");
        try {
            
        long t0 = System.currentTimeMillis();
        
        if(requestID.equalsIgnoreCase("generateDrivingFeatures")){
        	
            
            double support_threshold = Double.parseDouble(request.getParameter("supp"));
            double confidence_threshold = Double.parseDouble(request.getParameter("conf"));
            double lift_threshold = Double.parseDouble(request.getParameter("lift")); 
            int numIntervals = Integer.parseInt(request.getParameter("numIntervals")); 
             
            //[1,2,3,4,5]
            String selectedArchs_raw = request.getParameter("selected");
            String[] selectedArchs_split = selectedArchs_raw.substring(1, selectedArchs_raw.length()-1).split(",");
            String nonSelectedArchs_raw = request.getParameter("nonSelected");
            String[] nonSelectedArchs_split = nonSelectedArchs_raw.substring(1, nonSelectedArchs_raw.length()-1).split(",");
            
            ArrayList<Integer> behavioral = new ArrayList<>();
            ArrayList<Integer> non_behavioral = new ArrayList<>();

            
            for (String selectedArchs_split1:selectedArchs_split) {
                int tmp = Integer.parseInt(selectedArchs_split1);
                if(!behavioral.contains(tmp)){
                    behavioral.add(tmp);
                }
            }
            for (String non_selectedArchs_split1:nonSelectedArchs_split) {
                int tmp = Integer.parseInt(non_selectedArchs_split1);
                if(!non_behavioral.contains(tmp) && !behavioral.contains(tmp)){
                    non_behavioral.add(tmp);
                }
            }
            
            String scope = request.getParameter("scope");
            
            dfsGen = new DrivingFeaturesGenerator(dbq);
            dfsGen.initialize(scope, behavioral, non_behavioral, support_threshold,confidence_threshold,lift_threshold, numIntervals);
            
//            String userdef_features = request.getParameter("userDefFeatures");
//                        
//            String[] userdef_features_split = {};
//            if(!userdef_features.isEmpty()){
//                if(userdef_features.startsWith("[") && userdef_features.endsWith("]")){
//                    userdef_features = userdef_features.substring(1,userdef_features.length()-1);
//                }
//                if(userdef_features.startsWith("\"") && userdef_features.endsWith("\"")){
//                    userdef_features = userdef_features.substring(1,userdef_features.length()-1);
//                }
//                userdef_features_split= userdef_features.split("\",\"");
//            }


//            dfsGen.setUserDefFeatures(userdef_features_split);

            dfsGen.getPrimitiveDrivingFeatures();
            DFs = (ArrayList) dfsGen.getDrivingFeatures();
            
            if(DFs.isEmpty()){
                outputString="";
            }else{
                String jsonObj = gson.toJson(DFs);
                outputString = jsonObj;
            }
            

            long t1 = System.currentTimeMillis();
            System.out.println( "Data mining done in: " + String.valueOf(t1-t0) + " msec");
                        
            
        } 


        else if(requestID.equalsIgnoreCase("applyFilter")){
            String filterExpression_raw = request.getParameter("filterExpression");
            String preset = request.getParameter("preset");

            String filterExpression = "{"+filterExpression_raw+"}";
            boolean isPreset = false;
            if(preset.equalsIgnoreCase("true")){
                isPreset = true;
            }
            
            ArrayList<Integer> matchedArchIDs = feh.processSingleFilterExpression(filterExpression, isPreset);
            String jsonObj = gson.toJson(matchedArchIDs);
            outputString = jsonObj;            
        }

        else if(requestID.equalsIgnoreCase("applyComplexFilter")){
            String filterExpression_raw = request.getParameter("filterExpression");
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
