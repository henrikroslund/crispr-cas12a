# crispr-cas12a 

This program can be used to analyze genomes for CRISPR sequences and determine best suited candidates according to specified criteria. The execution of the analysis will performed according to a user defined `Pipeline`. A pipeline consists of any number of `Stages` and in general the output of one stage is used as input for the next, as a way of continuously filtering the remaining candidates. Stages will typically be configurable with different `Evaluators` to achieve the desired matching. There are also stages which do not provide additional filtering but rather additional analytical data, for example gene feature mapping and serotyping.

As the dataset can be large, this program contains several performance optimizations to utilize memory and cpu cores as efficient as possible. Parallelism is generally achieved by Java Stream API and can be configured accordingly. Despite these optimizations, the user needs to take special care the design the pipeline such that the dataset is reduced as much as possible as early as possible. The different evaluators will have significantly different performance impact.

## Genome

## Sequence

## Stages
This section describes the different stages available when defining a pipeline. Each stage will have its own input and output folder. Please see respective stage's javadoc for further details on each stage and possible configuration.

### CrisprSelection
This stage is typically first in a pipeline and is used to determine the initial set of CRISPR candidates.

### CrisprCommon

### CrisprElimination

### CandidateTyping

### CandidateFeature

### CoverageAnalysis

### Serotyping