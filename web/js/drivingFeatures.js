

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function open_df_scope_selection(){
    
    //document.getElementById('tab3').click();
    highlight_basic_info_box()
    
    d3.select("#basicInfoBox_div").select("#view3").select("g").remove();
    
    var displayBox = d3.select("#basicInfoBox_div").select("#view3").append("g");
    displayBox.append("div")
            .attr("id","df_title")
            .append("p")
            .text("Data Mining");
    displayBox.append('div')
            .attr('id','df_explanation_div_1')
            .attr('class','df_explanation_div');
    displayBox.append('div')
            .attr('id','df_scope_selection_div');
    displayBox.append('div')
            .attr('id','df_threshold_setup_div');
    displayBox.append('div')
            .attr('id','df_button_div');
    displayBox.append('div')
            .attr('id','df_explanation_div_2')
            .attr('class','df_explantion_div');

    var df_dropdown_selection = d3.select("#df_scope_selection_div")
            .append("select")
            .attr('id','df_scope_selection_dropdown_1')
            .attr("class","df_scope_selection_dropdown");
    
    df_dropdown_selection.selectAll("option")
            .data([{value:"not_selected",text:"Select the scope"},
                        {value:"design_input",text:"Design Inputs"},{value:"objective",text:"Objectives"}])
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            }); 
    d3.select('#df_scope_selection_dropdown_1').on("change",df_scope_selection_dropdown_1);

    d3.select('#df_explanation_div_1')
            .append('div')
            .attr('class','df_explanation')
            .text("To run data mining, select target solutions on the scatter plot. Then click the button below.");

    
}

function df_scope_selection_dropdown_1(){
    remove_df_scope_selection(1);
    var selectedScope = d3.select('#df_scope_selection_dropdown_1')[0][0].value;

    if(selectedScope==="not_selected"){return;}
    if(selectedScope==="design_input"){
        append_df_button('design_input');
    }else if(selectedScope==="objective"){
        
        var dropdown = d3.select('#df_scope_selection_div')
                .append('select')
                .attr('id','df_scope_selection_dropdown_2')
                .attr('class','df_scope_selection_dropdown');

        dropdown.selectAll("option")
                .data([{value:"not_selected",text:"Select objective"},
                            {value:"science",text:"Science benefit score"},{value:"cost",text:"Life-cycle cost"}])
                .enter()
                .append("option")
                .attr("value",function(d){
                    return d.value;
                })
                .text(function(d){
                    return d.text;
                });     
    }
    d3.select("#df_scope_selection_dropdown_2").on("change",df_scope_selection_dropdown_objectives);
}

function remove_df_scope_selection(level){
    
    d3.selectAll('.df_threshold_setup').remove();
    d3.selectAll('.df_threshold_setup_input').remove();
    
    d3.select('#df_button_div').select('div').remove(); 
    if(level==3){return;}
    d3.select('#df_scope_selection_dropdown_3').remove();
    if(level==2){return;}        
    d3.select('#df_scope_selection_dropdown_2').remove();
    if(level==1){return;}
}

function append_df_button(scope){ 
    append_df_threshold_setup();
    d3.select("#df_button_div")
            .append('div')
            .append("button")
            .attr("id","run_df_mining_button")
            .text("Run data mining");
    d3.selectAll("#run_df_mining_button").on("click", function(d){
        runDataMining(scope);
    });    
}




function append_df_threshold_setup(){
    var input1 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_1')
            .attr('class','df_threshold_setup');
    input1.append('div')
            .text('Support threshold: ');
    input1.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','0.015');

    var input2 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_2')
            .attr('class','df_threshold_setup');
    input2.append('div')
            .text('Confidence threshold (Recommendation: larger than 0.5): ');
    input2.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','0.3');
    
    var input3 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_3')
            .attr('class','df_threshold_setup');
    input3.append('div')
            .text('Lift threshold (Recommendation: larger than 1): ');
    input3.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','1');        
    
    var input3 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_4')
            .attr('class','df_threshold_setup');
    input3.append('div')
            .text('Number of discretized intervals: ');
    input3.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','3');      
    
}




function df_scope_selection_dropdown_objectives(){
    remove_df_scope_selection(2);
    var selectedOption = d3.select('#df_scope_selection_dropdown_2')[0][0].value;
    
    if(selectedOption==="not_selected"){return;}
    if(selectedOption==="science"){ 
        
        var dropdown = d3.select('#df_scope_selection_div')
            .append('select')
            .attr('id','df_scope_selection_dropdown_3')
            .attr('class','df_scope_selection_dropdown');
        dropdown.selectAll("option")
                .data([{value:"not_selected",text:"Select a scope"},
                        {value:"mission",text:"Mission"},
                        {value:"measurement",text:"Measurement"},
                        {value:"capabilities",text:"Capabilities"},
                        {value:"stakeholder",text:"Aggregation: stakeholder"},
                        {value:"objective",text:"Aggregation: objective"},
                        {value:"subobjective",text:"Aggregation: subobjective"}])
                .enter()
                .append("option")
                .attr("value",function(d){
                    return d.value;
                })
                .text(function(d){
                    return d.text;
                });     
        d3.select("#df_scope_selection_dropdown_3").on("change",df_scope_selection_dropdown_science);
        
    }else if(selectedOption==="cost"){
        append_df_button('cost.MANIFEST.Mission');
    }
}

