
            var margin_factHis = {top: 15, right: 10, bottom: 0, left: 10};
            var width_factHis = 1250 - margin_factHis.left - margin_factHis.right;        
            var height_factHis = 550 - margin_factHis.bottom - margin_factHis.top;
            var ruleVisualizerRectSize = {width:600, height:550};
            var jsonObj_factHis;
            var tree_factHis = d3.layout.tree()
                            .nodeSize([0, 20]);
            var i_factHis = 0,
                duration_factHis = 3,
                duration_fast = 50,
                duration_slow = 100,
                root_factHis;
        
            var mouseXLoc_tmp=0,
                mouseYLoc_tmp=0;
        
            var linkArrowLoc = [[]];
            var linkArrows;
        
            var barHeight_factHis = 25,
                barWidth_rule = 45,
                barWidth_fact = 80;
            
            var factArray = [];  // array of all the fact IDs
            var factArrayLoc = [[]];  // store the location of each fact
            var ruleArray = [[]]; //  array of all the rule IDs
            var factTypeArray = [];
            var factTypeCountArray=[];
            
            var node_factHis,
                nodes_factHis,
                nodeEnter_factHis;
                
        
            var link,
                linkEnter,
                diagonal;

            var fr_dist = 30;  // distance between a fact and a rule in y direction 
            var ff_dist = 150; // distance between a fact and another fact in y direction
            var expandableFacts = [];
            var expandableFactsLoc = [[]];
           
            var color_factHis = d3.scale.category10();
            var tooltip;
            
            var dragStart_x=0;
            var dragStart_y=0;
            var newX;
            var newY;
            var nth_f_drag;
            
            var current_scale_factHis=1;

            var highlightedFactID = 0;
            var fix_rule_visualizer = null;




            
function cValue(n) {
    if (n.type == "rule"){
        var ruleName = get_ruleName(n.ID);
        var module = ruleName.split("::")[0];
        return module;
    } else       
        return "Facts";
}
            

function factHistory_figure_request(subobj_id){
    var jsonObj_factHis;
    $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {ID:"factHistoryFigureRequest",subobj:subobj_id, factID:""},
        success:function(data, textStatus, jqXHR) 
        {
            jsonObj_factHis = JSON.parse(data);
            console.log(jsonObj_factHis);
        },
        complete: function(){
            reset_drawing();
            draw_factHistory_diagram(jsonObj_factHis);
            remove_unnecessary_nodes();
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
        }
    });
}
            


