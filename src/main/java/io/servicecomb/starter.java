package io.servicecomb;

import io.servicecomb.replacer.JavaFileReplacer;

public class starter {
    public static void main(String[] args) {
        JavaFileReplacer start = new JavaFileReplacer();
        start.ReadJavaFile(null);
    }
}
