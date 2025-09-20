import sys
import os
# Add servers/ directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
import grpc
import services_pb2.donation_pb2 as donation_pb2
import services_pb2_grpc.donation_pb2_grpc as donation_pb2_grpc
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from database import databaseManager

Session = databaseManager.get_session()

def test_donation_service(stub):
    print("\n=== Testing DonationService ===")
    
    # Test CreateDonation
    donation = donation_pb2.Donation(
        description="Test Donation",
        cantidad=100,
        eliminado=False
    )
    response = stub.CreateDonation(donation)
    print(f"CreateDonation: success={response.success}, message={response.message}")
    donation_id = None
    
    # Test ListDonations
    donation_list = stub.ListDonations(donation_pb2.Empty())
    print(f"ListDonations: found {len(donation_list.donation)} donations")
    if donation_list.donation:
        for d in donation_list.donation:
            print(f"  - Donation: id={d.id}, description={d.description}, cantidad={d.cantidad}, eliminado={d.eliminado}")
        donation_id = donation_list.donation[-1].id
    else:
        print("  - No donations found, skipping UpdateDonation and DeleteDonation")
        return
    
    # Test UpdateDonation
    if donation_id:
        donation_to_update = donation_pb2.Donation(
            id=donation_id,
            description="Updated Donation",
            cantidad=200,
            eliminado=False
        )
        response = stub.UpdateDonation(donation_to_update)
        print(f"UpdateDonation: success={response.success}, message={response.message}")
    
    # Test DeleteDonation
    if donation_id:
        donation_to_delete = donation_pb2.Donation(id=donation_id)
        response = stub.DeleteDonation(donation_to_delete)
        print(f"DeleteDonation: success={response.success}, message={response.message}")

def main():
    print("Starting DonationService Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_donation = donation_pb2_grpc.DonationServiceStub(channel)  # Fixed stub name
    except Exception as e:
        print(f"Failed to connect to server: {str(e)}")
        return
    
    test_donation_service(stub_donation)
    
    channel.close()
    print("\n==========================")
    print("DonationService Tests Completed")

if __name__ == '__main__':
    main()