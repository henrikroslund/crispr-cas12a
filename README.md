# crispr-cas12a 

This program is used for searching genomes for species-specific CRISPR-Cas12a target sites. Its application is CRISPR diagnostics where specific detection of a given species is desired. The program is optimized for Lachnospiraceae bacterium CRISPR-Cas12a system but can be configured for other CRISPR-Cas systems as well. The pipeline consists of a number of Stages that are typically configurable with different Evaluators to achieve desirable criteria. An output of one stage is used as an input of the next, as a way of continuously filtering the remaining target site candidates.

First, the program generates a pool of possible CRISPR-Cas12a target sites from a given genome of a species of interest (`CrisprSelection`). It then finds common target sites found in other genomes of the same species (`CrisprCommon`). These common target sites are then filtered through genomes of other species in order to remove any cross-reactions (`CrisprElimination` & `CandidateTyping`). Finally, remaining candidate are annotated with known features (`CandidateFeature`).

Genome assembly and feature table (annotation) can be downloaded from NCBI Assembly portal (https://www.ncbi.nlm.nih.gov/assembly)


As the dataset can be large, this program contains several performance optimizations to utilize memory and cpu cores as efficient as possible. Parallelism is generally achieved by Java Stream API and can be configured accordingly. Despite these optimizations, the user needs to take special care the design the pipeline such that the dataset is reduced as much as possible as early as possible. The different evaluators will have significantly different performance impact.

## Definitions

#### Genome
A genome is typically represented in a .fasta file format. For more information please see https://en.wikipedia.org/wiki/FASTA_format

#### Target site (Sequence)
A sequence is 24 characters long consisting of only characters {A,T,G,C}. A sequence is split into two parts, `PAM` and `TARGET`.

#### PAM
The PAM is the first 4 characters in a sequence. Optimal PAM sequence of Lachnospiraceae bacterium CRISPR-Cas12a is TTT^T. In some circumstance the criteria can be relaxed to be TTTN.

#### Target (spacer)
The Target is the last 20 characters in a sequence.

#### Seed
The Seed is the first 6 characters following the PAM; it is a part of the Target (spacer).

## Stages
This section describes the different stages available when defining a pipeline. Each stage will have its own input and output folder.
General configuration includes:
* skipDuplicates (true/false) - if true then sequences which are identical will only be processed once.
* mergeChromosomes (true/false) - For chromosomes on multiple fasta files, true must be selected in order to merged them and treated as one.

### CrisprSelection
This stage is typically first in a pipeline and is used to generate an initial set of CRISPR target site candidates. It uses the following evaluators:
1. Mandatory `CrisprPamEvaluator` strictMatching: true
2. Optional `NoConsecutiveIdenticalN1N20Evaluator` type: QUADRUPLE
3. Optional `GCContentN1N20Evaluator` range: 8-13

The following configuration is also applied:
* Mandatory mergeChromosomes: true
* Optional skipDuplicates

Input:
- Folder: /reference_sequence
- Content: .fasta or .genome

Output:
- Folder: /CrisprSelection
- Content: result.genome

### CrisprCommon
This stage is used to remove candidate sequences that are not found 100% identical in all other genomes of the same species. A sequence is considered identical if the PAM and Seed matches and the number of mismatches in N7 to N20 is less than the configured amount (i.e. 0). The following evaluators are used:

1. Mandatory `IdenticalEvaluator` checkPam: false, checkSeed: true, checkN7N20: false
   1. The PAM is implicitly checked already when reading genomes since the CrisprPamEvaluator is used as described below.
2. Mandatory `MismatchEvaluator` mismatches: Range(0-X), indexesToCompare: Range(N7-N20)

The following configuration is also applied when reading the genomes:
* Mandatory skipDuplicates: true
* Mandatory `CrisprPamEvaluator` strictMatching: true

Input:
- Folder: /strains
- Content: .fasta

Output:
- Folder: /CrisprCommon
- Content: result.genome

### CrisprElimination
This stage is used to remove candidate sequences if a similar CRISPR sequence is found within genomes of other cross reactive species. First, all possible CRISPR-Cas12a target sites are generated from these genomes. These sequences are then aligned with the candidates derived from the CrisprCommon stage. Two sequences are considered similar if they fulfills all the provided evaluators.

1. Mandatory list of `Evaluator`

The following configuration is also applied when reading the genomes:
* Mandatory skipDuplicates: true
* Mandatory mergeChromosomes: true
* Mandatory `CrisprPamEvaluator` strictMatching: false

Input:
- Folder: /cross_reactive_pathogens
- Content: .fasta

Output:
- Folder: /CrisprElimination
- Content: result.genome

### CandidateTyping
This stage is used for a more fine-grained classification of candidate sequences. Candidates that do not fulfill classification will also be removed. Each sequence will be evaluated against a set of genomes to determine the number of matches of different types as specific by the `TypeEvaluator`.
* Mandatory sampleSetCriteria a list of `Evaluator` which is used to determine eligible sequences in the genomes to be used for typing
* Mandatory bindCriteria an `Evaluator` used to determine if two sequences are considered binding to each other
* Mandatory `TypeEvaluator`

The following configuration is also applied:
* Optional saveSurroundingSequences - if true the output will also contain a list of sequences adjacent to any binding sequence

Input:
- Folder: /cross_reactive_pathogens
- Content: .fasta

Output:
- Folder: /CandidateTyping
- Content: result.genome

### CandidateFeature
This final stage is to annotate the final candidates with data for a reference genome in order to determine their chromosome, coordination, strand, function and copy number.

Input:
- Folder: /feature_mapping
- Subfolders: one per reference
- Content: .fasta
- Content: .feature

Output:
- Folder: /CandidateFeature
- Content: result.genome

## Evaluators

### CrisprPamEvaluator
Evaluates if a sequence is a CRISPR sequence by matching the PAM according to configuration:
* TTT^T when strictMatching: true
* TTTN when strictMatching: false

### NoConsecutiveIdenticalN1N20Evaluator
Evaluates if the `target` part of a sequence does not contain the same character repeated consecutively according to configuration:
* TRIPLE
* QUADRUPLE

### GCContentN1N20Evaluator
Evaluates if the `target` part of a sequence contains the number of G & C instances according to configuration:
* Range(min, max) - default (9,11)

### IdenticalEvaluator
High efficiency evaluator to determine if two sequences are identical according to the following configuration:
* checkPam (true/false) - if true then the PAM will be included in the evaluation
* checkSeed (true/false) - if true then the Seed will be included in the evaluation
* checkN7N20 (true/false) - if true then the character positions 7 to 20 will be included in the evaluation

If checkPam, checkSeed & checkN7N20 are all true then the full sequence will be used in the evaluation.

### MismatchEvaluator
Evaluates if the number of mismatches between two sequences is within the desired range for a configured part of the sequence:
* mismatches: Range(min,max)
* indexes: List of Range(start, end)

### TypeEvaluator
Evaluates two sequences according to the following types:
* TYPE_1: >=X mismatches in PAM's first 3
* TYPE_2: >=X consecutive mismatches in Seed
* TYPE_3: If Type_1 & Type_2
* TYPE_4: Did not bind in genome
* TYPE_5: >=X mismatches in N7-N20
* TYPE_6: >=X mismatches in Seed
* TYPE_DISCARD: If no other type applies

## File formats
### .genome
Contains a list of sequences according to the following format:

SEQUENCE | STRAND | INDEX | NAME | TYPES

### .feature
Same format as feature table which can be downloaded from https://www.ncbi.nlm.nih.gov/assembly. The file extension needs to be changed from .txt to .feature.

## How to run the program

There is no provided release jar for the user to run but the user needs to compile the program from source code. It is recommended to use an IDE such as Intellij to edit, build and run the program. The steps below assume you are using Intellij.

1. Install required programs as describe in section below
2. Clone git repository (`git clone https://github.com/henrikroslund/crispr-cas12a.git`)
3. Open project .pom file in Intellij as a project
4. Update defaultPipeline method in Main.java to your desired pipeline configuration. See examples in the same file.
5. Create input folder at ../crispr-cas12a-input/your-project from source code folder. Depending on the stages you have configured for you pipeline then populate the input folders as described for each stage.
6. Configure run target CrisprCas12 and set the PIPELINE_INPUT=your-project
7. Run CrisprCas12 target
8. After pipeline is finished the output will be located at ../crispr-cas12a-output from the source code folder in a folder named after the time at which the program was started. For example: "2022-03-27 021311 AM PDT".

### Run BP pipeline

For convenience there is an intellij run target called `CrisprCas12 BP` which will run the pipeline configuration as described in the paper "Insert reference here". In order to run the pipeline the following folders needs to be populated with the correct data. This is also described in the paper.

* ../crispr-cas12a-input/bp/reference_sequence
* ../crispr-cas12a-input/bp/strains
* ../crispr-cas12a-input/bp/cross_reactive_pathogens
* ../crispr-cas12a-input/bp/feature_mapping

### System requirements
* Minimum 16gb system memory
* Intellij (https://www.jetbrains.com/idea)
* OpenJDK 17 (https://formulae.brew.sh/formula/openjdk)
* Maven v3.8.4 (https://maven.apache.org/)
* Git (https://formulae.brew.sh/formula/git)