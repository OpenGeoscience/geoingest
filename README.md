## Instructions

Clone this repository.

```bash
git clone git@github.com:OpenGeoscience/geoingest.git
cd geoingest
```
### Fill the credential information

Copy the config file.
```sh
cp aws-credentials.mk.example aws-credentials.mk
```
Edit the information in the aws-credentials.mk. That file will be ignored when you push.

### (Optional) Build ingest assembly 
Since we have an assembly on s3 we can skip the assembly part. 

Get Geotrellis as a submodule
```bash
git submodule init
git submodule update
```

```bash
cd geotrellis
./sbt "project spark-etl" assembly
```

Once this succeeds your assembly will be
```bash
geoingest/geotrellis/spark-etl/target/scala-2.11/geotrellis-spark-etl-assembly-1.1.0-SNAPSHOT.jar
```

If you don't have this assembly on s3 you can push it by doing
```bash
aws s3 cp geoingest/geotrellis/spark-etl/target/scala-2.11/geotrellis-spark-etl-assembly-1.1.0-SNAPSHOT.jar s3://my-bucket/
```

### Create Json Specs For Geotrellis

Geotrellis ingest requires 3 json files for specifying the ingest job.
There is a very very small utility that is written for that purpose. 

```bash
mkvirtualenv geoingest
pip install -r requirements.txt
pip install -e .
```

```bash
ingest s3://locationOfS3BucketWithTiffs layerName s3://locationOfCatalog
```

This will create json specifications in the current directory for the ingest job.

Now we need to push those json files to s3 so that geotrellis can read it.
```bash
make copy-json-specs
```

### Create Cluster

At this point it is very easy to launch our cluster. 

```bash
make create-cluster
```

This will take roughly 10 minutes. After that we can ingest our layers.

### Submit the ingest job

To submit the ingest job just do:
```sh
make submit-remote-ingest
```
