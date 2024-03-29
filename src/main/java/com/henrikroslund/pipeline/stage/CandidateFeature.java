package com.henrikroslund.pipeline.stage;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.formats.CandidateFeatureResultCsv;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.genomeFeature.GenomeFeature;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.henrikroslund.Utils.*;

/**
 * This stage will analyze input genome sequences with gene feature files located in the input folder.
 * The results are saved to a csv file.
 */

@Log
public class CandidateFeature extends Stage {

    private static final String FEATURE_FILE_ENDING = ".feature";

    public CandidateFeature() {
        super(CandidateFeature.class);
    }

    @Override
    protected String getStageFolder() {
        return "/feature_mapping";
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<File> directories = getFolders(inputFolder);
        for(File directory : directories) {
            Genome referenceGenome = loadGenome(directory);
            GenomeFeature referenceGenomeFeature = loadGenomeFeatures(directory);
            processFeatures(inputGenome, referenceGenome, referenceGenomeFeature);
        }
        return null;
    }

    private Genome loadGenome(File directory) throws Exception {
        List<Genome> genomes = loadGenomesInFolder(directory.getPath(), Collections.emptyList(), false, false);
        if(genomes.size() != 1) {
            throw new Exception("Did not expect " + genomes.size() + " genomes to be found in folder " + directory.getPath());
        }
        return genomes.get(0);
    }

    private GenomeFeature loadGenomeFeatures(File directory) throws Exception {
        List<File> featureFiles = Utils.getFilesInFolder(directory.getPath(), FEATURE_FILE_ENDING);
        if(featureFiles.size() != 1) {
            throw new Exception("Did not expect " + featureFiles.size() + " feature files in folder " + directory.getPath());
        }
        return new GenomeFeature(featureFiles.get(0));
    }

    private void processFeatures(Genome candidates, Genome mainGenomeWithDuplicates, GenomeFeature genomeFeature) throws Exception {
        CandidateFeatureResultCsv candidateFeatureResultCsv = new CandidateFeatureResultCsv();

        String resultFile = outputFolder+"/"+mainGenomeWithDuplicates.getFilename()+".csv";
        throwIfFileExists(resultFile);

        for(Sequence candidate : candidates.getSequences()) {
            List<Sequence> matches = mainGenomeWithDuplicates.getSequencesMatchingAnyEvaluator(
                    Collections.singletonList(new IdenticalEvaluator(candidate)));
            boolean foundInReferenceGenome = !matches.isEmpty();
            if(matches.size()  > 1) {
                log.info("Multiple matches found:" + matches.size() + " " + candidate.toString());
            }

            // If we did not find it in the reference genome we should 0 the index since location does not make sense
            if(!foundInReferenceGenome) {
                matches.add(new Sequence(candidate.getRaw(), 0, candidate.getGenome(), candidate.getIsComplement()));
            }

            // We need to maintain the meta data so add it for all
            for(Sequence sequence : matches) {
                sequence.setMetaData(candidate.getMetaData());
            }

            List<Feature> features = Collections.emptyList();
            if(foundInReferenceGenome) {
                features = genomeFeature.getMatchingFeatures(matches, false);
            } else {
                log.warning("No matches for sequence: " + candidate);
            }
            candidateFeatureResultCsv.addFeatureMatches(matches, features, foundInReferenceGenome);
        }
        candidateFeatureResultCsv.writeToFile(resultFile);
    }

    @Override
    public String toString() {
        return getName() + " " + getStageFolder();
    }
}
