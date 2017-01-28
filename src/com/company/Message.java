package com.company;

import java.nio.file.Path;

/**
 * Created by cameronphillips on 1/27/17.
 */
//passed via blockingqueue between producers and consumers
//uses Poison pill design to signify end of processing
public class Message {

    boolean isPoison;
    final Path path;

    public Message(){

    }

}
