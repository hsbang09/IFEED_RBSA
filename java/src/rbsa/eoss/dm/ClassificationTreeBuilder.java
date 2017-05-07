//package ifeed_dm;
//
//import java.util.ArrayList;
////import java.util.HashMap;
//
//public class ClassificationTreeBuilder{
//	
//	private int[][] inputData;
//	private int[] inputLabels;
//	private int currentNodeID;
//	private ArrayList<TreeNode> tree;
//	private ArrayList<TreeNode> uncheckedNodes; // test nodes whose children have not been added yet
//	private ArrayList<DrivingFeature> drivingFeatures;
//	private int counter=0;
//	
//	
//	public ClassificationTreeBuilder(int[][] inputData, int[] labels, ArrayList<DrivingFeature> drivingFeatures){
//		this.inputData = inputData;
//		tree = new ArrayList<>();
//		this.inputLabels=labels;
//		currentNodeID = 0;
//		uncheckedNodes = new ArrayList<>();
//		this.drivingFeatures=drivingFeatures;
//	}
//	
//	public void buildTree(){
//		TreeNode root = new TreeNode(currentNodeID,-1,inputData,inputLabels); 
//		currentNodeID++;
//		uncheckedNodes.add(root);
//		int cnt = 0;
//		while(true){
//			TreeNode node = uncheckedNodes.get(0);
//			
//			addBranch(node);
//			uncheckedNodes.remove(0);
//			
//			cnt++;
//			if(uncheckedNodes.size()==0){
//				break;
//			}else if(cnt > 120){
//				for(TreeNode n:uncheckedNodes){
//					n.setLeaf();
//					tree.add(n);
//				}
//				break;
//			}
//		}
//	}
//	
//
//	
//	public void addBranch(TreeNode parent){
//		
//		if(parent.isLeaf()){
//			tree.add(parent);
//			return;
//		}
//		int[][] dat = parent.getData();
//		int[] lab = parent.getLabels();
//		int testFeatureID = parent.setTestFeature();
//		int[][] child1Data = this.splitData(dat, testFeatureID, 1);
//		int[][] child2Data = this.splitData(dat, testFeatureID, 0);
//		int[] child1Label = this.splitLabelData(dat,lab, testFeatureID, 1);
//		int[] child2Label = this.splitLabelData(dat,lab, testFeatureID, 0);
//		
//		
//		if(child1Label.length==0 || child2Label.length==0){
//			parent.setLeaf();
//			tree.add(parent);
//			return;
//		}
//		
//		TreeNode child1 = new TreeNode(this.currentNodeID,parent.getID(),child1Data,child1Label);
//		currentNodeID++;
//		TreeNode child2 = new TreeNode(this.currentNodeID,parent.getID(),child2Data,child2Label);
//		currentNodeID++;
//
//		// pruning criteria. needs to be refined later
//		if(child1Data.length < 10){
//			child1.setLeaf();  // child is a leaf node
//		} 
//		
//		// pruning criteria. needs to be refined later
//		if(child2Data.length < 10){  
//			child2.setLeaf(); // child is a leaf node
//		} 
//		
//		parent.setChild1(child1.getID());
//		parent.setChild2(child2.getID());
//		tree.add(parent);
//
//		this.uncheckedNodes.add(child1);
//		this.uncheckedNodes.add(child2);
//	}
//
//	
//	
//
//
//	public int[][] splitData(int[][] data, int featureIndex, int featureVal){
//		ArrayList<Integer> matched = new ArrayList<>();
//		for(int i=0;i<data.length;i++){
//			if(data[i][featureIndex]==featureVal){
//				matched.add(i);
//			}
//		}
//		int[][] reduced_data = new int[matched.size()][data[0].length];
//		for(int i=0;i<matched.size();i++){
//			reduced_data[i] = data[matched.get(i)];
//		}
//		return reduced_data;		
//	}
//	
//	public int[] splitLabelData(int[][] data,int[] label ,int featureIndex, int featureVal){
//		ArrayList<Integer> matched = new ArrayList<>();
//		for(int i=0;i<data.length;i++){
//			if(data[i][featureIndex]==featureVal){
//				matched.add(i);
//			}
//		}
//		int[] reduced_label = new int[matched.size()];
//		for(int i=0;i<matched.size();i++){
//			reduced_label[i] = label[matched.get(i)];
//		}
//		return reduced_label;		
//	}
//	
//	
//
//	
//	public void setDrivingFeatures(ArrayList<DrivingFeature> drivingFeatures){
//		this.drivingFeatures = drivingFeatures;
//	}
//
//
//	
//	
//
//	
//	
//	
//	public String printTree_json(){
//		
//		try{
////		{"id":id,"test":test,"numDat":numDat,"id_c1":id_c1,"id_c2":id_c2},
////		{"id":id,"test":"root","numDat":numDat,"id_c1":id_c1,"id_c2":id_c2},
////		{"id":id,"test":"leaf","numDat":numDat,"num_b":num_b,"num_nb":num_nb},
//		
////      [id,"root",numDat,id_c1,id_c2]		
////		[id,test,numDat,id_c1,id_c2]
////		[id,"leaf",numDat,num_b,num_nb]
//		
//		String out = "";
//		
//		for(int i=0;i<tree.size();i++){
//			TreeNode thisNode = tree.get(i);
//			
//			int num_behavioral = thisNode.getNumBehavioral();
//			int num_nonbehavioral = thisNode.getNumNonBehavioral();
//			
//
//			if(!thisNode.isLeaf()){ // Test Node
//				int testFeature = thisNode.getTestFeature();
//				if(testFeature>=drivingFeatures.size()){
//					System.out.println("dfsize: " + drivingFeatures.size() + " testFeature: " + testFeature);
//				}
//				DrivingFeature df = drivingFeatures.get(testFeature);
//				if(thisNode.isRoot()){
//					out = out + "[{\"nodeID\":" + thisNode.getID() + ",\"name\":\""+ df.getExpression() + "\",\"numDat\":" + thisNode.getData().length 
//							+ ",\"id_c1\":" + thisNode.getChild1() +",\"id_c2\":" + thisNode.getChild2()
//							+ ",\"num_b\":" + num_behavioral +",\"num_nb\":" + num_nonbehavioral + ",\"x\":0,\"y\":0}";
//				}else{
//					out = out + ",{\"nodeID\":" + thisNode.getID() + ",\"name\":\""+ df.getExpression() +"\",\"numDat\":" + thisNode.getData().length 
//							+ ",\"id_c1\":" + thisNode.getChild1() +",\"id_c2\":" + thisNode.getChild2()
//							+ ",\"num_b\":" + num_behavioral +",\"num_nb\":" + num_nonbehavioral + ",\"x\":0,\"y\":0}";
//				}
//			} 
//			else{  // Leaf Node
//				out = out + ",{\"nodeID\":" + thisNode.getID() + ",\"name\":\"leaf\",\"numDat\":" + thisNode.getData().length 
//						+ ",\"num_b\":" + num_behavioral +",\"num_nb\":" + num_nonbehavioral + ",\"x\":0,\"y\":0}";
//
//			}
//		}
//		//System.out.println(out);
//		return out + "]";
//		}catch(Exception e){
//			e.printStackTrace();
//			return "";
//		}
//	}
//	
//	
//	public static double log2(double n)
//	{
//	    return (Math.log(n) / Math.log(2));
//	}	
//	
//	
//	
//	public class TreeNode{
//	
//		private int id;
//		private int[][] data;
//		private int[] labels;
//		private int testFeature;
//		private int parentID;
//		private int child1ID;
//		private int child2ID;
//		private boolean leaf = false;
//		private int numData;
//		private int numFeat;
//		private ArrayList<Integer> has_feature_b;
//		private ArrayList<Integer> has_feature_nb;
//		private ArrayList<Integer> no_feature_b;
//		private ArrayList<Integer> no_feature_nb;
//		private int behavioral;
//		private int non_behavioral;
//		
//		public TreeNode(int id,int parent,int[][] data,int[] labels){
//			this.id=id;
//			this.data=data;
//			this.parentID=parent;
//			this.labels=labels;
//			this.numData = data.length;
//			this.numFeat = data[0].length;
//			
//			//System.out.println("id:"+id+", behavioral:"+getNumOfValues(labels,1) +", non:"+getNumOfValues(labels,0));
//			countData();
//		}
//	
//		private void countData(){
//			has_feature_b = new ArrayList<>();
//			has_feature_nb = new ArrayList<>();
//			no_feature_b = new ArrayList<>();
//			no_feature_nb = new ArrayList<>();
//						
//			for (int j=0;j<numFeat;j++){
//				int cnt_f_b = 0;
//				int cnt_f_nb = 0;
//				int cnt_nf_b = 0;
//				int cnt_nf_nb = 0;
//				for(int i=0;i<numData;i++){
//					if(labels[i]==1){ //behavioral
//						if(data[i][j]==1){cnt_f_b++;}
//						else{cnt_nf_b++;}
//					}else{  // nonbehavioral
//						if(data[i][j]==1){cnt_f_nb++;}
//						else{cnt_nf_nb++;}
//					}
//				}
//				has_feature_b.add(cnt_f_b);
//				has_feature_nb.add(cnt_f_nb);
//				no_feature_b.add(cnt_nf_b);
//				no_feature_nb.add(cnt_nf_nb);
//			}
//			
//			behavioral = 0;
//			non_behavioral = 0;
//			for(int i=0;i<numData;i++){
//				if(labels[i]==1){behavioral++;}
//				else{non_behavioral++;}
//			}
//		}		
//		
//		public int setTestFeature(){
//			// Save the index of the feature that maximizes infoGain
//			int saveInd = 0; 
//			double maxGain = -99999999;
//			for(int i=0;i<numFeat;i++){
//				double gain = compute_information_gain(i);
//				if(gain >= maxGain){
//					maxGain = gain;
//					saveInd = i;
//				}
//			}
//			this.testFeature=saveInd;
//			return saveInd;
//		}
//		
//		private double compute_information_gain(int featureIndex){
//			double num_has_feature = (double) (has_feature_b.get(featureIndex) + has_feature_nb.get(featureIndex));
//			double num_no_feature = (double) (no_feature_b.get(featureIndex) + no_feature_nb.get(featureIndex));
//			double total = num_has_feature + num_no_feature;
//			//  The gain is not normalized here, because we assumed that all the features are binary.
//			//  Normalization is necessary otherwise.
//			return entropy() - (num_has_feature/total*entropy(featureIndex,1) + num_no_feature/total*entropy(featureIndex,0));
//		}
//	
//		private double entropy(){
//			double info = 0;
//			for(int i=0;i<2;i++){
//				info = info - freq(i)*log2(freq(i));
//			}
//			return info;
//		}
//		
//		private double entropy(int featureIndex,int featureVal){
//			double info = 0;
//			for(int i=0;i<2;i++){
//				info = info - freq(i,featureIndex,featureVal)*log2(freq(i,featureIndex,featureVal));
//			}
//			return info;
//		}
//		
//		private double freq(int label){
//			if(label==1){
//				return (double)((double) behavioral)/((double) numData);
//			}else{
//				return (double)((double) non_behavioral)/((double) numData);
//			}
//		}
//		
//		private double freq(int classLabel, int featureIndex, int featureVal){
//			if(classLabel==1){
//				if(featureVal==1){
//					return (double)((double)has_feature_b.get(featureIndex))/((double) countFeature(featureIndex,featureVal));
//				}else{
//					return (double)((double)no_feature_b.get(featureIndex))/((double) countFeature(featureIndex,featureVal));
//				}
//			}else{
//				if(featureVal==1){
//					return (double)((double)has_feature_nb.get(featureIndex))/((double) countFeature(featureIndex,featureVal));
//				}else{
//					return (double)((double)no_feature_nb.get(featureIndex))/((double) countFeature(featureIndex,featureVal));
//				}
//			}
//		}
//		
//		public int countFeature (int featureIndex, int featureVal){
//			if(featureVal==1){
//				return has_feature_b.get(featureIndex) + has_feature_nb.get(featureIndex);
//			}else{
//				return no_feature_b.get(featureIndex) + no_feature_nb.get(featureIndex);
//			}
//		}
//		
//		
//		public void setChild1(int id){
//			this.child1ID = id;
//		}
//		public void setChild2(int id){
//			this.child2ID = id;
//		}
//		public int getChild1(){
//			return child1ID;
//		}
//		public int getChild2(){
//			return child2ID;
//		}
//		public int getID(){
//			return this.id;
//		}
//		public int getParentID(){
//			return parentID;
//		}
//		public int[][] getData(){
//			return data;
//		}
//		public int[] getLabels(){
//			return labels;
//		}
//		public void setLeaf(){
//			this.leaf = true;
//		}
//		public boolean isLeaf(){
//			return leaf;
//		}
//		public boolean isRoot(){
//			return parentID==-1;
//		}
//		public int getTestFeature(){
//			return this.testFeature;
//		}
//		public int getNumBehavioral(){
//			return this.behavioral;
//		}
//		public int getNumNonBehavioral(){
//			return this.non_behavioral;
//		}
//		
//		
//	
//		public int getNumOfValues(int[] arr,int val){
//			int cnt = 0;
//			for(int i=0;i<arr.length;i++){
//				if(arr[i]==val){
//					cnt++;
//				}
//			}
//			return cnt;
//		}
//			
//		
//	}
//	
//	
//}
//
//
//
