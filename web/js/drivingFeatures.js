

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */





function runDataMining() {
	
	document.getElementById('tab3').click();
    highlight_basic_info_box()
    
	if(selection_changed == false && sortedDFs != null){
		display_drivingFeatures(sortedDFs,"lift");
		if(testType=="3"){
			display_classificationTree(jsonObj_tree);
		}
		return;
	}
	
    var selectedArchs = d3.selectAll("[class=dot_clicked]");
    var nonSelectedArchs = d3.selectAll("[class=dot]");
    var numOfSelectedArchs = selectedArchs.size();
    var numOfNonSelectedArchs = nonSelectedArchs.size();
    
    if (numOfSelectedArchs==0){
    	alert("First select target solutions!");
    }else{

        buttonClickCount_drivingFeatures += 1;
        getDrivingFeatures_numOfArchs.push({numOfSelectedArchs,numOfNonSelectedArchs});
        getDrivingFeatures_thresholds.push({supp:support_threshold,lift:lift_threshold,conf:confidence_threshold});
        
        
        var selectedBitStrings = [];
        var nonSelectedBitStrings = [];
        selectedBitStrings.length = 0;
        nonSelectedBitStrings.length=0;

        for (var i = 0; i < numOfSelectedArchs; i++) {
            var tmpBitString = booleanArray2String(selectedArchs[0][i].__data__.archBitString);
            selectedBitStrings.push(tmpBitString);
        }
        for (var i = 0; i < numOfNonSelectedArchs; i++) {
            var tmpBitString = booleanArray2String(nonSelectedArchs[0][i].__data__.archBitString);
            nonSelectedBitStrings.push(tmpBitString);
        }

        sortedDFs = generateDrivingFeatures(selectedBitStrings,nonSelectedBitStrings,support_threshold,confidence_threshold,lift_threshold,userDefFilters,"lift");
        if(testType=="3"){
            jsonObj_tree = buildClassificationTree();
        }
        
        display_drivingFeatures(sortedDFs,"lift");
        if(testType=="3"){
        	display_classificationTree(jsonObj_tree);
        }
        selection_changed = false;
        
    }
}









