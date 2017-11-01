### Timeline

+ 创建一条曲线。

```
.es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct')

```


+ 绘两条曲线，offset代表时间间隔,offset=-1h为前一个小时

```
.es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct'), .es(offset=-1h,index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct')

```

+ .label()为曲线添加描述，如两条曲线可分别添加增加可视化。

```
.es(offset=-1h,index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('last hour'), .es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('current hour')

```

+ title()方法为时序图添加标题。使用方法为添加到最后

```
.es(offset=-1h,index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('last hour'), .es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('current hour').title('CPU usage over time')
```

+ .lines为曲线设置appearance，.lines(fill=1,width=0.5)为填充1，宽度为1。默认曲线为填充0宽1

```
.es(offset=-1h,index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('last hour').lines(fill=1,width=0.5), .es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('current hour').title('CPU usage over time')

```

+ .color()为曲线设置颜色，包括其label,如color(gray)，也可直接使用颜色值color(#1E90FF)

```
.es(offset=-1h,index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('last hour').lines(fill=1,width=0.5).color(gray), .es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('current hour').title('CPU usage over time').color(#1E90FF)
```

+ .legend()设置位置和图例的样式。

> For this example, place the legend in the north west position of the visualization with two columns by appending .legend(columns=2, position=nw)

```
.es(offset=-1h,index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('last hour').lines(fill=1,width=0.5).color(gray), .es(index=metricbeat-*, timefield='@timestamp', metric='avg:system.cpu.user.pct').label('current hour').title('CPU usage over time').color(#1E90FF).legend(columns=2, position=nw)

```

+ 使用数学计算

 - max 取最大值, 使用在metric中，如metric=max:system.network.in.bytes
 - derivative 取导数， .es返回对象的方法
 - multiply 乘法， 前面的应该为数字序列
 - divide 除法，前面的应该为数字序列
 
 ```
 .es(index=metricbeat*, timefield=@timestamp, metric=max:system.network.in.bytes).derivative().divide(1048576).lines(fill=2, width=1).color(green).label("Inbound traffic").title("Network traffic (MB/s)"), .es(index=metricbeat*, timefield=@timestamp, metric=max:system.network.out.bytes).derivative().multiply(-1).divide(1048576).lines(fill=2, width=1).color(blue).label("Outbound traffic").legend(columns=2, position=nw)

 ```
 //这个示例，是绘制出入网流，关心的是变化率，所以取导数，bytes to megabytes单位换算所以除以1024*1024，再则，出相对于入，在一副图中显示为增强视图感，便一个的值>0,一个<0来表示，故出的线乘-1
 
 
+ 使用条件 if ()， 参数为(eq/ne.. , value, then do, else do)//then do、else do没有的就写null

 - eq ==
 - ne !=
 - lt <
 - lte <=
 - gt >
 - gte >=

 ```
 .es(index=metricbeat-*, timefield='@timestamp', metric='max:system.memory.actual.used.bytes'), .es(index=metricbeat-*, timefield='@timestamp', metric='max:system.memory.actual.used.bytes').if(gt,12500000000,.es(index=metricbeat-*, timefield='@timestamp', metric='max:system.memory.actual.used.bytes'),null).label('warning').color('#FFCC11'), .es(index=metricbeat-*, timefield='@timestamp', metric='max:system.memory.actual.used.bytes').if(gt,15000000000,.es(index=metricbeat-*, timefield='@timestamp', ='max:system.memory.actual.used.bytes'),null).label('severe').color('red')
 ```
 //这个示例呢，是大于12500000000的绘制一种，大于15000000000绘制一种，看得出来if必须作为.es返回对象的方法，故有两种不同判断就得写两个相同的.es。
+ 趋势，取数据个数的窗口的平均值连线。对于消除时间连续来说是极好的选择

```
.es().if(lt, 500, null).if(gte, 500, 1000)
.es().if(lt, 500, 0, 1000)
```
	
	- mvavg()，如mvavg（10）



+ .bars, .lines, .point改变展现形式的，.point是圆形



```

.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-4d).color(#F5F5F5).label('Four days before today curve').title('user vent volume change curve in 11419'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-3d).color(#DEDEDE).label('Three days before today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-2d).color(#B5B5B5).label('Two days before today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-1d).color(#919191).label('A day before today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value').color(#1E90FF).label('Today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_action', timefield='message_infos.eventTime', metric='avg:message_infos.event.value',offset=-4d).label('').color(#F5F5F5).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_action', timefield='message_infos.eventTime', metric='avg:message_infos.event.value',offset=-3d).label('').color(#DEDEDE).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_action', timefield='message_infos.eventTime', metric='avg:message_infos.event.value',offset=-2d).label('').color(#B5B5B5).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_action', timefield='message_infos.eventTime', metric='avg:message_infos.event.value',offset=-1d).label('').color(#919191).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_action', timefield='message_infos.eventTime', metric='avg:message_infos.event.value').label('').color(#1E90FF).points()

```


```

.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-4d).color(#F5F5F5).label('Four days before today curve').title('user temperature change curve in 11419'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-3d).color(#DEDEDE).label('Three days before today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-2d).color(#B5B5B5).label('Two days before today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value', offset=-1d).color(#919191).label('A day before today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_value', timefield='message_infos.timestamp', metric='avg:message_infos.event.value').color(#1E90FF).label('Today curve'),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_action', timefield='message_infos.eventTime', metric='max:message_infos.event.value',offset=-4d).label('').color(#F5F5F5).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_action', timefield='message_infos.eventTime', metric='max:message_infos.event.value',offset=-3d).label('').color(#CFCFCF).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_action', timefield='message_infos.eventTime', metric='max:message_infos.event.value',offset=-2d).label('').color(#B5B5B5).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_action', timefield='message_infos.eventTime', metric='max:message_infos.event.value',offset=-1d).label('').color(#919191).points(),.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_temp_action', timefield='message_infos.eventTime', metric='max:message_infos.event.value').label('').color(#1E90FF).points(),

```

```
.es(index=storm_*, q='@type:user_perf && message_infos.event.houseId:11419 && message_infos.event.perfType:u_p_vent_action', timefield='message_infos.eventTime', metric='avg:message_infos.event.value').label('')
```


//变化

```
.es(index=storm_*, q='@type:executor && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.sensor.houseId:11419' metric='avg:message_infos.event.aqi.ch2O').label('ch2o').color(red),


.es(index=storm_*, q='@type:executor && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.sensor.houseId:11419' metric='avg:message_infos.event.aqi.co2').label('co2').color(yellow),


.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE' metric='avg:message_infos.event.aqi.pm10').label('pm10').color(green),

.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE' metric='avg:message_infos.event.aqi.pm25').label('pm25').color(blue),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE' metric='avg:message_infos.event.aqi.tvoc').label('tvoc').color(purple),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE' metric='avg:message_infos.event.executor.ventSpeed').multiply(20).label('ventspeed').color(grey).lines(fill=1,width=0.5),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.specificType: FRESH_AIR_VOLUME_MORE' metric='max:message_infos.event.specificAirScore').multiply(10).label('volume_more').color(#A4D3EE).bars(width=3),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.specificType: FRESH_AIR_VOLUME_LESS' metric='max:message_infos.event.specificAirScore').multiply(10).label('volume_less').color(#BCD2EE).bars(width=3),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.specificType: FRESH_AIR_MODE_TO_INDOOR' metric='avg:message_infos.event.executor.ventSpeed').multiply(20).label('mode to indoor').color(#8E388E).points(symbol=triangle),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.specificType: FRESH_AIR_MODE_TO_OUTDOOR' metric='avg:message_infos.event.executor.ventSpeed').multiply(20).label('mode to outdoor').color(#8B4C39).points(symbol=triangle),.es(index=storm_*, q='@type:executor && message_infos.event.sensor.houseId:11419 && message_infos.event.executor.executorType: FRESHAIRMACHINE && message_infos.event.specificType: OUTDOOR_CAUSE_OF_TEMPERATURE' metric='avg:message_infos.event.executor.ventSpeed').multiply(20).label('outdoor of temp').color(#EE82EE).points(symbol=triangle).legend(columns=5, position=nw)
```



```

if(lt,1,if(gt,0,1,if(gt,-1,.es(metric="max:value"),0)), .es(metric="max:value"))


if(gt,0,1).if(eq,1,1)

.es(index=storm_*, q='@type:sensor_before && message_infos.event.houseId:11419',metric='max:message_infos.event.humidityIn').points()
```


