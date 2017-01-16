/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



function selectNewFilterOption(){


    d3.select("[id=filter_input1]").remove();     
    d3.select("[id=filter_input2]").remove();
    d3.select("[id=filter_input3]").remove();
    d3.select('#filter_explanation_valid_inputs').remove();
    d3.selectAll("[class=newFilterButtons]").remove();
        
    var selectedOption = d3.select("[id=dropdown_newFilterOption]")[0][0].value;     
    selectFilterOption_filterInput(selectedOption,true);
                                
    var filterInput = d3.select("[id=filter_inputs]");
                    
                    
    filterInput.append("button")
            .attr("class","newFilterButtons")
            .attr("id","newFilter_add") 
            .style("margin-top","10px")
            .style("float","left")
            .text("Add this feature");
                    filterInput.append("button")
                            .attr("id","newFilter_openParen")
                            .attr("class","newFilterButtons")
                            .style("margin-top","10px")
                            .style("margin-left","5px")
                            .style("float","left")
                            .text("Open parenthesis");
                    filterInput.append("button")
                            .attr("id","newFilter_closeParen")
                            .attr("class","newFilterButtons")
                            .style("margin-top","10px")
                            .style("margin-left","5px")
                            .style("float","left")
                            .text("Close parenthesis");
                    filterInput.append("button")
                            .attr("id","newFilter_and")
                            .attr("class","newFilterButtons")
                            .style("margin-top","10px")
                            .style("margin-left","5px")
                            .style("float","left")
                            .text("AND");
                    filterInput.append("button")
                            .attr("id","newFilter_or")
                            .attr("class","newFilterButtons")
                            .style("margin-top","10px")
                            .style("margin-left","5px")
                            .style("float","left")
                            .text("OR");
                    filterInput.append("button")
		                    .attr("id","newFilter_not")
		                    .attr("class","newFilterButtons")
		                    .style("margin-top","10px")
		                    .style("margin-left","5px")
		                    .style("float","left")
		                    .text("NOT");
                    filterInput.append("button")
                            .attr("id","newFilter_back")
                            .attr("class","newFilterButtons")
                            .style("margin-top","10px")
                            .style("margin-left","5px")
                            .style("float","left")
                            .text("Undo");
                    
                    
    d3.select("[id=newFilter_add]").on("click",newFilter_addFeature);       
    d3.select("[id=newFilter_openParen]").on("click",newFilter_openParen);      
    d3.select("[id=newFilter_closeParen]").on("click",newFilter_closeParen);        
    d3.select("[id=newFilter_and]").on("click",newFilter_and);               
    d3.select("[id=newFilter_or]").on("click",newFilter_or);
    d3.select("[id=newFilter_not]").on("click",newFilter_not);     
    d3.select("[id=newFilter_back]").on("click",newFilter_back);         
}

