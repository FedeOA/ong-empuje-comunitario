import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
import logging

class DonationRequestCancellationHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, org_id, request_id):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping donation request cancellation publishing")
            return
        topic = 'baja_solicitud_donaciones'
        message = {
            'org_id': org_id,
            'request_id': request_id
        }
        self.producer.send_message(topic, message)

    def consume(self, callback):
        from ..consumer import KafkaConsumerManager
        consumer_manager = KafkaConsumerManager(
            topic='baja_solicitud_donaciones',
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id='donation_request_cancellation_consumer'
        )
        def process_message(cancellation_data):
            logging.info(f"Received donation request cancellation: org_id={cancellation_data['org_id']}, request_id={cancellation_data['request_id']}")
            callback(cancellation_data)
        consumer_manager.consume(process_message)