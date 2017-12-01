package io.servicecomb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.servicecomb.ProviderInfo;

public class JavaParseUtils {

  public static final String PACKAGE_NAME_REGEX_PATTERN = "\\s+([a-zA-Z0-9][\\.\\w]*);";

  public static String retrieveQualifiedInterfaceName(String interfaceName, String packageName, List<String> importPackageNames) {
    boolean isSamePackage = true;
    String qualifiedInterfaceName = null;
    for (String importPackageName : importPackageNames) {
      if (importPackageName.endsWith(interfaceName)) {
        qualifiedInterfaceName = importPackageName;
        isSamePackage = false;
      }
    }
    if (isSamePackage) {
      qualifiedInterfaceName = packageName + "." + interfaceName;
    }
    return qualifiedInterfaceName;
  }

  public static void registerInterfaceToProvider(String providerName, String javaFile) {
    // TODO: read full file content instead of introducing file operation here
    File java = new File(javaFile);
    List<String> importPackageNames = new ArrayList<>();
    String packageName = null;
    boolean isService = false;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(java));
      String row;
      while ((row = reader.readLine()) != null) {
        if (row.startsWith("package")) {
          packageName = StringUtils
              .extractValueFromString(row, "package" + PACKAGE_NAME_REGEX_PATTERN);
        } else if (row.startsWith("import")) {
          String qualifiedPackageName = StringUtils
              .extractValueFromString(row, "import" + PACKAGE_NAME_REGEX_PATTERN);
          importPackageNames.add(qualifiedPackageName);
        } else if (row.contains("@Service")) {
          isService = true;
        } else if (row.contains("implement")) {
          if (!isService) {
            break;
          }
          String interfaceName = StringUtils
              .extractValueFromString(row, "implements\\s+([a-zA-Z0-9]+?)\\s+\\{");
          String qualifiedInterfaceName = retrieveQualifiedInterfaceName(interfaceName, packageName,
              importPackageNames);
          ProviderInfo.addProviderInfo(qualifiedInterfaceName, providerName);
        }
      }
    } catch (IOException e) {
      System.out.println("Error processing files, " + e);
    }
  }
}
