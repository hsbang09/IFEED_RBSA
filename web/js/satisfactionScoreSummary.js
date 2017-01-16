


function satisfaction_score_summary_request(arch){
    var jsonObj_satScores;
    var bitString = booleanArray2String(arch[0].bitString);
        
    $.ajax({
        url: "ResultsServlet",
        type: "POST",
        data: {ID: "satisfactionScoreSummaryRequest", bitString: bitString},
        async: false,
        success:function(data, textStatus, jqXHR) 
        {
            jsonObj_satScores = JSON.parse(data);
        },
        error: function(jqXHR, textStatus, errorThrown) 
        {
            alert("error");
        } 
    });
    return jsonObj_satScores;  
}




function init_satisfaction_summary_tree(source){

    var nodes = tree_satTable.nodes(source);
    nodes.forEach(function(n,i) {  // All nodes collapsed by default
        if(n.level == "value"){
        }
        else {
            n._children = n.children;
            n.children = null;
        }
    });
    root_satTable = source;
    draw_satisfaction_summary_tree(source);
}



function draw_satisfaction_summary_tree(source){

    var satisfaction_summary_tree_div = d3.select("#satisfaction_score_summary_div");
    var satisfaction_summary_tree_svg = d3.select("#satisfaction_score_summary_svg")

    var nodes = tree_satTable.nodes(source);
    var height = Math.max(500, nodes.length * barHeight_satTable + margin_satTable.top + margin_satTable.bottom);
    
    satisfaction_summary_tree_svg.transition()
                    .duration(duration_satTable)
                    .attr("height", height);

     // Compute the "layout".
    nodes.forEach(function(n, i) {
        n.x = i * barHeight_satTable;
        if(n.level == "value"){n.y = 0;}
        else if (n.level == "stakeholder"){ n.y = barHeight_satTable / 2;}
        else if (n.level == "objective"){ n.y = barHeight_satTable;}
        else {n.y = barHeight_satTable * 3 / 2;}
    });

      // Update the nodes…
    var node = satisfaction_summary_tree_svg.selectAll("g.node")
                        .data(nodes, function(d) { return d.id || (d.id = ++i_satTable); });
    var nodeEnter = node.enter().append("g")
                                .attr("class", "node")
                                .style("opacity", 1e-6)
                                .on("click", click_satTable);

    // Enter any new nodes at the parent's previous position.
    nodeEnter.append("rect")
                .attr("height", barHeight_satTable)
                .attr("width", barWidth_satTable)
                .style("fill", color_satTable);

    nodeEnter.append("text")
                .attr("dy", 14)
                .attr("dx", 5.5)
                .style("font-size","11px")
                .text(function(d) {
                    var numb = d.score;
                    numb = numb.toFixed(4);

                    if(d.level == "value"){
                        return "Science Score: " + numb;
                    } else if (d.level == "stakeholder"){
                        return "[" + d.name + "] "+ " " + numb; 
                    } else {
                        return "[" + d.name + "] "+ d.description + ": " + numb; 
                    }
                    
                });


    // Transition nodes to their new position.
    nodeEnter.transition()
                .duration(duration_satTable)
                .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
                .style("opacity", 1);

    node.transition()
            .duration(duration_satTable)
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
            .style("opacity", 1)
            .select("rect")
            .style("fill", color_satTable);

    // Transition exiting nodes to the parent's new position.
    node.exit().transition()
        .duration(duration_satTable)
        .attr("transform", function(d) { 
            return "translate(" + d.parent.y + "," + d.parent.x + ")"; 
        })
        .style("opacity", 1e-6)
        .remove();

    // Stash the old positions for transition.
    nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
    });

}
            
            
            
                
// Toggle children on click.
function click_satTable(d) {
    
    if (d.level=="subobjective"){
        var subobjName = d.name;
        attribute_score_summary_request(subobjName);
        factHistory_figure_request(subobjName);
    } else{
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else {
            d.children = d._children;
            d._children = null;
        }
            draw_satisfaction_summary_tree(root_satTable);
        }
    }

function color_satTable(d) {
        if(d.level == "value"){return "#2F75FF";}
        else if (d.level == "stakeholder"){ return "#2FC5FF";}
        else if (d.level == "objective"){ return "#A9FFFE";}
        else {return "#F5FF91";}
}
