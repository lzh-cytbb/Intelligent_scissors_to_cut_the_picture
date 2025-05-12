package dsaab_project;
import java.util.*;
import dsaab_project.*;

public class Get_path {
    public int[][][] image;
    public Pixel[][] all_paths;
    double[][] path_cost;

    //起点不包括
    public Get_path(int[][][] image){
        this.image=image;
        this.path_cost=new double[image.length][image[0].length];
    }
    public void get_all_path_from_start(int start_x,int start_y){
        LiveWire lw=new LiveWire(get_cost());
        all_paths=lw.computePaths(start_x,start_y);
    }

    public StringBuilder[] get_path_to_end(int end_x,int end_y){
        StringBuilder[] path=new StringBuilder[2];
        path[0] = new StringBuilder();
        path[1] = new StringBuilder();
        int current_x=end_x;
        int current_y=end_y;
        if(all_paths[current_x][current_y]==null){
            path[0].append(current_x);
            path[1].append(current_y);
            return path;
        }
        while(all_paths[current_x][current_y]!=null){
            path[0].append(current_x).append(",");
            path[1].append(current_y).append(",");
            int temp_x=current_x;
            int temp_y=current_y;
            current_x=all_paths[temp_x][temp_y].x;
            current_y=all_paths[temp_x][temp_y].y;
        }
        if(path[0].length()>0 && path[1].length()>0){
            path[0].deleteCharAt(path[0].length()-1);
            path[1].deleteCharAt(path[1].length()-1);}
        return path;
    }




    public double[][] get_cost (){
//把RGB图像转为灰度图像
        double[][]gray=new double[image.length][image[0].length];
        for(int i=0;i<image.length;i++){
            for(int j=0;j<image[0].length;j++){
                gray[i][j]=0.299*image[i][j][0]+0.587*image[i][j][1]+0.114*image[i][j][2];
            }
        }

//卷积上内核获得G(x,y)
        double[][] G=new double[gray.length][gray[0].length];
        double[][] Ix=calculate_Ix(gray);
        double[][] Iy=calculate_Iy(gray);
        for(int i=0;i<Ix.length;i++){
            for(int j=0;j<Ix[0].length;j++){
                G[i][j]=Math.sqrt(Ix[i][j]*Ix[i][j]+Iy[i][j]*Iy[i][j]);
            }
        }
//计算路径成本
        for(int i=0;i<path_cost.length;i++){



            for(int j=0;j<path_cost[0].length;j++){
if(i==0 || i==path_cost.length-1 || j==0 || j==path_cost[0].length-1){
    path_cost[i][j]=0.00;
}
else{
                    path_cost[i][j]=1/(1+G[i][j]);}
            }
        }
        return path_cost;
    }

    public double[][] calculate_Ix(double[][] gray){
        //数组外围加0
//int kernel_x[][]={{-1,0,1},{-2,0,2},{-1,0,1}};
        int kernel_x_turn[][]={{-1,-2,-1},{0,0,0},{1,2,1}};
        double temp_gray[][]=new double[gray.length+2][gray[0].length+2];
        for(int i=0;i<gray.length;i++){
            for(int j=0;j<gray[0].length;j++){
                temp_gray[i+1][j+1]=gray[i][j];
            }
        }
        double [][] Ix=new double[gray.length][gray[0].length];
        for(int i=0;i<gray.length;i++){
            for(int j=0;j<gray[0].length;j++){
                for(int k=0;k<kernel_x_turn.length;k++){
                    for(int l=0;l<kernel_x_turn[0].length;l++){
                        Ix[i][j]+=temp_gray[i+k][j+l]*kernel_x_turn[k][l];
                    }
                }

            }
        }
        return Ix;
    }

