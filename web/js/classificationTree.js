/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


//function getClassificationTree(){
//	
//    highlight_basic_info_box()
//	
//	if(selection_changed == false && jsonObj_tree != null){
//		display_classificationTree(jsonObj_tree);
//		return;
//	}
//
//    var selectedArchs = d3.selectAll("[class=dot_clicked]");
//    var nonSelectedArchs = d3.selectAll("[class=dot]");
//    var numOfSelectedArchs = selectedArchs.size();
//    var numOfNonSelectedArchs = nonSelectedArchs.size();
//    var selectedBitStrings = [];
//    var nonSelectedBitStrings = [];
//    selectedBitStrings.length = 0;
//    nonSelectedBitStrings.length=0;
//    
//    
//    buttonClickCount_classificationTree += 1;
//    getClassificationTree_numOfArchs.push([{numOfSelectedArchs,numOfNonSelectedArchs}]);
//
//    
//    for (var i = 0; i < numOfSelectedArchs; i++) {
//        var tmpBitString = booleanArray2String(selectedArchs[0][i].__data__.archBitString);
//        selectedBitStrings.push(tmpBitString);
//    }
//    for (var i = 0; i < numOfNonSelectedArchs; i++) {
//        var tmpBitString = booleanArray2String(nonSelectedArchs[0][i].__data__.archBitString);
//        nonSelectedBitStrings.push(tmpBitString);
//    }
//   
//    jsonObj_tree = buildClassificationTree(selectedBitStrings,nonSelectedBitStrings,support_threshold,confidence_threshold,lift_threshold,userDefFilters);
//    display_classificationTree(jsonObj_tree);
//    selection_changed = false;
//    
//}


//function buildClassificationTree(selected,nonSelected,
//		support_threshold,confidence_threshold,lift_threshold,
//		userDefFilters){
//	
//	var output;
//    $.ajax({
//        url: "classificationTreeServlet",
//        type: "POST",
//        data: {ID: "buildClassificationTree",selected: JSON.stringify(selected),nonSelected:JSON.stringify(nonSelected),
//        	supp:support_threshold,conf:confidence_threshold,lift:lift_threshold,
//        	userDefFilters:JSON.stringify(userDefFilters)},
//        async: false,
//        success: function (data, textStatus, jqXHR)
//        {
//        	output = JSON.parse(data);
//        },
//        error: function (jqXHR, textStatus, errorThrown)
//        {alert("error");}
//    });
//    
//    return output;
//}

function buildClassificationTree(){
	
	var output;
    $.ajax({
        url: "drivingFeatureServlet",
        type: "POST",
        data: {ID: "buildClassificationTree"},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
        	output = JSON.parse(data);
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("error");}
    });
    
    return output;
}



function constructNestedTreeStructure(tree_objs){
	var root = tree_objs[0];
	addBranches(root,tree_objs);
	return root;
}
function addBranches(parent,objs){
	
	if (parent.name==="leaf"){
//		parent.children = false;
		return;
	} 
	var i = searchByNodeID(parent.id_c1,objs);
	var j = searchByNodeID(parent.id_c2,objs);
	
	if(i>=0 && j>=0){
		var c1 = objs[i];
		addBranches(c1,objs);
		c1.cond = true;
//		parent.child1 = c1;
		var c2 = objs[j];
		addBranches(c2,objs);
		c2.cond = false;
//		parent.child2 = c2;
//		parent.hasChildren = true;
		parent.children = [c1, c2];
	} else{
//		parent.hasChildren = false;
	}

}
function searchByNodeID(id,objs){
	for(var i=0;i<objs.length;i++){
		if(objs[i].nodeID===id){
			return i;
		}
	}
	return -1;
}








var i_tree = 0;
var root;
var tree;
var jsonObj_tree_nested;

var diagonal = d3.svg.diagonal()
    .projection(function(d) { return [d.y, d.x]; });

var edgeLabelLoc;