function generateDrivingFeatures(selected,nonSelected,
		support_threshold,confidence_threshold,lift_threshold,
		userDefFilters,sortBy){
	
	var output;
    $.ajax({
        url: "drivingFeatureServlet",
        type: "POST",
        data: {ID: "generateDrivingFeatures",selected: JSON.stringify(selected),nonSelected:JSON.stringify(nonSelected),
        	supp:support_threshold,conf:confidence_threshold,lift:lift_threshold,
        	userDefFilters:JSON.stringify(userDefFilters),sortBy:sortBy},
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




function display_filterOption(){
	
	document.getElementById('tab2').click();

    d3.select("[id=basicInfoBox_div]").select("[id=view2]").select("g").remove();

    var archInfoBox = d3.select("[id=basicInfoBox_div]").select("[id=view2]").append("g");
    archInfoBox.append("div")
            .attr("id","filter_title")
            .style("width","90%")
            .style("margin-top","10px")
            .style("margin-bottom","15px")
            .style("margin-left","2px")
            .style("float","left")
            .append("p")
            .text("Filter Setting")
            .style("font-size", "18px");
    var filterOptions = archInfoBox.append("div")
            .attr("id","filter_options")
            .style("width","100%")
            .style("height","40px")
            .style("float","left")
            .style("margin-bottom","10px");

    var filterDropdownMenu = d3.select("[id=filter_options]")
            .append("select")
            .attr("id","dropdown_presetFilters")
            .style("width","200px")
            .style("float","left")
            .style("margin-left","2px")
            .style("height","24px");

    filterDropdownMenu.selectAll("option").remove();
    filterDropdownMenu.selectAll("option")
            .data(filterDropdownOptions)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            });

    d3.select("[id=filter_options]").append("button")
            .attr("id","applyFilterButton_new")
            .attr("class","filterOptionButtons")
            .style("margin-left","6px")
            .style("float","left")
            .text("Apply new filter");
    d3.select("[id=filter_options]").append("button")
            .attr("class","filterOptionButtons")
            .attr("id","applyFilterButton_add")
            .style("margin-left","6px")
            .style("float","left")
            .text("Add to selection");
    d3.select("[id=filter_options]").append("button")
            .attr("id","applyFilterButton_within")
            .attr("class","filterOptionButtons")
            .style("margin-left","6px")
            .style("float","left")
            .text("Search within selection");
//    d3.select("[id=filter_options]").append("button")
//		    .attr("id","applyFilterButton_complement")
//		    .attr("class","filterOptionButtons")
//		    .style("margin-left","6px")
//		    .style("float","left")
//		    .text("Select complement");
    d3.select("[id=filter_options]").append("button")
            .attr("id","saveFilter")
            .attr("class","filterOptionButtons")
            .style("margin-left","6px")
            .style("float","left")
            .text("Save this filter")
            .attr('disabled', true);

    d3.select("[id=filter_options]").select("select").on("change",selectFilterOption);
    d3.select("[id=applyFilterButton_add]").on("click",applyFilter_add);
    d3.select("[id=applyFilterButton_new]").on("click",applyFilter_new);
    d3.select("[id=applyFilterButton_within]").on("click",applyFilter_within);
//    d3.select("[id=applyFilterButton_complement]").on("click",applyFilter_complement);
    
    highlight_basic_info_box()
}



function selectFilterOption(){

    var archInfoBox = d3.select("[id=basicInfoBox_div]").select("[id=view2]").select("g");

    archInfoBox.select("[id=filter_inputs]").remove();

    var filterDropdownMenu = d3.select("[id=dropdown_presetFilters]");
    var selectedOption = filterDropdownMenu[0][0].value;

    var filterInput = archInfoBox.append("div")
                .attr("id","filter_inputs");

    if (selectedOption==="defineNewFilter"){

        filterInput.append("div")
                .attr("id","newFilterDesignOptions")
                .text("Select preset filter to add: ");

        filterInput.select("[id=newFilterDesignOptions]")
                .append("select")
                .attr("id","dropdown_newFilterOption")
                .style("width","200px")
//                                .style("float","left")
                .style("margin-left","2px")
                .style("height","24px");

        var newFilterOptionDropdown = d3.select("[id=dropdown_newFilterOption]");

        newFilterOptionDropdown.selectAll("option").remove();

        var filterDropdownOptions_withoutUserDef = [];
        for(var i=0;i<filterDropdownOptions.length;i++){
            if(filterDropdownOptions[i].value!=="defineNewFilter"){
                filterDropdownOptions_withoutUserDef.push(filterDropdownOptions[i]);
            }
        }

        newFilterOptionDropdown.selectAll("option")
                .data(filterDropdownOptions_withoutUserDef)
                .enter()
                .append("option")
                .attr("value",function(d){
                    return d.value;
                })
                .text(function(d){
                    return d.text;
                });

        var filterDescription = filterInput.append("div")
                    .attr("id","userDefinedFilter_name_div")
                    .style("width","100%")
                    .style("float","left")
                    .style("margin-top","15px");
        filterDescription.append("div")
                .text("Filter name: ")
                .style("float","left");
        filterDescription.append("input")
                    .attr("id","userDefinedFilter_name")
                    .attr("type","text")
                    .style("width","450px")
                    .style("float","left")
                    .style("margin-left","5px")
                    .style("margin-right","10px");

        var filterExpression = filterInput.append("div")
                    .attr("id","filter_expression_div")
                    .style("width","100%")
                    .style("float","left")
                    .style("margin-top","15px")
                    .style("margin-bottom","5px");
        filterExpression.append("div")
                .text("Filter expression: ")
                .style("float","left");
        filterExpression.append("div")
                    .attr("id","filter_expression");

        userDefFilterExpressionHistory.length=0;
        d3.select("[id=dropdown_newFilterOption]").on("change",selectNewFilterOption); 


    } else if(selectedOption==="not_selected"){
        
    }else{
        selectFilterOption_filterInput(selectedOption,false); 
        d3.select("[id=saveFilter]").attr('disabled', true);
    }

}


function selectFilterOption_filterInput(selectedOption,userDefOption){

	
        d3.select("[id=filter_inputs]")
                .select("[id=filter_explanation]").remove();

    var filterInput = d3.select("[id=filter_inputs]");
    if(selectedOption=="not_selected"){
    }
    else if(selectedOption=="paretoFront"){

        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input Pareto Ranking (Integer number between 0-15): ");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");
    }
    else if (selectedOption=="present"){
        filterInputField_singleInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that have the specified instrument are selected)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    }
    else if (selectedOption=="absent"){
        filterInputField_singleInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that do not have the specified instrument are selected)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    }
    else if (selectedOption=="inOrbit"){
        filterInputField_orbitAndInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that have the specified instrument inside the chosen orbit are selected)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    }
    else if (selectedOption=="notInOrbit"){
        filterInputField_orbitAndInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that do not have the specified instrument inside the chosen orbit are selected)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    }
    else if (selectedOption=="together"){
        filterInputField_multipleInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that have the specified instruments in any one orbit are chosen)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    } 
    else if (selectedOption=="togetherInOrbit"){
        filterInputField_orbitAndMultipleInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that have the specified instruments in the specified orbit are chosen)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    } 
    else if (selectedOption=="separate"){
        filterInputField_multipleInstInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that do not have the specified instruments in the same orbit are chosen)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
        .append("div")
        .attr("id","filter_explanation_valid_inputs")
        .style("margin-top","15px")
        .style("margin-left","10px")
        .style("color", "#696969")
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');     
    } 
    else if (selectedOption=="emptyOrbit"){
        filterInputField_orbitInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that have no instrument inside the specified orbit are chosen)")
                .style("color", "#696969");   
        d3.select("[id=filter_inputs]")
	        .append("div")
	        .attr("id","filter_explanation_valid_inputs")
	        .style("margin-top","15px")
	        .style("margin-left","10px")
	        .style("color", "#696969")
	        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
	        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');         
    } 
    else if (selectedOption=="numOrbitUsed"){
        filterInputField_numOrbitInput();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: Designs that have the specified number of non-empty orbits are chosen)")
                .style("color", "#696969");  
        d3.select("[id=filter_inputs]")
		        .append("div")
		        .attr("id","filter_explanation_valid_inputs")
		        .style("margin-top","15px")
		        .style("margin-left","10px")
		        .style("color", "#696969")
		        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
		        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');         
    } 
    else if (selectedOption=="numOfInstruments"){
    	filterInputField_numOfInstruments();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: This highlights all the designs with the specified number of instruments. You can also specify the instrument name, and only those instruments will be counted.)")
                .style("color", "#696969");  
        d3.select("[id=filter_inputs]")
		        .append("div")
		        .attr("id","filter_explanation_valid_inputs")
		        .style("margin-top","15px")
		        .style("margin-left","10px")
		        .style("color", "#696969")
		        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
		        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');         
    } 
    
    else if(selectedOption=="subsetOfInstruments"){
        filterInputField_subsetOfInstruments();
        d3.select("[id=filter_inputs]")
                .append("div")
                .attr("id","filter_explanation")
                .style("margin-top","15px")
                .style("margin-left","10px")
                .text("(Hint: The specified orbit should contain at least m number and at maximum M number of instruments from the specified instrument set. m is the first entry and M is the second entry in the second field)")
                .style("color", "#696969");  
        d3.select("[id=filter_inputs]")
		        .append("div")
		        .attr("id","filter_explanation_valid_inputs")
		        .style("margin-top","15px")
		        .style("margin-left","10px")
		        .style("color", "#696969")
		        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
		        		+'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L'); 
    } else if(selectedOption=="defineNewFilter"){
    	
    } else{
        
    	if(!userDefOption){
    		
            var filterInput = d3.select("[id=filter_inputs]");

            var filterExpression = filterInput.append("div")
                        .attr("id","filter_expression_div")
                        .style("width","100%")
                        .style("float","left")
                        .style("margin-top","15px")
                        .style("margin-bottom","5px");
            filterExpression.append("div")
                    .text("Filter expression: ")
                    .style("float","left");
            filterExpression.append("div")
                        .attr("id","filter_expression");
    		
            var expression;
            for(var i=0;i<userDefFilters.length;i++){
                if(userDefFilters[i].name===selectedOption){
                    expression = userDefFilters[i].expression;
                }
            }

            d3.select("[id=filter_expression]")
                    .style("height","120px")
                    .text(expression);
    	}

    }    
}

