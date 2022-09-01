package main.rice.test;

import java.util.*;

public class TestResults {
    /**
     * fields
     */
    /**
     * a list of TestCase Objects representing the base test set
     */
    private List<TestCase> baseTestSet;
    /**
     * a per case list of files that were caught where the ith element in the list is the set of files caught
     * by the ith test case in allCases (baseTestSet)
     */
    private List<Set<Integer>> filesCaught;
    /**
     * a set of files that failed one or more tests
     */
    private Set<Integer> failedFiles;
    /**
     *
     * @param allCases
     * @param caseToFiles
     * @param wrongSet
     */
    public TestResults(List<TestCase> allCases, List<Set<Integer>> caseToFiles, Set<Integer> wrongSet) {
        this.baseTestSet = allCases;
        this.filesCaught = caseToFiles;
        this.failedFiles = wrongSet;
    }

    /**
     * Returns the test case at the input index, if that index is within the bounds of allCases;
     * returns null otherwise.
     *
     * @param index
     * @return
     */
    public TestCase getTestCase(int index) {
        if (index < this.baseTestSet.size() && index >= 0 && this.baseTestSet.get(index) instanceof TestCase) {
            return baseTestSet.get(index);
        }
        return null;
    }

    /**
     * Returns the set of files that failed one or more test cases.
     *
     * @return
     */
    public Set<Integer> getWrongSet() {
        return this.failedFiles;
    }

    /**
     * Returns the per-case list of files that were caught.
     *
     * @return
     */
    public List<Set<Integer>> getCaseToFiles() {
        return this.filesCaught;
    }


}
