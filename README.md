# dapla-exploration-metadata-ingest

Service that ingests metadata into the data-exploration database

To get a picture of this interact with the rest of the services have a look at the [architecture](https://github.com/statisticsnorway/dapla-project/blob/master/doc/architecture.adoc)

## This service gets a json file from pubsub containing: 
**dataset-meta** info about dataset<br>
**dataset-doc** for dataset documentation [example](https://github.com/statisticsnorway/dapla-exploration-metadata-ingest/blob/master/src/test/resources/testdata/template/simple.json) <br> 
**dataset-linage** for linage documentation [example](https://github.com/statisticsnorway/dapla-exploration-metadata-ingest/blob/master/src/test/resources/testdata/lineage/one-level.json) <br>

A template created by [gui](https://github.com/statisticsnorway/dapla-ipython-kernels) in jupyter and [dataset-doc-service](https://github.com/statisticsnorway/dataset-doc-service) is used for creating gsim objects. 
This now connected to lds, but it is easy to add this to another similar solution
Just override *PersistenceProvider* as is now done with *ExplorationLdsHttpProvider*
