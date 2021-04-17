package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.formats.PopCsv;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.genomeFeature.GenomeFeature;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.henrikroslund.Utils.*;

@Log
public class CandidateFeature extends Stage {

    public static String FEATURE_FILE_ENDING = ".feature";

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
        List<Genome> genomes = loadGenomes(directory.getPath(), Collections.emptyList(), false, false);
        if(genomes.size() != 1) {
            throw new Exception("Did not expect " + genomes.size() + " genomes to be found in folder " + directory.getPath());
        }
        return genomes.get(0);
    }

    private GenomeFeature loadGenomeFeatures(File directory) throws Exception {
        List<File> featureFiles = Utils.getFilesInFolder(directory.getPath(), FEATURE_FILE_ENDING);
        if(featureFiles.size() != 1) {
            throw new Exception("Did not expect " + featureFiles.size() + " genomes to be found in folder " + directory.getPath());
        }
        File genomeFeatureFile = featureFiles.get(0);
        FileUtils.copyFile(genomeFeatureFile, new File(outputInputFolder+"/"+genomeFeatureFile.getName()));
        return new GenomeFeature(genomeFeatureFile);
    }

    private void processFeatures(Genome candidates, Genome mainGenomeWithDuplicates, GenomeFeature genomeFeature) throws Exception {
        PopCsv popCsv = new PopCsv();

        String resultFile = outputFolder+"/"+mainGenomeWithDuplicates.getOutputFilename() + ".csv";
        throwIfFileExists(resultFile);

        for(Sequence candidate : candidates.getSequences()) {
            List<Sequence> matches = mainGenomeWithDuplicates.getSequencesMatchingAnyEvaluator(
                    Collections.singletonList(new IdenticalEvaluator(candidate)));
            boolean foundInReferenceGenome = !matches.isEmpty();
            if(matches.size()  > 1) {
                log.info("Matches:" + matches.size() + " " + candidate.toString());
            }
            // We need to maintain the meta data so add it for all
            for(Sequence sequence : matches) {
                sequence.setMetaData(candidate.getMetaData());
            }

            List<Feature> features = Collections.emptyList();
            if(foundInReferenceGenome) {
                features = genomeFeature.getMatchingFeatures(matches, false);
            } else {
                log.warning("No matches for sequence: " + candidate.toString());
                matches.add(candidate);
            }
            popCsv.addFeatureMatches(matches, features, foundInReferenceGenome);
        }
        popCsv.writeToFile(resultFile);
    }

    @Override
    public String toString() {
        return getName();
    }
}
