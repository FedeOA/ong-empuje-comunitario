import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
from datetime import datetime
import logging

class SolidarityEventHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, org_id, event_id, name, description, date_time):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping solidarity event publishing")
            return
        event_datetime = datetime.strptime(date_time, '%Y-%m-%d %H:%M:%S')
        current_datetime = datetime.now()
        if event_datetime <= current_datetime:
            raise ValueError("Event date and time must be in the future")
        topic = 'eventos_solidarios'
        message = {
            'org_id': org_id,
            'event_id': event_id,
            'name': name,
            'description': description,
            'date_time': date_time
        }
        self.producer.send_message(topic, message)

    def consume(self, own_org_id, callback):
        from ..consumer import KafkaConsumerManager
        consumer_manager = KafkaConsumerManager(
            topic='eventos_solidarios',
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id='solidarity_event_consumer'
        )
        def process_message(event_data):
            if event_data['org_id'] == own_org_id:
                logging.info(f"Skipped event from own organization: org_id={own_org_id}, event_id={event_data['event_id']}")
                return
            event_datetime = datetime.strptime(event_data['date_time'], '%Y-%m-%d %H:%M:%S')
            current_datetime = datetime.now()
            if event_datetime <= current_datetime:
                logging.info(f"Skipped past event: event_id={event_data['event_id']}, name={event_data['name']}")
                return
            callback(event_data)
        consumer_manager.consume(process_message)