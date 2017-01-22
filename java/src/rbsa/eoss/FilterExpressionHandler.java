/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.bson.Document;
import rbsa.eoss.local.Params;

/**
 *
 * @author bang
 */
public class FilterExpressionHandler {

    private DBQueryBuilder dbq;
    private String[] instr_list;
    private String[] orbit_list;
    private int norb;
    private int ninstr;
    private JessExpressionAnalyzer jea;

            
    public FilterExpressionHandler(){
        instr_list = Params.instrument_list;
        orbit_list = Params.orbit_list;
        norb = orbit_list.length;
        ninstr = instr_list.length;
        dbq = new DBQueryBuilder();
        jea = new JessExpressionAnalyzer();
    }
    public FilterExpressionHandler(DBQueryBuilder dbq){
        instr_list = Params.instrument_list;
        orbit_list = Params.orbit_list;
        norb = orbit_list.length;
        ninstr = instr_list.length;
        this.dbq = dbq;
        jea = new JessExpressionAnalyzer();
    }
    

    
    public ArrayList<Integer> processSingleFilterExpression(String inputExpression){
        boolean preset;
        if(inputExpression.contains(":")){
            preset=false;
        }else{
            preset=true;
        }
        return processSingleFilterExpression(inputExpression,preset);
    }
    
    
    public ArrayList<Integer> processSingleFilterExpression(String inputExpression, boolean preset){
        // Examples of feature expressions 
        // Preset filter: {presetName[orbits;instruments;numbers]}   
        
        ArrayList<Integer> matchedArchIDs = new ArrayList<>();
        String exp;
        if(inputExpression.startsWith("{") && inputExpression.endsWith("}")){
            exp = inputExpression.substring(1,inputExpression.length()-1);
        }else{
            exp = inputExpression;
        }
        
        if(preset){
            String presetName = exp.split("\\[")[0];
            String arguments = exp.substring(0,exp.length()-1).split("\\[")[1];
            
            String[] argSplit = arguments.split(";");
            String[] orbits = new String[1];
            String[] instruments = new String[1];
            String[] numbers = new String[1];
            
            if(argSplit.length>0){
                orbits = argSplit[0].split(",");
            }
            if(argSplit.length>1){
                instruments = argSplit[1].split(",");
            }
            if(argSplit.length>2){
                numbers = argSplit[2].split(",");
            }
            
            
//            System.out.println(arguments);
//            for(int i=0;i<orbits.length;i++){
//                System.out.println("orbit" + i + ": " + orbits[i]);
//            }
//            for(int i=0;i<instruments.length;i++){
//                System.out.println("instruments" + i + ": " + instruments[i]);
//            }            
//            for(int i=0;i<numbers.length;i++){
//                System.out.println("numbers" + i + ": " + numbers[i]);
//            }


            ArrayList<org.bson.Document> docs = dbq.getMetadata();
            for(org.bson.Document doc:docs){
                int ArchID = doc.get("ArchID",Double.class).intValue();
                String bitString = doc.get("bitString", String.class);
                if(comparePresetFilter(bitString, presetName,orbits,instruments,numbers)){
                    matchedArchIDs.add(ArchID);
                }
            }
        }else{
        // Examples of feature expressions
        // Variable in String: "{collectionName:gt[0],slotName:String}"
        // Variable in String: "{collectionName:gt[0],slotName:'String'}"
        // Variable in Double: "{collectionName:gt[0],slotName:[minVal;maxVal]}"
        // Variable in Double: "{collectionName:gt[0],slotName:[;maxVal]}"  
        // Conditions put on multiple slots: "{collectionName:gt[0],slotName:[minVal;],slotName:String}"
            
            String collectionExpression = exp.split(",",2)[0];
            String collectionName = collectionExpression.split(":")[0];
            String collectionArguments = collectionExpression.split(":")[1];
            String collectionCondition = collectionArguments.split("\\[")[0];
            String collectionNumber = collectionArguments.split("\\[")[1];
            collectionNumber = collectionNumber.substring(0,collectionNumber.length()-1);
            
            String slotExpression = exp.split(",",2)[1];
            String slotName = slotExpression.split(":")[0];
            String slotArguments = slotExpression.split(":")[1];
                        
            ArrayList<String> slotNames = new ArrayList<>();
            ArrayList<String> conditions = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            ArrayList<String> valueTypes = new ArrayList<>();
            

            // Set up conditions to make a query
            if(slotArguments.startsWith("[")){ // Numeric argument
                // Remove square brackets
                slotArguments = slotArguments.substring(1,slotArguments.length()-1); 

                if(slotArguments.contains(";")){
                    // Range given
                    String[] argSplit = slotArguments.split(";");
                    if(argSplit[0].isEmpty()){ // Only max value specified
                        slotNames.add(slotName);
                        conditions.add("lt");
                        values.add(argSplit[1]);
                        valueTypes.add("Double");
                    }else if(argSplit.length==1){ // Only min value specified
                        slotNames.add(slotName);
                        conditions.add("gt");
                        values.add(argSplit[0]);
                        valueTypes.add("Double");
                    }else{ // Both min and max values specified
                        slotNames.add(slotName);
                        conditions.add("gt");
                        values.add(argSplit[0]);
                        valueTypes.add("Double");
                        slotNames.add(slotName);
                        conditions.add("lt");
                        values.add(argSplit[1]);
                        valueTypes.add("Double");                            
                    }
                }else{
                    // Exact value given
                        slotNames.add(slotName);
                        conditions.add("eq");
                        values.add(slotArguments);
                        valueTypes.add("Double");
                }
            }else{ // String argument
                if(slotArguments.startsWith("'")){
                    // Search using regex
                    conditions.add("regex");
                    slotArguments = slotArguments.substring(1,slotArguments.length()-1);
                }else{
                    conditions.add("eq");
                }
                slotNames.add(slotName);
                values.add(slotArguments);
                valueTypes.add("String");
            }
                
                
            // Make a query on Facts and get the ID's of architectures that contain those Facts
            ArrayList<Integer> matchedArchIDs_slot = dbq.makeQuery_ArchID(collectionName, slotNames, conditions, values, valueTypes);

            // FactCounter counts the number of Facts corresponding to each architecture
            HashMap<Integer,Integer> FactCounter = new HashMap<>();
            for(int id:matchedArchIDs_slot){
                // Keys are unique architecture ID's
                if(FactCounter.containsKey(id)){
                    int cnt = FactCounter.get(id);
                    FactCounter.put(id, cnt+1);
                }else{
                    FactCounter.put(id, 1);
                }
            }
            String inequalitySign = collectionCondition;
            for(int uniqueArchID:FactCounter.keySet()){
                boolean pass = compare_number(FactCounter.get(uniqueArchID),inequalitySign,Integer.parseInt(collectionNumber));
                if(pass){
                    matchedArchIDs.add(uniqueArchID);
                }
            }  
            
        }
        return matchedArchIDs;
    }    
    
    
    
    
    