function df_scope_selection_dropdown_science(){
    remove_df_scope_selection(3);
    
    var scope = d3.select('#df_scope_selection_dropdown_3')[0][0].value;
    var collectionName;
    if(scope==="not_selected"){
        return;
    }else if(scope==="mission"){
       collectionName = "science.MANIFEST.Mission";
    }else if(scope==="measurement"){
        collectionName = "science.REQUIREMENTS.Measurement";
    }else if(scope==="capabilities"){
        collectionName = "science.CAPABILITIES.Manifested_instrument";
    }else if(scope==="stakeholder"){
        collectionName = "science.AGGREGATION.STAKEHOLDER";
    }else if(scope==="objective"){
        collectionName = "science.AGGREGATION.OBJECTIVE";
    }else if(scope==="subobjective"){
        collectionName = "science.AGGREGATION.SUBOBJECTIVE";
    }
    append_df_button(collectionName);
}



function runDataMining(scope) {
// scope can be 'preset_filers', or collectionName from the database
    
    if(selection_changed == false && sortedDFs != null){
        display_drivingFeatures(sortedDFs,"lift");
        display_classificationTree(jsonObj_tree);
        return;
    }
    
    current_scope = scope;
    processed_features = [];
    color_drivingFeatures = d3.scale.category10();   
    
    var selectedArchs = d3.selectAll("[class=dot_highlighted]");
    var nonSelectedArchs = d3.selectAll("[class=dot]");
    var numOfSelectedArchs = selectedArchs.size();
    var numOfNonSelectedArchs = nonSelectedArchs.size();
    
    if (numOfSelectedArchs==0){
    	alert("First select target solutions!");
        return;
    }


    var selectedBitStrings = [];
    var nonSelectedBitStrings = [];
    selectedBitStrings.length = 0;
    nonSelectedBitStrings.length=0;

    for (var i = 0; i < numOfSelectedArchs; i++) {
        selectedBitStrings.push(selectedArchs[0][i].__data__.ArchID);
    }
    for (var i = 0; i < numOfNonSelectedArchs; i++) {
        nonSelectedBitStrings.push(nonSelectedArchs[0][i].__data__.ArchID);
    }
    
    support_threshold = d3.select('#df_threshold_setup_1').select('.df_threshold_setup_input')[0][0].value;
    confidence_threshold = d3.select('#df_threshold_setup_2').select('.df_threshold_setup_input')[0][0].value;
    lift_threshold = d3.select('#df_threshold_setup_3').select('.df_threshold_setup_input')[0][0].value;
    var numIntervals = d3.select('#df_threshold_setup_4').select('.df_threshold_setup_input')[0][0].value;


    sortedDFs = generateDrivingFeatures(scope,selectedBitStrings,nonSelectedBitStrings,
                            userdef_features,numIntervals,
                            support_threshold,confidence_threshold,lift_threshold,"lift");
    display_drivingFeatures(sortedDFs,"lift");

//    if(testType=="3"){
//        jsonObj_tree = buildClassificationTree();
//        display_classificationTree(jsonObj_tree);
//    }
    
    
    selection_changed = false;
}






function generateDrivingFeatures(scope,selected,nonSelected,
                userdef_features,numIntervals,
		support_threshold,confidence_threshold,lift_threshold,
		sortBy){
        
    var output;
    $.ajax({
        url: "DrivingFeatureServlet",
        type: "POST",
        data: {ID: "generateDrivingFeatures",selected: JSON.stringify(selected),nonSelected:JSON.stringify(nonSelected),
        	scope:scope,
                userDefFeatures:JSON.stringify(userdef_features), numIntervals:numIntervals,
                supp:support_threshold,conf:confidence_threshold,lift:lift_threshold,
        	sortBy:sortBy},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            if(data===""){alert("No driving feature found!")}else{
        	output = JSON.parse(data);
            }
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("error");}
    });
    
    return output;
}


