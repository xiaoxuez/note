package leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.core.env.SystemEnvironmentPropertySource;

public class Arithmetic {

	/**
	 * 292. Nim Game You are playing the following Nim Game with your friend:
	 * There is a heap of stones on the table, each time one of you take turns
	 * to remove 1 to 3 stones. The one who removes the last stone will be the
	 * winner. You will take the first turn to remove the stones. Both of you
	 * are very clever and have optimal strategies for the game. Write a
	 * function to determine whether you can win the game given the number of
	 * stones in the heap.
	 * 
	 * For example, if there are 4 stones in the heap, then you will never win
	 * the game: no matter 1, 2, or 3 stones you remove, the last stone will
	 * always be removed by your friend.
	 */
	// 这个题吧 一开始想的是用递归，结果出现了数据大的时候堆栈溢出的情况.. 百度了一下 别人只用一行代码就搞定了...
	// 分析是这样的，
	// 1: 先手必胜 true
	// 2: 先手必胜 true
	// 3: 先手必胜 true
	// 4: 先手必败 fasle
	// 5: 先手拿1颗 让对手面对4的情况 先手必胜 true
	// 6: 先手拿2颗 让对手面对4的情况， 先手必胜 true
	// 7: 先手拿3颗， 让对手面对4的情况， 先手必胜 true
	// 8: 完了 先手必败..fasle
	public boolean canWinNim(int n) {
		return n % 4 != 0;
	}

	/**
	 * 485. Max Consecutive Ones Given a binary array, find the maximum number
	 * of consecutive 1s in this array.
	 * 
	 * Example 1: Input: [1,1,0,1,1,1] Output: 3 Explanation: The first two
	 * digits or the last three digits are consecutive 1s. The maximum number of
	 * consecutive 1s is 3. Note:
	 * 
	 * The input array will only contain 0 and 1. The length of input array is a
	 * positive integer and will not exceed 10,000
	 */
	public int findMaxConsecutiveOnes(int[] nums) {
		int sum = 0, count = 0;
		for (int i = 0; i < nums.length; i++) {
			int result = nums[i] ^ 0;
			if (result == 0) {
				sum = count > sum ? count : sum;
				count = 0;
				continue;
			}
			count++;
		}
		sum = count > sum ? count : sum;
		return sum;
	}

	/**
	 * 500. Keyboard Row 满足条件为键盘上在一排的单词
	 * 
	 * @param args
	 *            Input: ["Hello", "Alaska", "Dad", "Peace"] Output: ["Alaska",
	 *            "Dad"]
	 */
	// 一开始想的是将键盘上的字母存起来再匹配，发现还有正则的。
	public String[] findWords(String[] words) {

		List<String> list = new ArrayList<>();
		if (words != null) {
			String regs = "[qwertyuiop]*|[asdfghjkl]*|[zxcvbnm]*";
			for (String string : words) {
				if (string.toLowerCase().matches(regs)) {
					list.add(string);
				}
			}
		}
		String[] strings = new String[list.size()];
		list.toArray(strings);
		return strings;
	}

	/**
	 * 547. Friend Circles m[i, k] = 1, m[k, j] = 1, m[i, j]有朋友关系。 [[1,1,0],
	 * [1,1,0], [0,0,1]] Output: 2
	 * 
	 * [[1,1,0], [1,1,1], [0,1,1]] Output: 1
	 * 
	 * 查有多少朋友圈就是查多少个联通量， 要义是m[i][k]=1, 若m[k][j]=1， 则ikj都是通的，就是若m[i][k]=1，
	 * 应该遍历m[k][x] 所以应当有一个标识该行是否被遍历的数组，用来判断是否应当进行遍历。
	 * 
	 * n*n的矩阵m， 大循环： 按行进行循环， 1*n的矩阵v, 代表该行是否被遍历过, 大循环中， 若该行被遍历过，则直接跳过 小循环： 行确定后，
	 * 按列进行循环， 若m[i][j] = 1, 则将j 作为行， 再继续小循环， 能遍历跟m[i]是朋友的所有朋友,
	 * 因为将j作为行进行小循环了，所以将v[j] = 1标识该行已经被遍历过。 大循环进行的次数，就是联通量的个数。
	 */

