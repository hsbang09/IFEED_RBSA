/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




function openFilterOptions(){
    
    buttonClickCount_filterOptions += 1;
    
    document.getElementById('tab2').click();
    d3.select("[id=basicInfoBox_div]").select("[id=view2]").select("g").remove();

    var archInfoBox = d3.select("[id=basicInfoBox_div]").select("[id=view2]").append("g");
    archInfoBox.append("div")
            .attr("id","filter_title")
            .append("p")
            .text("Filter Setting");

    var filterOptions = archInfoBox.append("div")
            .attr("id","filter_options");
    var filterInputs = archInfoBox.append("div")
            .attr('id','filter_inputs')
    var filterHints = archInfoBox.append('div')
            .attr('id','filter_hints')
    var filterButtons = archInfoBox.append('div')
            .attr('id','filter_buttons')
    
    var filterDropdownMenu = d3.select("#filter_options")
            .append("select")
            .attr('id','filter_options_dropdown_1')
            .attr("class","filter_options_dropdown");
    
    filterDropdownMenu.selectAll("option").remove();
    filterDropdownMenu.selectAll("option")
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
    
    d3.select("#filter_buttons").append("button")
            .attr("id","applyFilterButton_new")
            .attr("class","filter_options_button")
            .text("Apply new filter");
    d3.select("#filter_buttons").append("button")
            .attr("class","filter_options_button")
            .attr("id","applyFilterButton_add")
            .style("margin-left","6px")
            .style("float","left")
            .text("Add to selection");
    d3.select("#filter_buttons").append("button")
            .attr("id","applyFilterButton_within")
            .attr("class","filter_options_button")
            .text("Search within selection");
//    d3.select("#filter_options").append("button")
//		    .attr("id","applyFilterButton_complement")
//		    .attr("class","filterOptionButtons")
//		    .text("Select complement");
    d3.select("#filter_buttons").append("button")
            .attr("id","saveFilter")
            .attr("class","filter_options_button")
            .text("Save this filter")
            .attr('disabled', true);

    d3.select("#filter_options_dropdown_1").on("change",filter_options_dropdown_1_changed);
    d3.select("#applyFilterButton_add").on("click",applyFilter_add);
    d3.select("#applyFilterButton_new").on("click",applyFilter_new);
    d3.select("#applyFilterButton_within").on("click",applyFilter_within);
//    d3.select("[id=applyFilterButton_complement]").on("click",applyFilter_complement);
    
    highlight_basic_info_box()
}


function remove_filter_option_inputs(level){
    
    d3.selectAll('.filter_inputs_div').remove(); 
    d3.selectAll('.filter_hints_div').remove();
    
    d3.select('#filter_options_dropdown_3').remove();
    if(level==2){return;}        
    d3.select('#filter_options_dropdown_2').remove();
    if(level==1){return;}
    
}




