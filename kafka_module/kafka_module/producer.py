import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from kafka import KafkaProducer
from kafka.errors import KafkaError, NoBrokersAvailable
import json
import logging
import sys
import os


class KafkaProducerManager:
    def __init__(self, bootstrap_servers='localhost:9092'):
        self.producer = None
        self.bootstrap_servers = bootstrap_servers
        if os.getenv('KAFKA_ENABLED', 'true').lower() == 'false':
            logging.info("Kafka integration disabled via environment variable")
            return
        try:
            self.producer = KafkaProducer(
                bootstrap_servers=bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                retries=5,
                acks='all',
                request_timeout_ms=30000,
                max_block_ms=30000
            )
            logging.info("Kafka producer initialized successfully")
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {bootstrap_servers}: {str(e)}")
            logging.warning("Continuing without Kafka integration")
        except Exception as e:
            logging.error(f"Failed to initialize Kafka producer: {str(e)}")
            logging.warning("Continuing without Kafka integration")

    def send_message(self, topic, message):
        if not self.producer:
            logging.warning(f"Kafka producer not initialized, skipping message to topic {topic}")
            return
        try:
            future = self.producer.send(topic, message)
            future.get(timeout=10)
            logging.info(f"Published message to topic {topic}: {message}")
        except KafkaError as e:
            logging.error(f"Failed to publish message to {topic}: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error publishing message to {topic}: {str(e)}")
            raise

    def flush(self):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping flush")
            return
        try:
            self.producer.flush(timeout=10)
            logging.info("Kafka producer flushed")
        except Exception as e:
            logging.error(f"Error flushing Kafka producer: {str(e)}")

    def close(self):
        if self.producer:
            try:
                self.producer.flush(timeout=30)
                self.producer.close(timeout=30)
                logging.info("Kafka producer closed")
            except Exception as e:
                logging.error(f"Error closing Kafka producer: {str(e)}")