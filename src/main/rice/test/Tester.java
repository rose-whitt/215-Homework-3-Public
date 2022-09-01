package main.rice.test;

import main.rice.obj.APyObj;

import java.io.*;
import java.util.*;

/**
 * A class for running a test suite. Encapsulates the ability to run the test suite on a
 * reference implementation to generate the expected results, the ability to create a
 * "wrapper" file for comparing actual results to these expected results, and the ability
 * to run the test suite on a set of files and identify which test cases each file fails
 * on.
 */
public class Tester {
    /**
     * the name of the function under test
     */
    private String funcToTest;
    /**
     * the absolute path to the file containing the reference implementation
     */
    private String solAbsPath;
    /**
     * The absolute path to the directory containing the buggy (student) implementations
     */
    private String implDirPath;
    /**
     * List of test cases to be executed
     */
    private List<TestCase> testsToExecute;

    /**
     * Constructor for the Tester class.
     *
     * @param funcName: a String representing the name of the function to be tested
     * @param solutionPath: a String representing the path to the solution file to test against
     * @param implDirPath: a String representing the path to the directory containing the buggy student implementations of the function
     * @param tests: a List of TestCase objects to be tested on the student implementations and reference solution
     */
    public Tester(String funcName, String solutionPath, String implDirPath, List<TestCase> tests) {
        this.funcToTest = funcName;
        this.solAbsPath = solutionPath;
        this.implDirPath = implDirPath;
        this.testsToExecute = tests;
    }

    /**
     * This method is responsible for running every test case in the List of test cases on the reference solution.
     *
     * @return: a List<String> containing the expected results when a list of test cases are
     * run reference solution. These are the results that are found in the expected.py file that was created.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public List<String> computeExpectedResults() throws IOException, InterruptedException {
        // initialize expected results list to be returned
        List<String> expectedResults = new ArrayList<>();

        // create the file from the absolute solution path to be written to
        File copy = new File(this.solAbsPath);

        // write footer
        writeFile(copy, "footer");

        // iterate over test cases
        for (TestCase test : this.testsToExecute) {
            // build command arguments
            // + 2 because you want to begin command with program to run
            String[] cmd = new String[test.getArgs().size() + 2];
            cmd[0] = "python3";
            cmd[1] = this.solAbsPath;
            int ind = 2;
            for (APyObj arg : test.getArgs()) {
                cmd[ind] = arg.toString();
                if (ind <  cmd.length - 1) {
                    ind++;
                }

            }

            // process
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(cmd);
            Process proc = pb.start();

            // write to buggy
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            proc.waitFor();


            // add to list
            String line;
            String prevLine = null;
            while ((line = reader.readLine()) != null) {
                prevLine = line;
            }
            expectedResults.add(prevLine);

            reader.close();
        }

        // need to write the expected results to a python file
        File expected = new File(this.implDirPath + "/expected.py");
        expected.createNewFile();
        FileWriter expectedWriter = new FileWriter(expected);
        expectedWriter.write("results = " + expectedResults);

        expectedWriter.close();

        return expectedResults;
    }

    /**
     * Helper method for both computeExpectedResults() and runTests() that creates
     * either the wrapper or footer file, or neither if it already exists
     *
     * @param file: the file that was created in the respective method to be written in
     * @param type: tells the helper which writing it will be doing (footer or wrapper)
     * @throws IOException
     * @throws InterruptedException
     */
    private void writeFile(File file, String type) throws IOException, InterruptedException {
        FileWriter writer = new FileWriter(file, true);

        BufferedReader wrapperReader = new BufferedReader(new FileReader(file));

        // only write file (footer or wrapper) if it does not already exist
        boolean fileType = false;
        while (!fileType) {
            // if at end of file, break
            String wrapperLine = wrapperReader.readLine();
            if (wrapperLine == null) {
                break;
            }
            // if line is if statement set footer = true
            if (wrapperLine.equals("if __name__ == \"__main__\":")) {
                fileType = true;
            }
            // next line
        }

        // used in runTest()
        if (type.equals("wrapper")) {
            if (!fileType) {
                // need to update solution file
                writer.write("\nimport sys\n");
                // (2) STATICALLY IMPORT EXPECTED FILE
                writer.write("from expected import results\n");
                // (3) DYNAMICALLY IMPORT THE BUGGY IMPLEMENTATIONS BEING TESTED
                writer.write("from importlib import import_module\n");

                writer.write("def test_buggy_impl(impl_name, fname, args, idx):\n");
                writer.write("   mod_name = impl_name[:-3]\n");
                writer.write("   mod = import_module(mod_name)\n");
                writer.write("   func = getattr(mod, fname)\n");
                writer.write("   actual = func(*args)\n");
                writer.write("   expected = results[idx]\n");
                writer.write("   return (actual == expected)\n");

                writer.write("if __name__ == \"__main__\":\n");
                writer.write("   case_num = int(sys.argv[1])\n");
                writer.write("   impl_name = sys.argv[2]\n");
                writer.write("   fname = sys.argv[3]\n");
                writer.write("   args = sys.argv[4:]\n");
                writer.write("   new_args = [eval(arg) for arg in args]\n");
                writer.write("   print(test_buggy_impl(impl_name, fname, new_args, case_num))\n");

                writer.close();
            }
        } else {
            // used in computeExpectedResults()
            if (!fileType) {
                // need to update solution file
                writer.write("\nimport sys\n");
                writer.write("if __name__ == \"__main__\":\n");
                writer.write("  args = sys.argv[1:]\n");
                // convert each arg to python object
                writer.write("  args = [eval(arg) for arg in args]\n");
                // * unpacks the values
                writer.write("  result = " + this.funcToTest + "(*args)\n");
                writer.write("  print(result)\n");

                writer.close();
            }
        }

    }

