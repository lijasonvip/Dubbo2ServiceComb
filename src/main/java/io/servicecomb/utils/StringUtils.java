package io.servicecomb.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  public static String extractValueFromString(String input, String regexp) {
    String packageName = null;
    Matcher packageMatcher = Pattern.compile(regexp).matcher(input);
    if (packageMatcher.find()) {
      packageName = packageMatcher.group(1);
    }
    return packageName;
  }
}