try {

function reset_drawing(){
    d3.select("#factHistoryFigure").selectAll("svg").remove();
    d3.select("#factHistoryFigure").selectAll("div").remove();
    factArray.length=0;
    factArrayLoc.length=0;
    ruleArray.length=0;
    expandableFacts.length=0;
    expandableFactsLoc.length=0;
    factTypeArray.length=0;
    factTypeCountArray.length=0;
    linkArrowLoc.length=0;
}    



function draw_factHistory_diagram(source){

    nodes_factHis = initialize_nodes(source,0,0);
    var allExpanded = false;
    while (allExpanded == false){
        allExpanded = update_nodes();
    }


    var svg = d3.select("#factHistoryFigure")
                .append("svg")
                .attr("width",width_factHis)
                .attr("height",height_factHis)
                .append("g")
                .attr("transform", "translate(" + margin_factHis.left + "," + margin_factHis.top + ")");

    node_factHis = svg.append("g").attr("id","nodeGroup")
                .selectAll("g.node")
                .data(nodes_factHis, function(d) { return d.id || (d.id = ++i_factHis); });

    diagonal = d3.svg.diagonal()
                        .projection(function(d) { return [d.x, d.y]; });    

    nodeEnter_factHis = node_factHis.enter().append("g")
                            .style("opacity", 1)
                            .attr("name",function(d){
                                if (d.type=="fact"){
                                    return "F"+d.ID;
                                } else{
                                    return "R"+d.ID;
                                }
                            })
                            .attr("parent",function(d){
                                if (d.type=="rule"){
                                    return "F"+d.parent.ID;
                                } else{
                                    return "NA";
                                }
                            })
                            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
                            .attr("class", "node");


    var factNodes = nodeEnter_factHis.filter(function(d){
                                if(d.type == "fact"){
                                    return true;
                                } else {return false;}
                            })
                            .attr("collapse_rules",true)
                            .call(dragListener)
                            .on("click",click_highlight);


    var ruleNodes = nodeEnter_factHis.filter(function(d){
                    if(d.type == "rule"){
                        return true;
                    } else {return false;}
                });

    var collapse_rule_button = factNodes.append("rect")
            .attr("width",10)
            .attr("height",barHeight_factHis)
            .style("fill","#9D9D9D")
            .attr("transform", "translate(" + barWidth_fact + "," + 0 + ")")
            .on("click",click_collapse_rules);

    factNodes.append("rect")
            .attr("id","factTextBox")
            .attr("width", barWidth_fact)
            .attr("height",barHeight_factHis)
            .style("fill", function(d){return color_factHis(cValue(d));});


    factNodes.append("text")
            .attr("dy", 18)
            .attr("dx", 5)        
            .text(function(d) { 
                var factName = get_factName(d.ID)
                var module = factName.split("::")[0];
                var name = factName.split("::")[1];
                return module.substring(0,3) + "::" + name.substring(0,3);
            });


      // Update the links…
    link = svg.append("g")
                  .attr("id","linkGroup")
                  .selectAll("path.link")
                  .data(tree_factHis.links(nodes_factHis), function(d) {return d.target.id;});

    // Enter any new links at the parent's previous position.
    linkEnter = link.enter().insert("path", "g")
                .filter(function(d){
                    if(d.source.type == "fact"){
                        return false;
                    } else if (d.source.parent.ID == d.target.ID){
                        return false;
                    }
                    else {return true;}
                })
                .attr("class", "link")
                .attr("name","link")
                .attr("d", function(d) {

                    var sx = d.source.x + barWidth_fact/2,
                        sy = d.source.y + barHeight_factHis,
                        tx = d.target.x + barWidth_fact/2,
                        ty = d.target.y;

                    if (d.source.y > d.target.y){
                        sy = d.source.y;
                        ty = d.target.y + barHeight_factHis;
                    }

                    var s = {x: sx, y: sy};
                    var t = {x: tx, y: ty};
                    return diagonal({source: s, target: t});
                });



//        
//                    d3.selectAll("[class=link]")[0]   // generate arrow locations
//                            .forEach(function(n,i){
//                                var leng = d3.selectAll("[class=link]")[0][i].getTotalLength();
//                                var pathPoint = d3.selectAll("[class=link]")[0][i].getPointAtLength(leng*0.05);
//                                var targetPoint = d3.selectAll("[class=link]")[0][i].getPointAtLength(0);
//                                var source = n.source;
//                                var target = n.target;
//                                var arrowPoints = {x0:pathPoint.x, x1: targetPoint.x, y0:pathPoint.y, y1:targetPoint.y, source:source, target:target};
//                                linkArrowLoc.push(arrowPoints);
//                    });
//                   
//
//                    linkArrows = d3.select("[id=linkGroup]")
//                            .selectAll("[class=arrow]")
//                            .data(linkArrowLoc)
//                            .enter() 
//                            .append("path")
//                            .attr("class","arrow")
//                            .attr("d", d3.svg.symbol().type("triangle-down")(10,1))
//                            .attr("transform", function(d,i){
//                                
//                                    var x0 = d.x0;
//                                    var x1 = d.x1;
//                                    var y0 = d.y0;
//                                    var y1 = d.y1;
//                                    var source = d.source;
//                                    var target = d.target;
//                                
//                                    if (Math.abs(x1-x0 < 0.02)){
//                                        if(y1 - y0 > 0 ){
//                                            return "translate(" + [x0, y0] + ")";
//                                        }else{
//                                            return "translate(" + [x0, y0] + ")rotate("+ 180  +")";
//                                        }
//                                        
//                                    } else {
//                                        if(y1 - y0 > 0){
//                                            var slope_rad = Math.atan(      (x1-x0)/(y1-y0)    );
//                                            var slope_ang = slope_rad * 180 / Math.PI;
//                                            return "translate(" + [x0, y0] + ")rotate("+ slope_ang  +")";
//                                        }
//                                        else {
//                                            var slope_rad = Math.atan(     -(x1-x0)/(y1-y0)    );
//                                            var slope_ang = slope_rad * 180 / Math.PI;
//                                            return "translate(" + [x0, y0] + ")rotate("+ 180 - slope_ang  +")";
//                                        }
//                                    }
//                            });


    // draw legend
    var legend = svg.selectAll(".legend")
                    .data(color_factHis.domain())
                    .enter().append("g")
                    .attr("class", "legend")
                    .attr("transform", function(d, i) { return "translate(0," + (i * 20) + ")"; })
                    .call(legendDragListener);

        // draw legend colored rectangles
    legend.append("rect")
            .attr("x", 12*barWidth_fact)
            .attr("width", 18)
            .attr("height", 18)
            .style("fill", color_factHis);

        // draw legend text
    legend.append("text")
            .attr("x", 12*barWidth_fact)
            .attr("y", 9)
            .attr("dy", ".35em")
            .style("text-anchor", "end")
            .text(function(d) { return d;});

    d3.select("[id=factHistoryFigure]").select("svg").call(zoom);


    ruleNodes.on("mouseover",function(n){
        var thisRuleID = d3.select(this).attr("name").substring(1);
//                        var ppdefrule = get_ppdefrule(thisRuleID);
//                        d3.select("[id=chatBox2]").text(get_ruleObj_json(thisRuleID));
//  
        var jsonObj_rule = JSON.parse(get_ruleObj_json(thisRuleID));
        rule_visualizer(jsonObj_rule);

    })
    .on("mouseout", rule_visualizer_out);


    var svg = d3.select("[id=factHistoryFigure]")
                .append("svg")
    var ruleVis = d3.select("[id=factHistoryFigure]").select("svg")
            .append("g")
            .attr("id","ruleVisualizer");

    ruleVis.append("rect")
        .attr("id","ruleVisRect_right")    
        .attr("transform", function(){
            var x = width_factHis + margin_factHis.left + margin_factHis.right;
            var y = 0;
            return "translate(" + x + "," + y + ")";
        })
        .attr("width",0)
        .attr("height",ruleVisualizerRectSize.height)
        .style("fill","#4B4B4B")
        .style("opacity", 0.93);

    ruleVis.append("rect")
        .attr("id","ruleVisRect_left")    
        .attr("transform", function(){
            var x = 0;
            var y = 0;
            return "translate(" + x + "," + y + ")";
        })
        .attr("width",0)
        .attr("height",ruleVisualizerRectSize.height)
        .style("fill","#4B4B4B")
        .style("opacity", 0.93);    

}


    ////////////////////////////////////////////////////////////////
    ///////////////////// Interactive part  ////////////////////////


    var zoom = d3.behavior.zoom()
                        .on("zoom", function(){
                            current_scale_factHis = d3.event.scale;
                            d3.select("[id=factHistoryFigure]").select("svg").select("g")
                            .attr("transform", "translate(" + d3.event.translate + ")scale(" + current_scale_factHis + ")");
                        });


    var dragStart_x_legend,
        dragStart_y_legend = [];
    var legendLoc_x = 0;
    var legendLoc_y = 0;
    var newX_legend=0;
    var newY_legend=0;

    var legendDragListener = d3.behavior.drag()
            .on("dragstart",function(n,i){
                d3.event.sourceEvent.stopPropagation();
                d3.select(this).classed("dragging", true); 

                dragStart_x_legend = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[0] - 3.5*barWidth_fact;
                dragStart_y_legend[i] =  d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[1] + (i*20);


            })
            .on("drag",function(n,i){   
                d3.selectAll(".legend").attr("transform", function(d,i){      // moving the main factNode

                                            var x_diff = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[0] - 3.5*barWidth_fact - dragStart_x_legend;
                                            var y_diff = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[1] + (i*20) - dragStart_y_legend[i];
                                            newX_legend = legendLoc_X + x_diff/current_scale_factHis;
                                            newY_legend = legendLoc_Y + y_diff/current_scale_factHis;
                                            return "translate(" + [ newX_legend, newY_legend ] + ")";
                                        });
            })
            .on("dragend",function(d,i){
                d3.select(this).classed("dragging", false);
                legendLoc_x = newX_legend;
                legendLoc_y = newY_legend;
            });




    var dragListener = d3.behavior.drag()
            .on("dragstart",function(n){
                d3.event.sourceEvent.stopPropagation();
                d3.select(this).classed("dragging", true);
                dragStart_x = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[0];
                dragStart_y = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[1];
                nth_f_drag = $.inArray(n.ID,factArray);

                newX = factArrayLoc[nth_f_drag][0];
                newY = factArrayLoc[nth_f_drag][1];
            })
            .on("drag",function(n){

                var nth_r;
                var rulerow = ruleArray[nth_f_drag];

                var x_diff = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[0] - dragStart_x;
                var y_diff = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[1] - dragStart_y;

                var prevX = factArrayLoc[nth_f_drag][0];
                var prevY = factArrayLoc[nth_f_drag][1];

                newX = prevX + x_diff/current_scale_factHis;
                newY = prevY + y_diff/current_scale_factHis;

                var collapse_rules = d3.select(this).attr("collapse_rules");

                d3.select(this).attr("transform", function(d,i){      // moving the main factNode
                        return "translate(" + [ newX, newY ] + ")";
                });


                d3.selectAll("[parent=F"+ n.ID +"]").attr("transform", function(d,i){ // moving ruleNodes
                    if (collapse_rules == "true"){
                        return "translate(" + [newX,newY] + ")";
                    } else{
                        var nth_r = $.inArray(d.ID,rulerow);
                        var x = newX;
                        var y = newY;
                        return "translate(" + [x,y] + ")";
                    }

                });

                var affectedLinks = linkEnter
                        .filter(function(d){
                            if (n.ID == d.target.ID){  // target fact moves
                                return true;
                            } else if ($.inArray(d.source.ID,rulerow) > -1 && d.source.parent.ID == n.ID){ // source rule moves
                                return true;
                            } else {
                                return false;
                            }
                        });

                affectedLinks.transition()
                        .duration(duration_factHis)
                        .attr("d", function(d){

                            nth_r = $.inArray(d.source.ID,rulerow);

                            if (d.target.ID == n.ID){ // target (fact) is moving

                                var source_collapse_rules = d3.select("[name = F" + d.source.parent.ID + "]").attr("collapse_rules");
                                var nth_sourceFact = $.inArray(d.source.parent.ID,factArray);
                                var sourceFactLoc = factArrayLoc[nth_sourceFact];
                                var sx,sy,tx,ty;

                                if (source_collapse_rules == "true"){
                                    sx = sourceFactLoc[0] + barWidth_fact/2;
                                    sy = sourceFactLoc[1] + barHeight_factHis;
                                } else {
                                    var sourceRuleRow = ruleArray[nth_sourceFact];
                                    var nth_sourceRule = $.inArray(d.source.ID,sourceRuleRow);  // finding the location of the source rule
                                    sx = sourceFactLoc[0] + nth_sourceRule *(barWidth_rule+3) + barWidth_rule/2;
                                    sy = sourceFactLoc[1] + fr_dist + barHeight_factHis;
                                }

                                tx = newX + barWidth_fact/2,
                                ty = newY;

                                if (sy > ty){
                                    sy = sy - barHeight_factHis;
                                    ty = ty + barHeight_factHis;
                                }

                                var s = {x: sx, y: sy};
                                var t = {x: tx, y: ty};
                                return diagonal({source: s, target: t});

                            } else if ( nth_r > -1 && d.source.parent.ID == n.ID){ // source (rule) is moving

                                var nth_targetFact = $.inArray(d.target.ID,factArray);
                                var targetFactLoc = factArrayLoc[nth_targetFact];
                                var sx,sy,tx,ty;

                                if (collapse_rules == "true"){
                                    sx = newX + barWidth_fact/2;
                                    sy = newY + barHeight_factHis;
                                } else{
                                    sx = newX + nth_r *(barWidth_rule+3) + barWidth_rule/2;
                                    sy = newY + fr_dist + barHeight_factHis;
                                }

                                tx = targetFactLoc[0] + barWidth_fact/2,
                                ty = targetFactLoc[1];

                                if (sy > ty){
                                    sy = sy - barHeight_factHis;
                                    ty = ty + barHeight_factHis;
                                }

                                var s = {x: sx, y: sy};
                                var t = {x: tx, y: ty};
                                return diagonal({source: s, target: t});
                            }

                        });
            })
            .on("dragend",function(d){
                d3.select(this).classed("dragging", false);
                factArrayLoc[nth_f_drag][0] = newX;  // save the location of the new target or source fact
                factArrayLoc[nth_f_drag][1] = newY;
            });

} catch(err) {
    console.log(err.message);
}

