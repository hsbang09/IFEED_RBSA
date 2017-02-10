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
public class DBQueryBuilder {
    
    private MongoClient mongoClient;
//    private String dbName = "EOSS_eval_data";
    private String dbName = "rbsa_eoss";
    private String metaDataCollectionName = "metadata";
    private String ruleCollectionName = "jessRules";
    private ArrayList<String> dataCollectionNames;
    private static DBQueryBuilder instance = null;

    
    public DBQueryBuilder(){
        try{            
            mongoClient = new MongoClient( "localhost" , 27017 );
//            MongoClientURI uri = new MongoClientURI("mongodb://bang:qkdgustmd@ds145828.mlab.com:45828/rbsa_eoss");
//            mongoClient = new MongoClient(uri);
            dataCollectionNames = new ArrayList<>();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }    
    public DBQueryBuilder(MongoClient client){
        mongoClient = client;
    }


    public static DBQueryBuilder getInstance()
    {
        if( instance == null ) 
        {
            instance = new DBQueryBuilder();
        }
        return instance;
    }


    
    public String getMetadataCollectionName(){
        return this.metaDataCollectionName;
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
     * @return Returns a string array of length 2. The first element is the minimum value stored in String.
     *          The second element is the maximum value stored in String. 
     */
    public String[] getMinMaxValue(String collectionName,String slotName){        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        FindIterable found = col.find();
        MongoCursor iter = found.iterator();
        if(!iter.hasNext()){
            System.out.println("Warning: The collection is empty (Check the collection name)");
            return new String[2];
        }
        org.bson.Document tempdoc = (Document) iter.next();
        String cl = tempdoc.get(slotName).getClass().toString(); 
        if(cl.contains("String")){
            //System.out.println("Warning: The given slot is a String");
            return new String[2];
        }
        String minval="";
        String maxval="";
        double min = 9999999;
        double max = -9999999;
        
        while(iter.hasNext()){
            org.bson.Document doc = (Document) iter.next();
            
            if(doc.get(slotName)==null){
                continue;
            }
            
            double val = (double) doc.get(slotName);
            if(val > max){
                max = val;
            }
            if(val < min){
                min = val;
            }
        }
                
        minval = Double.toString(min);
        maxval = Double.toString(max);
        String[] min_max_class = new String[2];
        min_max_class[0] = minval;
        min_max_class[1] = maxval;
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
     * Calls getSlotNames() in fast mode
     * @param collectionName
     * @return 
     */
    public ArrayList<String> getSlotNames(String collectionName){
        return getSlotNames(collectionName, "fast");
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
        found.projection(fields(
            exclude("_id","factName","module","factID","factHistory")
        ));
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
     * Makes a query from the database. This function can only take in a single condition.
     * 
     * @param collectionName: Name of a collection
     * @param slot: Name of the slot to be used in the filter
     * @param condition: Equality or inequality sign. Valid inputs are: gt, lt, gte, lte, eq
     * @param value: Value to be compared.
     * @param valueType: Type of the slot value. Valid inputs are: Double, String
     */
    public ArrayList<org.bson.Document> makeQuery(String collectionName, String slot, String condition, 
                        String value, String valueType){
        ArrayList<String> slots = new ArrayList<>();
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> valueTypes = new ArrayList<>();
        slots.add(slot);
        conditions.add(condition);
        values.add(value);
        valueTypes.add(valueType);
        return makeQuery(collectionName, slots, conditions, values, valueTypes);
    }
    
    
    /**
     * Makes a query from the database
     * 
     * @param collectionPrefix: Prefix for the collection name: science or cost
     * @param factName: Name of the fact to be searched
     * @param slots: Names of the slots to be used in the filter
     * @param conditions: Equality and inequality signs. Valid input are: gt, lt, gte, lte, eq
     * @param values: Values to be compared.
     * @param valueTypes: Types of values. Valid inputs are: Double, String
     */
    public ArrayList<org.bson.Document> makeQuery(String collectionPrefix, String factName, ArrayList<String> slots, ArrayList<String> conditions, 
                            ArrayList<String> values, ArrayList<String> valueTypes){ 
        String collectionName = collectionPrefix + "." + factName.replace("::",".").replace("-", "_");
        return makeQuery(collectionName,factName,slots,conditions,values,valueTypes);
    }   
    
    /**
     * Makes a query from the database
     * 
     * @param collectionName: Name of a collection
     * @param slots: Names of the slots to be used in the filter
     * @param conditions: Equality and inequality signs. Valid input are: gt, lt, gte, lte, eq
     * @param values: Values to be compared.
     * @param valueTypes: Types of values. Valid inputs are: Double, String
     * @return A list of bson documents
     */
    public ArrayList<org.bson.Document> makeQuery(String collectionName, ArrayList<String> slots, ArrayList<String> conditions, 
                            ArrayList<String> values, ArrayList<String> valueTypes){
        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        
        Document filter = new Document();
        
        for(int i=0;i<slots.size();i++){
            String slotName = slots.get(i);
            String cond = conditions.get(i);
            String val = values.get(i);
            String valType = valueTypes.get(i);
            if(cond.equals("eq")){
                if(valType.equals("String")){
                    filter.append(slotName,val);
                }
                else if(valType.equals("Double")){
                    filter.append(slotName, Double.parseDouble(val));
                }
            }
            else if(cond.equals("gt") || cond.equals("gte") || cond.equals("lt") || cond.equals("lte") || cond.equals("ne")){
                if(valType.equals("String")){
                    filter.append(slotName, new Document("$"+cond,val));
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
        ArrayList<org.bson.Document> docs = new ArrayList<>();
        while(iter.hasNext()){
            Document doc = (Document) iter.next();
            docs.add(doc);
        }
        return docs;
    }
    
    /**
     * Makes a query from the database and returns the ID's of the matching architectures
     * 
     * @param collectionName: Name of a collection
     * @param slots: Names of the slots to be used in the filter
     * @param conditions: Equality and inequality signs. Valid input are: gt, lt, gte, lte, eq
     * @param values: Values to be compared.
     * @param valueTypes: Types of values. Valid inputs are: Double, String
     */
    public ArrayList<Integer> makeQuery_ArchID(String collectionName, ArrayList<String> slots, ArrayList<String> conditions, 
                            ArrayList<String> values, ArrayList<String> valueTypes){
        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        
        Document filter = new Document();
        
        for(int i=0;i<slots.size();i++){
            String slotName = slots.get(i);
            String cond = conditions.get(i);
            String val = values.get(i);
            String valType = valueTypes.get(i);
            if(cond.equals("eq")){
                if(valType.equals("String")){
                    filter.append(slotName,val);
                }
                else if(valType.equals("Double")){
                    filter.append(slotName, Double.parseDouble(val));
                }
            }
            else if(cond.equals("gt") || cond.equals("gte") || cond.equals("lt") || cond.equals("lte") || cond.equals("ne")){
                if(valType.equals("String")){
                    filter.append(slotName, new Document("$"+cond,val));
                }
                else if(valType.equals("Double")){
                    filter.append(slotName, new Document("$"+cond,Double.parseDouble(val)));
                }
            }else if(cond.equals("regex")){
                filter.append(slotName, new Document("$regex",val));
            }            
        }
        FindIterable found = col.find(filter);
        found.projection(fields(
                    include("ArchID")
                ));
        MongoCursor iter = found.iterator();
        ArrayList<Integer> archIDs = new ArrayList<>();
        while(iter.hasNext()){
            Document doc = (Document) iter.next();
            int id = doc.getDouble("ArchID").intValue();
            archIDs.add(id);
        }
        return archIDs;
    }
    
    public ArrayList<Integer> getAllArchIDs(String collectionName){
        
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);
        FindIterable found = col.find();
        found.projection(fields(
                    include("ArchID")
                ));
        MongoCursor iter = found.iterator();
        ArrayList<Integer> archIDs = new ArrayList<>();
        while(iter.hasNext()){
            Document doc = (Document) iter.next();
            int id = doc.getDouble("ArchID").intValue();
            archIDs.add(id);
        }
        return archIDs;
    }

}