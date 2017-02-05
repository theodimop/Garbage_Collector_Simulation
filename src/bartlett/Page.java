package bartlett;

import java.util.Arrays;

/**
 * Created by td41 on 23/11/16.
 */
public class Page {

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

    public static int PAGE_SIZE = 4;    //Default page size


    private String[] memory;
    private int next;                       //Page Next free cell, "consp" in Barlett's paper
    private int freeSpace;                  //Page number of free cells, "conscnt" in Barlett's paper
    private int pageLocation;               //Page Location is its sequence number in page, it is neaded
    // only to construct page its value will NOT be updated!


    /**
     * Construct ew page, given its initial location in heap.
     */
    public Page(int pageLocation) {
        this.memory = new String[PAGE_SIZE];
        this.pageLocation = pageLocation;
        freeSpace = PAGE_SIZE;
        Arrays.fill(memory, "NIL");
    }

    /**
     * Add an integer node into page.
     *
     * @param value Node integer value.
     */
    public int addInteger(int value) {

        int nodePositionInPage = next;
        if (INT_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_INT;
            memory[next++] = String.valueOf(value);
            freeSpace -= INT_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a double node into page.
     *
     * @param value Node double value.
     */
    public int addDouble(double value) {
        int nodePositionInPage = next;
        if (DOUBLE_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_DOUBLE;
            memory[next++] = String.valueOf(value);
            freeSpace -= DOUBLE_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a char node into page.
     *
     * @param value Node char value.
     */
    public int addChar(char value) {
        int nodePositionInPage = next;
        if (CHAR_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_CHAR;
            memory[next++] = String.valueOf(value);
            freeSpace -= CHAR_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a boolean node into page.
     *
     * @param value Node boolean value.
     */
    public int addBoolean(boolean value) {
        int nodePositionInPage = next;
        if (BOOL_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_BOOL;
            memory[next++] = String.valueOf(value);
            freeSpace -= BOOL_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a data constructor node into page.
     *
     * @param constructor   Value constructor which is represented as String.
     * @param n             Number of pointers.
     * @param pointerValues Pointer values.
     */
    public int addDataConstructor(String constructor, int n, int[] pointerValues) {
        int nodePositionInPage = next;
        if (CONS_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_CONSTR;
            memory[next++] = constructor;
            memory[next++] = String.valueOf(n);
            for (int pointer : pointerValues) {
                memory[next++] = String.valueOf(pointer);
            }
            freeSpace -= n + 3;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a list constructor node into page.
     *
     * @param pointerValue1 The value of the first pointer.
     * @param pointerValue2 The value of the second pointer.
     */
    public int addListConstructor(int pointerValue1, int pointerValue2) {
        int nodePositionInPage = next;
        if (CONS_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_CONS;
            memory[next++] = String.valueOf(pointerValue1);
            memory[next++] = String.valueOf(pointerValue2);
            freeSpace -= CONS_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;

    }

    /**
     * Add a null node into page.
     */
    public int addNull() {
        int nodePositionInPage = next;
        if (NULL_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_NULL;
            freeSpace -= NULL_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a lambda function node into page.
     *
     * @param function      Function identifier which is represented as String.
     * @param n             Number of pointers.
     * @param pointerValues Pointer values.
     */
    public int addLambda(String function, int n, int[] pointerValues) {
        int nodePositionInPage = next;
        if (n + 3 <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_LAMBDA;
            memory[next++] = function;
            memory[next++] = String.valueOf(n);
            for (int pointer : pointerValues) {
                memory[next++] = String.valueOf(pointer);
            }
            freeSpace -= n + 3;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add an indirection node into page.
     *
     * @param pointerValue Pointer value.
     */
    public int addIndirection(int pointerValue) {
        int nodePositionInPage = next;
        if (IND_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_IND;
            memory[next++] = String.valueOf(pointerValue);
            freeSpace -= IND_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }

        return -1;
    }

    /**
     * Add a variable identifier node into page.
     *
     * @param variable Variable name.
     */
    public int addVariable(String variable) {
        int nodePositionInPage = next;
        if (VAR_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_VAR;
            memory[next++] = variable;
            freeSpace -= VAR_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a type value node into page.
     *
     * @param pointerValue Pointer value.
     * @param type         Type identifier is represented as String.
     */
    public int addType(int pointerValue, String type) {
        int nodePositionInPage = next;
        if (TYPE_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_TYPE;
            memory[next++] = String.valueOf(pointerValue);
            memory[next++] = type;
            freeSpace -= TYPE_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    /**
     * Add a weak pointer node into page.
     *
     * @param pointerValue Pointer value.
     */
    public int addWeak(int pointerValue) {
        int nodePositionInPage = next;
        if (WEAK_SIZE <= freeSpace) {
            memory[next++] = MostlyCopyingGarbageCollector.NODE_WEAK;
            memory[next++] = String.valueOf(pointerValue);
            freeSpace -= WEAK_SIZE;
            return pageLocation * PAGE_SIZE + nodePositionInPage;
        }
        return -1;
    }

    public String[] getMemory() {
        return memory;
    }

    public void clearPageMemory() {
        Arrays.fill(memory, null);
    }

    public int getPageLocation() {
        return pageLocation;
    }

    public void setPageLocation(int pageLocation) {
        this.pageLocation = pageLocation;
    }

    public void displayPage(int queueIndex) {
        System.out.println("    PAGE SIZE     -> " + PAGE_SIZE);
        System.out.println("    FREE SPACE    -> " + freeSpace);
        System.out.println("    INDEX IN HEAP -> " + pageLocation);
        System.out.println("    INDEX IN LIST -> " + queueIndex);
        System.out.println("    MEMORY        ->" + Arrays.toString(memory) + "\n");
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
