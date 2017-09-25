## timeline源码解读

在路径..kibana-5.4.2/src/core_plugins/timelion下为timeline的源码。

### fit-functitons

fit方法有average,carry,nearest,scale几种。

#### average

average的方法参数有2，dataTuples, targetTuples。前者为数据数据，后者为目标数据，数据结构都为[[time,value],[..]],目标数据的设定为以时间分的桶，传入时数据为[time,null]..

方法作用一，遍历时间桶，取数据中有在当前桶时间的范围内的数据，求平均值，若桶内无数据，则记为NaN,结果记为resultValues，随后再进行NaN处理，将resultValues中所有NaN的值都取代为值，取值的函数为	取前一次有值的数，与当前的数的差值 除以 连续NaN的个数+1，得出这期间的增长率，再依次给其中连续的nan的值赋值为前一次值+增长率。最后resultValues与目标数据中取出来的时间桶组成返回值。

求平均值部分的代码如

```
  while (i < dataTuplesQueue.length && dataTuplesQueue[i][0] <= time) {
      avgSet.push(dataTuplesQueue[i][1]);
      i++;
    }
    dataTuplesQueue.splice(0, i);

    const sum = _lodash2.default.reduce(avgSet, function (sum, num) {
      return sum + num;
    }, 0);
    return avgSet.length ? sum / avgSet.length : NaN;
```


#### carry

方法参数有2，dataTuples, targetTuples。要求dataTuples的长度大于targetTuples的，即原本数据的时间桶分布密集。作用是在targetTuples时间桶间，返回dataTuples时间桶中的值。targetTuples的长度是小于dataTuples的，从算法上来看dataTuples多出去了的就没了...


#### nearest

时间桶间隔，取离自己最近的一个桶的值为值。

#### 结论

通过上述代码可见，fit方法提供的主要是对已分好的数据桶进行再加工，传入参数为原有数据桶，和 目标桶，目标桶提供了新的时间桶。

#### 测试新方法

在fit中添加一个新方法，为某个时间桶无值，则保持上一次的值。

```
'use strict';

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// bug: 没考虑没值的情况，就是dataTuples为[].
module.exports = function (dataTuples, targetTuples) {
  return _lodash2.default.map(targetTuples, function (bucket) {
    const time = bucket[0];
    let i = 0;
    while (i < dataTuples.length - 1 && dataTuples[i + 1][0] < time) {
        i++;
    }
    const closest = dataTuples[i];
    dataTuples.splice(0, i);
    return [bucket[0], closest[1]];
  });
};
```
目前是添加到源码的fit_functions文件夹下，重启服务即可生效。

#### Question

以fit为例，timeline提供的其余处理数据的方法，如，mutilply等都是在原有数据桶进行操作，原有数据桶是通过.es生成，如何能控制原有数据桶呢？
