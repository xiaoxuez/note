#!/usr/bin/env ruby

require 'mysql2'

name=ARGV[2]
jar_path=ARGV[0]
class_file=ARGV[1]
config_path=ARGV[3]

client = Mysql2::Client.new(
    :host     => '127.0.0.1', # 主机
    :username => 'root',      # 用户名
    :password => '',    # 密码
    :database => 'storm',      # 数据库
    :encoding => 'utf8'       # 编码
)
client.query("create table if not exists storm_jar (id int AUTO_INCREMENT NOT NULL PRIMARY KEY, name varchar(40) NOT NULL, jar_path TEXT NOT NULL, class_file varchar(100) not null, config_path text); ")
result = client.query("select name from storm_jar where name='#{name}'")
option_column_config = config_path == nil ? "" : ", config_path='#{config_path}'"
if result.size > 0
   config_path == nil ? client.query("update storm_jar set jar_path='#{jar_path}', class_file='#{class_file}'  where name='#{name}'") : client.query("update storm_jar set jar_path='#{jar_path}', class_file='#{class_file}', config_path='#{config_path}'  where name='#{name}'")
else
  config_path == nil ? client.query("insert into storm_jar set name='#{name}', jar_path='#{jar_path}', class_file='#{class_file}'") : client.query("insert into storm_jar set name='#{name}', jar_path='#{jar_path}', class_file='#{class_file}' '#{option_column_config}'")
end
