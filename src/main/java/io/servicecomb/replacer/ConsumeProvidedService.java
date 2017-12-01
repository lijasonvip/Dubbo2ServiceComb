package io.servicecomb.replacer;

import io.servicecomb.utils.Files;

import java.util.Arrays;
import java.util.List;

public class ConsumeProvidedService {

    public String ConsumerCallingProviderReplacer(String code){
        String[] rows = code.split("\n");
        for(int i=0;i < rows.length;i++){
            String s = rows[i];

            if (s.contains(JavaFileReplacer.getBeanStr)) {
                String context = getContext(s);
                //replace context with Beanuitls and replace its defination
                int index = s.indexOf(JavaFileReplacer.getBeanStr);
                String tmp = s.replace(context, "BeanUtils");
                rows[i] = tmp;
                //find defination
                for (int j=0; j< rows.length; j++){
                    String t = rows[j];
                    if(t.contains(context)){
                        if (CheckDefination(t, context)){
                            //yes, this is defination, just delete it
                            String SCBeanDefine = "Log4jUtils.init();" + "\n" + "BeanUtils.init();" + "\n";
                            rows[j] = SCBeanDefine;
                            //
                        }
                    }
                }
            }
            //consider if return needed
        }



       return PrintList(Arrays.asList(rows));

    }

    public String getContext(String row){
        String[] sentence = row.split(" ");
        for (String s:sentence){
            if (s.contains(JavaFileReplacer.getBeanStr)){
                String[] temp = s.split("\\.");
                if(temp.length > 1){
                    return temp[0];
                }
            }
        }
        return "";
    }

    public boolean CheckDefination(String row, String var){
        if(!row.contains("=")){
            return false;
        }else{
            int varindex = row.indexOf(var);
            int equalindex = row.indexOf("=");
            if (varindex + var.length() < equalindex){
                return true;
            }
        }
        return false;
    }

    public String PrintList(List<String> input){
        StringBuffer sb = new StringBuffer();
        for (String s : input) {
            sb.append(sb).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
//        String temp = "SomeService someService = (SomeService) context.getBean(\"someServiceRef\");";
//        String replaced = temp.replace("context", "abc");
//        System.out.println(replaced);


        Files filetool = new Files();
        String code = filetool.getFileContentAsString("/home/bo/workspace/dubbo-example/dubbo-sample/dubbo-consumer/src/main/java/io/servicecomb/demo/consumer/ConsumerMain.java");
        ConsumeProvidedService cs = new ConsumeProvidedService();
//        String defination = "ApplicationContext context = new ClassPathXmlApplicationContext(\"conf/applicationContext.xml\");";
//        System.out.println(cs.CheckDefination(defination, "context"));
        cs.ConsumerCallingProviderReplacer(code);
    }
}
