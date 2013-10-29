#!/bin/bash

number=`expr $RANDOM % 40 + 1`
postfix=$(printf "%02d.ser" $number)
echo $postfix