try{



function click_collapse_rules(d){

    if (d3.select("[id=inspectionMode_button]").attr("class") == "toggle-button-selected"){
        return function(d){
            // do nothing
        };
    }

        var factID = d.ID;
        var nth_fact = $.inArray(factID,factArray);
        var factLoc = factArrayLoc[nth_fact];
        var x_init = factLoc[0];
        var y_init = factLoc[1];
        var ruleRow = ruleArray[nth_fact];
        var currentFactNode = d3.select("[name=F"+ factID +"]");

        if (currentFactNode.attr("collapse_rules")=="true"){

            currentFactNode.attr("collapse_rules", "false")
            var childrenRuleNodes = d3.selectAll("[parent=F"+ factID +"]");

            childrenRuleNodes.on("click",function(){

                if (fix_rule_visualizer == null){
                    var thisRule = d3.select(this);
                    fix_rule_visualizer = thisRule.attr("name");
                    thisRule.attr("rule_highlighted",function(){
                        return "highlighted";
                    });
                    thisRule.select("rect")
                                .style("fill", "#F51266");
                } else{
                    var thisRule = d3.select("[rule_highlighted=highlighted]");
                    thisRule.attr("rule_highlighted","unhighlighted")
                                .select("rect")
                                .style("fill", function(d){return color_factHis(cValue(d));});
                    fix_rule_visualizer = null;
                    rule_visualizer_out();
                }

            });
            childrenRuleNodes.append("rect")
                            .transition()
                            .duration(duration_slow)
                            .attr("transform", function(d) {
                                var nth_rule = $.inArray(d.ID,ruleRow);
                                var xloc = nth_rule *(barWidth_rule+3);
                                var yloc = fr_dist;
                                return "translate(" + xloc + "," + yloc + ")"; 
                            })
                            .attr("width", barWidth_rule)
                            .attr("height",barHeight_factHis)
                            .style("fill", function(d){return color_factHis(cValue(d));})
                            .attr("rx",15);

            childrenRuleNodes.append("text")
                            .transition()
                            .duration(duration_slow)
                            .attr("transform", function(d) {
                                var nth_rule = $.inArray(d.ID,ruleRow);
                                var xloc = nth_rule *(barWidth_rule+3);
                                var yloc = fr_dist;
                                return "translate(" + xloc + "," + yloc + ")"; 
                            })
                            .attr("dy", 18)
                            .attr("dx", 5)        
                            .text(function(d) { 
                                return "R" + d.ID;
                            });

            linkEnter.filter(function(d){
                        if (d.source.parent.ID == factID){return true;}
                        else {return false;}
                     })
                    .transition()
                    .duration(duration_slow)
                    .attr("d", function(d) {

                            var sx, sy, tx, ty;
                            var nth_sourceFact = $.inArray(d.source.parent.ID,factArray);
                            var sourceFactLoc = factArrayLoc[nth_sourceFact];
                            var nth_targetFact = $.inArray(d.target.ID,factArray);
                            var targetFactLoc = factArrayLoc[nth_targetFact];

                            var ruleRow = ruleArray[nth_sourceFact];
                            var nth_r = $.inArray(d.source.ID,ruleRow);

                            sx = sourceFactLoc[0] + nth_r *(barWidth_rule+3) + barWidth_rule/2;
                            sy = sourceFactLoc[1] + fr_dist + barHeight_factHis;
                            tx = targetFactLoc[0] + barWidth_fact/2,
                            ty = targetFactLoc[1];

                            if (sy > ty){
                                sy = sy - barHeight_factHis;
                                ty = ty + barHeight_factHis;
                            }

                            var s = {x: sx, y: sy};
                            var t = {x: tx, y: ty};
                            return diagonal({source: s, target: t});
                            });

        }else {
            currentFactNode.attr("collapse_rules", "true");
            var childrenRuleNodes = d3.selectAll("[parent=F"+ factID +"]");
            childrenRuleNodes.selectAll("rect")
                            .transition()
                            .duration(duration_slow)
                            .remove();
            childrenRuleNodes.selectAll("text")
                            .transition()
                            .duration(duration_slow)
                            .remove();

            linkEnter.filter(function(d){
                        if (d.source.parent.ID == factID){return true;}
                        else {return false;}
                     })
                    .transition()
                    .duration(duration_slow)
                    .attr("d", function(d) {

                            var sx, sy, tx, ty;
                            var nth_sourceFact = $.inArray(d.source.parent.ID,factArray);
                            var sourceFactLoc = factArrayLoc[nth_sourceFact];
                            var nth_targetFact = $.inArray(d.target.ID,factArray);
                            var targetFactLoc = factArrayLoc[nth_targetFact];

                            sx = sourceFactLoc[0] + barWidth_fact/2;
                            sy = sourceFactLoc[1] + barHeight_factHis;
                            tx = targetFactLoc[0] + barWidth_fact/2,
                            ty = targetFactLoc[1];

                            if (sy > ty){
                                sy = sy - barHeight_factHis;
                                ty = ty + barHeight_factHis;
                            }

                            var s = {x: sx, y: sy};
                            var t = {x: tx, y: ty};
                            return diagonal({source: s, target: t});
                            });           
        }
}    

function remove_unnecessary_nodes(){ // remove repeated fact Nodes
    var numFacts = factArray.length;
    for (var i=0;i<numFacts;i++){
        var factID = factArray[i];
        var repeatedFacts = d3.selectAll("[name=F"+ factID +"]");
        for (var j=1;j<repeatedFacts[0].length;j++){
            d3.select("[name=F"+ factID +"]").remove();
        }
    }
}

function update_nodes(){

    var outputNodes = nodes_factHis;
    var leng = expandableFacts.length;
    var noFactsToExpand = false;
    if (leng==0){
        noFactsToExpand = true;
        console.log("no facts to expand");
    }
    else {
        for (var i=0;i<leng;i++){

            var expandableFactID = expandableFacts[i];
            var xloc = expandableFactsLoc[i][0];
            var yloc = expandableFactsLoc[i][1];
            var tmpJsonObj = get_factHistory(expandableFactID);

            var newNodes = initialize_nodes(tmpJsonObj,xloc,yloc);
            outputNodes = mergeArrays(outputNodes,newNodes);
        }
        for (var j=0;j<leng;j++){
            expandableFacts.shift();
            expandableFactsLoc.shift();
        }
    }
    nodes_factHis = outputNodes;
    return noFactsToExpand;
}

function initialize_nodes(inputJsonObj,init_xloc,init_yloc){  // -> Even level means facts, odd level means rules. Initialization at level=0

    var factRow=[];
    var ruleRow=[];

    var temp_nodes = tree_factHis.nodes(inputJsonObj);
    temp_nodes.forEach(function(n, i) {

            var xloc = init_xloc;
            var yloc = init_yloc;
            var tmp = $.inArray(n.ID,factArray);
            if (n.type=="fact"){  
                if(i==0){
                    if (tmp==-1){
                        factArray.push(n.ID);
                        factArrayLoc.push([xloc,yloc]);
                    }
                }  // the main fact.. location does not change
                else {
                    if(tmp == -1){   // if the current fact node is unseen before, add new one
                        var tmp2 = $.inArray(get_factName(n.ID),factTypeArray);

                        if (tmp2 > -1){
                            yloc = (tmp2 +1) * ff_dist;
                            var nSameTypeFacts = factTypeCountArray[tmp2];
                            xloc = nSameTypeFacts * (barWidth_fact + 13);
                            factArray.push(n.ID);
                            factArrayLoc.push([xloc,yloc]);
                            expandableFacts.push(n.ID);
                            expandableFactsLoc.push([xloc,yloc]);
                            ++factTypeCountArray[tmp2];
                        } else{
                            var nTypes = factTypeArray.length;
                            ++nTypes;                                        
                            yloc = nTypes * ff_dist;
                            xloc = 0;
                            factArray.push(n.ID);
                            factArrayLoc.push([xloc,yloc]);
                            expandableFacts.push(n.ID);
                            expandableFactsLoc.push([xloc,yloc]);
                            factTypeArray.push(get_factName(n.ID));
                            factTypeCountArray[nTypes-1]=1;
                        }
                    } else{  // if the same fact node is already taken care of, refer to its previous location
                        xloc = factArrayLoc[tmp][0];
                        yloc = factArrayLoc[tmp][1];
                    }
                }
            } else{
                ruleRow.push(n.ID); // save list of rules fired
            }
            color_factHis(cValue(n));
            n.x = xloc;
            n.y = yloc;
        });
    // Will return temp_nodes with proper locations set up       
    ruleArray.push(ruleRow);
    return temp_nodes;
}

function mergeArrays(arr1,arr2){
    var leng1 = arr1.length;
    var leng2 = arr2.length;
    var newArray = [];
    newArray.length=0;
    for (var i=0;i<leng1;i++){
        newArray[i] = arr1[i];
    }
    for (var j=leng1;j<leng1+leng2;j++){
        newArray[j] = arr2[j-leng1];
    }
    return newArray;
}

function get_ruleObj_json(ruleID){
    var ruleObj_json;
    var ruleAJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"getRuleObjJson","ruleID":ruleID},
        success:function(data, textStatus, jqXHR) 
        {
            ruleObj_json = JSON.parse(data);  
//                        ruleObj_json = data;
        },
        complete: function(){
            return ruleObj_json;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function get rule Json Obj");
        }
    });
    return ruleAJAX.responseText;
}

