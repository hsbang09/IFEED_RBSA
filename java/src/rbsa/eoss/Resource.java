/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

/**
 *
 * @author Marc
 */
import jess.Rete;

public class Resource {
    
    private Rete r;
//    private Rete r2;
    private QueryBuilder qb;
//    private QueryBuilder qb2;
    private MatlabFunctions m;
//    private MatlabFunctions m2;
    
    public Resource()
    {
        r = new Rete();
//        r2 = new Rete();
        qb = new QueryBuilder( r );
//        qb2 = new QueryBuilder(r2);
        m = new MatlabFunctions(this);

        r.addUserfunction(m);
//        r2.addUserfunction(m);
        
        JessInitializer.getInstance().initializeJess( r, qb, m );
//        JessInitializer.getInstance().initializeJess( r2, qb2, m);
        
    }
    
    public Rete getRete()
    {
        return r;
    }
//    public Rete getRete2(){
//        return r2;
//    }
    
    public QueryBuilder getQueryBuilder()
    {
        return qb;
    }
//    public QueryBuilder getQueryBuilder2(){
//        return qb2;
//    }

    public MatlabFunctions getM() {
        return m;
    }
    
}
