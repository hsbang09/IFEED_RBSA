/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */





function reset_drawing_scatterPlot() {
    d3.select("[id=scatterPlotFigure]").selectAll("svg").remove();
}

function draw_scatterPlot(source) {

    source.forEach(function (d) {  // convert string to numbers
        d.science = +d.science;
        d.cost = +d.cost;
        if (d.cost == 100000) {
            d.cost = 0;
            d.science = 0;
        }
    });


    // setup x 
    xValue = function (d) {
        return d.science;
    }; // data -> value
    xScale = d3.scale.linear().range([0, width]); // value -> display

    // don't want dots overlapping axis, so add in buffer to data domain 
    xBuffer = (d3.max(source, xValue) - d3.min(source, xValue)) * 0.05;
    xScale.domain([d3.min(source, xValue) - xBuffer, d3.max(source, xValue) + xBuffer]);

    xMap = function (d) {
        return xScale(xValue(d));
    }; // data -> display
    xAxis = d3.svg.axis().scale(xScale).orient("bottom");
//                                    .tickSize(-height);
//                                    .tickFormat(d3.format("s"));

    // setup y
    yValue = function (d) {
        return d.cost;
    }; // data -> value
    yScale = d3.scale.linear().range([height, 0]); // value -> display

    yBuffer = (d3.max(source, yValue) - d3.min(source, yValue)) * 0.05;
    yScale.domain([d3.min(source, yValue) - yBuffer, d3.max(source, yValue) + yBuffer]);

    yMap = function (d) {
        return yScale(yValue(d));
    }; // data -> display
    yAxis = d3.svg.axis().scale(yScale).orient("left");
//                                .tickSize(-width);
//                                .tickFormat(d3.format("s"));


    svg = d3.select("[id=scatterPlotFigure]")
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .call(
                d3.behavior.zoom()
                .x(xScale)
                .y(yScale)
                .scaleExtent([0.4, 20])
                .on("zoom", function (d) {

                    svg = d3.select("[id=scatterPlotFigure]")
                            .select("svg");

                    svg.select(".x.axis").call(xAxis);
                    svg.select(".y.axis").call(yAxis);

                    objects.select(".hAxisLine").attr("transform", "translate(0," + yScale(0) + ")");
                    objects.select(".vAxisLine").attr("transform", "translate(" + xScale(0) + ",0)");
                    //d3.event.translate[0]

                    svg.selectAll("[class=dot]")
                            .attr("transform", function (d) {
                                var xCoord = xMap(d);
                                var yCoord = yMap(d);
                                return "translate(" + xCoord + "," + yCoord + ")";
                            });
                    svg.selectAll("[class=dot_highlighted]")
                            .attr("transform", function (d) {
                                var xCoord = xMap(d);
                                var yCoord = yMap(d);
                                return "translate(" + xCoord + "," + yCoord + ")";
                            });
                    svg.selectAll("[class=dot_hidden]")
                            .attr("transform", function (d) {
                                var xCoord = xMap(d);
                                var yCoord = yMap(d);
                                return "translate(" + xCoord + "," + yCoord + ")";
                            });
                    
                    svg.selectAll("[class=paretoFrontier]")
                            .attr("transform", function (d) {
                                return "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")";
                            })
                            .attr("stroke-width",1.5/d3.event.scale);


                    translate_tmp = d3.event.translate;
                    scale_tmp = d3.event.scale;

                })
                )
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");



    // x-axis
    svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis)
            .append("text")
            .attr("class", "label")
            .attr("x", width)
            .attr("y", -6)
            .style("text-anchor", "end")
            .text("Science benefit");

    // y-axis
    svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text("Cost");

    objects = svg.append("svg")
            .attr("class", "objects")
            .attr("width", width)
            .attr("height", height);

    //Create main 0,0 axis lines:
    objects.append("svg:line")
            .attr("class", "axisLine hAxisLine")
            .attr("x1", 0)
            .attr("y1", 0)
            .attr("x2", width)
            .attr("y2", 0)
            .attr("transform", "translate(0," + (yScale(0)) + ")");
    objects.append("svg:line")
            .attr("class", "axisLine vAxisLine")
            .attr("x1", 0)
            .attr("y1", 0)
            .attr("x2", 0)
            .attr("y2", height)
            .attr("transform", "translate(" + (xScale(0)) + ",0)");

    var dots = objects.selectAll(".dot")
            .data(source)
            .enter().append("circle")
            .attr("class", "dot")
            .attr("r", 4)
            .attr("transform", function (d) {
                var xCoord = xMap(d);
                var yCoord = yMap(d);
                return "translate(" + xCoord + "," + yCoord + ")";
            })
            .style("fill", function (d) {
                if (d.status == "origianlData") {
                    return "#000000";
                } else if (d.status == "added") {
                    return "#188836";
                } else if (d.status == "justAdded") {
                    return "#20FE5B";
                } else {
                    return "#000000";
                }
            });
    dots.on("mouseover", dot_mouseover)
            .on("mouseout", dot_mouseout);
    dots.on("click", dot_click);


    initialize_tabs();
    d3.select("[id=selectArchsWithinRangeButton]")[0][0].disabled = false;
    d3.select("[id=cancel_selection]")[0][0].disabled = false;
