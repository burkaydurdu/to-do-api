FROM clojure:openjdk-13-lein-2.9.1

RUN apt-get update && apt-get install -y curl
RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app
COPY project.clj /usr/src/app/

EXPOSE 3011

RUN lein deps
COPY . /usr/src/app

RUN mkdir -p /src/
WORKDIR /src
COPY . /src/

RUN lein uberjar

ENTRYPOINT exec java -jar \
                     -server \
                     -Xmx4g \
                     -XX:+UseConcMarkSweepGC \
                     -XX:+CMSParallelRemarkEnabled \
                     -XX:+UseCMSInitiatingOccupancyOnly \
                     -XX:CMSInitiatingOccupancyFraction=70 \
                     -XX:+ScavengeBeforeFullGC \
                     -XX:+CMSScavengeBeforeRemark \
                     -Djava.net.preferIPv4Stack=true \
                     /src/target/todo_back.jar