function rule_visualizer_out(){

    if(fix_rule_visualizer != null){
        return;
    }

    d3.select("[id=ruleVisRect_right]")
                .transition()
                .duration(duration_slow)  
            .attr("transform", function(){
                var x = width_factHis + margin_factHis.left + margin_factHis.right;
                var y = 0;
                return "translate(" + x + "," + y + ")";
            })
            .attr("width",0);

    d3.select("[id=ruleVisRect_left]")
                .transition()
                .duration(duration_slow)  
            .attr("transform", function(){
                var x = 0;
                var y = 0;
                return "translate(" + x + "," + y + ")";
            })
            .attr("width",0);

    d3.select("[id=ruleVisualizer]")
                    .selectAll("foreignObject")
                    .remove();
}

function rule_visualizer(jsonObj_rule){

    if(fix_rule_visualizer != null){
        return;
    }


    var mouseLoc = d3.mouse(d3.select("[id=factHistoryFigure]").select("svg")[0][0])[0];
    var ruleVisLoc;
    if(mouseLoc > (width_factHis + margin_factHis.left + margin_factHis.right)*0.5){
        ruleVisLoc = "left";
    } else{
        ruleVisLoc = "right";
    }                            

    d3.select("[id=ruleVisRect_"+ ruleVisLoc +"]")
                .transition()
                .duration(duration_fast)
                .attr("transform", function(){
                    if(ruleVisLoc=="left"){
                        var x =0;
                        var y =0;
                    }else{
                       var x = width_factHis + margin_factHis.left + margin_factHis.right - ruleVisualizerRectSize.width;
                       var y = 0;
                    }
                    return "translate(" + x + "," + y + ")";
                })
                .attr("width",ruleVisualizerRectSize.width);

    console.log("rule visualizer activated");

    var fo = d3.select("[id=ruleVisualizer]")
                    .append("foreignObject")
                    .attr("x",function(){
                        if(ruleVisLoc=="left"){
                            return 0;
                        }else{
                            return width_factHis + margin_factHis.left + margin_factHis.right - ruleVisualizerRectSize.width;
                        }
                    })
                    .attr("y",0)
                    .attr({
                        'width': ruleVisualizerRectSize.width - 10,
                        'height': ruleVisualizerRectSize.height,
                        'class': 'svg-tooltip'
                    });

//                var tree_ruleVis = d3.layout.tree();     
//                var ruleVisNodes = tree_ruleVis.nodes(jsonObj_rule);
//                
//                var ruleVisSVG = d3.select("[id=ruleVisualizer]")
//
////                ruleVisNodes.forEach(function(n, i) {
////                   n.x = i * barHeight;
////                 });
//
//                if (ruleVisLoc == "left"){
//                    
////                    ruleVisSVG
//                } else{
//                    
//                }


    var fo_div = fo.append('xhtml:div')
                            .attr({
                                'class': 'tooltip'
                            });
    fo_div.selectAll("div")
            .data(jsonObj_rule)
//                        .forEach(function(n,i){
//                            n.i = i;
//                        })
            .enter()
            .append("div")
            .append("text")
            .style("color", "#4BFF51")
            .style("overflow", "scroll")
            .text(function(d){
                return d;
            });


//                        append("text")
////                            .attr('class', 'lead')
//                            .text(jsonObj_rule)
//                            .style("fill", "#4EFF72");


//                fo_div.append('p')
//                        .html('He was quiet in his ways, and his habits were regular. It was rare for him to be up after ten at night, and he had invariably breakfasted and gone out before I rose in the morning.');




}