//    d3.select("[id=hide_selection]")[0][0].disabled = false;
//    d3.select("[id=show_all_archs]")[0][0].disabled = false;
	
    d3.select("[id=scatterPlotFigure]").on("click",unhighlight_basic_info_box);
    d3.select("[id=basicInfoBox_div]").on("click",highlight_basic_info_box);
    d3.selectAll("[id=getDrivingFeaturesButton]").on("click", runDataMining);
    d3.select("[id=selectArchsWithinRangeButton]").on("click", selectArchsWithinRange);
    d3.select("[id=cancel_selection]").on("click",cancelDotSelections);
//    d3.select("[id=hide_selection]").on("click",hideSelection);
//    d3.select("[id=show_all_archs]").on("click",show_all_archs);
    d3.select("[id=openFilterOptions]").on("click",openFilterOptions);
    d3.select("[id=numOfArchs_inputBox]").text(""+numOfArchs());
    d3.selectAll("[class=dot]")[0].forEach(function(d,i){
        d3.select(d).attr("paretoRank",-1);
    });

    orbitList = getOrbitList();
    instrList = getInstrumentList();
    ninstr = getNinstr();
    norb = getNorb();

    calculateParetoRanking();
    drawParetoFront();
    
    selection_changed = true;
}




function add_newArchs_to_scatterPlot() {
    for (var i = 0; i < newArchs.length; i++) {
        architectures.push(newArchs[i]);
    }
    reset_drawing_scatterPlot();
    draw_scatterPlot(architectures);
}

function selectArchsWithinRange() {
	
    var clickedArchs = d3.selectAll("[class=dot_highlighted]");
    var unClickedArchs = d3.selectAll("[class=dot]");

    var minCost = d3.select("[id=selectArchsWithinRange_minCost]")[0][0].value;
    var maxCost = d3.select("[id=selectArchsWithinRange_maxCost]")[0][0].value;
    var minScience = d3.select("[id=selectArchsWithinRange_minScience]")[0][0].value;
    var maxScience = d3.select("[id=selectArchsWithinRange_maxScience]")[0][0].value;

    if (maxCost == "inf") {
        maxCost = 1000000000000;
    }

    unClickedArchs.filter(function (d) {

        var sci = d.science;
        var cost = d.cost;

        if (sci < minScience) {
            return false;
        } else if (sci > maxScience) {
            return false;
        } else if (cost < minCost) {
            return false;
        } else if (cost > maxCost) {
            return false;
        } else {
            return true;
        }
    })
            .attr("class", "dot_highlighted")
            .style("fill", "#0040FF");

    clickedArchs.filter(function (d) {

        var sci = d.science;
        var cost = d.cost;

        if (sci < minScience) {
            return true;
        } else if (sci > maxScience) {
            return true;
        } else if (cost < minCost) {
            return true;
        } else if (cost > maxCost) {
            return true;
        } else {
            return false;
        }
    })
            .attr("class", "dot")
            .style("fill", function (d) {
                if (d.status == "added") {
                    return "#188836";
                } else if (d.status == "justAdded") {
                    return "#20FE5B";
                } else {
                    return "#000000";
                }
            });

    d3.select("[id=numOfSelectedArchs_inputBox]").text(""+numOfSelectedArchs());
    selection_changed = true;
    initialize_tabs_driving_features();
    initialize_tabs_classification_tree();
}

