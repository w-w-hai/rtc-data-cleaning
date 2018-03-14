在ETL中的"T"或者是实时流处理中我们经常需要针对每个用户需求开发一个类或者代码块来完成数据清洗的工作

使用这个类库，你只需要写一个JSON配置，就能轻松完成需求，节省大量开发，测试，发布和维护成本

Cleaner的输入是一个String，最终输出是一个JSON。这里借鉴了Logstash里的filter的概念，但这里为两类，decoder和filter。decoder负责将一个String解析成JSON；filter负责将一个JSON转化成另一个(也可以是同一个)JSON，最终形成一个清洗链

[Getting Started](./README.md#user-content-sample-config)

[Sample Config](./README.md#Sample Config)

# Getting Started
* 下载[rtc-data-cleaning-\<version\>.jar](./build/rtc-data-cleaning-0.0.1-SNAPSHOT.jar), 或者将整个项目clone下来mvn package自行编译打包
* 使用非常的简单，代码如下：
```java
String srcData;
JSONObject config;
Cleaner cleaner = Cleaner.create(config);
Result result = cleaner.process(srcData);
System.out.println(result.getPayload());
```
srcData传入需要清洗的数据，config是清洗的配置信息，具体配置见下一章节

# Sample Config
具体用法可以参考[测试代码](./src/test/java/com/sdo/dw/rtc/cleaning/Test.java)
```
{
	"decoder":{
		"type":"json"
	},

	"decoder":{
		"type":"grok",
		"grok_patterns_file":"src\\main\\resources\\mypatterns",
		"grok_patterns":{
			"YEAR":"(?>\\d\\d){1,2}",
			"MONTHNUM":"(?:0?[1-9]|1[0-2])",
			"DATE_CN":"%{YEAR:year}[/-]%{MONTHNUM:monthnum}"
		},
		"grok_entry":"DATE_CN"
	}
	
	"filters":[
		{"type":"rename", "params":{"fields":{"gameId":"game_id","monthnum":"MONTH"}}},
		{"type":"date", "params":{"field":"event_time","source":"yyyyMMdd","target":"yyyy-MM-dd"}},
		{"type":"remove", "params":{"fields":["abc"]}},
		{"type":"keep", "params":{"fields":["messageType","settleTime","ptId"]}},
		{"type":"add", "params":{"fields":{"newf1":"v1","newf2":"v2"}, "preserve_existing":true}},
		{"type":"trim", "params":{"fields":["abc"]}},
		{"type":"replaceall", "params":{"field":"event_time","regex":"abc","repl":"def"}},
		{"type":"underline", "params":{"fields":["abc"]}},
		{"type":"eval", "params":{"field":"new_calc", "expr":"(a/2+ b*5)"}},
		{"type":"bool", "params":{"conditions":" (a!='qq' or b!=123.1) and c !=1"}},
		{"type":"json", "params":{"field":"abc", "discard_existing":false, "preserve_existing":true, "append_prefix":false}},
		{"type":"smiley", "params":{"field":"abc", "discard_existing":false, "preserve_existing":true, "append_prefix":false}},
		{"type":"grok", "params":{"field":"abc", "discard_existing":false, "preserve_existing":true, "append_prefix":false, "entry":"SPLIT_DATA", "patterns":{"SPLIT_DATA":"%{DATA:f1}|%{GREEDYDATA:f2}"}}},
		{"type":"java", "params":{"code_file":"C:\\Users\\xiejing.kane.SNDA\\Desktop\\code","import":["com.google.common.collect.Lists"]}}
	]
}

```

# Decoder
### json
* 描述
```
将source string直接解析为jsonobject
```
* 范例
```
{
	"decoder":"json"
}
```

### grok
* 描述
```
通过正则表达式解析source string为jsonobject,语法参考logstash grok
```
* 参数
```
grok_patterns：正则表达式，默认会加载default_patterns中的所有正则表达式，如果entry已存在，则覆盖
grok_patterns_file：正则表达式文件
grok_entry：正则入口
```
* 范例
```
{
	"decoder":"grok",
	"record_decode_error":true,
	"grok_patterns_file":"src\\main\\resources\\default_patterns",
	"grok_patterns":{
		"YEAR":"(?>\\d\\d){1,2}",
		"MONTHNUM":"(?:0?[1-9]|1[0-2])",
		"DATE_CN":"%{YEAR:year}[/-]%{MONTHNUM:monthnum}"
	},
	"grok_entry":"DATE_CN"
}
```
		
# Filters
### rename
* 描述
```
对key进行重命名
```
* 参数
```
fields：重命名的keys
```
* 范例
```
{"filter":"rename", "params":{"fields":{"gameId":"game_id","monthnum":"MONTH"}}}
```

### remove
* 描述
```
删除指定的key
```
* 参数
```
fields：需要删除的keys
```
* 范例
```
{"filter":"remove", "params":{"fields":["abc"]}}
```

### keep
* 描述
```
与remove相反，保留指定的key，其他的删除
```
* 参数
```
fields：需要保留的keys
```
* 范例
```
{"filter":"keep", "params":{"fields":["messageType","settleTime","ptId"]}}
```		

### underline
* 描述
```
对key进行格式化，由驼峰表达式转为下划线表达式
```
* 参数
```
fields：需要格式化的keys
```
* 范例
```
{"filter":"underline", "params":{"fields":["abc"]}}
```

### iptolong
* 描述
```
将指定的值由ip格式转为long
```
* 参数
```
field: 需要格式化的字段对应的key
newField: 格式化之后写入的新字段的key
```
* 范例
```
{"filter":"remove", "params":{"field":"ip", "newField":"ip_long"}}
```

### add
* 描述
```
添加静态key value
```
* 参数
```
fields：需要添加的kv list
preserve_existing：如果与原有的key冲突，是否保留原有数据
```
* 范例
```
{"filter":"add", "params":{"fields":{"newf1":"v1","newf2":"v2"}, "preserve_existing":true}}
```

### date
* 描述
```
对日志类型的值进行格式化
```
* 参数
```
field：需要格式化的key
source：源格式
target：目标格式
```
* 范例
```
{"filter":"date", "params":{"field":"event_time","source":"yyyyMMdd","target":"yyyy-MM-dd"}}
```		

### trim
* 描述
```
去掉value前后的所有space
```
* 参数
```
需要trim的keys
```
* 范例
```
{"filter":"trim", "params":{"fields":["abc"]}}
```

### replaceall
* 描述
```
对value做正则替换
```
* 参数
```
field：需要做替换的key
regex：需要匹配的正则表达式
repl：用于替换的字符串
```
* 范例
```
{"filter":"replaceall", "params":{"field":"event_time","regex":"abc","repl":"def"}}
```

### bool
* 描述
```
根据条件过滤
```
* 参数
```
conditions：条件表达式，语法同sql的where字句，只支持比较运算符 > >= = != < <= ,空值判断is null，is not null,以及布尔运算符and or
```
* 范例
```
{"filter":"bool", "params":{"conditions":" (a!='qq' or b!=123.1) and c !=1"}}
```

### json
* 描述
```
将某个value解析为json并提取到原数据中
```
* 参数
```
field：要提取的value
discard_existing：是否丢弃原数据
preserve_existing：如果key冲突，是否保留原数据
append_prefix：是否给解析出来的key添加前缀
```
* 范例
```
{"filter":"json", "params":{"field":"abc", "discard_existing":false, "preserve_existing":true, "append_prefix":false}}
```

### split
* 描述
```
将某个value按照分割符拆分
```
* 参数
```
field：要提取的value
discard_existing：是否丢弃原数据
preserve_existing：如果key冲突，是否保留原数据
append_prefix：是否给解析出来的key添加前缀
delimiter:分割符
assigner:赋值符
```
* 范例
```
{"filter":"smiley", "params":{"field":"abc", "discard_existing":false, "preserve_existing":true, "append_prefix":false, "delimiter":",", "assigner":":"}}
```

### grok
* 描述
```
将某个value用正则表达式解析并提取到原数据中
```
* 参数
```
field：要提取的value
discard_existing：是否丢弃原数据
preserve_existing：如果key冲突，是否保留原数据
append_prefix：是否给解析出来的key添加前缀
patterns：用于解析的正则表达式，默认会加载default_patterns中的所有正则表达式，如果entry已存在，则覆盖
entry：正则解析入口
```
* 范例
```
{"filter":"grok", "params":{"field":"abc", "discard_existing":false, "preserve_existing":true, "append_prefix":false, "entry":"SPLIT_DATA", "patterns":{"SPLIT_DATA":"%{DATA:f1}|%{GREEDYDATA:f2}"}}}
```

### eval
* 描述
```
对数据进行重新运算
```
* 参数
```
field：计算结果保存的key
expr：计算表达式
```
* 范例
```
{"filter":"eval", "params":{"field":"new_calc", "expr":"(a/2+ b*5)"}}
```

### java
* 描述
```
使用java代码片段进行数据处理。
```
* 参数
```
code：代码
code_file：代码片段文件，如果同时指定了code和code_file，code优先.注意:代码中不允许出现双斜杠注释
import：代码需要引入的类，如果pom里并没有指定相应的dependency，则需要事先提交给管理员
```
* 范例
```
{"filter":"java", "params":{"code_file":"./code.txt","import":["com.google.common.collect.Lists"]}}
```