function get_ppdefrule(ruleID){
    var ppdefrule;
    var ppdefruleAJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"requestppdefrule","ruleID":ruleID},
        success:function(data, textStatus, jqXHR) 
        {
            ppdfrule = data;
        },
        complete: function(){
            return ppdefrule;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function return_factName");
        }
    });
    return ppdefruleAJAX.responseText;
}

function get_factName(factID){
    var factName;
    var factAJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"getFactName","factID":factID},
        success:function(data, textStatus, jqXHR) 
        {
            factName = data;
        },
        complete: function(){
            return factName;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function return_factName");
        }
    });
    return factAJAX.responseText;
}

function get_ruleName(ruleID){
    var ruleName;
    var ruleAJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"getRuleName","ruleID":ruleID},
        success:function(data, textStatus, jqXHR) 
        {
            ruleName = data;
        },
        complete: function(){
            return ruleName;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function return_ruleName");
        }
    });
    return ruleAJAX.responseText;
}

function get_factHistory(idNum){
    var factHis;
    $.ajax({
        url: "JessCommandServlet",
        type: "POST",
        data: {ID: "factHistoryFigureRequest",subobj:"",factID: idNum},
        async: false,

        success:function(data, textStatus, jqXHR) 
        {
            console.log(data);
            factHis = JSON.parse(data);
            },
        error: function(jqXHR, textStatus, errorThrown) 
            {
            }

    });
    return factHis;
}

