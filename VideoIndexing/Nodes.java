package VideoIndexing;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;

public class Nodes {

    final private int frameVal;
    private ArrayList<Nodes> children;
    public Nodes(int timeFrame){
        this.frameVal = timeFrame;
        this.children = new ArrayList<>(10);
    }
    public void add(Nodes e){
        this.children.add(e);
    }
    public int getChildrenCount(){
        return this.children.size();
    }
    public Nodes getChild(int i){
        return this.children.get(i);
    }
    public int getVal(){
        return this.frameVal;
    }

    public String toString() {
        if(this.children.size()>0)
        return this.children.toString();
        else
            return String.valueOf(this.frameVal);
    }
}
