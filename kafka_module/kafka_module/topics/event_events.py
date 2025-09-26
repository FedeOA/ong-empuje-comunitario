import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
import logging

class EventEventsHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, action, event_data):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping event publishing")
            return
        topic = 'event_events'
        message = {
            'service': 'event',
            'action': action,
            'data': event_data
        }
        self.producer.send_message(topic, message)

    def consume(self, callback):
        from ..consumer import KafkaConsumerManager
        consumer_manager = KafkaConsumerManager(
            topic='event_events',
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id='event_events_consumer'
        )
        def process_message(data):
            logging.info(f"Processing event: action={data['action']}, data={data['data']}")
            callback(data)
        consumer_manager.consume(process_message)