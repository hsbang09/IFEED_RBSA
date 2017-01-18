/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCursor;
import com.mongodb.DBCursor;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import jess.Defrule;
import jess.Fact;
import jess.Rete;
import rbsa.eoss.local.Params;


/**
 *
 * @author Bang
 */
public class DBManagement {
    
    private MongoClient mongoClient;
//    private String dbName = "EOSS_eval_data";
    private String dbName = "rbsa_eoss";
    private String metaDataCollectionName = "metadata";
    private String ruleCollectionName = "jessRules";
    private ArrayList<String> dataCollectionNames;
    private static DBManagement instance = null;

    
    public DBManagement(){
        try{            
//            mongoClient = new MongoClient( "localhost" , 27017 );
            MongoClientURI uri = new MongoClientURI("mongodb://bang:qkdgustmd@ds145828.mlab.com:45828/rbsa_eoss");
            mongoClient = new MongoClient(uri);
            
            dataCollectionNames = new ArrayList<>();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }    
    
    public DBManagement(String dbName){
        try{            
            this.dbName = dbName;
            mongoClient = new MongoClient( "localhost" , 27017 );
            dataCollectionNames = new ArrayList<>();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static DBManagement getInstance()
    {
        if( instance == null ) 
        {
            instance = new DBManagement();
        }
        return instance;
    }


    
    
    

    public void addNewCollection(String colName){
        this.dataCollectionNames.add(colName);
    }
    
    public void createNewDB(){
        boolean dbExists = false;
        MongoCursor<String> iter = mongoClient.listDatabaseNames().iterator();
        while(iter.hasNext()){
            if(iter.next().equals(dbName)){
                dbExists = true;
            }
        }   
        if(dbExists) {
            mongoClient.getDatabase(dbName).drop();
        }
    }
    
    

    public void encodeMetadata(int ArchID, String bitString, double science, double cost){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(metaDataCollectionName);
        col.insertOne(
                new Document()
                    .append("ArchID", ArchID)
                    .append("bitString",bitString)
                    .append("science", science)
                    .append("cost",cost)
        );
    }
    
    public void encodeData(int ArchID, String collectionPrefix, Rete r, QueryBuilder qb){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);

        ArrayList<Integer> factsToEncode = new ArrayList<>();
        ArrayList<Integer> factsEncoded = new ArrayList<>();
            
        try{
            
            JessFactHandler jfh = null;
            if(collectionPrefix.equalsIgnoreCase("science")){
                jess.Fact value = qb.makeQuery("AGGREGATION::VALUE").get(0);
                jfh = new JessFactHandler(value, r, qb);
                factsToEncode = jfh.getParentFactIDs();
            }else if(collectionPrefix.equalsIgnoreCase("cost")){
                ArrayList<Fact> missions = qb.makeQuery("MANIFEST::Mission");
                for(Fact m:missions){
                    factsToEncode.add(m.getFactId());
                }
                jfh = new JessFactHandler(missions.get(0), r, qb);
            }
            
            int cnt =0;
            while(factsToEncode.size() > 0){
                // Get the first element from the array and remove it from the list
                jess.Fact thisFact = r.findFactByID(factsToEncode.get(0));
                
                // Requested Fact had been retracted
                if(thisFact==null){
                    // Related to the launch vehicle selection during cost calculation
                    System.out.println("Requested Fact had been retracted"); 
                    factsEncoded.add(factsToEncode.get(0));
                    factsToEncode.remove(0);
                    cnt++;
                    continue;
                } else if(thisFact.getName().equals("DATABASE::Instrument")){
                    jess.Value slotVal = thisFact.getSlotValue("Name");
                    String instrumentName = slotVal.stringValue(r.getGlobalContext());                    
                    if(this.QueryExists("science.DATABASE.Instrument","Name",instrumentName)){
                        // Remove the encoded fact from the list
                        factsToEncode.remove(0);
                        factsEncoded.add(thisFact.getFactId());
                        continue;
                    }
                } else if(thisFact.getName().equals("DATABASE::Launch-vehicle")){
                    jess.Value slotVal = thisFact.getSlotValue("id");
                    String lvName = slotVal.stringValue(r.getGlobalContext());                    
                    if(this.QueryExists("cost.DATABASE.Launch_vehicle","id",lvName)){
                        // Remove the encoded fact from the list
                        factsToEncode.remove(0);
                        factsEncoded.add(thisFact.getFactId());
                        continue;
                    }
                }
                
                // Encode the fact
                org.bson.Document doc = encodeFact(ArchID, thisFact,r,qb);
                
                String collectionName = collectionPrefix + "." + thisFact.getName().replace("::",".");
                collectionName = collectionName.replace("-", "_");
                
                MongoCollection col = Mdb.getCollection(collectionName);
                col.insertOne(doc);
                
                // Remove the encoded fact from the list
                factsToEncode.remove(0);
                factsEncoded.add(thisFact.getFactId());
                // Get new list of facts to encode
                jfh.setNewFact(thisFact);
                ArrayList<Integer> newFacts = jfh.getParentFactIDs();
                for(int fid:newFacts){
                    // If it is a new fact, add it to the list
                    if(!factsToEncode.contains(fid) && !factsEncoded.contains(fid)){
                        factsToEncode.add(fid);
                    }
                }           
                cnt++;
                if(cnt>3000){break;}
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public org.bson.Document encodeFact(int ArchID, jess.Fact f, Rete r, QueryBuilder qb){
        
        org.bson.Document doc = new org.bson.Document();
        
        try{
            doc.append("ArchID",ArchID);
            doc.append("factName", f.getName());
            doc.append("factID",f.getFactId());
            doc.append("module", f.getModule());            
        
            jess.Deftemplate factTemplate = f.getDeftemplate();
            String[] slots = factTemplate.getSlotNames();
            
            for(int i=0;i<slots.length;i++){
                String slot = slots[i];
                jess.Value slotVal = f.getSlotValue(slot);
                String slotVal_string = slotVal.toString();
                
                if(slotVal_string.contains("Java-Object")){
                    // Java Objects are not saved in the database for now
                    continue;
                }else if(slotVal_string.isEmpty() || slotVal_string.equals("\"\"")){
                    // Value is empty
                    continue;
                }
                
                if(factTemplate.isMultislot(i)){
                    // Save as string
                    doc.append(slot, slotVal_string);
                }else{
                    // Not a multi-slot
                    if(!slotVal_string.equals("nil")){
                        // If the value is nil then don't save it in the DB
                        if(slotVal.isNumeric(r.getGlobalContext())){
                            if(slotVal_string.contains(".")){ 
                                // float
                                double slotVal_double = slotVal.floatValue(r.getGlobalContext());
                                doc.append(slot,slotVal_double);
                            }else{ 
                                // integer
                                int slotVal_int = slotVal.intValue(r.getGlobalContext());
                                doc.append(slot,slotVal_int);
                            }
                        }else{
                            // Save as string
                            String slotVal_string2 = slotVal.stringValue(r.getGlobalContext());
                            doc.append(slot, slotVal_string2);
                        }
                    }
                }
                
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(f.getFactId());
        }
        return doc;
    }
    
    
    
    public void encodeRules(){
        
        HashMap<Integer,String> rules_id_to_name = Params.rules_IDtoName_map;
        HashMap<String,Integer> rules_name_to_id = Params.rules_NametoID_Map;
        HashMap<String,Defrule> rules = Params.rules_defrule_map;
        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(this.ruleCollectionName);

        Set<Integer> ruleIDs = rules_id_to_name.keySet();
        Iterator<Integer> iter = ruleIDs.iterator();
        
        try{
            while(iter.hasNext()){
                
                int id = iter.next();
                String ruleName = rules_id_to_name.get(id);
                Defrule defrule = rules.get(ruleName);
                
                String module = ruleName.split("::")[0];
                                
                // Encode the fact
                org.bson.Document doc = new org.bson.Document();
                
                doc.append("ruleID",id)
                    .append("ruleName",ruleName)
                    .append("module",module);
                col.insertOne(doc);  
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }    
    
    
    
    
    
    
    /**
     * Returns the number of documents encoded in the metadata
     * @return 
     */
    
    public double getNArchs(){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(this.metaDataCollectionName);
        return col.count();
    }
    
    /**
     * Checks if the dataset contains the data related to a specific architecture defined by a boolean string
     * @param booleanString
     * @return 
     */
    public boolean findMatchingArch(String booleanString){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        
        MongoCollection col = Mdb.getCollection(this.metaDataCollectionName);
        Document filter = new Document("bitString",booleanString);
  
        FindIterable found = col.find(filter);
        MongoCursor iter = found.iterator();
        if(iter.hasNext()){
            return true;
        }
        return false;
    }
    
    /**
     * Checks if there is a document whose slot value matches with a certain string input
     * @param collectionName: Name of the collection
     * @param slotName: Slot Name
     * @param value: Value to compare with the actual slot value
     * @return 
     */
    public boolean QueryExists(String collectionName, String slotName, String value){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        Document filter = new Document(slotName,value);
        FindIterable found = col.find(filter);
        MongoCursor iter = found.iterator();
        return iter.hasNext();    
    }
    
    /**
     * Finds and returns all bson documents in the metadata collection
     * @return 
     */
    public ArrayList<org.bson.Document> getMetadata(){
        ArrayList<org.bson.Document> docs = new ArrayList<>();
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(this.metaDataCollectionName);
        FindIterable found = col.find();
        MongoCursor iter = found.iterator();
        while(iter.hasNext()){
            org.bson.Document doc = (Document) iter.next();
            docs.add(doc);
        }
        return docs;
    }
    
    
    
    /**
     * Returns a String array, containing the minimum value, the maximum value, and the class information of a given slot
     * @param collectionName: Name of a collection
     * @param slotName: Name of a slot
     * @return Returns a string array of length 3. The first element is the minimum value stored in String.
     *          The second element is the maximum value stored in String. The third element is the name of the class.
     *          The class can be either java.lang.Double or java.lang.Integer.
     */
    public String[] getMinMaxValue(String collectionName,String slotName){        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        FindIterable found = col.find();
        MongoCursor iter = found.iterator();
        if(!iter.hasNext()){
            System.out.println("Warning: The collection is empty (Check the collection name)");
            return new String[3];
        }
        org.bson.Document tempdoc = (Document) iter.next();
        String cl = tempdoc.get(slotName).getClass().toString(); 
        if(cl.contains("String")){
            System.out.println("Warning: The given slot is a String");
            return new String[3];
        }
        double min = 9999999;
        double max = -9999999;
        while(iter.hasNext()){
            org.bson.Document doc = (Document) iter.next();
            double val = (double) doc.get(slotName);
            if(val > max){
                max = val;
            }
            if(val < min){
                min = val;
            }
        }
        String[] min_max_class = new String[3];
        min_max_class[0] = Double.toString(min);
        min_max_class[1] = Double.toString(max);
        min_max_class[2] = cl;
        return min_max_class;
    }
        
    
    /**
     * Returns a String specifying what the class of a requested slot is
     * @param collectionName: Name of a collection
     * @param slotName: Name of a slot
     * @return 
     */
    public String getClassOfSlot(String collectionName, String slotName){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        FindIterable found = col.find();
        MongoCursor iter = found.iterator();
        org.bson.Document doc = (org.bson.Document) iter.next();
        return doc.get(slotName).getClass().toString();
    }  
    
    
    
    public ArrayList<String> getValidValueList(String collectionName,String slotName){        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        FindIterable found = col.find();
        MongoCursor iter = found.iterator();
        ArrayList<String> list = new ArrayList<>();
        while(iter.hasNext()){
            org.bson.Document doc = (Document) iter.next();
            String val = doc.get(slotName, String.class);
            if(!list.contains(val)){
                list.add(val);
            }
        }
        return list;
    }
 
    
    /**
     * Returns names of all the slots for a given collection. 
     * @param collectionName: Name of a collection in the database
     * @param mode: 'slow' mode will go over all documents to gather the slot names (useful when the slot names vary across documents)
     *              'fast' mode will only check the first document to save the slot names
     * @return 
     */
    public ArrayList<String> getSlotNames(String collectionName, String mode){
        ArrayList<String> slotNames = new ArrayList<>();
        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        FindIterable found = col.find();
        MongoCursor iter = found.iterator();
        if(!iter.hasNext()){
            System.out.println("Warning: The collection is empty (Check the collection name)");
            return slotNames;
        }
        if (mode.equalsIgnoreCase("slow")){
            while(iter.hasNext()){
                org.bson.Document doc = (Document) iter.next();
                for(String key:doc.keySet()){
                    if(slotNames.contains(key)){continue;}
                    else{slotNames.add(key);}
                }
            }
        }
        else{
            org.bson.Document doc = (Document) iter.next();
            for(String key:doc.keySet()){
                slotNames.add(key);
            }
        }
        return slotNames;
    }
    
    
    
    
    /**
     * Makes a query from the database
     * 
     * @param collectionPrefix: Prefix for the collection name: science or cost
     * @param factName: Name of the fact to be searched
     * @param slots: Names of the slots to be used in the filter
     * @param conditions: Equality and inequality signs. Valid input are: gt, lt, gte, lte, eq
     * @param values: Values to be compared.
     * @param valueTypes: Types of values. Valid inputs are: Integer, Double, String
     */
    public void makeQuery(String collectionPrefix, String factName, ArrayList<String> slots, ArrayList<String> conditions, 
                            ArrayList<String> values, ArrayList<String> valueTypes){
        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        
        String collectionName = collectionPrefix + "." + factName.replace("::",".").replace("-", "_");
        MongoCollection col = Mdb.getCollection(collectionName);
        
        Document filter = new Document("factName",factName);
        
        for(int i=0;i<slots.size();i++){
            String slotName = slots.get(i);
            String cond = conditions.get(i);
            String val = values.get(i);
            String valType = valueTypes.get(i);
            if(cond.equals("eq")){
                if(valType.equals("String")){
                    filter.append(slotName,val);
                }
                else if(valType.equals("Integer")){
                    filter.append(slotName, Integer.parseInt(val));
                }
                else if(valType.equals("Double")){
                    filter.append(slotName, Double.parseDouble(val));
                }
            }
            else if(cond.equals("gt") || cond.equals("gte") || cond.equals("lt") || cond.equals("lte")){
                if(valType.equals("String")){
                    filter.append(slotName, new Document("$"+cond,val));
                }
                else if(valType.equals("Integer")){
                    filter.append(slotName, new Document("$"+cond,Integer.parseInt(val)));
                }
                else if(valType.equals("Double")){
                    filter.append(slotName, new Document("$"+cond,Double.parseDouble(val)));
                }
            }            
        }
        
        FindIterable found = col.find(filter);
        found.projection(fields(
                    exclude("_id","factID","factHistory")
                ));
        MongoCursor iter = found.iterator();
        while(iter.hasNext()){
            Document doc = (Document) iter.next();
            System.out.println(doc.toString());
        }
    }
    

    
    
    public class JessFactHandler{
        
        private jess.Fact f;
        private int factID;
        private String factName;
        private String factHistory;
        private Rete r;
        private QueryBuilder qb;
        
        public JessFactHandler(jess.Fact f, Rete r, QueryBuilder qb){
            try{
                this.factID = f.getFactId();
                this.factName = f.getName();
                String facthis = f.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                
//              "{R110 {R112 {R112 {R112 F672 S675} S674} S673}}"
                this.factHistory = facthis.replace('{', '(').replace('}',')');
                this.r = r;
                this.qb = qb;
                
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public String getName(){return this.factName;}
        public int getID(){return this.factID;}
        
        public void setNewFact(jess.Fact f){
            try{
                this.factID = f.getFactId();
                this.factName = f.getName();
                String facthis = f.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                this.factHistory = facthis.replace('{', '(').replace('}',')');
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        
        /**
         * Returns a list of ID's of the Facts that are used to generate and modify the current Fact
         * 
         * @return ArrayList of factIDs
         */
        public ArrayList<Integer> getParentFactIDs(){
            ArrayList<Integer> factIDs = new ArrayList<>();

            JessExpressionAnalyzer jea = new JessExpressionAnalyzer();
            int level = jea.getNestedParenLevel(this.factHistory);
            
            for (int i=0; i < level; i++){
                
                String inside = jea.getInsideParen(factHistory,i+1);
                String[] insideSplit = inside.split(" ", 2);
                if (insideSplit[0].substring(1).equalsIgnoreCase("nil")){
//                    System.out.println("nil found: " + this.factID);
//                    System.out.println(factHistory);
                    continue;
                }
                // First element of a factHistory should be a rule
                int ruleID = Integer.parseInt(insideSplit[0].substring(1));
                String rest = insideSplit[1];
                rest = jea.collapseAllParenIntoSymbol(rest);
                
                String[] restSplit = rest.split(" ");                
                for (String tmp:restSplit){
                    if((tmp.startsWith("A")) || (tmp.startsWith("F")) || (tmp.startsWith("S")) || (tmp.startsWith("D")) || (tmp.startsWith("J"))){
                        // A: Newly asserted
                        // F: Modified
                        // S: Slot values are used
                        // D: Duplicated
                        // J: Asserted from Java
                        
                        
                        if(tmp.startsWith("F")){continue;}
                        
                        int id = Integer.parseInt(tmp.substring(1));
                        if(!factIDs.contains(id)){
                            factIDs.add(id);
                        }
                    }
                }                
            }            
            return factIDs;
        }
        

        
        
    }
        
        
    
   
    
    
    
    
}