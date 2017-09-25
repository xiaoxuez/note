## kafka 消费者自行存储offset

基于kafka-client0.11.0.0



### 操作

```    
 createSqlHandler();
 kafkaConsumer = new KafkaConsumer<>(kafkaSpoutConfig.getKakfaConfig().getKafkaProps(),
             kafkaSpoutConfig.getKakfaConfig().getKeyDeserializer(), kafkaSpoutConfig.getKakfaConfig().getValueDeserializer());
 
//订阅,同consumer.subscribe(topic, listerner)，当订阅成功，会回调listerner中onPartitionsAssigned方法，分区的offset会在那里seek
kafkaSpoutConfig.getKakfaConfig().getSubscription().subscribe(kafkaConsumer, new KafkaSpoutConsumerRebalanceListener(), context);
 
 
 .....
      
 //消费数据   
 final Set<TopicPartition> assignments = kafkaConsumer.assignment();
        Map<String,Map<Integer, Long>> topicOffsets = new HashMap<>();

        if (assignments == null || assignments.size() !=  KafkaTridentSpoutTopicPartitionRegistry.INSTANCE.getTopicPartitions().size()) {
            LOG.warn("SKIPPING processing batch , " +
                            "[collector = {}] because assignments is null or assignment not equel initial partitions: [assignments={}], [partitions={}]", tridentCollector, assignments, KafkaTridentSpoutTopicPartitionRegistry.INSTANCE.getTopicPartitions());
        } else {
            try {
                final ConsumerRecords<K, V> records = kafkaConsumer.poll(this.pollTimeoutMs);
                LOG.debug("Polled [{}] records from Kafka. at {}", records.count(), new Date().getTime());

                if (!records.isEmpty()) {
                  //消费数据
                    emitTuples(tridentCollector, records, topicOffsets);
                    // build new metadata
                }
            } finally {
            		//更新消费记录到表中，见表的update方法
                if (kafkaManager.persistent()) {
                    kafkaManager.updateDB(topicOffsets);
                }
            }

			//定期查询消费offset以及消息的offset,当消费者速度跟不上的情况下，选择跳过...
            if (kafkaManager.catchDete() && topicOffsets.size() > 0) {
                final long now = new Date().getTime();
                if (now - intervalToLoggerOffsets > 1000 * 60 * 15) {
                    intervalToLoggerOffsets = now;
                    Map<TopicPartition, Long> offsets = kafkaConsumer.endOffsets(assignments);
                    for (TopicPartition partition: offsets.keySet()) {
                        if (topicOffsets.containsKey(partition.topic()) && topicOffsets.get(partition.topic()).containsKey(partition.partition())) {
                            if (offsets.get(partition) - topicOffsets.get(partition.topic()).get(partition.partition()) > 500) {
                                kafkaConsumer.seek(partition, offsets.get(partition) - 500);
                                LOG_ERR.info("consumer fall behind producer: [topic:{}, partition: {}, consumer_offset: {}, producer offset: {}], seek consumer offset smaller 500 than producer ", partition.topic(), partition.partition(), topicOffsets.get(partition.topic()).get(partition.partition()), offsets.get(partition));
                            }
                        }
                    }

                }
            }

        }
        
        
private class KafkaSpoutConsumerRebalanceListener implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            LOG.info("Partitions revoked. [consumer-group={}, consumer={}, topic-partitions={}]",
                    kafkaSpoutConfig.getKakfaConfig().getConsumerGroupId(), kafkaConsumer, partitions);
            KafkaTridentSpoutTopicPartitionRegistry.INSTANCE.removeAll(partitions);

        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            KafkaTridentSpoutTopicPartitionRegistry.INSTANCE.addAll(partitions);
            LOG.info("Partitions reassignment. [consumer-group={}, consumer={}, topic-partitions={}]",
                    kafkaSpoutConfig.getKakfaConfig().getConsumerGroupId(), kafkaConsumer, partitions);

            if (kafkaSpoutConfig.getKakfaConfig().getFirstPollOffsetStrategy() == KafkaSpoutConfig.FirstPollOffsetStrategy.UNCOMMITTED_LATEST) {
                List<Map<TopicPartition, Long>> lastCommittedOffsetsGroup = kafkaOffsetManager.getLastCommittedOffsets(partitions);
                Map<TopicPartition, Long> lastCommittedOffsets = lastCommittedOffsetsGroup.get(0);
                Map<TopicPartition, Long> committedWithoutOffsets = lastCommittedOffsetsGroup.get(1);

                for (TopicPartition topicParition : lastCommittedOffsets.keySet()
                        ) {
                    kafkaConsumer.seek(topicParition, lastCommittedOffsets.get(topicParition) + 1);
                    LOG.info("Seek offset! [topic-partition={}, offset={}]", topicParition, lastCommittedOffsets.get(topicParition));
                }
                for (TopicPartition topicParition : committedWithoutOffsets.keySet()){
                    kafkaConsumer.seekToBeginning(partitions);
                    LOG.info("Seek offset! [topic-partition={}, offset={}]", topicParition, lastCommittedOffsets.get(topicParition));
                }
            }

        }
    }
    

```