function isOdd(num) { return (num % 2) == 1;}

function unhighlightFact(factID){

        var currentFactNode = d3.select("[name=F"+ factID +"]");

        currentFactNode.select("[id=factTextBox]")
                .transition()
                .duration(duration_slow)
                .attr("width", barWidth_fact)
                .attr("height",barHeight_factHis)
                .style("fill", function(d){return color_factHis(cValue(d));});

        var nth_f = $.inArray(factID,factArray);
        var rulerow = ruleArray[nth_f];

        var affectedLinks = linkEnter
                        .filter(function(d){
                            if (factID == d.target.ID){  // target fact highlighted
                                return true;
                            } else if ($.inArray(d.source.ID,rulerow) > -1 && d.source.parent.ID == factID){ // source fact hightlighted
                                return true;
                            } else {
                                return false;
                            }
                        })
                        .transition()
                        .duration(duration_slow)
                        .style("stroke","#FAA93F")
                        .style("stroke-width","2px");

        highlightedFactID = 0;

        var dropdownMenu = d3.select("[id=dropdown_slots]");
        dropdownMenu.selectAll("option").remove(); // remove slots from previous fact
        dropdownMenu.append("option")
            .attr("value","not_selected")
            .text("Select a fact");

        d3.select("[id=slotValue_textDisplay]").html(""); 
}

