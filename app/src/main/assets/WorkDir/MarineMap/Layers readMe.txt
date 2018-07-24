MAP NAME: Globle Map   //图幅名称
MAP ORGINAL SCALE: 10000 //图幅原始比例尺
BEGIN
<
LAYER NAME: NAVTEX电台  // 图层名字
ATTRIBUTES COUNT: 2     //一个图层可以有多个属性字段, 这里务必要指定属性的数目
 <
  ATTR NAME: NAME       //第1个属性字段名称
  DATA TYPE: STRING     //属性字段数据类型, 可以是:  INT, FLOAT, STRING
 >
 <
  ATTR NAME: 电台类型   //第2个属性字段名称, 注意这里列举的属性的个数需与上面指定的数目一致.
  DATA TYPE: INT
 >
GEO TYPE: POINT         // 图层的地理类型, 可以是:  POINT, LINE, REGION
GEO STYLE COUNT: 1      //一个图层可以多个 GEO STYLE 叠加起来(一般情况下一个足矣), 这里务必要指定 GEO STYLE 的数目
 <
 CONDITION:             //每个 GEO STYLE 可以有一个条件,针对某个字段的不同取值来制定SYTLE,这样可针对1个图层不同类型对象制定不同STYLE. 但一般都只有1个style条件,为空.
 MINSCALE:              //该GEO STYLE 的最小显示比例尺
 MAXSCALE:              //该GEO STYLE 的最大显示比例尺
 SYMBOL STYLE: 1, RGB(0, 0, 0), 0, 0, 0  // 参数格式为 "符号ID,符号颜色,符号偏移量X,符号偏移量Y,符号旋转角度".
                                         // 如果使用自定义符号, 则符号ID为使用SymbolEditor.exe程序可查看到的符号ID;
                                         // 如果使用S52符号, 则格式为S52符号缩写加旋转角度, 如"RACNSP01,45",参见文件"S57Lib\\S57Symbols"
                                         // "符号偏移量X,符号偏移量Y" 指符号重心与目标显示位置的偏移量, 单位为 Himeter
                                         // "旋转角度"为符号显示顺时针旋转的度数
 TEXT STYLE: "@0","Arial", 400, NORMAL, RGB(0, 0, 0) //显示字符串: "@0"表示显示第0个属性字段的内容; "Arial"表示字体; 400是高度(单位是himeter); NORMAL和BOLD指定是否为粗体; RGB是字体颜色
 >
>
//////////////////////////////////////////////////////////////////
<
LAYER NAME: INMARSAT卫星覆盖范围
ATTRIBUTES COUNT: 1
 <
  ATTR NAME: 覆盖半径
  DATA TYPE: FLOAT
 >
GEO TYPE: LINE
GEO STYLE COUNT: 1
 <
 CONDITION: 
 MINSCALE: 
 MAXSCALE: 
 BASIC STYLE: SOLID, 50, RGB(37,121,106)  //LINE类型的基本线型风格参数格式为 "线型,线宽,颜色", 取值范例为: "SOLID(或者 DOT 或者 DASH), 50(单位Himeter), RGB(37,121,106)"
 CYCLE SYMBOL STYLE: 1,1000                     //参数格式为 "循环符号ID,循环符号距离间隔" 
					        // 如果使用自定义符号, 则符号ID为使用SymbolEditor.exe程序可查看到的符号ID;
                                         	// 如果使用S52符号, 则格式为S52符号缩写加旋转角度, 如"RACNSP01,45",参见文件"S57Lib\\S57Symbols"
      						// "循环符号距离间隔" 指循环符号的距离间隔, 单位为 Himeter
 TEXT STYLE: "@0","Arial", 400, NORMAL, RGB(0, 0, 0) //在LINE的中心线段上显示文字标注,格式同POINT类型
 >
>
//////////////////////////////////////////////////////////////////
<
LAYER NAME: 自定义警戒区
ATTRIBUTES COUNT: 1
 <
  ATTR NAME: 警戒区名
  DATA TYPE: FLOAT
 >
GEO TYPE: REGION
GEO STYLE COUNT: 1
 <
 CONDITION: 
 MINSCALE: 
 MAXSCALE: 
 BASIC STYLE: RGB(255, 0, 0),90  // REGION类型的基本线型风格参数格式为 "填充色,填充透明度", 透明度0~100, 0为全透明,100为全色;
 PATTERN SYMBOL STYLE:           // REGION类型的填充符号风格参数格式为 "填充符号ID,填充符号水平距离间隔,填充符号垂直距离间隔": 
					// 如果使用自定义符号, 则符号ID为使用SymbolEditor.exe程序可查看到的符号ID;
                                        // 如果使用S52符号, 则格式为S52符号缩写加旋转角度, 如"RACNSP01,45",参见文件"S57Lib\\S57Symbols"
      					// "填充符号水平距离间隔" 与 "填充符号垂直距离间隔"指填充符号分别在水平方向和垂直方向上的距离间隔, 单位为 Himeter
 BOUNDARY LINE STYLE: SOLID, 50, RGB(37,121,106) //边界线的现型,格式参数同LINE类型的"BASIC STYLE"参数;
 TEXT STYLE:"@0","Arial", 400, NORMAL, RGB(0, 0, 0)
 >
>
//////////////////////////////////////////////////////////////////
END
