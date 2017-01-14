/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function relabel(input){  

    var newOrbitList = ["1000","2000","3000","4000","5000"];
    var newInstrList = ["A","B","C","D","E","F","G","H","I","J","K","L"];

    if(input.indexOf("-")!==-1){
        var nth = $.inArray(input,orbitList);
        if(nth==-1){
            return input;
        }
        return newOrbitList[nth];
    } else if(input.indexOf("_")!==-1){
        var nth = $.inArray(input,instrList);
        if(nth==-1){
            return input;
        }
        return newInstrList[nth];
    } else{
//        console.log("could not find orbit or instrument name: " + input);
        return input;
//                        error("could not find orbit or instrument name");
    }
}
//        A          B         C          D         E        F
// {"ACE_ORCA","ACE_POL","ACE_LID","CLAR_ERB","ACE_CPR","DESD_SAR",
//       G        H           I            J         K              L
// "DESD_LID","GACM_VIS","GACM_SWIR","HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};
//{"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};

function relabelback(input){   // has to match with the method in Scheme.java

    var newOrbitList = ["1000","2000","3000","4000","5000"];
    var newInstrList = ["A","B","C","D","E","F","G","H","I","J","K","L"];

    if(input.indexOf("000")!==-1){
        var nth = $.inArray(input,newOrbitList);
        if(nth==-1){
            return input;
        }
        return orbitList[nth];
    } else if(input.length===1) {
    	input = input.toUpperCase();
        var nth = $.inArray(input,newInstrList);
        if(nth==-1){
            return input;
        }
        return instrList[nth];
    } else{
        return input;
    }
}   



function relabelDrivingFeatureName(name){
    var open = name.indexOf("[");
    var close = name.indexOf("]");
    var type = name.substring(0,open);
    var name_tmp = name.substring(open+1,close);
    var output = "";
    var first = true;
    
    while(true){
        if(name_tmp.indexOf(",")===-1){
            if(first){
                name_tmp = name_tmp.trim();
                output = output + relabel(name_tmp);
            }else{
                name_tmp = name_tmp.trim();
                output = output + relabel(name_tmp);
            }
            break;
        } else {
            var tmp = name_tmp.substring(0,name_tmp.indexOf(","));
            tmp = tmp.trim();
            output = output  + relabel(tmp) + ",";
            name_tmp = name_tmp.substring(name_tmp.indexOf(",")+1);
            first = false;
        } 
    }
    output = type + "[" + output + "]";
    
    if(output==="[]"){
        return name;
    }
    
    return output;
}