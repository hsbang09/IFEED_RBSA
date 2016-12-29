/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;


import java.util.*;
import rbsa.eoss.local.Params;
/**
 *
 * @author Bang
 */
public class Apriori {
    
    private int numItems;
    private int numTransactions;
    private ArrayList<boolean[]> transactions;
    private boolean[] classLabels;
    
    private double supp;
    private double conf;
    private double lift;
    
    private ArrayList<CAR> frontier;
    private ArrayList<DrivingFeature> CARs;
    
    private int selectedSize;
    
    
    String[] instrList = Params.instrument_list;
    String[] orbitList = Params.orbit_list;
    String[] newOrbitList = {"1000","2000","3000","4000","5000"};
    String[] newInstrList = {"A","B","C","D","E","F","G","H","I","J","K","L"};
    
    
    
    public Apriori(ArrayList<int[][]> selected_input, ArrayList<int[][]> pop_input, double supp, double conf, double lift){

    	ArrayList<boolean[]> selected = intMatrix2BoolArray(selected_input);
    	ArrayList<boolean[]> pop = intMatrix2BoolArray(pop_input);
    	
    	
        transactions = pop;
        selectedSize = selected.size();
        classLabels = new boolean[pop.size()];
        numItems = transactions.get(0).length;
        numTransactions = transactions.size();
        
//        System.out.println("numItems: " + numItems);
//        System.out.println("numTrans: " + numTransactions);
//        System.out.println("selected: " + selectedSize);
        int tmpcnt=0;
        
        for(int i=0;i<pop.size();i++){
            boolean[] thisTrans = pop.get(i);
            boolean foundMatch = false;
            
            for(boolean[] selected1:selected){
                boolean match = true;
                for(int j=0;j<thisTrans.length;j++){
                    if(thisTrans[j]!=selected1[j]){
                        match = false;
                        break;
                    }
                }
                if(match){
                    foundMatch = true;
                    break;
                }
            }
            classLabels[i] = foundMatch;
            if(foundMatch){tmpcnt++;}
        }
        
//        System.out.println("matchingCount: " + tmpcnt);
        
        this.supp = supp;
        this.conf = conf;
        this.lift = lift;
        CARs = new ArrayList<>();
    }
    
//    public Apriori(ArrayList<boolean[]> pop, ArrayList<boolean[]> selected, double supp, double conf, double lift){
//
//        transactions = pop;
//        selectedSize = selected.size();
//        classLabels = new boolean[pop.size()];
//        numItems = transactions.get(0).length;
//        numTransactions = transactions.size();
//        
//        for(int i=0;i<pop.size();i++){
//            boolean[] thisTrans = pop.get(i);
//            boolean foundMatch = false;
//            
//            for(boolean[] selected1:selected){
//                boolean match = true;
//                for(int j=0;j<thisTrans.length;j++){
//                    if(thisTrans[j]!=selected1[j]){
//                        match = false;
//                        break;
//                    }
//                }
//                if(match){
//                    foundMatch = true;
//                    break;
//                }
//            }
//            classLabels[i] = foundMatch;
//        }
//        
//        this.supp = supp;
//        this.conf = conf;
//        this.lift = lift;
//        CARs = new ArrayList<>();
//    }
    
    
    public void runApriori(){
        
        ArrayList<CAR> CAR_firstOrder = createCAROfSize1();
        frontier = CAR_firstOrder;
        
        
        while(frontier.size() > 0){
            ArrayList<CAR> tmp = new ArrayList<>();
            for(CAR thisCAR:frontier){
                
                thisCAR = calculateFrequency(thisCAR);
                double thisSupp = getSupp(thisCAR);
                double thisConf = getConf(thisCAR);
                double thisConf2 = getConf2(thisCAR);
                double thisLift = getLift(thisCAR);
                if(thisSupp>=supp && thisConf>=conf && thisConf2>=conf && thisLift>=lift){
                    
                    int[] cond = thisCAR.getCondset();
                    String name = "";
                    for(int j=0;j<cond.length;j++){
                        String instrument = newInstrList[cond[j]%instrList.length];
                        String orbit = newOrbitList[cond[j]/instrList.length];
                        name = name + instrument + " in " + orbit + " ";
                    }
                    if(thisCAR.getClassLabel()==true){
                        name = name + "-> " + "good design";
                        double[] metrics = {thisSupp, thisLift, thisConf, thisConf2};
                        CARs.add(new DrivingFeature(name,"good",metrics));
                    } else{
                        name = name + "-> " + "bad design";
                        double[] metrics = {thisSupp, thisLift, thisConf, thisConf2};
                        CARs.add(new DrivingFeature(name,"bad",metrics));
                    }
                    tmp.add(thisCAR);
                }
            }
            System.out.println("length: " + frontier.get(0).getCondset().length + " number: "+ tmp.size() );
            
            if(tmp.size() > 0){
                frontier = createNewCandidatesFromPrevious(tmp);
            } else{
                frontier = new ArrayList<>();
            }
        }
//        printCARs();
    }
    
    
//    public ArrayList<String> printCARs(){
//        ArrayList<String> outputStrings = new ArrayList<>();
//        
//        String[] instrList = Params.instrument_list;
//        String[] orbitList = Params.orbit_list;
//        String[] newOrbitList = {"1000","2000","3000","4000","5000"};
//        String[] newInstrList = {"A","B","C","D","E","F","G","H","I","J","K","L"};
//        
//        for(int i=0;i<CARs.size();i++){
//            
//            
//                    .text("Lift: " + thisDF.lift.toFixed(5) + 
//                    " " + relabelDrivingFeatureName(thisDF.name) + " Supp: " + thisDF.supp.toFixed(5) + 
//                    " Conf(feature->selection): " + thisDF.conf.toFixed(5) + 
//                    " Conf(selection->feature): " + thisDF.conf2.toFixed(5));
//            
//            
//            
//            DrivingFeature thisCAR = CARs.get(i);
//            outputStrings.add(thisCAR.getName() + " Supp:" + thisCAR.gets);
//            
//            
//            System.out.println(out + "-> " + thisCAR.getClassLabel());
//            outputStrings.add(out + "-> " + thisCAR.getClassLabel());
//        }
//        return outputStrings;
//    }
    
    
    public ArrayList<DrivingFeature> getCARs(){
        return CARs;
    }
    
    
    private ArrayList<CAR> createNewCandidatesFromPrevious(ArrayList<CAR> prev){
        
        ArrayList<CAR> newCandidates = new ArrayList<>();
        
        for(int i=0;i<prev.size();i++){
            for(int j=i+1;j<prev.size();j++){
                
                int[] cond1 = prev.get(i).getCondset();
                int[] cond2 = prev.get(j).getCondset();
                boolean cond1cl = classLabels[i];
                int currentSize = cond1.length;
                
                int[] newCond = new int[currentSize+1];
                for(int s=0;s<newCond.length-1;s++){
                    newCond[s] = cond1[s];
                }

                for(int s2=0;s2<cond2.length;s2++){   // check cond2[s2] is present in cond1
                    boolean found = false;
                    for(int s1=0;s1<cond1.length;s1++){
                        if(cond1[s1]==cond2[s2]){
                            found=true;
                            break;
                        }
                    }
                    if(!found){ // not found
                        newCond[newCond.length-1] = cond2[s2];  // create new cond with length k+1
                        Arrays.sort(newCond);
                        
                        boolean allSubsetsPresent = true; 
                        for(int k=0;k<newCond.length;k++){
                            
                            int[] tmpCond = combination(newCond,k);
                            CAR tmpCAR = new CAR(tmpCond,cond1cl);
                            
                            if(!condPresentInList(tmpCAR,prev)){ // must be present
                                allSubsetsPresent = false;
                                break;
                            }
                        }
                        if(allSubsetsPresent && !condPresentInList(new CAR(newCond,cond1cl),newCandidates)){
                        	newCandidates.add(new CAR(newCond,cond1cl));
                        }
                    }
                }
            }
        }
        return newCandidates;
    }
    