function sortDrivingFeatures(drivingFeatures,sortBy){
	
	var newlySorted = [];
	newlySorted.length=0;
	
	for (var i=0;i<drivingFeatures.length;i++){
		
		var thisDF = drivingFeatures[i];
		var value=0;
		var maxval = 1000000000;
		var minval = -1;
		
		if(newlySorted.length==0){
			newlySorted.push(thisDF);
			continue;
		}
		
		var metrics = thisDF.metrics;
	       
        if(sortBy=="lift"){
            value = thisDF.metrics[1];
            maxval = newlySorted[0].metrics[1];
            minval = newlySorted[newlySorted.length-1].metrics[1];
        } else if(sortBy=="supp"){
            value = thisDF.metrics[0];
            maxval = newlySorted[0].metrics[0];
            minval = newlySorted[newlySorted.length-1].metrics[0];
        } else if(sortBy=="confave"){
            value = (thisDF.metrics[2] + thisDF.metrics[3])/2;
            maxval = (newlySorted[0].metrics[2] + newlySorted[0].metrics[3])/2;
            minval = (newlySorted[newlySorted.length-1].metrics[2]+newlySorted[newlySorted.length-1].metrics[3])/2;
        } else if(sortBy=="conf1"){
            value = thisDF.metrics[2];
            maxval = newlySorted[0].metrics[2];
            minval = newlySorted[newlySorted.length-1].metrics[2];
        } else if(sortBy=="conf2"){
            value = thisDF.metrics[3];
            maxval = newlySorted[0].metrics[3];
            minval = newlySorted[newlySorted.length-1].metrics[3];
        }
		
		if(value>=maxval){
			newlySorted.splice(0,0,thisDF);
		} else if (value<=minval){
			newlySorted.push(thisDF);
		} else {
			for (var j=0;j<newlySorted.length;j++){
				var refval=0; var refval2=0;
				
				if(sortBy=="lift"){
					refval=newlySorted[j].metrics[1];
					refval2=newlySorted[j+1].metrics[1];
				} else if(sortBy=="supp"){
					refval=newlySorted[j].metrics[0];
					refval2=newlySorted[j+1].metrics[0];
				} else if(sortBy=="confave"){
					refval=(newlySorted[j].metrics[2]+newlySorted[j].metrics[3])/2
					refval2=(newlySorted[j+1].metrics[2]+newlySorted[j+1].metrics[3])/2
				} else if(sortBy=="conf1"){
					refval=newlySorted[j].metrics[2];
					refval2=newlySorted[j+1].metrics[2];
				} else if(sortBy=="conf2"){
					refval=newlySorted[j].metrics[3];
					refval2=newlySorted[j+1].metrics[3];
				}
				if(value <=refval && value > refval2){
					newlySorted.splice(j+1,0,thisDF); break;
				}
		
			}
		}
	}         
	return newlySorted;
}





               
var xScale_df;
var yScale_df;
var xAxis_df;
var yAxis_df;
var dfbar_width;
          
