### Server failure model

#####Issues for original system

In the original system, no matter a server quits with or without a quit message(crash), 2 situations will happen:

- If only one server is connected with the quiting server: All clients connected with this server will not work normally which means they cannot use any services of the system unless they connect to a working server again.
- If two or more servers are connected with this quitting server: Despite the effect above, the whole system will be divided into several parts and each one works well as an independent system. But this is not expected as clients in different parts cannot send activity to each other.

##### ![image-20180423215938927](/Users/eric/Documents/projects/DistributedSystem/document/fail_model01.png)

##### How to imporve

- Quit with a message

In order to keep providing services to all exsting servers and clients, a strategy will be applied:

1. If more than one servers are connected with it, then pick one server as a "main" server randomly, let's call it `Server M`.
2. Send a message with below format to all other servers and clients.

```json
{
    "command":"QUIT",
    "new_server_ip":"Server M's IP",
    "new_server_port":"Server M's port"
}
```

3. Servers and clients received this kind of message will redirect themselves to the given server.

With this strategy, the whole system can work well for server quitting with message.

- Crash

In this case, there is no good solutions for client/servers (who directly connect to this crashed server) and servers (who directly connect to this crashed server) to redirect themselves to a working server automatically. 

A defensive strategy that **backuping all relevant data** of running server into a file every, for example, 10-15 minutes can be a very helpful way for recovery this server after crashing.

##### Server Restart

For the original system, the restarted server will not have any user information that **registered before** its restart time. To improve this, a **new type of message** which contains all registered user information need to be replied when a existing server receives an "AUTHENTICATE" :

```json
{
    "command":"USER_INFO",
    "user_info":[
       {"username":"username01","secret":"secret01"},
       {"username":"username02","secret":"secret02"},
       {"username":"username03","secret":"secret03"},
        ...
    ]
}
```

With this message, the new/restarted server can have a copy of all user information which will allow registered user login to this server.

Anf for our solution, once a server is crash, all data (information of users who registered in this server) will be lost. But as the report points above, an improvement plan is made that the program can **backup** all relevant data into a file so that the crashed server can use it to **restore** that kind of data.








​		
​			
​				
any concurrency issues that you identify, be specific with examples

1. broadcast - server load latency - redirect issues 
2. user can login before user register completely successfully(Aaron's solution)
   1. we improve it by ….
   2. but we also have …. two same username register/login from different servers at same time





### Scalability

Aaron's solution: O(n^2)

Our improvement:  (server_count-1) * 2 ~ O(n), then system chain is long, latency maybe little longer.






​			
​		
​	