function cancelDotSelections(){
    var clickedArchs = d3.selectAll("[class=dot_highlighted]");

    clickedArchs.attr("class", "dot")
            .style("fill", function (d) {
                if (d.status == "added") {
                    return "#188836";
                } else if (d.status == "justAdded") {
                    return "#20FE5B";
                } else {
                    return "#000000";
                }
            });
    d3.select("[id=instrumentOptions]")
            .select("table").remove();        
    d3.select("[id=numOfSelectedArchs_inputBox]").text(""+numOfSelectedArchs());
    selection_changed = true;
    initialize_tabs_driving_features();
    initialize_tabs_classification_tree();
}

function hideSelection(){

    var clickedArchs = d3.selectAll("[class=dot_highlighted]");

    clickedArchs.attr("class", "dot_hidden")
            .style("opacity", 0.085);
    d3.select("[id=instrumentOptions]")
            .select("table").remove();        
    d3.select("[id=numOfSelectedArchs_inputBox]").text(""+numOfSelectedArchs());
    d3.select("[id=numOfArchs_inputBox]").text(""+numOfArchs());
    selection_changed = true;
    initialize_tabs_driving_features();
    initialize_tabs_classification_tree();
}
function show_all_archs(){

    var hiddenArchs = d3.selectAll("[class=dot_hidden]");

    hiddenArchs.attr("class", "dot")
            .style("fill", function (d) {
                if (d.status == "added") {
                    return "#188836";
                } else if (d.status == "justAdded") {
                    return "#20FE5B";
                } else {
                    return "#000000";
                }
            })
            .style("opacity",1);
    d3.select("[id=instrumentOptions]")
            .select("table").remove();        
    d3.select("[id=numOfSelectedArchs_inputBox]").text(""+numOfSelectedArchs());
    d3.select("[id=numOfArchs_inputBox]").text(""+numOfArchs());
    selection_changed = true;
    initialize_tabs_driving_features();
    initialize_tabs_classification_tree();
}






function dot_mouseover(d) {

    if(infoBox_active==true){
            return;
    }	
    d3.select(this).style("fill", "#D32020");
    d3.select("[id=basicInfoBox_div]").select("[id=view1]").select("g").remove();
    var archInfoBox = d3.select("[id=basicInfoBox_div]").select("[id=view1]")
            .append("g");

    archInfoBox.append("p")
            .text("Benefit: " + d.science.toFixed(4));
    archInfoBox.append("p")
            .text("Cost: " + d.cost.toFixed(1));

    var bitString = d.bitString;
    draw_archBasicInfoTable(bitString);
}


function color_dots(d){
    if (d3.select(d).attr("class") == "dot_highlighted") {
        d3.select(d).style("fill","#0040FF");
    } else if (d3.select(d)[0][0].__data__.status == "justAdded") {
        d3.select(d).style("fill", "#20FE5B");
    } else if (d3.select(d)[0][0].__data__.status == "added") {
        d3.select(d).style("fill", "#188836");
    } else {
        d3.select(d).style("fill", "#000000");
    }
}

function dot_mouseout(d){
    color_dots(this);
}

function dot_click(d) {
    if (d3.select(this).attr("class") == "dot_highlighted") {
        d3.select(this).attr("class", "dot");
    } else {
        d3.select(this).attr("class", "dot_highlighted");
    }
    color_dots(this);
    
    d3.select("[id=numOfSelectedArchs_inputBox]").text(""+numOfSelectedArchs());
    selection_changed = true;
    initialize_tabs_driving_features();
    initialize_tabs_classification_tree();
}










