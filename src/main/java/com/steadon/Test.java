package com.steadon;

import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        EasyIO easyIO = new EasyIO();
        try {
            easyIO.showImages("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
