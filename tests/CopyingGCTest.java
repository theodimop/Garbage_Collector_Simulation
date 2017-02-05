import cheneys.CopyingGarbageCollector;
import cheneys.HeapBuilder;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This class is used to examine the coping collector
 * behaviour.
 */
public class CopyingGCTest {
    private static final int LIMIT = 60;
    private static final HeapBuilder heapBuilder = new HeapBuilder();

    private int[] stack;                                //Stack with roots
    private int nextStack;                              //Points to the next free cell in stack
    private String[] heap;                              //Memory is represented in form of heap
    private CopyingGarbageCollector garbageCollector;   //Garbage collector
    private boolean spaceFlag = false;                  //Auxiliary variable to identify spaces


    /**
     * Simple example of creating manually a heap
     * and triggering the garbage collector.
     */
    @Test
    public void simpleHeapCollectionExample1() {
        System.out.println("COPYING GARBAGE COLLECTOR, EXAMPLE 1. SIMPLE HEAP\nHEAP SIZE : 28\nOBJECTS IN HEAP : 6\n");

        heap = heapBuilder.buildHeapExample1(28);                       //Build heap
        stack = new int[]{2, 0, 0, 2, 0, 4, 0};                         //Create stack with roots
        garbageCollector = new CopyingGarbageCollector(heap, stack);    //Initialize garbageCollector

        System.out.println("Heap before collection : ");                //Display informative message
        displayGarbageCollectionState(false);

        garbageCollector.evacuateRoots();
        garbageCollector.scavenge();

        System.out.println("\nHeap after collection phase :");
        displayGarbageCollectionState(false);
        System.out.println("Number of objects copied to new Space : " + garbageCollector.numberOfObjectsCopied);

        garbageCollector.clearOldMemory();
        garbageCollector.flip();

        System.out.println("\nMemory has been cleaned and flipped :");
        displayGarbageCollectionState(true);

        garbageCollector.evacuateRoots();
        garbageCollector.scavenge();
        garbageCollector.flip();

    }

    /**
     * A more complicated example of creating manually
     * a heap and triggering the garbage collector.
     */
    @Test
    public void simpleHeapCollectionExample2() {
        System.out.println("COPYING GARBAGE COLLECTOR, EXAMPLE 2. MANY FUN OBJECTS IN HEAP\n HEAP SIZE : 70\nOBJECTS IN HEAP : 11\n");
        heap = heapBuilder.buildHeapExample2(70);
        stack = new int[]{6, 13};
        garbageCollector = new CopyingGarbageCollector(heap, stack);

        System.out.println("Heap before collection : ");
        displayGarbageCollectionState(false);

        garbageCollector.evacuateRoots();
        garbageCollector.scavenge();

        System.out.println("\nHeap after collection phase :");
        displayGarbageCollectionState(false);
        System.out.println("Number of objects copied to new Space : " + garbageCollector.numberOfObjectsCopied);

        garbageCollector.clearOldMemory();
        garbageCollector.flip();

        System.out.println("\nMemory has been cleaned and flipped :");
        displayGarbageCollectionState(true);
    }

    /**
     * Example of garbage collection that CS4201 - p2 -report
     * analyses with graphs.
     **/
    @Test
    public void garbageCollectionHeapDescribedInReportWithMutation() {
        System.out.println("COPYING GARBAGE COLLECTOR, EXAMPLE 3. SIMPLE HEAP\nHEAP SIZE : 80\nOBJECTS IN HEAP : 14\n");

        heap = heapBuilder.buildHeapDescribedInReport(80);                  //Build heap
        stack = new int[]{6, 29, 19, 11, 32, -1, -1, -1, -1};               //Create stack
        nextStack = 5;                                                      //Store next stack location
        garbageCollector = new CopyingGarbageCollector(heap, stack);        //Initialize garbageCollector

        System.out.println("Heap before collection : ");
        displayGarbageCollectionState(false);

        garbageCollector.evacuateRoots();                                   //Garbage collection in progress
        garbageCollector.scavenge();                                        //Garbage collection in progress

        System.out.println("\nHeap after collection phase :");
        displayGarbageCollectionState(false);
        System.out.println("Number of objects copied to new Space : " + garbageCollector.numberOfObjectsCopied);

        garbageCollector.clearOldMemory();                               //Garbage collection in progress
        heapBuilder.setNext(garbageCollector.getNextTo());                  //Store the next, so mutator adds new nodes there.
        garbageCollector.flip();                                            //Garbage collection finished, flip semispaces

        System.out.println("\nMemory has been cleaned and flipped :");
        displayGarbageCollectionState(true);

        //Mutation takes place here
        performMutation();

        System.out.println("Heap before collection : ");
        displayGarbageCollectionState(false);

        garbageCollector.evacuateRoots();
        garbageCollector.scavenge();

        System.out.println("\nHeap after collection phase :");
        displayGarbageCollectionState(false);
        System.out.println("Number of objects copied to new Space : " + garbageCollector.numberOfObjectsCopied);

        garbageCollector.clearOldMemory();
        garbageCollector.flip();

        System.out.println("\nMemory has been cleaned and flipped :");
        displayGarbageCollectionState(true);
    }

