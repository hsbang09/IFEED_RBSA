/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var orbitList_displayName = ["1000","2000","3000","4000","5000"];
var instrList_displayName = ["A","B","C","D","E","F","G","H","I","J","K","L"];

/*
 * @param {int} index: Number indicating either an oribt or an instrument
 * @param {String} type: Type of the input name. Could be either "orbit" or "instrument"
 * @returns The actual name of an instrument or an orbit
 */
function Index2ActualName(index, type){
    if(type=="orbit"){
        return orbitList[index];
    }else if(type=="instrument"){
        return instrList[index];
    }else{
        return "Naming Error"
    }
}

/*
 * @param {int} index: Number indicating either an orbit or an instrument
 * @param {String} type: Type of the variable. Could be either "orbit" or "instrument"
 */
function Index2DisplayName(index, type){
    if(type=="orbit"){
        return orbitList_displayName[index];
    }else if(type=="instrument"){
        return instrList_displayName[index];
    }else{
        return "Naming Error";
    }
}

function ActualName2Index(name, type){
    var name=name.trim();
    
    if(name.indexOf(",")!=-1){
        var names = name.split(",");
        var newName = "";
        for(var i=0;i<names.length;i++){
            var comma = ",";
            if(i==0){
                comma = "";
            }
            if(type=="orbit"){
                newName = newName + comma + $.inArray(names[i],orbitList);
            }else if(type=="instrument"){
                newName = newName + comma + $.inArray(names[i],instrList);
            }else{
                newName = newName + comma + "Naming Error";
            }              
        }
        return newName;
    }else{
        if(type=="orbit"){
            return $.inArray(name,orbitList);
        }else if(type=="instrument"){
            return $.inArray(name,instrList);
        }else{
            return "Naming Error";
        }        
    }
}


function DisplayName2Index(name, type){
    var name=name.trim();
    if(type=="orbit"){
        return $.inArray(name,orbitList_displayName);
    }else if(type=="instrument"){
        return $.inArray(name,instrList_displayName);
    }else{
        return "Naming Error";
    }
}



function ActualName2DisplayName(name,type){
    var name = name.trim();
    if(type=="orbit"){
        var nth = $.inArray(name,orbitList);
        if(nth==-1){// Couldn't find the name from the list
            return name;
        }
        return orbitList_displayName[nth];
    } else if(type=="instrument"){
        var nth = $.inArray(name,instrList);
        if(nth==-1){ // Couldn't find gthe name from the list
            return name;
        }
        return instrList_displayName[nth];
    } else{
        return name;
    }
}


function DisplayName2ActualName(name,type){
    var name = name.trim();
    if(type=="orbit"){
        var nth = $.inArray(name,orbitList_displayName);
        if(nth==-1){// Couldn't find the name from the list
            return name;
        }
        return orbitList[nth];
    } else if(type=="instrument"){
        var nth = $.inArray(name,instrList_displayName);
        if(nth==-1){ // Couldn't find gthe name from the list
            return name;
        }
        return instrList[nth];
    } else{
        return name;
    }
}


//        A          B         C          D         E        F
// {"ACE_ORCA","ACE_POL","ACE_LID","CLAR_ERB","ACE_CPR","DESD_SAR",
// 
//       G        H           I            J         K              L
// "DESD_LID","GACM_VIS","GACM_SWIR","HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};
// 
//      1000                2000            3000        4000            5000
//{"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};



function Name2Index(name,type){
    var name = name.trim();
    var temp = DisplayName2Index(name,type);
    if(name!=temp+""){
        return temp;
    }else{
        return ActualName2Index(name,type);
    }
}



function ppdf(expression,preset){
    if(preset){
                
        var exp = expression;
        if(exp[0]==="{"){
            exp = exp.substring(1,exp.length-1);
        }
        var featureName = exp.split("[")[0];
        var featureArg = exp.split("[")[1];
        featureArg = featureArg.substring(0,featureArg.length-1);
        
        var orbits = featureArg.split(";")[0].split(",");
        var instruments = featureArg.split(";")[1].split(",");
        var numbers = featureArg.split(";")[2];
        
        var pporbits="";
        var ppinstruments="";
        for(var i=0;i<orbits.length;i++){
            if(orbits[i].length===0){
                continue;
            }
            if(i>0){pporbits = pporbits + ",";}
            pporbits = pporbits + Index2ActualName(orbits[i], "orbit");
        }
        for(var i=0;i<instruments.length;i++){
            if(instruments[i].length===0){
                continue;
            }
            if(i>0){ppinstruments = ppinstruments + ",";}
            ppinstruments = ppinstruments + Index2ActualName(instruments[i], "instrument");
        }
        var ppexpression = featureName + "[" + pporbits + ";" + ppinstruments + ";" + numbers + "]";
        return ppexpression;
    } else{
        return expression;
    }    
}