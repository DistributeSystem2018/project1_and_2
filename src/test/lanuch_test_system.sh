../../build.sh

nohup java -jar  ../../ActivityStreamerServer.jar -lh localhost -lp 8001 -s abc > ../../log/8001.log &
sleep 3
nohup java -jar  ../../ActivityStreamerServer.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001 > ../../log/8002.log 2>&1 &
sleep 3
nohup java -jar  ../../ActivityStreamerServer.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001 > ../../log/8003.log 2>&1 &
sleep 3
nohup java -jar  ../../ActivityStreamerServer.jar -lh localhost -lp 8004 -s abc -rh localhost -rp 8002 > ../../log/8004.log 2>&1 &
sleep 3
nohup java -jar  ../../ActivityStreamerServer.jar -lh localhost -lp 8005 -s abc -rh localhost -rp 8004 > ../../log/8005.log 2>&1 &
sleep 3
nohup java -jar  ../../ActivityStreamerServer.jar -lh localhost -lp 8006 -s abc -rh localhost -rp 8004 > ../../log/8006.log 2>&1 &
sleep 3

#register client
rm -f secret_map.csv
nohup java -jar  ../../ActivityStreamerClient.jar -u yirupan -rp 8001 -rh localhost > ../../log/yirupan.log 2>&1 &
nohup java -jar  ../../ActivityStreamerClient.jar -u wenyizhao -rp 8002 -rh localhost > ../../log/wenyizhao.log 2>&1 &
nohup java -jar  ../../ActivityStreamerClient.jar -u nanangu -rp 8004 -rh localhost > ../../log/nanangu.log 2>&1 &
nohup java -jar  ../../ActivityStreamerClient.jar -u ningk -rp 8003 -rh localhost > ../../log/ningk.log 2>&1 &
