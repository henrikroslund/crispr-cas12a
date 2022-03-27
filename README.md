# crispr-cas12a 

This program can be used to analyze genomes for CRISPR sequences and determine best candidates according to specified criteria. The execution of the analysis will performed according to a user defined `Pipeline`. The pipeline consists of any number of `Stages` and in general an output of one stage is used as an input for the next, as a way of continuously filtering the remaining candidates. Stages will typically be configurable with different `Evaluators` to achieve the desired matching. There are also stages which do not provide additional filtering but rather additional analytical data, for example gene feature mapping and serotyping.

As the dataset can be large, this program contains several performance optimizations to utilize memory and cpu cores as efficient as possible. Parallelism is generally achieved by Java Stream API and can be configured accordingly. Despite these optimizations, the user needs to take special care the design the pipeline such that the dataset is reduced as much as possible as early as possible. The different evaluators will have significantly different performance impact.

## Concepts

#### Genome
A genome is typically represented in a .fasta file format. For more information please see https://en.wikipedia.org/wiki/FASTA_format

#### Sequence (target sequence)
A sequence is 24 characters long consisting of only characters {A,T,G,C}. A sequence is split into two parts, `PAM` and `TARGET`.

#### PAM
The PAM is the first 4 characters in a sequence. For a sequence to be considered a CRISPR sequence the PAM must be TTT^T. In some circumstance the critera can be relaxed to be TTTN.

#### Target (spacer)
The Target is the last 20 characters in a sequence.

#### Seed
The Seed is the 6 characters following the PAM.

## Stages
This section describes the different stages available when defining a pipeline. Each stage will have its own input and output folder.
General configuration includes:
* skipDuplicates (true/false) - if true then sequences which are identical will only be processed once.
* mergeChromosomes (true/false) - if true then multiple chromosomes will be merged and treated as one.

### CrisprSelection
This stage is typically first in a pipeline and is used to determine the initial set of CRISPR candidates. It uses the following evaluators:
1. Mandatory `CrisprPamEvaluator` strictMatching: true
2. Optional `NoConsecutiveIdenticalN1N20Evaluator` type: QUADRUPLE
3. Optional `GCContentN1N20Evaluator` range: 8-13

The following configuration is also applied:
* Mandatory mergeChromosomes: true
* Optional skipDuplicates

Input:
- Folder: /reference_sequence
- Content: .fasta

Output:
- Folder: /CrisprSelection
- Content: result.genome

### CrisprCommon
This stage is used to remove candidate sequences where a similar sequence is not found within a set of genomes. A sequence is considered similar if the `PAM` and `Seed` matches and the number of mismatches in N7 to N20 is less than the configured amount. The following evaluators are used:
1. Mandatory `IdenticalEvaluator` checkPam: false, checkSeed: true, checkN7N20: false
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
This stage is used to remove candidate sequences if a similar sequence is found within a set of genomes. A sequence is considered similar if it fulfills all the provided evaluators.
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
This stage is used for a more fine-grained classification of candidate sequences. Each sequence will be evaluated against a set of genomes to determine the number of matches of different types as specific by the `TypeEvaluator`.
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
Input:
- Folder: /feature_mapping
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
* checkPam (true/false)
* checkSeed (true/false)
* checkN7N20 (true/false)

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
