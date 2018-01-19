package slack;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class BinaryTree {  

    public void printNode(TreeNode<String> node)  
    {  
        System.out.print(node.getData()+"  ");  
    }  

    class TreeNode<T> {  
        private T data;  
        private TreeNode<T> leftNode;  
        private TreeNode<T> rightNode;
        private boolean visited;
  
        public TreeNode(T data, TreeNode<T> leftNode, TreeNode<T> rightNode) {  
            this.data = data;  
            this.leftNode = leftNode;  
            this.rightNode = rightNode;
            this.visited = false;
        }  
          
  
        public T getData() {  
            return data;  
        }  
  
        public void setData(T data) {  
            this.data = data;  
        }  
  
        public TreeNode<T> getLeftNode() {  
            return leftNode;  
        }  
  
        public void setLeftNode(TreeNode<T> leftNode) {  
            this.leftNode = leftNode;  
        }  
  
        public TreeNode<T> getRightNode() {  
            return rightNode;  
        }  
  
        public void setRightNode(TreeNode<T> rightNode) {  
            this.rightNode = rightNode;  
        }


		public boolean isVisited() {
			return visited;
		}


		public void setVisited(boolean visited) {
			this.visited = visited;
		}  
        
    }  

    public TreeNode<String> init() {  
        TreeNode<String> D = new TreeNode<String>("D", null, null);  
        TreeNode<String> H = new TreeNode<String>("H", null, null);  
        TreeNode<String> I = new TreeNode<String>("I", null, null);  
        TreeNode<String> J = new TreeNode<String>("J", null, null);  
        TreeNode<String> P = new TreeNode<String>("P", null, null);  
        TreeNode<String> G = new TreeNode<String>("G", P, null);  
        TreeNode<String> F = new TreeNode<String>("F", null, J);  
        TreeNode<String> E = new TreeNode<String>("E", H, I);  
        TreeNode<String> B = new TreeNode<String>("B", D, E);  
        TreeNode<String> C = new TreeNode<String>("C", F, G);  
        TreeNode<String> A = new TreeNode<String>("A", B, C);  
        return A;  
    }  

    public void xianIterator(TreeNode<String> node)  
    {  
        this.printNode(node);  
        if(node.getLeftNode()!=null)  
        {  
            this.xianIterator(node.getLeftNode());  
        }  
        if(node.getRightNode()!=null)  
        {  
            this.xianIterator(node.getRightNode());  
        }  
    }  
      

    public void zhongIterator(TreeNode<String> node)  
    {  
        if(node.getLeftNode()!=null)  
        {  
            this.zhongIterator(node.getLeftNode());  
        }  
        this.printNode(node);  
        if(node.getRightNode()!=null)  
        {  
            this.zhongIterator(node.getRightNode());  
        }  
    }  
     

    public void houIterator(TreeNode<String> node)  
    {  
        if(node.getLeftNode()!=null)  
        {  
            this.houIterator(node.getLeftNode());  
        }  
        if(node.getRightNode()!=null)  
        {  
            this.houIterator(node.getRightNode());  
        }  
        this.printNode(node);  
    }  
    public void cengIterator(TreeNode<String> node)  
    {  
    	Queue<TreeNode<String>> myQ=new LinkedList<TreeNode<String>>();
    	myQ.add(node);
        while(!myQ.isEmpty()){
        	node = myQ.poll();
           this.printNode(node);
            if(node.leftNode!=null){
            	myQ.add(node.leftNode);
            }
            if (node.rightNode!=null){
            	myQ.add(node.rightNode);
            }
        }    
    } 
    public void xiannodiguiIterator(TreeNode<String> node)  
    {  
    	Stack<TreeNode<String>> stack = new Stack<TreeNode<String>>();
    	while (node!=null||!stack.empty()) {
    		while (node!=null) {
    			this.printNode(node);
    			stack.add(node);
    			node = node.leftNode;
    			
			}
    		node = stack.pop();
    			if (node!=null){
    				node = node.rightNode;
    			}
		}
    }
    public void midnodiguiIterator(TreeNode<String> node)
    {  
    	Stack<TreeNode<String>> stack = new Stack<TreeNode<String>>();
    	while (node!=null||!stack.empty()) {
    		while (node!=null) {
    			System.out.print("("+node.getData()+")");
    			stack.add(node);
    			node = node.leftNode;
    			
			}
    		node = stack.pop();
    			if (node!=null){
    				this.printNode(node);
    				node = node.rightNode;
    			}
		}
    }
    public void hounodiguiIterator(TreeNode<String> node)
    {  
    	Stack<TreeNode<String>> stack = new Stack<TreeNode<String>>();
    	while (node!=null||!stack.empty()) {
    		while (node!=null) {
    			stack.add(node);
    			node = node.leftNode;
    			
			}
    		node = stack.peek();
    			if (node!=null){
    				if(!node.isVisited()&&(node.leftNode==null&&node.rightNode==null || node.leftNode!=null&&node.rightNode==null&&node.leftNode.isVisited()||node.leftNode==null&&node.rightNode!=null&&node.rightNode.isVisited()||node.leftNode!=null&&node.rightNode!=null&&node.leftNode.isVisited()&&node.leftNode.isVisited())){
    					this.printNode(node);
    					stack.pop();
    					node.setVisited(true);
    				}
    				System.out.print("("+node.getData()+node.isVisited()+")");
    				node = node.rightNode;
    				
    			}
		}
    }
    public static void main(String[] args) {  
       BinaryTree binaryTree = new BinaryTree();  
       TreeNode<String> node = binaryTree.init();  
       System.out.println("first");  
       binaryTree.xianIterator(node);  
       System.out.println("\nmid");  
       binaryTree.zhongIterator(node);  
       System.out.println("\nlast");  
       binaryTree.houIterator(node);  
       System.out.println("\nceng");  
       binaryTree.cengIterator(node);  
       System.out.println("\nxiannodiguiIterator");  
       binaryTree.xiannodiguiIterator(node); 
       System.out.println("\nmidnodiguiIterator");  
       binaryTree.midnodiguiIterator(node); 
       System.out.println("\nhounodiguiIterator");  
       binaryTree.hounodiguiIterator(node);
    }  
}   