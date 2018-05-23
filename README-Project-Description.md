# Project 1 & 2 - Multi-server Network

## Introduction

This is a multi-server communication network which allows

- Any number of servers join the system (if it has the correct secret of this system).
- Users register to this system with a unique username.
- Users login from **any server** within this network if he/she registered in this system (any server is ok) or he/she uses an **anonymous** user.
- Users send activities to the system and all other online users (include anonymous users) will receive this activities.


## How to start this system

Check [here](src/test/readme_shortcut_lanuch.md) for detail.



## Branches

There are two versions for this software:

#### Project 1: Simple version (branch `Aaron_Solution`)

This version did not all servers to join after the system begins to provide service and not recover strategy is applied when a server crashes.

Check [here](./Project1-Document/Project1.md) to see how this version works.

How to check out this version(branch)

```bash
# if you did clone this reps yet
git clone https://kangnwh.visualstudio.com/DistributedSystem/_git/DistributedSystem
# change to simple version
git checkout Aaron_Solution
```



#### Project 2: Complex version (branch `layered`)

This version improves the design of project 1 to allow server to quit/join at any time and a strategy is applied to recover the system if a server crashes. Data eventually consistency can also be conducted.

Check [here](Project2-Document/Project2.md) to see how this version works.

How to check out this version(branch)

```bash
# if you did clone this reps yet
git clone https://kangnwh.visualstudio.com/DistributedSystem/_git/DistributedSystem
# change to simple version
git checkout layered
```



## Contributors

Ning Kang

Nannan Gu

Yiru Pan

Wenyi Zhao



## Copyright

This is a solution of Distributed System of University of Melbourne(2018).

Refer to the idea of this project is ok but **DO NOT COPY**.