#!/bin/bash
declare -A map=(
    ["git-url"]=""
    ["targetrepo"]=""
    ["servicecenterip"]=""
    ["username"]=""
    ["password"]=""
)

data=$(echo $CO_DATA |awk '{print}')
for i in ${data[@]}
do
    temp=$(echo $i |awk -F '=' '{print $1}')
    value=$(echo $i |awk -F '=' '{print $2}')
    for key in ${!map[@]}
    do
        if [ "$temp" = "$key" ]
        then
            map[$key]=$value
        fi
    done
done


if [ "" = "${map["git-url"]}" ]
then
    printf "[COUT] Handle input error: %s\n" "git-url"
    printf "[COUT] CO_RESULT = %s\n" "false"
    exit 1
fi

git clone ${map["git-url"]}
if [ "$?" -ne "0" ]
then
    printf "[COUT] CO_RESULT = %s\n" "false"
    exit 1
fi

printf "[COUT] Finish git clone, Begin to Convert program \n"

pdir=`echo ${map["git-url"]} | awk -F '/' '{print $NF}' | awk -F '.' '{print $1}'`

#source path
#git

export GITHUB_USERNAME=${map["username"]}
export GITHUB_PASSWORD=${map["password"]}
export SOURCE_DIR=$pdir

java -jar /root/x2servicecomb-1.0.jar $SOURCE_DIR
if [ "$?" -ne "0" ]
then
    printf "[COUT] CO_RESULT = %s\n" "false"
    exit 1
fi

#tee -a $SOURCE_DIR/Dockerfile <<EOF
#FROM 10.229.40.121:8082/system/openjdk:8-jre-alpine
#
#EXPOSE 8081
#EXPOSE 8888
#
#RUN mkdir -p /maven/project/
#
#COPY ./*.jar  /maven/project/
#
#ENTRYPOINT java $JAVA_OPTS -jar /maven/project/'$JAR_NAME'.jar
#EOF
cd $SOURCE_DIR
git config --global user.email "einstatham@163.com"
git config --global user.name "lijasonvip"
git remote set-url origin "https://${GITHUB_USERNAME}:${GITHUB_PASSWORD}@github.com/lijasonvip/x2servicecomb-result.git"
git add .
git commit -m 'update'
git push -f origin master
#git commit push



