package main.rice.obj;

import java.util.Set;

/**
 * A representation of Python objects of type set.
 *
 * @param <InnerType> the type of each element in this set
 */
public class PySetObj<InnerType extends APyObj> extends AIterablePyObj<InnerType> {

    /**
     * Constructor for a PySetObj; initializes its value.
     *
     * @param value a set whose contents will become the value of this PySetObj
     */
    public PySetObj(Set<InnerType> value) {
        this.value = value;
    }

    /**
     * Builds and returns a string representation of this object that mirrors the Python
     * string representation (i.e., {elem1, elem2, elem3, ...})).
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        // Special case for an empty set
        if (this.value.size() == 0) {
            return "set()";
        }

        String valueStr = this.value.toString();
        return "{" + valueStr.substring(1, valueStr.length() - 1) + "}";
    }

    /**
     * Compares this to the input object by value, ensuring that the two inner lists
     * contain identical sets of elements (order doesn't matter).
     *
     * @param obj the object to compare against
     * @return true if this is equivalent by value to obj; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        // If obj is the wrong type, it's not equivalent
        if (!(obj instanceof PySetObj)) {
            return false;
        }

        // Compare the two PySetObjs' values
        return super.equals(obj);
    }
}