function filterInputField_singleInstInput(){
    var filterInput = d3.select("[id=filter_inputs]");
        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input single instrument name: ");
        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")  
                .attr("type","text");
}
function filterInputField_orbitInput(){
    var filterInput = d3.select("[id=filter_inputs]");

        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input orbit name");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");
}
function filterInputField_orbitAndInstInput(){
    var filterInput = d3.select("[id=filter_inputs]");

        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input orbit name: ");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");

        filterInput.append("div")
                .attr("id","filter_input2")
                .text("Input single instrument name: ");

        filterInput.select("[id=filter_input2]")
                .append("input")
                .attr("id","filter_input2_textBox")
                .attr("type","text")
                .style("width","300px")
                .style("margin-left","5px")
                .style("margin-right","10px")
                .style("margin-bottem","5px");
}
function filterInputField_multipleInstInput(){
        var filterInput = d3.select("[id=filter_inputs]");
        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input instrument names (2 or 3) separated by comma:");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");
}
function filterInputField_orbitAndMultipleInstInput(){
        var filterInput = d3.select("[id=filter_inputs]");
        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input orbit name: ");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");

        filterInput.append("div")
                .attr("id","filter_input2")
                .text("Input instrument names (2 or 3) separated by comma: ");

        filterInput.select("[id=filter_input2]")
                .append("input")
                .attr("id","filter_input2_textBox")
                .attr("type","text")
                .style("width","300px")
                .style("margin-left","5px")
                .style("margin-right","10px")
                .style("margin-bottem","5px");
}
function filterInputField_numOfInstruments(){
    var filterInput = d3.select("[id=filter_inputs]");
    filterInput.append("div")
            .attr("id","filter_input1")
            .text("Input instrument name (Could be N/A): ");

    filterInput.select("[id=filter_input1]")
            .append("input")
            .attr("id","filter_input1_textBox")
            .attr("type","text");
    
    d3.select("#filter_input1_textBox").attr("value","N/A");

    filterInput.append("div")
            .attr("id","filter_input2")
	    .text("Input a number of instrument used (should be greater than or equal to 0): ");

    filterInput.select("[id=filter_input2]")
            .append("input")
            .attr("id","filter_input2_textBox")
            .attr("type","text")
            .style("width","300px")
            .style("margin-left","5px")
            .style("margin-right","10px")
            .style("margin-bottem","5px");
}
function filterInputField_numOrbitInput(){
        var filterInput = d3.select("[id=filter_inputs]");

        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input number of orbits");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");
}
function filterInputField_subsetOfInstruments(){
        var filterInput = d3.select("[id=filter_inputs]");
        filterInput.append("div")
                .attr("id","filter_input1")
                .text("Input orbit name: ");

        filterInput.select("[id=filter_input1]")
                .append("input")
                .attr("id","filter_input1_textBox")
                .attr("type","text");

        filterInput.append("div")
                .attr("id","filter_input2")
                .text("Input the min and the max (optional) number of instruments in the subset, separated by comma: ")
                ;

        filterInput.select("[id=filter_input2]")
                .append("input")
                .attr("id","filter_input2_textBox")
                .attr("type","text")
                .style("width","100px")
                .style("margin-left","5px")
                .style("margin-right","10px")
                .style("margin-bottem","5px");

        filterInput.append("div")
                .attr("id","filter_input3")
                .text("Input a set of instrument names, separated by comma: ");

        filterInput.select("[id=filter_input3]")
                .append("input")
                .attr("id","filter_input3_textBox")
                .attr("type","text")
                .style("width","300px")
                .style("margin-left","5px")
                .style("margin-right","10px")
                .style("margin-bottem","5px");
}



