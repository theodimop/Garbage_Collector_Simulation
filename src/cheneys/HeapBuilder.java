package cheneys;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by td41 on 18/11/16.
 *
 * This is an auxiliary class which generates heaps that will be
 * collected by the CopyingGarbageCollector class, using Cheney's
 * algorithm.
 */
public class HeapBuilder {

    private static final int INT_SIZE = 2;
    private static final int DOUBLE_SIZE = 2;
    private static final int CHAR_SIZE = 2;
    private static final int BOOL_SIZE = 2;
    private static final int CONS_SIZE = 3;
    private static final int NULL_SIZE = 1;
    private static final int IND_SIZE = 2;
    private static final int VAR_SIZE = 2;
    private static final int TYPE_SIZE = 3;
    private static final int WEAK_SIZE = 2;
    private int heapLength;
    private int next = 0;
    //Holds the objects (or nodes) positions in
    private ArrayList<Integer> nodesPositions = new ArrayList<>();
    public int numberOfObjects;    //number of objects in heap

    /**
     * Example heap from tutorial sheets.
     * first simple test, no weak pointers , no lambda and constr nodes.
     */
    public String[] buildHeapExample1(int size) {
        this.heapLength = size;
        String[] heap = new String[this.heapLength];

        addIndirection(heap, 2);
        addInteger(heap, 2);
        addListConstructor(heap, 0, 7);
        addListConstructor(heap, 0, 7);
        addListConstructor(heap, 7, 13);
        addNull(heap);

        return heap;
    }

    /**
     * MyExample heap.
     * Constr node testing
     */
    public String[] buildHeapExample2(int size) {
        this.heapLength = size;
        String[] heap = new String[this.heapLength];
        addInteger(heap, 5);
        addDouble(heap, 10.5);
        addIndirection(heap, 13);
        addDataConstructor(heap, "aConstructor", 4, new int[]{26, 0, 2, 4});
        addVariable(heap, "x");
        addListConstructor(heap, 6, 13);
        addType(heap, 23, "aType");
        addBoolean(heap, false);
        addNull(heap);
        addIndirection(heap, 15);
        addChar(heap, 'c');

        return heap;
    }

    /**
     * MyExample heap.
     * Weak pointer testing heap. This heap is explained thoroughly
     * in report document.
     */
    public String[] buildHeapDescribedInReport(int size) {
        this.heapLength = size;
        String[] heap = new String[this.heapLength];
        //Node positions in heap
        nodesPositions.add(addWeak(heap, 2));                                          //0 :
        nodesPositions.add(addInteger(heap, 10));                                     //2 :
        nodesPositions.add(addChar(heap, 'c'));                                        //4 :
        nodesPositions.add(addType(heap, 2, "aType"));                                  //6 :
        nodesPositions.add(addIndirection(heap, 13));                                 //9 :
        nodesPositions.add(addWeak(heap, 23));                                         //11 :
        nodesPositions.add(addLambda(heap, "func", 3, new int[]{2, 4, 6}));               //13 :
        nodesPositions.add(addDataConstructor(heap, "const", 1, new int[]{9}));       //19 :
        nodesPositions.add(addDouble(heap, 7.1));                                     //23 :
        nodesPositions.add(addBoolean(heap, false));                                  //25 :
        nodesPositions.add(addVariable(heap, "x"));                                   //27 :
        nodesPositions.add(addListConstructor(heap, 25, 27));                          //29 :
        nodesPositions.add(addWeak(heap, 27));                                         //32 :
        nodesPositions.add(addNull(heap));                                            //34 :

        return heap;
    }

    /**
     * Generates a heap of size given by the parameter.
     */
    public String[] generateValidHeap(int size) {
        Random random = new Random(1);
        String[] heap = new String[size];
        heapLength = size;
        next = 0;
        int objectType = 0;
        int min = 1;
        int max = 12;

        next = initHeap(heap); //Add some objects manually

        while (next < size * 0.45) {
            objectType = random.nextInt(max - min + 1) + min;
            next += createObject(heap, objectType);
            numberOfObjects++;
        }
        return heap;
    }

