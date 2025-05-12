package dsaab_project;
import java.util.*;
import dsaab_project.*;
class Pixel implements Comparable<Pixel> {
    int x, y;
    double cost;
    
     public Pixel(int x, int y, double cost) {
        this.x = x;
        this.y = y;
        this.cost = cost;
    }
    
   

    @Override
    public int compareTo(Pixel other) {
        return Double.compare(this.cost, other.cost);
    }
}