function applyFilter_new(){
	
    buttonClickCount_applyFilter += 1;

    cancelDotSelections();
    var wrong_arg = false;
    var filterType = d3.select("[id=dropdown_presetFilters]")[0][0].value;
    var neg = false;
    
	
    
    if (filterType == "paretoFront"){
        var filterInput = d3.select("[id=filter_input1_textBox]")[0][0].value;
        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
        	var rank = parseInt(d3.select(d).attr("paretoRank"));
            if (rank <= +filterInput && rank >= 0){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });

    }
    else if (filterType == "present" || filterType == "absent" || filterType == "inOrbit" || filterType == "notInOrbit" || filterType == "together" || filterType == "togetherInOrbit" || filterType == "separate" || 
            filterType == "emptyOrbit" || filterType=="numOrbitUsed" ||
            filterType=="subsetOfInstruments" || filterType=="numOfInstruments"){

        var filterInputs = [];
        if(d3.select("[id=filter_input1_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input1_textBox]")[0][0].value);
        }
        if(d3.select("[id=filter_input2_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input2_textBox]")[0][0].value);
        }
        if(d3.select("[id=filter_input3_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input3_textBox]")[0][0].value);
        }

        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
            var bitString = d.__data__.archBitString;
            var temp = presetFilter2(filterType,bitString,filterInputs,neg);
            if(temp==null){
            	wrong_arg = true;
            	return;
            }
            else if (temp){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });
    } else if(filterType == "defineNewFilter" || (filterType =="not_selected" && userDefFilters.length !== 0)){
        var filterExpression = d3.select("[id=filter_expression]").text();
        tmpCnt =0;

        d3.selectAll("[class=dot]")[0].forEach(function(d){
        	
            var bitString = d.__data__.archBitString;
            if(applyUserDefFilterFromExpression(filterExpression,bitString)){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });

        d3.select("[id=saveFilter]").attr('disabled', null)
                                    .on("click",saveNewFilter);
    }
    else{
        for(var k=0 ; k < userDefFilters.length; k++){
           if(userDefFilters[k].name == filterType){
                var filterExpression = userDefFilters[k].expression;
                d3.selectAll("[class=dot]")[0].forEach(function(d){
                    var bitString = d.__data__.archBitString;
                    if(applyUserDefFilterFromExpression(filterExpression,bitString)){
                        d3.select(d).attr("class", "dot_clicked")
                                    .style("fill", "#0040FF");
                    }
                }); 
           } 
        }
    }
    if(wrong_arg){
    	alert("Invalid input argument");
    }
    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
}

