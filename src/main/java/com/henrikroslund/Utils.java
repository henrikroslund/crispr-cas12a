package com.henrikroslund;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Utils {

    public static String getFirstRow(String filename) throws Exception {
        Path filePath = Path.of(filename);
        Stream<String> lines = Files.lines(filePath);
        return lines.findFirst().get() + "\n";
    }

    public static List<File> getFilesInFolder(String folder, String suffix) throws Exception {
        File directoryPath = new File(folder);
        List<File> results = new ArrayList();
        for(File file: directoryPath.listFiles()) {
            if(file.isFile()) {
                if(file.getName().endsWith(suffix)) {
                    results.add(file);
                }
            } else if(file.isDirectory()) {
                results.addAll(getFilesInFolder(file.getPath(), suffix));
            } else {
                throw new Exception("Something whent wrong when reading all files");
            }
        }
        return results;
    }

}
