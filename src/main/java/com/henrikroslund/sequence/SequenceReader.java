package com.henrikroslund.sequence;

import com.henrikroslund.formats.JakeCsv;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class SequenceReader {

    public static List<Sequence> SequenceReader(File file) throws IOException {
        List<Sequence> sequences = Collections.synchronizedList(new ArrayList<>());
        Path filePath = Path.of(file.getAbsolutePath());
        Stream<String> lines = Files.lines(filePath);
        lines.parallel().forEach(line -> {
            sequences.add(new Sequence(line, 0, file.getName()));
        });
        return sequences;
    }

    public static JakeCsv JakeCsvReader(File file) throws IOException, CsvException {
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);
        List<String[]> list = csvReader.readAll();
        fileReader.close();
        csvReader.close();
        return new JakeCsv(list);
    }
}
