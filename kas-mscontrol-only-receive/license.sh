#!/bin/bash

for i in ` ls licenses/`
do
	echo "Setting license for $i files"
	for j in `find . | grep -e "\.$i"`
	do
		TEMP=/tmp/licensing
		cat licenses/$i > $TEMP
		cat $j >> $TEMP
		echo "Seting license to $j"
		cat $TEMP > $j
	done
done

echo "Finished"
