FROM  10.229.40.121:8082/system/maven:localcenter
MAINTAINER wang qilin <qilin.wang@huawei.com>

WORKDIR /root
COPY run.sh /root/run.sh
COPY x2servicecomb-1.0.jar /root/x2servicecomb-1.0.jar
RUN chmod 777 /root/run.sh
RUN chmod 777 /root/x2servicecomb-1.0.jar

CMD bash run.sh