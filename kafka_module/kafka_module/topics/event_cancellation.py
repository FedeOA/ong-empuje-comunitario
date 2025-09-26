import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
import logging

class EventCancellationHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, org_id, event_id):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping event cancellation publishing")
            return
        topic = 'baja_evento_solidario'
        message = {
            'org_id': org_id,
            'event_id': event_id
        }
        self.producer.send_message(topic, message)

    def consume(self, callback):
        from ..consumer import KafkaConsumerManager
        consumer_manager = KafkaConsumerManager(
            topic='baja_evento_solidario',
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id='event_cancellation_consumer'
        )
        def process_message(cancellation_data):
            logging.info(f"Received event cancellation: org_id={cancellation_data['org_id']}, event_id={cancellation_data['event_id']}")
            callback(cancellation_data)
        consumer_manager.consume(process_message)