	public static void dfs(int[][] M, int[] visited, int i) {
		for (int j = 0; j < visited.length; j++) {
			if (M[i][j] == 1 && visited[j] == 0) {
				visited[j] = 1;
				dfs(M, visited, j);
			}
		}
	}

	public static int findCircleNum(int[][] M) {
		int count = 0;
		int[] visited = new int[M.length];
		for (int i = 0; i < visited.length; i++) {
			if (visited[i] == 0) {
				dfs(M, visited, i);
				count++;
			}
		}
		return count;
	}

	/**
	 * 442. Find All Duplicates in an Array Given an array of integers, 1 ≤ a[i]
	 * ≤ n (n = size of array), some elements appear twice and others appear
	 * once. Find all the elements that appear twice in this array. Could you do
	 * it without extra space and in O(n) runtime? 入口点： 1 ≤ a[i] <=n ,
	 * 就是说这个数组里的数都是小于长度n的且大于1的，如果以数作为下标索引a数组，若数重复出现，则索引重复出现。 故 索引后进行计算+n，
	 * 若有重复的数，进行计算的次数一定>1, 所以最后判断计算的结果是否>2n即可，
	 * 
	 * @param args
	 */
	public List<Integer> findDuplicates(int[] nums) {
		List<Integer> result = new ArrayList<Integer>();

		if (nums == null || nums.length == 0)
			return result;
		int n = nums.length;
		for (int i = 0; i < n; i++) {
			nums[(nums[i] - 1) % n] += n;
		}
		for (int i = 0; i < n; i++) {
			if (nums[i] > 2 * n)
				result.add(i + 1);
		}
		return result;
	}

	/**
	 * 413. Arithmetic Slices A sequence of number is called arithmetic if it
	 * consists of at least three elements and if the difference between any two
	 * consecutive elements is the same. A = [1, 2, 3, 4] return: 3, for 3
	 * arithmetic slices in A: [1, 2, 3], [2, 3, 4] and [1, 2, 3, 4] itself.
	 * 
	 * @param A
	 * @return
	 */
	public int numberOfArithmeticSlices(int[] A) {
		int number = 0;
		for (int i = 0; i < A.length - 2; i++) {
			int differ = A[i] - A[i + 1];
			for (int j = i + 2; j < A.length; j++) {
				if (differ == A[j - 1] - A[j]) {
					number++;
				} else {
					break;
				}
			}
		}
		return number;
	}

	/**
	 * 582. Kill Process Given n processes, each process has a unique PID
	 * (process id) and its PPID (parent process id). 给两个数组， 第一个是pids,
	 * 第二个数组对应的是ppid Input: pid = [1, 3, 10, 5] ppid = [3, 0, 5, 3] kill = 5
	 * Output: [5,10] Explanation: 3 / \ 1 5 / 10 Kill 5 will also kill 10.
	 * 
	 * @param args
	 */
	public List<Integer> killProcess(List<Integer> pid, List<Integer> ppid, int kill) {
		Map<Integer, Set<Integer>> allFatherSon = new HashMap<>();
		for (int i = 0; i < ppid.size(); i++) {
			if (!allFatherSon.containsKey(ppid.get(i))) {
				Set<Integer> sons = new HashSet<>();
				sons.add(pid.get(i));
				allFatherSon.put(ppid.get(i), sons);
			} else {
				allFatherSon.get(ppid.get(i)).add(pid.get(i));
			}
		}

		ArrayList<Integer> delete = new ArrayList<>();
		Queue<Integer> queue = new LinkedBlockingQueue<>();
		queue.offer(kill);
		Integer father;
		while ((father = queue.poll()) != null) {
			delete.add(father);
			Set<Integer> sons = allFatherSon.get(father);
			if (sons != null) {
				for (Integer integer : sons) {
					queue.offer(integer);
				}
			}
		}
		return delete;
	}

	public static void main(String[] args) {
		Arithmetic a = new Arithmetic();
		System.out.println(new Date().getTime());
		List<Integer> all = a.killProcess(Arrays.asList(1, 3, 10, 5), Arrays.asList(3, 0, 5, 3), 5);
		System.out.println(new Date().getTime());
		for (Integer integer : all) {
			System.out.println(integer);
		}
	}
}
