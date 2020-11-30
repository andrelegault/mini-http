#!/bin/bash
PATH_TO_CLIENT_JAR="target/udp-client.jar"
GET_OPTIONS="get http://localhost:8080/ &"
POST_OPTIONS="post -d \"Hello World!\" http://localhost:8080/threading.txt &"
GET_COMMAND="java -jar ${PATH_TO_CLIENT_JAR} ${GET_OPTIONS}"
POST_COMMAND="java -jar ${PATH_TO_CLIENT_JAR} ${POST_OPTIONS}"

usage() {
	echo "Usage: $0 [-c <positive int>]" 1>&2;
	exit 1;
}

test_get() {

	local i=0
	while [[ "$i" -lt "${CLIENT_NUM}" ]]
	do
		eval "${GET_COMMAND}"
		((i++))
	done
}

test_post() {
	local i=0
	while [[ "$i" -lt "${CLIENT_NUM}" ]]
	do
		eval "${POST_COMMAND}"
		((i++))
	done
}

test_get_post() {
	local i=0
	while [[ "$i" -lt "${CLIENT_NUM}" ]]
	do
		eval "${POST_COMMAND}"
		eval "${GET_COMMAND}"
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

echo "Testing multiple reads on same file"
test_get

echo "Sleeping for 1 second..." && sleep 1

echo "Testing multiple writes on same file"
test_post

echo "Sleeping for 1 second..." && sleep 1

echo "Testing multiple read and writes on same file"
test_get_post

bashtrap() {
	echo "CTRL+C detected... exiting and closing all java apps!"
	pkill java && exit(0)
}