function display_drivingFeatures(source,sortby) {

    var size = source.length;
    var drivingFeatures = [];
    var df_name = [];
    var df_expression = [];
    var i_drivingFeatures=0;
    var lifts=[];
    var supps=[];
    var conf1s=[];
    var conf2s=[];

    for (var i=0;i<size;i++){
        lifts.push(source[i].metrics[1]);
        supps.push(source[i].metrics[0]);
        conf1s.push(source[i].metrics[2]);
        conf2s.push(source[i].metrics[3]);
        drivingFeatures.push(source[i]);
        df_name.push(source[i].name);
        df_expression.push(source[i].expression);
    }

//  InOrbit [orbit,inst1,inst2];

    var margin_df = {top: 20, right: 20, bottom: 10, left:65},
    width_df = 800 - 35 - margin_df.left - margin_df.right,
    height_df = 430 - 20 - margin_df.top - margin_df.bottom;

//    xScale_df = d3.scale.ordinal()
//            .rangeBands([0, width_df]);
    xScale_df = d3.scale.linear()
            .range([0, width_df]);
    yScale_df = d3.scale.linear().range([height_df, 0]);
    xScale_df.domain([0,drivingFeatures.length-1]);
    
    
    var minval;
    if(sortby==="lift"){
        minval = d3.min(lifts);
        yScale_df.domain([d3.min(lifts), d3.max(lifts)]);
    } else if(sortby==="supp"){
        minval = d3.min(supps);
        yScale_df.domain([d3.min(supps), d3.max(supps)]);
    }else if(sortby==="confave"){
        var min_tmp = (d3.min(conf1s) + d3.min(conf2s))/2;
        minval = min_tmp;
        var max_tmp = (d3.max(conf1s) + d3.max(conf2s))/2;
        yScale_df.domain([min_tmp, max_tmp]);
    }else if(sortby==="conf1"){
        minval = d3.min(conf1s);
        yScale_df.domain([d3.min(conf1s), d3.max(conf1s)]);
    }else if(sortby==="conf2"){
        minval = d3.min(conf2s);
        yScale_df.domain([d3.min(conf2s), d3.max(conf2s)]);
    }

    xAxis_df = d3.svg.axis()
            .scale(xScale_df)
            .orient("bottom")
            .tickFormat(function (d) { return ''; });
    yAxis_df = d3.svg.axis()
            .scale(yScale_df)
            .orient("left");

    d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("g").remove();
    var infoBox = d3.select("[id=basicInfoBox_div]").select("[id=view3]")
            .append("g");
    
    
    infoBox.append('button')
            .attr('id','df_action_button_delete')
            .attr('class','df_action_button')
            .text('Remove selected features')
            .style('background-color','#FF9393');
//    infoBox.append('button')
//            .attr('id','df_action_button_mrmr')
//            .attr('class','df_action_button')
//            .text('Run feature selection')
//            .on('click',config_df_feature_selection);
    infoBox.append('button')
            .attr('id','df_action_button_dm_config')
            .attr('class','df_action_button')
            .text('Extract higher order features')
            .on('click',config_df_mining_higher_order);
    infoBox.append('button')
            .attr('id','df_action_button_reset')
            .attr('class','df_action_button')
            .text('Reset data mining')
            .on('click',function(d){
                selection_changed = true;
                selected_features = [];
                removed_features = [];
                initialize_tabs_driving_features();
            });    
    
    
    
    var svg_df = infoBox.append("svg")
    		.style("float","left")
            .attr("width", width_df + margin_df.left + margin_df.right)
            .attr("height", height_df + margin_df.top + margin_df.bottom)
                .call(
                    d3.behavior.zoom()
                    .x(xScale_df)
                    .scaleExtent([1, 10])
                    .on("zoom", function (d) {

                        var svg = d3.select("[id=basicInfoBox_div]").select("[id=view3]")
                                .select("svg");
                        var scale = d3.event.scale;

                        svg.select(".x.axis").call(xAxis_df);
                 
                        svg.selectAll("[class=bar]")
                                .attr("transform",function(d){
                                    var xCoord = xScale_df(d.id);
                                    return "translate(" + xCoord + "," + 0 + ")";
                                })
                                .attr("width", function(d){
                                    return dfbar_width*scale;
                                });
                        })
                    )
            .append("g")        
            .attr("transform", "translate(" + margin_df.left + "," + margin_df.top + ")");

    
    
    var df_explanation_box = infoBox.append("div")
		.attr("id","df_explanation_box")
		.style("float","left")
		.style("background-color","#E7E7E7")
	    .style('height','350px')
	    .style('margin-top','15px')
	    .style('padding','15px')
	    .style('width','320px');
    
    df_explanation_box.append('div')
		.style("font-family","sans-serif")
		.style('margin-left','10px')
		.style("font-size","18px")
    	.style('width','100%')
    	.text('Total number of designs: ' + numOfArchs());
    
    df_explanation_box.append("svg")
		.style('width','320px')  			
		.style('height','305px')
		.style('margin-top','10px')
		.style('margin-bottom','10px'); 
    

////////////////////////////////////////////////////////
    // x-axis
    svg_df.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height_df + ")")
            .call(xAxis_df)
            .append("text")
            .attr("class", "label")
            .attr("x", width_df)
            .attr("y", -6)
            .style("text-anchor", "end");

    // y-axis
    svg_df.append("g")
            .attr("class", "y axis")
            .call(yAxis_df)
            .append("text")
            .attr("class","label")
            .attr("transform", "rotate(-90)")
            .attr("y",-60)
            .attr("x",-3)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(function(d){
                if(sortby==="lift"){
                    return "Lift";
                } else if(sortby==="supp"){
                    return "Support";
                }else if(sortby==="confave"){
                    return "Average Confidence";
                }else if(sortby==="conf1"){
                    return "Confidence {feature}->{selection}"
                }else if(sortby==="conf2"){
                    return "Confidence {selection}->{feature}"
                }
            });

    var objects = svg_df.append("svg")
            .attr("class","dfbars_svg")
            .attr("width",width_df)
            .attr("height",height_df);

    //Create main 0,0 axis lines:
    objects.append("svg:line")
            .attr("class", "axisLine hAxisLine")
            .attr("x1", 0)
            .attr("y1", 0)
            .attr("x2", width_df)
            .attr("y2", 0)
            .attr("transform", "translate(0," + (yScale_df(minval)) + ")");
    objects.append("svg:line")
            .attr("class", "axisLine vAxisLine")
            .attr("x1", 0)
            .attr("y1", 0)
            .attr("x2", 0)
            .attr("y2", height_df)
            .attr("transform", "translate(" + (xScale_df(0)) + ",0)");
    /////////////////////////////////////////////////////////////////////////////////



    objects.selectAll(".bar")
            .data(drivingFeatures, function(d){return (d.id = i_drivingFeatures++);})
            .enter()
            .append("rect")
            .attr("class","bar")
            .attr("x", function(d) {
                return 0;
            })
            .attr("width", xScale_df(1))
            .attr("y", function(d) { 
                if(sortby==="lift"){
                    return yScale_df(d.metrics[1]); 
                } else if(sortby==="supp"){
                    return yScale_df(d.metrics[0]); 
                }else if(sortby==="confave"){
                    return yScale_df((d.metrics[2]+d.metrics[3])/2); 
                }else if(sortby==="conf1"){
                    return yScale_df(d.metrics[2]); 
                }else if(sortby==="conf2"){
                    return yScale_df(d.metrics[3]); 
                }
            })
            .attr("height", function(d) { 
                if(sortby==="lift"){
                    return height_df - yScale_df(d.metrics[1]); 
                } else if(sortby==="supp"){
                    return height_df - yScale_df(d.metrics[0]); 
                }else if(sortby==="confave"){
                    return height_df - yScale_df((d.metrics[2]+d.metrics[3])/2); 
                }else if(sortby==="conf1"){
                    return height_df - yScale_df(d.metrics[2]); 
                }else if(sortby==="conf2"){
                    return height_df - yScale_df(d.metrics[3]); 
                }
            })
            .attr("transform",function(d){
                var xCoord = xScale_df(d.id);
                return "translate(" + xCoord + "," + 0 + ")";
            })
            .style("fill", function(d,i){return color_drivingFeatures(df_name[i]);});
    dfbar_width = d3.select("[class=bar]").attr("width");

    var bars = d3.selectAll("[class=bar]")
            .on("click",function(d){
                var DFID = d.DFID;
                var expression = d.expression;
                
                var was_selected = false;
                for(var i=0;i<selected_features.length;i++){
                    if(selected_features[i]===DFID){
                        selected_features.splice(i,1);
                        was_selected = true;
                    }
                }
                
                if(was_selected){
                    d3.selectAll("[class=bar]").filter(function(d){
                        if(d.DFID===DFID){
                            return true;
                        }else{
                            return false;
                        }
                    }).style("stroke-width",0);
                }else{
                    d3.selectAll("[class=bar]").filter(function(d){
                        if(d.DFID===DFID){
                            return true;
                        }else{
                            return false;
                        }
                    }).style("stroke-width",2); 
                    selected_features.push(DFID);
                    
                    
                }
            })
                .on("mouseover",function(d){

                    numOfDrivingFeatureViewed = numOfDrivingFeatureViewed+1;
                	
                    var mouseLoc_x = d3.mouse(d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("[class=dfbars_svg]")[0][0])[0];
                    var mouseLoc_y = d3.mouse(d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("[class=dfbars_svg]")[0][0])[1];
                    var featureInfoLoc = {x:0,y:0};
                    var h_threshold = (width_df + margin_df.left + margin_df.right)*0.5;
                    var v_threshold = (height_df + margin_df.top + margin_df.bottom)*0.55;
                    var tooltip_width = 360;
                    var tooltip_height = 210;
                    if(mouseLoc_x >= h_threshold){
                        featureInfoLoc.x = -10 - tooltip_width;
                    } else{
                        featureInfoLoc.x = 10;
                    }
                    if(mouseLoc_y < v_threshold){
                        featureInfoLoc.y = 10;
                    } else{
                        featureInfoLoc.y = -10 -tooltip_height;
                    }
                    var svg_tmp = d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("[class=dfbars_svg]");
                    var featureInfoBox = svg_tmp.append("g")
                                                .attr("id","featureInfo_tooltip")
                                                .append("rect")
                                                .attr("id","featureInfo_box")
                                                .attr("transform", function(){
                                                    var x = mouseLoc_x + featureInfoLoc.x;
                                                    var y = mouseLoc_y + featureInfoLoc.y;
                                                    return "translate(" + x + "," + y + ")";
                                                })
                                                .attr("width",tooltip_width)
                                                .attr("height",tooltip_height);
                    var DFID= d.DFID;
                    var name = d.name;
                    var expression = d.expression;
                    var lift = d.metrics[1];
                    var supp = d.metrics[0];
                    var conf = d.metrics[2];
                    var conf2 = d.metrics[3];
                    var preset = d.preset;

                    d3.selectAll("[class=bar]").filter(function(d){
                        if(d.DFID===DFID){
                            return true;
                        }else{
                            return false;
                        }
                    }).style("stroke-width",1.5)
                            .style("stroke","black");

                    
                    var matchedArchIDs=null;
                    for(var i=0;i<processed_features.length;i++){
                        if(processed_features[i].DFID===DFID){
                            matchedArchIDs = processed_features[i].matchedArchIDs;
                        }
                    }
                    if(matchedArchIDs===null){
                        $.ajax({
                            url: "DrivingFeatureServlet",
                            type: "POST",
                            data: {ID: "applyComplexFilter",filterExpression:expression},
                            async: false,
                            success: function (data, textStatus, jqXHR)
                            {
                                matchedArchIDs = JSON.parse(data);
                            },
                            error: function (jqXHR, textStatus, errorThrown)
                            {alert("Error in applying the filter");}
                        });
                        processed_features.push({DFID:DFID,expression:expression,matchedArchIDs:matchedArchIDs});
                    }


                    d3.selectAll(".dot")[0].forEach(function (d) {
                        var id = d.__data__.ArchID;
                        if($.inArray(id,matchedArchIDs)!==-1){
                            d3.select(d).attr('class','dot_DFhighlighted')
                                        .style("fill", "#F75082");
                        }
                    });

                    d3.selectAll("[class=dot_highlighted]")[0].forEach(function (d) {
                        var id = d.__data__.ArchID;
                        if($.inArray(id,matchedArchIDs)!==-1){
                            d3.select(d).attr('class','dot_selected_DFhighlighted')
                                        .style("fill", "#F75082");
                        }
                    });
                    

                    
                    var fo = d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("[class=dfbars_svg]")
                                    .append("g")
                                    .attr("id","foreignObject_tooltip")
                                    .append("foreignObject")
                                    .attr("x",function(){
                                        return mouseLoc_x + featureInfoLoc.x;
                                    })
                                    .attr("y",function(){
                                       return mouseLoc_y + featureInfoLoc.y; 
                                    })
                                    .attr({
                                        'width':tooltip_width,
                                        'height':tooltip_height  
                                    });
                                    
                    var fo_div = fo.append('xhtml:div')
                                    .attr('class','fo_tooltip')
                                    .style('width',tooltip_width);
                    var textdiv = fo_div.selectAll("div")
                            .data([{name:expression,supp:supp,conf:conf,conf2:conf2,lift:lift,preset:preset}])
                            .enter()
                            .append("div")
                            .style("padding","13px");
//                    
                    textdiv.html(function(d){
                        var output= "" + ppdf(d.name,d.preset) + "<br><br> Lift: " + round_num_2_dec(d.lift) + 
                        "<br> Support: " + round_num_2_dec(d.supp) + 
                        "<br> Confidence (Feature->Selection): " + round_num_2_dec(d.conf) + 
                        "<br> Confidence (Selection->Feature): " + round_num_2_dec(d.conf2) +"";
                        return output;
                    })
//                    textdiv.html(function(d){
//                        var output= "" + ppdf(d.name,d.preset) + "<br><br> The % of designs in the intersection out of all designs: " + round_num_2_perc(d.supp) + 
//                        "% <br> The % of selected designs among designs with the feature: " + round_num_2_perc(d.conf) + 
//                        "%<br> The % of designs with the feature among selected designs: " + round_num_2_perc(d.conf2) +"%";
//                        return output;
//                    })
                    .style("color", "#F7FF55")
                    .style("word-wrap","break-word");                         

                    draw_venn_diagram(df_explanation_box,supp,conf,conf2);

                })
                .on("mouseout",function(d){
                    d3.select("[id=basicInfoBox_div]").select("[id=view3]").selectAll("[id=featureInfo_tooltip]").remove();
                    d3.select("[id=basicInfoBox_div]").select("[id=view3]").selectAll("[id=foreignObject_tooltip]").remove();
                    var DFID= d.DFID;
                    var expression = d.expression;
                                        
                    d3.selectAll("[class=bar]").filter(function(d){
                           if(d.DFID===DFID){
                               for(var i=0;i<selected_features.length;i++){
                                   if(selected_features[i]===DFID){
                                       return false;
                                   }
                               }
                               return true;
                           }else{
                               return false;
                           }
                       }).style("stroke-width",0)
                               .style("stroke","black");
                    
                    var highlighted = d3.selectAll("[class=dot_DFhighlighted]");
                    highlighted.attr("class", "dot")
                            .style("fill", "#000000");     
                    d3.selectAll("[class=dot_selected_DFhighlighted]")
                    		.attr("class", "dot_highlighted")
                            .style("fill","#20DCCC");    
                });

                if(color_drivingFeatures.domain().length > 10){
                    // skip drawing legend
                }else{

                    // draw legend
                    var legend_df = objects.selectAll(".legend")
                                    .data(color_drivingFeatures.domain())
                                    .enter().append("g")
                                    .attr("class", "legend")
                                    .attr("transform", function(d, i) { return "translate(0," + (i * 20) + ")"; });

                        // draw legend colored rectangles
                    legend_df.append("rect")
                            .attr("x", 655)
                            .attr("width", 18)
                            .attr("height", 18)
                            .style("fill", color_drivingFeatures);

                        // draw legend text
                    legend_df.append("text")
                            .attr("x", 655)
                            .attr("y", 9)
                            .attr("dy", ".35em")
                            .style("text-anchor", "end")
                            .text(function(d) { return d;});

                }

        
        
    d3.select('#df_action_button_delete')
            .on('click',function(d){
                for(var i=0;i<selected_features.length;i++){
                    var DFID = selected_features[i];                    
                    removed_features.push(DFID);
                    for(var j=0;j<source.length;j++){
                        if(source[j].DFID===DFID){
                            break;
                        }
                    }
                    source.splice(j,1);
                }
                selected_features = [];
                display_drivingFeatures(source,sortby);
            });        
        
        
        
    num_features=d3.selectAll('.bar')[0].length;
    //d3.select("[id=dfsort_options]").on("change",dfsort);
}
             
             
             
             


