package io.servicecomb;

import io.servicecomb.replacer.JavaFileReplacer;
import io.servicecomb.utils.Files;

import java.util.List;

public class starter {
    public static void main(String[] args) {
        JavaFileReplacer start = new JavaFileReplacer();
        Files filetools = new Files();
        //traverse destination folder
//        filetools.traverseFolder("/home/bo/workspace/dubbo-example");
        filetools.traverseFolder("/home/bo/workspace/dubbo-example/dubbo-sample");
        //wangkirin dubbo app
        String kirin_app = "/home/bo/workspace/dubbo-app";

        List<String> javaFiles = filetools.getJavaFiles();
        start.ReadJavaFile(javaFiles);
    }
}
