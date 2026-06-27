package io.github.fobshippingpoint;

import static io.github.fobshippingpoint.TtyTestListener.*;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TtyTestListenerTest {

  private TtyTestListener listener;
  private final int NEW_LINE_WIDTH = 40;
  private ByteArrayOutputStream output;

  @BeforeMethod
  public void beforeMethod() {
    output = new ByteArrayOutputStream();
    listener = new TtyTestListener(new PrintStream(output), NEW_LINE_WIDTH, "", false);
  }

  @Test
  public void testOnTestSuccess() {
    listener.onTestSuccess(null);

    assertEquals(output.toString(), TestStatus.SUCCESS.value());
  }

  @Test
  public void testOnTestFailure() {
    listener.onTestFailure(null);

    assertEquals(output.toString(), TestStatus.FAILURE.value());
  }

  @Test
  public void testOnTestSkipped() {
    listener.onTestSkipped(null);

    assertEquals(output.toString(), TestStatus.SKIP.value());
  }

  @Test
  public void testOnStart() {
    listener.onStart(testContext("Example Suite"));

    assertEquals(output.toString(), "Test started: Example Suite" + System.lineSeparator());
  }

  @Test
  public void testOnFinish() {
    listener.onFinish(testContext("Example Suite"));

    assertEquals(output.toString(), "Test finished: Example Suite" + System.lineSeparator());
  }

  @Test
  public void testAddsNewLineAfterConfiguredWidth() {
    for (int i = 0; i < NEW_LINE_WIDTH; i++) {
      listener.onTestSuccess(null);
    }

    assertEquals(
        output.toString(),
        TestStatus.SUCCESS.value().repeat(NEW_LINE_WIDTH) + System.lineSeparator());
  }

  @Test
  public void testOnFinishResetsCount() {
    for (int i = 0; i < NEW_LINE_WIDTH - 1; i++) {
      listener.onTestSuccess(null);
    }
    listener.onFinish(testContext("Example Suite"));
    listener.onTestSuccess(null);

    assertEquals(
        output.toString(),
        TestStatus.SUCCESS.value().repeat(NEW_LINE_WIDTH - 1)
            + System.lineSeparator()
            + "Test finished: Example Suite"
            + System.lineSeparator()
            + TestStatus.SUCCESS.value());
  }

  @Test
  public void testCustomPrefix() {
    listener = new TtyTestListener(new PrintStream(output), NEW_LINE_WIDTH, "[TEST] ", false);

    listener.onStart(testContext("Example Suite"));
    listener.onTestSuccess(null);

    assertEquals(
        output.toString(),
        "[TEST] Test started: Example Suite"
            + System.lineSeparator()
            + "[TEST] "
            + TestStatus.SUCCESS.value());
  }

  @Test
  public void testEmptyPrefixDisablesPrefix() {
    listener = new TtyTestListener(new PrintStream(output), NEW_LINE_WIDTH, "", false);

    listener.onStart(testContext("Example Suite"));
    listener.onTestSuccess(null);

    assertEquals(
        output.toString(),
        "Test started: Example Suite" + System.lineSeparator() + TestStatus.SUCCESS.value());
  }

  @Test
  public void testColoredStatuses() {
    listener = new TtyTestListener(new PrintStream(output), NEW_LINE_WIDTH, "", true);

    listener.onTestSuccess(null);
    listener.onTestFailure(null);
    listener.onTestSkipped(null);

    assertEquals(
        output.toString(),
        ANSI_GREEN
            + TestStatus.SUCCESS.value()
            + ANSI_RESET
            + ANSI_RED
            + TestStatus.FAILURE.value()
            + ANSI_RESET
            + ANSI_YELLOW
            + TestStatus.SKIP.value()
            + ANSI_RESET);
  }

  @Test
  public void testPrefixIsPrintedAfterWrap() {
    listener = new TtyTestListener(new PrintStream(output), 3, "[INFO] ", false);

    for (int i = 0; i < 4; i++) {
      listener.onTestSuccess(null);
    }

    assertEquals(
        output.toString(),
        "[INFO] "
            + TestStatus.SUCCESS.value().repeat(3)
            + System.lineSeparator()
            + "[INFO] "
            + TestStatus.SUCCESS.value());
  }

  @Test
  public void testOnFinishClosesPartialProgressLineBeforeFinishMessage() {
    listener = new TtyTestListener(new PrintStream(output), NEW_LINE_WIDTH, "[INFO] ", false);

    listener.onTestSuccess(null);
    listener.onFinish(testContext("Example Suite"));

    assertEquals(
        output.toString(),
        "[INFO] "
            + TestStatus.SUCCESS.value()
            + System.lineSeparator()
            + "[INFO] Test finished: Example Suite"
            + System.lineSeparator());
  }

  private ITestContext testContext(String name) {
    return (ITestContext)
        Proxy.newProxyInstance(
            ITestContext.class.getClassLoader(),
            new Class<?>[] {ITestContext.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "getName" -> name;
                  default -> throw new UnsupportedOperationException(method.getName());
                });
  }
}
