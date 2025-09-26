import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from kafka import KafkaConsumer
import json
import logging

class KafkaConsumerManager:
    def __init__(self, topic, bootstrap_servers, group_id, enable_auto_commit=True):
        self.topic = topic
        self.consumer = KafkaConsumer(
            topic,
            bootstrap_servers=bootstrap_servers,
            group_id=group_id,
            enable_auto_commit=enable_auto_commit,
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        logging.info(f"Initialized KafkaConsumer for topic: {topic}, group_id: {group_id}")

    def consume(self, process_message):
        try:
            # Verify subscription
            logging.info(f"Subscribed to topic: {self.topic}, partitions: {self.consumer.partitions_for_topic(self.topic)}")
            for message in self.consumer:
                try:
                    logging.info(f"Received message from topic {self.topic}: {message.value}")
                    process_message(message.value)
                except Exception as e:
                    logging.error(f"Failed to process message: {str(e)}")
                    raise
        except Exception as e:
            logging.error(f"Consumer error for topic {self.topic}: {str(e)}")
            raise

    def close(self):
        try:
            self.consumer.close()
            logging.info(f"Closed Kafka consumer for topic {self.topic}")
        except Exception as e:
            logging.error(f"Failed to close Kafka consumer: {str(e)}")