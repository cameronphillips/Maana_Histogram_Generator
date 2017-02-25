package com.company;

import java.nio.file.Path;
import java.util.Map;

/**
 * Created by cameronphillips on 1/27/17.
 */
//passed via blockingqueue between producers and consumers
//uses Poison pill design to signify end of processing
public class Message {

    public boolean isPoison() {
        return isPoison;
    }

    public Path getPath() {
        return path;
    }

    public Map<String, Long> getText() {
        return text;
    }

    private final boolean isPoison;

    public void setText(Map<String, Long> text) {
        this.text = text;
    }

    private final Path path;

    private Map<String, Long> text;

    Message(boolean isPoison, Path path){
        this.path = path;
        this.isPoison = isPoison;
        this.text = null;
    }
}
