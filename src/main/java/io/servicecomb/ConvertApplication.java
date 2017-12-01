package io.servicecomb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.servicecomb.replacer.PomReplacer;
import io.servicecomb.utils.DirectoryManager;
import io.servicecomb.utils.FileManager;
import io.servicecomb.utils.FileUtils;
import io.servicecomb.utils.MicroserviceYamlGenerator;
import io.servicecomb.utils.XmlGenerator;

public class ConvertApplication {
  public static void main(String[] args) throws IOException, XmlPullParserException {
    DirectoryManager directoryManager = new DirectoryManager("/tmp/dubbo-sample");

//    List<File> todoDirectories = directoryManager.getTodoDirectories();
    Queue<File> todoDirectories = new ConcurrentLinkedQueue<>(directoryManager.getTodoDirectories());
    while (!todoDirectories.isEmpty()) {
      File dir = todoDirectories.poll();
      boolean hasAdded = false;
      File[] files = dir.listFiles();
      if (files == null) {
        continue;
      }
      for (File file : files) {
        String providerName = null;
        if (file.getName().equals("pom.xml")) {
          PomReplacer.convert(file.getAbsolutePath());
        } else {
          FileManager fileManager = new FileManager(file.getAbsolutePath());
          List<String> allXmlFiles = fileManager.getAllXmlFiles();
          Iterator<String> xmlFilesIterator = allXmlFiles.iterator();
          boolean isSatisfied = true;
          // process only dubbo xml files
          while (xmlFilesIterator.hasNext()) {
            String xmlFileName = xmlFilesIterator.next();
            DubboProperties dubboProperties = new DubboProperties(xmlFileName);
            if (dubboProperties.isValidDubboPropertyFile()) {
              isSatisfied = dubboProperties.isSatisfiedConsumer();
            }
            if (dubboProperties.isValidDubboPropertyFile() && dubboProperties.isSatisfiedConsumer()) {
              xmlFilesIterator.remove();
              String resourcePath = FileUtils.getResourcePath(xmlFileName);
              // TODO: how to set up application id?
              MicroserviceYamlGenerator
                  .generate(resourcePath, "applicationId", dubboProperties);
              XmlGenerator.generate(resourcePath, dubboProperties);
              providerName = dubboProperties.getApplication();
            }
          }
          if (!isSatisfied) {
            if (!hasAdded) {
              todoDirectories.add(dir);
              hasAdded = true;
            }
            continue;
          }

          List<String> allJavaFiles = fileManager.getAllJavaFiles();
          for (String javaFile : allJavaFiles) {
            File java = new File(javaFile);
            List<String> importStatements = new ArrayList<>();
            String packageName = null;
            boolean isService = false;
            try {
              BufferedReader reader = new BufferedReader(new FileReader(java));
              String row;
              while ((row = reader.readLine()) != null) {
                if (row.startsWith("package")) {
                  Pattern packagePattern = Pattern.compile("package\\s+([a-zA-Z0-9][\\.\\w]*);");
                  Matcher packageMatcher = packagePattern.matcher(row);
                  if (packageMatcher.find()) {
                    packageName = packageMatcher.group(1);
                  }
                } else if (row.startsWith("import")) {
                  importStatements.add(row);
                } else if (row.contains("@Service")) {
                  isService = true;
                }
                if (row.contains("implement")) {
                  if (!isService) {
                    break;
                  }
                  Pattern pattern = Pattern.compile("implements\\s+([a-zA-Z0-9]+?)\\s+\\{");
                  Matcher matcher = pattern.matcher(row);
                  String interfaceName = null;
                  if (matcher.find()) {
                    interfaceName = matcher.group(1);
                  }
                  boolean isSamePackage = true;
                  String fullInterfaceName = null;
                  for (String importStatement : importStatements) {
                    if (importStatement.endsWith(interfaceName + ";")) {
                      Pattern importPattern = Pattern.compile("import\\s+([a-zA-Z0-9\\.]+?);");
                      Matcher importMatcher = importPattern.matcher(importStatement);
                      if (importMatcher.find()) {
                        fullInterfaceName = importMatcher.group(1);
                      }
                      isSamePackage = false;
                    }
                  }
                  if (isSamePackage) {

                    fullInterfaceName = packageName + "." + interfaceName;
                  }
                  ProviderInfo.addProviderInfo(fullInterfaceName, providerName);
                }
              }
            } catch (IOException e) {
              System.out.println("Error processing files, " + e);
            }
          }
          // TODO: parse java file

        }
      }
    }
  }
}