function click_highlight(d){

    if (d3.select("[id=inspectionMode_button]").attr("class") == "toggle-button"){
        return function(d){
            // do nothing
        };
    } else { // highlights facts and all the paths
        if (d.ID == highlightedFactID){
             return unhighlightFact(d.ID);
        }else{
            return highlightFact(d.ID);
        }
    }
}

function highlightFact(factID){

        if (highlightedFactID != 0){
            unhighlightFact(highlightedFactID);
        }

        var currentFactNode = d3.select("[name=F"+ factID +"]");
        highlightedFactID = factID;

        currentFactNode.select("[id=factTextBox]")
                .transition()
                .duration(duration_slow)
                .attr("width", barWidth_fact+10)
                .attr("height",barHeight_factHis+5)
                .style("fill", "#F51266");

        var nth_f = $.inArray(factID,factArray);
        var rulerow = ruleArray[nth_f];

//                    var affectedLinks = linkEnter
//                                    .filter(function(d){
//                                        if (factID == d.target.ID){  // target fact highlighted
//                                            return true;
//                                        } else if ($.inArray(d.source.ID,rulerow) > -1 && d.source.parent.ID == factID){ // source fact hightlighted
//                                            return true;
//                                        } else {
//                                            return false;
//                                        }
//                                    })
//                                    .transition()
//                                    .duration(duration_slow)
//                                    .style("stroke","#F51266")
//                                    .style("sroke-width","2.3px");

        setDropdownMenu_slotNames(factID);   

        var slotNames = JSON.parse(get_slotNames(factID)); 
        var slotVal = get_slotValue(factID, slotNames[0]);
//                    d3.select("[id=slotValue_textDisplay]")
//                            .html(slotVal);
}

