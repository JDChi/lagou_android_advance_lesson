package com.example.testclassloaderdemo;

public class SayException implements ISay {
    @Override
    public String saySomething() {
        return "something wrong here";
    }
}