function applyFilter_within(){
	
	
    buttonClickCount_applyFilter += 1;
    var wrong_arg = false;
    var filterType = d3.select("[id=dropdown_presetFilters]")[0][0].value;
    var neg = false;

    if (filterType == "paretoFront"){
        var filterInput = d3.select("[id=filter_input1_textBox]")[0][0].value;
        var clickedArchs = d3.selectAll("[class=dot_clicked]")[0].forEach(function (d) {

        	var rank = parseInt(d3.select(d).attr("paretoRank"));
            if (rank <= +filterInput && rank >= 0){
            }else {
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

    }
    else if (filterType == "present" || filterType == "absent" || filterType == "inOrbit" || filterType == "notInOrbit" || 
            filterType == "together" || filterType == "togetherInOrbit" || filterType == "separate" || 
            filterType == "emptyOrbit" || filterType=="numOrbitUsed" || filterType=="subsetOfInstruments"||
            filterType == "numOfInstruments"	){


        var filterInputs = [];
        if(d3.select("[id=filter_input1_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input1_textBox]")[0][0].value);
        }
        if(d3.select("[id=filter_input2_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input2_textBox]")[0][0].value);
        }
        if(d3.select("[id=filter_input3_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input3_textBox]")[0][0].value);
        }


        var clickedArchs = d3.selectAll("[class=dot_clicked]")[0].forEach(function (d) {
//                            var bitString = booleanArray2String(d.__data__.archBitString)

            var bitString = d.__data__.archBitString;
            var temp = presetFilter2(filterType,bitString,filterInputs,neg);
            if(temp==null){
            	wrong_arg = true;
            	return;
            } else if(temp){
            } else {
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
    }
    else{
        for(var k=0 ; k < userDefFilters.length; k++){
           if(userDefFilters[k].name == filterType){
                var filterExpression = userDefFilters[k].expression;
                d3.selectAll("[class=dot_clicked]")[0].forEach(function(d){
                    var bitString = d.__data__.archBitString;
                    if(applyUserDefFilterFromExpression(filterExpression,bitString)){
                        d3.select(d).attr("class", "dot_clicked")
                                    .style("fill", "#0040FF");
                    }
                }); 
           } 
        }
    }
    if(wrong_arg){
    	alert("Invalid input argument");
    }
    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
}


function applyFilter_add(){
	
    buttonClickCount_applyFilter += 1;
    var wrong_arg = false;
    var filterType = d3.select("[id=dropdown_presetFilters]")[0][0].value;
    var neg = false;
    
    if (filterType == "paretoFront"){
        var filterInput = d3.select("[id=filter_input1_textBox]")[0][0].value;
        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
        	var rank = parseInt(d3.select(d).attr("paretoRank"));
            if (rank <= +filterInput && rank >= 0){
            	d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });

    }
    else if (filterType == "present" || filterType == "absent" || filterType == "inOrbit" || filterType == "notInOrbit" || 
            filterType == "together" || filterType == "togetherInOrbit" || filterType == "separate" || 
            filterType == "emptyOrbit" || filterType=="numOrbitUsed" || filterType =="subsetOfInstruments"||
            filterType=="numOfInstruments"){


        var filterInputs = [];
        if(d3.select("[id=filter_input1_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input1_textBox]")[0][0].value);
        }
        if(d3.select("[id=filter_input2_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input2_textBox]")[0][0].value);
        }
        if(d3.select("[id=filter_input3_textBox]")[0][0]!==null){
            filterInputs.push(d3.select("[id=filter_input3_textBox]")[0][0].value);
        }

        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
//                            var bitString = booleanArray2String(d.__data__.archBitString)
            var bitString = d.__data__.archBitString;
            var temp = presetFilter2(filterType,bitString,filterInputs,neg);
            if(temp==null){
            	wrong_arg = true;
            	return;
            }
            else if (temp){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });
    }
    else{
        for(var k=0 ; k < userDefFilters.length; k++){
           if(userDefFilters[k].name == filterType){
                var filterExpression = userDefFilters[k].expression;
                d3.selectAll("[class=dot]")[0].forEach(function(d){
                    var bitString = d.__data__.archBitString;
                    if(applyUserDefFilterFromExpression(filterExpression,bitString)){
                        d3.select(d).attr("class", "dot_clicked")
                                    .style("fill", "#0040FF");
                    }
                }); 
           } 
        }
    }
    if(wrong_arg){
    	alert("Invalid input argument");
    }
    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
}









               
var xScale_df;
var yScale_df;
var xAxis_df;
var yAxis_df;
var dfbar_width;
          
function display_drivingFeatures(source,sortby) {

    var size = source.length;
    var drivingFeatures = [];
    var drivingFeatureNames = [];
    var drivingFeatureTypes = [];
    i_drivingFeatures=0;
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
        if(source[i].preset===true){
            drivingFeatureNames.push(source[i].name);
            drivingFeatureTypes.push(source[i].type);
        } else{
            drivingFeatureNames.push(source[i].type);
            drivingFeatureTypes.push(source[i].name);
        }
    }

