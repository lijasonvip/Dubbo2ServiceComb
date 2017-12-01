package io.servicecomb;

import java.io.IOException;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.servicecomb.replacer.PomReplacer;
import io.servicecomb.utils.MicroserviceYamlGenerator;
import io.servicecomb.utils.XmlGenerator;

public class Application {
  public static void main(String[] args) throws IOException, XmlPullParserException {
    PomReplacer.migrateDubboToServiceComb("/tmp/pom.xml");
    // provider
    DubboProperties providerDubboProperties = new DubboProperties("/tmp/dubbo.provider.xml");

    // consumer
    DubboProperties dubboProperties = new DubboProperties("/tmp/dubbo.consumer.xml");
    if (!dubboProperties.isSatisfiedConsumer()) {
      // added current path to todo path
    }
    System.out.println(dubboProperties.getApplication());
    MicroserviceYamlGenerator.generate("/tmp", "test", dubboProperties);
    XmlGenerator.generate("/tmp", dubboProperties);
  }
}
