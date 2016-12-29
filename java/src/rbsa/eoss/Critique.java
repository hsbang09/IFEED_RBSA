/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import jess.Rete;

/**
 *
 * @author Bang
 */

public class Critique {
    
    private Result resu;
    private Architecture arc;
    private Resource res;
    private Rete r;
    
    public void addRule(String inputRule){
    }
    public void getCritique(){
    }
    
    public Critique(Result resu){
        this.resu = resu;
        this.arc = resu.getArch();
        
        this.res = new Resource();
        this.r = res.getRete();
        
        try{
            
        }catch(Exception e){
            System.out.println(e);
        }
               
    }
    
    
    
    
}