    private boolean condPresentInList(CAR thisCAR, ArrayList<CAR> list){
        
        for(CAR tmpCAR:list){
            int[] cond1 = thisCAR.getCondset();
            int[] cond2 = tmpCAR.getCondset();
            boolean cond1cl = thisCAR.getClassLabel();
            boolean cond2cl = tmpCAR.getClassLabel();
            if(compareConds(cond1,cond2) && cond1cl==cond2cl){
                return true;
            }
        }
        return false;
    }

    private boolean compareConds(int[] cond1, int[] cond2){
        boolean match = true;
        for(int i=0;i<cond1.length;i++){
            if(cond1[i]!=cond2[i]){
                match = false;
                break;
            }
        }
        return match;
    }

    private int[] combination(int[] X, int index){
        int[] out = new int[X.length-1];
        int cnt=0;
        for(int i=0;i<X.length-1;i++){
            if(i!=index){
                out[cnt] = X[i];
                cnt++;
            }
        }
        return out;
    }    
    

    
    private double getSupp(CAR thisCAR){
//        thisCAR = calculateFrequency(thisCAR);
        double carsupp = (double) (((double)thisCAR.getRulecnt())/((double)numTransactions));
        return carsupp;
    }
    
    private double getConf(CAR thisCAR){
//        thisCAR = calculateFrequency(thisCAR);
        double carconf =  (double) (((double)thisCAR.getRulecnt())/((double)thisCAR.getCondcnt()));
        return carconf;
    }
    private double getConf2(CAR thisCAR){
//        thisCAR = calculateFrequency(thisCAR);
        double carconf2 =  (double) (((double)thisCAR.getRulecnt())/((double) selectedSize));
        return carconf2;
    }
    
