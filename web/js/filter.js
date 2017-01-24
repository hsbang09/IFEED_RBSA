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

    var filterApplicationStatus = archInfoBox.append('div')
            .attr('id','filter_application_status');
    var filterOptions = archInfoBox.append("div")
            .attr("id","filter_options");
    var filterInputs = archInfoBox.append("div")
            .attr('id','filter_inputs');
    var filterAppendSlots = archInfoBox.append("div")
            .attr('id','filter_inputs_append_slots');
    var filterHints = archInfoBox.append('div')
            .attr('id','filter_hints');
    var filterButtons = archInfoBox.append('div')
            .attr('id','filter_buttons');
    
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
    d3.select("#applyFilterButton_add").on("click",function(d){
        applyFilter("add");
    });
    d3.select("#applyFilterButton_new").on("click",function(d){
        applyFilter("new");
    });
    d3.select("#applyFilterButton_within").on("click",function(d){
        applyFilter("within");
    });
//    d3.select("[id=applyFilterButton_complement]").on("click",applyFilter_complement);
    
    highlight_basic_info_box()
}


function remove_filter_option_inputs(level){
    
    d3.selectAll('.filter_inputs_div').remove(); 
    d3.selectAll('.filter_hints_div').remove();
    d3.select('#filter_inputs_append_slot_button').remove();
    d3.select('#filter_inputs_append_slot_select').remove();
    
    
    d3.select('#filter_options_dropdown_4').remove();
    if(level==3){return;}
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
            .attr('class','filter_options_dropdown');

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
    else{
        filter_input_preset(selectedOption,false); 
        d3.select("[id=saveFilter]").attr('disabled', true);
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
    else if (selectedOption=="numOrbits"){
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
                .text("(Hint: This highlights all the designs with the specified number of instruments. If you specify an orbit name, it will count all instruments in that orbit. If you can also specify an instrument name, and only those instruments will be counted across all orbits. If you leave both instruments and orbits blank, all instruments across all orbits will be counted.)"); 
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

    }  
    
//    d3.select("#filter_hints")
//        .append("div")
//        .attr("id","filter_hints_div_2")
//        .attr('class','filter_hints_div')
//        .html('<p>Valid orbit names: 1000, 2000, 3000, 4000, 5000</p>'
//                        +'Valid instrument names: A, B, C, D, E, F, G, H, I, J, K, L');      
}



function append_filterInputField_singleInstInput(){
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input single instrument name: ");
    d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");
}


function append_filterInputField_orbitInput(){
    d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input orbit name");
    d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
}
function append_filterInputField_orbitAndInstInput(){

        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input orbit name: ");
        d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");

        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input single instrument name: ");
        d3.select('#filter_inputs_div_2')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
    
}
function append_filterInputField_multipleInstInput(){
        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input instrument names (2 or 3) separated by comma:");
        d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
}
function append_filterInputField_orbitAndMultipleInstInput(){
        d3.select('#filter_inputs')
            .append("div")
            .attr('id','filter_inputs_div_1')
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input orbit name: ");
        d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");

        d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input instrument names (2 or 3) separated by comma: ");
        d3.select('#filter_inputs_div_2')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text");
}


function append_filterInputField_numOfInstruments(){
    d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input an orbit name (Could be N/A): ");
    d3.select('#filter_inputs_div_1')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text")
            .attr("value","N/A");    
    
    d3.select('#filter_inputs')
            .append("div")
            .attr("id","filter_inputs_div_2")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Input instrument name (Could be N/A): ");
    d3.select('#filter_inputs_div_2')
            .append("input")
            .attr("class","filter_inputs_textbox")
            .attr("type","text")
            .attr("value","N/A");

    d3.select('#filter_inputs').append("div")
            .attr("id","filter_inputs_div_3")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
	    .text("Input a number of instrument used (should be greater than or equal to 0): ");
    d3.select('#filter_inputs_div_3')
            .append("input")
            .attr('class',"filter_inputs_textbox")
            .attr("type","text");
}
function append_filterInputField_numOrbitInput(){
        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_1")
                .attr('class','filter_inputs_div')
                .append('div')
                .attr('class','filter_inputs_supporting_comments_begin')
                .text("Input number of orbits");
        d3.select('#filter_inputs_div_1')
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");
}
function append_filterInputField_subsetOfInstruments(){
        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_1")
                .attr('class','filter_inputs_div')
                .append('div')
                .attr('class','filter_inputs_supporting_comments_begin')
                .text("Input orbit name: ");
        d3.select('#filter_inputs_div_1')
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");

        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_2")
                .attr('class','filter_inputs_div')
                .append('div')
                .attr('class','filter_inputs_supporting_comments_begin')
                .text("Input the min and the max (optional) number of instruments in the subset, separated by comma: ");
        d3.select('#filter_inputs_div_2')
                .append("input")
                .attr("class","filter_inputs_textbox")
                .attr("type","text");

        d3.select('#filter_inputs')
                .append("div")
                .attr("id","filter_inputs_div_3")
                .attr('class','filter_inputs_div')
                .append('div')
                .attr('class','filter_inputs_supporting_comments_begin')
                .text("Input a set of instrument names, separated by comma: ");
        d3.select('#filter_inputs_div_3')
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
            if(slot==="not_selected"){
                d3.selectAll('.filter_inputs_div').remove();
                d3.selectAll('.filter_hints_div').remove();
                return;
            }
            filter_options_dropdown_attribute(collectionName,slot,true);
        });
    }
}