    public double[][] calculate_Iy(double[][] gray){
        //数组外围加0
        int kernel_y_turn[][]={{-1,0,1},{-2,0,2},{-1,0,1}};
//int kernel_x_turn[][]={{-1,-2,-1},{0,0,0},{1,2,1}};
        double temp_gray[][]=new double[gray.length+2][gray[0].length+2];
        for(int i=0;i<gray.length;i++){
            for(int j=0;j<gray[0].length;j++){
                temp_gray[i+1][j+1]=gray[i][j];
            }
        }
        double [][] Iy=new double[gray.length][gray[0].length];
        for(int i=0;i<gray.length;i++){
            for(int j=0;j<gray[0].length;j++){
                for(int k=0;k<kernel_y_turn.length;k++){
                    for(int l=0;l<kernel_y_turn[0].length;l++){
                        Iy[i][j]+=temp_gray[i+k][j+l]*kernel_y_turn[k][l];
                    }
                }

            }
        }
        return Iy;
    }
    //附加功能1：路径冷却
    public int[] path_cooling(StringBuilder[] path1,StringBuilder[] path2){
        int[] new_start=new int[2];
        String[] xCoords1 = path1[0].toString().split(",");
        String[] yCoords1 = path1[1].toString().split(",");

        String[] xCoords2 = path2[0].toString().split(",");
        String[] yCoords2 = path2[1].toString().split(",");
        int count1 =xCoords1.length-1;
        int count2 =xCoords2.length-1;
        int pre_count1=count1;
        int pre_count2=count2;
        while (true){
            if(xCoords1[count1].equals(xCoords2[count2]) && yCoords1[count1].equals(yCoords2[count2])){
                pre_count1=count1--;
                pre_count2=count2--;
                if(count1==-1||count2==-1){
                    new_start[0]=Integer.parseInt(xCoords2[pre_count2]);
                    new_start[1]=Integer.parseInt(yCoords2[pre_count2]);
                    return  new_start;
                }
            }
            else{
                new_start[0]=Integer.parseInt(xCoords2[pre_count2]);
                new_start[1]=Integer.parseInt(yCoords2[pre_count2]);
                return new_start;
            }
        }
    }

    //附加功能2：光标追踪边缘
    public int[] Cursor_Snap (int x,int y,int size){
        if(path_cost[y][x]<=0.03){
            System.out.println("光标正在边缘，不需要强制移动");
            return new int[]{0,y,x};
        }
        else{
            // ArrayList <Integer> x_list=new ArrayList<>();
            // ArrayList <Integer> y_list=new ArrayList<>();
            boolean flag=false;
            int min_x=0,min_y=0;
            double min_pastcost=Double.MAX_VALUE;
            for(int i=x-size/2;i<=x+size/2;i++){
                for(int j=y-size/2;j<=y+size/2;j++){
                    if(j<=0 || j>=path_cost.length || i<=0 || i>=path_cost[0].length )continue;
                    if(path_cost[j][i]<=0.10){
                        flag=true;
                        if(path_cost[j][i]<min_pastcost){
                            min_x=j;
                            min_y=i;
                            min_pastcost=path_cost[j][i];
                        }

                        // System.out.println("发现光标附近的边缘，已完成光标追踪");
                        // return new int[]{1,j,i};
                    }
                }
            }
            if(flag){
                System.out.println("发现光标附近的边缘，已完成光标追踪");
                return new int[]{1,min_x,min_y};
            }


            System.out.println("光标附近没有边缘，无法完成光标追踪");
            return new int[]{0,y,x};

        }
    }

    // public static void main(String[] args) {
    //     ImageReader reader=new ImageReader();
    //     int[][][] image=reader.return_array("C:\\Users\\Hi\\Desktop\\img1.png");
    //     Get_path gp=new Get_path(image);
    //     gp.get_all_path_from_start(100,100);
    //     StringBuilder[] path=gp.get_path_to_end(100,100);
    //     String[] xCoords = path[0].toString().split(",");
    //     String[] yCoords = path[1].toString().split(",");
    //     for (int i = xCoords.length-1; i >= 0; i--) {
    //         if (!xCoords[i].isEmpty() && !yCoords[i].isEmpty()) {
    //             System.out.printf("%03d %03d%n", Integer.parseInt(xCoords[i]), Integer.parseInt(yCoords[i]));
    //             System.out.println("----------------");
    //         }
    //     }
    // }

}