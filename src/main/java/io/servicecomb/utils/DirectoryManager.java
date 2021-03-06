package io.servicecomb.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.servicecomb.replacer.PomReplacer;

public class DirectoryManager {
  private List<File> todoDirectories = new ArrayList<>();

  private String artifactId;

  public DirectoryManager(String directoryPath) {
    init(directoryPath);
  }

  private void init(String directoryPath) {
    File directory = new File(directoryPath);
    if (!directory.isDirectory()) {
      System.out.println("Not a valid directory path");
      return;
    }
    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        todoDirectories.add(file);
      } else {
        if (file.getName().equals("pom.xml")) {
          String pomAbsolutePath = file.getAbsolutePath();
          PomReplacer.convert(pomAbsolutePath);
          artifactId = PomReplacer.retrieveArtifactId(pomAbsolutePath);
        }
      }
    }
  }

  public List<File> getTodoDirectories() {
    return todoDirectories;
  }

  public String getArtifactId() {
    return artifactId;
  }
}
