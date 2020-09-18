#Cron line parser

This repository contains simple implementation of cron line parser.

The application only supports standard cron format with five time fields:
 - minute
 - hour 
 - day of month
 - month 
 - day of week
  
It doesn't handle the special time strings such as "@yearly". 
  
  
## Run the application

### Requirements

In order to compile the project the following applications should be installed:
 - JDK 1.8 or higher 
 - [sbt](https://www.scala-sbt.org/download.html)
 
### Running commands

To run the program run following command
```
> ./run.sh "<CronString>"

```

Where `CronString` is cron string to be validated.

Example:
```
> ./run.sh "*/15 900 1,15 */-1,900/1,-1/22 1-5 /usr/bin/find" 
```

To run tests type:
```
> sbt test
```

## Known issues / limitations:
 - there are tests which still failing (wasn't fixed due to time limit)
 - there are not enough tests which check successful result (existing tests doesn't check actual result. Just check that parse result is not error)
 - program output contains `sbt` output followed by program run result. This is limitation of usage of `sbt` as run tool. 
 It is possible to fix by making jar file but required more sophisticated build configuration.    