### 表

```
	
	 public void create() {
	        String create = "CREATE TABLE IF NOT EXISTS " + KAFKA_OFFSET_TABLE + "(" +
	                "topic varchar(60)," +
	                "consumer_g varchar(60)," +
	                "t_partition int, " +
	                "PRIMARY KEY (topic, consumer_g, t_partition),"+
	                "m_offset int, " +
	                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP )";
	        execString(create);
	    }
    
    
    
	  public void updateCommitedOffsets(String topic, int partition, long offset) {
	        String updateOffsets = "INSERT INTO " + KAFKA_OFFSET_TABLE + " ( topic, consumer_g, t_partition, m_offset) VALUES ( '" +
	                topic + "', '" + consumerGroup + "', " + partition + ", " + offset
	                + ")  on DUPLICATE KEY UPDATE m_offset=" + offset;
	        execString(updateOffsets);
	    }
	    
	    
	  public void updateCommitedOffsets(Map<String,Map<Integer, Long>> topicOffsets) {
        Connection connection = getConnection();
        if (connection != null) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                String updateOffsets = "INSERT INTO " + KAFKA_OFFSET_TABLE + " ( topic, consumer_g, t_partition, m_offset) VALUES ( '%s', '%s', %d, %d) on DUPLICATE KEY UPDATE m_offset=%d;";
                for (String topic : topicOffsets.keySet()) {
                    for (Integer partition: topicOffsets.get(topic).keySet()) {
//                        kafkaOffsetManager.updateCommitedOffsets(topic, partition, topicOffsets.get(topic).get(partition));
                        statement.execute(String.format(updateOffsets, topic, consumerGroup, partition, topicOffsets.get(topic).get(partition), topicOffsets.get(topic).get(partition)));
                    }
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("Cant connect Sql!");
            throw new NullPointerException();
        }
    }   
	    
	    
 /**
     *
     * @param partitions
     * @return  List : 0-> offset; 1-> no offset
     */
    public List<Map<TopicPartition, Long>> getLastCommittedOffsets(Collection<TopicPartition> partitions) {
        Connection connection = getConnection();
        Map<TopicPartition, Long> partitionOffsets = new HashMap<>();
        Map<TopicPartition, Long> partitionWithoutOffsets = new HashMap<>();
        List<Map<TopicPartition, Long>> group = new ArrayList<>();
        if (connection != null) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                String queryOffset = "select m_offset from " + KAFKA_OFFSET_TABLE + " where topic='%s' and consumer_g='%s' and t_partition=%d;";

                for (TopicPartition partition: partitions
                        ) {
                    ResultSet offset = statement.executeQuery(String.format(queryOffset, partition.topic(), consumerGroup, partition.partition()));
                    TopicPartition partition1 = new TopicPartition(partition.topic(), partition.partition());
                    if (offset.next()) {
                        partitionOffsets.put(partition1, offset.getLong(1));
                    } else {
                        partitionWithoutOffsets.put(partition1, 0l);
                    }
                    offset.close();
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("Cant connect Sql!");
            throw new NullPointerException();
        }
        group.add(partitionOffsets);
        group.add(partitionWithoutOffsets);
        return  group;
    }
```


