package bri.kafka.producer;


import bri.avro.kafka.dto.Event1;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProducerAvro1  implements  Runnable{
    public static final String BROKER = "localhost:29092";
    public static final String SCHEMA_REGISTRY = "http://localhost:8081";
    public static final String TOPIC = "event1-topic";
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String FULL_ALPHABET = ALPHABET + ALPHABET.toLowerCase() + "123456789";

    Producer<Integer, Event1> producer;
    char  exit=0;

    public ProducerAvro1( ) {
        this.producer   = new KafkaProducer<>(getProperties());
    }
    public ProducerAvro1(Producer<Integer, Event1> producer) {
        this.producer = producer;
    }
    public void finish(){
        System.out.println("Bye!");
        exit = 'y';
    }
    public static void main(String[] args) {
       ProducerAvro1 avroProducer = new ProducerAvro1();
        Thread t1 = new Thread(avroProducer);
        t1.start();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       avroProducer.finish();
    }
    public static void main5(String[] args) {
        int noOfProducers =1;
        ExecutorService executor = Executors.newFixedThreadPool(noOfProducers);
        final List<ProducerAvro1> runnableProducers = new ArrayList<>();
        for (int i = 0; i < noOfProducers; i++) {
            ProducerAvro1 runnableProducer = new ProducerAvro1();
            runnableProducers.add(runnableProducer);
            executor.submit(runnableProducer);
        }
        System.out.println("Started producer ... ");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ProducerAvro1 p : runnableProducers) p.finish();
            executor.shutdown();
            System.out.println("Closing Executor Service");
            try {
                executor.awaitTermination(2000 * 2, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }
    @Override
    public void run(){
        int count=0;
        System.out.println("Producer run");
        while (exit!='y'){
            int key= new Random().nextInt(10 );
             Event1  event1= generateAvro(key);
           // String event1 = "";
            send(key, event1);
            count++;
        }
        producer.close();
        System.out.println("Total records sent: "+count);
    }

    public  void send(int key, Event1 event1){
        ProducerRecord<Integer, Event1> record = new ProducerRecord<>(TOPIC, key, event1);
        producer.send(
                record,
                ((metadata, exception) -> {
                    if (exception == null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("key", key);
                        data.put("value", event1.toString());
                        data.put("topic", metadata.topic());
                        data.put("partition", metadata.partition());
                        data.put("offset", metadata.offset());
                        data.put("timestamp", metadata.timestamp());
                        System.out.println("Producer => " + data.toString());
                    } else {
                        exception.printStackTrace();
                    }
                })
        );
    }

     public static Event1 generateAvro(int key) {
        Random random = new Random();
      String val = Character.toString(FULL_ALPHABET.charAt(random.nextInt(FULL_ALPHABET.length())));
         Event1 event1 = new Event1();
         event1.setCount(key);
         event1.setId(val);
         event1.setTimestamp(System.currentTimeMillis());
         return  event1;
    }
    static Properties getProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-string");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY);
        return props;
    }
}