    public boolean compare_number(int num1, String condition, int num2){
        if(condition.equals("eq")){
            return num1==num2;
        }else if(condition.equals("gt")){
            return num1>num2;
        }else if(condition.equals("lt")){
            return num1<num2;
        }else if(condition.equals("gte")){
            return num1>=num2;
        }else if(condition.equals("lte")){
            return num1<=num2;
        }else if(condition.equals("ne")){
            return num1!=num2;
        }
        return false;
    }    
    
    
    public boolean comparePresetFilter(String bitString, String type, String[] orbits, String[] instruments, String[] numbers){
        
        int[][] mat = booleanString2IntArray(bitString);
        if(type.equalsIgnoreCase("present")){
            int instrument = Integer.parseInt(instruments[0]);
            for (int i=0;i<norb;i++) {
                if (mat[i][instrument]==1) return true;
            }
            return false;
        } else if(type.equalsIgnoreCase("absent")){
            
            int instrument = Integer.parseInt(instruments[0]);
            for (int i = 0; i < norb; ++i) {
                if (mat[i][instrument]==1) return false;
            }
            return true;  
        } else if(type.equalsIgnoreCase("inOrbit")){
            
            int orbit = Integer.parseInt(orbits[0]);
            int instrument = Integer.parseInt(instruments[0]);
            return mat[orbit][instrument] == 1;
            
        } else if(type.equalsIgnoreCase("notInOrbit")){
            
            int orbit = Integer.parseInt(orbits[0]);
            int instrument = Integer.parseInt(instruments[0]);
            return mat[orbit][instrument] == 0;
            
        } else if(type.equalsIgnoreCase("together")){
            
            for(int i=0;i<norb;i++){
                boolean together = true;
                for(int j=0;j<instruments.length;j++){
                    int instrument = Integer.parseInt(instruments[j]);
                    if(mat[i][instrument]==0){together=false;}
                }
                if(together){return true;}
            }
            return false;
            
        } else if(type.equalsIgnoreCase("togetherInOrbit")){
            
            int orbit = Integer.parseInt(orbits[0]);
            boolean together = true;
            for(int j=0;j<instruments.length;j++){
                int instrument = Integer.parseInt(instruments[j]);
                if(mat[orbit][instrument]==0){together=false;}
            }
            if(together){return true;}            
            return false;
            
        } else if(type.equalsIgnoreCase("separate")){
            
            for(int i=0;i<norb;i++){
                boolean together = true;
                for(int j=0;j<instruments.length;j++){
                    int instrument = Integer.parseInt(instruments[j]);
                    if(mat[i][instrument]==0){together=false;}
                }
                if(together){return false;}
            }
            return true;
            
        } else if(type.equalsIgnoreCase("emptyOrbit")){
            
            int orbit = Integer.parseInt(orbits[0]);
            for(int i=0;i<ninstr;i++){
                if(mat[orbit][i]==1){return false;}
            }
            return true;
           
        } else if(type.equalsIgnoreCase("numOrbits")){
            
            int num = Integer.parseInt(numbers[0]);
            int count = 0;
            for (int i = 0; i < norb; ++i) {
               boolean empty= true;
               for (int j=0; j< ninstr; j++){
                   if(mat[i][j]==1){
                       empty= false;
                   }
               }
               if(empty==false) count++;
            }
            return count==num;     
            
        } else if(type.equalsIgnoreCase("numOfInstruments")){
            // Three cases
            //numOfInstruments[;i;j]
            //numOfInstruments[i;;j]
            //numOfInstruments[;;i]
            
            int num = Integer.parseInt(numbers[0]);
            int count = 0;

            if(orbits[0]!=null && !orbits[0].isEmpty()){
                // Number of instruments in a specified orbit
                int orbit = Integer.parseInt(orbits[0]);
                for(int i=0;i<ninstr;i++){
                    if(mat[orbit][i]==1){count++;}
                }
            }else if(instruments[0]!=null && !instruments[0].isEmpty()){
                // Number of a specified instrument
                int instrument = Integer.parseInt(instruments[0]);
                for(int i=0;i<norb;i++){
                    if(mat[i][instrument]==1){count++;}
                }
            }else{
                // Number of instruments in all orbits
                for(int i=0;i<norb;i++){
                    for(int j=0;j<ninstr;j++){
                        if(mat[i][j]==1){count++;}
                    }
                }
            }
            if(count==num){return true;}
            return false;
            
        } else if(type.equalsIgnoreCase("subsetOfInstruments")){ 
            // To be implemented later
        }
        
        return false;
    }
    
    
    
