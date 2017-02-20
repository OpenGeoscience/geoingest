get-ingest-jar:
	rm -f $(PWD)/*.jar
	wget https://s3-us-west-2.amazonaws.com/kitware-geotrellis-demo/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar
