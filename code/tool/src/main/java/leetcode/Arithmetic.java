package leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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

	public static void main(String[] args) {
		Arithmetic a = new Arithmetic();
		String[] test = { "Hello","Alaska","Dad","Peace" };
		System.out.println(a.findWords(test));
	}
}