function scatterPlot_option(selected_option){ // three options: zoom, drag_selection, drag_deselection

    if (selected_option=="1"){

    	
        translate_tmp_local[0] = translate_tmp[0];
        translate_tmp_local[1] = translate_tmp[1];
        scale_tmp_local = scale_tmp;

        var svg_tmp =  d3.select("[id=scatterPlotFigure]")
            .select("svg")
            .on("mousedown",null)
            .on("mousemove",null)
            .on("mouseup",null);

        d3.select("[id=scatterPlotFigure]")
            .select("svg")
            .call(
                d3.behavior.zoom()
                        .x(xScale)
                        .y(yScale)
                        .scaleExtent([0.4, 20])
                        .on("zoom", function (d) {

                            var svg = d3.select("[id=scatterPlotFigure]")
                                    .select("svg");

                            svg.select(".x.axis").call(xAxis);
                            svg.select(".y.axis").call(yAxis);

                            objects.select(".hAxisLine").attr("transform", "translate(0," + yScale(0) + ")");
                            objects.select(".vAxisLine").attr("transform", "translate(" + xScale(0) + ",0)");
                            //d3.event.translate[0]

                            svg.selectAll("[class=dot]")
                                    .attr("transform", function (d) {
                                        var xCoord = xMap(d);
                                        var yCoord = yMap(d);
                                        return "translate(" + xCoord + "," + yCoord + ")";
                                    });
                            svg.selectAll("[class=dot_highlighted]")
                                    .attr("transform", function (d) {
                                        var xCoord = xMap(d);
                                        var yCoord = yMap(d);
                                        return "translate(" + xCoord + "," + yCoord + ")";
                                    });
                            svg.selectAll("[class=dot_hidden]")
                            .attr("transform", function (d) {
                                var xCoord = xMap(d);
                                var yCoord = yMap(d);
                                return "translate(" + xCoord + "," + yCoord + ")";
                            });
                            
                            svg.selectAll("[class=paretoFrontier]")
                                    .attr("transform", function (d) {
                                         var x = translate_tmp_local[0]*d3.event.scale + d3.event.translate[0];
                                         var y = translate_tmp_local[1]*d3.event.scale + d3.event.translate[1];
                                         var s = d3.event.scale*scale_tmp_local;
                                        return "translate(" + x +","+ y + ")scale(" + s + ")";
                                    })
                                     .attr("stroke-width",function(){
                                         return 1.5/(d3.event.scale*scale_tmp_local);
                                     });

                            translate_tmp[0] = d3.event.translate[0] + translate_tmp_local[0]*d3.event.scale;
                            translate_tmp[1] = d3.event.translate[1] + translate_tmp_local[1]*d3.event.scale;
                            scale_tmp = d3.event.scale*scale_tmp_local;

                        })       
            )  
    } else{
    	
        var option;
        if(selected_option=="2"){
            option = "selection";
        }else{
            option = "deselection";
        }

        var svg_tmp =  d3.select("[id=scatterPlotFigure]")
            .select("svg")
            .call(d3.behavior.zoom().on("zoom",null));

        svg_tmp
            .on( "mousedown", function() {
//                        d3.selectAll("[class=dot_selected]").attr("class","dot"); 
                var p = d3.mouse( this);
                svg_tmp.append( "rect")
                        .attr({
                            rx      : 0,
                            ry      : 0,
                            class   : "selection",
                            x       : p[0],
                            y       : p[1],
                            width   : 0,
                            height  : 0,
                            x0      : p[0],
                            y0      : p[1]
                        })
                        .style("background-color", "#EEEEEE")
                        .style("opacity", 0.18);
            })
            .on( "mousemove", function() {

                var s = svg_tmp.select("rect.selection");
               if( !s.empty()) {
                    var p = d3.mouse( this);

                        b = {
                            x       : parseInt( s.attr("x"),10),
                            y       : parseInt( s.attr("y"), 10),
                            x0       : parseInt( s.attr("x0"),10),
                            y0       : parseInt( s.attr("y0"), 10),
                            width   : parseInt( s.attr("width"),10),
                            height  : parseInt( s.attr("height"), 10)
                        },
                        move = {
                            x : p[0] - b.x0,
                            y : p[1] - b.y0
                        };

                        if (move.x < 0){
                            b.x = b.x0 + move.x;

                        } else{
                            b.x = b.x0;
                        }
                        if (move.y < 0){
                            b.y = b.y0 + move.y;
                        } else {
                            b.y = b.y0;
                        }
                        b.width = Math.abs(move.x);
                        b.height = Math.abs(move.y);

                    s.attr( b);

                    var dots;  

                    if(option=="selection"){
                        dots = d3.selectAll("[class=dot]")[0].forEach(function(d,i){
                            var sci = d.__data__.science;
                            var cost = d.__data__.cost;
                            var xCoord = xScale(sci);
                            var yCoord = yScale(cost);

                            if( 
                                xCoord + margin.left>= b.x && xCoord + margin.left <= b.x+b.width && 
                                yCoord + margin.top >= b.y && yCoord + margin.top  <= b.y+b.height
                            ) {
                                d3.select(d).attr("class","dot_highlighted")
                                        .style("fill", "#0040FF");      
                                selection_changed = true;
                                initialize_tabs_driving_features();
                                initialize_tabs_classification_tree();
                            }
                        });

                    }else{
                        dots = d3.selectAll("[class=dot_highlighted]")[0].forEach(function(d,i){
                            var sci = d.__data__.science;
                            var cost = d.__data__.cost;
                            var xCoord = xScale(sci);
                            var yCoord = yScale(cost);

                            if( 
                                xCoord + margin.left>= b.x && xCoord + margin.left <= b.x+b.width && 
                                yCoord + margin.top >= b.y && yCoord + margin.top  <= b.y+b.height
                            ) {
                                d3.select(d).attr("class","dot")
                                        .style("fill", function (d) {
                                            if (d.status == "added") {
                                                return "#188836";
                                            } else if (d.status == "justAdded") {
                                                return "#20FE5B";
                                            } else {
                                                return "#000000";
                                            }
                                        });      
                                selection_changed = true;
                                initialize_tabs_driving_features();
                                initialize_tabs_classification_tree();
                            }
                        });
                    }
                    d3.select("[id=numOfSelectedArchs_inputBox]").text(""+numOfSelectedArchs());

            }      
    })
    .on( "mouseup", function() {
    	unhighlight_basic_info_box();
        var svg_tmp =  d3.select("[id=scatterPlotFigure]")
            .select("svg")

           // remove selection frame
        svg_tmp.selectAll( "rect.selection").remove();
        
    })
    }               
}