function filter_options_dropdown_1_changed(){
    remove_filter_option_inputs(1);
    var selectedScope = d3.select('#filter_options_dropdown_1')[0][0].value;
    
    if(selectedScope==="not_selected"){return;}
    var filterDropdownMenu = d3.select('#filter_options')
            .append('select')
            .attr('id','filter_options_dropdown_2')
            .attr('class','filter_options_dropdown')
            .style('margin-left',filter_options_dropdown_indentation);

    if(selectedScope==="design_input"){
        filterDropdownMenu.selectAll("option")
                .data(preset_filter_options)
                .enter()
                .append("option")
                .attr("value",function(d){
                    return d.value;
                })
                .text(function(d){
                    return d.text;
                }); 
    }else if(selectedScope==="objective"){
        filterDropdownMenu.selectAll("option")
                .data([{value:"not_selected",text:"Select objective"},{value:"pareto_front",text:"Pareto front"},
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
    d3.select("#filter_options_dropdown_2").on("change",function(d){
        if(selectedScope==="design_input"){filter_options_dropdown_preset_filters()}
        else if(selectedScope==="objective"){filter_options_dropdown_objectives()}
    });
}



function filter_options_dropdown_preset_filters(){
    
    remove_filter_option_inputs(2);
    var selectedOption = d3.select('#filter_options_dropdown_2')[0][0].value;
    
    if (selectedOption==="not_selected"){return;}
    else if (selectedOption==="defineNewFilter"){
//
//        filterInput.append("div")
//                .attr("id","newFilterDesignOptions")
//                .text("Select preset filter to add: ");
//
//        filterInput.select("[id=newFilterDesignOptions]")
//                .append("select")
//                .attr("id","dropdown_newFilterOption")
//                .style("width","200px")
////                                .style("float","left")
//                .style("margin-left","2px")
//                .style("height","24px");
//
//        var newFilterOptionDropdown = d3.select("[id=dropdown_newFilterOption]");
//
//        newFilterOptionDropdown.selectAll("option").remove();
//
//        var filterDropdownOptions_withoutUserDef = [];
//        for(var i=0;i<filterDropdownOptions.length;i++){
//            if(filterDropdownOptions[i].value!=="defineNewFilter"){
//                filterDropdownOptions_withoutUserDef.push(filterDropdownOptions[i]);
//            }
//        }
//
//        newFilterOptionDropdown.selectAll("option")
//                .data(filterDropdownOptions_withoutUserDef)
//                .enter()
//                .append("option")
//                .attr("value",function(d){
//                    return d.value;
//                })
//                .text(function(d){
//                    return d.text;
//                });
//
//        var filterDescription = filterInput.append("div")
//                    .attr("id","userDefinedFilter_name_div")
//                    .style("width","100%")
//                    .style("float","left")
//                    .style("margin-top","15px");
//        filterDescription.append("div")
//                .text("Filter name: ")
//                .style("float","left");
//        filterDescription.append("input")
//                    .attr("id","userDefinedFilter_name")
//                    .attr("type","text")
//                    .style("width","450px")
//                    .style("float","left")
//                    .style("margin-left","5px")
//                    .style("margin-right","10px");
//
//        var filterExpression = filterInput.append("div")
//                    .attr("id","filter_expression_div")
//                    .style("width","100%")
//                    .style("float","left")
//                    .style("margin-top","15px")
//                    .style("margin-bottom","5px");
//        filterExpression.append("div")
//                .text("Filter expression: ")
//                .style("float","left");
//        filterExpression.append("div")
//                    .attr("id","filter_expression");
//
//        userDefFilterExpressionHistory.length=0;
//        d3.select("[id=dropdown_newFilterOption]").on("change",selectNewFilterOption); 

    } else{
        filter_input_preset(selectedOption,false); 
        d3.select("[id=saveFilter]").attr('disabled', true);
        
        
//            d3.select('#filter_inputs')
//            .append('div')
//            .attr('id','filter_inputs_div_1')
//            .attr('class','filter_inputs_div');
    }

}




function filter_input_preset(selectedOption,userDefOption){

    var filter_inputs = d3.select("[id=filter_inputs]");


    if (selectedOption=="present"){
        append_filterInputField_singleInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that have the specified instrument are selected)");
   
    }
    else if (selectedOption=="absent"){
        append_filterInputField_singleInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that do not have the specified instrument are selected)");   
    }
    else if (selectedOption=="inOrbit"){
        append_filterInputField_orbitAndInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that have the specified instrument inside the chosen orbit are selected)");
    }
    else if (selectedOption=="notInOrbit"){
        append_filterInputField_orbitAndInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that do not have the specified instrument inside the chosen orbit are selected)");    
    }
    else if (selectedOption=="together"){
        append_filterInputField_multipleInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that have the specified instruments in any one orbit are chosen)");    
    } 
    else if (selectedOption=="togetherInOrbit"){
        append_filterInputField_orbitAndMultipleInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that have the specified instruments in the specified orbit are chosen)"); 
    } 
    else if (selectedOption=="separate"){
        append_filterInputField_multipleInstInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that do not have the specified instruments in the same orbit are chosen)");    
    } 
    else if (selectedOption=="emptyOrbit"){
        append_filterInputField_orbitInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that have no instrument inside the specified orbit are chosen)");       
    } 
    else if (selectedOption=="numOrbitUsed"){
        append_filterInputField_numOrbitInput();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: Designs that have the specified number of non-empty orbits are chosen)");      
    } 
    else if (selectedOption=="numOfInstruments"){
    	append_filterInputField_numOfInstruments();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: This highlights all the designs with the specified number of instruments. You can also specify the instrument name, and only those instruments will be counted.)"); 
    } 
    
    else if(selectedOption=="subsetOfInstruments"){
        append_filterInputField_subsetOfInstruments();
        d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint: The specified orbit should contain at least m number and at maximum M number of instruments from the specified instrument set. m is the first entry and M is the second entry in the second field)");  
    } else if(selectedOption=="defineNewFilter"){
    	
    } else{
//        
//    	if(!userDefOption){
//    		
//            var filterInput = d3.select("[id=filter_inputs]");
//
//            var filterExpression = filterInput.append("div")
//                        .attr("id","filter_expression_div")
//                        .style("width","100%")
//                        .style("float","left")
//                        .style("margin-top","15px")
//                        .style("margin-bottom","5px");
//            filterExpression.append("div")
//                    .text("Filter expression: ")
//                    .style("float","left");
//            filterExpression.append("div")
//                        .attr("id","filter_expression");
//    		
//            var expression;
//            for(var i=0;i<userDefFilters.length;i++){
//                if(userDefFilters[i].name===selectedOption){
//                    expression = userDefFilters[i].expression;
//                }
//            }
//
//            d3.select("[id=filter_expression]")
//                    .style("height","120px")
//                    .text(expression);
//    	}

    }  
    
    d3.select("#filter_hints")
        .append("div")
        .attr("id","filter_hints_div_2")
        .attr('class','filter_hints_div')
        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
                        +'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');      
}



