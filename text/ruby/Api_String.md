字符串既有单引号的形式，也有双引号的形式，但他们的含义却是截然不同的。单引号，指的是原生字符串，其中不含有转义的规则，例如'\n'不是指换行符，而是指两个字符的字符串。"\n"就是指换行符。另外，双引号的字符串还可以使用#{...}的方式嵌入表达式。如"My name is #{name},"

+ 转换 try_convert(obj) -> String or nil
+ 格式化转换  str % arg -> new_str
+ 运算符：
	1. *: "ho! " * 3 -> "ho! ho! ho!"
	2. +:
	3. <<: 同 +
	4. <=>: 返回 -1, 0, +1, or nil，比较大小。
	5.  ==: 值相等
	6. =~ 正则匹配，返回值为匹配位置或nil
+ 取值s[i], i可为下标，下标+length, 或正则。
+ 判断全为ascii码： ascii_only
+ bytes，转相应bytes数组..byteslice,可截取bytes数组。
+ 首字母大写: capitalize
+ 比较大小，大下写忽略。casecmp返回值与<=>一样
+ 居中center,
+ 替换 gsub, 可匹配正则进行替换

疑问： 字符串分frozen? not frozen?