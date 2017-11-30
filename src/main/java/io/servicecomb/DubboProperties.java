package io.servicecomb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DubboProperties {
  private String application;

  private String port;

  private List<Service> services;

  private List<Bean> beans;

  private List<Reference> references;

  private boolean isSatisfiedConsumer;

  public DubboProperties(String dubboPropertyFileName) {
    load(dubboPropertyFileName);
  }

  public boolean isProvider() {
    return services != null;
  }

  private void load(String dubboPropertyFileName) {
    File dubboFile = new File(dubboPropertyFileName);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(dubboFile);
      document.getDocumentElement().normalize();
      application = retrieveDubboAttribute(document, "dubbo:application", "name");
      port = retrieveDubboAttribute(document, "dubbo:protocol", "port");
      NodeList serviceNodeList = document.getElementsByTagName("dubbo:service");
      if (serviceNodeList.getLength() > 0) {
        services = new ArrayList<>();
      }
      for (int i = 0; i < serviceNodeList.getLength(); i++) {
        Node node = serviceNodeList.item(i);
        Element element = (Element) node;
        String interfaceName = element.getAttribute("interface");
        String reference = element.getAttribute("ref");
        Service service = new Service(interfaceName, reference);
        services.add(service);
        ProviderInfo.addProviderInfo(interfaceName, application);
      }
      NodeList beanNodeList = document.getElementsByTagName("bean");
      if (beanNodeList.getLength() > 0) {
        beans = new ArrayList<>();
      }
      for (int i = 0; i < beanNodeList.getLength(); i++) {
        Node node = beanNodeList.item(i);
        Element element = (Element) node;
        String id = element.getAttribute("id");
        String className = element.getAttribute("class");
        Bean bean = new Bean(id, className);
        beans.add(bean);
      }
      NodeList referenceNodeList = document.getElementsByTagName("dubbo:reference");
      if (referenceNodeList.getLength() > 0) {
        references = new ArrayList<>();
      }
      for (int i = 0; i < referenceNodeList.getLength(); i++) {
        Node node = referenceNodeList.item(i);
        Element element = (Element) node;
        String id = element.getAttribute("id");
        String interfaceName = element.getAttribute("interface");
        Reference reference = new Reference(id, interfaceName);
        references.add(reference);
      }

      isSatisfiedConsumer = true;
      for (Reference reference : references) {
        if (ProviderInfo.getMicroserviceNameByInterface(reference.getInterfaceName()) == null) {
          isSatisfiedConsumer = false;
          break;
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      System.out.println("Unable to parse dubbo property file");
    }
  }

  private String retrieveDubboAttribute(Document document, String tagName, String attributeName) {
    NodeList applicationNodeList = document.getElementsByTagName(tagName);
    if (applicationNodeList.getLength() <= 0) {
      return null;
    }
    Node node = applicationNodeList.item(0);
    Element element = (Element) node;
    return element.getAttribute(attributeName);
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  public List<Bean> getBeans() {
    return beans;
  }

  public void setBeans(List<Bean> beans) {
    this.beans = beans;
  }

  public List<Reference> getReferences() {
    return references;
  }

  public void setReferences(List<Reference> references) {
    this.references = references;
  }

  public boolean isSatisfiedConsumer() {
    return isSatisfiedConsumer;
  }

  public class Service {
    private String interfaceName;

    private String reference;

    public Service(String interfaceName, String reference) {
      this.interfaceName = interfaceName;
      this.reference = reference;
    }

    public String getInterfaceName() {
      return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
      this.interfaceName = interfaceName;
    }

    public String getReference() {
      return reference;
    }

    public void setReference(String reference) {
      this.reference = reference;
    }
  }

  public class Bean {
    private String id;

    private String className;

    public Bean(String id, String className) {
      this.id = id;
      this.className = className;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getClassName() {
      return className;
    }

    public void setClassName(String className) {
      this.className = className;
    }
  }

  public class Reference {
    private String id;

    private String interfaceName;

    private boolean async;

    private String timeout;

    public Reference(String id, String interfaceName) {
      this.id = id;
      this.interfaceName = interfaceName;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getInterfaceName() {
      return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
      this.interfaceName = interfaceName;
    }

    public boolean isAsync() {
      return async;
    }

    public void setAsync(boolean async) {
      this.async = async;
    }

    public String getTimeout() {
      return timeout;
    }

    public void setTimeout(String timeout) {
      this.timeout = timeout;
    }
  }
}
