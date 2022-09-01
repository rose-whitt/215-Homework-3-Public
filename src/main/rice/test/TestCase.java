package main.rice.test;

import main.rice.obj.APyObj;

import java.util.*;

public class TestCase {
    /**
     * field for constructor arguments
     */
    private List<APyObj> listArgs;

    /**
     * Constructor for a TestCase object, which takes in a List of arguments.
     *
     * @param args
     */
    public TestCase (List<APyObj> args) {
        this.listArgs = args;
    }

    /**
     * Returns the List of arguments comprising this test case.
     *
     * @return
     */
    public List<APyObj> getArgs() {
        return this.listArgs;
    }

    /**
     * This should simply invoke the toString() method on the internal (List) representation of the arguments.
     * @return a string representation of this test case.
     */
    public String toString() {
        return this.getArgs().toString();
    }

    /**
     * Compares this object to the input object by value.
     * Returns true if this and obj are both TestCase objects comprised of the same arguments in the same order;
     * returns false otherwise.
     *
     * @param obj
     * @return boolean
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof TestCase)) {

        }
        if (obj instanceof TestCase) {
            if (((TestCase) obj).getArgs() instanceof List) {
                if (((TestCase) obj).getArgs().size() != this.getArgs().size()) {
                    return false;
                } else if (((TestCase) obj).getArgs().size() == 0) {
                    return true;
                } else {
                    for (int i = 0; i < ((TestCase) obj).getArgs().size(); i++) {
                        if (!(((TestCase) obj).getArgs().get(i).equals(this.getArgs().get(i)))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
//        if (obj instanceof TestCase && this.listArgs instanceof TestCase) {
//            List<APyObj> tempObj = ((TestCase) obj).getArgs();
//            for (int i = 0; i < tempObj.size(); i++) {
//                if (tempObj.get(i) != this.getArgs().get(i)) {
//                    return false;
//                }
//            }
//            return true;
//        } else {
//            return false;
//        }
    }

    /**
     * Computes and returns a hash code for this object.
     * You should ensure that two objects that are considered equal by equals() will also have the same hash code.
     * This method may not return the same value for all TestCase instances, though some collisions are inevitable.
     *
     * @return hashcode
     */
    public int hashCode() {
        return this.getArgs().hashCode();
    }


}
