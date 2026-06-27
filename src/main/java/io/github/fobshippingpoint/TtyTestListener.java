package io.github.fobshippingpoint;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.reporters.DotTestListener;

/** Derived from TestNG's {@link DotTestListener} */
public class TtyTestListener implements ITestListener {

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_YELLOW = "\u001B[33m";

  public enum TestStatus {
    SUCCESS(".", ANSI_GREEN),
    FAILURE("F", ANSI_RED),
    SKIP("S", ANSI_YELLOW);

    private final String value;
    private final String color;

    TestStatus(String value, String color) {
      this.value = value;
      this.color = color;
    }

    public String value() {
      return value;
    }

    private String coloredValue() {
      return color + value + ANSI_RESET;
    }
  }

  private final int NEW_LINE_WIDTH;
  private final PrefixPrintStream out;
  private final boolean colorEnabled;
  private int mCount;

  protected TtyTestListener(PrintStream out, int maxWidth, String prefix, boolean colorEnabled) {
    this.out = new PrefixPrintStream(out, prefix);
    this.NEW_LINE_WIDTH = maxWidth;
    this.colorEnabled = colorEnabled;
  }

  public TtyTestListener() {
    this(
        getPrintStream(),
        Integer.getInteger("io.github.fobshippingpoint.TtyTestListener.newLineWidth", 40),
        System.getProperty(
            "io.github.fobshippingpoint.TtyTestListener.prefix",
            isColorEnabled() ? "[\u001B[1;34mINFO\u001B[m] " /* Blue */ : "[INFO] "),
        isColorEnabled());
  }

  private static boolean isColorEnabled() {
    return Boolean.getBoolean("io.github.fobshippingpoint.TtyTestListener.isColorEnabled");
  }

  /** Returns TTY print stream if available, fallbacks to {@code System.out}. */
  protected static PrintStream getPrintStream() {
    try {
      return new PrintStream(new FileOutputStream("/dev/tty"));
    } catch (FileNotFoundException e) {
      // Fall back to stdout
      return System.out;
    }
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    log(TestStatus.SUCCESS);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    log(TestStatus.FAILURE);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    log(TestStatus.SKIP);
  }

  @Override
  public void onStart(ITestContext context) {
    out.println("Test started: " + context.getName());
    out.flush();
  }

  @Override
  public void onFinish(ITestContext context) {
    out.finishLine();
    out.println("Test finished: " + context.getName());
    out.flush();
    mCount = 0;
  }

  private void log(TestStatus status) {
    out.print(colorEnabled ? status.coloredValue() : status.value());
    if (++mCount % NEW_LINE_WIDTH == 0) {
      out.println();
    }
    out.flush();
  }

  private static final class PrefixPrintStream extends PrintStream {

    private final PrintStream delegate;
    private final String prefix;
    private boolean isLineStart = true;

    private PrefixPrintStream(PrintStream delegate, String prefix) {
      super(OutputStream.nullOutputStream());
      this.delegate = delegate;
      this.prefix = prefix;
    }

    @Override
    public void print(String value) {
      if (isLineStart) {
        delegate.print(prefix);
        isLineStart = false;
      }
      delegate.print(value);
    }

    @Override
    public void println(String value) {
      print(value);
      println();
    }

    @Override
    public void println() {
      delegate.println();
      isLineStart = true;
    }

    @Override
    public void flush() {
      delegate.flush();
    }

    private void finishLine() {
      if (!isLineStart) {
        println();
      }
    }
  }
}
