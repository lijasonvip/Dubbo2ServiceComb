package io.servicecomb.replacer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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

    public void ReadJavaFile(List<String> files) {
        for (String f : files) {
            Replacer(f);
            System.out.println("replacing " + f);
        }
        System.out.println("done");
    }

    public void Replacer(String f) {
        //mark if import alibaba.dubbo...Service is find
        //mark @Service is find

        boolean isServiceImportFind = false;
        boolean isServiceAnnoted = false;

        boolean isBeanFind = false;
        boolean isLogFind = false;
        boolean isMainFind = false;

        File file = new File(f);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String fileRow = null;
            while ((fileRow = reader.readLine()) != null) {
                if (fileRow.contains(importDubboServiceStr)) {
                    fileRow = importSCRpcSchemaStr;
                    isServiceImportFind = true;
                }

                if (isServiceImportFind && !fileRow.startsWith("/") && fileRow.contains(dubboServiceAnnotStr)) {
                    fileRow = SCRpcSchemaAnnotStr;
                    isServiceAnnoted = true;
                }

                if (fileRow.contains(DubboApplicationCtxImport)){
                    fileRow = SCBeanUtilsImport;
                    isBeanFind = true;
                }
                if(fileRow.contains(DubboClassPathXmlAppCtxImport)){
                    fileRow = SCLogUtilImport;
                    isLogFind = true;
                }

                if (fileRow.contains(mainStr)){
                    isMainFind = true;
                }
                sb.append(fileRow);
                sb.append("\n");
                if (isBeanFind && isLogFind && isMainFind){
                    //readline to main
                    //use stack to find } to mach { in main line
                    Stack<String> stack = new Stack<String>();
                    stack.push("{");
                    while((fileRow = reader.readLine()) != null){
                        stack = findMainEnd(stack, fileRow);
                        if (!stack.isEmpty()){
                            continue;
                        }else{
                            sb.append(SCLogUtilInit);
                            sb.append("\n");
                            sb.append(SCBeanUtilInit);
                            sb.append("\n");
                            sb.append("}\n");
                            break;
                        }
                    }

                }
            }
            reader.close();
            //only when isImportFind and isAnnoted find do the save work
            //if main, main content need to replace too
            if ((isServiceImportFind && isServiceAnnoted) || (isBeanFind && isLogFind && isMainFind)) {
                PrintWriter printWriter = new PrintWriter(f);
                printWriter.write(sb.toString().toCharArray());
                printWriter.flush();
                printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stack<String > findMainEnd(Stack<String> stack, String str){
        if (!str.contains("{") && !str.contains("}"))
            return stack;
        else{
            char[] chars = str.toCharArray();
            for (char c:chars){
                if(c == '{'){
                    stack.push("{");
                }else if (c == '}'){
                    if(!stack.isEmpty()){
                        stack.pop();
                    }
                }
            }
        }
        return stack;
    }

    public static void main(String[] args) {
        JavaFileReplacer jfr = new JavaFileReplacer();
        List<String> files = new ArrayList<String>();
        files.add("/home/bo/workspace/dubbo-example/dubbo-sample/dubbo-provider/src/main/java/io/servicecomb/demo/provider/SomeServiceImpl.java");
        files.add("/home/bo/workspace/dubbo-example/dubbo-sample/dubbo-provider/src/main/java/io/servicecomb/demo/provider/ProviderMain.java");
        jfr.ReadJavaFile(files);

    }


}
