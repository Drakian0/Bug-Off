package common;
// Create a new package or move down to test package?

import decathlon.*;
// Add/change import of package as needed

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.math.BigDecimal; // Makes sure the test uses 2 decimals in increments. First test without BigDecimal failed this and the increments were in 0.08
import java.math.RoundingMode;

public class Tests { // bad choice of name but can be changed if needed

    @TestFactory
    Collection<DynamicTest> dynamicTestsForCalculateResult() {
        List<DynamicTest> dynamicTests = new ArrayList<>();

        BigDecimal start = new BigDecimal("5.00");
        BigDecimal end = new BigDecimal("17.80");
        BigDecimal increment = new BigDecimal("0.10"); // Could be changed to make more tests, might have to change to another Decimal system

        for (BigDecimal runningTime = start; runningTime.compareTo(end) <= 0; runningTime = runningTime.add(increment)) {
            double finalRunningTime = runningTime.setScale(2, RoundingMode.HALF_UP).doubleValue();
            Executable exec = () -> {
                // Mock the InputResult class
                InputResult mockInputResult = Mockito.mock(InputResult.class);
                when(mockInputResult.enterResult()).thenReturn(finalRunningTime);

                // Inputs the mock into Deca100M
                Deca100M deca100M = new Deca100M();
                deca100M.inputResult = mockInputResult;

                int expectedScore = calculateExpectedScore(finalRunningTime);
                int actualScore = deca100M.calculateResult(finalRunningTime);
                assertEquals(expectedScore, actualScore, "The score should match the expected value for running time: " + finalRunningTime);
            };
            String testName = "Test Testsson " + finalRunningTime; // Mock input name
            DynamicTest dynamicTest = DynamicTest.dynamicTest(testName, exec);
            dynamicTests.add(dynamicTest);
        }

        // Add tests for values outside the acceptable range. Add a larger scope?
        dynamicTests.add(createOutOfRangeTest("Test for running time: 4.99", 4.99, "Value too low")); // If it changes the test gives an error, if left like this it passes
        dynamicTests.add(createOutOfRangeTest("Test for running time: 17.81", 17.81, "Value too high")); // Same as above

        return dynamicTests;
    }

    private DynamicTest createOutOfRangeTest(String testName, double runningTime, String expectedMessage) {
        return DynamicTest.dynamicTest(testName, () -> {
            // Mock the InputResult class
            InputResult mockInputResult = Mockito.mock(InputResult.class);
            when(mockInputResult.enterResult()).thenReturn(10.0); // Provide a valid value after invalid input

            // Capture the console output
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            // Input the mock into Deca100M
            Deca100M deca100M = new Deca100M();
            deca100M.inputResult = mockInputResult;

            // Call the method with the out-of-range value
            deca100M.calculateResult(runningTime);

            // Restore the original System.out
            System.setOut(originalOut);

            // Check the console output
            String output = outContent.toString().trim();
            String[] lines = output.split("\n");
            assertEquals(expectedMessage, lines[0].trim(), "The output message should match the expected value");
        });
    }

    private int calculateExpectedScore(double runningTime) { // Doesn't call on CalcTrackAndField class but uses the same formula, might have to change
        double A = 25.4347;
        double B = 18;
        double C = 1.81;
        return (int) (A * Math.pow((B - runningTime), C));
    }
}
