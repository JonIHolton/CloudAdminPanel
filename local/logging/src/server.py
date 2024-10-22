from flask import Flask, request, jsonify
import logging
import os

from elasticsearch7 import Elasticsearch, NotFoundError

from utils.ResultParser import ResultParser
from utils.QueryConstructor import QueryConstructor

# Initialize Flask app
app = Flask(__name__)

# host = 'https://search-itsag1t1-dev-2t2xvd66arsw2vclfgyom3w7jy.ap-southeast-2.es.amazonaws.com:443'
host = db_host = os.getenv('OPENSEARCH_URL')
password = os.getenv('OPENSEARCH_PASSWORD')
masteruser = os.getenv('OPENSEARCH_MASTERUSER')


index = "itsa_g1t1_logs"
client = Elasticsearch(
    host,
    http_auth=(masteruser, password),
    use_ssl=True,
    verify_certs=True
)

print(client.info())




@app.route('/logs/query', methods=['POST'])
def query_logs():
    request_data = request.get_json()
    logging.info(f"RS {request_data}")
    if not request_data:
        return jsonify({"error": "Invalid or missing JSON payload"}), 400
    log_id = request_data.get('logId')
    if log_id:
        try:
            result = client.get(index=index, id=log_id)
            response = ResultParser.result_parser(result)
            return jsonify({"total": 1, "results": [response]})
        except NotFoundError:
            return jsonify({"error": "Log not found", "details": f"logId {log_id} does not exist"}), 404
        except Exception as e:
            logging.exception("Error processing request with logId")
            return jsonify({"error": "Internal server error"}), 500

    errors = validate_request_data(request_data)
    if errors:
        logging.info(f"Validation errors: {errors}")
        return jsonify({"error": "Validation failed", "details": errors}), 400
    logging.info(f"Validated request data: {request_data}")
    try:
        query = QueryConstructor.construct_query(request_data)
        logging.info(f"Final constructed Query object: {query}")
        results = client.search(index=index, body=query)
        logging.info(f"Results: {results}")

        responses = [
            ResultParser.result_parser(doc) for doc in results["hits"]["hits"]
        ]
        count = results["hits"]["total"]["value"]
        return jsonify({"total": count, "results": responses})
    except Exception as e:
        logging.exception("Error processing request")
        return jsonify({"error": "Internal server error"}), 500


@app.route('/logs/query/get_by_id', methods=['POST'])
def get_log_by_id():
    request_data = request.get_json()
    logging.info(f"Received request data: {request_data}")

    if not request_data:
        return jsonify({"error": "Invalid or missing JSON payload"}), 400
    log_id = request_data.get("logId", None)

    if log_id is None or (isinstance(log_id, str) and len(log_id) == 0):
        logging.info("Log ID not specified in request.")
        return jsonify({"error": "Validation failed"}), 400

    logging.info(f"Validated request data: {request_data}")

    try:
        # transform logId to escape hyphens
        # log_id = str(log_id).replace("-", "\\-")
        result = client.get(index=index, id=log_id)
        response = ResultParser.result_parser(result)

        return jsonify(response)
    except NotFoundError as e:
        logging.exception(f"Error retrieving log by ID: {log_id}")
        return jsonify({"error": e.error}), 404
    except Exception as e:
        logging.exception(f"Error retrieving log by ID: {log_id}")
        return jsonify({"error": "Internal server error"}), 500


def validate_request_data(request_data):
    required_fields = {
        'logId': (str, type(None)),  
        'searchTimestamp': (int, float),
        'startTimestamp': (int, float, type(None)),  
        'endTimestamp': (int, float, type(None)),  
        'description': (str, type(None)),  
        'initiatorUser': (str, type(None)),  
        'targetUser': (str, type(None)),  
        'start': int,
        'size': int,
    }

    errors = []
    for field, expected_type in required_fields.items():
        if field not in request_data:
            errors.append(f"Missing required field: {field}")
            logging.info(f"Missing required field: {field}")
        elif not isinstance(request_data[field], expected_type):
            errors.append(f"Invalid type for field {field}. Expected {expected_type}, got {type(request_data[field])}")
            logging.info(f"Invalid type for field {field}. Expected {expected_type}, got {type(request_data[field])}")


    return errors


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')

    app.run(debug=True, host='0.0.0.0', port=8005)
