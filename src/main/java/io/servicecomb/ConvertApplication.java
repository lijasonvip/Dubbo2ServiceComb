package io.servicecomb;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.servicecomb.replacer.JavaFileReplacer;
import io.servicecomb.replacer.PomReplacer;
import io.servicecomb.utils.DirectoryManager;
import io.servicecomb.utils.FileManager;
import io.servicecomb.utils.FileUtils;
import io.servicecomb.utils.JavaParseUtils;
import io.servicecomb.utils.MicroserviceYamlGenerator;
import io.servicecomb.utils.XmlGenerator;

public class ConvertApplication {

    public static void main(String[] args) throws IOException, XmlPullParserException {
        String default_transfe_path = "/home/bo/workspace/opensource/dubbo-to-servicecomb/transfer-demo";
        String default_scaddress = "http://10.229.42.155:30100";
        if (args.length > 0){
            //if length is 1, set destination path
            //if length si 2, set destination path and default service center address
            if (args.length == 1){
                default_transfe_path = (String) args[0];
            }else if(args.length == 2){
                default_transfe_path = (String) args[0];
                default_scaddress = (String) args[1];
            }else{
                //log error
            }

        }

        DirectoryManager directoryManager = new DirectoryManager(default_transfe_path);


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
                if (file.getName().contains("target")) {
                    continue;
                }
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
                                    .generate(resourcePath, directoryManager.getArtifactId(), dubboProperties, default_scaddress);
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
