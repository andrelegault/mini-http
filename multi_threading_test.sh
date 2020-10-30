#!/bin/bash
PATH_TO_JAR="httpc/target/httpc-1.0-SNAPSHOT-jar-with-dependencies.jar"
GET_OPTIONS="get -v http://localhost:8080/data.txt"
POST_OPTIONS="post -v -f /home/void/mini-http/httpfs/DATA/data3.pdf http://localhost:8080/threading.pdf"

# trap bashtrap int

usage() {
	echo "Usage: $0 [-c <positive int>]" 1>&2;
	exit 1;
}

test_get() {
	GET_COMMAND="java -jar ${PATH_TO_JAR} ${GET_OPTIONS}"

	echo "Testing multiple readers"
	local i=0
	while [[ "$i" -lt "${CLIENT_NUM}" ]]
	do
		eval $GET_COMMAND &
		((i++))
	done
}

test_post() {
	POST_COMMAND="java -jar ${PATH_TO_JAR} ${POST_OPTIONS}"

	echo "Testing multiple writers"
	echo
	local i=0
	while [[ "$i" -lt "${CLIENT_NUM}" ]]
	do
		eval "java -jar ${PATH_TO_JAR} post -v -d ${i} http://localhost:8080/threading.txt &"
		((i++))
	done
}

CLIENT_NUM=5

while getopts "c:" option
do
	case "${option}" in
		c)
			CLIENT_NUM=${OPTARG}
			if [[ "$CLIENT_NUM" -lt "1" ]]; then
				usage
			fi
			;;
		*)
			echo "Using default client num with value of 5"
			;;
	esac
done


if [[ "$CLIENT_NUM" -eq "0" ]]; then
	usage
fi

#test_get

#echo "Sleeping for 2 seconds..."
#sleep 2

test_post

bashtrap() {
	echo "CTRL+C detected... exiting!"
}
