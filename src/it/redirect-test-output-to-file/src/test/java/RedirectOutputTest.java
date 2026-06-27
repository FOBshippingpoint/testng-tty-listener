package test.java;

import java.util.Set;
import java.util.stream.IntStream;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RedirectOutputTest {

  static final Set<Integer> SKIP_NUMBERS = Set.of(3, 21, 40, 41, 42, 77);
  static final Set<Integer> FAILURE_NUMBERS = Set.of(1, 18, 19, 33, 54, 96);

  @Test(dataProvider = "dataProvider")
  public void sayHello(int num) {
    System.out.println("HELLO " + num);
    if (SKIP_NUMBERS.contains(num)) {
      throw new SkipException("");
    }
    Assert.assertFalse(FAILURE_NUMBERS.contains(num));
  }

  @DataProvider
  public Object[][] dataProvider() {
    return IntStream.rangeClosed(1, 100).mapToObj(i -> new Object[] {i}).toArray(Object[][]::new);
  }
}