function newFilter_addFeature(){
                    
                    var selectedOption = d3.select("[id=dropdown_newFilterOption]")[0][0].value;
                    var filterInput1 = "";
                    var filterInput2 = "";
                    var filterInput3 = "";
                    if(d3.select("[id=filter_input1_textBox]")[0][0]==null){
                        filterInput1="Error: no input";
                    } else{
                        filterInput1 = d3.select("[id=filter_input1_textBox]")[0][0].value;
                    }
                    if(d3.select("[id=filter_input2_textBox]")[0][0]==null){
                        filterInput2="";
                    } else{
                        filterInput2 = ";" + d3.select("[id=filter_input2_textBox]")[0][0].value;
                    }
                    if(d3.select("[id=filter_input3_textBox]")[0][0]==null){
                        filterInput3="";
                    } else{
                        filterInput3 = ";" + d3.select("[id=filter_input3_textBox]")[0][0].value;
                    }
                    
                    var filterExpression = d3.select("[id=filter_expression]");
                    var prevExpression = filterExpression.text();
                    
                    if (prevExpression.length==0 || prevExpression.charAt(prevExpression.length-1)=="&" || prevExpression.charAt(prevExpression.length-1)=="|" 
                    	|| prevExpression.charAt(prevExpression.length-1)=="{" || prevExpression.charAt(prevExpression.length-1)=="~"){
                    	
                    	
                    	if(selectedOption == "present" || selectedOption == "absent" || selectedOption == "inOrbit" || 
                    			selectedOption == "notInOrbit" || selectedOption == "together" || 
                    			selectedOption == "togetherInOrbit" || selectedOption == "separate" || 
                    			selectedOption == "emptyOrbit" || selectedOption=="numOrbitUsed" || 
                    			selectedOption=="subsetOfInstruments" ||
                    			selectedOption=="numOfInstruments"){
                    		
                    		 var newExpression = prevExpression + selectedOption + "(" + filterInput1 + filterInput2 + filterInput3 + ")";

                    	}else{
                    		var expression
                            for(var i=0;i<userDefFilters.length;i++){
                                if(userDefFilters[i].name==selectedOption){
                                    expression = userDefFilters[i].expression;
                                }
                            }
                    		var newExpression = prevExpression + "{" + expression + "}";
                    	}
                    	
                        userDefFilterExpressionHistory.push(newExpression);
                        filterExpression.text(newExpression);
                        
                    }else{
                        filterExpression.text("Error: logic symbol needed");
                        userDefFilterExpressionHistory.push("error");
                    }
                    d3.select("[id=saveFilter]").attr('disabled', true);
}
                
function newFilter_openParen(){
                    var filterExpression = d3.select("[id=filter_expression]");
                    var prevExpression = filterExpression.text();
                    var newExpression = prevExpression + "{";
                    userDefFilterExpressionHistory.push(newExpression);
                    filterExpression.text(newExpression);
                    d3.select("[id=saveFilter]").attr('disabled', true);
}
                
function newFilter_closeParen(){
                    var filterExpression = d3.select("[id=filter_expression]");
                    var prevExpression = filterExpression.text();
                    var newExpression = prevExpression + "}";
                    userDefFilterExpressionHistory.push(newExpression);
                    filterExpression.text(newExpression);
                    d3.select("[id=saveFilter]").attr('disabled', true);
}
                
function newFilter_and(){
                    var filterExpression = d3.select("[id=filter_expression]");
                    var prevExpression = filterExpression.text();
                    var newExpression = prevExpression + "&&";
                    userDefFilterExpressionHistory.push(newExpression);
                    filterExpression.text(newExpression);
                    d3.select("[id=saveFilter]").attr('disabled', true);
}
                
                
function newFilter_or(){
                    var filterExpression = d3.select("[id=filter_expression]");
                    var prevExpression = filterExpression.text();
                    var newExpression = prevExpression + "||";
                    userDefFilterExpressionHistory.push(newExpression);
                    filterExpression.text(newExpression);
                    d3.select("[id=saveFilter]").attr('disabled', true);
}
function newFilter_not(){
    var filterExpression = d3.select("[id=filter_expression]");
    var prevExpression = filterExpression.text();
    var newExpression = prevExpression + "~";
    userDefFilterExpressionHistory.push(newExpression);
    filterExpression.text(newExpression);
    d3.select("[id=saveFilter]").attr('disabled', true);
}      


function newFilter_back(){
                    userDefFilterExpressionHistory.pop();
                    var leng = userDefFilterExpressionHistory.length;
                    var last = userDefFilterExpressionHistory[leng-1];
                    var filterExpression = d3.select("[id=filter_expression]");
                    filterExpression.text(last);
                    d3.select("[id=saveFilter]").attr('disabled', true);
                
}


