# Project 2 - Usage & Test Cases

[Introduction](#Introduction)

[How to start this system](#How to start this system)

[Test Scenario](#Test Scenario)

- [High Available](#High Available)
- [Client can join and leave any time](#Client can join and leave any time) & [Server can join at any time](#Server can join at any time)
- [Unique Register](#Unique Register)
- [Message ensure](#Message ensure)
- [Message order](#Message order)
- [Load Balancing](#Load balancing)



## Introduction

This is an advanced version of project 1 which provides:

- High Available
- Eventually Consistancy


**NOTE: Our implementation for delivering activity to client is synchronous, so that you may need to wait a period of time before you can actually receive an activity (default period is 1 second) **

## How to complie this project

You need maven for this build:

```bash
cd $project_folder
./build.sh
```

Two jar files will be generated under `$project_folder`.

## System User Guide

### System Set Up

There are two JAR files in source code package, `ActivityStreamerClient.jar` and `ActivityStreamerServer.jar`. 

### Jar file usage:

#### Server startup

```bash
usage: ActivityStreamer.Server [-a <arg>] [-activity_check_interval <arg>]
       [-lh <arg>] [-lp <arg>] [-rh <arg>] [-rp <arg>] [-s <arg>]
       [-sync_interval <arg>] [-time_before_reconnect <arg>]
An ActivityStream Server for Unimelb COMP90015

 -a <arg>                         announce interval in milliseconds

 -lh <arg>                        local hostname
 -lp <arg>                        local port number
 -rh <arg>                        remote hostname
 -rp <arg>                        remote port number
 -s <arg>                         secret for the server to use
 -sync_interval <arg>             Provide the interval (in milliseconds,
                                  5000 by default) to sync data amoung
                                  servers.
 -time_before_reconnect <arg>     Provide the time (in milliseconds, 0 by
                                  default) to wait before reconnect if a
                                  server crashes, mainly for testing
                                  eventually consistancy
 -activity_check_interval <arg>   Provide the interval (in milliseconds,
                                  1000 by default) to check whether there
                                  is new activity coming in.
```

#### Client startup

```bash
usage: ActivityStreamer.Client [-rh <arg>] [-rp <arg>] [-s
       <arg>] [-u <arg>]
An ActivityStream Client for Unimelb COMP90015
 -rh <arg>   remote hostname
 -rp <arg>   remote port number
 -s  <arg>   secret for username, if not provided, run "register" process
 -u  <arg>   username, if not provided, login as "anonymous".
```



## Test Scenario 

Per projectspecification, this system is supposed to achieve following functions:

- High Availability: system can reconnect automaticallyafter network partition
- Clients can join (register/login) and leave (logout)the network at any time, Servers can join the network at any time 
- Unique Register: a given username can only beregistered once over the server network

-  Message ensure: a message sent by a client can reachall clients that are connected to the network at the time

- Message order: all activity messages sent by a clientare delivered in the same order at each receiving client
- Load balancing: clients are evenly distributed overthe servers

Our implementationfor delivering activity to clients is synchronous, so you may need to wait aperiod of time before you can actual receive an activity, default period is 1second.

Six scenarios havebeen designed for test case.

**NOTE: All test screenshot shown below are using our new version client which is more useful for debug. If you want to test the last version client(client of project1) , change the command to user `ActivityStreamerClient-old.jar` as the client jar package. The old version also passed all these test cases.**

#### High Available

##### Test Case

1. Start 4 servers

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc
java -jar ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
java -jar ActivityStreamerServer.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001
java -jar ActivityStreamerServer.jar -lh localhost -lp 8006 -s abc -rh localhost -rp 8001
```

2. Force quit server 8001

Click ***Close*** icon in *<u>server UI</u>* or press ***CTRL+C*** in *<u>command line</u>*

##### Expected Result

After that you will see server 8002, 8003, 8006 will automatically connected. The picture shows a successful situation (the one, 8002, that takes 8001's place may vary).

![hight_level_design](Project2-Document/pictures/test_case_ha.png)

##### Screentshot:

After 4 servers were started:

![](Project2-Document/pictures/test-screenshot/ha-1.png)

After force quit server 8001:

![image-20180523111014339](Project2-Document/pictures/reconnect_ui_result.png)

##### Testing Result:

As expected.



#### Client can join and leave any time

##### Test case

1. Start the very first server

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc
```

2. Register a user at 8001 and ***remember*** its secret

```bash
java -jar ActivityStreamerClient.jar -u user1 -rp 8001 -rh localhost
```

3. Quit client of last step (close GUI or press CTRL+C in terminal)
4. Start a new server and connect it to 8001

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
```

5. Login user1 at new server 8002 (replace `$secret` by actual secret)

```bash
java -jar ActivityStreamerClient.jar -u user1 -s $secret -rp 8002 -rh localhost 
```

##### Expected Result

User1 should login on new server 8002 successfully, and all data of 8002 should be consistent with 8001.

##### Screentshot

Register success and auto login with given secret

![client-leave-join-1](Project2-Document/pictures/test-screenshot/client-leave-join-1.png)



User1 relogin on 8002 (user1 login successfully, 8001 and 8002 is consistent)

![client-leave-join-2](Project2-Document/pictures/test-screenshot/client-leave-join-2.png)

##### Testing Result

Result as expected.



#### Server can join at any time

##### Test case

1. start the very first server

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc
```

2. register a user at this server and remember its secret.

```bash
java -jar ActivityStreamerClient.jar -u user1 -rp 8001 -rh localhost
```

3. Quit client of step 2
4. start a new server connecting to server 8001

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
```

5. Login user1 at the new server (8002) by replace `$secret` of below script

```bash
java -jar ActivityStreamerClient.jar -u user1 -s $secret -rp 8002 -rh localhost
```

##### Expected Result

- user1 should login successfully at new server (8002) and all data of 8002 should be synced with 8001

- From test case [Message ensure](#Message ensure) we can also see that: 

  > user A is online at the time T, when a activity is sent by some other user B and A loses its connection it can receive this message.
  >
  > When user A reconnects to any server of this system, it can also receive this lost message.

##### Screentshot:

Snapshot of register success and auto login with given secret

![new-server-join-1](Project2-Document/pictures/test-screenshot/new-server-join-1.png)

Snapshot of user1 relogin on 8002 (user1 login successfully, 8001 and 8002 is consistent)

![new-server-join-2](Project2-Document/pictures/test-screenshot/new-server-join-2.png)

##### Testing Result

Result as expected.



#### Unique Register

##### Test case

1. start several servers, say 3

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc
java -jar ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
java -jar ActivityStreamerServer.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001
```

1. register user1 at server 8001

```bash
java -jar ActivityStreamerClient.jar -u user1 -rp 8001 -rh localhost
```

1. try to register user1 at another server, say 8002

```bash
java -jar ActivityStreamerClient.jar -u user1 -rp 8002 -rh localhost
```

##### Expected Result

- the registration of step 3 (server 8002) will fail with error like "user already exists".

##### Screentshot

Snapshot of 3 servers’ GUI

![unique-register-1](Project2-Document/pictures/test-screenshot/unique-register-1.png)

Snapshot of error message (user1 already exists in server)

![image-20180526140653990](Project2-Document/pictures/test-screenshot/unique-register-2.png)

##### Testing Result

Result as excepted.



#### Message ensure

##### Test case

In order to simulate message loss case, let us start servers with a parameter to ***delay*** the reconnection function.

1. Start 4 servers with `time_before_reconnect=10000 (10 seconds)` 

```bash
# start the very first server, which will be terminated
java -jar ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc 
# start other servers  
java -jar ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001 -time_before_reconnect 10000
java -jar ActivityStreamerServer.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001 -time_before_reconnect 10000
java -jar ActivityStreamerServer.jar -lh localhost -lp 8006 -s abc -rh localhost -rp 8001 -time_before_reconnect 10000
```

2. Connect 3 clients to 3 different servers

***Note: Please record the secret of user1 for future use***

```bash
# Keept the secret after register successfully
java -jar ActivityStreamerClient.jar -u user1 -rp 8001 -rh localhost 
# you can just run below 2 clients and no need to record their secretrs
java -jar ActivityStreamerClient.jar -u user2 -rp 8002 -rh localhost
java -jar ActivityStreamerClient.jar -u user3 -rp 8003 -rh localhost
```

1. Terminate server 8001 and send a message from user2 within 10 seconds

- Click ***Close*** icon in *<u>server UI</u>* or press ***CTRL+C*** in *<u>command line</u>* (user 1 will lose connection)
- Send message `{"a":1}` from user2.
- Wait for reconnection happens (10 seconds)

4. Reconnect user1 to any working server, let's say 8006

Replace `$secret` of below script with the secret from step 2.

```bash
java -jar ActivityStreamerClient.jar -u user1 -s $secret -rp 8006 -rh localhost
```

##### Expected Result

- user3 will receive the activity of user2 after reconection is done ( about 10 seconds after disconnection)
- user1 will receive the activity of user2 after relogin to server 8006

> user A is online at the time T, when a activity is sent by some other user B and A loses its connection it can receive this message.
>
> When user A reconnects to any server of this system, it can also receive this lost message.

##### Screentshot

clients login on 8001,8002, 8003 respectively

![message-ensure-0](Project2-Document/pictures/test-screenshot/message-ensure-0.png)

clients after reconnection(user1, user2, user3 all received activity from user2)

![message-ensure-1](Project2-Document/pictures/test-screenshot/message-ensure-1.png)

![message-ensure-2](Project2-Document/pictures/test-screenshot/message-ensure-2.png)

##### Testing Result

Result as excepted.



#### Message order

In order to simulate message disorder case, let us use a ***telnet session*** to simulate a ***server***  and make the order checking period a littler longer with `activity_check_interval=10000 `. Fake messages will be broadcasted by the telnet server with a hooker ***"backTime"*** to set the send time of fake messages to be a time in the past.

>  'timeBack' field is a back door used for this kind of testing. If that field exists in an ActivityBroadcast message, then set the `sendTime` of this activity to `currentTimeInMillis() - timeBack`

##### Operations

1. Start 1 server with `activity_check_interval=10000 (10 seconds)` 

```bash
java -jar ActivityStreamerServer.jar -activity_check_interval 10000 -lh localhost -lp 8001 -s abc
```

2. Start a normal client connecting to server 1

```bash
java -jar ActivityStreamerClient.jar -u user1 -rp 8001 -rh localhost
```

3. Start a terminate and using telnet to simulate a client in following steps

- start telent session

```bash
telnet localhost 8001
```

- paste below string to authenticate this "server" with server 8001

```json
{"command":"AUTHENTICATE","serverId":"serverId01","secret":"abc","host":"localhost","port":8002}
```

- Broadcast 2 "fake" activities (**<u>within 10 seconds</u>**) by pasting below 2 string **separately(one by one)** into telnet session to simulate disordered message.

*You can ignore the message telnet session receives. All of them are used by real server to sync data.*

> 

Message 1: a "fake" message that was sent 0 second ago

```json
{"id":0,"activity":{"message_num":2,"authenticated_user":"user2"},"isDelivered":false,"command":"ACTIVITY_BROADCAST","timeBack":0}
```

Message 2: a "fake" message that was sent 10 seconds ago, which is early than preious one.

```json
{"id":0,"activity":{"message_num":1,"authenticated_user":"user2"},"isDelivered":false,"command":"ACTIVITY_BROADCAST","timeBack":10000}
```

##### Expected Result

- After waiting ***10-20*** seconds,  user1 (normal client with GUI) will receive 2 activities in order (message_num=1 first and then message_num=2) separately.


In real server, this order checking period can be relately shorter, like 0.5 or 1 second.

##### Screentshot

Telnet session input (in white, you can ignore other information, they are sync message from server)

The 3rd white block shows the ordered message list, in which the first is the one with message_num=1

![image-20180526104432677](Project2-Document/pictures/test-screenshot/message-order-0.png)



Messages user1 received  (message_num1 is before message_num 2)

![message-order-2](Project2-Document/pictures/test-screenshot/message-order-2.png)

##### Testing Result

Result as excepted.



#### Load balancing

##### Operations

1. start 2 servers 

```bash
java -jar ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc
java -jar ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
```

2. Register and login 2 clients both to server 8001

```bash
java -jar ActivityStreamerClient.jar -u user1 -rp 8001 -rh localhost
java -jar ActivityStreamerClient.jar -u user2 -rp 8001 -rh localhost
```

##### Expected Result

- user2 will be redirected to server 8002


##### Screentshot

Starting 2 servers

![load-balancing-1](Project2-Document/pictures/test-screenshot/load-balancing-1.png)

After two clients login(load of each server has been changed to 1)

![load-balancing-2](Project2-Document/pictures/test-screenshot/load-balancing-2.png)

##### Testing Result

Result as excepted.