S3_URI := s3://kitware-geotrellis-demo
SUBNET_ID := subnet-9ce499eb
EC2_KEY := doruk-NEX
SECURITY_GROUP := sg-4c811928
BACKEND := accumulo

MASTER_INSTANCE := m3.xlarge
WORKER_INSTANCE := m3.2xlarge
WORKER_COUNT := 4

DRIVER_MEMORY := 12000M
DRIVER_CORES := 4
EXECUTOR_MEMORY := 12000M
EXECUTOR_CORES := 4
YARN_OVERHEAD := 1400


ifndef CLUSTER_ID
CLUSTER_ID=$(shell if [ -e "cluster-id.txt" ]; then cat cluster-id.txt; fi)
endif

get-ingest-jar:
	rm -rf $(PWD)/jar
	mkdir $(PWD)/jar
	wget https://s3-us-west-2.amazonaws.com/kitware-geotrellis-demo/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar -O $(PWD)/jar/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar

create-json-specs:
	ingest $(PWD)/data SinglebandIngest local

create-remote-json-specs:
	ingest $(PWD)/data SinglebandIngest remote

submit-ingest:
	spark-submit --class geotrellis.spark.etl.SinglebandIngest --master 'local[*]' --driver-memory 10G $(PWD)/jar/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar --input $(PWD)/json/input.json --output $(PWD)/json/output.json --backend-profiles $(PWD)/json/backend-profiles.json

copy-json-specs:
	@aws s3 cp json/backend-profiles.json ${S3_URI}/
	@aws s3 cp json/input.json ${S3_URI}/
	@aws s3 cp json/output.json ${S3_URI}/output.json

create-cluster:
	aws emr create-cluster --name Kitware-Geotrellis-Cluster \
--release-label emr-5.0.0 \
--output text \
--use-default-roles \
--configurations "file://$(CURDIR)/conf/configurations.json" \
--log-uri ${S3_URI}/logs \
--ec2-attributes KeyName=${EC2_KEY},SubnetId=${SUBNET_ID},EmrManagedMasterSecurityGroup=${SECURITY_GROUP},EmrManagedSlaveSecurityGroup=${SECURITY_GROUP} \
--applications Name=Ganglia Name=Hadoop Name=Hue Name=Spark Name=Zeppelin \
--instance-groups \
'Name=Master,InstanceCount=1,InstanceGroupType=MASTER,InstanceType=${MASTER_INSTANCE},EbsConfiguration={EbsOptimized=true,EbsBlockDeviceConfigs=[{VolumeSpecification={VolumeType=io1,SizeInGB=500,Iops=5000},VolumesPerInstance=1}]}' \
'Name=Workers,InstanceCount=${WORKER_COUNT},InstanceGroupType=CORE,InstanceType=${WORKER_INSTANCE},EbsConfiguration={EbsOptimized=true,EbsBlockDeviceConfigs=[{VolumeSpecification={VolumeType=io1,SizeInGB=500,Iops=5000},VolumesPerInstance=1}]}' \
--bootstrap-actions \
Name=BootstrapGeoWave,Path=${S3_URI}/bootstrap-geowave.sh \
Name=BootstrapDemo,Path=${S3_URI}/bootstrap-demo.sh,\
Args=[--tsj=${S3_URI}/server-assembly-0.1.0.jar,--site=${S3_URI}/site.tgz,--s3u=${S3_URI},--backend=${BACKEND}] \
| tee cluster-id.txt

ssh:
	aws emr ssh --cluster-id ${CLUSTER_ID} --key-pair-file "${HOME}/${EC2_KEY}.pem"

submit-remote-ingest:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name=Ingest,Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,geotrellis.spark.etl.SinglebandIngest,\
--driver-memory,${DRIVER_MEMORY},\
--driver-cores,${DRIVER_CORES},\
--executor-memory,${EXECUTOR_MEMORY},\
--executor-cores,${EXECUTOR_CORES},\
--conf,spark.dynamicAllocation.enabled=true,\
--conf,spark.yarn.executor.memoryOverhead=${YARN_OVERHEAD},\
--conf,spark.yarn.driver.memoryOverhead=${YARN_OVERHEAD},\
${S3_URI}/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar,\
--input,${S3_URI}/input.json,\
--output,${S3_URI}/output.json,\
--backend-profiles,${S3_URI}/backend-profiles.json\
] | cut -f2 | tee last-step-id.txt

proxy:
	aws emr socks --cluster-id ${CLUSTER_ID} --key-pair-file "${HOME}/${EC2_KEY}.pem"

terminate-cluster:
	aws emr terminate-clusters --cluster-ids ${CLUSTER_ID}
	rm -f cluster-id.txt
	rm -f last-step-id.txt