function dfsort(){
    var sortby = d3.select("[id=dfsort_options]")[0][0].value;
//    "lift","supp","conf(ave)","conf(feature->selection)","conf(selection->feature)"

    var sortedDrivingFeatures = sortDrivingFeatures(sortedDFs,sortby);
    sortedDFs=sortedDrivingFeatures;
    display_drivingFeatures(sortedDrivingFeatures,sortby);
}


function draw_venn_diagram(df_explanation_box,supp,conf,conf2){

    df_explanation_box.select("svg").remove();
    var svg_venn_diag = df_explanation_box
            .append("svg")
            .style('width','320px')  			
            .style('border-width','3px')
            .style('height','305px')
            .style('border-style','solid')
            .style('border-color','black')
            .style('border-radius','40px')
            .style('margin-top','10px')
            .style('margin-bottom','10px'); 

    var F_size = supp * 1/conf;
    var S_size = supp * 1/conf2;

    // Radius range: 30 ~ 80
    // Intersecting distance range: 0 ~ (r1+r2)
	
    var radius_scale = d3.scale.pow()
    				.exponent(0.5)
					.domain([0,5])
	    			.range([10, 150]);
    var r1 = radius_scale(1);
    var	r2 = radius_scale(F_size/S_size);
    
    var intersection_scale = d3.scale.linear()
					.domain([0,1])
					.range([r1+r2, 20+ r2-r1]);
    
    var left_margin = 50;
    var c1x = left_margin + r1;
    var c2x;
	if (conf2 > 0.99){
		c2x = c1x + r2 - r1;
    }else{
    	c2x = c1x + intersection_scale(conf2);
    }
	
	svg_venn_diag
		.append("circle")
		.attr("id","venn_diag_c1")
	    .attr("cx", c1x)
	    .attr("cy", 180-30)
	    .attr("r", r1)
	    .style("fill", "steelblue")
	    .style("fill-opacity", ".5");
    
	svg_venn_diag
		.append("circle")
		.attr("id","venn_diag_c2")
	    .attr("cx", c2x)
	    .attr("cy", 180-30)
	    .attr("r", r2)
	    .style("fill", "brown")
	    .style("fill-opacity", ".5");
	
	
	svg_venn_diag
		.append("text")
		.attr("x",left_margin-10)
		.attr("y",70-30)
		.attr("font-family","sans-serif")
		.attr("font-size","18px")
		.attr("fill","black")
		.text("Intersection: " + Math.round(supp * numOfArchs()));
	
	svg_venn_diag
		.append("text")
		.attr("x",c1x-110)
		.attr("y",180+r1+50-30)
		.attr("font-family","sans-serif")
		.attr("font-size","18px")
		.attr("fill","steelblue")
		.text("Selected:" + numOfSelectedArchs() );
	svg_venn_diag
		.append("text")
		.attr("x",c1x+30)
		.attr("y",180+r1+50-30)
		.attr("font-family","sans-serif")
		.attr("font-size","18px")
		.attr("fill","brown")
		.text("Features:" + Math.round(F_size * numOfArchs()) );
}