function display_classificationTree(source){
	
	// top  right bottem left
	var margin_tree = [15, 120, 20, 350],
	    width_tree = 3280 - margin_tree[1] - margin_tree[3],
	    height_tree = 500 - margin_tree[0] - margin_tree[2];

	tree = d3.layout.tree().size([height_tree, width_tree]);
	
	d3.select("[id=basicInfoBox_div]").select("[id=view4]").select("g").remove();
    var infoBox = d3.select("[id=basicInfoBox_div]").select("[id=view4]")
            .append("g");
    
	var svg_tree = infoBox.append("svg")
    			.attr("width", width_tree + margin_tree[1] + margin_tree[3])
				.attr("height", height_tree + margin_tree[0] + margin_tree[2])
				.append("svg:g")
				.attr("transform", "translate(" + margin_tree[3] + "," + margin_tree[0] + ")");
	
	jsonObj_tree_nested = constructNestedTreeStructure(source);
    root = jsonObj_tree_nested;
    root.x0 = height / 2;
    root.y0 = 0;
    edgeLabelLoc = [];
    
    function toggleAll(d) {
        if (d.children) {
            d.children.forEach(toggleAll);
            toggle_tree(d);
          }
    }
    root.children.forEach(toggleAll);
    // Initialize the display to show a few nodes.
    toggle_tree(root.children[0]);
    toggle_tree(root.children[1]);
    update(root);   
}



function update(source) {

	var duration = d3.event && d3.event.altKey ? 5000 : 500;
    // Compute the new tree layout.
    var nodes = tree.nodes(root);
    // Normalize for fixed-depth.
    nodes.forEach(function(d) { d.y = d.depth * 170; });
    
    var vis = d3.select("[id=basicInfoBox_div]").select("[id=view4]").select("svg").select("g");
    
    // Update the nodes…
    var node = vis.selectAll("g.node")
        .data(nodes, function(d) { return d.id || (d.id = ++i_tree); });
    // Enter any new nodes at the parent's previous position.
    var nodeEnter = node.enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
        .on("click", function(d) { toggle_tree(d); update(d);})
        .on("mouseover",tree_node_mouse_over)
        .on("mouseout", function (d) {
            var highlighted = d3.selectAll("[class=dot_DFhighlighted]");
            highlighted.attr("class", "dot")
                    .style("fill", function (d) {
                        if (d.status == "added") {
                            return "#188836";
                        } else if (d.status == "justAdded") {
                            return "#20FE5B";
                        } else {
                            return "#000000";
                        }
                    });     
            d3.selectAll("[class=dot_selected_DFhighlighted]")
            		.attr("class", "dot_clicked")
                    .style("fill","#0040FF");     
        });
    
    nodeEnter.append("svg:circle")
        .attr("r", 1e-6)
         .style("fill", function(d) { 
        	 if(d._children){
        		 if(d.num_nb > d.num_b){
        			 return "#343434";
        		 }else{
        			 return "#2383FF"
        		 }
        	 }else{
        		 return "#A3A3A3";
        	 }
    	 });
    
    nodeEnter.append("svg:text")
        .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
        .attr("dy", ".40em")
        .style("font-size","14px")
        .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
        .text("default")
        .style("fill-opacity", 1e-6);

    // Transition nodes to their new position.
    var nodeUpdate = node.transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

    nodeUpdate.select("circle")
        .attr("r", 9.5)
        .style("fill", function(d) { 
        	 if(d._children){
        		 if(d.num_nb > d.num_b){
        			 return "#343434";
        		 }else{
        			 return "#2383FF"
        		 }
        	 }else{
        		 return "#A3A3A3";
        	 }
    	 });
//        .style("fill", function(d) { return d._children ? "#3A3A3A" : "#A3A3A3"; });

    nodeUpdate.select("text")
            .attr("x",function(d){
                if(d.children){ return -10; }
                else{ return 10; }
            })
        .attr("text-anchor", function(d) { 
            if(d.children){ return "end"; }
            else{ return "start"; }
        })
        .text(function(d) { 
            var out="";

            if(d.children){
            	out += relabelDrivingFeatureName(d.name) + "?";
            }else { // leafNode
            	if(d.num_b >= d.num_nb){
            		// classified as selected
            		var weight = d.num_b + d.num_nb;
            		var accuracy = d.num_b / weight;
            		out += "selected (" + round_num_2_perc(accuracy) + "%) - Weight: " + weight;
            	}else{
            		// classified as not selected
            		var weight = d.num_b + d.num_nb;
            		var accuracy = d.num_nb / weight;
            		out += "not selected (" + round_num_2_perc(accuracy) + "%) - Weight: " + weight;
            	}
            }
            
            return out;
        })
        .style("font-size",23)
        .style("fill-opacity", 1);
 
    // Transition exiting nodes to the parent's new position.
    var nodeExit = node.exit().transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
        .remove();

    nodeExit.select("circle")
        .attr("r", 1e-6);

    nodeExit.select("text")
        .style("fill-opacity", 1e-6);

    // Update the links…
    var link = vis.selectAll("path.treeLink")
    	.data(tree.links(nodes), function(d) { return d.target.id; });

    
//    var path_scale = d3.scale.pow().exponent(0.8);
    var path_scale = d3.scale.pow().exponent(0.6);
    path_scale.range([2,27])
              	.domain([1,jsonObj_tree_nested.numDat]);


    // Enter any new links at the parent's previous position.
    link.enter().insert("svg:path", "g")
        .attr("class", "treeLink")
        .attr("d", function(d) {
          var o = {x: source.x0, y: source.y0};
          return diagonal({source: o, target: o});
        })
        .style("stroke",function(d){
            if(d.target.cond === true){
                return "#1CAB00";
            } else{
                return "#FF2238";
            }
        })
        .style("stroke-width",function(d){
            return path_scale(d.target.numDat);
        })
        .style("fill-opacity", 0.94)
        .transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition links to their new position.
    link.transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition exiting nodes to the parent's new position.
    link.exit().transition()
        .duration(duration)
        .attr("d", function(d) {
          var o = {x: source.x, y: source.y};
          return diagonal({source: o, target: o});
        })
        .remove();
    

    
    
    
    
    
    
    
    // Update the link labels
    var link_label = vis.selectAll(".linkLabel")
    		.data(tree.links(nodes), function(d) { return d.target.id; });
  
    link_label.enter().append("text")
		    .attr("class", "linkLabel")
		    .attr("x",function(d){
		        return (d.source.y + d.target.y)/2;
		    })
		    .attr("y", function(d){
		    	return (d.source.x + d.target.x)/2;
		    })
		    .text(function(d){
		    	if(d.target.cond){
		    		return "Yes";
		    	}else{
		    		return "No";
		    	}
		    })
			.style("fill-opacity",0)
			.transition()
			.duration(duration)
			.style("fill-opacity",1);

    // Transition links to their new position.
    link_label.transition()
        		.duration(duration)
				.attr("x",function(d){
			        return (d.source.y + d.target.y)/2;
			    })
			    .attr("y", function(d){
			    	return (d.source.x + d.target.x)/2;
			    })
			    .style("fill-opacity",1);
    
    link_label.exit().transition()
		    .duration(duration)
		    .style("fill-opacity",0)
		    .remove();



    // Stash the old positions for transition.
    nodes.forEach(function(d) {
      d.x0 = d.x;
      d.y0 = d.y;
    });
}