function filter_options_dropdown_science(){
    
    remove_filter_option_inputs(3);
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
        if(slot==="not_selected"){
            d3.selectAll('.filter_inputs_div').remove();
            d3.selectAll('.filter_hints_div').remove();
            return;
        }
        filter_options_dropdown_attribute(collectionName,slot,true);
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



function filter_options_dropdown_attribute(collectionName, slotName, first){    
    
    if(first){
        d3.selectAll('.filter_inputs_div').remove();
        d3.selectAll('.filter_hints_div').remove();
        d3.select('#filter_inputs_append_slot_button').remove();
        d3.select('#filter_inputs_append_slot_select').remove();
        
        filter_input_num_instances();
        
        var append_slot_select = d3.select('#filter_inputs_append_slots')
                .append('select')
                .attr('id','filter_inputs_append_slot_select');
        append_filterInputField_fact_slot(append_slot_select, collectionName);
        
        d3.select('#filter_inputs_append_slots')
                .append('button')
                .attr('id','filter_inputs_append_slot_button')
                .text('Add condition')
                .on("click",function(){
                    var slot = d3.select('#filter_inputs_append_slot_select')[0][0].value;
                    if(slot==="not_selected"){
                        return
                    }
                    filter_options_dropdown_attribute(collectionName, slot, false);
                });
    }

    var slotClass = get_class_of_slot(collectionName,slotName);
    if(slotClass.includes("String")){
        filter_input_String(collectionName,slotName);
    }else{ // java.lang.Double
        filter_input_Double(collectionName,slotName);
    }
}


function filter_input_num_instances(){
    
    var condition_options = [{value:"not_selected",text:"Select a condition"},{value:"gt",text:"more than"},
                    {value:"eq",text:"exactly"},{value:"ne",text:"not equal to"},
                    {value:"all",text:"all"},{value:"lt",text:"less than"}];    
    
    d3.select("#filter_inputs")
            .append("div")
            .attr("id","filter_inputs_div_1")
            .attr('class','filter_inputs_div')
            .append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text("Select all designs that contain ");
    
    d3.select('#filter_inputs_div_1')
            .append('select')
            .attr('class','filter_inputs_select');
            
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
            .attr('class','filter_inputs_supporting_comments_end')
            .text(' instances of facts that satisfy the following conditions:');
    
}





function filter_input_String(collectionName, slotName){
    
    var validList = get_valid_value_list(collectionName,slotName);   
    var value_options = [{value:"not_selected",text:"Select value"}];
    for(var i=0;i<validList.length;i++){
        var val = validList[i];
        value_options.push({value:val,text:val});
    }    
    

    var inputNum = get_number_of_inputs() + 1;
    
    var thisInput = d3.select("#filter_inputs")
            .append("div")
            .attr("id",function(){
                return "filter_inputs_div_" + inputNum;
            })
            .attr('class','filter_inputs_div')
            .attr('slotType','String')
            .attr('slotName',function(){
                return slotName;
            });
    
    thisInput.append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text(function(){
                return " - The value of slot ["+ slotName +"] is ";
            });
    thisInput.append('select')
            .attr('class','filter_inputs_select');
            
    thisInput.select('select')
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
            
            
    thisInput.append("div")
            .attr("class","filter_inputs_supporting_comments_end")
            .text("Or it includes a substring ");
    thisInput.append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");  
}






function filter_input_Double(collectionName,slotName){
    
    var min_max = get_minmax_value(collectionName,slotName);   
    var min_val = min_max[0];
    var max_val = min_max[1];
    
    var condition_options = [{value:"not_selected",text:"Select a condition"},{value:"gt",text:"greater than"},
                    {value:"eq",text:"equal to"},{value:"ne",text:"not equal to"},{value:"lt",text:"less than"}];
    
    var inputNum = get_number_of_inputs() + 1;

    var thisInput = d3.select("#filter_inputs")
            .append("div")
            .attr("id",function(){
                return "filter_inputs_div_" + inputNum;
            })
            .attr('class','filter_inputs_div')
            .attr('slotType','Double')
            .attr('slotName',function(){
                return slotName;
            });
    
    thisInput.append('div')
            .attr('class','filter_inputs_supporting_comments_begin')
            .text(function(){
                return " - The value of slot ["+ slotName +"] is ";
            });
    
    thisInput.append('select')
            .attr('class','filter_inputs_select');
    
    thisInput.select('select')
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
    thisInput.append("input")
            .attr("class","filter_inputs_textbox")  
            .attr("type","text");
    
    thisInput.append("div")
                .attr('class','filter_inputs_supporting_comments_end')
                .text("(Max value: +"+ max_val +", min value: "+ min_val +")");
}

function get_number_of_inputs(){
    return d3.selectAll('.filter_inputs_div')[0].length;
}





function applyFilter(option){
    buttonClickCount_applyFilter += 1;
    
    var wrong_arg = false;
    
    var filterExpression;
    var preset = false;
    var matchedArchIDs = null;

    var dropdown1 = d3.select("#filter_options_dropdown_1")[0][0].value;
    var dropdown2 = null;
    var dropdown3 = null;
    var dropdown4 = null;
    

    if(d3.select('#filter_options_dropdown_2')[0][0]!==null){
        dropdown2 = d3.select('#filter_options_dropdown_2')[0][0].value;
    }
    if(d3.select('#filter_options_dropdown_3')[0][0]!==null){
        dropdown3 = d3.select('#filter_options_dropdown_3')[0][0].value;
    }
    if(d3.select('#filter_options_dropdown_4')[0][0]!==null){
        dropdown4 = d3.select('#filter_options_dropdown_4')[0][0].value;
    }    
    
    var numInputs = get_number_of_inputs();
    var input_textbox = [];
    var input_select = [];
    var inputObj =  d3.selectAll('.filter_inputs_div')[0];
    inputObj.forEach(function(d,i){
        var textboxObj = d3.select(d).select('.filter_inputs_textbox')[0][0];
        var selectObj = d3.select(d).select('.filter_inputs_select')[0][0];
        if(textboxObj!==null){
            input_textbox.push(textboxObj.value);
        }else{
            input_textbox.push(null);
        }
        if(selectObj!==null){
            input_select.push(selectObj.value);
        }else{
            input_select.push(null);
        }
    })
//    for(var i=0;i<numInputs;i++){
//        var textboxObj = d3.select(inputObj[i]).select('.filter_inputs_textbox')[0][0];
//        var selectObj = d3.select(inputObj[i]).select('.filter_inputs_select')[0][0];
//        if(textboxObj!==null){
//            input_textbox.push(textboxObj.value);
//        }else{
//            input_textbox.push(null);
//        }
//        if(selectObj!==null){
//            input_select.push(selectObj.value);
//        }else{
//            input_select.push(null);
//        }
//    }

    
    if(dropdown1=="design_input"){ 
        
        preset = true;
        
        // Example of an filter expression: {presetName[orbits;instruments;numbers]} 
        var presetFilter = dropdown2;
        if(presetFilter=="present" || presetFilter=="absent" || presetFilter=="together" || presetFilter=="separate"){
            var instrument = input_textbox[0];
            filterExpression = presetFilter + "[;" + ActualName2Index(instrument,"instrument") + ";]";
        }else if(presetFilter == "inOrbit" || presetFilter == "notInOrbit" || presetFilter=="togetherInOrbit"){
            var orbit = input_textbox[0];
            var instrument = input_textbox[1];
            filterExpression = presetFilter + "["+ ActualName2Index(orbit,"orbit") + ";" + ActualName2Index(instrument,"instrument")+ ";]";
        }else if(presetFilter =="emptyOrbit"){
            var orbit = input_textbox[0];
            filterExpression = presetFilter + "[" + ActualName2Index(orbit,"orbit") + ";;]";
        }else if(presetFilter=="numOrbits"){
            var number = input_textbox[0];
            filterExpression = presetFilter + "[;;" + number + "]";
        }else if(presetFilter=="subsetOfInstruments"){
            // To be implemented
        }else if(presetFilter=="numOfInstruments"){
            var orbit = input_textbox[0];
            var instrument = input_textbox[1];
            var number = input_textbox[2];
            // There are 3 possibilities
            
            var orbitEmpty = false; 
            var instrumentEmpty = false;
            
            if(orbit=="N/A" || orbit.length==0){
                orbitEmpty=true;
            }
            if(instrument=="N/A" || instrument.length==0){
                instrumentEmpty = true;
            }
            if(orbitEmpty && instrumentEmpty){
                // Count all instruments across all orbits
                filterExpression=presetFilter + "[;;" + number + "]";
            }else if(orbitEmpty){
                // Count the number of specified instrument
                filterExpression=presetFilter + "[;" + ActualName2Index(instrument,"instrument") + ";" + number + "]";
            }else if(instrumentEmpty){
                // Count the number of instruments in an orbit
                filterExpression=presetFilter + "[" + ActualName2Index(orbit,"orbit") + ";;" + number + "]";
            }
        }

    }else if(dropdown1==="objective"){
        var collectionName;
        var slotName;
        
        if(dropdown2==="pareto_front"){
            // To be implemented    
            matchedArchIDs = [];
            var filterInput = d3.select("#filter_inputs_div_1").select('.filter_inputs_textbox')[0][0].value;
            d3.selectAll("[class=dot]")[0].forEach(function (d) {
                var rank = parseInt(d3.select(d).attr("paretoRank"));
                if (rank <= +filterInput && rank >= 0){
                    var id = d.__data__.ArchID;
                    matchedArchIDs.push(id);
                }
            });  
            d3.selectAll("[class=dot_highlighted]")[0].forEach(function (d) {
                var rank = parseInt(d3.select(d).attr("paretoRank"));
                if (rank <= +filterInput && rank >= 0){
                    var id = d.__data__.ArchID;
                    matchedArchIDs.push(id);
                }
            });             
        }else if(dropdown2==="science"){

            if(dropdown3==="not_selected"){
                return;
            }else if(dropdown3==="mission"){
               collectionName = "science.MANIFEST.Mission";
            }else if(dropdown3==="measurement"){
                collectionName = "science.REQUIREMENTS.Measurement";
            }else if(dropdown3==="capabilities"){
                collectionName = "science.CAPABILITIES.Manifested_instrument";
            }else if(dropdown3==="stakeholder"){
                collectionName = "science.AGGREGATION.STAKEHOLDER";
            }else if(dropdown3==="objective"){
                collectionName = "science.AGGREGATION.OBJECTIVE";
            }else if(dropdown3==="subobjective"){
                collectionName = "science.AGGREGATION.SUBOBJECTIVE";
            }     
            slotName = dropdown4;
            if(slotName=="not_selected"){return;}
        }else if(dropdown2=="cost"){
            slotName = dropdown3;
            if(slotName=="not_selected"){return;}
            collectionName = "cost.MANIFEST.Mission";
        }else{
            // Not selected
            return;
        }
        
        
        // Examples of feature expressions
        // "{collectionName:gt[0],slotName:"String"}"
        // "{collectionName:gt[0],slotName:[minVal;maxVal]}"
        // "{collectionName:gt[0],slotName:[;maxVal]}"
        // "{collectionName:gt[0],slotName:[;maxVal],slotName:[minVal;]}"
        
        var Fact_condition = input_select[0];
        var Fact_number = input_textbox[0]; 
        var slot_expression="";
        
        for(var i=1;i<input_select.length;i++){
            var current_slotName = d3.select(inputObj[i]).attr('slotName'); 
            var text = input_textbox[i];
            var select = input_select[i];
            var thisExpression = "";

            if(d3.select(inputObj[i]).attr('slotType')==="Double"){
                // Numeric variable
                var slot_condition = select;
                var slot_value = text; 
                if(slot_condition=="not_selected"){
                    return;
                }else if(slot_condition=="gt"){
                    thisExpression = current_slotName + ":[" + slot_value + ";]";
                }else if(slot_condition=="lt"){
                    thisExpression = current_slotName + ":[;" + slot_value + "]";
                }else if(slot_condition=="eq"){
                    thisExpression = current_slotName + ":[" + slot_value + "]";
                }            
            }else{
                // String variable
                var slot_value = select;
                var slot_value_substring = text;
                if(slot_value_substring===null || slot_value_substring===''){
                    thisExpression = current_slotName + ":" + slot_value;
                }else{
                    // Query using substring
                    thisExpression = current_slotName + ":"+ "'" + slot_value_substring + "'";
                }
            }  
            slot_expression = slot_expression + "," + thisExpression;
        }
       
        filterExpression = collectionName + ":" + Fact_condition+"["+ Fact_number + "]" + slot_expression;
    }else{// not selected
        return;
    }

    if(matchedArchIDs===null){
        update_filter_application_status(filterExpression,option);
        $.ajax({
            url: "DrivingFeatureServlet",
            type: "POST",
            data: {ID: "applyFilter",filterExpression:filterExpression, preset:preset},
            async: false,
            success: function (data, textStatus, jqXHR)
            {
                if(data===""){
                    alert("No architecture returned");
                    return;
                }
                matchedArchIDs = JSON.parse(data);
            },
            error: function (jqXHR, textStatus, errorThrown)
            {alert("Error in applying the filter");}
        });
    }

    if(option==="new"){
        cancelDotSelections();
        d3.selectAll('.dot')[0].forEach(function(d){
            var id = d.__data__.ArchID;
            if($.inArray(id,matchedArchIDs)!==-1){
                d3.select(d).attr('class','dot_highlighted')
                            .style("fill", "#20DCCC");
            }
        });
    }else if(option==="add"){
        d3.selectAll('.dot')[0].forEach(function(d){
            var id = d.__data__.ArchID;
            if($.inArray(id,matchedArchIDs)!==-1){
                d3.select(d).attr('class','dot_highlighted')
                            .style("fill", "#20DCCC");
            }
        });
    }else if(option==="within"){
        d3.selectAll('.dot_highlighted')[0].forEach(function(d){
            var id = d.__data__.ArchID;
            if($.inArray(id,matchedArchIDs)===-1){
                d3.select(d).attr('class','dot')
                .style("fill", function (d) {return "#000000";});   
            }
        });     
    }


    if(wrong_arg){
    	alert("Invalid input argument");
    }
    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
}



function update_filter_application_status(inputExpression,option){    
    
    var application_status = d3.select('#filter_application_status');
    var count = application_status.selectAll('.applied_filter').size();
    
    var thisFilter = application_status.append('div')
            .attr('id',function(){
                var num = count+1;
                return 'applied_filter_' + num;
            })
            .attr('class','applied_filter');
    
    thisFilter.append('input')
            .attr('type','checkbox')
            .attr('class','filter_application_activate');
    thisFilter.append('select')
            .attr('class','filter_application_logical_connective')
            .selectAll('option')
            .data([{value:"&&",text:"AND"},{value:"||",text:"OR"}])
            .enter()
            .append("option")
            .attr("value",function(d){
                return d.value;
            })
            .text(function(d){
                return d.text;
            });
    thisFilter.append('div')
            .attr('class','filter_application_expression')
            .text(inputExpression);
    thisFilter.append('button')
            .attr('class','filter_application_delete')
            .text('remove');
    
    
    if(option==="new"){
        // Activate only the current filter
        d3.selectAll('.filter_application_activate')[0].forEach(function(d){
            d3.select(d)[0][0].checked=false;
        })        
        d3.selectAll('.filter_application_expression').style("color","#989898"); // gray
        thisFilter.select('.filter_application_expression').style("color","#000000"); // black
        thisFilter.select('.filter_application_activate')[0][0].checked=true;
        thisFilter.select('.filter_application_logical_connective')[0][0].value="&&";
    }else if(option==="add"){ // or
        thisFilter.select('.filter_application_activate')[0][0].checked=true;
        thisFilter.select('.filter_application_logical_connective')[0][0].value="||";
    }else if(option==="within"){ // and
        thisFilter.select('.filter_application_activate')[0][0].checked=true;
        thisFilter.select('.filter_application_logical_connective')[0][0].value="&&";
    }
    
    thisFilter.select(".filter_application_delete").on("click",function(d){
        var activated = thisFilter.select('.filter_application_activate')[0][0].checked;
        thisFilter.remove();
        if(activated){
            applyComplexFilter();
        }
    });
    
    thisFilter.select('.filter_application_activate').on("change",function(d){
        var activated = thisFilter.select('.filter_application_activate')[0][0].checked;
        thisFilter.select('.filter_application_expression').style("color",function(d){
            if(activated){
                return "#000000"; //black
            }else{
                return "#989898"; // gray
            }
        });
        applyComplexFilter();
    });
    thisFilter.select('.filter_application_logical_connective').on("change",function(d){
        applyComplexFilter();
    });

}


function parse_filter_application_status(){
    var application_status = d3.select('#filter_application_status');
    var count = application_status.selectAll('.applied_filter').size();
    var filter_expressions = [];
    var filter_logical_connective = [];
    application_status.selectAll('.applied_filter')[0].forEach(function(d){
        var activated = d3.select(d).select('.filter_application_activate')[0][0].checked;
        var expression = d3.select(d).select('.filter_application_expression').text();
        var logic = d3.select(d).select('.filter_application_logical_connective')[0][0].value;
        if(activated){
            filter_expressions.push(expression);
            filter_logical_connective.push(logic);
        }
    });
    var filterExpression = "";
    for(var i=0;i<filter_expressions.length;i++){
        if(i > 0){
            filterExpression = filterExpression + filter_logical_connective[i];
        }
        filterExpression = filterExpression + "{" + filter_expressions[i] + "}";
    }
    return filterExpression;
}

function applyComplexFilter(){
    var filterExpression = parse_filter_application_status();
    var matchedArchIDs;
      
    if(filterExpression===""){
        cancelDotSelections();
        return;
    }
    $.ajax({
        url: "DrivingFeatureServlet",
        type: "POST",
        data: {ID: "applyComplexFilter",filterExpression:filterExpression},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
            matchedArchIDs = JSON.parse(data);
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("Error in applying the filter");}
    });
    
    cancelDotSelections();
    d3.selectAll('.dot')[0].forEach(function(d){
        var id = d.__data__.ArchID;
        if($.inArray(id,matchedArchIDs)!==-1){
            d3.select(d).attr('class','dot_highlighted')
                        .style("fill", "#20DCCC");
        }
    });  
    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
}











