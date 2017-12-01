package io.servicecomb.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.servicecomb.DubboProperties;
import io.servicecomb.DubboProperties.Bean;
import io.servicecomb.DubboProperties.Reference;
import io.servicecomb.DubboProperties.Service;
import io.servicecomb.ProviderInfo;

public class XmlGenerator {
  private static final String FILE_LOCATION = "/META-INF/spring/";

  public static void generate(String resourceLocation, DubboProperties dubboProperties) {
    boolean isProvider = false;
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      document.setXmlStandalone(true);
      Element rootElement = document.createElementNS("http://www.springframework.org/schema/beans", "beans");
      rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      rootElement.setAttribute("xmlns:p", "http://www.springframework.org/schema/p");
      rootElement.setAttribute("xmlns:util", "http://www.springframework.org/schema/util");
      rootElement.setAttribute("xmlns:cse", "http://www.huawei.com/schema/paas/cse/rpc");
      rootElement.setAttribute("xmlns:context", "http://www.springframework.org/schema/context");
      rootElement.setAttribute("xsi:schemaLocation",
          "http://www.springframework.org/schema/beans classpath:org/springframework/beans/factory/xml/spring-beans-3.0.xsd "
              + "http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd "
              + "http://www.huawei.com/schema/paas/cse/rpc classpath:META-INF/spring/spring-paas-cse-rpc.xsd");

      document.appendChild(rootElement);
      if (dubboProperties.getServices() != null) {
        for (Service service : dubboProperties.getServices()) {
          String schemaId = getSchemaId(service.getInterfaceName());
          Element serviceElement = document.createElement("cse:rpc-schema");
          serviceElement.setAttribute("schema-id", schemaId);
          for (Bean bean : dubboProperties.getBeans()) {
            if (bean.getId().equals(service.getReference())) {
              serviceElement.setAttribute("implementation", bean.getClassName());
              break;
            }
          }
          rootElement.appendChild(serviceElement);
          isProvider = true;
        }
      }
      if (dubboProperties.getReferences() != null) {
        for (Reference reference : dubboProperties.getReferences()) {
          String schemaId = getSchemaId(reference.getInterfaceName());
          Element referenceElement = document.createElement("cse:rpc-reference");
          referenceElement.setAttribute("id", reference.getId());
          String providerName = ProviderInfo.getMicroserviceNameByInterface(reference.getInterfaceName());
          referenceElement.setAttribute("microservice-name", providerName);
          referenceElement.setAttribute("schema-id", schemaId);
          referenceElement.setAttribute("interface", reference.getInterfaceName());
          rootElement.appendChild(referenceElement);
        }
      }

      String fileName = isProvider ? "provider" : "consumer";
      fileName += ".bean.xml";

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      DOMSource source = new DOMSource(document);
      String directory = resourceLocation + FILE_LOCATION;
      new File(directory).mkdirs(); // in case there is no such directory
      StreamResult result = new StreamResult(new File(directory + fileName));
      transformer.transform(source, result);
    } catch (ParserConfigurationException | TransformerException e) {
      System.out.println("unable to generate servicecomb xml file " + e);
    }
  }

  private static String getSchemaId(String interfaceName) {
    int index = interfaceName.lastIndexOf('.');
    return interfaceName.substring(index + 1) + "Endpoint";
  }
}
