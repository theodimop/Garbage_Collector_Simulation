import bartlett.BartlettHeapBuilder;
import bartlett.MostlyCopyingGarbageCollector;
import bartlett.Page;
import org.junit.Test;

/**
 * This class is used to examine the mostly coping collector
 * behaviour.
 */
public class MostlyCopyingGCTest {

    private int numberOfObjectsInHeap;
    /**
     * Simple heap to test the collection.
     */
    @Test
    public void MostlyCopyingGCExample1() {
        BartlettHeapBuilder bartlettHeapBuilder = new BartlettHeapBuilder();
        Page[] heap = bartlettHeapBuilder.buildHeapExample1(32);
        numberOfObjectsInHeap = bartlettHeapBuilder.numberOfObjects;

        System.out.println("\n--------------DISPLAY MEMORY INFO--------------\n");
        int[] stack = new int[]{8};
        MostlyCopyingGarbageCollector mcgc =
                new MostlyCopyingGarbageCollector(heap, stack, heap.length / 2);
        displayCollectorInfo(mcgc);

        System.out.println("\n--------------EVACUATE ROOTS PHASE--------------\n");
        mcgc.evacuateRoots();
        displayCollectorInfo(mcgc);

        System.out.println("\n--------------SCAVENGING PHASE--------------\n");
        mcgc.scavenge();
        displayCollectorInfo(mcgc);

        System.out.println("\n--------------END OF COLLECTION--------------\n");
        mcgc.copyToNewSpace();
        mcgc.clearOldMemory();
        displayCollectorInfo(mcgc);

    }

    /**
     * This tests the collector with the heap that is built
     * in the report. Although, heap contains the same
     * items the pointers values are different because of the paging.
     * The pages may include free space that cannot be allocated
     * from a full object.
     */
    @Test
    public void MostlyCopyingGCTestHeapDescribedInReport() {
        Page.PAGE_SIZE = 6;     //Change the page size to fit the largest object
        BartlettHeapBuilder bartlettHeapBuilder = new BartlettHeapBuilder();
        Page[] heap = bartlettHeapBuilder.buildHeapDescribedInReport(84);
        numberOfObjectsInHeap = bartlettHeapBuilder.numberOfObjects;


        System.out.println("\n--------------DISPLAY MEMORY INFO--------------\n");

        int[] stack = new int[]{6, 36, 24, 12, 39, -1, -1};    //7 is not a root!
        MostlyCopyingGarbageCollector mcgc =
                new MostlyCopyingGarbageCollector(heap, stack, heap.length / 2);
        displayCollectorInfo(mcgc);

        System.out.println("\n--------------EVACUATE ROOTS PHASE--------------\n");
        mcgc.evacuateRoots();
        displayCollectorInfo(mcgc);

        System.out.println("\n--------------SCAVENGING PHASE--------------\n");
        mcgc.scavenge();
        displayCollectorInfo(mcgc);

        System.out.println("\n--------------END OF COLLECTION--------------\n");
        mcgc.copyToNewSpace();
        mcgc.clearOldMemory();
        displayCollectorInfo(mcgc);

    }

    /**
     * Prints information as are known from the collector.
     */
    private void displayCollectorInfo(MostlyCopyingGarbageCollector mcgc) {
        mcgc.printHeap();       //print heap
        mcgc.printStack();      //print stack
        mcgc.printSpaceFlags(); //print space flag that is associated with each page in heap
        mcgc.printQueue();      //print Linked list
    }

}
