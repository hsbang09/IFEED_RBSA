package rbsa.eoss;

import java.util.ArrayList;
//import java.util.HashMap;


public class ClassificationTreeBuilder { // assumes binary features

	private int[][] mat;
	private int nodeID;
	private ArrayList<node> tree;
//	private HashMap<Integer, node> tmpNodes; 
	private ArrayList<node> tmpNodes; // test nodes whose children have not been added yet
	private ArrayList<DrivingFeature> dfs;

	public ClassificationTreeBuilder(int[][] inputMat){
		this.mat = inputMat;
		tree = new ArrayList<>();
		nodeID = 0;
		tmpNodes = new ArrayList<>();
	}
	
	public void buildTree(){
		node root = new node(nodeID,-1,mat); nodeID++;
		tmpNodes.add(root);
		int cnt = 0;
		while(true){
			
			node thisNode = tmpNodes.get(0);
			addBranch(thisNode);
			tmpNodes.remove(0);
			
			if(tmpNodes.size()==0){
				break;
			}
			cnt++;
			if(cnt>120){
				break;
			}
		}
	}
	
	public void addBranch(node parent){
		
		if(parent.isLeaf()){
			tree.add(parent);
//			System.out.println("Leaf length:" + parent.getData().length + " myID: " + 
//								parent.getID() + " parentID: " + parent.getParentID());
			return;
		}
		int[][] inputDat = parent.getData();
		subMat m = new subMat(inputDat);
		int test = m.getTest();
		int[][] child1_dat = createSubMatrix(inputDat,m.splitData_has_feat(test));
		int[][] child2_dat = createSubMatrix(inputDat,m.splitData_no_feat(test));
		
		node c1;
		node c2;
		
//		subMat c1_mat = new subMat(child1_dat);
//		subMat c2_mat = new subMat(child2_dat);
		
//		if(child1_dat.length < 10 || (child1_dat.length < 50 && c1_mat.getClassificationAccuracy() > 0.9 )){  // pruning criteria. needs to be refined later
		if(child1_dat.length < 10){  // pruning criteria. needs to be refined later
			c1 = new node(nodeID,parent.getID(),child1_dat,true); nodeID++; // child is a leaf node
		} else{
			c1 = new node(nodeID,parent.getID(),child1_dat); nodeID++;
		}
//		if(child2_dat.length < 10 || (child2_dat.length < 50 && c2_mat.getClassificationAccuracy() > 0.9 )){  // pruning criteria. needs to be refined later
		if(child2_dat.length < 10){  // pruning criteria. needs to be refined later
			c2 = new node(nodeID,parent.getID(),child2_dat,true); nodeID++; // child is a leaf node
		} else{
			c2 = new node(nodeID,parent.getID(),child2_dat); nodeID++;
		}
		
		if(c1.getData().length==0 || c2.getData().length==0){
			node pa = parent;
			pa.setLeaf();
			tree.add(pa);
			return;
		}

		node pa = parent;
		pa.setTest(test);
		pa.setChild1(c1.getID());
		pa.setChild2(c2.getID());
		tree.add(pa);
//		System.out.println("Normal length:" + pa.getData().length + " myID: " + 
//				pa.getID() + " parentID: " + pa.getParentID());
		tmpNodes.add(c1);
		tmpNodes.add(c2);
	}




	public class node{
		private int id;
		private int test;
		private int[][] dat;
		private int parentID;
		private int child1ID;
		private int child2ID;
		private boolean leaf;
//		private int num_behavioral;
//		private int num_nonbehavioral;

		public node(int id,int parent, int[][] dat){
			this.id=id;
			this.dat = dat;
			this.parentID = parent;
			this.leaf = false;
		}
		public node(int id, int parent, int[][] dat, boolean leaf){
			this.id = id;
			this.test = -1;
			this.dat = dat;
			this.parentID = parent;
			this.leaf = leaf;
		}
		
		public void setTest(int test){
			this.test = test;
		}
		
		public void setChild1(int id){
			this.child1ID = id;
		}
		public void setChild2(int id){
			this.child2ID = id;
		}
		public int getChild1(){
			return child1ID;
		}
		public int getChild2(){
			return child2ID;
		}
		public int getID(){
			return this.id;
		}
		public int getParentID(){
			return parentID;
		}
		public int[][] getData(){
			return dat;
		}
		public void setLeaf(){
			this.leaf = true;
			this.test = -1;
		}
		
		public boolean isLeaf(){
			return leaf;
		}
		public boolean isRoot(){
			return parentID==-1;
		}
		public int getTest(){
			return test;
		}
		
	}
	
	public class subMat{
		
		private int[][] dataMat;
		private int numDat;
		private int numFeat;
		private int classIndex; // class label index
		
		private int[] has_feature_b; // behavioral group
		private int[] has_feature_nb; // nonbehavioral group
		private int[] no_feature_b;
		private int[] no_feature_nb;

