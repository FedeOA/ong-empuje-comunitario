import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
import logging

class DonationEventsHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, action, donation_data):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping donation event publishing")
            return
        topic = 'donation_events'
        message = {
            'service': 'donation',
            'action': action,
            'data': donation_data
        }
        self.producer.send_message(topic, message)

    def consume(self, callback, group_id='donation_events_consumer', topic='donation_events'):
        from ..consumer import KafkaConsumerManager
        logging.info(f"Starting consumer for topic: {topic}, group_id: {group_id}")
        consumer_manager = KafkaConsumerManager(
            topic=topic,
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id=group_id,
            enable_auto_commit=False
        )
        logging.info(f"Consumer assigned partitions: {consumer_manager.consumer.assignment()}")
        def process_message(data):
            logging.info(f"Processing message: {data}")
            callback(data)
            consumer_manager.consumer.commit()
            logging.info("Message committed")
        try:
            consumer_manager.consume(process_message)
        except Exception as e:
            logging.error(f"Error in consumer: {str(e)}")
            raise
        finally:
            consumer_manager.close()
            logging.info(f"Closed consumer for topic: {topic}")