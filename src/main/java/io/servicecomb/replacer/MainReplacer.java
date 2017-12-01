package io.servicecomb.replacer;

import io.servicecomb.utils.Files;

import java.util.Arrays;
import java.util.Stack;

public class MainReplacer {

    //input filepath output replaced file string
    public void ReplaceMainEntrance(String filepath) {
        Files filetool = new Files();
        String code = filetool.getFileContentAsString(filepath);
        String[] rows = code.split("\n");
        //delete origin unnecessary import
        for (int i = 0; i < rows.length; i++) {
            String r = rows[i];
            if (r.contains(JavaFileReplacer.DubboApplicationCtxImport)) {
                rows[i] = "\n";
            }
            if (r.contains(JavaFileReplacer.DubboClassPathXmlAppCtxImport)) {
                rows[i] = "\n";
            }

        }
        //add sc import
        for (int i = 0; i < rows.length; i++) {
            String r = rows[i];
            if (r.startsWith("import")) {
                rows[i] = JavaFileReplacer.SCLogUtilImport + "\n" + JavaFileReplacer.SCBeanUtilsImport + "\n";
                //make sure scimport only been imported once
                break;
            }
        }

        //find line number of main and delete original context defination
        int mainrow = -1;
        for (int i = 0; i < rows.length; i++) {
            String r = rows[i];
            if (r.contains(JavaFileReplacer.mainStr)) {
                //step into main
                //deal exception
                String rowStr = rows[i];

                int startindex = rowStr.indexOf(")");
                rowStr = rowStr.substring(0,startindex) + ") throws Exception {";
                rows[i] = rowStr;
                mainrow = i;
            }
            //ApplicationContext context = new ClassPathXmlApplicationContext("conf/applicationContext.xml");
            if (mainrow != -1) {
                if (r.contains("ApplicationContext") || r.contains("ClassPathXmlApplicationContext")) {
                    rows[i] = "\n";
                    for(int j=i-1;j>mainrow;j--){
                        if (!rows[j].endsWith(";") && !rows[j].startsWith("@")){
                            rows[j] = "\n";
                        }
                        if(rows[j].startsWith("@")){
                            rows[j] = "\n";
                            break;
                        }
                    }
                }

            }
        }

        //add sc log4j util init and bean util init
        if (mainrow != -1) {
            rows[mainrow] = rows[mainrow] + "\n" + JavaFileReplacer.SCLogUtilInit + "\n" + JavaFileReplacer.SCBeanUtilInit + "\n";
        }
        String replaced = filetool.PrintList(Arrays.asList(rows));
        filetool.SaveStringToFile(filepath, replaced);
    }
    
}
