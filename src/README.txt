a. Steps to run the project

1. Open demo > src > main > java > com.synpulse8hiringchllenge.demo > Synpulse8HiringChallengeApplication.java and run the file to start the springboot application.

2. Open a browser or API testing application such as postman and query the following format: http://localhost:8080/transactions?sessionId=customerID_dd-mm-yyyy-hh-mm&month=mm-yyyy where, customerID is the ID of the customer (such as P-12300000), dd-mm-yyyy is a date format (such as 24-03-2023), hh-mm is a time format (14-21), and mm-yyyy is the month for which transactions are queried (such as 12-2022).

3. Keep refreshing the page to get 10 transactions at a time until the screen shows: No more records found for user.


b. System design

The system consists of two Kafka configurations- a producer and a consumer. Both these configurations are used by their manager classes respectively. The purpose of all these classes along with a system diagram are as below:

1. ProducerManager: The ProducerManager class creates random records for a broad range of customerIDs and sends it to the producer configuration to send it to the Kafka topic. This class is purely made for future extension purposes when real transactions can be stored through POST API calls rather than creating fake transactions for the demonstration of this project.

2. Producer: The Producer configuration receives the transactions from ProducerManager and sends it to the Kafka topic.

3. ConsumerManager: The ConsumerManager class receives the REST API call from client to retrieve paginated transactions along with credit and debit values. It then asks the Consumer configuration to retrieve relevant records from the Kafka topic. Once it receives the transactions, it segregates them into credit and debit transactions, then makes a call to a third party API to retrieve current exchange rates. It converts all credit and debit values to USD from EUR, GBP and CHF and adds them up. Lastly, it appends these credit and debit values to the list of transactions and returns it to the client.

4. Consumer: The Consumer configuration is responsible for retrieving relevant records from the Kafka topic. It returns a list of 10 transactions for a given customer in a given month.

The system diagram can be found at: https://portland-my.sharepoint.com/:i:/g/personal/akasliwal2-c_my_cityu_edu_hk/EQYKnOFX2D9KtgvcxFcJcMcBw_PyN0d93TVGB_LSezbCkA?e=NKsOe6


c. Testing

Apart form these four classes, the test files are included in demo > src > test > java > con.synpulse8hiringchallenge.demo. Only the Consumer and ConsumerManager classes are tested as they perform the necessary requirements of the problem statement. The Producer and ProducerManager classes are purely present to create dummy transactions in the kafka topic.

The JUnit tests include Unit and Integration tests, functional tests, and API contract tests.


d. Assumptions

1. All transactions are converted to USD before calculating total credit/debit values as the problem statement does not mention which currency it should be converted to.

2. The consumer is already logged in and authenticated on client side and the GET request is made with a unique session identifier for every user. This project uses customerID_dd-mm-yyyy-hh-mm as a session identifier and passes this as a group ID for reading from Kafka topic. This largely helps to skip past the records already read from the topic using a particular groupID.

3. The current implementation does not allow the client to request for a specific page of transactions. It retrieves 10 transactions at a time for one given customer within one given month. This is largely because the problem statement does not mention that the user can request a specific page.