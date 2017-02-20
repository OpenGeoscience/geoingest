S3_URI := s3://kitware-geotrellis-demo

get-ingest-jar:
	rm -rf $(PWD)/jar
	mkdir $(PWD)/jar
	wget https://s3-us-west-2.amazonaws.com/kitware-geotrellis-demo/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar -O $(PWD)/jar/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar

create-json-specs:
	ingest $(PWD)/data TemporalMultibandIngest local

create-remote-json-specs:
	ingest $(PWD)/data TemporalMultibandIngest remote

submit-ingest:
	spark-submit --class geotrellis.spark.etl.TemporalMultibandIngest --master 'local[*]' --driver-memory 10G $(PWD)/jar/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar --input $(PWD)/json/input.json --output $(PWD)/json/output.json --backend-profiles $(PWD)/json/backend-profiles.json

copy-json-specs:
	@aws s3 cp json/backend-profiles.json ${S3_URI}/
	@aws s3 cp json/input.json ${S3_URI}/
	@aws s3 cp json/output.json ${S3_URI}/output.json
