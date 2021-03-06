package com.mentics.shenj;

import java.io.File;


public class REPL {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            File f = new File(args[0]);
            if (f.exists()) {
                ShenjRuntime.loadImage(new File(args[0]));
            } else {
                ShenjRuntime.loadDefaultImage();
            }
        } else {
            ShenjRuntime.loadDefaultImage();
        }
        ShenjRuntime.topLevel.apply("shenj.root.ShenShen");
    }
}