    /**
     * Add an integer node into heap.
     *
     * @param heap  The heap that represents the memory.
     * @param value Node integer value.
     */
    public int addInteger(String[] heap, int value) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + INT_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_INT;
                heap[next++] = String.valueOf(value);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a double node into heap.
     *
     * @param heap  The heap that represents the memory.
     * @param value Node double value.
     */
    public int addDouble(String[] heap, double value) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + DOUBLE_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_DOUBLE;
                heap[next++] = String.valueOf(value);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a char node into heap.
     *
     * @param heap  The heap that represents the memory.
     * @param value Node char value.
     */
    public int addChar(String[] heap, char value) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + CHAR_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_CHAR;
                heap[next++] = String.valueOf(value);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a boolean node into heap.
     *
     * @param heap  The heap that represents the memory.
     * @param value Node boolean value.
     */
    public int addBoolean(String[] heap, boolean value) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + BOOL_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_BOOL;
                heap[next++] = String.valueOf(value);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a data constructor node into heap.
     *
     * @param heap          The heap that represents the memory.
     * @param constructor   Value constructor which is represented as String.
     * @param n             Number of pointers.
     * @param pointerValues Pointer values.
     */
    public int addDataConstructor(String[] heap, String constructor, int n, int[] pointerValues) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + n + 3 < heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_CONSTR;
                heap[next++] = constructor;
                heap[next++] = String.valueOf(n);
                for (int pointer : pointerValues) {
                    heap[next++] = String.valueOf(pointer);
                }
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a list constructor node into heap.
     *
     * @param heap          The heap that represents the memory.
     * @param pointerValue1 The value of the first pointer.
     * @param pointerValue2 The value of the second pointer.
     */
    public int addListConstructor(String[] heap, int pointerValue1, int pointerValue2) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + CONS_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_CONS;
                heap[next++] = String.valueOf(pointerValue1);
                heap[next++] = String.valueOf(pointerValue2);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a null node into heap.
     *
     * @param heap The heap that represents the memory.
     */
    public int addNull(String[] heap) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + NULL_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_NULL;
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a lamda function node into heap.
     *
     * @param heap          The heap that represents the memory.
     * @param function      Function identifier which is represented as String.
     * @param n             Number of pointers.
     * @param pointerValues Pointer values.
     */
    public int addLambda(String[] heap, String function, int n, int[] pointerValues) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + n < heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_LAMBDA;
                heap[next++] = function;
                heap[next++] = String.valueOf(n);
                for (int pointer : pointerValues) {
                    heap[next++] = String.valueOf(pointer);
                }
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add an indirection node into heap.
     *
     * @param heap         The heap that represents the memory.
     * @param pointerValue Pointer value.
     */
    public int addIndirection(String[] heap, int pointerValue) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + IND_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_IND;
                heap[next++] = String.valueOf(pointerValue);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a variable identifier node into heap.
     *
     * @param heap     The heap that represents the memory.
     * @param variable Variable name.
     */
    public int addVariable(String[] heap, String variable) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + VAR_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_VAR;
                heap[next++] = variable;
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a type value node into heap.
     *
     * @param heap         The heap that represents the memory.
     * @param pointerValue Pointer value.
     * @param type         Type identifier is represented as String.
     */
    public int addType(String[] heap, int pointerValue, String type) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + TYPE_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_TYPE;
                heap[next++] = String.valueOf(pointerValue);
                heap[next++] = type;
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Add a weak pointer node into heap.
     *
     * @param heap         The heap that represents the memory.
     * @param pointerValue Pointer value.
     */
    public int addWeak(String[] heap, int pointerValue) {
        if (heap != null) {
            int nodePositionInHeap = next;
            if (next + WEAK_SIZE <= heapLength) {
                heap[next++] = CopyingGarbageCollector.NODE_WEAK;
                heap[next++] = String.valueOf(pointerValue);
            }
            return nodePositionInHeap;
        }
        return -1;
    }

    /**
     * Sets manualy the next free space
     * that heap uses to store objects.
     */
    public void setNext(int next) {
        this.next = next;
    }

    /**
     * Get the objects positions.
     */
    public ArrayList<Integer> getNodesPositions() {
        return nodesPositions;
    }

    /**
     * Given node positions this method returns and array of valid roots.
     */
    public int[] generateValidStack(ArrayList<Integer> nodePositions, int roots) {
        Random rand = new Random(178);
        int min = 0;
        int max = nodePositions.size() - 1;
        int[] stack = new int[roots];
        for (int i = 0; i < roots; i++) {
            stack[i] = nodePositions.get(rand.nextInt(max - min + 1) + min);
        }
        return stack;
    }

    /**
     * Adds some objects to heap, so random objects (which may have pointer attribute)
     * can be created and point to them if necessary.
     */
    private int initHeap(String[] heap) {
        nodesPositions.add(addInteger(heap, 10));                                     //2 :
        nodesPositions.add(addChar(heap, 'c'));                                       //4 :
        nodesPositions.add(addDouble(heap, 10.5));                                     //6 :
        nodesPositions.add(addChar(heap, 'c'));                                       //8 :
        return 8;   //The index of the next free position in heap

    }

    /**
     * Creates an object into heap. If the object has a pointer attribute, it
     * assigns a value that points to the previous inserted object.
     *
     * @return Returns the size of the object created in heap.
     */
    private int createObject(String[] heap, int objectType) {
        switch (objectType) {
            case 1:
                nodesPositions.add(addInteger(heap, 100));
                return 2;
            case 2:
                nodesPositions.add(addDouble(heap, 10.5));
                return 2;
            case 3:
                nodesPositions.add(addChar(heap, 'c'));
                return 2;
            case 4:
                nodesPositions.add(addBoolean(heap, true));
                return 2;
            case 5:
                nodesPositions.add(addDataConstructor(heap, "constr", 2, new int[]{nodesPositions.get(nodesPositions.size() - 1), nodesPositions.get(nodesPositions.size() - 2), nodesPositions.get(nodesPositions.size() - 3), nodesPositions.get(nodesPositions.size() - 4)}));
                return 5;
            case 6:
                nodesPositions.add(addListConstructor(heap, nodesPositions.get(nodesPositions.size() - 1), nodesPositions.get(nodesPositions.size() - 2)));
                return 3;
            case 7:
                nodesPositions.add(addNull(heap));
                return 1;
            case 8:
                nodesPositions.add(addLambda(heap, "func", 1, new int[]{nodesPositions.get(nodesPositions.size() - 1), nodesPositions.get(nodesPositions.size() - 2), nodesPositions.get(nodesPositions.size() - 3), nodesPositions.get(nodesPositions.size() - 4)}));
                return 4;
            case 9:
                nodesPositions.add(addIndirection(heap, nodesPositions.get(nodesPositions.size() - 1)));
                return 2;
            case 10:
                nodesPositions.add(addVariable(heap, "var"));
                return 2;
            case 11:
                nodesPositions.add(addType(heap, nodesPositions.get(nodesPositions.size() - 1), "type"));
                return 3;
            case 12:
                nodesPositions.add(addWeak(heap, nodesPositions.get(nodesPositions.size() - 1)));
                return 2;
        }
        return -1;
    }
}