    private double getLift(CAR thisCAR){
//        thisCAR = calculateFrequency(thisCAR);
        double carconf =  (double) (((double)thisCAR.getRulecnt())/((double)thisCAR.getCondcnt()));
        return (double) carconf/selectedSize;
    }
    
    
    
    private ArrayList<CAR> createCAROfSize1(){
        ArrayList<CAR> candidates = new ArrayList<>();
        for(int i=0;i<numItems;i++){
            int[] tmp = new int[1];
            tmp[0] = i;
            CAR thisCAR1 = new CAR(tmp,true);
            CAR thisCAR2 = new CAR(tmp,false);

            candidates.add(thisCAR1);
            candidates.add(thisCAR2);
        }
        return candidates;
    }
    
    public CAR calculateFrequency(CAR inputCAR){
        CAR thisCAR = inputCAR;
        thisCAR.setCntZero();
        
//        if(inputCAR.getCondcnt()!=0 || inputCAR.getRulecnt()!=0){
//            return inputCAR;
//        }
        
        for(int i=0;i<numTransactions;i++){
            boolean match = true;
            int[] cond = thisCAR.getCondset();
            boolean[] thisTrans = transactions.get(i);
            
            for(int j=0;j<cond.length;j++){
                if(!thisTrans[cond[j]]){
                    match = false;
                    break;
                }
            }
            
            if(match){
                thisCAR.condcntUp();
                if(classLabels[i]==thisCAR.getClassLabel()){
                    thisCAR.rulecntUp();
                }
            }
        }
        return thisCAR;
    }
   
    public class CAR{
        
        private int[] condset;
        private boolean classLabel;
        
        private int condcnt;
        private int rulecnt;
        
        public CAR(int[] condset){
            this.condset=condset;
            condcnt=0;
            rulecnt=0;
        }
        public CAR(int[] condset, boolean classLabel){
            this.condset=condset;
            this.classLabel=classLabel;
            condcnt=0;
            rulecnt=0;
        }
        public void setCntZero(){
        	condcnt=0;
        	rulecnt=0;
        }
        public void condcntUp(){
            condcnt++;
        }
        public void rulecntUp(){
            rulecnt++;
        }
        public int getCondcnt(){
            return condcnt;
        }
        public int getRulecnt(){
            return rulecnt;
        }
        public int[] getCondset(){
            return condset;
        }
        public boolean getClassLabel(){
            return classLabel;
        }
        
    }
    
    
    public ArrayList<boolean[]> intMatrix2BoolArray(ArrayList<int[][]> input){
    	
    	ArrayList<boolean[]> boolArray = new ArrayList<>();
    	int len = input.get(0).length * input.get(0)[0].length;
    	
    	for(int i=0;i<input.size();i++){
    		
    		boolean[] tmpArray = new boolean[len];
    		int cnt=0;
    		for(int j=0;j<input.get(i).length;j++){
    			for(int k=0;k<input.get(i)[j].length;k++){
    				if(input.get(i)[j][k]==1){
    					tmpArray[cnt]=true;
    				}else{
    					tmpArray[cnt]=false;
    				}
    				cnt++;
    			}
    		}
    		
    		
    		boolArray.add(tmpArray);
    	}

    	return boolArray;
    }
   
    
}