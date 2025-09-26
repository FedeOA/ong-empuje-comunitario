import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from .producer import KafkaProducerManager
from .topics.user_events import UserEventsHandler
from .topics.donation_events import DonationEventsHandler
from .topics.event_events import EventEventsHandler
from .topics.donation_transfer import DonationTransferHandler
from .topics.donation_offer import DonationOfferHandler
from .topics.donation_request_cancellation import DonationRequestCancellationHandler
from .topics.solidarity_event import SolidarityEventHandler
from .topics.event_cancellation import EventCancellationHandler
from .topics.event_adhesion import EventAdhesionHandler

class KafkaManager:
    def __init__(self, bootstrap_servers='localhost:9092'):
        self.producer = KafkaProducerManager(bootstrap_servers)
        self.user_events = UserEventsHandler(self.producer)
        self.donation_events = DonationEventsHandler(self.producer)
        self.event_events = EventEventsHandler(self.producer)
        self.donation_transfer = DonationTransferHandler(self.producer)
        self.donation_offer = DonationOfferHandler(self.producer)
        self.donation_request_cancellation = DonationRequestCancellationHandler(self.producer)
        self.solidarity_event = SolidarityEventHandler(self.producer)
        self.event_cancellation = EventCancellationHandler(self.producer)
        self.event_adhesion = EventAdhesionHandler(self.producer)

    def publish_user_event(self, action, user_data):
        self.user_events.publish(action, user_data)

    def publish_donation_event(self, action, donation_data):
        self.donation_events.publish(action, donation_data)

    def publish_event_event(self, action, event_data):
        self.event_events.publish(action, event_data)

    def publish_donation_transfer(self, request_id, donor_org_id, donations, recipient_org_id):
        self.donation_transfer.publish(request_id, donor_org_id, donations, recipient_org_id)

    def publish_donation_offer(self, offer_id, donor_org_id, donations):
        self.donation_offer.publish(offer_id, donor_org_id, donations)

    def publish_donation_request_cancellation(self, org_id, request_id):
        self.donation_request_cancellation.publish(org_id, request_id)

    def publish_solidarity_event(self, org_id, event_id, name, description, date_time):
        self.solidarity_event.publish(org_id, event_id, name, description, date_time)

    def publish_event_cancellation(self, org_id, event_id):
        self.event_cancellation.publish(org_id, event_id)

    def publish_event_adhesion(self, event_id, org_id, volunteer_id, volunteer_name, volunteer_last_name, volunteer_phone, volunteer_email, volunteer_org_id):
        self.event_adhesion.publish(event_id, org_id, volunteer_id, volunteer_name, volunteer_last_name, volunteer_phone, volunteer_email, volunteer_org_id)

    def consume_user_events(self, callback):
        self.user_events.consume(callback)

    def consume_donation_events(self, callback):
        self.donation_events.consume(callback)

    def consume_event_events(self, callback):
        self.event_events.consume(callback)

    def consume_donation_transfer(self, org_id, callback, group_id=None, topic=None):
        self.donation_transfer.consume(org_id, callback, group_id or f'donation_transfer_{org_id}', topic or f'transferencia_donaciones_{org_id}')

    def consume_donation_offer(self, callback, group_id=None, topic=None):
        self.donation_offer.consume(callback, group_id or 'donation_offer_consumer', topic or 'oferta_donaciones')

    def consume_donation_request_cancellation(self, callback):
        self.donation_request_cancellation.consume(callback)

    def consume_solidarity_event(self, own_org_id, callback):
        self.solidarity_event.consume(own_org_id, callback)

    def consume_event_cancellation(self, callback):
        self.event_cancellation.consume(callback)

    def consume_event_adhesion(self, own_org_id, callback, group_id=None, topic=None):
        self.event_adhesion.consume(own_org_id, callback, group_id or f'event_adhesion_consumer_{own_org_id}', topic or f'adhesion_evento_{own_org_id}')

    def close(self):
        self.producer.close()