function config_df_mining_higher_order(){

    d3.select("#basicInfoBox_div").select("#view3").select("g").remove();
    var displayBox = d3.select("#basicInfoBox_div").select("#view3").append("g");
    displayBox.append("div")
            .attr("id","df_title")
            .append("p")
            .text("Higher-order feature extraction");
    
    displayBox.append('div')
            .attr('id','df_hdf_return_button_div')
            .append('button')
            .attr('id','df_hdf_return_button')
            .text('Return to feature extraction results')
            .on('click',function(d){
                display_drivingFeatures(sortedDFs,"lift");
            });
            
    var level_selection = displayBox.append('div')
            .attr('id','df_hdf_level_selection_div');
    level_selection.append('div')
            .text('Select the maximum level of features to be mined: ');
    level_selection.append('input')
            .attr('id','df_hdf_level_selection_input')
            .attr('value','2');
            
    displayBox.append('div')
            .attr('id','df_threshold_setup_div');
    displayBox.append('div')
            .attr('id','df_button_div');
    displayBox.append('div')
            .attr('id','df_explanation_div_2')
            .attr('class','df_explantion_div');


    var input1 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_1')
            .attr('class','df_threshold_setup');
    input1.append('div')
            .text('Support threshold: ');
    input1.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','0.015');

    var input2 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_2')
            .attr('class','df_threshold_setup');
    input2.append('div')
            .text('Confidence threshold (Recommendation: larger than 0.5): ');
    input2.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','0.3');
    
    var input3 = d3.select("#df_threshold_setup_div")
            .append("div")
            .attr("id",'df_threshold_setup_3')
            .attr('class','df_threshold_setup');
    input3.append('div')
            .text('Lift threshold (Recommendation: larger than 1): ');
    input3.append("input")
            .attr("class","df_threshold_setup_input")  
            .attr("type","text")
            .attr('value','1');        

    d3.select("#df_button_div")
            .append('div')
            .append("button")
            .attr("id","run_df_mining_button")
            .text("Extract higher-order driving features");
    d3.selectAll("#run_df_mining_button").on("click", run_df_mining_higher_order);    
    
}