    public int[][] booleanString2IntArray(String booleanString){
        int[][] mat = new int[norb][ninstr];
        for(int i=0;i<norb;i++){
            for(int j=0;j<ninstr;j++){
                int loc = i*ninstr+j;
                mat[i][j] = Integer.parseInt(booleanString.substring(loc,loc+1));
            }
        }
        return mat;
    }
    
    


    
    public ArrayList<Integer> processFilterExpression(String filterExpression, ArrayList<Integer> prevMatched, String prevLogic){
        String e=filterExpression;
        // Remove outer parenthesis
        if(e.startsWith("(") && e.endsWith(")")){
            e=e.substring(1,e.length()-1);
        }
        ArrayList<Integer> currMatched = new ArrayList<>();
        boolean first = true;
        
        String e_collapsed;
        if(jea.getNestedParenLevel(e)==0){
            // Given expression does not have a nested structure
            if(e.contains("&&")||e.contains("||")){
               e_collapsed=e; 
            }else{
                currMatched = this.processSingleFilterExpression(filterExpression);
                return compareMatchedIDSets(prevLogic, currMatched, prevMatched);
            }
        }else{
            // Removes the nested structure
            e_collapsed = jea.collapseAllParenIntoSymbol(e);
        }

        while(true){
            String current_collapsed;
            String prev;
            
            if(first){
                // The first filter in a series to be applied
                prev = "||";
                first = false;
            }else{
                prev = e_collapsed.substring(0,2);
                e_collapsed = e_collapsed.substring(2);
                e = e.substring(2);
            }
            
            String next; // The imediate next logical connective
            int and = e_collapsed.indexOf("&&");
            int or = e_collapsed.indexOf("||");
            if(and==-1 && or==-1){
                next = "";
            } else if(and==-1){ 
                next = "||";
            } else if(or==-1){
                next = "&&";
            } else if(and < or){
                next = "&&";
            } else{
                next = "||";
            }
            
            if(!next.isEmpty()){
                if(next.equals("||")){
                    current_collapsed = e_collapsed.split("\\|\\|",2)[0];
                }else{
                    current_collapsed = e_collapsed.split(next,2)[0];
                }
                String current = e.substring(0,current_collapsed.length());
                e_collapsed = e_collapsed.substring(current_collapsed.length());
                e = e.substring(current_collapsed.length());
                currMatched = processFilterExpression(current,currMatched,prev); 
            }else{
                currMatched = processFilterExpression(e,currMatched,prev); 
                break;
            }
        }
        return compareMatchedIDSets(prevLogic, currMatched, prevMatched);
    }
    
    
    
    
    public ArrayList<Integer> compareMatchedIDSets(String logic, ArrayList<Integer> set1, ArrayList<Integer> set2){
        ArrayList<Integer> output = new ArrayList<>();
        if(logic.equals("&&")){
            for(int i:set1){
                if(set2.contains(i)){
                    output.add(i);
                }
            }
        }else{
            for(int i:set1){
                output.add(i);
            }
            for(int i:set2){
                if(!output.contains(i)){
                    output.add(i);
                }
            }
        }
        return output;
    }
    
    
    
    
    

    
}
