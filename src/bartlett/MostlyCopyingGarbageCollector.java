package bartlett;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of mostly copying garbage collector as it was perceived.
 * by me, after reading :
 * Bartlett, J. F. (1988). Compacting Garbage Collection with Ambiguous Roots. ACM SIGPLAN Lisp Pointers, 1(6), 3–12.
 * https://doi.org/10.1145/1317224.1317225
 * ------------------------------------------------------------------------------------------------------------------
 * PHASE 1
 * Algorithm evacuates the root :
 *          Update page space to 1 (promotion).
 *          Add page to LinkedList.
 *          Update the root
 *
 * PHASE 2
 * Collector Scavenges the LinkedList:
 *          Traverse all the Pages in LinkList, evacuate the pages that are pointed.
 *          Do this until all the pages that are referenced by promoted objects have space == 1.
 *
 * PHASE 3
 * Update the weak pointers that have been promoted by traversing the Linked List.
 * Set the LinkedList as the next old memory.
 * Clear linkedList and reset the pointers.
 */
public class MostlyCopyingGarbageCollector {

    public static final int OLD_SPACE = 0;
    public static final int NEW_SPACE = 1;
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

    private Page[] heap;
    private int[] stack;
    private int[] space;               //The space "attribute" associate with each page

    private int oldFirstPage;           //First page of the old space
    private int oldLastPage;            //Last page of the old space
    private int newFirstPage;           //First page of the new space
    private int newLastPage;            //Last page of the new space
    private int scan;                   //Pages that have been scavenged
    private int allocPages;             //Number of allocated pages in heap
    private LinkedList<Page> queue;

    public MostlyCopyingGarbageCollector(Page[] heap, int[] stack, int allocPages) {
        this.heap = heap;
        this.stack = stack;
        this.allocPages = allocPages;
        init();
    }

    /**
     * initialize garbage collector.
     */
    private void init() {
        this.space = new int[heap.length];          //initialized with zeros that indicate the page belongs to old space
        queue = new LinkedList<>();                 //initialize the linked list that promoted pages will added to its tail.

        //init useful variables
        oldFirstPage = 0;
        oldLastPage = allocPages;
        newFirstPage = heap.length / 2;
        newLastPage = heap.length - 1;

    }

    /**
     * Find first memory cell of page.
     */
    public int pageToCp(int pageIndex) {
        return pageIndex * Page.PAGE_SIZE;
    }

    /**
     * Find page from pointer value.
     */
    public int cpToPage(int cpIndex) {
        return cpIndex / Page.PAGE_SIZE;
    }

    /**
     * Evacuate the roots, checks for ambiguous roots.
     */
    public void evacuateRoots() {
        for (int i = 0; i < stack.length; i++) {
            int pageIndex = cpToPage(stack[i]);     //Find page index
            if (checkAmbiguousRoot(stack[i])) {
                int posInQueue = evacuate(pageIndex);   //Evacuate page
                stack[i] = (posInQueue * Page.PAGE_SIZE) + (stack[i] % Page.PAGE_SIZE); //Update Root
            }
        }
    }

    /**
     * Traverse the heap, and scavenge every node that
     * belongs to promoted page.
     */
    public void scavenge() {
        for (int i = scan; i < queue.size(); i++) {
            Page p = queue.get(i);
            String[] pageMemory = p.getMemory();

            for (int j = 0; j < pageMemory.length; j++) {
                switch (pageMemory[j]) {
                    case NODE_INT:
                        /**Do nothing*/
                        break;
                    case NODE_DOUBLE:
                        /**Do nothing*/
                        break;
                    case NODE_CHAR:
                        /**Do nothing*/
                        break;
                    case NODE_BOOL:
                        /**Do nothing*/
                        break;
                    case NODE_CONSTR:
                        scavengeUnknownSizeNode(p, j);
                        break;
                    case NODE_CONS:
                        scavengeListConstructorNode(p, j);
                        break;
                    case NODE_NULL:
                        break;
                    case NODE_LAMBDA:
                        scavengeUnknownSizeNode(p, j);
                        break;
                    case NODE_IND:
                        scavengeUniquePointerNode(p, j);
                        break;
                    case NODE_VAR:
                        /**Do nothing*/
                        break;
                    case NODE_TYPE:
                        scavengeUniquePointerNode(p, j);
                        break;
                    case NODE_WEAK:
                        /**Do nothing, weak pointer cannot evacuates a node*/
                        break;
                }
            }
            scan++;
        }
        updateWeakPointers();
    }

