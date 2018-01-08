package leetcode;

import java.util.*;
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
	 *
	 * input : ["Hello", "Alaska", "Dad", "Peace"] Output: ["Alaska",
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
	 * @param
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
	 * @param
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

	/**
	 * 592. Fraction Addition and Subtraction Input:"-1/2+1/2" Output: "0/1"
	 * 解题思路： 拆分数据，进行计算。
	 * 
	 * @param expression
	 * @return
	 */
	public String fractionAddition(String expression) {
		List<Character> sign = new ArrayList<>();
		for (int i = 1; i < expression.length(); i++) {
			if (expression.charAt(i) == '+' || expression.charAt(i) == '-')
				sign.add(expression.charAt(i));
		}
		// 分子
		List<Integer> num = new ArrayList<>();
		// 分母
		List<Integer> den = new ArrayList<>();
		for (String sub : expression.split("\\+")) {
			for (String subsub : sub.split("-")) {
				if (subsub.length() > 0) {
					String[] fraction = subsub.split("/");
					num.add(Integer.parseInt(fraction[0]));
					den.add(Integer.parseInt(fraction[1]));
				}
			}
		}
		// 第一个数据是正的则忽略+.但是负的则不忽略，所以需要特殊处理
		if (expression.charAt(0) == '-')
			num.set(0, -num.get(0));
		// mul为所有分母乘积， g为最大公约数。 那么最小公倍数就为mul/g
		int mul = 1, g = 0;
		for (int x : den) {
			mul *= x;
			g = gcd(g, x);
		}
		int lcm = mul / g;
		int res = lcm / den.get(0) * num.get(0); // 第一个数比较特别，所以单独存放，这里是计算所有分子之和。
		for (int i = 1; i < num.size(); i++) {
			if (sign.get(i - 1) == '+')
				res += lcm / den.get(i) * num.get(i);
			else
				res -= lcm / den.get(i) * num.get(i);
		}
		g = gcd(Math.abs(res), Math.abs(lcm));
		return (res / g) + "/" + (lcm / g);
	}

	public int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/**
	 * 452. Minimum Number of Arrows to Burst Balloons Input: [[10,16], [2,8],
	 * [1,6], [7,12]]
	 * 
	 * Output: 2
	 * 
	 * @param
	 */
	public int findMinArrowShots(int[][] points) {
		List<List<Integer>> sets = new ArrayList<>();
		for (int i = 0; i < points.length; i++) {
			boolean newSet = false;
			for (List<Integer> list : sets) {
				if ((points[i][1] >= list.get(0) && points[i][1] <= list.get(1))
						|| (points[i][1] >= list.get(1) && points[i][0] <= list.get(1))) {

				}
			}
		}
		return sets.size();
	}

	/**
	 * 137. Single Number II Given an array of integers, every element appears
	 * three times except for one, which appears exactly once. Find that single
	 * one. 解题思路，其他的数都出现了3次，只要一个数出现了一次,
	 * 相加的话，出现3次的数的和一定是3的整数。也就是说如果这些数都比3小的话，求和取3的余就能得答案。
	 * 所以转换一下思想，如果是3进制的加法的话，某一个加到了3就清零(取3的余)，那么加到最后剩下的数就是答案。
	 * 最后，采用二进制模拟3进制，二进制的加法，需要有存放进位，存放无进位的和的变量，当和与进位对应位上都为1的情况下，就说明现在对应位上加到了3，应该清零了。
	 * 
	 * @param
	 */

	public int singleNumber(int[] nums) {
		int sums = 0, carrys = 0;
		for (int i = 0; i < nums.length; i++) {
			carrys |= sums & nums[i]; // 进位，
										// sums与nums[i]为当前是否有进位，总进位应该跟当前进行或取所有位的进位
			sums ^= nums[i]; // 无进位的加法 就是异或
			// 判断是否有3

			int full = ~(carrys & sums); // 对应位都为1的情况,应该清零，后面用&方便一点的话，所以应该取反
			carrys = full & carrys;
			sums = full & sums;
		}
		return sums;
	}

	/**
	 * 260. Single Number III Given an array of numbers nums, in which exactly
	 * two elements appear only once and all the other elements appear exactly
	 * twice. Find the two elements that appear only once. For example: Given
	 * nums = [1, 2, 1, 3, 2, 5], return [3, 5]. Note: The order of the result
	 * is not important. So in the above example, [5, 3] is also correct.
	 * 这个题吧，说实话很忧桑.... 看了别人的答案发现别人真机智... 反正我是想不到这个答案。还是开始解题吧,以上例为例
	 * 1.首先，其他的都只出现了2次，很明显这个题用异或。关键在于一次全部异或的结果是3^5，难点在于怎么把这个3和5分解出来。
	 * 2.还是从其他的都出现了2次入手，如果能将3和5分开进行异或，就是根据某个规则，if/else 3和5分别被分到两个分支中进行异或..
	 * 最后就有结果啦。 那么规则是什么呢，3和5都只出现了一次，3^5的结果肯定不会都是0，肯定某位上有1.那么根据这个1，这个1的得来是
	 * 3和5中相应对应位为1，相应为0,只要把这1位置为1其他的置为0，那么和3或5进行与的结果就是0和1.
	 * 
	 * @param nums
	 * @return
	 */
	public int[] singleNumber3(int[] nums) {
		int axorb = 0;
		for (int i : nums) {
			axorb = axorb ^ i;
		}

		int differ = axorb & ~(axorb - 1);
		int[] results = new int[2];
		for (int i : nums) {
			if ((differ & i) == 0) {
				results[0] = results[0]^i;
			} else {
				results[1] = results[1]^i;
			}
		}
		return results;
	}

	public int jump(int[] nums) {
		// return jumpToMax(nums, 0, 0);

		if (nums.length <= 1) {
			return nums.length % 1;
		}
		int start = 0, count = 1, max = 0, end = nums[0];
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] > nums[max]) {
				max = i;
			}
			if (i == end) {
				if (max == start) {
					start = end;
				} else {
					start = max;
				}
				count++;
				// max = ;
				end = i + nums[i];

				if (end >= nums.length - 1) {
					break;
				}

			}
			System.out.println(" i= " + i + ", max=" + max + ", end=" + end + " , count=" + count);
		}
		return count;
	}

	// public int jumpToMax(int[] nums, int index, int count) {

	// if (index == nums.length - 1) {
	// return count;
	// }
	// count ++;
	//
	// if (nums[index] >= nums.length - index) {
	// return count;
	// }
	// int maxI = index + 1;
	// for (int i = index + 2; i <= index + nums[index]; i++) {
	// if (i == nums.length - 1) {
	// return count;
	// }
	// if (nums[i] > nums[maxI]) {
	// maxI = i;
	// }
	// }

	// return jumpToMax(nums, maxI, count);
	// }

	/**
	 *
	 * Suppose you have a random list of people standing in a queue. Each person is described by a pair of integers (h, k), where h is the height of the person and k is the number of people in front of this person who have a height greater than or equal to h. Write an algorithm to reconstruct the queue.
	 * @param people
	 * @return
	 * Input:
	 *[[7,0], [4,4], [7,1], [5,0], [6,1], [5,2]]
	 *Output:
	 *[[5,0], [7,0], [5,2], [6,1], [4,4], [7,1]]
	 *
	 * TODO: 还没解决，看网上的解法，和我想法类似，排序想法是一样的，就是装的不一样，我的想法是排一次序，慢慢装，看网上的
	 * 是循环次次排，并且给每一个数据加一项，[h, n] => [h, n',m],m为结果里已经放了比它大的个数， n' + m = n, 然后次次按n和h排，
	 * 每一次放入结果的数即为第一个。
	 */
	public int[][] reconstructQueue(int[][] people) {
		List<People> people1 = new ArrayList<>();
		for (int i = 0; i < people.length; i++) {
			people1.add(new People(people[i][0], people[i][1]));
		}
		Collections.sort(people1);
		LinkedList<People> lList = new LinkedList<People>();
		People pLast = null;
		for (People p: people1 ) {
			if (p.num == 0) {
				lList.add(p);
			} else {
				if (pLast.num == p.num) {
					 insert(p, lList, lList.indexOf(pLast));
				} else {
					insert(p, lList, 0);
				}


			}
			pLast = p;

		}
		int [][]ordered = new int[people.length][2];
		Iterator<People> pl = lList.iterator();
		int i = 0;
		while (pl.hasNext()) {
			People p = pl.next();
			ordered[i][0] = p.height;
			ordered[i][1] = p.num;
			i ++;
		}
		return ordered;
	}

	public int insert(People people, LinkedList<People> list, int preIndex) {
		int index = 0;
		int number = 0;
		Iterator<People> pl = list.iterator();
		LinkedList<People> newList = new LinkedList<>(list);
		while(pl.hasNext()) {
			People p = pl.next();
			index ++ ;
			if (p.height >= people.height) {
				number ++;
			}
			if (number == people.num && index > preIndex) {
				newList.add(index, people);
				break;
			}
		}
		list.clear();
		list.addAll(newList);

		return index;
	}

	public class People implements Comparable<People>{

		public int height;
		public int num;
		public People (int height,int num) {
			this.height = height;
			this.num = num;
		}
		@Override
		public int compareTo(People o) {
			if (this.num == o.num) {
				return this.height - o.height;
			}
			return this.num - o.num;
		}
	}

	/**
	 * 这个解题思路很强。 上面说最关键的是，插入。h从小到大的这个顺序，
	 * 再按k排了之后，在正确的顺序里，h的那个顺序其实是相对没有变化的，只是按k顺序插入了一些而已。
	 * 这个解题思路就是，按身高h排序,h相同按k排序,h先按从大到小的顺序走，再使用arrayList,利用add(index, item)方法依次插入，
	 * index为k,那么相同k的值h大的在后面，h小的后插入在前面，和上述位置相对不变照应。而且，按h插入，后面插入的一定比前面小，item就直接是其位置。不需要调整。
	 * 另外，排序使用的是二分排序
	 * @param people
	 * @return
	 */

	public int[][] reconstructQueueBest(int[][] people) {
		if(people == null || people.length == 0)
			return people;

		int[][] buf = mergeSort(people);

		List<int[]> res = new ArrayList<>(people.length);

		res.add(buf[0]);

		for(int i = 1; i < buf.length; i++){
			res.add(buf[i][1],buf[i]);
		}



		res.toArray(buf);

		System.out.println(" after sort");
		for (int i = 0; i < buf.length ; i++) {
			System.out.print("[" + buf[i][0] + "," + buf[i][1] + "]" + ",");
		}
		return buf;
	}

	private int[][] mergeSort(int[][] input) {

		if (input == null || input.length < 2) //Sorting pre-check
			return input;

		int[][] buffer = new int[input.length][input[0].length];//Create buffer array
		for (int i = 0; i < input.length; i++)//Initiate buffer array
			buffer[i] = input[i];

		//int[] buffer = input.clone();

		mergeSorting(input, 0, input.length, buffer);//Split merge sort

		return input;
	}

	private int[][] mergeSorting(int[][] input, int start, int end, int[][] buffer) {

		if (end - start < 2) 	// If run size == 1
			return input;		// Consider as sorted

		int mid = (start + end) / 2;

		// Recursively sort both half arrays BUT from buffer into input
		// In this way, we don't have to update the other array after the merge
		mergeSorting(buffer, start, mid, input);	//Sort left array
		mergeSorting(buffer, mid, end, input);		//Sort right array

		int i = start;
		int j = mid;
		System.out.println(" start: " + start + ", end: " + end);
		for (int k = start; k < end; k++) {
			if (i < mid && (j >= end || buffer[i][0] > buffer[j][0]))
				input[k] = buffer[i++];
			else if((i < mid && (j >= end || buffer[i][0] == buffer[j][0])))
				input[k] = buffer[i][1] < buffer[j][1] ? buffer[i++] : buffer[j++];
			else
				input[k] = buffer[j++];
		}
		for (int k = 0; k < input.length; k ++) {
			System.out.print("[" + input[k][0] + "," + input[k][1] + "]" + ",");
		}
		System.out.println();
		return input;
	}


	//  顺手学习二分排序, 方法1，递归归并。方法解释为，从中间开始向两边化简，再中间再化简，到最简单的2位的比较，再合并，合并到最大的。
	public int[]  binarySort(int[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " - ");

		}
		System.out.println();
		splitSort(array, 0, array.length - 1);

		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " - ");

		}
		System.out.println();
		return array;
	}

	public void splitSort(int[] array, int left, int right) {

		if (left == right)
			return;
		int mid = (left + right) / 2;
		splitSort(array, left, mid);
		splitSort(array, mid + 1, right);
		sort(array, left, right);
	}

	public void sort(int[] array, int left, int right){
		int mid = (right + left) / 2 + 1;
		int[] leftArray = new int[mid - left];
		int[] rightArray = new int[right -  mid + 1];

		for (int i = left; i <= right; i++) {
			if (i < mid) {
				leftArray[i - left] = array[i];
			} else {
				rightArray[i - mid] = array[i];
			}
		}


		int i=0,j=0,k=left;
		while (i < leftArray.length && j < rightArray.length) {
			if (leftArray[i] < rightArray[j]) {
				array[k++] = leftArray[i++];
			} else {
				array[k++] = rightArray[j++];
			}
		}

		while (i < leftArray.length) {
			array[k++] = leftArray[i++];
		}
		while(j < rightArray.length) {
			array[k++] = rightArray[j++];
		}

	}

	//  顺手学习二分排序, 方法2，循环查找，方法解释为，for循环，依次插入，一个个插入，前面则是有序的，每次插入新的数值只需找到插入的位置，即可。
	// 故插入是将待插入的数的位置空出来，找到插入正确的位置，将后面的数往后挪。寻找插入位置的方法是折半查找。
	public  void binaryInsertSort(int[] array) {
		int i,j;
		int low,high,mid;
		int temp;
		for(i=1;i<array.length;i++){
			temp=array[i];
			low=0;
			high=i-1;
			while(low<=high){
				mid=(low+high)/2;
				if(array[mid]>temp)
					high=mid-1;
				else
					low=mid+1;

			}
			for(j=i-1;j>high;j--)
				array[j+1]=array[j];
			array[high+1]=temp;
		}

		for (int a: array) {
			System.out.println(a);
		}
	}

	/**
	 * TinyURL is a URL shortening service where you enter a URL such as https://leetcode.com/problems/design-tinyurl and it returns a short URL such as http://tinyurl.com/4e9iAk.
	 Design the encode and decode methods for the TinyURL service. There is no restriction on how your encode/decode algorithm should work. You just need to ensure that a URL can be encoded to a tiny URL and the tiny URL can be decoded to the original URL.
	 目的是实现URL短连接。实现思路为，长生成短，然后短找到长。生成短的过程呢，可以以某个特定的规律。这个人的这个规律真难
	 */

	Map<Integer, String> map1=new HashMap<Integer, String>();
	Map<String, Integer> map2=new HashMap<String, Integer>();
	String s="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	// Encodes a URL to a shortened URL.
	public String encode1(String longUrl) {
		if(!map2.containsKey(longUrl)) {
			map1.put(map1.size()+1,longUrl);
			map2.put(longUrl, map2.size()+1);
		}
		int n=map2.get(longUrl);
		StringBuilder sb=new StringBuilder();
		//首先每个longUrl的索引n是不同的，可以使用对62的商和余数唯一标识n，然后将s中相应位置的字符插入短连接即可。
		while(n>0) {
			//共有62个字符可以用于短连接的编码
			int r=n%62;
			n/=62;
			sb.insert(0,s.charAt(r));
		}
		return sb.toString();
	}

	// Decodes a shortened URL to its original URL.
	public String decode1(String shortUrl) {
		int val=0;
		int n=shortUrl.length();
		for(int i=0;i<n;i++) {
			val=val*62+s.indexOf(shortUrl.charAt(i));
		}
		return map1.get(val);
	}

	/**
	 * Given a sorted array consisting of only integers where every element appears twice except for one element which appears once. Find this single element that appears only once.
	 *  这个问题用亦或是可以解决，可是条件给了是已排好序的数组!!!! 所以应该用二分。
	 * @param nums
	 * @return
	 */
	public int singleNonDuplicate(int[] nums) {
		if (nums == null || nums.length == 0)
			return -1;

		int low = 0;
		int high = nums.length-1;

		while (low < high) {
			int mid = low + (high-low)/2;
			if (mid % 2 == 0 && nums[mid] == nums[mid+1])
				low = mid+1;
			else if (mid % 2 == 1 && nums[mid] == nums[mid-1])
				low = mid+1;
			else
				high = mid;
		}

		return nums[low];
	}

    public int searchInsert(int[] nums, int target) {
		int i = 0;
		for (; i < nums.length; i ++) {
			if (nums[i] < target) {
				continue;
			} else
				break;
		}
		return i;
    }

	/**
	 * 这个题是上面那个题的变体
	 * Suppose an array sorted in ascending order is rotated at some pivot unknown to you beforehand.

	 (i.e., 0 1 2 4 5 6 7 might become 4 5 6 7 0 1 2).

	 You are given a target value to search. If found in the array return its index, otherwise return -1.

	 You may assume no duplicate exists in the array.
	 * @param nums
	 * @param target
	 * @return
	 *
	 * 要先看哪边有序，如果左边有序的话，按理左移的话，如果左边最头上比target还大，就往右边乱序的走
	 */
	public int searchInsertRotate(int[] nums, int target) {
		if(nums==null || nums.length==0)
			return -1;
		int start = 0;
		int end = nums.length - 1;
		while(start <= end) {

			int mid = start + (end - start) / 2;
			System.out.println("start=" + start + "; end=" + end + ", mid=" + mid);
			if (nums[mid] == target) {
				return mid;
			}
			if (nums[mid] < nums[end]) {
				//右边肯定是有序的
				if (nums[mid] < target && nums[end] >= target) {
					start = mid + 1;
				} else {
					end = mid - 1;
				}
			} else {
				if (nums[mid] > target && nums[start] <= target) {
					end = mid - 1;
				} else {
					start = mid + 1;
				}
			}
		}
		return -1;
	}


	public static void main(String[] args) {
		Arithmetic a = new Arithmetic();
		System.out.println(a.searchInsertRotate(new int[]{4, 5, 6, 7, 0, 1, 2}, 2));



	}
}
