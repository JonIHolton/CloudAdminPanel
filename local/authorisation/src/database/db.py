import psycopg2
from psycopg2.extras import RealDictCursor
import json

class Database:
    def __init__(self, db_uri):
        self.connection = psycopg2.connect(db_uri)
    
    def get_policy_document(self):
        """Fetches the policy document from the database."""
        with self.connection.cursor(cursor_factory=RealDictCursor) as cursor:
            cursor.execute("SELECT policy FROM policies WHERE id=1;")
            result = cursor.fetchone()
            # If result is not None, return the 'policy' field directly without json.loads()
            return result['policy'] if result else None


    def update_policy_document(self, policy_document):
        """Updates the policy document in the database."""
        policy_json = json.dumps(policy_document)
        with self.connection.cursor() as cursor:
            cursor.execute("UPDATE policies SET policy=%s WHERE id=1;", (policy_json,))
            self.connection.commit()
    
    def get_role_permissions(self, role_name):
        """Fetches permissions for a specific role."""
        policy = self.get_policy_document()
        role = next((r for r in policy['roles'] if r['roleName'] == role_name), None)
        return role['permissions'] if role else None

    def update_role_permission(self, role_name, resource, permission, value):
        """Updates a specific permission for a role."""
        policy = self.get_policy_document()
        role = next((r for r in policy['roles'] if r['roleName'] == role_name), None)
        if role:
            if resource in role['permissions']:
                role['permissions'][resource][permission] = value
                self.update_policy_document(policy)
                return True
        return False


    def __del__(self):
        if hasattr(self, 'connection') and self.connection:
            self.connection.close()