    /**
     * Example of garbage collection that CS4201 - p2 -report
     * analyses with graphs.
     **/
    @Test
    public void garbageCollectionRandomGeneratedHeapExample4() {
        int heapSize = 250000;
        int roots = (int) (heapSize * 0.1);
        int copiedObjects = 0;

        Instant start = Instant.now();
        heap = heapBuilder.generateValidHeap(heapSize);
        Instant end = Instant.now();
        System.out.println("COPYING GARBAGE COLLECTOR, EXAMPLE 4. RANDOM HEAP\nHEAP SIZE : " + heapSize + "\nOBJECTS IN HEAP :"+heapBuilder.numberOfObjects+"\n");
        System.out.println("Generated heap size of " + heapSize + " in " + Duration.between(start, end));

        start = Instant.now();
        stack = heapBuilder.generateValidStack(heapBuilder.getNodesPositions(), roots);
        garbageCollector = new CopyingGarbageCollector(heap, stack);
        end = Instant.now();
        System.out.println("Generated stack with " + roots + " roots in " + Duration.between(start, end));
        System.out.println("Number of objects in heap : " + heapBuilder.getNodesPositions().size());
        //System.out.println(Arrays.toString(stack));

        start = Instant.now();
        garbageCollector.evacuateRoots();
        garbageCollector.scavenge();
        copiedObjects = garbageCollector.numberOfObjectsCopied;
        garbageCollector.flip();
        end = Instant.now();

        System.out.println("Garbage collection in heap size of :" + heapSize + " in " + Duration.between(start, end));
        System.out.println("Number of objects copied to new Space : " + copiedObjects);


    }

    /**
     * This method used to analyse the time complexity of the
     * collector.
     */
    @Test
    public void garbageCollectionExperimentalTimeAnalysis() {
        int heapSize = 5000000;
        int roots = (int) (heapSize * 0.1);
        int copiedObjects;

        //Generate HEAP
        Instant start = Instant.now();                      //Start measuring time
        heap = heapBuilder.generateValidHeap(heapSize);
        Instant end = Instant.now();                        //Stop measuring time
        System.out.println("Generated heap size of : " + heapSize + " in " + Duration.between(start, end));

        //Generate STACK
        start = Instant.now();
        stack = heapBuilder.generateValidStack(heapBuilder.getNodesPositions(), roots);
        garbageCollector = new CopyingGarbageCollector(heap, stack);
        end = Instant.now();
        System.out.println("Generated stack with " + roots + " roots in " + Duration.between(start, end));
        System.out.println("Number of objects in heap : " + heapBuilder.getNodesPositions().size());

        //COLLECT
        start = Instant.now();
        garbageCollector.evacuateRoots();
        garbageCollector.scavenge();
        copiedObjects = garbageCollector.numberOfObjectsCopied;
        garbageCollector.flip();
        end = Instant.now();

        //PRINT RESULTS TO STD.OUTPUT
        System.out.println("\nGarbage collection in heap size of :" + heapSize + " in " + Duration.between(start, end));
        System.out.println("\nNumber of objects copied to new Space : " + copiedObjects);
    }

    /**
     * Performs mutation
     */
    private void performMutation() {
        System.out.println("\nMUTATION STARTED...");

        //Mutation changes the heap
        String[] heap = garbageCollector.getHeap();
        int posInHeap = heapBuilder.addBoolean(heap, true);    //Add Boolean
        posInHeap = heapBuilder.addType(heap, posInHeap, "t"); //Add Type that references to previous add boolean

        //Mutation changes the stack
        int[] stack = garbageCollector.getStack();
        stack[nextStack] = posInHeap;                        //Insert Root that pointed to the added type
        nextStack++;                                         //Update stack pointer

        deleteRoot(2); //Delete a random root
        System.out.println("MUTATION FINISHED...\n");
    }

    /**
     * Used to perform mutation.
     */
    private void deleteRoot(int index) {
        for (int i = index; i < stack.length - 1; i++) {
            stack[i] = stack[i + 1];
        }
    }

    /**
     * Print applications state to the standard output.
     */
    private void displayGarbageCollectionState(boolean hasFlipped) {
        printStack();
        printOldSpace(hasFlipped);
        printNewSpace(false);
    }

    /**
     * Auxiliary method for printing old space.
     */
    private void printOldSpace(boolean hasFlipped) {
        if (hasFlipped) {
            spaceFlag = !spaceFlag;
        }

        if (!spaceFlag) {
            System.out.println("    OLD SPACE -> " + Arrays.toString(Arrays.copyOfRange(heap, 0, heap.length / 2 - 1)));
        } else {
            System.out.println("    OLD SPACE -> " + Arrays.toString(Arrays.copyOfRange(heap, heap.length / 2, heap.length - 1)));
        }

    }

    /**
     * Auxiliary method for printing new space.
     */
    private void printNewSpace(boolean hasFlipped) {
        if (hasFlipped) {
            spaceFlag = !spaceFlag;
        }

        if (spaceFlag) {
            System.out.println("    NEW SPACE -> " + Arrays.toString(Arrays.copyOfRange(heap, 0, heap.length / 2 - 1)));
        } else {
            System.out.println("    NEW SPACE -> " + Arrays.toString(Arrays.copyOfRange(heap, heap.length / 2, heap.length - 1)));
        }
    }

    /**
     * Auxiliary method for printing the stack.
     */
    private void printStack() {
        System.out.println("        STACK -> " + Arrays.toString(stack));
    }
}
