package bartlett;

import java.util.ArrayList;


/**
 * Created by td41 on 18/11/16.
 * <p>
 * This is an auxiliary class which generates heaps that will be
 * collected by the MostlyCopyingGarbageCollector class, using Bartlett's
 * algorithm.
 */
public class BartlettHeapBuilder {

    private int heapSize;               //Heap size in cells
    public int numberOfObjects;         //Number of objects in heap

    //Holds the objects (or nodes) positions in heap
    private ArrayList<Integer> nodesPositions = new ArrayList<>();

    /**
     * Example heap from tutorial sheets.
     * first simple test, no weak pointers , no lambda and constr nodes.
     */
    public Page[] buildHeapExample1(int size) {
        this.heapSize = size;                                 //Size in words
        Page[] heap = new Page[this.heapSize / Page.PAGE_SIZE];

        for (int i = 0; i < heap.length; i++) {
            heap[i] = (2 * i < heap.length) ? createAllocatedPage(i, i % 4) : createFreePage(i);
        }
        return heap;
    }

    /**
     * Builds the same heap as the one is explained through
     * graph in report.
     * Page size is 6 to be able to fit the biggest node.(Lambda & constr)
     */
    public Page[] buildHeapDescribedInReport(int size) {
        this.heapSize = size;                                 //Size in words
        Page[] heap = new Page[this.heapSize / Page.PAGE_SIZE];

        heap[0] = new Page(0);
        heap[0].addWeak(2);
        heap[0].addInteger(10);
        heap[0].addChar('c');

        heap[1] = new Page(1);
        heap[1].addType(2, "aType");
        heap[1].addIndirection(18);

        heap[2] = new Page(2);
        heap[2].addWeak(28);

        heap[3] = new Page(3);
        heap[3].addLambda("func", 3, new int[]{2, 4, 6});

        heap[4] = new Page(4);
        heap[4].addDataConstructor("constr", 1, new int[]{9});
        heap[4].addDouble(7.1);

        heap[5] = new Page(5);
        heap[5].addBoolean(false);
        heap[5].addVariable("x");

        heap[6] = new Page(6);
        heap[6].addListConstructor(30, 32);
        heap[6].addWeak(32);
        heap[6].addNull();
        numberOfObjects +=14;

        for (int i = 7; i < heap.length; i++) {
            heap[i] = createFreePage(i);
        }
        return heap;
    }

    /**
     * Create pages with nodes.
     */
    private Page createAllocatedPage(int position, int pageKind) {
        Page page = new Page(position);
        switch (pageKind) {
            case 0:
                nodesPositions.add(page.addWeak(12));
                nodesPositions.add(page.addIndirection(nodesPositions.get(0)));
                numberOfObjects +=2;
                break;
            case 1:
                nodesPositions.add(page.addListConstructor(nodesPositions.get(0), nodesPositions.get(1)));
                numberOfObjects ++;
                break;
            case 2:
                nodesPositions.add(page.addDataConstructor("constr", 1, new int[]{nodesPositions.get(2)}));
                numberOfObjects ++;
                break;
            case 3:
                nodesPositions.add(page.addType(nodesPositions.get(3), "type") /*page.addWeak(nodesPositions.get(3))*/);
                nodesPositions.add(page.addWeak(nodesPositions.get(2)));
                numberOfObjects +=2;
                break;
        }
        return page;
    }

    /**
     * Create empty pages.
     */
    private Page createFreePage(int position) {
        return new Page(position);
    }


}
