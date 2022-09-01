package test.rice.test;

import main.rice.obj.*;
import main.rice.test.TestCase;
import main.rice.test.TestResults;
import main.rice.test.Tester;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test cases for the Tester class.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TesterTest {

    /**
     * The absolute path to this project directory, which we'll use to find the provided
     * pyfiles.
     */
    private static String userDir = System.getProperty("user.dir");

    /**
     * Lists of test cases for each of the functions under test (function definitions
     * themselves can be found in the test.rice.test.pyfiles package).
     */
    private static List<TestCase> f0Tests = new ArrayList<>();

    /**
     * Sets up all test cases for all functions under test.
     */
    @BeforeAll
    static void setUp() {
        setUpF0Tests();
    }

    /**
     * Tests computeExpectedResults() on the provided function.
     */
    @Test
    void testGetExpectedResults1() {
        expectedHelper("func0", Collections.singletonList(f0Tests.get(3)),
            "func0sol.py", List.of("3"));
    }

    /**
     * Tests computeExpectedResults() on the provided function.
     */
    @Test
    void testGetExpectedResults2() {
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            expected.add(String.valueOf(i));
        }
        expectedHelper("func0", f0Tests, "func0sol.py", expected);
    }

    /**
     * Tests runTests() for the provided function; checks wrongSet.
     */
    @Test
    void testRunTests1() {
        runTestsHelper("func0", Collections.singletonList(f0Tests.get(0)), "f0oneWrong",
            "results = [0]", Set.of(0), List.of(Set.of(0)), 0);
    }

    /**
     * Tests runTests() for the provided function; checks caseToFiles.
     */
    @Test
    void testRunTests2() {
        runTestsHelper("func0", Collections.singletonList(f0Tests.get(0)), "f0oneWrong",
            "results = [0]", Set.of(0), List.of(Set.of(0)), 1);
    }

    /**
     * Tests runTests() for the provided function; checks test cases.
     */
    @Test
    void testRunTests3() {
        runTestsHelper("func0", Collections.singletonList(f0Tests.get(0)), "f0oneWrong",
            "results = [0]", Set.of(0), List.of(Set.of(0)), 2);
    }

    /**
     * Tests runTests() for the provided function; checks wrongSet.
     */
    @Test
    void testRunTests4() {
        runTestsHelper("func0", f0Tests, "f0multiple",
            "results = [0, 1, 2, 3, 4]", Set.of(0, 1),
            List.of(Set.of(0), Set.of(1), Set.of(0), Set.of(1), Set.of(0)), 0);
    }

    /**
     * Tests runTests() for the provided function; checks caseToFiles.
     */
    @Test
    void testRunTests5() {
        runTestsHelper("func0", f0Tests, "f0multiple",
            "results = [0, 1, 2, 3, 4]", Set.of(0, 1),
            List.of(Set.of(0), Set.of(1), Set.of(0), Set.of(1), Set.of(0)), 1);
    }

    /**
     * Sets up test cases for the provided function.
     */
    private static void setUpF0Tests() {
        // Create five test cases with one argument that's an integer
        for (int i = 0; i < 5; i++) {
            f0Tests.add(new TestCase(Collections.singletonList(new PyIntObj(i))));
        }
    }

    /**
     * Helper function for testing the runTests() function; instantiates a Tester object,
     * fakes computation of the expected results, gets the actual results, and compares
     * both the wrongSet and caseToFile mappings between the actual and expected results.
     *
     * @param funcName    name of the function under test
     * @param tests       the set of tests to be run
     * @param implDir     the path to the directory containing the buggy implementations
     * @param solResults  the expected contents of expected.py, assuming
     *                    computeExpectedResults() is correct
     * @param expWrongSet the expected wrongSet
     * @param expResults  the expected caseToFile list
     */
    private void runTestsHelper(String funcName, List<TestCase> tests, String implDir,
        String solResults, Set<Integer> expWrongSet, List<Set<Integer>> expResults,
        int outputToCheck) {
        Tester tester = new Tester(funcName, null,
            userDir + "/src/test/rice/test/pyfiles/" + implDir, tests);
        try {
            // Generate the expected.py file (to fake computing the expected results
            // without creating a dependency on computeExpectedResults())
            FileWriter writer = new FileWriter(userDir +
                "/src/test/rice/test/pyfiles/" + implDir + "/expected.py");
            writer.write(solResults);
            writer.close();

            // Run the tester
            TestResults results = tester.runTests();
            if (outputToCheck == 0) {
                assertEquals(expWrongSet, results.getWrongSet());
            } else if (outputToCheck == 1) {
                assertEquals(expResults, results.getCaseToFiles());
            } else {
                for (int i = 0; i < tests.size(); i++) {
                    assertEquals(tests.get(i), results.getTestCase(i));
                }
            }
        } catch (Exception e) {
            fail();
        } finally {
            // Clean up, so that we don't artificially make it look like
            // computeExpectedResults() works
            deletedExpected(implDir);
        }
    }

    /**
     * Helper function for testing the computeExpectedResults() function; instantiates a
     * Tester object, computes the expected results, and compares those to the
     * manually-created expected results.
     *
     * @param funcName the name of the function under test
     * @param tests    the set of tests to be run
     * @param solName  the filename of the reference solution, which can be found in * the
     *                 test.rice.test.pyfiles.sols package
     * @param expected the expected (expected) results
     */
    private void expectedHelper(String funcName, List<TestCase> tests, String solName,
        List<String> expected) {
        // Since this just tests computeExpectedResults (and not runTests), we can use
        // any valid implDirPath. We'll fix the implDirPath for simplicity.
        Tester tester = new Tester(funcName, userDir +
            "/src/test/rice/test/pyfiles/sols/" + solName, userDir +
            "/src/test/rice/test/pyfiles/f0oneWrong", tests);
        try {
            // Compute the actual results and compare to the expected
            List<String> actual = tester.computeExpectedResults();
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Deletes the file containing the expected results.
     *
     * @param implDir the path to the directory containing the expected results
     */
    private void deletedExpected(String implDir) {
        File expFile = new File(userDir + "/src/test/rice/test/pyfiles/" +
                implDir + "/expected.py");
        expFile.delete();
    }
}
