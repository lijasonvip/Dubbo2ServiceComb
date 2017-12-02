package io.servicecomb.replacer;

import io.servicecomb.utils.Files;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsumeProvidedService {

    public void ConsumerCallingProviderReplacer(String filepath) {
        Files filetool = new Files();
        String code = filetool.getFileContentAsString(filepath);
        String[] rows = code.split("\n");
        for (int i = 0; i < rows.length; i++) {
            String s = rows[i];
            //not necessary
//            if (s.contains(JavaFileReplacer.DubboApplicationCtxImport) || s.contains(JavaFileReplacer.DubboClassPathXmlAppCtxImport)) {
//                rows[i] = "\n";
//            }

            //delete import com.alibaba.dubbo.rpc.RpcContext; this kind of import not suppose to be here, just in case.
            if (s.contains("com.alibaba.")) {
                System.out.println("------ dealing with: "+s);
                rows[i] = "\n";
            }

            //get bean
            Set<String> set = new HashSet<>();
            if (s.contains(JavaFileReplacer.getBeanStr)) {
                System.out.println("------ dealing with: "+s);
                String context = getContext(s);
                int index = s.indexOf(JavaFileReplacer.getBeanStr);
                String tmp = s.replace(context, "BeanUtils");
                rows[i] = tmp;
            }
            //consider if return needed
        }

        String replaced = filetool.PrintList(Arrays.asList(rows));
        filetool.SaveStringToFile(filepath, replaced);
    }

    public String getContext(String row) {
        String[] sentence = row.split(" ");
        for (String s : sentence) {
            if (s.contains(JavaFileReplacer.getBeanStr)) {
                String[] temp = s.split("\\.");
                if (temp.length > 1) {
                    return temp[0];
                }
            }
        }
        return "";
    }

    public boolean CheckDefination(String row, String var) {
        if (!row.contains("=")) {
            return false;
        } else {
            int varindex = row.indexOf(var);
            int equalindex = row.indexOf("=");
            if (varindex + var.length() < equalindex) {
                return true;
            }
        }
        return false;
    }

}
