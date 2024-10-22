This is a standalone environment, integration to the APA has not been done.

## Notes
Logs need to be dump into a file for fluentd to tail.
Change `path /home/fluent/user_creation_logs.txt` in fluentd-node.conf to follow
the log file.

## Environment Configuration
Create a .env file and have these information configured:
```
AWS_ACCESS_KEY_ID : Your aws access key id
AWS_SECRET_ACCESS_KEY : Your aws secret access key
S3_BUCKET : Your aws S3 bucket name
AWS_REGION : Your aws region
```

## Fluentd Configuration
Currently the fluentd setting is to aggregate all logs and push to redis 
every 5 seconds and push to aws S3 every 1 minute

## Log Generation
```
logId: A randomly generated id for each log
timestamp: Timestamp in UTC timing
timezone: Time Zone
description: Description of the user action
deviceInfo: Device info of the client
ipAddress: IP address of the client
initiatorUser: User ID of the user initiates the action
targetUser: User ID of the user that the action acts on
```

## How to use:
1. `pip install -r requirements.txt` to install the dependencies for flask and LogGenerator
2. run `python LogGenerator.py` to generate some fake logs before step 3
3. run `docker compose up`
4. run `python app.py` to have a flask app running

## API endpoint documentation:
URL: `/api/v1/logs/query`
Method: `GET`
Request Parameters (required):
```
searchTimestamp (required): Timestamp in UTC that the user initated the search
```
Request Parameters (optional):
```
startTimestamp: Timestamp in UTC indicating the lower bound of the timestamp to return
endTimestamp: Timestamp in UTC indicating the upper bound of the timestamp to return
logId: Only supports exact match
description: Text in description to be searched, supports full text search
initiatorUser: Only supports exact match
targetUser: Only supports exact match
sort: Sort the logs by timestamp. Default is descending, set sort=asc for ascending
page: Indicates the starting index of the log to be searched, default is 0
size: Indicate the number of logs to be returned, default is 10
```
Response Structure:
200 ok
- total (integer): The total number of log entries matching the query.
- results (array of objects): A list of log entries.
Example: 
`Request: GET /api/v1/logs/query?searchTimestamp=1711350789&size=2`
```
{
  "results": [
    {
      "description": "Tammy Murphy(agent) updated user information of Thomas Bell(customer)",
      "deviceInfo": "Mozilla/5.0 (compatible; MSIE 9.0; Windows 98; Win 9x 4.90; Trident/3.1)",
      "ipAddress": "12.34.56.78",
      "initiatorUser": "87ca1b4e-1760-4361-97a3-c32ead251ecd",
      "logId": "14abe842-87de-4109-831d-06511f4446e9",
      "targetUser": "f058f75c-a489-47a2-9e62-a1cfe3d20c0f",
      "timestamp": "1711254962.3848",
      "timezone": "Asia/Shanghai"
    },
    {
      "description": "Craig Graves(agent) deleted user information of Jeffrey Scott(customer)",
      "deviceInfo": "Mozilla/5.0 (Windows NT 4.0; ga-IE; rv:1.9.0.20) Gecko/6780-05-21 12:15:05 Firefox/3.8",
      "ipAddress": "12.34.56.79",
      "initiatorUser": "881fb1ef-e71d-493f-b729-338bb6d68cd4",
      "logId": "0e51ec9a-21a9-4417-bd31-491eea499bb3",
      "targetUser": "8014dd5f-ba84-4448-8371-2bdc0c2b38b1",
      "timestamp": "1711254962.3848",
      "timezone": "Asia/Shanghai"
    }
  ],
  "total": 390012
}
```

400 bad request
- error (str): The error message from the server
- request (dictionary): The request parameters 
Example: 
`Request: GET /api/v1/logs/query?searchTimestamp=fsaf&page=fea`
```
{
  "error": "Syntax error at offset 17 near fsaf",
  "request": {
    "SearchTimestamp": "fsaf",
    "page": "fea"
  }
}
```

## TODO:
1. Change flask to be running in production server
2. Might need to use ElastiCache for Redis server else might need to configure redis cluster
3. Fix timestamp in S3, it is currently in UTC timing
4. Return the number of logs that match the filtering
5. And a lot