		public subMat(int[][] mat){ // [numData][numFeature+1], the last feature is the class label
			this.dataMat = mat;
			this.numDat = mat.length;
			this.numFeat = mat[0].length;
			this.classIndex = numFeat-1;
			analyzeDataMat();
		}
		public subMat(int[][] originalMat,ArrayList<String> subset){ // [numData][numFeature+1], the last feature is the class label
			this.dataMat = createSubMatrix(originalMat,subset);
			this.numDat = subset.size();
			this.numFeat = originalMat[0].length;
			this.classIndex = numFeat-1;
			analyzeDataMat();
		}
		
		
		public int getTest(){
			int saveInd = 0;
			double maxGain = -99999999;
			
			for(int i=0;i<numFeat-1;i++){
				if(infoGain(i) >= maxGain){
					maxGain = infoGain(1);
					saveInd = i;
				}
			}
			return saveInd;
		}
		public ArrayList<String> splitData_has_feat(int test){
			ArrayList<String> has_feature = new ArrayList<>();
			for(int i=0;i<numDat;i++){
				if(dataMat[i][test]==1){
					has_feature.add(Integer.toString(i));
				}
			}
			return has_feature;
		}
		public ArrayList<String> splitData_no_feat(int test){
			ArrayList<String> no_feature = new ArrayList<>();
			for(int i=0;i<numDat;i++){
				if(dataMat[i][test]==0){
					no_feature.add(Integer.toString(i));
				}
			}
			return no_feature;
		}
		
		public double getClassificationAccuracy(){
			
			int num_b = has_feature_b[classIndex];
			int num_nb = no_feature_nb[classIndex];
			int total = num_b + num_nb;
			double acc;
			
			if(num_b > num_nb){ // classified as behavioral
				acc = (double)  ((double)num_b)/((double)total);
			} else{  // classified as non-behavioral
				acc = (double)  ((double)num_nb)/((double)total);
			}
			return acc;
		}

		
		private void analyzeDataMat(){
			has_feature_b = new int[numFeat];
			has_feature_nb = new int[numFeat];
			no_feature_b = new int[numFeat];
			no_feature_nb = new int[numFeat];
			
			for (int j=0;j<numFeat;j++){
				int cnt_f_b = 0;
				int cnt_f_nb = 0;
				int cnt_nf_b = 0;
				int cnt_nf_nb = 0;
				for(int i=0;i<numDat;i++){
					if(dataMat[i][classIndex]==1){ //behavioral
						if(dataMat[i][j]==1){cnt_f_b++;}
						else{cnt_nf_b++;}
					}else{                         // nonbehavioral
						if(dataMat[i][j]==1){cnt_f_nb++;}
						else{cnt_nf_nb++;}
					}
				}
				has_feature_b[j] = cnt_f_b;
				has_feature_nb[j] = cnt_f_nb;
				no_feature_b[j] = cnt_nf_b;
				no_feature_nb[j] = cnt_nf_nb;
			}
		}

		
		public int numOfData_withFeature(int featureIndex, int featureVal){
			if(featureVal==1){
				return has_feature_b[featureIndex] + has_feature_nb[featureIndex];
			}else{
				return no_feature_b[featureIndex] + no_feature_nb[featureIndex];
			}
		}

		
		private double freq(int classLabel){
			if(classLabel==1){
				return (double)((double)has_feature_b[classIndex])/((double) numDat);
			}else{
				return (double)((double)no_feature_nb[classIndex])/((double) numDat);
			}
		}
		
		private double freq(int classLabel, int featureIndex, int featureVal){
			if(classLabel==1){
				if(featureVal==1){
					return (double)((double)has_feature_b[featureIndex])/((double) numOfData_withFeature(featureIndex,featureVal));
				}else{
					return (double)((double)no_feature_b[featureIndex])/((double) numOfData_withFeature(featureIndex,featureVal));
				}
			}else{
				if(featureVal==1){
					return (double)((double)has_feature_nb[featureIndex])/((double) numOfData_withFeature(featureIndex,featureVal));
				}else{
					return (double)((double)no_feature_nb[featureIndex])/((double) numOfData_withFeature(featureIndex,featureVal));
				}
			}
		}

		private double entropy(){
			double tmp = freq(0)*log2(freq(0))
					+ freq(1)*log2(freq(1));
			return -tmp;
		}
		
