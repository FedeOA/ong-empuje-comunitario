import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))

from test_donation_events import main as test_donation_events
from test_donation_offer import main as test_donation_offer
from test_donation_request_cancellation import main as test_donation_request_cancellation
from test_donation_transfer import main as test_donation_transfer
from test_event_adhesion import main as test_event_adhesion
from test_event_cancellation import main as test_event_cancellation
from test_event_events import main as test_event_events
from test_solidarity_event import main as test_solidarity_event
from test_user_events import main as test_user_events

def run_all_tests():
    print("Running all Kafka handler tests...")
    
    test_donation_events()
    test_donation_offer()
    test_donation_request_cancellation()
    test_donation_transfer()
    test_event_adhesion()
    test_event_cancellation()
    test_event_events()
    test_solidarity_event()
    test_user_events()
    
    print("All tests completed!")

if __name__ == '__main__':
    run_all_tests()