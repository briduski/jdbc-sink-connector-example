# jdbc-sink-connector-example
Example of how to run Kafka [Kafka Connect JDBC Connector](https://github.com/confluentinc/kafka-connect-jdbc)  


1. Run in the directory /docker-compose:  docker-compose up -d

2. Start SimpleJdbcSinkv1 sink connector:

curl -X POST -s --header "Content-Type: application/json" --data @../connector-config/simple-jdbc.json http://localhost:8083/connectors

At this point, the connector is listening' to topic 'event1-topic'

3. Verify the connector is up&running
- curl -s "http://localhost:8083/connectors?expand=info&expand=status" | jq '. | to_entries[] | [ .value.info.type, .key, .value.status.connector.state,.value.status.tasks[].state,.value.info.config."connector.class"]|join(":|:")' | column -s : -t| sed 's/\"//g'| sort
- curl -s http://localhost:8083/connectors | jq
- See config: curl -s http://localhost:8083/connectors/SimpleJdbcSinkv1 | jq


5. Run the producer under project Event1Producer:
**Event1Producer/src/main/java/bri/kafka/producer/ProducerAvro1.java** 

> > >
Producer run
 Bye!
 Producer => {partition=0, offset=0, topic=event1-topic, value={"id": "i", "count": 5, "timestamp": 1612699197456}, key=5, timestamp=1612699197646}
 Total records sent: 1
> > >  

6. Check Topic 'event1-topic' using kafkacat

kafkacat -C -b localhost:29092 -t event1-topic -s key=s -s value=avro -r http://localhost:8081

7. Check the postgres database to see the record inserted into a table

![Screeshot](images/TablePlus-record.jpg) 

8. Destroy container: docker-compose down


##  SimpleJdbcSinkv1
    kafka topic: event1-topic  --> SimpleJdbcSinkv1 --> Postgress DB
    
   ```
    Connector configuration - SimpleJdbcSinkv1 => file: ./connector-config/simple-jdbc.json:  
   
      1.- Record value => Avro converter
     "value.converter": "io.confluent.connect.avro.AvroConverter",
     "value.converter.schema.registry.url": "http://schema-registry:8081",
   
      2.- Primary key
        "pk.mode":"kafka",
       "pk.fields": "__connect_topic,__connect_partition,__connect_offset",
          
      3.- Fields from avro record to insert in the database 
        "fields.whitelist": "id,count,timestamp",      
      
   ```