运行transform_total即可
会将云笔记数据转换为myscript数据
顺便会将图片渲染出来


```可以写注释
输入：/cloudpen/
输出：/output/
运行环境：python3.6
```

运行完transform_total之后，把output文件夹复制到another\interactive-ink-additional-examples-android\java\samples\batch-mode\src\main\assets\conf\pointerEvents\text\zh_CN\目录下，运行batch-mode即可

运行之后的结果保存在Android/data/com.myscript目录下

********************************************
邓冰峰前辈所在组必读：
请使用transform_specify.py进行转换，这个脚本会将你们产生的行划分后的文件转换成myscript API输入格式文件

将output_specify文件夹下的结果放到another\interactive-ink-additional-examples-android\java\samples\batch-mode\src\main\assets\conf\pointerEvents\text\zh_CN\目录下，改名为output，运行安卓程序