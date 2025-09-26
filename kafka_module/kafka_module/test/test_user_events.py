import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
import json
import time
from kafka_module.topics.user_events import UserEventsHandler
from kafka_module.producer import KafkaProducerManager
from kafka import KafkaConsumer

def test_publish():
    print("Testing publish method...")
    bootstrap_servers = 'localhost:9092'
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = UserEventsHandler(producer)
    
    action = 'create'
    user_data = {'id': 1, 'name': 'Test User'}
    expected_message = {'service': 'user', 'action': action, 'data': user_data}
    
    try:
        handler.publish(action, user_data)
        print("test_publish: OK")
        
        consumer = KafkaConsumer(
            'user_events',
            bootstrap_servers=bootstrap_servers,
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            group_id='test_user_events',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        
        time.sleep(1)
        message_found = False
        for message in consumer:
            received_message = message.value
            if (received_message['service'] == expected_message['service'] and
                received_message['action'] == expected_message['action'] and
                received_message['data'] == expected_message['data']):
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
    handler = UserEventsHandler(producer)
    
    action = 'create'
    user_data = {'id': 1, 'name': 'Test User'}
    expected_message = {'service': 'user', 'action': action, 'data': user_data}
    
    try:
        producer.send_message('user_events', expected_message)
        print("Test message published for consumption")
        
        callback_data = []
        def callback(data):
            print(f"Callback received data: {data}")
            callback_data.append(data)
        
        import threading
        consumer_thread = threading.Thread(
            target=handler.consume,
            args=(lambda data: callback_data.append(data),)
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
    print("Testing UserEventsHandler...")
    publish_result = test_publish()
    consume_result = test_consume()
    
    if publish_result and consume_result:
        print("All UserEventsHandler tests passed!")
    else:
        print("One or more UserEventsHandler tests failed!")

if __name__ == '__main__':
    main()