function append_filterInputField_singleInstInput(){
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Input single instrument name: ")
            .append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");
}


function append_filterInputField_orbitInput(){
    d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Input orbit name")
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
}
function append_filterInputField_orbitAndInstInput(){

        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Input orbit name: ")
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");

        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .text("Input single instrument name: ")
            .append("input")
            .attr("class","filter_inputs_textobx")
            .attr("type","text");
    
}
function append_filterInputField_multipleInstInput(){
        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Input instrument names (2 or 3) separated by comma:")
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
}
function append_filterInputField_orbitAndMultipleInstInput(){
        d3.select('#filter_inputs')
            .append("div")
            .attr('id','filter_inputs_div_1')
            .attr('class','filter_inputs_div')
            .text("Input orbit name: ")
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");

        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .text("Input instrument names (2 or 3) separated by comma: ")
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
}


function append_filterInputField_numOfInstruments(){
        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Input instrument name (Could be N/A): ")
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text")
            .attr("value","N/A");

    d3.select('#filter_inputs').append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
	    .text("Input a number of instrument used (should be greater than or equal to 0): ")
            .append("input")
            .attr('class',"filter_inputs_textbox")
            .attr("type","text");
}
function append_filterInputField_numOrbitInput(){
        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_1")
                .attr('class','filter_inputs_div')
                .text("Input number of orbits")
                .append("input")
                .attr("class","filter_input_textbox")
                .attr("type","text");
}
function append_filterInputField_subsetOfInstruments(){
        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_1")
                .attr('class','filter_inputs_div')
                .text("Input orbit name: ")
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");

        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_2")
                .attr('class','filter_inputs_div')
                .text("Input the min and the max (optional) number of instruments in the subset, separated by comma: ")
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");

        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_3")
                .attr('class','filter_inputs_div')
                .text("Input a set of instrument names, separated by comma: ")
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");
}




function filter_options_dropdown_objectives(){

    remove_filter_option_inputs(2);
    var selectedOption = d3.select('#filter_options_dropdown_2')[0][0].value;
    
    if(selectedOption==="pareto_front"){
        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_1")
                .attr('class','filter_inputs_div')
                .text("Input Pareto Ranking (Integer number between 0-15): ")
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");
    }else if(selectedOption==="science"){   
                
        var filterDropdownMenu = d3.select('#filter_options')
            .append('select')
            .attr('id','filter_options_dropdown_3')
            .attr('class','filter_options_dropdown');
        filterDropdownMenu.selectAll("option")
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
        d3.select("#filter_options_dropdown_3").on("change",filter_options_dropdown_science);
        
    }else if(selectedOption==="cost"){
        
        var filterDropdownMenu = d3.select('#filter_options')
            .append('select')
            .attr('id','filter_options_dropdown_3')
            .attr('class','filter_options_dropdown');
        var collectionName = "cost.MANIFEST.Mission"
        append_filterInputField_fact_slot(filterDropdownMenu,collectionName);

        d3.select("#filter_options_dropdown_3").on("change",function(d){
            var slot = d3.select('#filter_options_dropdown_3')[0][0].value;
            if(slot==="not_selected"){return;}
            filter_options_dropdown_attribute(collectionName,slot);
        });
    }
}



function filter_options_dropdown_science(){
    var scope = d3.select('#filter_options_dropdown_3')[0][0].value;
    
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
    
    var filterDropdownMenu = d3.select('#filter_options')
            .append('select')
            .attr('id','filter_options_dropdown_4')
            .attr('class','filter_options_dropdown');
    append_filterInputField_fact_slot(filterDropdownMenu,collectionName);

    d3.select("#filter_options_dropdown_4").on("change",function(d){
        var slot = d3.select('#filter_options_dropdown_4')[0][0].value;
        if(slot==="not_selected"){return;}
        filter_options_dropdown_attribute(collectionName,slot);
    });
    
    
    
}


function append_filterInputField_fact_slot(select_object, collectionName){
    
    var slotNames = get_slotNames(collectionName);
    var slotNames_filter_options = [{value:"not_selected",text:"Select an attribute"}];
    for(var i=0;i<slotNames.length;i++){
        var name = slotNames[i];
        slotNames_filter_options.push({value:name,text:name});
    }
    select_object.selectAll("option")
            .data(slotNames_filter_options)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            });  
}