// Toggle children.
function toggle_tree(d) {

    if (d.children) {
      d._children = d.children;
      d.children = null;
    } else {
      d.children = d._children;
      d._children = null;
    }
}


function apply_filter_name(name, isFirst, condInput){
	
	var paren = name.indexOf("[");
	
	var type;
	if (paren == -1){
		type = name;
	} else{
		type = name.substring(0,paren);
	}
	
    if(type=="present" || type=="absent" || type=="inOrbit" ||type=="notInOrbit"||type=="together2"||
    		type=="togetherInOrbit2"||type=="separate2"||type=="together3"||type=="togetherInOrbit3"||
    		type=="separate3"||type=="emptyOrbit"||type=="numOrbits" || type=="numOfInstruments"){
    	
    	var type_modified;
    	var filterInputs = [];
    	if(type=="together2"||type=="together3"||type=="separate2"||type=="separate3"||
    		type=="togetherInOrbit2"||type=="togetherInOrbit3"){
    		type_modified = type.substring(0,type.length-1);
    	}else{
    		type_modified = type;
    	}
    	var arg = name.substring(name.indexOf("[")+1,name.indexOf("]"));
            
    	if(type_modified=="present" || type_modified=="absent" || type_modified=="emptyOrbit"
    				|| type_modified=="numOrbits" || type_modified=="together" 
    					|| type_modified=="separate"|| type_modified=="numOfInstruments"){
    		
    		filterInputs.push(arg);
    		
    	}else if(type_modified=="inOrbit" || type_modified=="notInOrbit" || 
    											type_modified=="togetherInOrbit"){
    		var first = arg.substring(0,arg.indexOf(","));
    		var second = arg.substring(arg.indexOf(",")+1);
    		filterInputs.push(first);
    		filterInputs.push(second);
    	} else{
    		filterInputs.push(arg);
    	}
            
    	if(isFirst==true){
            d3.selectAll("[class=dot]")[0].forEach(function (d) {
            	var bitString = d.__data__.archBitString;
        		if (presetFilter2(type_modified,bitString,filterInputs,false)==condInput){
        			d3.select(d).attr("class", "dot_DFhighlighted")
        						.style("fill", "#F75082");
    			}
            });
            d3.selectAll("[class=dot_clicked]")[0].forEach(function (d) {
            	var bitString = d.__data__.archBitString;
        		if (presetFilter2(type_modified,bitString,filterInputs,false)==condInput){
        			d3.select(d).attr("class", "dot_selected_DFhighlighted")
        						.style("fill", "#F75082");
    			}
            });
    	}else{
            d3.selectAll("[class=dot_DFhighlighted]")[0].forEach(function (d) {
            	var bitString = d.__data__.archBitString;
        		if (presetFilter2(type_modified,bitString,filterInputs,false) != condInput){
        			d3.select(d).attr("class", "dot")
                            .style("fill", function (d) {
                                if (d.status == "added") {
                                    return "#188836";
                                } else if (d.status == "justAdded") {
                                    return "#20FE5B";
                                } else {
                                    return "#000000";
                                }
                            });   
    			}
            });
            d3.selectAll("[class=dot_selected_DFhighlighted]")[0].forEach(function (d) {
            	var bitString = d.__data__.archBitString;
        		if (presetFilter2(type_modified,bitString,filterInputs,false) != condInput){
                   
        			d3.select(d).attr("class", "dot_clicked")
                    		.style("fill","#0040FF");     
    			}
            });
    	}
    }else{
    		type_modified = type;
        	if(first==true){
                d3.selectAll("[class=dot]")[0].forEach(function (d) {
                	var bitString = d.__data__.archBitString;
            		if (applyUserDefFilterFromExpression(type_modified,bitString) == condInput){
            			d3.select(d).attr("class", "dot_DFhighlighted")
            						.style("fill", "#F75082");
        			}
                });
                d3.selectAll("[class=dot_clicked]")[0].forEach(function (d) {
                	var bitString = d.__data__.archBitString;
            		if (applyUserDefFilterFromExpression(type_modified,bitString) == condInput){
            			d3.select(d).attr("class", "dot_selected_DFhighlighted")
            						.style("fill", "#F75082");
        			}
                });
        	}else{
                d3.selectAll("[class=dot_DFhighlighted]")[0].forEach(function (d) {
                	var bitString = d.__data__.archBitString;
            		if (applyUserDefFilterFromExpression(type_modified,bitString) != condInput){
                        
            			d3.select(d).attr("class", "dot")
                                .style("fill", function (d) {
                                    if (d.status == "added") {
                                        return "#188836";
                                    } else if (d.status == "justAdded") {
                                        return "#20FE5B";
                                    } else {
                                        return "#000000";
                                    }
                                });   
        			}
                });
                d3.selectAll("[class=dot_selected_DFhighlighted]")[0].forEach(function (d) {
                	var bitString = d.__data__.archBitString;
            		if (applyUserDefFilterFromExpression(type_modified,bitString) != condInput){
                       
            			d3.select(d).attr("class", "dot_clicked")
                        		.style("fill","#0040FF");     
        			}
                });
        	}
    }
}


function tree_node_mouse_over(d){

	if(d.children==null){
		
		if(d.depth==0){
			return;
		}
		
		var condition = d.cond;
		var currentNode = d.parent;
		var name = currentNode.name;
		
		for(var i=0;i<d.depth;i++){
			if(i==0){
				apply_filter_name(name, true, condition);
			}else{
				apply_filter_name(name, false, condition);
			}
			if (currentNode.depth==0){
				break;
			}
			condition = currentNode.cond;
			currentNode = currentNode.parent;
			name = currentNode.name;
		}
	}else{
		return;
	}
}
