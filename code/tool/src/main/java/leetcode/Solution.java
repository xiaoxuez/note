package leetcode;

import com.esotericsoftware.minlog.Log;

/**
 * Created by xiaoxuez on 2017/8/4.
 */
public class Solution {

    public static void main(String[] args){
        int[][] people = new int[][]{{7,0}, {4,4}, {7,1}, {5,0}, {6,1}, {5,2}};
        new Solution().reconstructQueue(people);
        for (int i = 0; i < people.length; i++)
            System.out.println(people[i][0] + "," + people[i][1] );
    }

    public int[][] reconstructQueue(int[][] people) {
        for(int i = 0; i < people.length; i++){
            //向前寻找k（h）
            for(int k = 0; k < i; k++){
                if(people[i][1] < people[k][1]){
                    changePosition(people, i, k);
                }else if(people[i][1] == people[k][1]){
                    if(people[i][0] <= people[k][0])
                        changePosition(people, i, k);
                    else
                        changePosition(people, i, k + 1);
                }
            }
        }

        for (int n = 0; n < people.length; n++){
            boolean allRight = true;
            for(int i = 0; i < people.length; i++){
                if(!rightPlace(people, i)){
                    //数据不对，向前移动，直到ok
                    boolean right = false;
                    int newPlace = i - 1;
                    for(int k = newPlace + 1; k >= 0; k--){
                        changePosition(people, k, k - 1);
                        newPlace = k - 1;
                        if(rightPlace(people, newPlace)){
                            right = true;
                            break;
                        }
                    }
                    allRight = allRight && right;
                }
            }
            if (allRight)
                break;
        }
        return people;
    }

    //将from出的数据项移动到to处，同时to之后的数据后移
    private void changePosition(int[][] people, int from, int to){
        if(from == to)
            return;
        int[] temp = people[to];
        people[to] = people[from];
        for(int i = from - 1; i >= to + 1 ; i--){
            people[i+1] = people[i];
        }
        people[to + 1] = temp;
    }

    //which的位置是否正确
    private boolean rightPlace(int[][] people, int which){
        int total = 0;
        for(int i = 0; i < which; i++){
            if(people[i][0] >= people[which][0])
                total++;
        }
        return total == people[which][1];
    }
}