		private double entropy(int featureIndex, int featureVal){
			double tmp = freq(0,featureIndex,featureVal)*log2(freq(0,featureIndex,featureVal))
					+ freq(1,featureIndex,featureVal)*log2(freq(1,featureIndex,featureVal));
			return -tmp;
		}

		
		private double infoGain(int featureIndex){
			double num_has_feature = (double) (has_feature_b[featureIndex] + has_feature_nb[featureIndex]);
			double num_no_feature = (double) (no_feature_b[featureIndex] + no_feature_nb[featureIndex]);
			double total = num_has_feature + num_no_feature;
			
			return entropy() - (num_has_feature/total*entropy(featureIndex,1) + num_no_feature/total*entropy(featureIndex,0));
		
			//  The gain is not normalized here, because we assumed that all the features are binary.
			//  Normalization is necessary otherwise.
		
		}
			
		
		
	}
	
	
	public int[][] createSubMatrix(int[][] originalMat, ArrayList<String> subset){
		int[][] newMat = new int[subset.size()][originalMat[0].length];
		for(int i=0;i<subset.size();i++){
			String s = subset.get(i);
			int ind = Integer.parseInt(s);
			newMat[i] = originalMat[ind];
		}
		return newMat;
	}
	
	public static double log2(double n)
	{
	    return (Math.log(n) / Math.log(2));
	}
	
	public void setDrivingFeatures(ArrayList<DrivingFeature> dfs){
		this.dfs = dfs;
	}
	
	
//	public String printTree(){
//		
////      [id,"root",numDat,id_c1,id_c2]		
////		[id,test,numDat,id_c1,id_c2]
////		[id,"leaf",numDat,num_b,num_nb]
//		
//		String out = "";
//		
//		for(int i=0;i<tree.size();i++){
//			node thisNode = tree.get(i);
//			if(thisNode.isRoot()){
//				out = out + "[" + thisNode.getID() + ",root," + thisNode.getData().length 
//						+ "," + thisNode.getChild1() +"," + thisNode.getChild2() + "]";
//			}else if(thisNode.isLeaf()){
//				subMat mat = new subMat(thisNode.getData());
//				int num_behavioral = mat.numOfData_withFeature(thisNode.getData()[0].length-1, 1);
//				int num_nonbehavioral = mat.numOfData_withFeature(thisNode.getData()[0].length-1,0);
//				out = out + ",[" + thisNode.getID() + ",leaf," + thisNode.getData().length 
//						+ "," + num_behavioral +"," + num_nonbehavioral + "]";
//			}else{
//				out = out + ",[" + thisNode.getID() + ","+ thisNode.getTest() +"," + thisNode.getData().length 
//						+ "," + thisNode.getChild1() +"," + thisNode.getChild2() + "]";
//			}
//		}
//		return "";
//	}
	
	public String printTree_json(){
		
//		{"id":id,"test":test,"numDat":numDat,"id_c1":id_c1,"id_c2":id_c2},
//		{"id":id,"test":"root","numDat":numDat,"id_c1":id_c1,"id_c2":id_c2},
//		{"id":id,"test":"leaf","numDat":numDat,"num_b":num_b,"num_nb":num_nb},
		
//      [id,"root",numDat,id_c1,id_c2]		
//		[id,test,numDat,id_c1,id_c2]
//		[id,"leaf",numDat,num_b,num_nb]
		
		String out = "";

		
		for(int i=0;i<tree.size();i++){
			node thisNode = tree.get(i);
			subMat mat = new subMat(thisNode.getData());
			int num_behavioral = mat.numOfData_withFeature(thisNode.getData()[0].length-1, 1);
			int num_nonbehavioral = mat.numOfData_withFeature(thisNode.getData()[0].length-1,0);
			int thisTest = thisNode.getTest();
			DrivingFeature df;
			
			if(thisTest >= 0){
				df = dfs.get(thisTest);
//				System.out.println(df.getName());
				if(thisNode.isRoot()){
					out = out + "[{\"nodeID\":" + thisNode.getID() + ",\"name\":\""+ df.getName() + "\",\"numDat\":" + thisNode.getData().length 
							+ ",\"id_c1\":" + thisNode.getChild1() +",\"id_c2\":" + thisNode.getChild2()
							+ ",\"num_b\":" + num_behavioral +",\"num_nb\":" + num_nonbehavioral + ",\"x\":0,\"y\":0}";
				}else{
					out = out + ",{\"nodeID\":" + thisNode.getID() + ",\"name\":\""+ df.getName() +"\",\"numDat\":" + thisNode.getData().length 
							+ ",\"id_c1\":" + thisNode.getChild1() +",\"id_c2\":" + thisNode.getChild2()
							+ ",\"num_b\":" + num_behavioral +",\"num_nb\":" + num_nonbehavioral + ",\"x\":0,\"y\":0}";
				}
			} 
			else{  // the node is a leaf node
				out = out + ",{\"nodeID\":" + thisNode.getID() + ",\"name\":\"leaf\",\"numDat\":" + thisNode.getData().length 
						+ ",\"num_b\":" + num_behavioral +",\"num_nb\":" + num_nonbehavioral + ",\"x\":0,\"y\":0}";
				
				if (!thisNode.isLeaf()){
					System.out.println("something's wrong");
				}
			}
		}

		return out + "]";
	}
}