//
//
//function applyFilter(filterType,filterInput){
//    cancelDotSelections();
//    var wrong_arg = false;
//    var neg = false;
//    if (filterType == "paretoFront"){
//        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
//        	var rank = parseInt(d3.select(d).attr("paretoRank"));
//            if (rank <= +filterInput && rank >= 0){
//                d3.select(d).attr("class", "dot_clicked")
//                            .style("fill", "#20DCCC");
//            }
//        });
//
//    }
//    else if (filterType == "present" || filterType == "absent" || filterType == "inOrbit" || filterType == "notInOrbit" || filterType == "together" || filterType == "togetherInOrbit" || filterType == "separate" || 
//            filterType == "emptyOrbit" || filterType=="numOrbits" || filterType=="subsetOfInstruments"||
//            filtertype == "numOfInstruments"){
//
//        var unClickedArchs = d3.selectAll("[class=dot]")[0].forEach(function (d) {
//            var bitString = d.__data__.bitString;
//            var temp = presetFilter2(filterType,bitString,filterInputs,neg);
//            if(temp==null){
//            	wrong_arg = true;
//            	return;
//            }
//            else if (temp){
//                d3.select(d).attr("class", "dot_clicked")
//                            .style("fill", "#20DCCC");
//            }
//        });
//    } else if(filterType == "defineNewFilter" || (filterType =="not_selected" && userDefFilters.length !== 0)){
//        var filterExpression = d3.select("[id=filter_expression]").text();
//        tmpCnt =0;
//
//        d3.selectAll("[class=dot]")[0].forEach(function(d){
//        	
//            var bitString = d.__data__.bitString;
//            if(applyUserDefFilterFromExpression(filterExpression,bitString)){
//                d3.select(d).attr("class", "dot_clicked")
//                            .style("fill", "#20DCCC");
//            }
//        });
//
//        d3.select("[id=saveFilter]").attr('disabled', null)
//                                    .on("click",saveNewFilter);
//    }
//    else{
//    	
//        for(var k=0 ; k < userDefFilters.length; k++){
//           if(userDefFilters[k].name == filterType){
//                var filterExpression = userDefFilters[k].expression;
//                d3.selectAll("[class=dot]")[0].forEach(function(d){
//                    var bitString = d.__data__.bitString;
//                    if(applyUserDefFilterFromExpression(filterExpression,bitString)){
//                        d3.select(d).attr("class", "dot_clicked")
//                                    .style("fill", "#20DCCC");
//                    }
//                }); 
//           } 
//        }
//    }
//    if(wrong_arg){
//    	alert("Invalid input argument");
//    }
//    d3.select("[id=numOfSelectedArchs_inputBox]").text("" + numOfSelectedArchs());  
//}
//   
//   
//   
//   
//   
//   
//   
//   
//   
   
   
   
   
   
   
   
   
   
   
//
//
//function presetFilter2(filterName,bitString,inputs,neg){
//    var filterInput1;
//    var filterInput2;
//    var filterInput3;
//
//    filterInput1 = inputs[0];
//    if(inputs.length > 1){
//        filterInput2 = inputs[1];
//    }
//    if(inputs.length > 2){
//        filterInput3 = inputs[2];
//    }
//
//    var output;
//    var leng = bitString.length;
//    var norb = orbitList.length;
//    var ninstr = instrList.length;
//
//    if(filterName==="present"){
//        filterInput1 = relabelback(filterInput1);
//        var thisInstr = $.inArray(filterInput1,instrList);
//        if(thisInstr==-1){
//        	return null;
//        }
//        output = false;
//        for(var i=0;i<orbitList.length;i++){
//            if(bitString[ninstr*i+thisInstr]===true){
//                output = true;
//                break;
//            }
//        }
//    } else if(filterName==="absent"){
//        filterInput1 = relabelback(filterInput1);
//        var thisInstr = $.inArray(filterInput1,instrList);
//        if(thisInstr==-1){
//        	return null;
//        }
//        output = true;
//        for(var i=0;i<orbitList.length;i++){
//            if(bitString[ninstr*i+thisInstr]===true){
//                output = false;
//                break;
//            }
//        }
//    } else if(filterName==="inOrbit"){
//        filterInput1 = relabelback(filterInput1);
//        filterInput2 = relabelback(filterInput2);
//        output = false;
//        var thisOrbit = $.inArray(filterInput1,orbitList);
//        var thisInstr = $.inArray(filterInput2,instrList);
//        
//        if(thisInstr==-1 || thisOrbit==-1){
//        	return null;
//        }
//            if(bitString[thisOrbit*ninstr + thisInstr]===true){
//                output = true;
//            }
//    } else if(filterName==="notInOrbit"){
//        filterInput1 = relabelback(filterInput1);
//        filterInput2 = relabelback(filterInput2);
//        output = true;
//        var thisOrbit = $.inArray(filterInput1,orbitList);
//        var thisInstr = $.inArray(filterInput2,instrList);
//        if(thisInstr==-1 || thisOrbit==-1){
//        	return null;
//        }
//            if(bitString[thisOrbit*ninstr + thisInstr]===true){
//                output = false;
//            }
//    } else if(filterName === "together"){
//        output = false;
//        var splitInstruments = filterInput1.split(",");
//        var thisInstr1 = $.inArray(relabelback(splitInstruments[0]),instrList);
//        var thisInstr2 = $.inArray(relabelback(splitInstruments[1]),instrList);
//        var thisInstr3;
//        if(splitInstruments.length===2){
//            if(thisInstr1==-1 || thisInstr2==-1){
//            	return null;
//            }
//            for(var i=0;i<norb;i++){
//                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true){
//                    output = true;
//                    break;
//                }
//            }
//        } else {
//            thisInstr3 = $.inArray(relabelback(splitInstruments[2]),instrList);
//            if(thisInstr1==-1 || thisInstr2==-1 || thisInstr3==-1){
//            	return null;
//            }
//            for(var i=0;i<norb;i++){
//                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true
//                        && bitString[i*ninstr + thisInstr3] === true){
//                    output = true;
//                    break;
//                }
//            }
//        }
//    } else if(filterName === "togetherInOrbit"){
//        output = false;
//        var thisOrbit =  $.inArray(relabelback(filterInput1),orbitList);
//        var splitInstruments = filterInput2.split(",");
//        var thisInstr1 = $.inArray(relabelback(splitInstruments[0]),instrList);
//        var thisInstr2 = $.inArray(relabelback(splitInstruments[1]),instrList);
//        var thisInstr3;
//        if(splitInstruments.length===2){
//            if(thisInstr1==-1 || thisInstr2==-1 || thisOrbit==-1){
//            	return null;
//            }
//            if(bitString[thisOrbit*ninstr + thisInstr1] === true && bitString[thisOrbit*ninstr + thisInstr2] === true){
//                output = true;
//            }
//        } else {
//            thisInstr3 = $.inArray(relabelback(splitInstruments[2]),instrList);
//            if(thisInstr1==-1 || thisInstr2==-1 || thisInstr3==-1 || thisOrbit==-1){
//            	return null;
//            }
//            if(bitString[thisOrbit*ninstr + thisInstr1] === true && bitString[thisOrbit*ninstr + thisInstr2] === true
//                        && bitString[thisOrbit*ninstr + thisInstr3] === true){
//                output = true;
//            }
//        }
//    } else if(filterName ==="separate"){
//        output = true;
//        var splitInstruments = filterInput1.split(",");
//        var thisInstr1 = $.inArray(relabelback(splitInstruments[0]),instrList);
//        var thisInstr2 = $.inArray(relabelback(splitInstruments[1]),instrList);
//        var thisInstr3;
//        if(splitInstruments.length===2){
//            if(thisInstr1==-1 || thisInstr2==-1){
//            	return null;
//            }
//            for(var i=0;i<norb;i++){
//                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true){
//                    output = false;
//                    break;
//                }
//            }
//        } else {
//            thisInstr3 = $.inArray(relabelback(splitInstruments[2]),instrList);
//            if(thisInstr1==-1 || thisInstr2==-1 || thisInstr3==-1){
//            	return null;
//            }
//            for(var i=0;i<norb;i++){
//                if(bitString[i*ninstr + thisInstr1] === true && bitString[i*ninstr + thisInstr2] === true
//                        && bitString[i*ninstr + thisInstr3] === true){
//                    output = false;
//                    break;
//                }
//            }
//        }
//    } else if(filterName ==="emptyOrbit"){
//        var thisOrbit =  $.inArray(relabelback(filterInput1),orbitList);
//        if(thisOrbit==-1){
//        	return null;
//        }
//        output = true;
//        for(var i=0;i<ninstr;i++){
//            if(bitString[thisOrbit*ninstr + i]===true){
//                output=false;
//                break;
//            }
//        }
//    } else if(filterName ==="numOrbits"){
//        var numOrbits = filterInput1;
//        if(numOrbits > 5 || numOrbits < 0){
//        	return null;
//        }
//        var cnt = 0;
//        for (var i=0;i<norb;i++){
//            for (var j=0;j<ninstr;j++){
//                if(bitString[i*ninstr+j]==true){
//                    cnt++;
//                    break;
//                }
//            }
//        }
//        if(cnt==numOrbits){
//            output = true;
//        } else{
//            output= false;
//        }  
//        
//    } else if(filterName === "numOfInstruments"){
//        output = false;
//
//        var all_instruments = false;
//        var thisInstr = -1;
//        
//        var instrument;
//        var num_of_instruments;
//        if(filterInput1.indexOf(",") != -1){
//        	var comma = filterInput1.indexOf(",");
//        	instrument = filterInput1.substring(0,comma);
//        	num_of_instruments = filterInput1.substring(comma+1);
//        }else{
//        	instrument = filterInput1;
//        	num_of_instruments = filterInput2;
//        }
//        
//        if(instrument=="N/A"){
//        	//continue;
//        	all_instruments = true;
//        }
//        else{
//            thisInstr = $.inArray(relabelback(instrument),instrList);
//            if(thisInstr==-1){
//            	return null;
//            }
//        }        
//        var cnt = 0;
//        for(var i=0;i<orbitList.length;i++){
//        	if(all_instruments){
//        		for(var j=0;j<instrList.length;j++){
//                	if(bitString[i*ninstr + j] === true){
//                        cnt = cnt+1;
//                    }
//        		}
//        	}else{
//            	if(bitString[i*ninstr + thisInstr] === true){
//                    cnt = cnt+1;
//                }
//        	}
//        }
//
//            
//        if(num_of_instruments== ""+cnt){
//        	output=true;
//        }else{
//        	output=false;
//        }
//        return checkNeg(output,neg)     
//        
//    } else if(filterName === "subsetOfInstruments"){ 
//        var thisOrbit = $.inArray(relabelback(filterInput1),orbitList);
//        var minmax = filterInput2.split(",");
//        var instruments = filterInput3.split(",");
//
//        var constraint = minmax.length;
//        var numOfInstr = instruments.length;
//
//        var min,max;
//        if(constraint===1){ // only the minimum number of instruments is typed in
//            min = minmax[0];
//            max = 100;
//        } else if(constraint===2){
//            min = minmax[0];
//            max = minmax[1];
//        }
//
//        var size = instruments.length;
//        var cnt=0;
//
//        for(var i=0;i<size;i++){ //var thisInstr1 = $.inArray(splitInstruments[0],instrList);
//            var thisInstr = $.inArray(relabelback(instruments[i]),instrList);
//            if(bitString[thisOrbit*ninstr + thisInstr]===true){
//                cnt++;
//            }
//        }
//        if(cnt <= max && cnt >= min){
//            output = true;
//        }else{
//            output = false;
//        }
//    }
//
//    return checkNeg(output,neg)
//}
//
