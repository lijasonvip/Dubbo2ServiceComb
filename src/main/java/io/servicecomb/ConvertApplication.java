package io.servicecomb;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.servicecomb.replacer.JavaFileReplacer;
import io.servicecomb.utils.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.servicecomb.replacer.PomReplacer;

public class ConvertApplication {

    public static void main(String[] args) throws IOException, XmlPullParserException {
        DirectoryManager directoryManager = new DirectoryManager("/home/bo/workspace/dubbo-example/dubbo-sample");

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
                            MicroserviceYamlGenerator
                                    .generate(resourcePath, directoryManager.getArtifactId(), dubboProperties);
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

                    // register interface to provider first to make Service annotation work
                    List<String> allJavaFiles = fileManager.getAllJavaFiles();
                    for (String javaFile : allJavaFiles) {
                        JavaParseUtils.registerInterfaceToProvider(providerName, javaFile);
                    }
                    // TODO: parse java file
                    JavaFileReplacer start = new JavaFileReplacer();
                    start.ReadJavaFile(allJavaFiles);
                }
            }
        }
    }
}
