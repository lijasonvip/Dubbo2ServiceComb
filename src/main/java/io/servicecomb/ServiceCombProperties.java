package io.servicecomb;

public class ServiceCombProperties {
  private String APPLICATION_ID;
  private ServiceDefinition service;

  private Cse cse;

  public String getAPPLICATION_ID() {
    return APPLICATION_ID;
  }

  public void setAPPLICATION_ID(String APPLICATION_ID) {
    this.APPLICATION_ID = APPLICATION_ID;
  }

  public ServiceDefinition getService() {
    return service;
  }

  public void setService(ServiceDefinition service) {
    this.service = service;
  }

  public Cse getCse() {
    return cse;
  }

  public void setCse(Cse cse) {
    this.cse = cse;
  }

  public static class ServiceDefinition {
    private String name;
    private String version;

    public ServiceDefinition(String name, String version) {
      this.name = name;
      this.version = version;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }
  }

  public static class Cse {
    private CseService service_description;
    private CseRestAddress rest;

    public CseService getService_description() {
      return service_description;
    }

    public void setService_description(CseService service_description) {
      this.service_description = service_description;
    }

    public CseRestAddress getRest() {
      return rest;
    }

    public void setRest(CseRestAddress rest) {
      this.rest = rest;
    }

    public static class CseService {
      private CseServiceRegistry registry;

      public static class CseServiceRegistry {
        private String address;

        public String getAddress() {
          return address;
        }

        public void setAddress(String address) {
          this.address = address;
        }
      }

      public CseServiceRegistry getRegistry() {
        return registry;
      }

      public void setRegistry(CseServiceRegistry registry) {
        this.registry = registry;
      }
    }

    public static class CseRestAddress {
      private String address;

      public String getAddress() {
        return address;
      }

      public void setAddress(String address) {
        this.address = address;
      }
    }
  }
}