function run_df_mining_higher_order(){

    support_threshold = d3.select('#df_threshold_setup_1').select('.df_threshold_setup_input')[0][0].value;
    confidence_threshold = d3.select('#df_threshold_setup_2').select('.df_threshold_setup_input')[0][0].value;
    lift_threshold = d3.select('#df_threshold_setup_3').select('.df_threshold_setup_input')[0][0].value;

    var maxLevel = d3.select('#df_hdf_level_selection_input')[0][0].value;

    $.ajax({
        url: "DrivingFeatureServlet",
        type: "POST",
        data: {ID: "generateHigherOrderDrivingFeautres",
        	scope:current_scope,
                removedFeatures:JSON.stringify(removed_features),
                supp:support_threshold,
                conf:confidence_threshold,
                lift:lift_threshold,
        	sortBy:"lift",
                maxLevel:maxLevel
            },
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            if(data===""){alert("No driving feature found!")}else{
        	sortedDFs = JSON.parse(data);
            }
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("error");}
    });

    display_drivingFeatures(sortedDFs,"lift");
    selection_changed = false;    
}

function config_df_feature_selection(){
    
    d3.select("#basicInfoBox_div").select("#view3").select("g").remove();
    var displayBox = d3.select("#basicInfoBox_div").select("#view3").append("g");
    displayBox.append("div")
            .attr("id","df_title")
            .append("p")
            .text("Feature selection (Minimum redundancy maximum relevance algorithm");
    
    displayBox.append('div')
            .attr('id','df_hdf_return_button_div')
            .append('button')
            .attr('id','df_hdf_return_button')
            .text('Return to feature extraction results')
            .on('click',function(d){
                display_drivingFeatures(sortedDFs,"lift");
            });
            
    var level_selection = displayBox.append('div')
            .attr('id','df_hdf_level_selection_div');
    level_selection.append('div')
            .text(function(){
                return 'Select the the number of features to be selected ('+ num_features +' currently shown): ';
            });
    level_selection.append('input')
            .attr('id','df_hdf_level_selection_input');
            
    displayBox.append('div')
            .attr('id','df_threshold_setup_div');
    displayBox.append('div')
            .attr('id','df_button_div');
    displayBox.append('div')
            .attr('id','df_explanation_div_2')
            .attr('class','df_explantion_div');
    
    d3.select("#df_button_div")
            .append('div')
            .append("button")
            .attr("id","run_df_mining_button")
            .text("Run feature selection");
    d3.selectAll("#run_df_mining_button").on("click", run_df_feature_selection);        
}

function run_df_feature_selection(){
    
    var numFeatures = d3.select('#df_hdf_level_selection_input')[0][0].value;

    $.ajax({
        url: "DrivingFeatureServlet",
        type: "POST",
        data: {ID: "runFeatureSelection",
        	scope:current_scope,
                removedFeatures:JSON.stringify(removed_features),
        	sortBy:"lift",
                numFeatures:numFeatures
            },
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            if(data===""){alert("No driving feature found!")}else{
        	sortedDFs = JSON.parse(data);
            }
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("error");}
    });

    display_drivingFeatures(sortedDFs,"lift");
    selection_changed = false;      
}