    /**
     * This method is responsible for running the list of tests on every file in the directory of buggy implementations
     * and returning the results in the form of a TestResults object.
     *
     * @return: A valid TestResults object that represents the result of running a series of test cases
     * on a series of student implementations
     * @throws IOException
     * @throws InterruptedException
     */
    public TestResults runTests() throws IOException, InterruptedException {
        // delete the cache before beginning
        deletePyCache();

        // initialize caseToFiles for the TestResults object that will be returned
        List<Set<Integer>> caseToFiles = new ArrayList<>();
        for (TestCase test : this.testsToExecute) {
            // ensure that there is an empty set for every test case
            // ensures proper formatting when all implementations pass for a case
            caseToFiles.add(new HashSet<>());
        }

        // initialize wrongSet for TestResults object that will be returned
        Set<Integer> failedFiles = new HashSet<>();

        // create accessible buggy implementation directory
        File buggyImplementations = new File(this.implDirPath);
        String[] buggyDirectoryTemp = buggyImplementations.list();
        Arrays.sort(buggyDirectoryTemp);

        // removing all files that don't end with .py, are expected.py, or wrapper.py
        int validBugFileCount = countValidFiles(buggyDirectoryTemp);
        String[] validBuggyDirectory = new String[validBugFileCount];

        // copy over all valid file names
        int countBugIndex = 0;
        for (int i = 0; i < buggyDirectoryTemp.length; i++) {
            if (buggyDirectoryTemp[i].endsWith(".py") && !buggyDirectoryTemp[i].endsWith("expected.py") && !buggyDirectoryTemp[i].endsWith("wrapper.py")) {
                validBuggyDirectory[countBugIndex] = buggyDirectoryTemp[i];
                countBugIndex++;
            }
        }

        // BEFORE RUNNING ANY TESTS

        // (1) : generate wrapper file
        File wrapperFile = new File(this.implDirPath + "/wrapper.py");
        writeFile(wrapperFile, "wrapper");

        // now we can run tests!
        // counters for appending to failedFiles and caseToFiles
        int fileIndex = 0;
        int caseIndex = 0;
        // for each test
        for (TestCase test : this.testsToExecute) {
            // for each file
            for (String file : validBuggyDirectory) {
                // run test on file

                // create command with the size of arguments plus 5
                // to accommodate formatting needs
                String[] cmdBuggy = new String[test.getArgs().size() + 5];
                cmdBuggy[0] = "python3";
                cmdBuggy[1] = this.implDirPath + "/wrapper.py";
                cmdBuggy[2] = caseIndex + "";
                // third element is relative path (name), not absolute path
                cmdBuggy[3] = file;
                cmdBuggy[4] = this.funcToTest;
                int cmdIndex = 5;
                for (APyObj arg : test.getArgs()) {
                    cmdBuggy[cmdIndex] = arg.toString();
                    cmdIndex++;
                }

                // process
                ProcessBuilder pbBug = new ProcessBuilder();
                pbBug.command(cmdBuggy);
                Process proc = pbBug.start();

                // ready buggy
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                proc.waitFor();

                // read through file
                String line;
                // prevLine is necessary or else you will always get null
                String prevLine = null;
                // last line will be null
                while ((line = reader.readLine()) != null) {
                    prevLine = line;
                }

                // you want to append to failedFiles and caseToFiles when a test fails
                // a test fails if it either gives null or a value that is not "True"
                if (prevLine == null || !prevLine.equals("True")) {
                    // add the current file index
                    failedFiles.add(fileIndex);
                    // add the current file index to the corresponding case index
                    caseToFiles.get(caseIndex).add(fileIndex);
                }
                // increment file index
                fileIndex++;
            }
            // update fileIndex to zero since file iteration starts over after each test case
            fileIndex = 0;
            // increment case index
            caseIndex++;
        }

        // clear cache
        deletePyCache();

        // return correct TestResults object
        return new TestResults(this.testsToExecute, caseToFiles, failedFiles);
    }

    /**
     * Helper method for runTests() that counts the number of valid files in the given buggy directory.
     * A "valid file" ends with ".py", with the exception of "expected.py" and "wrapper.py"
     *
     * @param buggyDirectoryTemp: the directory of the buggy implementations to search
     * @return: an int representing the number of valid file names in the given input
     */
    private int countValidFiles(String[] buggyDirectoryTemp) {
        int validBugFile = 0;
        for (int i = 0; i < buggyDirectoryTemp.length; i++) {
            if (buggyDirectoryTemp[i].endsWith(".py") && !buggyDirectoryTemp[i].endsWith("expected.py") && !buggyDirectoryTemp[i].endsWith("wrapper.py")) {
                validBugFile++;
            }
        }
        return validBugFile;
    }

    /**
     * Helper function for deleting all cached Python files, so that an old cached version
     * of expected.pyc doesn't accidentally get invoked.
     *
     * @throws IOException if the path to the pycache is invalid or a deletion operation
     *                     fails
     */
    private void deletePyCache() throws IOException {
        // Get the list of all files in the pycache
        File pyCacheDir = new File(this.implDirPath + "/__pycache__/");
        String[] filepaths = pyCacheDir.list();

        if (filepaths != null) {
            for (String filepath : filepaths) {
                // Only delete .pyc files
                if (filepath.contains(".pyc")) {
                    File cachedFile =
                        new File(this.implDirPath + "/__pycache__/" + filepath);
                    if (!cachedFile.delete()) {
                        throw new IOException("could not delete cached file " + filepath);
                    }
                }
            }
        }
    }
}