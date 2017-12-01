package io.servicecomb;

import java.util.HashMap;
import java.util.Map;

public class ProviderInfo {
  // key is interface name, value is microservice name
  private static Map<String, String> providerInfo = new HashMap<>();

  public static void addProviderInfo(String interfaceName, String microserviceName) {
    providerInfo.put(interfaceName, microserviceName);
  }

  public static String getMicroserviceNameByInterface(String interfaceName) {
    return providerInfo.getOrDefault(interfaceName, null);
  }
}