function drawParetoFront(){

    var archsInParetoFront = d3.selectAll("[class=dot]")[0].filter(function(d){
        if(d3.select(d).attr("paretoRank")=="0"){
            return true;
        }
    });

    var sortedScoreList = []; sortedScoreList.length=0;
    var sortedArchList = []; sortedArchList.length=0;

    var size = archsInParetoFront.length;

    for(var i=0;i<size;i++){
        var thisScore = archsInParetoFront[i].__data__.science;
        var tmp = {
                cost: archsInParetoFront[i].__data__.cost,
                sci: archsInParetoFront[i].__data__.science
        };

        if(sortedScoreList.length==0){
            sortedScoreList.push(thisScore);
            sortedArchList.push(tmp);
        }else{
            var sortedLength = sortedScoreList.length;
            for(var j=0;j<sortedLength;j++){
                if(thisScore > sortedScoreList[j]){
                    break;
                }
            }
            sortedScoreList.splice(j, 0, thisScore);
            sortedArchList.splice(j, 0, tmp);
        }
    }

    var lines = []; lines.length=0;
    for (var i=1;i<size;i++){
        var line = {
            x1: xScale(sortedArchList[i-1].sci),
            x2: xScale(sortedArchList[i].sci),
            y1: yScale(sortedArchList[i-1].cost),
            y2: yScale(sortedArchList[i].cost) 
        };
        lines.push(line);
    }

    d3.select("[id=scatterPlotFigure]").select("svg")
            .select("[class=objects]")
            .selectAll("[class=paretoFrontier]")
            .data(lines)
            .enter()
            .append("line")
            .attr("class","paretoFrontier")
            .attr("stroke-width", 1.5)
            .attr("stroke", "#D00F0F")
            .attr("x1",function(d){
                return d.x1;
            })
            .attr("x2",function(d){
                return d.x2;
            })
            .attr("y1",function(d){
                return d.y1;
            })
            .attr("y2",function(d){
                return d.y2;
            });

}

