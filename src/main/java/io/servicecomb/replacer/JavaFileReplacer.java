package io.servicecomb.replacer;

import io.servicecomb.utils.ReplacerJavaType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JavaFileReplacer {
    //tool class to dead java files
    public static final String importDubboServiceStr = "import com.alibaba.dubbo.config.annotation.Service;";
    public static final String dubboServiceAnnotStr = "@Service";
    public static final String importSCRpcSchemaStr = "import io.servicecomb.provider.pojo.RpcSchema;";
    public static final String SCRpcSchemaAnnotStr = "@RpcSchema()";


    public static final String SCBeanUtilsImport = "import io.servicecomb.foundation.common.utils.BeanUtils;";
    public static final String SCLogUtilImport = "import io.servicecomb.foundation.common.utils.Log4jUtils;";

    public static final String DubboApplicationCtxImport = "import org.springframework.context.ApplicationContext;";
    public static final String DubboClassPathXmlAppCtxImport = "import org.springframework.context.support.ClassPathXmlApplicationContext;";
    public static final String SCBeanUtilInit = "BeanUtils.init();";
    public static final String SCLogUtilInit = "Log4jUtils.init();";
    public static final String mainStr = "public static void main(String[] args)";
    public static final String getBeanStr = ".getBean(";


    public void ReadJavaFile(List<String> files) {
        for (String f : files) {
            ReplacerJavaType r = MarkReplaceType(f);
            ReplaceStarter(f, r);
            System.out.println("--- Transfering: " + f);
            System.out.println("--- Success!");
        }
    }

    public ReplacerJavaType MarkReplaceType(String f) {
        ReplacerJavaType replacerJavaType = new ReplacerJavaType();
        //mark replace type first
        //replace second
        File file = new File(f);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String fileRow = null;
            while ((fileRow = reader.readLine()) != null) {
                if (fileRow.contains(importDubboServiceStr)) {
                    replacerJavaType.setServiceImportFind(true);
                }

                if (fileRow.contains(dubboServiceAnnotStr)) {
                    replacerJavaType.setServiceAnnoted(true);
                }

                if (fileRow.contains(DubboApplicationCtxImport)) {
                    replacerJavaType.setBeanUtilFind(true);
                }
                if (fileRow.contains(DubboClassPathXmlAppCtxImport)) {
                    replacerJavaType.setLogUtilFind(true);
                }

                if (fileRow.contains(mainStr)) {
                    replacerJavaType.setMainFind(true);
                }

                if (fileRow.contains(getBeanStr)) {
                    replacerJavaType.setGetBeanFild(true);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return replacerJavaType;
    }

    public void ReplaceStarter(String f, ReplacerJavaType replacerJavaType) {
        if (replacerJavaType.isServiceImportFind() && replacerJavaType.isServiceAnnoted()) {
            //Service type
            ServiceToScheme sts = new ServiceToScheme();
            sts.ServiceTypeReplacer(f);
        }

        if (replacerJavaType.isMainFind()) {
            MainReplacer mr = new MainReplacer();
            mr.ReplaceMainEntrance(f);
        }

        if (replacerJavaType.isGetBeanFild()) {
            ConsumeProvidedService cps = new ConsumeProvidedService();
            cps.ConsumerCallingProviderReplacer(f);
        }
    }

}
