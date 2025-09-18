import os

# Database configuration
DB_USER = os.getenv('DB_USER', 'root')
DB_PASS = os.getenv('DB_PASS', '')
DB_HOST = os.getenv('DB_HOST', '127.0.0.1')
DB_PORT = os.getenv('DB_PORT', '3306')
DB_NAME = os.getenv('DB_NAME', 'ong-empuje-comunitario')

# SMTP configuration
SMTP_SERVER = os.getenv('SMTP_SERVER', 'smtp.gmail.com')
SMTP_PORT = os.getenv('SMTP_PORT', '587')
SMTP_USER = os.getenv('SMTP_USER', 'gastonromero210@gmail.com')  # e.g., 'your-email@gmail.com'
SMTP_PASSWORD = os.getenv('SMTP_PASSWORD', 'tqsslhzjgujoquof')  # e.g., Gmail App Password