function calculateParetoRanking(){      
    cancelDotSelections();

    var archs = d3.selectAll("[class=dot]")[0].filter(function(d){
        if(d3.select(d).attr("paretoRank")=="-1"){
            return true;
        }
    });
    if (archs.length==0){
        return;
    }

    var rank=0;
    while(archs.length > 0){

        var numArchs = archs.length;
        if (rank>15){
            break;
        }

        for (var i=0; i<numArchs; i++){
            var non_dominated = true;
            var thisArch = archs[i];

            for (var j=0;j<numArchs;j++){
                if (i==j) continue;
                if (
                    (thisArch.__data__.science <= archs[j].__data__.science &&
                    thisArch.__data__.cost > archs[j].__data__.cost) || 
                    (thisArch.__data__.science < archs[j].__data__.science &&
                    thisArch.__data__.cost >= archs[j].__data__.cost) 
                ){
                    non_dominated = false;
                }
            }
            if (non_dominated == true){
                d3.select(thisArch).attr("paretoRank",""+rank);
            } 
        }
        archs = d3.selectAll("[class=dot]")[0].filter(function(d){
            if(d3.select(d).attr("paretoRank")=="-1"){
                return true;
            }
        });
        rank++;
    }

}


function highlight_basic_info_box(){

    d3.select("[id=scatterPlotFigure]")
    	.style("border-width","1px");
	d3.select("[id=basicInfoBox_div]")
		.style("border-width","3.3px");
	infoBox_active=true;
}
function unhighlight_basic_info_box(){

    d3.select("[id=scatterPlotFigure]")
			.style("border-width","3.3px");
	d3.select("[id=basicInfoBox_div]")
			.style("border-width","1px");
	infoBox_active=false;
}





var cars;
function getDrivingFeatures_automated(){
	
    cancelDotSelections();
    var selectedArchs = d3.selectAll("[class=dot]");
    var nonSelectedArchs = d3.selectAll("[class=dot]");

    var minCost = 0;
    var maxCost = 5000;
    var minScience = 0.15;
    var maxScience = 1;

    nonSelectedArchs = nonSelectedArchs.filter(function (d) {

        var sci = d.science;
        var cost = d.cost;

        if (sci < minScience) {
            return true;
        } else if (sci > maxScience) {
            return true;
        } else if (cost < minCost) {
            return true;
        } else if (cost > maxCost) {
            return true;
        } else {
            return false;
        }
    });

    selectedArchs = selectedArchs.filter(function (d) {

        var sci = d.science;
        var cost = d.cost;

        if (sci < minScience) {
            return false;
        } else if (sci > maxScience) {
            return false;
        } else if (cost < minCost) {
            return false;
        } else if (cost > maxCost) {
            return false;
        } else {
            return true;
        }
    });
//    d3.select("[id=numOfSelectedArchs_inputBox]").attr("value",numOfSelectedArchs());
    
    var support_threshold = d3.select("[id=support_threshold_input]")[0][0].value;
    var confidence_threshold = d3.select("[id=confidence_threshold_input]")[0][0].value;
    var lift_threshold = d3.select("[id=lift_threshold_input]")[0][0].value;


    var numOfSelectedArchs = selectedArchs.size();
    var numOfNonSelectedArchs = nonSelectedArchs.size();
    
    
    buttonClickCount_drivingFeatures += 1;
    getDrivingFeatures_numOfArchs.push({numOfSelectedArchs,numOfNonSelectedArchs});
    getDrivingFeatures_thresholds.push({supp:support_threshold,lift:lift_threshold,conf:confidence_threshold});
    
    
    var selectedBitStrings = [];
    var nonSelectedBitStrings = [];
    selectedBitStrings.length = 0;
    nonSelectedBitStrings.length=0;

    for (var i = 0; i < numOfSelectedArchs; i++) {
        var tmpBitString = booleanArray2String(selectedArchs[0][i].__data__.bitString);
        selectedBitStrings.push(tmpBitString);
    }
    for (var i = 0; i < numOfNonSelectedArchs; i++) {
        var tmpBitString = booleanArray2String(nonSelectedArchs[0][i].__data__.bitString);
        nonSelectedBitStrings.push(tmpBitString);
    }

    $.ajax({
        url: "DrivingFeatureServlet",
        type: "POST",
        data: {ID: "automaticFeatureGeneration",selected: JSON.stringify(selectedBitStrings),nonSelected:JSON.stringify(nonSelectedBitStrings),
        	supp:support_threshold,conf:confidence_threshold,lift:lift_threshold,
        	sortBy:"confave"},
        async: false,
        success: function (data, textStatus, jqXHR)
        {
//        	console.log(data);
        	sortedDFs = JSON.parse(data);
        },
        error: function (jqXHR, textStatus, errorThrown)
        {alert("error");}
    });

    display_drivingFeatures(sortedDFs,"confave");
}




