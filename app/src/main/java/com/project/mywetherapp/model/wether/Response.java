package com.project.mywetherapp.model.wether;

public class Response {
    public Header header;
    public Body body;

    @Override
    public String toString() {
        return "Response{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}
