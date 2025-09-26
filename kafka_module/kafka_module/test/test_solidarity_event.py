import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
import json
import time
from datetime import datetime, timedelta
from kafka_module.topics.solidarity_event import SolidarityEventHandler
from kafka_module.producer import KafkaProducerManager
from kafka import KafkaConsumer

def test_publish():
    print("Testing publish method...")
    bootstrap_servers = 'localhost:9092'
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = SolidarityEventHandler(producer)
    
    org_id = 1
    event_id = 1
    name = 'Test Event'
    description = 'Test Description'
    date_time = (datetime.now() + timedelta(days=1)).strftime('%Y-%m-%d %H:%M:%S')
    expected_message = {
        'org_id': org_id,
        'event_id': event_id,
        'name': name,
        'description': description,
        'date_time': date_time
    }
    
    try:
        handler.publish(org_id, event_id, name, description, date_time)
        print("test_publish: OK")
        
        consumer = KafkaConsumer(
            'eventos_solidarios',
            bootstrap_servers=bootstrap_servers,
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            group_id='test_solidarity_event',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        
        time.sleep(1)
        message_found = False
        for message in consumer:
            received_message = message.value
            if (received_message['org_id'] == expected_message['org_id'] and
                received_message['event_id'] == expected_message['event_id'] and
                received_message['name'] == expected_message['name']):
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
    handler = SolidarityEventHandler(producer)
    
    own_org_id = 1
    org_id = 2
    event_id = 1
    name = 'Test Event'
    description = 'Test Description'
    date_time = (datetime.now() + timedelta(days=1)).strftime('%Y-%m-%d %H:%M:%S')
    expected_message = {
        'org_id': org_id,
        'event_id': event_id,
        'name': name,
        'description': description,
        'date_time': date_time
    }
    
    try:
        producer.send_message('eventos_solidarios', expected_message)
        print("Test message published for consumption")
        
        callback_data = []
        def callback(data):
            print(f"Callback received data: {data}")
            callback_data.append(data)
        
        import threading
        consumer_thread = threading.Thread(
            target=handler.consume,
            args=(own_org_id, callback,)
        )
        consumer_thread.daemon = True
        consumer_thread.start()
        
        time.sleep(10)
        
        if len(callback_data) >= 1 and expected_message in callback_data:
            print("test_consume: OK - Callback received correct data")
            return True
        else:
            print(f"test_consume: FAILED - Expected at least 1 callback with {expected_message}, got {len(callback_data)} callbacks")
            return False
    except Exception as e:
        print(f"test_consume: FAILED - Error: {str(e)}")
        return False
    finally:
        producer.close()

def main():
    print("Testing SolidarityEventHandler...")
    publish_result = test_publish()
    consume_result = test_consume()
    
    if publish_result and consume_result:
        print("All SolidarityEventHandler tests passed!")
    else:
        print("One or more SolidarityEventHandler tests failed!")

if __name__ == '__main__':
    main()