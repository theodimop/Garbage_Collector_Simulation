package cheneys;

/**
 * ----------------------------------------------------------------------------  <br>
 * cheneys.CopyingGarbageCollector.java created byTheo Dimopoulos on 17-11-2016.                                <br>
 * Email:   dimopoulosth.td@gmail.com | td41@st-andrews.ac.uk                             <br>
 * ----------------------------------------------------------------------------  <br>
 * This class represents a garbage collector implemeting the Cheney's algorithm.
 *
 * @author Theo Dimopoulos
 * @version 17-11-2016
 */
public class CopyingGarbageCollector {

    public static final String NODE_INT = "INT";
    public static final String NODE_DOUBLE = "DOUBLE";
    public static final String NODE_CHAR = "CHAR";
    public static final String NODE_BOOL = "BOOL";
    public static final String NODE_CONSTR = "CONSTR";
    public static final String NODE_CONS = "CONS";
    public static final String NODE_NULL = "NULL";
    public static final String NODE_LAMBDA = "LAMBDA";
    public static final String NODE_IND = "IND";
    public static final String NODE_VAR = "VAR";
    public static final String NODE_TYPE = "TYPE";
    public static final String NODE_WEAK = "WEAK";
    public static final String TAG_FORWARD = "FWD";


    private String[] heap;              //Array that represents the heap
    private int[] stack;                //Array that represents the stack with roots
    private int nextTo;                 //The next free cell in new semispace
    private int scan;                   //Pointer which is used to point the scavenge initial Location
    private int fromSpaceStart;         //The start position of the old semispace
    private int toSpaceStart;           //The start position of the new semispace
    private int flip;                   //Counter that indicates the odd/even flip to identify new & old semispace

    public int numberOfObjectsCopied;

    /**
     * Construct new collector, given a heap and stack with roots.
     */
    public CopyingGarbageCollector(String[] heap, int[] stack) {
        this.heap = heap;
        this.stack = stack;
        fromSpaceStart = 0;
        scan = toSpaceStart = nextTo = heap.length / 2;


    }

    /**
     * Evacuate roots that are in stack.
     * Negative values are use to generate random
     * stack with roots.
     */
    public void evacuateRoots() {
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == -1)
                continue;

