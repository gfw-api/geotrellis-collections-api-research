.DEFAULT_GOAL := setup

.PHONY: api-console api-assembly compile server setup ingest-assembly ingest \
	compile-ingest download-tif

compile-api:
	bash -c "trap 'cd ..' EXIT; cd api; sbt compile"

server:
	bash -c "trap 'cd ..' EXIT; cd api; spark-submit --name \"GLAD Alerts API\" \
	--master yarn-client --driver-memory 16G --driver-cores 4 --executor-cores 2 \
        --executor-memory 5G --conf spark.dynamicAllocation.enabled=true \
	target/scala-2.11/geotrellis_collections_api-assembly-1.0.jar"

api-console:
	bash -c "trap 'cd ..' EXIT; cd api; sbt console"

api-assembly:
	bash -c "trap 'cd ..' EXIT; cd api; sbt assembly"

compile: compile-api compile-ingest

setup: ingest api-assembly

download-tif:
	curl -o clip.tif \
	http://s3.amazonaws.com/gfw2-data/alerts-tsv/temp/example-glad-dataset/clip.tif;
	hdfs dfs -mkdir /data;
	hdfs dfs -mkdir /data/glad;
	hdfs dfs -put clip.tif /data/

ingest-assembly:
	bash -c "trap 'cd ..' EXIT; cd ingest; sbt assembly"

compile-ingest:
	bash -c "trap 'cd ..' EXIT; cd ingest; sbt compile"

ingest: ingest-assembly download-tif
	bash -c "trap 'cd ..' EXIT; cd ingest; spark-submit --name \"GLAD ingest\" \
	--master yarn --driver-memory 4G \
	target/scala-2.11/geotrellis_collections_api_ingest-assembly-1.0.jar"
