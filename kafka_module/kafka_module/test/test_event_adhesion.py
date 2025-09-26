import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
import json
import time
import uuid
from datetime import datetime, timedelta
from kafka import KafkaConsumer
from kafka_module.topics.event_adhesion import EventAdhesionHandler
from kafka_module.producer import KafkaProducerManager
from database.databaseManager import get_session
from database.models import EventAdhesion, ExternalEvent, User

def test_publish():
    print("Testing publish method...")
    bootstrap_servers = 'localhost:9092'
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = EventAdhesionHandler(producer)
    
    event_id = 1
    org_id = 1
    volunteer_id = 1
    volunteer_name = 'John'
    volunteer_last_name = 'Doe'
    volunteer_phone = '123456789'
    volunteer_email = 'john.doe@example.com'
    volunteer_org_id = 1
    expected_topic = f'adhesion_evento_{org_id}'
    expected_message = {
        'event_id': int(event_id),
        'volunteer': {
            'id': int(volunteer_id),
            'name': str(volunteer_name),
            'last_name': str(volunteer_last_name),
            'phone': str(volunteer_phone),
            'email': str(volunteer_email)
        },
        'org_id': int(volunteer_org_id)
    }
    
    try:
        handler.publish(event_id, org_id, volunteer_id, volunteer_name, volunteer_last_name, volunteer_phone, volunteer_email, volunteer_org_id)
        print("test_publish: OK")
        
        consumer = KafkaConsumer(
            expected_topic,
            bootstrap_servers=bootstrap_servers,
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            group_id=f'test_event_adhesion_{uuid.uuid4()}',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        
        time.sleep(1)
        message_found = False
        for message in consumer:
            received_message = message.value
            if (received_message['event_id'] == expected_message['event_id'] and
                received_message['volunteer']['id'] == expected_message['volunteer']['id'] and
                received_message['volunteer']['email'] == expected_message['volunteer']['email']):
                print("Published message verified in Kafka topic")
                message_found = True
                break
        consumer.close()
        
        if not message_found:
            print("test_publish: FAILED - Message not found in Kafka topic")
            return False
        return True
    except Exception as e:
        print(f"test_publish: FAILED - Error: {str(e)}")
        return False
    finally:
        producer.close()

def test_consume():
    print("Testing consume method...")
    bootstrap_servers = 'localhost:9092'
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = EventAdhesionHandler(producer)
    
    own_org_id = 1
    event_id = 1
    volunteer_id = 1
    volunteer_name = 'John'
    volunteer_last_name = 'Doe'
    volunteer_phone = '123456789'
    volunteer_email = 'john.doe@example.com'
    volunteer_org_id = 1
    test_topic = f'adhesion_evento_{own_org_id}'
    
    # Insert test external event
    session = get_session()
    try:
        external_event = ExternalEvent(
            event_id=event_id,
            org_id=own_org_id,
            name="Test External Event",
            description="Test event for adhesion",
            date_time=datetime.utcnow() + timedelta(days=15),
            is_canceled=False,
            created_at=datetime.utcnow()
        )
        session.add(external_event)
        session.commit()
        print("Inserted test external event successfully")
    finally:
        session.close()
    
    try:
        # Insert test volunteer user if needed
        session = get_session()
        try:
            user = session.query(User).filter_by(id=volunteer_id).first()
            if not user:
                # Assume user exists or create a placeholder
                print(f"Assuming volunteer_id={volunteer_id} exists")
        finally:
            session.close()
        
        message = {
            'event_id': event_id,
            'volunteer': {
                'id': volunteer_id,
                'name': volunteer_name,
                'last_name': volunteer_last_name,
                'phone': volunteer_phone,
                'email': volunteer_email
            },
            'org_id': volunteer_org_id
        }
        producer.send_message(test_topic, message)
        print("Test message published for consumption")
        
        callback_data = []
        def callback(data):
            print(f"Callback received data: {data}")
            callback_data.append(data)
        
        group_id = f"test_event_adhesion_{uuid.uuid4()}"
        import threading
        consumer_thread = threading.Thread(
            target=handler.consume,
            args=(own_org_id, callback, group_id, test_topic)
        )
        consumer_thread.daemon = True
        consumer_thread.start()
        
        time.sleep(10)
        
        session = get_session()
        try:
            adhesion = session.query(EventAdhesion).filter_by(
                event_id=event_id,
                org_id=own_org_id,
                volunteer_id=volunteer_id
            ).first()
            if adhesion and adhesion.volunteer_name == volunteer_name and adhesion.volunteer_email == volunteer_email:
                print(f"test_consume: OK - Found adhesion in database: event_id={adhesion.event_id}, "
                      f"volunteer_id={adhesion.volunteer_id}, volunteer_name={adhesion.volunteer_name}")
                return True
            else:
                print("test_consume: FAILED - Adhesion not found in database or data mismatch")
                return False
        finally:
            session.close()
    except Exception as e:
        print(f"test_consume: FAILED - Error: {str(e)}")
        return False
    finally:
        producer.close()

def main():
    print("Testing EventAdhesionHandler...")
    publish_result = test_publish()
    consume_result = test_consume()
    
    if publish_result and consume_result:
        print("All EventAdhesionHandler tests passed!")
    else:
        print("One or more EventAdhesionHandler tests failed!")

if __name__ == '__main__':
    main()