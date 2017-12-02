package io.servicecomb.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    //tool class to deal files
    //list all file recursively and return java file list and xml file list separately

    private List<String> allJavaFiles = new ArrayList<>();
    private List<String> allXmlFiles = new ArrayList<>();

    public FileManager(String path) {
        traverseFolder(path);
    }

    private void traverseFolder(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            // empty dir
            if (files == null || files.length == 0) {
                return;
            }
            for (File f : files){
                if (f.isDirectory()) {
                    //recursive
                    traverseFolder(f.getAbsolutePath());
                }else{
                    String filename = f.getAbsolutePath();
                    if (filename.endsWith(".java")){
                        allJavaFiles.add(filename);
                    } else if (filename.endsWith(".xml")) {
                        allXmlFiles.add(filename);
                    }
                }
            }
        }
    }

    public List<String> getAllJavaFiles() {
        return allJavaFiles;
    }

    public List<String> getAllXmlFiles() {
        return allXmlFiles;
    }


}
