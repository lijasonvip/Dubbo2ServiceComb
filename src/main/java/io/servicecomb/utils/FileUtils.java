package io.servicecomb.utils;

public class FileUtils {

  private static final String RESOURCE_PATH_PATTERN = "src/main/resources";

  public static String getResourcePath(String absolutePath) {
    if (absolutePath == null) {
      return null;
    }
    int index = absolutePath.indexOf(RESOURCE_PATH_PATTERN);
    if (index == -1) {
      return null;
    }
    return absolutePath.substring(0, index + RESOURCE_PATH_PATTERN.length());
  }
}
