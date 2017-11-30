package io.servicecomb.utils;

public class ReplacerJavaType {
    boolean isServiceImportFind;
    boolean isServiceAnnoted;

    boolean isBeanUtilFind;
    boolean isLogUtilFind;
    boolean isMainFind;

    boolean isGetBeanFild;

    public ReplacerJavaType() {
    }

    public ReplacerJavaType(boolean isServiceImportFind, boolean isServiceAnnoted, boolean isBeanUtilFind, boolean isLogUtilFind, boolean isMainFind, boolean isGetBeanFild) {
        this.isServiceImportFind = isServiceImportFind;
        this.isServiceAnnoted = isServiceAnnoted;
        this.isBeanUtilFind = isBeanUtilFind;
        this.isLogUtilFind = isLogUtilFind;
        this.isMainFind = isMainFind;
        this.isGetBeanFild = isGetBeanFild;
    }

    public boolean isServiceImportFind() {
        return isServiceImportFind;
    }

    public void setServiceImportFind(boolean serviceImportFind) {
        isServiceImportFind = serviceImportFind;
    }

    public boolean isServiceAnnoted() {
        return isServiceAnnoted;
    }

    public void setServiceAnnoted(boolean serviceAnnoted) {
        isServiceAnnoted = serviceAnnoted;
    }

    public boolean isBeanUtilFind() {
        return isBeanUtilFind;
    }

    public void setBeanUtilFind(boolean beanUtilFind) {
        isBeanUtilFind = beanUtilFind;
    }

    public boolean isLogUtilFind() {
        return isLogUtilFind;
    }

    public void setLogUtilFind(boolean logUtilFind) {
        isLogUtilFind = logUtilFind;
    }

    public boolean isMainFind() {
        return isMainFind;
    }

    public void setMainFind(boolean mainFind) {
        isMainFind = mainFind;
    }

    public boolean isGetBeanFild() {
        return isGetBeanFild;
    }

    public void setGetBeanFild(boolean getBeanFild) {
        isGetBeanFild = getBeanFild;
    }
}