function applyUserDefFilterFromExpression(filterExpression,bitString){ // bitString: boolean array
                   // present(DESD_SAR)&&inOrbit(SSO-600-SSO-PM;CNES_KaRIN)
//                   something(arg)&&{{present(DESD_SAR)&&inOrbit(SSO-600-SSO-PM)}||inOrbit(SSO-600_SSO-AM)}
    
    var output;                
    var fe = filterExpression;   
    
    for(var k=0;k<10;k++){                 
        if(fe.indexOf("{")!=-1){           
            var cnt =0;
            var level=0;
            var maxLevel = 0;
            var maxLevelLoc = [];
            for(var i=0;i<fe.length;i++){
                var char = fe.charAt(i);
                if(char==="{"){
                    level++;
                    if(level > maxLevel) {
                        maxLevel++;
                    }
                } else if(char==="}"){
                    level--;
                }
            }
            level=0;
            for (var i=0;i<fe.length;i++){
                var char = fe.charAt(i);
                if(char==="{"){
                    level++;
                    if(level === maxLevel) {
                        maxLevelLoc.push(i);
                    }
                } else if(char==="}"){
                    level--;
                }
            }
            for (var i =0;i<maxLevelLoc.length;i++){  // abc{abcdef}         
                var currentMaxLoc;
                level=0;
                for (var j=0;j<fe.length;j++){
                    var char = fe.charAt(j);
                    if(char=="{"){
                        level++;
                        if(level == maxLevel) {
                            currentMaxLoc = j;break;
                        }
                    } else if(char=="}"){
                        level--;
                    }     
                } 
                var innermostParenLoc = currentMaxLoc;
                var innermostExpression = fe.substring(innermostParenLoc+1); 
                var innermostParenEndLoc = innermostExpression.indexOf("}");
                innermostExpression = innermostExpression.substring(0,innermostParenEndLoc);
                var tmp = applyUserDefFilter_withoutParen(innermostExpression,bitString);
                var fe1,fe2;
                if(innermostParenLoc==0){ fe1 = "";}
                else {fe1 = fe.substring(0,innermostParenLoc);}
                if(innermostParenLoc + 1 + innermostExpression.length + 1 == fe.length) {fe2="";}
                else {fe2 = fe.substring(innermostParenLoc + innermostExpression.length + 2);}

                if(tmp==true){
                    fe = fe1 + "true()" + fe2;
                } else {
                    fe = fe1 + "false()" + fe2;
                }
            }
        } else {
            output = applyUserDefFilter_withoutParen(fe,bitString);
            break;
        }
    }
    return output;
}
                
                

function applyUserDefFilter_withoutParen(filterExpression,bitString){
    var output = true;
    var fe = filterExpression;
    var connection = "and";
    
    while(true){
        var andLoc = fe.indexOf("&&");
        var orLoc = fe.indexOf("||");
        var firstLogicLoc;
        var nextConn;
        if (andLoc === -1 && orLoc===-1){ //  no logic expression used: single feature
            var thisFilter = fe; //apply filter
            output = applyUserDefFilter_single(thisFilter,output,connection,bitString);
            break;
        } else{ 
            if(andLoc === -1){
                firstLogicLoc = orLoc;
                nextConn = "or";
            } else if(orLoc === -1){
                firstLogicLoc = andLoc;
                nextConn = "and";
            } else if(andLoc < orLoc){
                firstLogicLoc = andLoc;
                nextConn = "and";
            } else if(orLoc < andLoc){
                firstLogicLoc = orLoc;
                nextConn = "or";
            }
            var thisFilter = fe.substring(0,firstLogicLoc); // apply filter
            output = applyUserDefFilter_single(thisFilter,output,connection,bitString);
            var fe = fe.substring(firstLogicLoc+2); // rest of the expression
        }
        connection = nextConn;
    }
    return output;
}
                
