package minijavac.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompilerIT {

    /*
    * run compiler
    * check for expected class files
    * run class files
    * assert against output
    * */
    private void test(String sourceDir, String fileName, List<String> expectedClasses, List<String> output, Path tmpDir) throws IOException, InterruptedException {
        compile(sourceDir, fileName, expectedClasses, tmpDir);
        execute(expectedClasses.get(0), output, tmpDir);
    }

    private void test(String sourceDir, String fileName, String className, List<String> output, Path tmpDir) throws IOException, InterruptedException {
        test(sourceDir, fileName, List.of(className), output, tmpDir);
    }

    private void compile(String sourceDir, String file, List<String> expectedClasses, Path tmpDir) throws IOException, InterruptedException {
        String jarFile = String.format("target/minijavac-%s.jar", System.getProperty("app.version"));
        String sourceDirPath = String.format("src/test/resources/integration/%s", sourceDir);
        String filePath = String.format("%s/%s", sourceDirPath, file);
        List<String> compilationCommand = List.of(
                "java",
                "-jar",
                jarFile,
                filePath,
                "-sourcepath",
                sourceDirPath,
                "-d",
                tmpDir.toString()
        );

        Process compilation = new ProcessBuilder(compilationCommand).start();

        compilation.waitFor();

        for (String expectedClass : expectedClasses) {
            assertTrue(Files.exists(tmpDir.resolve(String.format("%s.class", expectedClass))));
        }
    }

    private void execute(String className, List<String> expectedOutput, Path tmpDir) throws IOException {
        List<String> executionCommand = List.of(
                "java",
                "-classpath",
                tmpDir.toString(),
                className
        );

        Process execution = new ProcessBuilder(executionCommand).start();
        Iterator<String> iter = expectedOutput.iterator();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(execution.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                assertEquals(iter.next(), line);
            }
        }
    }

    @Test
    public void expr_binop_arithmetic(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/arithmetic",
                "1.java",
                "Test",
                List.of(
                        "4",
                        "5",
                        "6",
                        "7",
                        "8"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_arithmetic_compound(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/arithmetic",
                "2.java",
                "Test",
                List.of(
                        "6",
                        "8",
                        "8",
                        "2",
                        "10"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_arithmetic_float(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/arithmetic",
                "3.java",
                "Test",
                List.of(
                        "4.7",
                        "5.0",
                        "8.059999",
                        "4.862069",
                        "8.199999"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_logical(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/logical",
                "1.java",
                "Test",
                List.of(
                        "true",
                        "false",
                        "true",
                        "false",
                        "false",
                        "false",
                        "true",
                        "true",
                        "true",
                        "false"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_unop_not(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/logical",
                "2.java",
                "Test",
                List.of(
                        "false",
                        "true",
                        "true",
                        "false"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_comparison(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/logical",
                "3.java",
                "Test",
                List.of(
                        "true",
                        "false",
                        "false",
                        "true",
                        "false",
                        "true",
                        "false",
                        "true",
                        "true",
                        "true",
                        "false",
                        "true",
                        "true",
                        "false"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_logical_compound(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/logical",
                "4.java",
                "Test",
                List.of(
                        "true",
                        "true",
                        "false"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_logical_method_call(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/logical",
                "5.java",
                "Test",
                List.of(
                        "false",
                        "true",
                        "false"
                ),
                tmpDir
        );
    }

    @Test
    public void expr_binop_comparison_null(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/comparison",
                "1.java",
                "Test",
                List.of(
                        "1",
                        "2"
                ),
                tmpDir
        );
    }

    @Test
    public void stmt_while(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/while",
                "1.java",
                "Test",
                List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
                tmpDir
        );
    }

    @Test
    public void stmt_while_break(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/while",
                "2.java",
                "Test",
                List.of("0", "1", "2", "3", "4", "5"),
                tmpDir
        );
    }

    @Test
    public void stmt_while_continue(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/while",
                "3.java",
                "Test",
                List.of("1", "3", "5", "7", "9"),
                tmpDir
        );
    }

    @Test
    public void stmt_while_break_nested(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/while",
                "4.java",
                "Test",
                List.of("0", "0", "1", "1", "0", "1", "2", "0", "1"),
                tmpDir
        );
    }

    @Test
    public void stmt_doWhile(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/doWhile",
                "1.java",
                "Test",
                List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
                tmpDir
        );
    }

    @Test
    public void stmt_doWhile_break(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/doWhile",
                "2.java",
                "Test",
                List.of("0", "1", "2", "3", "4", "5"),
                tmpDir
        );
    }

    @Test
    public void stmt_doWhile_continue(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/doWhile",
                "3.java",
                "Test",
                List.of("1", "3", "5", "7", "9"),
                tmpDir
        );
    }

    @Test
    public void stmt_doWhile_once(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/doWhile",
                "4.java",
                "Test",
                List.of("10"),
                tmpDir
        );
    }

    @Test
    public void stmt_for(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/for",
                "1.java",
                "Test",
                List.of("0", "1", "2"),
                tmpDir
        );
    }

    @Test
    public void stmt_for_break(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/for",
                "2.java",
                "Test",
                List.of("0", "1", "2", "3"),
                tmpDir
        );
    }

    @Test
    public void stmt_for_continue(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/for",
                "3.java",
                "Test",
                List.of("1", "3", "5", "7", "9"),
                tmpDir
        );
    }

    @Test
    public void stmt_for_nested(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/for",
                "4.java",
                "Test",
                List.of("0", "0", "1", "1", "0", "1", "2", "0", "1"),
                tmpDir
        );
    }

    @Test
    public void method_chaining(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "1.java",
                "Test",
                List.of("10", "2", "18", "12"),
                tmpDir
        );
    }

    @Test
    public void ref_ix_multi(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "2.java",
                "Test",
                List.of("1", "2", "3", "4", "8"),
                tmpDir
        );
    }

    @Test
    public void ref_ix_afterCall(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "3.java",
                "Test",
                List.of("13", "19"),
                tmpDir
        );
    }

    @Test
    public void ref_method_overloading(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "4.java",
                "Test",
                List.of("9", "12", "true", "false"),
                tmpDir
        );
    }

    @Test
    public void ref_ix_length(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "5.java",
                "Test",
                List.of("2", "3", "2"),
                tmpDir
        );
    }

    @Test
    public void ref_ix_init(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "6.java",
                "Test",
                List.of("1", "2", "3", "true", "false", "1", "2"),
                tmpDir
        );
    }

    @Test
    public void ref_ix_init_2D(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "7.java",
                "Test",
                List.of("1", "2", "3", "4", "true", "false", "true", "false", "1", "2", "3", "4"),
                tmpDir
        );
    }

    @Test
    public void method_chaining_float(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "8.java",
                "Test",
                List.of("10.0", "2.0", "18.0", "12.0"),
                tmpDir
        );
    }

    @Test
    public void varDecl_late_init(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "9.java",
                "Test",
                List.of("3"),
                tmpDir
        );
    }

    @Test
    public void constructor(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/ref",
                "10.java",
                "Test",
                List.of("13", "true", "20", "false", "9", "true"),
                tmpDir
        );
    }

    @Test
    public void expr_fix(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/fix",
                "1.java",
                "Test",
                List.of("1", "1", "2", "1"),
                tmpDir
        );
    }

    @Test
    public void expr_fix_float(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/fix",
                "2.java",
                "Test",
                List.of("1.5", "1.5", "2.5", "1.5"),
                tmpDir
        );
    }

    @Test
    public void expr_ternary(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/expr/ternary",
                "1.java",
                "Test",
                List.of("1", "2", "1", "2"),
                tmpDir
        );
    }

    @Test
    public void stmt_if_elseIf(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("features/stmt/if",
                "1.java",
                "Test",
                List.of("0", "1", "2", "3"),
                tmpDir
        );
    }

    @Test
    public void bubbleSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "BubbleSort.java",
                "BubbleSort",
                List.of("1", "2", "3", "4", "5", "6", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void bubbleSortFloat(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "BubbleSortFloat.java",
                "BubbleSortFloat",
                List.of("1.1", "2.4", "3.8", "4.3", "5.3", "6.3", "6.4", "7.9", "8.0"),
                tmpDir
        );
    }

    @Test
    public void inorderTraversal(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "InOrder.java",
                List.of("InOrder", "TreeNode"),
                List.of("0", "1", "2", "3", "4", "5", "6"),
                tmpDir
        );
    }

    @Test
    public void preorderTraversal(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "PreOrder.java",
                List.of("PreOrder", "TreeNode"),
                List.of("3", "1", "0", "2", "5", "4", "6"),
                tmpDir
        );
    }

    @Test
    public void postorderTraversal(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "PostOrder.java",
                List.of("PostOrder", "TreeNode"),
                List.of("0", "2", "1", "4", "6", "5", "3"),
                tmpDir
        );
    }

    @Test
    public void fibonacci(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "Fibonacci.java",
                "Fibonacci",
                List.of("0", "1", "1", "2", "3", "5", "8", "13", "0", "1", "1", "2", "3", "5", "8", "13"),
                tmpDir
        );
    }

    @Test
    public void factorial(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "Factorial.java",
                "Factorial",
                List.of("1", "1", "2", "6", "24", "120", "720"),
                tmpDir
        );
    }

    @Test
    public void hammingWeight(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "HammingWeight.java",
                "HammingWeight",
                List.of("0", "1", "1", "2", "1", "2", "2", "3", "1", "2", "2", "3", "2", "3", "3", "4", "1"),
                tmpDir
        );
    }

    @Test
    public void mergeSortedArrays(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "MergeSortedArrays.java",
                "MergeSortedArrays",
                List.of("0", "1", "1", "2", "3", "3", "3", "5", "6", "7", "8", "9", "10", "11"),
                tmpDir
        );
    }

    @Test
    public void binarySearch(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "BinarySearch.java",
                "BinarySearch",
                List.of("-1", "0", "-1", "1", "2", "-1", "3", "-1", "4", "-1", "5", "6", "7", "8", "-1", "9", "10", "-1", "11", "-1", "12", "-1"),
                tmpDir
        );
    }

    @Test
    public void sqrt(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "Sqrt.java",
                "Sqrt",
                List.of("0", "1", "1", "1", "2", "2", "2", "2", "2", "3"),
                tmpDir
        );
    }

    @Test
    public void pascalsTriangle(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "PascalsTriangle.java",
                "PascalsTriangle",
                List.of("true", "1", "true", "1", "1", "true", "1", "2", "1", "true", "1", "3", "3", "1", "true", "1", "4", "6", "4", "1"),
                tmpDir
        );
    }

    @Test
    public void linkedList(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/linkedlist",
                "LinkedList.java",
                List.of("LinkedList", "Node"),
                List.of("2", "3", "2", "5", "2", "3", "7", "2"),
                tmpDir
        );
    }

    @Test
    public void reverseLinkedList(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/linkedlist",
                "Reverse.java",
                List.of("Reverse", "Node"),
                List.of("3", "2", "1"),
                tmpDir
        );
    }

    @Test
    public void removeLinkedList(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/linkedlist",
                "Remove.java",
                List.of("Remove", "Node"),
                List.of("2", "3", "3", "4", "5", "4", "2", "4", "5", "4", "2", "5"),
                tmpDir
        );
    }

    @Test
    public void oddEvenLinkedList(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/linkedlist",
                "OddEven.java",
                List.of("OddEven", "Node"),
                List.of("1", "3", "5", "2", "4"),
                tmpDir
        );
    }

    @Test
    public void sameTree(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "SameTree.java",
                List.of("SameTree", "TreeNode"),
                List.of("false", "true"),
                tmpDir
        );
    }

    @Test
    public void maxDepthTree(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "MaxDepth.java",
                List.of("MaxDepth", "TreeNode"),
                List.of("3"),
                tmpDir
        );
    }

    @Test
    public void pathSumTree(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "PathSum.java",
                List.of("PathSum", "TreeNode"),
                List.of("false", "true"),
                tmpDir
        );
    }

    @Test
    public void reverseInteger(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "ReverseInteger.java",
                "ReverseInteger",
                List.of("321", "-321", "21", "0", "2147483641"),
                tmpDir
        );
    }

    @Test
    public void validateBST(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "ValidateBST.java",
                List.of("ValidateBST", "TreeNode"),
                List.of("true", "false"),
                tmpDir
        );
    }

    @Test
    public void queue(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "Queue.java",
                List.of("Queue"),
                List.of("-1", "-1", "true", "0", "true", "1", "-1", "true", "true", "true", "true", "true", "5", "false", "false", "2", "2", "true", "5", "3", "4", "5", "6", "1", "8", "true", "-1", "0"),
                tmpDir
        );
    }

    @Test
    public void BFS(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "BFS.java",
                List.of("BFS", "TreeNode", "Queue"),
                List.of("1", "2", "3", "1", "2", "3", "4", "5", "6", "7"),
                tmpDir
        );
    }

    @Test
    public void houseRobber(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "HouseRobber.java",
                List.of("HouseRobber"),
                List.of("4", "4", "12", "12"),
                tmpDir
        );
    }

    @Test
    public void minHeap(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "MinHeap.java",
                List.of("MinHeap"),
                List.of("0", "true", "1", "3", "true", "3", "true", "2", "true", "2", "true", "1", "5", "false", "1", "2", "3", "5", "4", "1", "4", "2", "4", "3", "5", "2", "3", "4", "5", "3", "4", "5", "4", "5", "5", "0"),
                tmpDir
        );
    }

    @Test
    public void kthLargestElement(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "KthLargestElement.java",
                List.of("KthLargestElement", "MinHeap"),
                List.of("6", "5", "4", "3", "2", "1"),
                tmpDir
        );
    }

    @Test
    public void selectionSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "SelectionSort.java",
                List.of("SelectionSort"),
                List.of("1", "2", "3", "4", "5", "6", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void selectionSortFloat(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "SelectionSortFloat.java",
                List.of("SelectionSortFloat"),
                List.of("1.1", "2.4", "3.8", "4.3", "5.3", "6.3", "6.4", "7.9", "8.0"),
                tmpDir
        );
    }

    @Test
    public void insertionSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "InsertionSort.java",
                List.of("InsertionSort"),
                List.of("1", "2", "3", "4", "5", "6", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void insertionSortFloat(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "InsertionSortFloat.java",
                List.of("InsertionSortFloat"),
                List.of("1.1", "2.4", "3.8", "4.3", "5.3", "6.3", "6.4", "7.9", "8.0"),
                tmpDir
        );
    }

    @Test
    public void mergeSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "MergeSort.java",
                List.of("MergeSort"),
                List.of("1", "2", "3", "4", "5", "6", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void mergeSortFloat(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "MergeSortFloat.java",
                List.of("MergeSortFloat"),
                List.of("1.1", "2.4", "3.8", "4.3", "5.3", "6.3", "6.4", "7.9", "8.0"),
                tmpDir
        );
    }

    @Test
    public void quickSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "QuickSort.java",
                List.of("QuickSort"),
                List.of("1", "2", "3", "4", "5", "6", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void quickSortFloat(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "QuickSortFloat.java",
                List.of("QuickSortFloat"),
                List.of("1.1", "2.4", "3.8", "4.3", "5.3", "6.3", "6.4", "7.9", "8.0"),
                tmpDir
        );
    }

    @Test
    public void countingSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "CountingSort.java",
                List.of("CountingSort"),
                List.of("0", "1", "1", "1", "2", "3", "3", "4", "4", "5", "6", "6", "7", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void radixSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "RadixSort.java",
                List.of("RadixSort"),
                List.of("5", "11", "37", "102", "758", "999", "1101", "2310"),
                tmpDir
        );
    }

    @Test
    public void bucketSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "BucketSort.java",
                List.of("BucketSort", "Node"),
                List.of("3", "11", "13", "22", "25", "30", "42", "49", "56", "61", "65", "74", "77", "84", "89", "91", "100"),
                tmpDir
        );
    }

    @Test
    public void heapSort(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/sort",
                "HeapSort.java",
                List.of("HeapSort"),
                List.of("1", "2", "3", "4", "5", "6", "7", "8"),
                tmpDir
        );
    }

    @Test
    public void validSudoku(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs",
                "ValidSudoku.java",
                List.of("ValidSudoku"),
                List.of("true", "false"),
                tmpDir
        );
    }

    @Test
    public void lowestCommonAncestor(@TempDir Path tmpDir) throws IOException, InterruptedException {
        test("programs/tree",
                "LowestCommonAncestor.java",
                List.of("LowestCommonAncestor", "TreeNode"),
                List.of("3", "5", "1"),
                tmpDir
        );
    }
}
