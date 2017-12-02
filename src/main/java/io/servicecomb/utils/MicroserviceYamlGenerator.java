package io.servicecomb.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import io.servicecomb.DubboProperties;
import io.servicecomb.ServiceCombProperties;
import io.servicecomb.ServiceCombProperties.Cse;
import io.servicecomb.ServiceCombProperties.Cse.CseRestAddress;
import io.servicecomb.ServiceCombProperties.Cse.CseService;
import io.servicecomb.ServiceCombProperties.Cse.CseService.CseServiceRegistry;
import io.servicecomb.ServiceCombProperties.ServiceDefinition;

public class MicroserviceYamlGenerator {
  private static final String MICROSERVICE_FILE_NAME = "microservice.yaml";

  private String DEFAULT_SERVICE_REGISTRY_ADDRESS = "http://10.229.42.155:30100";

  private static final Representer representer = new Representer() {
    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,Tag customTag) {
      // if value of property is null, ignore it.
      if (propertyValue == null) {
        return null;
      }
      else {
        return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
      }
    }
  };

  public static void generate(String resourceLocation, String applicationId,
      DubboProperties dubboProperties, String scaddress) throws IOException {
    ServiceCombProperties serviceCombProperties = convertDubboPropertiesToServiceCombProperties(applicationId,
        dubboProperties, scaddress);
    try (Writer writer = new FileWriter(resourceLocation + "/" + MICROSERVICE_FILE_NAME)) {
      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(FlowStyle.BLOCK);
      Yaml yaml = new Yaml(representer, options);
      yaml.dump(serviceCombProperties, writer);
    }
  }



  private static ServiceCombProperties convertDubboPropertiesToServiceCombProperties(String rootArtifactId,
      DubboProperties dubboProperties, String scaddress) {
    ServiceCombProperties serviceCombProperties = new ServiceCombProperties();
    serviceCombProperties.setAPPLICATION_ID(rootArtifactId);

    ServiceDefinition serviceDefinition = new ServiceDefinition(dubboProperties.getApplication(), "0.0.1");
    serviceCombProperties.setService_description(serviceDefinition);

    Cse cse = new Cse();
    CseService cseService = new CseService();
    CseServiceRegistry cseServiceRegistry = new CseServiceRegistry();
    cseServiceRegistry.setAddress(scaddress);
    cseService.setRegistry(cseServiceRegistry);
    cse.setService(cseService);

    if (dubboProperties.isProvider()) {
      CseRestAddress cseRestAddress = new CseRestAddress();
      cseRestAddress.setAddress("0.0.0.0:" + dubboProperties.getPort());
      cse.setRest(cseRestAddress);
    }
    serviceCombProperties.setCse(cse);
    return serviceCombProperties;
  }
}
