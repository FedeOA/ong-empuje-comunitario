# kafka_module/setup.py
import os
from setuptools import setup, find_packages

setup(
    name='kafka_integration',
    version='0.1.0',
    packages=find_packages(),
    install_requires=[
        'kafka-python==2.0.2',
        'sqlalchemy==1.4.41',
        'pymysql==1.0.2',
        'grpcio==1.51.1',
        'grpcio-tools==1.51.1',
    ],
    author='Your Name',
    author_email='your.email@example.com',
    description='Kafka integration module for ONG system',
    long_description=open('README.md').read() if os.path.exists('README.md') else '',
    long_description_content_type='text/markdown',
)