# strike-off-objections-api

### Overview
API for handling objections to a company being struck off the register

| :exclamation:  To ensure communication to the Kafka Broker isn't broken The Kafka-clients dependency is included |
|------------------------------------------------------------------------------------------------------------------|

``` xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>2.3.1</version>
    </dependency> 
```
### Requirements
 
In order to run the service locally you will need the following:

- [OpenJDK 21](https://jdk.java.net/21/)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

### Getting started

To checkout and build the service:
1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.
2. Run ./bin/chs-dev modules enable strike-off-objections
3. Run ./bin/chs-dev development enable strike-off-objections-api (this will allow you to make changes).
4. Run docker using "tilt up" in the docker-chs-development directory.
5. Use spacebar in the command line to open tilt window - wait for strike-off-objections-web to become green.
6. Open your browser and go to page http://chs.local/strike-off-objections
7. Work though the pages using example company THE BEAR RETAIL 05916434

These instructions are for a local docker environment.


### Endpoints

The full path for each endpoint begins with the app url:

`${API_URL}/company/{COMPANY_NUMBER}/strike-off-objections`

This is common to all the endpoints.

| Method     | Path                                                 | Description                                                                                                                                                  |
|:-----------|:-----------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **GET**    | `/eligibility`                                       | Returns information describing the strike-off eligibility of the company.                                                                                    |
| **GET**    | `/{objectionId}`                                     | Returns the objection identified by objectionId.                                                                                                             |
| **GET**    | `/{objectionId}/attachments`                         | Get all attachments from the Objection.                                                                                                                      |
| **GET**    | `/{objectionId}/attachments/{attachmentId}`          | Get this attachment.                                                                                                                                         |
| **GET**    | `/{objectionId}/attachments/{attachmentId}/download` | Download this attachment.                                                                                                                                    |
| **POST**   | `/`                                                  | Create a new strike-off objection.                                                                                                                           |
| **POST**   | `/{objectionId}/attachments`                         | Add an attachment to the Objection.                                                                                                                          |
| **PATCH**  | `/{objectionId}`                                     | Updates the strike-off objection identified by objectionId with the values provided. If status set to SUBMITTED, this will trigger the Objection processing. |
| **DELETE** | `/{objectionId}/attachments/{attachmentId}`          | Delete this attachment from the Objection.                                                                                                                   |

### Config variables

| Key                                              | Example Value                                         | Description                                                                               |
|--------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------------------------------------------|
| `ACTION_CODES_COMPANY_STRUCK_OFF`                | 90,9000,9100                                          | Company already struck off. Objections cannot be raised.                                  |
| `ACTION_CODES_STRIKE_OFF_NOTICE`                 | 4100,4300,4400,5000                                   | Notice given, but not struck off. Objections allowed.                                     |
| `API_URL`                                        | `http://api.chs.local:4001`                           |                                                                                           |
| `CHS_API_KEY`                                    | <API_KEY>                                             | Secures access to the objections api.                                                     |
| `EMAIL_ATTACHMENT_DOWNLOAD_URL_PREFIX`           | `http://chs.local/strike-off-objections/download`     |                                                                                           |
| `EMAIL_SCHEMA_MAXIMUM_RETRY_ATTEMPTS`            | 6                                                     |                                                                                           |
| `EMAIL_SCHEMA_URI`                               | `/subjects/email-send/versions/latest`                |                                                                                           |
| `EMAIL_SEND_QUEUE_TOPIC`                         | email-send                                            | kafka queue for the internal and external confirmation emails.                            |
| `EMAIL_SENDER_APP_ID`                            | strike_off_objections                                 |                                                                                           |
| `EMAIL_RECIPIENTS_BELFAST`                       | `NAME@companieshouse.gov.uk`                          | Internal email addresses.                                                                 |
| `EMAIL_RECIPIENTS_CARDIFF`                       | `NAME@companieshouse.gov.uk`                          | Internal email addresses.                                                                 |
| `EMAIL_RECIPIENTS_EDINBURGH`                     | `NAME@companieshouse.gov.uk`                          | Internal email addresses.                                                                 |
| `EMAIL_SUBJECT`                                  | {{ COMPANY_NUMBER }}: Objection Application Submitted | Reference to company objection is raised against.                                         |
| `EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE` | strike_off_objections_application_submitted_external  | Ensures notification api sends the correct email relating to what the user has requested. |
| `EMAIL_SUBMITTED_INTERNAL_TEMPLATE_MESSAGE_TYPE` | strike_off_objections_application_submitted_internal  | Ensures notification api sends the correct email relating to what the user has requested. |
| `FEATURE_FLAG_SEND_CHIPS_CONTACT_DATA`           | true                                                  | Temporary feature flag.                                                                   |
| `FILE_TRANSFER_API_URL`                          | `https://<AWS_URL>/strike-off-objections/files`       | Allows upload of user documents.                                                          |
| `FILE_TRANSFER_API_KEY`                          | <API_KEY>                                             | Secures access to the file transfer api.                                                  |
| `GAZ_1_ACTION_CODE`                              | 5000                                                  | As above notice given, but not struck off objections allowed.                             |
| `HUMAN_LOG`                                      | 1                                                     |                                                                                           |
| `KAFKA_BROKER_ADDR`                              | `kafka:9092`                                          |                                                                                           |
| `MONGODB_URL`                                    | `mongodb://mongo/strike_off_objections`               |                                                                                           |
| `ORACLE_QUERY_API_URL`                           | `http://oracle-query-api:8080`                        | Company lookup.                                                                           |
| `SCHEMA_REGISTRY_URL`                            | `http://chs-kafka-schemas`                            | Where email schema is stored.                                                             |
| `UPLOAD_MAX_FILE_SIZE`                           | 6MB                                                   |                                                                                           |
| `UPLOAD_MAX_REQUEST_SIZE`                        | 6MB                                                   |                                                                                           |
| `FEATURE_FLAG_USE_KAFKA_FOR_CHIPS_CALL_170121`   | false                                                 |                                                                                           |
| `PAYMENTS_API_URL`                               | 'NOT_USED'                                            |                                                                                           |
| `DOCUMENT_API_LOCAL_URL`                         | 'NOT-USED'                                            |                                                                                           |
