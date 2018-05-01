.DEFAULT_GOAL := setup

.PHONY: build bundle-app compile-app app-server api-server app-console \
	api-console assembly compile server setup restart ingest-assembly ingest \
	compile-ingest download-tif

build:
	docker-compose -f docker-compose.yml run --rm --no-deps app yarn

bundle-app:
	bash -c "trap 'cd ..' EXIT; cd app; npm install; npm run bundle"

compile-api:
	bash -c "trap 'cd ..' EXIT; cd api; sbt compile"

app-server:
	docker-compose up -d app

api-server:
	bash -c "trap 'cd ..' EXIT; cd api; spark-submit --name \"NLCD API\" \
	--master yarn-client --driver-memory 5G --driver-cores 4 --executor-cores 2 \
        --executor-memory 5G --conf spark.dynamicAllocation.enabled=true \
	target/scala-2.11/geotrellis_collections_api-assembly-1.0.jar"

app-console:
	docker-compose exec app /bin/bash

api-console:
	bash -c "trap 'cd ..' EXIT; cd api; sbt console"

assembly:
	bash -c "trap 'cd ..' EXIT; cd api; sbt assembly"

compile: bundle-app compile-api compile-ingest

restart: api-server

server: app-server api-server

setup: build ingest

download-tif:
	curl -o nlcd_pa.tif \
	https://azavea-research-public-data.s3.amazonaws.com/geotrellis/samples/nlcd_pa.tif;
	hdfs dfs -mkdir /tmp/land-cover-data;
	hdfs dfs -mkdir /tmp/land-cover-data/catalog;
	hdfs dfs -put nlcd_pa.tif /tmp/

ingest-assembly:
	bash -c "trap 'cd ..' EXIT; cd ingest; sbt assembly"

compile-ingest:
	bash -c "trap 'cd ..' EXIT; cd ingest; sbt compile"

ingest: ingest-assembly download-tif
	bash -c "trap 'cd ..' EXIT; cd ingest; spark-submit --name \"NLCDPA Ingest\" \
	--master yarn --driver-memory 4G \
	target/scala-2.11/geotrellis_collections_api_ingest-assembly-1.0.jar"