function applyUserDefFilter_single(expression,prev,logic,bitString){
//                    inOrbit(SSO-600-SSO-PM;CNES_KaRIN)
    var paren1 = expression.indexOf("(");
    var paren2 = expression.indexOf(")");
    var param = expression.substring(paren1+1,paren2);
    var filterType = expression.substring(0,paren1);
    var params = [];
    var output;
    params.length=0;
    if(param.indexOf(";")===-1){
        params.push(param);
    } else{
        while(param.indexOf(";")!==-1){
            var semicolon = param.indexOf(";");
            params.push(param.substring(0,semicolon));
            param = param.substring(semicolon+1);
        }
        params.push(param)
    }
    
    if(filterType==="true" || filterType=="~false"){
        output = true;
    } else if(filterType==="false" || filterType=="~true"){
        output = false;
    } 
    else if (filterType === "paretoFront"){
        var paretoRank = params[0];
        var a = d3.selectAll("[class=dot]")[0].filter(function(d){
            if(d3.select(d).attr("paretoRank")!== ""+paretoRank){
                return false;
            }
            var thisBitString = d.__data__.bitString;
            for(var i=0;i<thisBitString.length;i++){
                if(thisBitString[i]!==bitString[i]){
                    return false;
                }
            }
            return true;
        });
        var b = d3.selectAll("[class=dot_clicked]")[0].filter(function(d){
            if(d3.select(d).attr("paretoRank")!== ""+paretoRank){
                return false;
            }
            var thisBitString = d.__data__.bitString;
            for(var i=0;i<thisBitString.length;i++){
                if(thisBitString[i]!==bitString[i]){
                    return false;
                }
            }
            return true;
        });
        if(a.length===0 && b.length===0){
            output = false;
        } else{
            output = true;
        }
    } else {

    	var neg = false;
    	if(filterType.indexOf("~")!=-1){
    		filterType = filterType.substring(1);
    		neg = true;
    	}
    	if(filterType === "present" || filterType === "absent" || 
                filterType === "inOrbit" || filterType === "notInOrbit" || 
                filterType === "together" || filterType === "togetherInOrbit" || 
                filterType === "separate" || filterType === "emptyOrbit" || 
                filterType==="numOrbitUsed" || filterType==="subsetOfInstruments"||
                filterType==="numOfInstruments"){
			if(presetFilter2(filterType,bitString,params,neg)){
				output = true;
			} else{
				output = false;
			}
    	}
    	
    }
    

    if(prev==output){
        if(prev==true){
            return true;
        } else{
            return false;
        }
    } else{
        if(logic=="and"){
            return false;
        } else{ // logic==or
            return true;
        }
    }
}
                




function saveNewFilter(){
    var name = d3.select("[id=userDefinedFilter_name]")[0][0].value;
    
    if(name.length==0){
    	var filterExpression = d3.select("[id=filter_expression]");
        filterExpression.text("Error: name required to save!");
        userDefFilterExpressionHistory.push("error");
    	return;
    }
    
    var filterExpression = d3.select("[id=filter_expression]").text();
    userDefFilters.push({name:name,expression:filterExpression});
    console.log(name + " " + filterExpression);
    filterDropdownOptions.push({value:name,text:name});
    
    var filterDropdownMenu = d3.select("[id=dropdown_presetFilters]");
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

    buttonClickCount_addUserDefFilter += 1;
}

function calculateLiftSuppConf2(feature_selected,feature_non_selected,selected,non_selected){
    var total = selected+non_selected;
    var feature = feature_selected+feature_non_selected;
    var lift = (feature_selected/selected)/(feature/total);
    var supp = feature_selected/(total);
    var conf = feature_selected/(feature);
    var conf2 = feature_selected/(selected);
    return {lift:lift,supp:supp,conf:conf,conf2:conf2};
}

function calculateLiftSuppConf(filtered){
    var selected = d3.selectAll("[class=dot_clicked]")[0];
    var not_selected = d3.selectAll("[class=dot]")[0];
//    ((double) count_focus / focusData.size()) / ((double) count_random / randomData.size());
    var cnt=0;
    for(var i=0;i<filtered.length;i++){
        for(var j=0;j<selected.length;j++){
            if(filtered[i].__data__.science==selected[j].__data__.science && filtered[i].__data__.cost == selected[j].__data__.cost){
                if(booleanArray2String(filtered[i].__data__.bitString)==booleanArray2String(selected[j].__data__.bitString)){
                    cnt++;
                    break;
                }
            }
        }
    }
    var lift = (cnt/selected.length)/(filtered.length/(selected.length+not_selected.length));
    var supp = cnt/(selected.length+not_selected.length);
    var conf = cnt/(filtered.length);
    return {lift:lift,supp:supp,conf:conf};
}
                
