## logstash_config

组成部分为： input, filter, output
[文档介绍](https://www.elastic.co/guide/en/logstash/current/configuration-file-structure.html)

### value

>A plugin can require that the value for a setting be a certain type, such as boolean, list, or hash. 

+ Array
	  	
	  	users => [ {id => 1, name => bob}, {id => 2, name => jane} ]

+ Lists

	  	path => [ "/var/log/messages", "/var/log/*.log" ]
  		uris => [ "http://elastic.co", "http://example.net" ]
  		
+ Boolean

	 	 ssl_enable => true

+ Bytes

		  my_bytes => "1113"   # 1113 bytes
		  my_bytes => "10MiB"  # 10485760 bytes
		  my_bytes => "100kib" # 102400 bytes
		  my_bytes => "180 mb" # 180000000 bytes

+ Codec
		
		  codec => "json"
		  //更多类型https://www.elastic.co/guide/en/logstash/current/codec-plugins.html

+ Hash
	
		match => {
		  "field1" => "value1"
		  "field2" => "value2"
		  ...
		}
		
+ Number

		port => 33

+ Password
	
		 my_password => "password"

+ URI

		  my_uri => "http://foo:bar@example.net"
+ Path

		  my_path => "/tmp/logstash"
+ String

		  name => "Hello world"
  		  name => 'It\'s a beautiful day'
+ Comments
	
			# this is a comment

			input { # comments can appear at the end of a line, too
			  # ...
			}
###filter

+ mutate

		mutate { replace => { "type" => "file_test" } }
		
+ grok

	>Parses unstructured event data into fields

		grok {
	      match => { "message" => "%{COMBINEDAPACHELOG}" }
	    }
	    
	[patterns的集合](https://github.com/logstash-plugins/logstash-patterns-core/blob/master/patterns/grok-patterns)
			
+ date

		  date {
	  		  match => [ "timestamp" , "dd/MMM/yyyy:HH:mm:ss Z" ]
		  }
		  

+ json
	>Parses JSON events



#####drop example:

	filter {
		    grok {
	            match => ["message","%{TIMESTAMP_ISO8601:logtime} \[%{NUMBER}\] \(\(%{WORD}\)\) %{WORD:loglevel} %{GREEDYDATA:other}"]
	    }
	
	    if [loglevel]!= "ERROR" {
	            drop {}
	    }
	}

### input

[file](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-file.html)


### output

### filter

  + grok
  	 消息解析，消息以按行为单位进行解析。基本格式为
	  	
	  	 grok {
	    match => { "message" => "%{IP:client} %{WORD:method} %{URIPATHPARAM:request} %{NUMBER:bytes} %{NUMBER:duration}" }
	  	}
	  	
	 >Grok sits on top of regular expressions, so any regular expressions are valid in grok as well.
	 
	 其中，可将正则组成pattern，如"%{IP:client}" IP为pattern的类型, client为变量名，解析出来的变量如client可在后面进行使用。    
	 [自定义pattern](https://www.elastic.co/guide/en/logstash/current/plugins-filters-grok.html#_custom_patterns),总结方式就是在某文件下创建pattern解析方式，如
		 
		 # contents of ./patterns/extra:
		JAVA_CLASS ([a-zA-Z]+[.][a-zA-Z]+)[.]*.*
		
再在grok中增加字段patterns\_dir，patterns\_dir为文件夹，非文件。如

	  grok {
		  patterns_dir => ["/Users/xiaoxuez/Library/apache/logstash-5.4.2/patterns"]
		  match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{JAVA_CLASS:producer} %{WORD:printer} %{GREEDYDATA:content}" }
    }
  
    
### config
 config中还可以使用[if条件语句]()，如  

	filter {
	 	...
	 	if ([producer] =~ /[m].*/) { 
	 		# do .. 
		} else {
			 drop { }  #该条消息丢掉 不进入output
		}
   }
   		# =~  为正则匹配运算符， 
   		
   		


EXAMPLE
--

file使用正则匹配
 		
 	 path => "/var/log/%{type}.%{+yyyy.MM.dd.HH}"
 	 
使用变量用[]，如[path]

  # ([a-zA-Z]+[.][a-zA-Z]+)[.]*.*  正则匹配类名
  
      json{
           source => "content"
           target => "content"
       }
  
	   mutate {
		         add_field => {
		           "event_type" => "%{[jsoncontent][type]}"
		           "event_msg" => "%{[jsoncontent][event]}"
		         }
	       }
	
		
		      
    
      #控制打印的
    if ([producer] == "STUDIO" ) {
        mutate { replace => { "type" => "System_Airguru_Log" } }
    } else if ([producer] =~ /[m].*/) { 
        json{
              source => "content"
              target => "jsoncontent"
          }
        mutate {
          replace => { "type" => "Stratege_Airguru_Log" }
          add_field => {
  		           "event_type" => "%{[jsoncontent][type]}"
  		           "event_msg" => "%{[jsoncontent][event]}"
  		         }
          }
    } else {
      drop { }
    }		
   		
   		

### Example

#### filter-aggregate 

	input {
	  file {
	    path => "${PROJECT}/logs/**/**/worker.log"
	    start_position => "beginning"
	  }
	}
	
	filter {
	#  if [path] =~ ".log" {
	    #消息解析，
	    grok {
				id => "storm_log_filter"
	      patterns_dir => ["${PROJECT}/patterns"]
	      #match => { "message" => "%{GREEDYDATA:unknow}" }
	      #match => { "message" => "%{TIMESTAMP_ISO8601:time} %{JAVA_CLASS:producer} %{WORD:printer} %{GREEDYDATA:content}" }
	      match => { "path" => ".*/%{WORD:topology}\-%{JUST_NUMBER:order}\-%{JUST_NUMBER:submittime}/[0-9]+/worker.log" }
	      break_on_match => false
	    }
	
	    aggregate {
	     task_id => "%{topology}-%{order}-%{submittime}"
	     code => "map['counts'] ||= 0; map['counts'] += 1;"
	     push_map_as_event_on_timeout => true
	     timeout_task_id_field => "topology_file"
	     timeout => 360 # 10 minutes timeout
	     timeout_tags => ['_aggregatetimeout']
	   }
	
	
	#  }
	
	}
	
	output {
	  stdout { codec => rubydebug }
	}


#### filter-basic-grok

	input {
	  file {
	    path => "/Users/xiaoxuez/Library/apache/logstash-5.4.2/test/file_test_log.log"
	    start_position => "beginning"
	  }
	}
	
	filter {
	  if [path] =~ ".log" {
	    #消息解析，
	    grok {
	      match => { "message" => "%{TOMCAT_DATESTAMP:time} %{JAVACLASS:producer} %{WORD} %{GREEDYDATA:content}" }
	      break_on_match => false
	    }
	
	
	
	
	
	    # if ([producer] !~ /[m].*/ or "_grokparsefailure" in [tags]) {
	      # drop { }
	  #  }
	
	    mutate {
	      #  remove_field => ["message"]
	    }
	
	
	  }
	
	}
	
	output {
	  #elasticsearch {
	  #  hosts => ["localhost:9200"]
	  #}
	  #输出到 stdout#
	  stdout { codec => rubydebug }
	}


##坑

+ 调式过程中出现解析文件没报错也没任何的东西...

	logstash会监听文件读取信息记录的位置,所以解析过的文件，如果文件内容并没有发生变化(暂且说是内容吧，可能logstash只能监听位置改变而不能识别之前内容有所改变，这个我还未验证过..),那么再次解析的话，将不会解析以前解析过的内容。解决方式是将监听的记录删掉，记录存在于{logstash}/data/plugins/inputs/file/.sincedb*