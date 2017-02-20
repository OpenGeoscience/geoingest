get-ingest-jar:
	rm -rf $(PWD)/jar
	mkdir $(PWD)/jar
	wget https://s3-us-west-2.amazonaws.com/kitware-geotrellis-demo/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar -O $(PWD)/jar/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar

create-json-specs:
	ingest $(PWD)/data TemporalMultibandIngest

submit-ingest:
	spark-submit --class geotrellis.spark.etl.TemporalMultibandIngest --master 'local[*]' --driver-memory 10G $(PWD)/jar/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar --input $(PWD)/json/input.json --output $(PWD)/json/output.json --backend-profiles $(PWD)/json/backend-profiles.json
