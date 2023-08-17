# HiOA Online Meeting & Automation Office System

## Important Note

This guide assume you have the following already installed.

- Java JDK 15.0.2 

- Redis 3.2.100

- MongoDb

- *Visual C++* Redistributable Packages

- Navicat

- RabbitMQ

- Node.js

  The versions above match the development environment in which this software product was built. Strongly recommend using Java JDK of 15.0.2 or newer versions or else the project could not be run. 

## Build and run

1. Create "hioa" database in Navicat with UTF-8 encoding.

2. Execute "HiOA.sql" SQL file in /HiOA-api 

3. Configure your MySQL and Redis username and password to match the ones in application.yml in HiOA-api\src\main\resources and HiOA-workflow\src\main\resources

4. Build and run both back-end and workflow back-end in IntelliJ or 

   download runnable binaries in https://drive.google.com/drive/u/0/folders/14utI6y_IXm-cCF6YPHiZNqN_tqmXSuwX 

   copy "hioa-workflow.jar" to \HiOA-workflow\src\main\resources

   run the following commands in \HiOA-workflow\src\main\resources

   ```
   java -jar -Dfile.encoding=utf-8 hioa-workflow.jar --spring.config.location=application.yml
   ```

   copy "hioa-api.jar" to /HiOA-api/src/main/resources

   run the following commands in /HiOA-api/src/main/resources

   ```
   java -jar -Dfile.encoding=utf-8 hioa-api.jar --spring.config.location=application.yml
   ```

5. run the following commands in hioa-vue

   ```
   npm run dev
   ```

    if the front-end do not run successfully, try deleting the /node_modules folder and try running following command before running the project

   ```
   npm install
   ```

6. browse http://localhost:3000/hioa-vue/ at your browser