package leetcode;

import java.util.ArrayList;
import java.util.List;

public class Tree {
	public static class TreeNode {
		int val;
		TreeNode left;
		TreeNode right;

		TreeNode(int x) {
			val = x;
		}
	}

	/**
	 * 94. Binary Tree Inorder Traversal Given a binary tree, return the inorder
	 * traversal of its nodes' values.
	 * 
	 * For example: Given binary tree [1,null,2,3], 1 \ 2 / 3 return [1,3,2].
	 */
	// 题意分析 中序遍历
	public List<Integer> inorderTraversal(TreeNode root) {
		List<Integer> list = new ArrayList<>();
		if (root != null) {
			addValueToList(root, list);
		}
		return list;
	}

	public void addValueToList(TreeNode root, List<Integer> list) {
		if (root.left != null) {
			addValueToList(root.left, list);
		}
		list.add(root.val);
		if (root.right != null) {
			addValueToList(root.right, list);
		}
	}

	/**
	 * Leetcode-554. Brick Wall here is a brick wall in front of you. The wall
	 * is rectangular and has several rows of bricks. The bricks have the same
	 * height but different width. You want to draw a vertical line from the top
	 * to the bottom and cross the least bricks.
	 * 
	 * @param args
	 */
//	public int leastBricks(List<List<Integer>> wall) {
//
//	}

	public static void main(String[] args) {
		Tree tree = new Tree();
		TreeNode node = new TreeNode(1);
		TreeNode right = new TreeNode(2);
		TreeNode rLeft = new TreeNode(3);
		node.right = right;
		right.left = rLeft;
		System.out.println(tree.inorderTraversal(node));
	}
}