function get_slotNames(collectionName){
    var slotNames;
    $.ajax({
        url: "ResultsServlet",
        type: "POST",
        data: {ID: "get_slotNames",CollectionName:collectionName},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            slotNames = JSON.parse(data);
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("Error in getting the slot names");}
    });    
    return slotNames;
}
function get_minmax_value(collectionName, slotName){
    var min_and_max;
    $.ajax({
        url: "ResultsServlet",
        type: "POST",
        data: {ID: "get_minmax_value",CollectionName:collectionName,SlotName:slotName},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            min_and_max = JSON.parse(data);
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("Error in getting the slot names");}
    });    
    return min_and_max;
}
function get_valid_value_list(collectionName, slotName){
    var validList;
    $.ajax({
        url: "ResultsServlet",
        type: "POST",
        data: {ID: "get_valid_value_list",CollectionName:collectionName,SlotName:slotName},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            validList = JSON.parse(data);
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("Error in getting the slot names");}
    });    
    return validList;
}
function get_class_of_slot(collectionName, slotName){
    var slotClass;
    $.ajax({
        url: "ResultsServlet",
        type: "POST",
        data: {ID: "get_class_of_slot",CollectionName:collectionName,SlotName:slotName},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            slotClass = data;
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("Error in getting the slot names");}
    });    
    return slotClass;
}



function filter_options_dropdown_attribute(collectionName, slotName){
    
    d3.selectAll('.filter_inputs_div').remove();
    d3.selectAll('.filter_hints_div').remove();
    
    var slotClass = get_class_of_slot(collectionName,slotName);
    if(slotClass.includes("String")){
        filter_input_String(collectionName,slotName);
    }else{ // java.lang.Double or java.lang.Integer
        filter_input_Double(collectionName,slotName);
    }
}

function filter_input_String(collectionName, slotName){
    
    var validList = get_valid_value_list(collectionName,slotName);   
    var value_options = [{value:"not_selected",text:"Select value"}];
    for(var i=0;i<validList.length;i++){
        var val = validList[i];
        value_options.push({value:val,text:val});
    }    
    
    var condition_options = [{value:"not_selected",text:"Select a condition"},{value:"gt",text:"greater than"},
                    {value:"eq",text:"equal to"},{value:"neq",text:"not equal to"},{value:"lt",text:"less than"}];    
    
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Select all designs that contain")
            .append('select')
            .append('class','filter_inputs_select');
            
    d3.select('#filter_inputs_div_1')
            .select('select')
            .selectAll("option")
            .data(condition_options)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            });  
            
    d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");  
    
    d3.select('#filter_inputs_div_1')
            .append('div')
            .text(' instances of facts');
    
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .text("which have slot values that are ")
            .append('select')
            .append('class','filter_inputs_select');
            
    d3.select('#filter_inputs_div_2')
            .select('select')
            .selectAll("option")
            .data(value_options)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            }); 
            
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_3")
            .attr('class','filter_inputs_div')
            .text("Or includes substring ")
            .append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");  
            
    d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Hint:)");
}
function filter_input_Double(collectionName,slotName){
    
    var min_max = get_minmax_value(collectionName,slotName);   
    var min_val = min_max[0];
    var max_val = min_max[1];
    
    var condition_options = [{value:"not_selected",text:"Select a condition"},{value:"gt",text:"greater than"},
                    {value:"eq",text:"equal to"},{value:"neq",text:"not equal to"},{value:"lt",text:"less than"}];
    
   
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .text("Select all designs that contain")
            .append('select')
            .append('class','filter_inputs_select');
            
    d3.select('#filter_inputs_div_1')
            .select('select')
            .selectAll("option")
            .data(condition_options)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            });  
            
    d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");  
    
    d3.select('#filter_inputs_div_1')
            .append('div')
            .text(' instances of facts');

   
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .text("which have slot values that are ")
            .append('select')
            .append('class','filter_inputs_select');
    d3.select('#filter_inputs_div_2')
            .select('select')
            .selectAll("option")
            .data(condition_options)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            });  
    d3.select('#filter_inputs_div_2')
            .append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");
    
    d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_1")
                .attr('class','filter_hints_div')
                .text("(Max value: +"+ max_val +", min value: "+ min_val +")");
    d3.select("#filter_hints")
                .append("div")
                .attr("id","filter_hints_div_2")
                .attr('class','filter_hints_div')
                .text("(Hint:)");        
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
            var bitString = d.__data__.bitString;
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
        	
            var bitString = d.__data__.bitString;
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
                    var bitString = d.__data__.bitString;
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
//                            var bitString = booleanArray2String(d.__data__.bitString)

            var bitString = d.__data__.bitString;
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
                    var bitString = d.__data__.bitString;
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
//                            var bitString = booleanArray2String(d.__data__.bitString)
            var bitString = d.__data__.bitString;
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
                    var bitString = d.__data__.bitString;
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
            var bitString = d.__data__.bitString;
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
        	
            var bitString = d.__data__.bitString;
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
                    var bitString = d.__data__.bitString;
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
