package GrammarAnalysis;

import java.util.Vector;

/**
 * Created by yancychan on 17-6-19.
 */
public class Production {
    private String[] productions={"S'->E","E->E+T","E->T","T->T*F","T->F","F->(E)","F->i"};
    private int[] numOfP={1,3,1,3,1,3,1};
    private Vector<String> productionList=new Vector<String>();

    public Production() {
        for(String st:productions)
            productionList.addElement(st);
    }

    public String getProduction(int i){
        return productions[i];
    }

    public int getNumOfP(int i){
        return numOfP[i];
    }
}
