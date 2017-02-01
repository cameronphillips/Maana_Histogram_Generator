package com.company;

import java.lang.Runtime;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by cameronphillips on 1/22/17.
 */
public class PathProducer implements Runnable{
    private final BlockingQueue<Message> newQueue;
    private final String root;
    private final List<Path> toDelete;

    PathProducer(BlockingQueue q, String path){
        newQueue = q;
        root = path;
        toDelete = new ArrayList<>();

        //register shutdown hook to clean up temporary unzipped directories
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                toDelete.forEach((pathToDelete) -> pathToDelete.toFile().delete());
            }
        });
    }

    @Override
    public void run() {
        traverseTree(root);
        //after fully traversing, send poison to indicate processing is complete and to shutdown
        final Message poison = new Message(true, null);
        produce(poison);
        //print out
    }

    private void produce(Message message) {
        try{
            newQueue.put(message);
        }catch (InterruptedException e){
            System.err.println(e);
        }
    }

    private void traverseTree(String root) {
        //boolean function that can be utilized by lambda expression
        //returns only txt and zip files from directory stream
        BiPredicate<Path, BasicFileAttributes> shouldVisit = (path, attr) -> String.valueOf(path).endsWith(".txt");
        shouldVisit = shouldVisit.or( (path, attr) -> String.valueOf(path).endsWith(".zip") );

        //lazy stream generated from directory walk, runs against a bipredicate looking for zip and txt files
        //Compare to calling filter on the Stream returned by walk method, this method may be more efficient
        //by avoiding redundant retrieval of the BasicFileAttributes.
        try (Stream<Path> stream = Files.find(Paths.get(root), Integer.MAX_VALUE,
                shouldVisit)) {
            stream.forEach(this::handlePath);
        } catch (IOException e) {
            System.err.println(e);
            //return false;
        } catch(UncheckedIOException e) {
            System.err.println("This program does not have permission to read this directory or file");
            System.err.println(e);
        }
    }

    private void handlePath(Path path){
        if(path.toString().endsWith(".zip")){
            try{
                traverseTree(unzipFile(path));
            }catch(IOException e){
                System.err.println(e);
            }
        }else if(path.toString().endsWith(".txt")){
            //insert into producer queue
            final Message msg = new Message(false, path);
            produce(msg);
        }
    }

    private String unzipFile(Path path) throws IOException{
        String fileZip = path.toString();
        Path tDirect = Files.createTempDirectory(path.getParent(), path.getFileName().toString());
        byte[] buffer = new byte[1024];
        try{
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                String fileName = zipEntry.getName();
                File newFile = new File(tDirect + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

        }catch(FileNotFoundException e){
            System.err.println("File does not exist!");
            System.err.println(e);
        }catch(IOException e){
            System.err.println(e);
        }
        toDelete.add(tDirect);
        return tDirect.toString();
    }
}