            int root = stack[i];
            int newLocation = nextTo;
            if (!heap[root].equals("FWD")) {                 //Root is not evacuated
                evacuate(root);                              //Evacuate node that root points
            }
            stack[i] = newLocation;                          //Update the root

        }
    }

    /**
     * Implements the scavenging phase.
     */
    public void scavenge() {
        do {
            doScavenging();
        } while (scan < nextTo);

        updateWeakPointers();   //Update the weak pointer that are in new semispace
    }

    /**
     * Perform the flip between the new semi spaces.
     */
    public void flip() {
        if (flip % 2 == 0) {
            toSpaceStart = scan = nextTo = 0;
            fromSpaceStart = heap.length / 2;
        } else {
            toSpaceStart = scan = nextTo = heap.length / 2;
            fromSpaceStart = 0;
        }
        flip++;
        numberOfObjectsCopied = 0;
    }

    /**
     * Clear the old semi space.
     */
    public void clearOldMemory() {
        if (flip % 2 == 0) {
            for (int i = fromSpaceStart; i < heap.length / 2; i++) {
                if (heap[i] == null) {
                    return;
                } else
                    heap[i] = null;
            }
        } else {
            for (int i = fromSpaceStart; i < heap.length; i++) {
                if (heap[i] == null) {
                    return;
                } else
                    heap[i] = null;
            }
        }

    }

    /**
     * Get the next Free memory cell in heap.
     */
    public int getNextTo() {
        return nextTo;
    }

    public String[] getHeap() {
        return heap;
    }

    public int[] getStack() {
        return stack;
    }

    /**
     * Evacuate node. Copy the node to the new semi space.
     */
    private void evacuate(int root) {
        String nodeTag = heap[root];                //Get Node tag
        int nodeNewLocation = nextTo;               //Store the new location
        heap[nextTo++] = nodeTag;                   //Copy Node  tag


        switch (nodeTag) {
            case NODE_INT:                              //Evacuate INT
                heap[nextTo++] = heap[root + 1];        //Copy  value
                break;
            case NODE_DOUBLE:                           //Evacuate DOUBLE
                heap[nextTo++] = heap[root + 1];        //Copy  value
                break;
            case NODE_CHAR:                             //Evacuate CHAR
                heap[nextTo++] = heap[root + 1];        //Copy  value
                break;
            case NODE_BOOL:                             //Evacuate Boolean
                heap[nextTo++] = heap[root + 1];        //Copy  value
                break;
            case NODE_CONSTR:
                heap[nextTo++] = heap[root + 1];        //Copy constructor identifier
                heap[nextTo++] = heap[root + 2];        //Copy number of pointers
                int n = Integer.valueOf(heap[root + 2]);
                for (int i = 0; i < n; i++) {            //Copy all pointers
                    heap[nextTo++] = heap[root + i + 3];
                }
                break;
            case NODE_CONS:                             //Evacuate List Constructor
                heap[nextTo++] = heap[root + 1];        //Copy  value
                heap[nextTo++] = heap[root + 2];        //Copy  value
                break;
            case NODE_NULL:
                break;
            case NODE_LAMBDA:
                heap[nextTo++] = heap[root + 1];        //Copy function
                heap[nextTo++] = heap[root + 2];        //Copy number of pointers
                int l = Integer.valueOf(heap[root + 2]);

                for (int i = 0; i < l; i++) {            //Copy all pointers
                    heap[nextTo++] = heap[root + i + 3];
                }
                break;
            case NODE_IND:                              //Evacuate Indirection
                heap[nextTo++] = heap[root + 1];        //Copy  value
                break;
            case NODE_VAR:
                heap[nextTo++] = heap[root + 1];        //Copy  variable identifier
                break;
            case NODE_TYPE:
                heap[nextTo++] = heap[root + 1];        //Copy  pointer value
                heap[nextTo++] = heap[root + 2];        //Copy  type identifier
                break;
            case NODE_WEAK:
                heap[nextTo++] = heap[root + 1];        //Copy  weak pointer value
                break;

        }
        heap[root] = "FWD";
        numberOfObjectsCopied++;
        if (nodeTag != NODE_NULL) {                                   //If it was not a null value
            heap[root + 1] = String.valueOf(nodeNewLocation);       //FWD to new location
        }
        //System.out.println(Arrays.toString(heap));
    }

    /**
     * Performs scavenging.
     */
    private void doScavenging() {
        String objectTag = heap[scan++];               //Get object tag

        switch (objectTag) {
            case NODE_INT:
                scan++;
                break;
            case NODE_DOUBLE:
                scan++;
                break;
            case NODE_CHAR:
                scan++;
                break;
            case NODE_BOOL:
                scan++;
                break;
            case NODE_CONSTR:
                scan++;                                 //skip constructor identifier
                int n = Integer.valueOf(heap[scan++]);  //skip number of arguments
                for (int i = 0; i < n; i++) {
                    scavengeCell(heap[scan]);
                }
                break;
            case NODE_CONS:
                scavengeCell(heap[scan]);               //update pointers
                scavengeCell(heap[scan]);               //update pointers
                break;
            case NODE_NULL:
                scan++;
                break;
            case NODE_LAMBDA:
                scan++;                                //skip function identifier
                int l = Integer.valueOf(heap[scan++]); //skip number of arguments
                for (int i = 0; i < l; i++) {
                    scavengeCell(heap[scan]);
                }
                break;
            case NODE_IND:
                scavengeCell(heap[scan]);
                break;
            case NODE_VAR:
                scan++;
                break;
            case NODE_TYPE:
                scavengeCell(heap[scan]);
                scan++;
                break;
            case NODE_WEAK:
                scan++;
                break;
        }
    }

    /**
     * Scavenge a cell, copy the node into the new space.
     */
    private void scavengeCell(String node) {
        int n = Integer.valueOf(node);
        int oldNextTo = nextTo;
        if (!heap[n].equals("FWD")) {
            evacuate(n);
            heap[scan++] = String.valueOf(oldNextTo);
        } else {
            heap[scan++] = heap[n + 1]; //Node has a FWD so update the pointer
        }

    }

    /**
     * Update weak pointers.
     */
    private void updateWeakPointers() {
        int start = toSpaceStart;
        int stop = start + heap.length / 2;
        do {
            if (heap[start].equals(NODE_WEAK)) {
                try {
                    int weakPointerValue = Integer.valueOf(heap[start + 1]);
                    if (heap[weakPointerValue].equals(TAG_FORWARD)) {
                        heap[start + 1] = heap[weakPointerValue + 1];   //update weak pointer
                    } else {
                        // heap[start] = null;
                        heap[start + 1] = "NULL";                         //This pointer is pointed by root by the node that points in collected
                    }
                } catch (NumberFormatException e) {

                }
            }
            start += nextNode(heap[start], start);
        } while (start < nextTo);
    }

    /**
     * Find the next Node.
     */
    private int nextNode(String nodeTag, int currentPosition) {

        if (nodeTag.equals(NODE_INT) || nodeTag.equals(NODE_DOUBLE) || nodeTag.equals(NODE_CHAR)
                || nodeTag.equals(NODE_BOOL) || nodeTag.equals(NODE_IND) || nodeTag.equals(NODE_VAR) || nodeTag.equals(NODE_WEAK)) {
            return 2;
        } else if (nodeTag.equals(NODE_CONS) || nodeTag.equals(NODE_TYPE)) {
            return 3;
        } else if (nodeTag.equals(NODE_CONSTR) || nodeTag.equals(NODE_LAMBDA)) {
            return Integer.valueOf(heap[currentPosition + 2]);
        } else
            return 1;
    }
}
