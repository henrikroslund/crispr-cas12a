package com.henrikroslund.sequence;

import com.henrikroslund.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class SequenceReader {

    public static List<Sequence> getSequencesFromFile(File file) throws IOException {
        List<Sequence> sequences = Collections.synchronizedList(new ArrayList<>());
        Path filePath = Path.of(file.getAbsolutePath());
        Stream<String> lines = Files.lines(filePath);
        String genome = Utils.getStringWithoutWhitespaces(file.getName());
        lines.parallel().forEach(line -> sequences.add(new Sequence(line, 0, genome)));
        return sequences;
    }
}
