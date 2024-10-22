import sys
import json
import logging

logger = logging.getLogger(__name__)


class QueryConstructor:
    """
    This class will take in a dictionary representation of arguments in a request
    and return an Elasticsearch Query Object.

    Supports:
    - Query by Log-ID (exact match)
    - Query by Timestamp (range match)
    - Query by Description (full text search)
    - Query by user ID (exact match)
    """
    @staticmethod
    def construct_log_id(request: dict) -> str:
        if 'logId' not in request or request['logId'] is None:
            logger.debug("Log ID not specified in request.")
            return ""
        # log_id = request['logId'].replace("-", "_")
        log_id = request['logId']

        log_id_query = {
            "term": {
                "logId": log_id
            }
        }

        log_id_query = json.dumps(log_id_query)

        logger.debug(f"Constructed Log ID query: {log_id_query}")
        return log_id_query

    @staticmethod
    def construct_timestamp(request: dict) -> str:
        # takes the minimum of the searchTimestamp and endTimestamp
        timestamp_upper_bound = (
                request['searchTimestamp']
                if (
                        'endTimestamp' not in request or
                        request['endTimestamp'] == 0 or
                        request['endTimestamp'] is None
                )
                else min(request['endTimestamp'], request['searchTimestamp']) 
            )

        logger.debug(timestamp_upper_bound)
        if timestamp_upper_bound > sys.maxsize:
            timestamp_upper_bound = sys.maxsize
        
        if (
                'startTimestamp' not in request or
                request['startTimestamp'] == 0.0 or
                request['startTimestamp'] is None
        ):
            timestamp_query = {
                "range": {
                    "timestamp": {
                        "lte": max(timestamp_upper_bound, 0)
                    }
                }
            }
            logger.debug(timestamp_query)
            timestamp_query = json.dumps(timestamp_query)

            logger.debug(f"Constructed Timestamp query: {timestamp_query}")
            return timestamp_query

        timestamp_query = {
                "range": {
                    "timestamp": {
                        "gte": min(max(request.get('startTimestamp', 0), 0), sys.maxsize),
                        "lte": max(timestamp_upper_bound, 0)
                    }
                }
            }

        timestamp_query = json.dumps(timestamp_query)
        logger.debug(f"Constructed Timestamp query: {timestamp_query}")
        return timestamp_query

    @staticmethod
    def construct_description(request: dict) -> dict:
        if 'description' not in request or request['description'] is None:
            logger.debug("Description not specified in request.")
            return {}

        description_query = {
            "match_phrase": {
                "description": request['description']
            }
        }

        logger.debug(f"Constructed Description query: {description_query}")
        return description_query

    @staticmethod
    def construct_initiator_user(request: dict) -> dict:
        if 'initiatorUser' not in request or request['initiatorUser'] is None:
            logger.debug("Initiator User not specified in request.")
            return {}

        initiator_user_query = {
            "match_phrase": {
                "initiatorUser": request['initiatorUser']
            }
        }

        logger.debug(f"Constructed Initiator User query: {initiator_user_query}")
        return initiator_user_query

    @staticmethod
    def construct_target_user(request: dict) -> dict:
        if 'targetUser' not in request or request['targetUser'] is None:
            logger.debug("Target User not specified in request.")
            return {}

        target_user_query = {
            "match_phrase": {
                "targetUser": request['targetUser']
            }
        }

        logger.debug(f"Constructed Target User query: {target_user_query}")
        return target_user_query

    @staticmethod
    def construct_query(request: dict) -> dict:
        query_parts = [
            QueryConstructor.construct_description(request),
            QueryConstructor.construct_initiator_user(request),
            QueryConstructor.construct_target_user(request)
        ]

        size = request.get('size', 10)
        start = request.get('start', 0)

        if size < 0 or size > sys.maxsize:
            size = 10

        if start < 0 or start > sys.maxsize:
            start = 0

        query_list = {
            "must": []
        }


        for query_part in query_parts:
            if query_part != {}:
                query_list["must"].append(query_part)

        logger.debug(query_list)

        query = {
            "query": {
                "bool": {
                    **query_list,
                    "filter": [
                        json.loads(QueryConstructor.construct_timestamp(request)),
                    ]
                },
            },
            "size": size,
            "from": start,
            "sort": [{
                "timestamp": {
                    "order": "desc"
                }
            }]
        }

        logger.debug(f"Final constructed Query object: {query}")
        return query
