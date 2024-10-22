class PolicyCache:
    def __init__(self):
        self.policy_cache = None

    def get_policy(self, db_instance):
        if self.policy_cache is None:
            self.policy_cache = db_instance.get_policy_document()
        return self.policy_cache

    def update_policy(self, db_instance, policy_document):
        db_instance.update_policy_document(policy_document)
        self.policy_cache = policy_document

    def invalidate_cache(self):
        self.policy_cache = None
