package dsaab_project;
import java.util.*;
import dsaab_project.*;
public class LiveWire {
    private int width, height;
    private double[][] g;
    private Pixel[][] p;
    private boolean[][] processed;
    public double[][] path_costs;
    public LiveWire(double[][] path_costs) {
        this.width = path_costs[0].length;
        this.height = path_costs.length;
        this.g = new double[width][height];
        this.p = new Pixel[width][height];
        this.processed = new boolean[width][height];
        this.path_costs = path_costs;
        // 初始化成本为无穷大
        for (double[] row : g) Arrays.fill(row, Double.MAX_VALUE);
    }
    
    public Pixel[][] computePaths(int startX, int startY) {
        PriorityQueue<Pixel> activeList = new PriorityQueue<>();
        p[startX][startY]=null;
        g[startX][startY] = 0;
        activeList.add(new Pixel(startX, startY, 0));
        
        while (!activeList.isEmpty()) {
            Pixel q = activeList.poll();
            int x = q.x, y = q.y;
            if (processed[x][y]) continue;
            processed[x][y] = true;
            
            // 遍历8邻域
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx, ny = y + dy;
                    if (ny < 0 || ny >= height || nx < 0 || nx >= width) continue;
                    
                    double newCost = g[x][y] + localCost(dx, dy, ny, nx);
                    if (newCost < g[nx][ny]) {
                        g[nx][ny] = newCost;
                        p[nx][ny] = new Pixel(x, y,newCost);
                        activeList.add(new Pixel(nx, ny, newCost));
                    }
                }
            }
        }
        return p;
    }
    
    // 
    private double localCost(int x1, int y1, int x2, int y2) {
        if(x1 == 0 || y1 == 0) return path_costs[x2][y2];
        else return Math.sqrt(2)*path_costs[x2][y2];
    }
    
   
}
