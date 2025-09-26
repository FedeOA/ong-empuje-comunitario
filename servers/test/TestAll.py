import subprocess
import os
import sys
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))

def run_test(test_file):
    full_path = os.path.join(os.path.dirname(__file__), test_file)
    result = subprocess.run([sys.executable, full_path], 
                           cwd=os.path.dirname(__file__), 
                           capture_output=True, 
                           text=True)
    print(result.stdout)
    if result.stderr:
        print(f"Errors in {test_file}:")
        print(result.stderr)
    return result.returncode == 0

if __name__ == '__main__':
    tests = ['TestUser.py', 'TestEvent.py', 'TestDonation.py', 'TestAuthentication.py']
    all_passed = True
    print("Starting All Service Tests")
    print("==========================")
    
    for test in tests:
        print(f"\n=== Running {test} ===")
        if not run_test(test):
            all_passed = False
    
    print("\n==========================")
    if all_passed:
        print("All Service Tests Completed Successfully")
    else:
        print("Some Service Tests Failed")
        sys.exit(1)