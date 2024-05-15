# Blockchain Data Pipeline

Platform to ingest real-time smart contract events for the Utiliti.ai toolsuite.

## Description

The data pipeline was originally built to be integrated into the [Utiliti AI](https://utiliti.ai) tool suite offering for web2 and web3 companies. 
The platform allowed users to select smart contracts and the events detected from their ABI, and create webhooks to receive a parsed JSON payload of the event in real time.

The system supports failover and resyncing and was designed to be deployed locally with LocalStack and on AWS utilizing Lambdas and SQS. The project was created in node, then moved over to Java for a better and cleaner system.
