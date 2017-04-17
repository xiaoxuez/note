	#!/usr/bin/env bash
	#
	# Licensed to the Apache Software Foundation (ASF) under one
	# or more contributor license agreements.  See the NOTICE file
	# distributed with this work for additional information
	# regarding copyright ownership.  The ASF licenses this file
	# to you under the Apache License, Version 2.0 (the
	# "License"); you may not use this file except in compliance
	# with the License.  You may obtain a copy of the License at
	#
	#     http://www.apache.org/licenses/LICENSE-2.0
	#
	# Unless required by applicable law or agreed to in writing, software
	# distributed under the License is distributed on an "AS IS" BASIS,
	# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	# See the License for the specific language governing permissions and
	# limitations under the License.
	#
	
	# Resolve links - $0 may be a softlink
	PRG="${0}"  
	
	#-h是判断PRG是否存在并且是个符号链接
	while [ -h "${PRG}" ]; do
	  ls=`ls -ld "${PRG}"`
	  link=`expr "$ls" : '.*-> \(.*\)$'`
	  if expr "$link" : '/.*' > /dev/null; then
	    PRG="$link"
	  else
	    PRG=`dirname "${PRG}"`/"$link"
	  fi
	done
	
	# find python >= 2.6 -a 文件存在
	if [ -a /usr/bin/python2.6 ]; then
	  PYTHON=/usr/bin/python2.6
	fi
	# -z PYTHON的长度为零则为真
	if [ -z "$PYTHON" ]; then
	  PYTHON=/usr/bin/python
	fi
	
	# check for version 2>&1 是指错误信息重定向到标准输出, awk每接收文件的一行，然后执行相应的命令，来处理文本, cut -d 分割字符， -f 选择第几个字符
	majversion=`$PYTHON -V 2>&1 | awk '{print $2}' | cut -d'.' -f1`
	minversion=`$PYTHON -V 2>&1 | awk '{print $2}' | cut -d'.' -f2`
	numversion=$(( 10 * $majversion + $minversion))
	if (( $numversion < 26 )); then
	  echo "Need python version > 2.6"
	  exit 1
	fi
	
	STORM_BIN_DIR=`dirname ${PRG}`
	export STORM_BASE_DIR=`cd ${STORM_BIN_DIR}/..;pwd`
	
	#check to see if the conf dir is given as an optional argument
	# -gt 大于
	if [ $# -gt 1 ]; then
	  if [ "--config" = "$1" ]; then
	    conf_file=$2
	    if [ ! -f "$conf_file" ]; then
	      echo "Error: Cannot find configuration directory: $conf_file"
	      exit 1
	    fi
	    STORM_CONF_FILE=$conf_file
	    STORM_CONF_DIR=`dirname $conf_file`
	  fi
	fi
	
	export STORM_CONF_DIR="${STORM_CONF_DIR:-$STORM_BASE_DIR/conf}"
	export STORM_CONF_FILE="${STORM_CONF_FILE:-$STORM_BASE_DIR/conf/storm.yaml}"
	
	if [ -f "${STORM_CONF_DIR}/storm-env.sh" ]; then
	  . "${STORM_CONF_DIR}/storm-env.sh"
	fi
	
	exec "$PYTHON" "${STORM_BIN_DIR}/storm.py" "$@"
	
	
+ -h "${PRG}": -h是判断PRG是否存在并且是个符号链接
+ expr命令一般用于整数值，但也可用于字符串,可表示计算(后接+-), 可表示模式匹配(后接:),  如.*意即任何字符重复0次或多次
    
			$value=accounts.doc
			$expr $value : '.*'
			12

+ -a 文件存在
+ -z string, string的长度为0
+ 2>&1 是指错误信息重定向到标准输出
+ awk每接收文件的一行，然后执行相应的命令，来处理文本
+ cut -d 分割字符， -f 选择第几个字符
+ -gt 大于
+ $#输入参数个数
