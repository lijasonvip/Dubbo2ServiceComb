package io.servicecomb.replacer;

import io.servicecomb.utils.Files;

import java.util.Arrays;

public class ServiceToScheme {

    //tool class to handler service type
    //input file path
    //output replaced String
    public void ServiceTypeReplacer(String filepath){
        Files filetool = new Files();
        String code = filetool.getFileContentAsString(filepath);
        String[] rows = code.split("\n");
        String schemeId = "";
        //find schemeid
        for(int i=0;i<rows.length;i++){
            String r = rows[i];
            if (r.contains("class") && r.contains("implements") && r.indexOf("class") < r.indexOf("implements")){
                schemeId = GetSchemeId(r);
            }
        }


        JavaFileReplacer jfr = new JavaFileReplacer();
        for(int i=0;i<rows.length;i++){
            String r = rows[i];
            if (r.contains(JavaFileReplacer.importDubboServiceStr)){
                rows[i] = JavaFileReplacer.importSCRpcSchemaStr;
            }
            if (r.contains(JavaFileReplacer.dubboServiceAnnotStr)){
                rows[i] = MakeSchemeAnnot(schemeId);
            }
        }

        String replaced = filetool.PrintList(Arrays.asList(rows));
        filetool.SaveStringToFile(filepath, replaced);
    }

    public String GetSchemeId(String row){
        //public class SomeServiceImpl implements SomeService {
        int implIndex = row.indexOf("implements");
        int endIndex = row.indexOf("{");
        String sub = row.substring(implIndex+"implements".length(), endIndex);
        return sub.trim()+"Endpoint";
    }

    public String MakeSchemeAnnot(String schemaId){
        return "@RpcSchema(schemaId = \""+ schemaId + "\")";
    }

    public static void main(String[] args) {
        ServiceToScheme sts = new ServiceToScheme();
        String row = "public class SomeServiceImpl implements SomeService {";
        System.out.println(sts.MakeSchemeAnnot(sts.GetSchemeId(row)));
    }
}