function get_slotNames(factID){
    var slotNames;
    var slotNameAJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"getSlotNames","factID":factID},
        success:function(data, textStatus, jqXHR) 
        {
            slotNames = JSON.parse(data); 
        },
        complete: function(){
            return slotNames;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function return_slotNames");
        }
    });
    return slotNameAJAX.responseText;
}

function setDropdownMenu_slotNames(factID){

    var slotNames = JSON.parse(get_slotNames(factID)); 
    slotNames.unshift("Select a slot");
    var nSlots = slotNames.length;

    var dropdownMenu = d3.select("[id=dropdown_slots]");

    dropdownMenu.selectAll("option").remove(); // remove slots from previous fact
    dropdownMenu.selectAll("option")
            .data(slotNames)
            .enter()
            .append("option")
            .attr("value",function(d){
                return d;
            })
            .text(function(d){
                return d;
            });

    dropdownMenu.on("change",function(d){

        var selectedSlot = dropdownMenu[0][0].value;
        if (selectedSlot == "Select a slot"){
            d3.select("[id=slotValue_textDisplay]")
                    .html("");
            return;
        }
        var slotVal = get_slotValue(factID, selectedSlot);
        d3.select("[id=slotValue_textDisplay]")
                .html(slotVal);

        highlight_links(factID,selectedSlot);

    });

}

function highlight_links(factID, slotName){

        d3.selectAll("[name=link-highlighted]")
                .attr("name","link")
                .transition()
                .duration(duration_slow)
                .style("stroke","#FAA93F")
                .style("stroke-width","2px");

        // given factID and slotName, find the related rule
        var relRuleID = get_relevantRule(factID,slotName);

        var affectedLinks = d3.selectAll("[class=link]") 
                        .filter(function(d){
                            if (factID == d.source.parent.ID && relRuleID == d.source.ID){  // target fact highlighted
                                return true;
                            }
                        })
                        .attr("name","link-highlighted")
                        .transition()
                        .duration(duration_slow)
                        .style("stroke","#F51266")
                        .style("sroke-width","2.5px");



}

function get_slotValue(factID,slotName){
    var slotValue;
    var slotValueAJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"getSlotValue","factID":factID, "slotName":slotName},
        success:function(data, textStatus, jqXHR) 
        {
            slotValue = data; 
        },
        complete: function(){
            return slotValue;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function return_slotNames");
        }
    });
    return slotValueAJAX.responseText;
}

function get_relevantRule(factID,slotName){
    var relevantRuleID;
    var AJAX = $.ajax({
        url : "JessCommandServlet",
        type: "POST",
        async: false,
        data : {"ID":"getRelevantRule","factID":factID, "slotName":slotName},
        success:function(data, textStatus, jqXHR) 
        {
            relevantRuleID = data; 
        },
        complete: function(){
            return relevantRuleID;
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("Error - function return_slotNames");
        }
    });
    return AJAX.responseText;
}



} catch(err) {
    console.log(err.message);
}
            