//                    InOrbit [orbit,inst1,inst2];

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
            .style("fill", function(d,i){return color_drivingFeatures(drivingFeatureTypes[i]);});
    dfbar_width = d3.select("[class=bar]").attr("width");

    var bars = d3.selectAll("[class=bar]")
   
                .on("mouseover",function(d){

                	numOfDrivingFeatureViewed = numOfDrivingFeatureViewed+1;
                	
                    var mouseLoc_x = d3.mouse(d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("[class=dfbars_svg]")[0][0])[0];
                    var mouseLoc_y = d3.mouse(d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("[class=dfbars_svg]")[0][0])[1];
                    var featureInfoLoc = {x:0,y:0};
                    var h_threshold = (width_df + margin_df.left + margin_df.right)*0.5;
                    var v_threshold = (height_df + margin_df.top + margin_df.bottom)*0.55;
                    var tooltip_width = 350;
                    var tooltip_height = 170;
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
                                                .attr("height",tooltip_height)
                                                .style("fill","#4B4B4B")
                                                .style("opacity", 0.92);
                    var tmp= d.id;
                    var name = relabelDrivingFeatureName(d.name);
                    var type = d.type;
                    var lift = d.metrics[1];
                    var supp = d.metrics[0];
                    var conf = d.metrics[2];
                    var conf2 = d.metrics[3];

                    d3.selectAll("[class=bar]").filter(function(d){
                        if(d.id===tmp){
                            return true;
                        }else{
                            return false;
                        }
                    }).style("stroke-width",1.5)
                            .style("stroke","black");

                    
                    if(type=="present" || type=="absent" || type=="inOrbit" ||type=="notInOrbit"||type=="together2"||
                    		type=="togetherInOrbit2"||type=="separate2"||type=="together3"||type=="togetherInOrbit3"||
                    		type=="separate3"||type=="emptyOrbit"||type=="numOrbits"|| type=="numOfInstruments"){
                    	
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
                    					|| type_modified=="separate" || type_modified=="numOfInstruments"){
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
                            
                        d3.selectAll("[class=dot]")[0].forEach(function (d) {
                        	var bitString = d.__data__.archBitString;
                            var temp = presetFilter2(type_modified,bitString,filterInputs,false);
                            if(temp==null){
                            	return;
                            }
                            else if (temp){
                    			d3.select(d).attr("class", "dot_DFhighlighted")
                    						.style("fill", "#F75082");
                			}
                        });
                        d3.selectAll("[class=dot_clicked]")[0].forEach(function (d) {
                        	var bitString = d.__data__.archBitString;
                            var temp = presetFilter2(type_modified,bitString,filterInputs,false);
                            if(temp==null){
                            	return;
                            }
                            else if (temp){
                    			d3.select(d).attr("class", "dot_selected_DFhighlighted")
                    						.style("fill", "#F75082");
                			}
                        });

                    }else{
                    		type_modified = type;
                            d3.selectAll("[class=dot]")[0].forEach(function (d) {
                            	var bitString = d.__data__.archBitString;
                        		if (applyUserDefFilterFromExpression(type_modified,bitString)){
                        			d3.select(d).attr("class", "dot_DFhighlighted")
                        						.style("fill", "#F75082");
                    			}
                            });
                            d3.selectAll("[class=dot_clicked]")[0].forEach(function (d) {
                            	var bitString = d.__data__.archBitString;
                        		if (applyUserDefFilterFromExpression(type_modified,bitString)){
                        			d3.select(d).attr("class", "dot_selected_DFhighlighted")
                        						.style("fill", "#F75082");
                    			}
                            });
                    }
                    

                    
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
                                            .attr({
                                                'class': 'fo_tooltip'
                                            });
                    var textdiv = fo_div.selectAll("div")
                            .data([{name:name,supp:supp,conf:conf,conf2:conf2,lift:lift}])
                            .enter()
                            .append("div")
                            .style("padding","15px");
//                            .style("margin-left","15px")
//                            .style("margin-top","10px");
                          
//                    
                    textdiv.html(function(d){
                        var output= "" + d.name + "<br><br> The % of designs in the intersection out of all designs: " + round_num_2_perc(d.supp) + 
                        "% <br> The % of selected designs among designs with the feature: " + round_num_2_perc(d.conf) + 
                        "%<br> The % of designs with the feature among selected designs: " + round_num_2_perc(d.conf2) +"%";
                        return output;
                    }).style("color", "#F7FF55");                         

                    draw_venn_diagram(df_explanation_box,supp,conf,conf2);

                })
                .on("mouseout",function(d){
                    d3.select("[id=basicInfoBox_div]").select("[id=view3]").selectAll("[id=featureInfo_tooltip]").remove();
                    d3.select("[id=basicInfoBox_div]").select("[id=view3]").selectAll("[id=foreignObject_tooltip]").remove();
                    var tmp= d.id;
                    d3.selectAll("[class=bar]").filter(function(d){
                           if(d.id===tmp){
                               return true;
                           }else{
                               return false;
                           }
                       }).style("stroke-width",0)
                               .style("stroke","black");
                    
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


    
    if(testType==="4"){
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
    

    d3.select("[id=instrumentOptions]")
            .select("table").remove();
    
    d3.select("[id=dfsort_options]").on("change",dfsort);
}
                


function dfsort(){
    var sortby = d3.select("[id=dfsort_options]")[0][0].value;
//    "lift","supp","conf(ave)","conf(feature->selection)","conf(selection->feature)"

    var sortedDrivingFeatures = sortDrivingFeatures(sortedDFs,sortby);
    sortedDFs=sortedDrivingFeatures;
    display_drivingFeatures(sortedDrivingFeatures,sortby);
}
                

function openFilterOptions(){
    d3.select("[id=basicInfoBox_div]").select("[id=view2]").select("g").remove();
    d3.select("[id=instrumentOptions]").select("table").remove();
    display_filterOption();
    
    buttonClickCount_filterOptions += 1;
}



                
   
   
   
   


function presetFilter2(filterName,bitString,inputs,neg){
    var filterInput1;
    var filterInput2;
    var filterInput3;

    filterInput1 = inputs[0];
    if(inputs.length > 1){
        filterInput2 = inputs[1];
    }
    if(inputs.length > 2){
        filterInput3 = inputs[2];
    }

    var output;
    var leng = bitString.length;
    var norb = orbitList.length;
    var ninstr = instrList.length;

    if(filterName==="present"){
        filterInput1 = relabelback(filterInput1);
        var thisInstr = $.inArray(filterInput1,instrList);
        if(thisInstr==-1){
        	return null;
        }
        output = false;
        for(var i=0;i<orbitList.length;i++){
            if(bitString[ninstr*i+thisInstr]===true){
                output = true;
                break;
            }
        }
    } else if(filterName==="absent"){
        filterInput1 = relabelback(filterInput1);
        var thisInstr = $.inArray(filterInput1,instrList);
        if(thisInstr==-1){
        	return null;
        }
        output = true;
        for(var i=0;i<orbitList.length;i++){
            if(bitString[ninstr*i+thisInstr]===true){
                output = false;
                break;
            }
        }
    } else if(filterName==="inOrbit"){
        filterInput1 = relabelback(filterInput1);
        filterInput2 = relabelback(filterInput2);
        output = false;
        var thisOrbit = $.inArray(filterInput1,orbitList);
        var thisInstr = $.inArray(filterInput2,instrList);
        
        if(thisInstr==-1 || thisOrbit==-1){
        	return null;
        }
            if(bitString[thisOrbit*ninstr + thisInstr]===true){
                output = true;
            }
    } else if(filterName==="notInOrbit"){
        filterInput1 = relabelback(filterInput1);
        filterInput2 = relabelback(filterInput2);
        output = true;
        var thisOrbit = $.inArray(filterInput1,orbitList);
        var thisInstr = $.inArray(filterInput2,instrList);
        if(thisInstr==-1 || thisOrbit==-1){
        	return null;
        }
            if(bitString[thisOrbit*ninstr + thisInstr]===true){
                output = false;
            }
    } else if(filterName === "together"){
        output = false;
        var splitInstruments = filterInput1.split(",");
        var thisInstr1 = $.inArray(relabelback(splitInstruments[0]),instrList);
        var thisInstr2 = $.inArray(relabelback(splitInstruments[1]),instrList);
        var thisInstr3;
        if(splitInstruments.length===2){
            if(thisInstr1==-1 || thisInstr2==-1){
            	return null;
            }
            for(var i=0;i<norb;i++){
                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true){
                    output = true;
                    break;
                }
            }
        } else {
            thisInstr3 = $.inArray(relabelback(splitInstruments[2]),instrList);
            if(thisInstr1==-1 || thisInstr2==-1 || thisInstr3==-1){
            	return null;
            }
            for(var i=0;i<norb;i++){
                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true
                        && bitString[i*ninstr + thisInstr3] === true){
                    output = true;
                    break;
                }
            }
        }
    } else if(filterName === "togetherInOrbit"){
        output = false;
        var thisOrbit =  $.inArray(relabelback(filterInput1),orbitList);
        var splitInstruments = filterInput2.split(",");
        var thisInstr1 = $.inArray(relabelback(splitInstruments[0]),instrList);
        var thisInstr2 = $.inArray(relabelback(splitInstruments[1]),instrList);
        var thisInstr3;
        if(splitInstruments.length===2){
            if(thisInstr1==-1 || thisInstr2==-1 || thisOrbit==-1){
            	return null;
            }
            if(bitString[thisOrbit*ninstr + thisInstr1] === true && bitString[thisOrbit*ninstr + thisInstr2] === true){
                output = true;
            }
        } else {
            thisInstr3 = $.inArray(relabelback(splitInstruments[2]),instrList);
            if(thisInstr1==-1 || thisInstr2==-1 || thisInstr3==-1 || thisOrbit==-1){
            	return null;
            }
            if(bitString[thisOrbit*ninstr + thisInstr1] === true && bitString[thisOrbit*ninstr + thisInstr2] === true
                        && bitString[thisOrbit*ninstr + thisInstr3] === true){
                output = true;
            }
        }
    } else if(filterName ==="separate"){
        output = true;
        var splitInstruments = filterInput1.split(",");
        var thisInstr1 = $.inArray(relabelback(splitInstruments[0]),instrList);
        var thisInstr2 = $.inArray(relabelback(splitInstruments[1]),instrList);
        var thisInstr3;
        if(splitInstruments.length===2){
            if(thisInstr1==-1 || thisInstr2==-1){
            	return null;
            }
            for(var i=0;i<norb;i++){
                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true){
                    output = false;
                    break;
                }
            }
        } else {
            thisInstr3 = $.inArray(relabelback(splitInstruments[2]),instrList);
            if(thisInstr1==-1 || thisInstr2==-1 || thisInstr3==-1){
            	return null;
            }
            for(var i=0;i<norb;i++){
                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true
                        && bitString[i*ninstr + thisInstr3] === true){
                    output = false;
                    break;
                }
            }
        }
    } else if(filterName ==="emptyOrbit"){
        var thisOrbit =  $.inArray(relabelback(filterInput1),orbitList);
        if(thisOrbit==-1){
        	return null;
        }
        output = true;
        for(var i=0;i<ninstr;i++){
            if(bitString[thisOrbit*ninstr + i]===true){
                output=false;
                break;
            }
        }
    } else if(filterName ==="numOrbitUsed"){
        var numOrbits = filterInput1;
        if(numOrbits > 5 || numOrbits < 0){
        	return null;
        }
        var cnt = 0;
        for (var i=0;i<norb;i++){
            for (var j=0;j<ninstr;j++){
                if(bitString[i*ninstr+j]==true){
                    cnt++;
                    break;
                }
            }
        }
        if(cnt==numOrbits){
            output = true;
        } else{
            output= false;
        }  
        
    } else if(filterName === "numOfInstruments"){
        output = false;

        var all_instruments = false;
        var thisInstr = -1;
        
        var instrument;
        var num_of_instruments;
        if(filterInput1.indexOf(",") != -1){
        	var comma = filterInput1.indexOf(",");
        	instrument = filterInput1.substring(0,comma);
        	num_of_instruments = filterInput1.substring(comma+1);
        }else{
        	instrument = filterInput1;
        	num_of_instruments = filterInput2;
        }
        
        if(instrument=="N/A"){
        	//continue;
        	all_instruments = true;
        }
        else{
            thisInstr = $.inArray(relabelback(instrument),instrList);
            if(thisInstr==-1){
            	return null;
            }
        }        
        var cnt = 0;
        for(var i=0;i<orbitList.length;i++){
        	if(all_instruments){
        		for(var j=0;j<instrList.length;j++){
                	if(bitString[i*ninstr + j] === true){
                        cnt = cnt+1;
                    }
        		}
        	}else{
            	if(bitString[i*ninstr + thisInstr] === true){
                    cnt = cnt+1;
                }
        	}
        }

            
        if(num_of_instruments== ""+cnt){
        	output=true;
        }else{
        	output=false;
        }
        return checkNeg(output,neg)     
        
    } else if(filterName === "subsetOfInstruments"){ 
        var thisOrbit = $.inArray(relabelback(filterInput1),orbitList);
        var minmax = filterInput2.split(",");
        var instruments = filterInput3.split(",");

        var constraint = minmax.length;
        var numOfInstr = instruments.length;

        var min,max;
        if(constraint===1){ // only the minimum number of instruments is typed in
            min = minmax[0];
            max = 100;
        } else if(constraint===2){
            min = minmax[0];
            max = minmax[1];
        }

        var size = instruments.length;
        var cnt=0;

        for(var i=0;i<size;i++){ //var thisInstr1 = $.inArray(splitInstruments[0],instrList);
            var thisInstr = $.inArray(relabelback(instruments[i]),instrList);
            if(bitString[thisOrbit*ninstr + thisInstr]===true){
                cnt++;
            }
        }
        if(cnt <= max && cnt >= min){
            output = true;
        }else{
            output = false;
        }
    }

    return checkNeg(output,neg)
}