    /**
     * Copy the queue to the heap.
     */
    public void copyToNewSpace() {
        int i = 0;
        for (Page p : queue) {
            heap[i] = p;
            i++;
        }
    }

    /**
     * Clear the unpromoted pages.
     */
    public void clearOldMemory() {
        //Need to created new Page objects to the new memory space.
        for (int i = queue.size(); i < heap.length; i++) {
            heap[i] = new Page(i);
        }
        queue.clear();                  //Clear Linked List
        Arrays.fill(space, OLD_SPACE);  //Reset all the page space flags.
        scan = 0;

    }

    /**
     * Print heap.
     */
    public void printHeap() {
        if (heap != null) {
            System.out.println("HEAP : ");
            System.out.println("PAGE SIZE : " + Page.PAGE_SIZE);
            for (int i = 0; i < heap.length; i++) {

                System.out.println("    PAGE " + i + " -> " + Arrays.toString(heap[i].getMemory()));
            }
        }
    }

    /**
     * Print Stack which contains roots.
     */
    public void printStack() {
        System.out.println("STACK ->" + Arrays.toString(stack));
    }

    /**
     * Print the linkedList, named queue which holds
     * the promoted pages. A page is identified as
     * promoted if space array has 1 instead of zero
     * at page index.
     */
    public void printQueue() {
        System.out.println("QUEUE : ");
        if (queue.isEmpty()) {
            System.out.println("    EMPTY QUEUE");
        }

        for (int i = 0; i < queue.size(); i++) {
            queue.get(i).displayPage(i);
        }
    }

    /**
     * Print the space array.
     */
    public void printSpaceFlags() {
        System.out.println("SPACE VECTOR -> " + Arrays.toString(space));
    }

    /**
     * Evacuate/promote the page because of the referenced object inside it.
     * DO NOT copy anything just update space bit that is associated
     * with the page in the space array.
     */
    private int evacuate(int pageIndex) {
        if (!(space[pageIndex] == NEW_SPACE)) {
            space[pageIndex] = NEW_SPACE;           //Instead of copying it change the page space
            queue.add(heap[pageIndex]);             //Add page to queue
            return queue.size() - 1;
        }
        return queue.indexOf(heap[pageIndex]);

    }

    /**
     * Scavenge Weak, Type & Indirection nodes. Promote the page
     * which they belong (if it is not promoted), and update the pointer.
     */
    private void scavengeUniquePointerNode(Page p, int indexInPageMemory) {
        String[] pageMemory = p.getMemory();
        int pageIndex;
        int pointerPosInPage = indexInPageMemory + 1;
        int pointerValue = Integer.valueOf(pageMemory[pointerPosInPage]);
        if (space[cpToPage(pointerValue)] != NEW_SPACE) {
            pageIndex = cpToPage(pointerValue);
            evacuate(pageIndex);
            promotePage(cpToPage(pointerValue));
        }
        updatePointer(pageMemory, pointerPosInPage, pointerValue);
    }

