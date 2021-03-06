import java.util.concurrent.atomic.AtomicReference;

/**
 * <b>Concurrent Unbounded Double-Ended Queue</b>
 * 
 * 
 * @author Ryan Dozier, Soliman Alnaizy, Natasha Zdravkovic
 * @param <T>
 * @category Term Project
 *  
 */

public class ConcurrentDequeue<T> {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Object left_null_copy = left_null;
		Object right_null_copy = right_null;
		ConcurrentDequeue<Integer> dequeue = new ConcurrentDequeue<>();
		dequeue.push_left(5);
		dequeue.push_left(4);
		dequeue.push_left(3);
		dequeue.push_left(2);
		dequeue.push_left(1);
		dequeue.push_left(0);
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		dequeue.push_left(7);
		dequeue.push_left(8);
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		dequeue.push_left(5);
		dequeue.push_left(4);
		dequeue.push_left(3);
		dequeue.push_left(2);
		dequeue.push_left(1);
		dequeue.push_left(0);
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println(dequeue.pop_left());
		System.out.println("test");
	}

	public static Object left_null = new Object();
	public static Object right_null = new Object();
	public static Object left_seal = new Object();
	public static Object right_seal = new Object();
	// Set array size here
	public static final int array_size = 10; 
	private AtomicReference<NodeHint<T>> leftNodeHint, rightNodeHint;

	public ConcurrentDequeue() {
		this.leftNodeHint = new AtomicReference<>(new NodeHint<>(new Node<>(array_size / 2), 0));
		this.rightNodeHint = new AtomicReference<>(new NodeHint<>(this.leftNodeHint.get().buffer, 0));
		this.leftNodeHint.get().loc = this.leftNodeHint.get().buffer.leftHint;
		this.rightNodeHint.get().loc = this.rightNodeHint.get().buffer.rightHint;
	}


	/**
	 * TODO: This function has not been tested, the pseudo code was not included in 
	 * the paper. <br>
	 * <b>Needs extensive testing.<b>
	 * @param hint
	 * @return
	 */
	NodeHint<T> findLeftEdge(NodeHint<T> hint) {
		while(true) {
			if(hint.loc == array_size) {
				hint.buffer = hint.buffer.right.get();
				hint.loc = 0;
			}
			if(hint.buffer.array[hint.loc].get().data != left_null) 
				return hint;
			hint.loc++;
		}
	}

	/**
	 * TODO: This function has not been tested, the pseudo code was not included in 
	 * the paper. <br>
	 * <b>Needs extensive testing.<b>
	 * @param hint
	 * @return
	 */
	NodeHint<T> findRightEdge(NodeHint<T> hint) {
		while(true) {
			if(hint.loc < 0) {
				hint.buffer = hint.buffer.left.get();
				hint.loc = array_size - 1;
			}
			
			if(hint.buffer.array[hint.loc].get() != right_null) {
				return hint;
			}
			hint.loc--;

		}
	}

	/**
	 * TODO: This function has not been tested, the pseudo code was not included in 
	 * the paper. <br>
	 * <b>Needs extensive testing.<b> 
	 * @param old
	 * @param newNode
	 * @param newIndex
	 * @return
	 */
	NodeHint<T> updateLeftHint(NodeHint<T> old, Node<T> newNode, int newIndex) {
		NodeHint<T> newHint = new NodeHint<>(newNode, newIndex);
		if(old.buffer.leftHint != 0 || old.buffer.left.get() == null)
			old.buffer.leftHint = newIndex;
		if(this.leftNodeHint.compareAndSet(old, newHint))
			return newHint;
		else
			return null;
	}

	/**
	 * TODO: This function has not been tested, the pseudo code was not included in 
	 * the paper. <br>
	 * <b>Needs extensive testing.<b>
	 * @param old
	 * @param newNode
	 * @param newIndex
	 * @return
	 */
	NodeHint<T> updateRightHint(NodeHint<T> old, Node<T> newNode, int newIndex) {
		NodeHint<T> newHint = new NodeHint<>(newNode, newIndex);
		old.buffer.leftHint = newIndex;
		if(this.rightNodeHint.compareAndSet(old, newHint))
			return newHint;
		else
			return null;
	}


	@SuppressWarnings("unchecked")
	public void push_left(T data) {
		NodeHint<T> edge;
		Node<T> edgeNode;
		int edgeIndex;
		NodeHint<T> hintCopy;
		AtomicReference<DequeueSlot<T>> in;
		DequeueSlot<T> inCopy;
		AtomicReference<DequeueSlot<T>> out;
		DequeueSlot<T> outCopy;
		while(true) {
			hintCopy = this.leftNodeHint.get();

			// Get the edge and split into components
			edge = this.findLeftEdge(hintCopy);
			edgeNode = edge.buffer;
			edgeIndex = edge.loc;

			in = edgeNode.array[edgeIndex];
			inCopy = in.get();
			
			if(edgeIndex != 0) {
				out = edgeNode.array[edgeIndex - 1];
				outCopy = out.get();
			} else {
				outCopy = new DequeueSlot<T>((T) left_null, 0);
				out = new AtomicReference<DequeueSlot<T>>(outCopy);
			}

			// check edge
			if((inCopy.data == left_null || inCopy.data == right_seal)
					//|| (edgeIndex != 0 && outCopy.data != left_null)
					|| (edgeIndex == array_size && edgeNode.right.get() != null)) 
				continue;

			// interior push
			if(edgeIndex != 0) {
				if(in.compareAndSet(inCopy, new DequeueSlot<T>(inCopy.data, inCopy.count + 1))
						&& out.compareAndSet(outCopy, new DequeueSlot<T>(data, outCopy.count + 1))) {
					this.updateLeftHint(hintCopy, edgeNode, edgeIndex -1);
					return;
				}
			}
			// edge is either in between or boundary
			else {
				// check if the node is at the boundary
				if(outCopy.data == left_null) {
					Node<T> newNode = new Node<>(array_size - 1);
					newNode.array[array_size - 1].set(new DequeueSlot<>(data));
					newNode.right = new AtomicReference<Node<T>>(edgeNode);

					if(edgeNode.left.compareAndSet(null, newNode)) {
						this.updateLeftHint(hintCopy, newNode, array_size - 1);
						return;
					} else {
						// TODO: double check this is right
						Node<T> outLeftNeighbor = edge.buffer.left.get();
						AtomicReference<DequeueSlot<T>> far = outLeftNeighbor.array[array_size - 1];
						DequeueSlot<T> farCopy = far.get();

						// ensure left neighbor points back
						AtomicReference<Node<T>> back = outLeftNeighbor.right;
						Node<T> backCopy = back.get();

						// This may be wrong
						if (backCopy != edgeNode) {
							continue;
						}
						// remove sealed node on the left
						else if (farCopy.data == left_seal) { 
							if(in.compareAndSet(inCopy, new DequeueSlot<T>(inCopy.data, inCopy.count + 1))
									&& out.compareAndSet(outCopy, new DequeueSlot<T>((T) left_null, outCopy.count + 1))) {
								// TODO: Debug
								this.updateLeftHint(hintCopy, edgeNode, 1);
								NodeHint<T> rightHintCopy = this.rightNodeHint.get();
								NodeHint<T> rightEdge = this.findRightEdge(rightHintCopy);
								this.updateRightHint(rightHintCopy, rightEdge.buffer, rightEdge.loc);
							}						
						}
					}
				}
			}
		}
	}

	public void push_right(T data) {
		
	}

	@SuppressWarnings("unchecked")
	public T pop_left() {
		NodeHint<T> hintCopy;
        NodeHint<T> edge;
		Node<T> edgeNode;
		int edgeIndex;
		AtomicReference<DequeueSlot<T>> in;
		DequeueSlot<T> inCopy;
		AtomicReference<DequeueSlot<T>> out;
		DequeueSlot<T> outCopy;
		
        while(true) {
            hintCopy = leftNodeHint.get();
            edge = this.findLeftEdge(hintCopy);
            edgeNode = edge.buffer;
            edgeIndex = edge.loc;
            in = edgeNode.array[edgeIndex];
            inCopy = in.get();
            
            if(edgeIndex != 0) {
    			out = edgeNode.array[edgeIndex - 1];
    			outCopy = out.get();
            } else if(edgeNode.left.get() != null) {
            	out = edgeNode.left.get().array[array_size - 1];
            	outCopy = out.get();
            } else {
            	outCopy = new DequeueSlot<T>((T) left_null, 0);
            	out = new AtomicReference<>(outCopy);
            }
            
			if((inCopy.data == (T) left_null || inCopy.data == (T) right_seal)
					//|| (edgeIndex != 0 && outCopy.data !=(T) left_null)
					|| (edgeIndex == array_size && edgeNode.right.get() != null))
				continue;
			
			// interior edge
			if(edgeIndex != 0) {
				if(inCopy.data == right_null && in.get() == inCopy)
					return null;
				if(out.compareAndSet(outCopy, new DequeueSlot<>((T) left_null, outCopy.count + 1))  && in.compareAndSet(inCopy,  new DequeueSlot<>((T) left_null, inCopy.count + 1))) {
					this.updateLeftHint(hintCopy, edgeNode, edgeIndex + 1);
					return inCopy.data;
				}

			} // end interior edge
			else {
				if(outCopy.data != (T) left_null) {
					Node<T> outNode = edgeNode.left.get();
					AtomicReference<DequeueSlot<T>> far;
					DequeueSlot<T> farCopy;
					AtomicReference<Node<T>> back;
					Node<T> backCopy;
					far = outNode.array[array_size - 1];
					farCopy = far.get();
					back = outNode.right;
					backCopy = back.get();

					
					if(backCopy != edgeNode)
						continue;
					
					// check for edge + seal
					if(farCopy.data == (T) left_null) {
						if((inCopy.data == (T) right_null || inCopy.data == (T) right_seal)
								&& in.get() == inCopy)
							return null;
						
						if(in.compareAndSet(inCopy, new DequeueSlot<>(inCopy.data, inCopy.count + 1))
								&& far.compareAndSet(farCopy, new DequeueSlot<>((T) left_null, farCopy.count + 1))) {
							farCopy.data = (T) left_seal;
							farCopy.count++;
							inCopy.count++;
						}
					}
					if(farCopy.data == (T) left_seal) {
						if(inCopy.data == (T) right_null && in.get() == inCopy) {
							return null;
						}
						if(in.compareAndSet(inCopy, new DequeueSlot<>(inCopy.data, inCopy.count + 1))
								&& out.compareAndSet(outCopy, new DequeueSlot<>((T) left_null, outCopy.count + 1))) {
							hintCopy = this.updateLeftHint(hintCopy, edgeNode, 0);
							NodeHint<T> right = this.findRightEdge(this.rightNodeHint.get());
							this.updateRightHint(right, right.buffer, right.loc);
							inCopy.count++;
							outCopy.data = (T) left_null;
							outCopy.count++;
						}
					}
				}
				if(outCopy.data == (T) left_null) {
					if(inCopy.data == (T) right_null && in.get() == inCopy) 
						return null;
					if(edgeNode.left.compareAndSet(edgeNode.left.get(), null)
							&& in.compareAndSet(inCopy, new DequeueSlot<T>((T) left_null, inCopy.count + 1))) {
						this.updateLeftHint(hintCopy, edgeNode, 1);
						return inCopy.data;
					}
				}
			}	// end straddle edge
        }	// end while
	}	// End pop_left

	public T pop_right() {
		T result = null;
		return result;
	}

	/**
	 * 
	 * @author Ryan Dozier
	 *
	 * @param <T> Is used for generic data structure construction. 
	 */
	static class DequeueSlot <T> {
		public T data;
		public int count;
		/** 
		 * @param data To be stored in the data structure
		 */
		public DequeueSlot(T data) {
			this.data = data;
			this.count = 0;
		}

		public DequeueSlot(T data, int count) {
			this.data = data;
			this.count = count;
		}
	}

	static class NodeHint<T> {
		public Node<T> buffer;
		public int loc;

		public NodeHint(Node<T> node, int index) {
			this.buffer = node;
			this.loc = index;
		}
	}

	static class Node<T> {
		public int leftHint;
		public int rightHint;
		//public DequeueSlot<T>[] array;
		public AtomicReference<DequeueSlot<T>>[] array;
		public AtomicReference<Node<T>> left;
		public AtomicReference<Node<T>> right;

		@SuppressWarnings("unchecked")
		public Node(int half) {
			//this.array = (DequeueSlot<T>[]) new DequeueSlot<?>[array_size];
			this.array = (AtomicReference<DequeueSlot<T>>[]) new AtomicReference[array_size];
			for(int i = 0; i < half; i++) 
				array[i] = new AtomicReference<>(new DequeueSlot<>((T) left_null));
			for(int i = half; i < this.array.length; i++) 
				array[i] = new AtomicReference<>(new DequeueSlot<>((T) right_null));

			this.leftHint = half-1;
			this.rightHint = half;

			this.left = new AtomicReference<Node<T>>(null);
			this.right = new AtomicReference<Node<T>>(null);;
		}
	}

}

