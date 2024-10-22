
from flask import Flask, jsonify, request
from database.db import Database
from cache.cache import PolicyCache
import os

from flasgger import Swagger

app = Flask(__name__)
Swagger(app)

db_host = os.getenv('DB_HOST')
db_user = os.getenv('DB_USER')
db_password = os.getenv('DB_PASSWORD')
db_name = os.getenv('DB_NAME')
# db = Database(f'postgresql://postgres:123123123@primary-host/testdb')
db = Database(f'postgresql://{db_user}:{db_password}@{db_host}/{db_name}')

cache = PolicyCache()

@app.route('/policies', methods=['GET'])
def get_policy():
    policy = cache.get_policy(db)
    return jsonify(policy), 200

@app.route('/policies/<role_name>/<resource>/<permission>', methods=['PUT'])
def update_policy(role_name, resource, permission):

    value = request.json.get('value')
    success = db.update_role_permission(role_name, resource, permission, value)
    if success:
        cache.invalidate_cache()  
        return jsonify({"message": "Permission updated successfully"}), 200
    else:
        return jsonify({"error": "Role or permission not found"}), 404

@app.route('/policies/<role_name>/<resource>/requires_approval', methods=['PUT', 'DELETE'])
def update_requires_approval(role_name, resource):
    if request.method == 'PUT':
        approvers = request.json.get('approvers')
        success = db.add_requires_approval(role_name, resource, approvers)
        action = "added"
    elif request.method == 'DELETE':
        success = db.remove_requires_approval(role_name, resource)
        action = "removed"

    if success:
        cache.invalidate_cache()  
        return jsonify({"message": f"Approval requirement {action} successfully"}), 200
    else:
        return jsonify({"error": "Role or resource not found"}), 404

@app.route('/getroles', methods=['GET'])
def get_roles():
    policy = cache.get_policy(db)
    if not policy:
        return jsonify({"error": "Policy document not found"}), 404

    role_names = list(policy.keys())
    role_names.insert(0,"User")

    return jsonify(role_names), 200

if __name__ == '__main__':
    app.run(debug=True, port=8002, host='0.0.0.0')