function checkNeg(original,neg){
	if(neg==true){
		return !original;
	}else{
		return original;
	}
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
	
    radius_scale = d3.scale.pow()
    				.exponent(0.5)
					.domain([0,5])
	    			.range([10, 150]);
    var r1 = radius_scale(1);
    var	r2 = radius_scale(F_size/S_size);
    
    intersection_scale = d3.scale.linear()
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




function applyFilter(filterType,filterInput){
    cancelDotSelections();
    var wrong_arg = false;
    var neg = false;
    if (filterType == "paretoFront"){
        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
        	var rank = parseInt(d3.select(d).attr("paretoRank"));
            if (rank <= +filterInput && rank >= 0){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });

    }
    else if (filterType == "present" || filterType == "absent" || filterType == "inOrbit" || filterType == "notInOrbit" || filterType == "together" || filterType == "togetherInOrbit" || filterType == "separate" || 
            filterType == "emptyOrbit" || filterType=="numOrbitUsed" || filterType=="subsetOfInstruments"||
            filtertype == "numOfInstruments"){

        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
            var bitString = d.__data__.archBitString;
            var temp = presetFilter2(filterType,bitString,filterInputs,neg);
            if(temp==null){
            	wrong_arg = true;
            	return;
            }
            else if (temp){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });
    } else if(filterType == "defineNewFilter" || (filterType =="not_selected" && userDefFilters.length !== 0)){
        var filterExpression = d3.select("[id=filter_expression]").text();
        tmpCnt =0;

        d3.selectAll("[class=dot]")[0].forEach(function(d){
        	
            var bitString = d.__data__.archBitString;
            if(applyUserDefFilterFromExpression(filterExpression,bitString)){
                d3.select(d).attr("class", "dot_clicked")
                            .style("fill", "#0040FF");
            }
        });

        d3.select("[id=saveFilter]").attr('disabled', null)
                                    .on("click",saveNewFilter);
    }
    else{
    	
        for(var k=0 ; k < userDefFilters.length; k++){
           if(userDefFilters[k].name == filterType){
                var filterExpression = userDefFilters[k].expression;
                d3.selectAll("[class=dot]")[0].forEach(function(d){
                    var bitString = d.__data__.archBitString;
                    if(applyUserDefFilterFromExpression(filterExpression,bitString)){
                        d3.select(d).attr("class", "dot_clicked")
                                    .style("fill", "#0040FF");
                    }
                }); 
           } 
        }
    }
    if(wrong_arg){
    	alert("Invalid input argument");
    }
    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
}


