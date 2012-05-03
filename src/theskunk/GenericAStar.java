package theskunk;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

abstract class GenericAStar<T> {
	static int nodeIDFromXY(int x, int y) {
		assert x <= 0xFF;
		assert y <= 0xFF;
			
		return (x << 8) | (y & 0xFF);
	}
	
	static int yFromNodeID(int nodeID) {
		assert nodeID <= 0xFFFF;
		
		return nodeID & 0xFF;
	}
	
	static int xFromNodeID(int nodeID) {
		assert nodeID <= 0xFFFF;

		return (nodeID >> 8) & 0xFF;
	}
	
	protected class Node implements Comparable<Node> {
		// The cost that is needed to get here
		int usedCost;
		// The estimated remaining cost
		int estimatedRemainingCost;
		// The Node we came from
		int prevNodeID;
		// The id that identifies the node		
		int nodeID;
		// The State
		T nodeState;
		
		public Node(T state, Node prevNode, int x, int y, int cost, int estimatedRemainingCost) {
			assert state != null;
			assert x >= 0 && y >= 0;
			assert cost >= 0 && estimatedRemainingCost >= 0;
			
			this.nodeState = state;
			this.nodeID = nodeIDFromXY(x, y);
			if (prevNode != null) {
				this.prevNodeID = prevNode.nodeID;
				this.usedCost = prevNode.usedCost + cost;
			}
			this.estimatedRemainingCost = estimatedRemainingCost;
		}
		
		public int getEstimatedCost() {
			return this.usedCost + this.estimatedRemainingCost;
		}
		
		public boolean reachedGoal()
		{
			return this.estimatedRemainingCost == 0;
		}
		
		public int x()
		{
			return xFromNodeID(this.nodeID);
		}
		
		public int y()
		{
			return yFromNodeID(this.nodeID);
		}
		
		public T nodeState()
		{
			return this.nodeState;
		}
		
		@Override
		public int hashCode() {
			return this.nodeID;
		}

		@Override
		public int compareTo(Node otherNode) {
			return new Integer(this.getEstimatedCost()).compareTo(otherNode.getEstimatedCost());
		}
		
		@Override
		public String toString() {
			return String.format("<Node>(x=%d, y=%d)", xFromNodeID(this.nodeID), yFromNodeID(this.nodeID));
		}
	};
	
	private HashMap<Integer, Node> closedNodes;
	private List<Node> openNodes;
	
	protected GenericAStar() {
		this.closedNodes = new HashMap<Integer, Node>();
		this.openNodes = new LinkedList<Node>();
	}
	
	protected void setStartNode(Node startNode) {
		this.openNodes.add(startNode);
	}
	
	// This returns the nodes that will be adjacent to the source node.
	// The returned nodes will be fully populated with the used cost
	// prevnode, remaining distance etc.
	// It does not factor in closed and open nodes.
	protected abstract Set<Node> adjacentNodes(Node sourceNode);
	
	// Does one step in the a* algorithm. Returns true
	// when more processing is required. Returns false
	// when the algorithm is complete.
	protected boolean doStep() {
		Node sourceNode;
		Set<Node> adjacentNodes;
		
		if (this.openNodes.size() == 0)
			return false;
		
		// We get the element with the least estimated cost
		sourceNode = this.openNodes.get(0);
		
		if (sourceNode.reachedGoal())
			return false;
		
		adjacentNodes = this.adjacentNodes(sourceNode);
		
		for (Node node : adjacentNodes) {
			if (this.closedNodes.containsKey(node.nodeID)) {
				// This should not happend
				//throw new RuntimeException("Got a new node which was already closed!");
			}
			else if (this.openNodes.contains(node)) {
				int oldIndex = this.openNodes.indexOf(node);
				Node oldNode = this.openNodes.get(oldIndex);
				
				if (node.compareTo(oldNode) < 0) {
					this.openNodes.remove(oldIndex);
					int insertionIndex = -1;
					
					for (Node n : this.openNodes) {
						if (node.compareTo(n) < 0) {
							insertionIndex = this.openNodes.indexOf(n);
						}
					}
					
					if (insertionIndex == -1) {
						this.openNodes.add(node);
					}
					else {
						this.openNodes.add(insertionIndex, node);
					}
				}
			}
			else {
				this.openNodes.add(node);
			}
		}
		
		// This may be 1 but we have not reached the goal
		if (this.openNodes.size() == 1)
			return false;
		
		this.closedNodes.put(sourceNode.nodeID, sourceNode);
		this.openNodes.remove(sourceNode);
		
		return true;
	}

	protected List<Node> nodePath() {
		List<Node> list = new LinkedList<Node>();
		
		Node currNode = this.openNodes.get(0);
		
		while (currNode != null) {
			list.add(currNode);
			currNode = this.closedNodes.get(currNode.prevNodeID);
		}
		
		Collections.reverse(list);
		
		return list;
	}
}
