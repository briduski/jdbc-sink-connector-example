FROM confluentinc/cp-kafka-connect:latest

RUN \
  echo "PRE-INSTALLING CONNECTORS..." && \
  echo "===> Installing Kafka JDBC Connector ..." && \
  confluent-hub install confluentinc/kafka-connect-jdbc:10.0.1  --no-prompt
