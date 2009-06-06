package vgrazi.util;

/**
 * Created by Victor Grazi.
 * Date: Jun 5, 2009 - 7:27:16 PM
 */
public class StringUtils {
  /**
   * Returns true if the value is null or blank, false otherwise
   * @param value the value to test
   * @return true if the value is null or blank, false otherwise
   */
  public static boolean isBlank(String value) {
    return value == null || value.trim().equals("");
  }
}
