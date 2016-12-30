

           
function attribute_score_summary_request(thisSubobj){
    
    var jsonObj_attrScores  
    $.ajax({      
        url: "resultsGUIServlet",          
        type: "POST",        
        data: {ID: "attributeScoreSummaryRequest", subobj: thisSubobj},
        async: false,
                    
        success:function(data, textStatus, jqXHR)       
        {   
            jsonObj_attrScores = JSON.parse(data);
            reset_attribute_summary_table();
            draw_attribute_summary_table(jsonObj_attrScores);
        },    
        error: function(jqXHR, textStatus, errorThrown)          
        {            
            alert("error");      
        }      
    });
}

  
function reset_attribute_summary_table(){
    d3.select("#attribute_score_summary_box")
                                        .select("g")
                                        .remove();
}      
            
function draw_attribute_summary_table(source){          
    var attributeSummary = d3.select("#attribute_score_summary_box")
                                        .append("g");
                
    var table = attributeSummary.append("table")
                                 .attr("id","attribute_score_summary_table");
    
    var numOfAttrs = source[0].length;
    var header = [],
        thresholds = [], 
        referenceScores = [];
 
    header.push({columnName:"Taken-by"});         
//    header.push({columnName:"taken-by"});
//    thresholds.push({columnName:""});
    thresholds.push({columnName:"{Thresholds}"});
//    referenceScores.push({columnName:""});
    referenceScores.push({columnName:"{Reference Scores}"});
    
    for (var i=0;i<numOfAttrs;i++){                
        header.push({columnName: source[0][i].attrName});
        thresholds.push({columnName: "[" + source[0][i].thresholds + "]"});
        referenceScores.push({columnName: "["+ source[0][i].referenceScores + "]"});
    }
                    // create table header
    table.append('thead').append('tr')
                        .selectAll('th')
                        .data(header).enter()
                        .append('th')
                        .text(function(d){return d.columnName;});
             
    table.select('thead').append('tr')         
                        .selectAll('th')
                        .data(thresholds).enter()
                        .append('th')
                        .text(function(d){return d.columnName;});
    table.select('thead').append('tr')         
                        .selectAll('th')
                        .data(referenceScores).enter()
                        .append('th')
                        .text(function(d){return d.columnName;});
                
    // create table body
    table.append('tbody')      
            .selectAll('tr')
            .data(source).enter()       
            .append('tr')
            .selectAll('td')    
            .data(function(row,i) {
                            var thisRow = [];
                            var obj = {text:source[i][0].instrument, archNum:source[i][0].archNum};
                            thisRow.push(obj);
                            for (var j=0;j<source[0].length;j++){
                                var obj = {text:source[i][j].actualValue + " (" + source[i][j].attrScore +  ")", archNum:source[i][j].archNum};
                                thisRow.push(obj);
                            }
                            return thisRow;
                        }).enter()        
            .append('td')      
            .text(function(d){   
                return d.text;
            });
                
                
}
            
            