    /**
     * Scavenge ListConstructor.
     */
    private void scavengeListConstructorNode(Page p, int indexInPageMemory) {
        String[] pageMemory = p.getMemory();
        int pageIndex;
        int pointer1PosInPage = indexInPageMemory + 1;
        int pointer2PosInPage = indexInPageMemory + 2;
        int pointerValue1 = Integer.valueOf(pageMemory[pointer1PosInPage]);
        int pointerValue2 = Integer.valueOf(pageMemory[pointer1PosInPage]);

        //First Pointer
        if (space[cpToPage(pointerValue1)] != NEW_SPACE) {

            pageIndex = cpToPage(pointerValue1);
            evacuate(pageIndex);
            promotePage(cpToPage(pointerValue2));
        }
        updatePointer(pageMemory, pointer1PosInPage, pointerValue1);

        //Second Pointer
        if (space[cpToPage(pointerValue2)] != NEW_SPACE) {
            pageIndex = cpToPage(pointerValue2);
            evacuate(pageIndex);
            promotePage(cpToPage(pointerValue2));
        }
        updatePointer(pageMemory, pointer2PosInPage, pointerValue2);
    }

    /**
     * Scavenge DataConstructor and Lambda nodes.
     */
    private void scavengeUnknownSizeNode(Page p, int indexInPageMemory) {
        String[] pageMemory = p.getMemory();
        int pageIndex;
        int numberOfPointers = Integer.valueOf(pageMemory[2]);
        for (int i = 0; i < numberOfPointers; i++) {
            int pointerPosInPage = indexInPageMemory + 3 + i;
            int pointerValue = Integer.valueOf(pageMemory[pointerPosInPage]);
            if (space[cpToPage(pointerValue)] != NEW_SPACE) {
                pageIndex = cpToPage(pointerValue);
                evacuate(pageIndex);
                promotePage(cpToPage(pointerValue));
            }
            updatePointer(pageMemory, pointerPosInPage, pointerValue);
        }
    }

    /**
     * This is the last  phase of the algorithm before clearing. The weakpointers
     * that belong to a promoted page are part of the live objects. Their value
     * will be updated to refer to the live object.
     */
    private void updateWeakPointers() {
        for (Page p : heap) {
            String[] pageMemory = p.getMemory();
            for (int i = 0; i < Page.PAGE_SIZE; i++) {
                if (pageMemory[i].equals(NODE_WEAK)) {
                    int pointerValue = Integer.valueOf(pageMemory[i + 1]);
                    if (space[cpToPage(pointerValue)] == 1) {    //If points to a promoted page, update the pointer
                        updatePointer(pageMemory, i + 1, pointerValue);
                    }
                }
            }
        }
    }

    /**
     * Updates the pointer using the page location that it references in the
     * linked list (queue).
     */
    private void updatePointer(String[] pageMemory, int pointerPosInPage, int pointerValue) {
        pageMemory[pointerPosInPage] = String.valueOf(queue.indexOf(heap[cpToPage(pointerValue)]) //Update the pointer so it points to a promoted page
                * Page.PAGE_SIZE + Integer.valueOf(pageMemory[pointerPosInPage]) % Page.PAGE_SIZE); //Use the position in LinkedList to identify the new page location
    }

    /**
     * Checks for ambiguous root.
     * If root points to an objects is considered as root.
     */
    private boolean checkAmbiguousRoot(int pointerValue) {
        boolean isRoot = false;
        List<String> nodes = Arrays.asList(NODE_INT, NODE_DOUBLE, NODE_CHAR, NODE_BOOL,
                NODE_CONSTR, NODE_CONS, NODE_NULL, NODE_LAMBDA, NODE_IND, NODE_VAR,
                NODE_TYPE, NODE_WEAK);
        try {
            if (nodes.contains(heap[cpToPage(pointerValue)].getMemory()[pointerValue % Page.PAGE_SIZE])) {
                isRoot = true;
            } else {
                isRoot = false;
            }
        } catch (IndexOutOfBoundsException e) {
            //pointer does not contain pointer for sure...
        }

        return isRoot;
    }

    private void promotePage(int pageIndex) {
        space[pageIndex] = NEW_SPACE;
    }

}