function initialize_tabs(){
	initialize_tabs_inspection();
	initialize_tabs_filter_options();
	initialize_tabs_driving_features();
	initialize_tabs_classification_tree();
}


function initialize_tabs_inspection(){
	d3.select("[id=basicInfoBox_div]").select("[id=view1]").select("g").remove();
	d3.select("[id=basicInfoBox_div]").select("[id=view1]").append("g")
			.append("div")
			.style("width","900px")
			.style("margin","auto")
			.append("div")
			.style("width","100%")
			.style("font-size","21px")
			.text("If you hover the mouse over a design, relevant information will be displayed here.");
}
function initialize_tabs_filter_options(){

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
    
}
function initialize_tabs_driving_features(){
	
	d3.select("[id=basicInfoBox_div]").select("[id=view3]").select("g").remove();
	var guideline = d3.select("[id=basicInfoBox_div]").select("[id=view3]")
			.append("g")
			.append("div")
			.style("width","900px")
			.style("margin","auto")
			
	guideline.append("div")
			.style("width","100%")
			.style("font-size","21px")
			.text("To run data mining, select target solutions on the scatter plot. Then click the button below.");

	guideline.append("div")
			.style("width","300px")
			.style("margin","auto")
			.append("button")
			.attr("id","getDrivingFeaturesButton")
			.style("margin-top","30px")
			.style("width","200px")
			.style("font-size","19px")
			.text("Run data mining");
	d3.selectAll("[id=getDrivingFeaturesButton]").on("click", runDataMining);
}

function initialize_tabs_classification_tree(){

		d3.select("[id=basicInfoBox_div]").select("[id=view4]").select("g").remove();
		var guideline = d3.select("[id=basicInfoBox_div]").select("[id=view4]")
				.append("g")
				.append("div")
				.style("width","900px")
				.style("margin","auto")
				
		guideline.append("div")
				.style("width","100%")
				.style("font-size","21px")
				.text("To run data mining, select target solutions on the scatter plot. Then click the button below.");

		guideline.append("div")
				.style("width","300px")
				.style("margin","auto")
				.append("button")
				.attr("id","getDrivingFeaturesButton")
				.style("margin-top","30px")
				.style("width","200px")
				.style("font-size","19px")
				.text("Run data mining");
		d3.selectAll("[id=getDrivingFeaturesButton]").on("click", runDataMining);
}



function set_selection_option(selected_option){
	if(selected_option=="1"){
		d3.select("#zoom")[0][0].checked=true;
		d3.select("#drag-select")[0][0].checked=false;
		d3.select("#de-select")[0][0].checked=false;
	}else if(selected_option=="2"){
		d3.select("#zoom")[0][0].checked=false;
		d3.select("#drag-select")[0][0].checked=true;
		d3.select("#de-select")[0][0].checked=false;
	}else{
		d3.select("#zoom")[0][0].checked=false;
		d3.select("#drag-select")[0][0].checked=false;
		d3.select("#de-select")[0][0].checked=true;
	}
	scatterPlot_option(selected_option)
}


function round_num_2_perc(num){
	return Math.round((num + 0.01) * 100);
}



