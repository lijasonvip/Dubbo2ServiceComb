package io.servicecomb.replacer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PomReplacer {

  private static final Dependency JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY = new Dependency();

  private static final Dependency TRANSPORT_HIGHWAY_DEPENDENCY = new Dependency();

  private static final Dependency TRANSPORT_REST_VERTX_DEPENDENCY = new Dependency();

  public static final Dependency PROVIDER_POJO_DEPENDENCY = new Dependency();

  private static final String SERVICECOMB_GROUP_ID = "io.servicecomb";

  public static final String SERVICECOMB_VERSION = "0.4.0";

  private static final String DUBBO_GROUP_ID = "com.alibaba";

  private static final String DUBBO_ARTIFACT_ID = "dubbo";

  static {
    JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY.setGroupId(SERVICECOMB_GROUP_ID);
    JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY.setArtifactId("java-chassis-dependencies");
    JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY.setVersion(SERVICECOMB_VERSION);
    JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY.setScope("import");
    JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY.setType("pom");

    TRANSPORT_HIGHWAY_DEPENDENCY.setGroupId(SERVICECOMB_GROUP_ID);
    TRANSPORT_HIGHWAY_DEPENDENCY.setArtifactId("transport-highway");

    TRANSPORT_REST_VERTX_DEPENDENCY.setGroupId(SERVICECOMB_GROUP_ID);
    TRANSPORT_REST_VERTX_DEPENDENCY.setArtifactId("transport-rest-vertx");

    PROVIDER_POJO_DEPENDENCY.setGroupId(SERVICECOMB_GROUP_ID);
    PROVIDER_POJO_DEPENDENCY.setArtifactId("provider-pojo");
  }

  public static void convert(String pomXmlFileName) {
    Model model = retrieveModel(pomXmlFileName);
    if (model == null) {
      return;
    }
    model = retrieveMavenModelAfterReplacement(model);

    if (model == null) {
      System.out.println("error parsing pom file, filename: " + pomXmlFileName);
      return;
    }
    MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
    try (Writer writer = new FileWriter(pomXmlFileName)) {
      xpp3Writer.write(writer, model);
    } catch (IOException e) {
      System.out.println("error overwriting pom file, filename: " + pomXmlFileName);
    }
  }

  private static Model retrieveMavenModelAfterReplacement(Model model) {
    List<Dependency> dependencyList = model.getDependencies();
    boolean hasRemoved = dependencyList.removeIf(dependency -> dependency.getArtifactId().equals(DUBBO_ARTIFACT_ID) &&
        dependency.getGroupId().equals(DUBBO_GROUP_ID));

    // no dubbo dependencies, no need to replace
    if (!hasRemoved) {
      return model;
    }

    DependencyManagement dependencyManagement = model.getDependencyManagement();
    if (dependencyManagement == null) {
      dependencyManagement = new DependencyManagement();
    }
    dependencyManagement.addDependency(JAVA_CHASSIS_DEPENDENCIES_DEPENDENCY);
    model.setDependencyManagement(dependencyManagement);

    model.addDependency(TRANSPORT_HIGHWAY_DEPENDENCY);
    model.addDependency(TRANSPORT_REST_VERTX_DEPENDENCY);
    model.addDependency(PROVIDER_POJO_DEPENDENCY);

    List<Profile> profiles = model.getProfiles();
    if (profiles == null) {
      profiles = new ArrayList<>();
    }
    Profile huaweiCloudProfile = getHuaweiCloudProfile();
    profiles.add(huaweiCloudProfile);
    model.setProfiles(profiles);
    return model;
  }

  private static Profile getHuaweiCloudProfile() {
    Profile huaweiCloudProfile = new Profile();
    huaweiCloudProfile.setId("HuaweiCloud");
    Repository repository = new Repository();
    repository.setId("huawei_cloud_cse_dependencies");
    repository.setName("huawei_cloud_cse_dependencies");
    repository.setUrl("http://117.78.31.114:32700/nexus/content/repositories/cse");
    RepositoryPolicy repositoryPolicy = new RepositoryPolicy();
    repositoryPolicy.setEnabled(true);
    repository.setReleases(repositoryPolicy);
    RepositoryPolicy snapshotPolicy = new RepositoryPolicy();
    snapshotPolicy.setEnabled(false);
    repository.setSnapshots(snapshotPolicy);
    List<Repository> repositories = new ArrayList<>();
    repositories.add(repository);
    huaweiCloudProfile.setPluginRepositories(repositories);
    huaweiCloudProfile.setRepositories(repositories);
    List<Dependency> huaweiCloudDependencies = new ArrayList<>();
    Dependency huaweiCloudDependency = new Dependency();
    huaweiCloudDependency.setGroupId("com.huawei.paas.cse");
    huaweiCloudDependency.setArtifactId("cse-solution-service-engine");
    huaweiCloudDependency.setVersion("2.2.7");
    huaweiCloudDependencies.add(huaweiCloudDependency);
    huaweiCloudProfile.setDependencies(huaweiCloudDependencies);
    return huaweiCloudProfile;
  }

  public static String retrieveArtifactId(String pomXmlFileName) {
    Model model = retrieveModel(pomXmlFileName);
    if (model == null) {
      return "";
    }
    return model.getArtifactId();
  }

  private static Model retrieveModel(String pomXmlFileName) {
    try (Reader reader = new FileReader(pomXmlFileName)) {
      MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
      return xpp3Reader.read(reader);
    } catch (IOException | XmlPullParserException e) {
      System.out.println("error parsing pom file, filename: " + pomXmlFileName);
      return null;
    }
  }

  //TODO: regexp with dubbo literal
}
