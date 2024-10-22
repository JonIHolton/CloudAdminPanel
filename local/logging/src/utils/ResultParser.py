import logging
logger = logging.getLogger(__name__)


class ResultParser:
    """
    Parses the result from the search query into a dictionary
    """
    @staticmethod
    def result_parser(result: dict):
        logger.debug(result)
        result = result["_source"]

        return {
            "logId": result.__getitem__("logId"),
            "timestamp": result.__getitem__("timestamp"),
            "timezone": result.__getitem__("timezone"),  # "Timezone": "Asia/Singapore
            "description": result.__getitem__("description"),
            "deviceInfo": result.__getitem__("deviceInfo"),
            "ipAddress": result.__getitem__("ipAddress"),
            "initiatorUser": result.__getitem__("initiatorUser"),
            "targetUser": result.__getitem__("targetUser")
        }
