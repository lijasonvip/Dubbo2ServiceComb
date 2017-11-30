package io.servicecomb.replacer;

import com.sun.prism.impl.packrect.RectanglePacker;
import com.sun.xml.internal.ws.wsdl.writer.document.Import;
import io.servicecomb.utils.ReplacerJavaType;

import javax.swing.*;
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
    public static final String getBeanStr = ".getBean(";


    public void ReadJavaFile(List<String> files) {
        for (String f : files) {
            ReplacerJavaType r = MarkReplaceType(f);
            ReplaceStarter(f, r);
            System.out.println("replacing " + f);
        }
        System.out.println("done");
    }

    public ReplacerJavaType MarkReplaceType(String f) {
        ReplacerJavaType replacerJavaType = new ReplacerJavaType();
        //mark replace type first
        //replace second
        File file = new File(f);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return replacerJavaType;
    }

    public void ReplaceStarter(String f, ReplacerJavaType replacerJavaType) {
        if (replacerJavaType.isServiceImportFind() && replacerJavaType.isServiceAnnoted()) {
            //Service type
            ReplaceServiceType(f, replacerJavaType);
        }

        if (replacerJavaType.isMainFind() && !replacerJavaType.isGetBeanFild()) {
            //consumer main type
            ReplaceProviderMainType(f, replacerJavaType);
        }

        if (replacerJavaType.isBeanUtilFind() && replacerJavaType.isLogUtilFind() && replacerJavaType.isMainFind() && !replacerJavaType.isGetBeanFild()) {
            //provider main type
            ReplaceConsumerMainType(f, replacerJavaType);
        }

    }

    public void ReplaceServiceType(String f, ReplacerJavaType replacerJavaType) {
        File file = new File(f);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String fileRow = null;
            while ((fileRow = reader.readLine()) != null) {
                if (fileRow.contains(importDubboServiceStr)) {
                    fileRow = importSCRpcSchemaStr;
                }

                if (!fileRow.startsWith("/") && fileRow.contains(dubboServiceAnnotStr)) {
                    //TODO:need schemaID here
                    fileRow = SCRpcSchemaAnnotStr;
                }

                sb.append(fileRow);
                sb.append("\n");
            }
            reader.close();
            //only when isImportFind and isAnnoted find do the save work
            //if main, main content need to replace too
            PrintWriter printWriter = new PrintWriter(f);
            printWriter.write(sb.toString().toCharArray());
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ReplaceProviderMainType(String f, ReplacerJavaType replacerJavaType) {
        File file = new File(f);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        boolean inMainFuction = false;
        try {
            reader = new BufferedReader(new FileReader(file));
            String fileRow = null;
            while ((fileRow = reader.readLine()) != null) {
                if (fileRow.contains(DubboApplicationCtxImport)) {
                    fileRow = SCBeanUtilsImport;
                }
                if (fileRow.contains(DubboClassPathXmlAppCtxImport)) {
                    fileRow = SCLogUtilImport;
                }
                if (fileRow.contains(mainStr)) {
                    inMainFuction = true;
                }
                sb.append(fileRow);
                sb.append("\n");
                if (inMainFuction) {
                    //readline to main
                    //use stack to find } to mach { in main line
                    Stack<String> stack = new Stack<String>();
                    stack.push("{");
                    while ((fileRow = reader.readLine()) != null) {
                        stack = findMainEnd(stack, fileRow);
                        if (!stack.isEmpty()) {
                            continue;
                        } else {
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
            PrintWriter printWriter = new PrintWriter(f);
            printWriter.write(sb.toString().toCharArray());
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ReplaceConsumerMainType(String f, ReplacerJavaType replacerJavaType) {
        //replace before getBean with util.init
        //Get object getBean returned and replace with SC object
        //user CompletableFuture to call provider service
        boolean inMainFunction = false;
        List<String> importsStrs = new ArrayList<String>(); //delete Future and ClassPathXmlApplicationContext

        File file = new File(f);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        boolean inMainFuction = false;
        try {
            reader = new BufferedReader(new FileReader(file));
            String fileRow = null;
            while ((fileRow = reader.readLine()) != null) {
                if (fileRow.startsWith("import")) {
                    importsStrs.add(fileRow);
                }
                if (fileRow.contains(mainStr)) {
                    inMainFuction = true;
                }
                sb.append(fileRow);
                sb.append("\n");
                if (inMainFuction) {
                    //delete all annotation

                    //replace context

                    //replace future
                }
            }
            reader.close();
            //only when isImportFind and isAnnoted find do the save work
            //if main, main content need to replace too
            PrintWriter printWriter = new PrintWriter(f);
            printWriter.write(sb.toString().toCharArray());
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Stack<String> findMainEnd(Stack<String> stack, String str) {
        if (!str.contains("{") && !str.contains("}"))
            return stack;
        else {
            char[] chars = str.toCharArray();
            for (char c : chars) {
                if (c == '{') {
                    stack.push("{");
                } else if (c == '}') {
                    if (!stack.isEmpty()) {
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
        files.add("/home/bo/workspace/dubbo-example/dubbo-sample/dubbo-consumer/src/main/java/io/servicecomb/demo/consumer/ConsumerMain.java");
//        files.add("/home/bo/workspace/dubbo-example/dubbo-sample/dubbo-provider/src/main/java/io/servicecomb/demo/provider/ProviderMain.java");
        jfr.ReadJavaFile(files);

    }


}
