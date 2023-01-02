## Android富文本编辑器学习总结



[TOC]

### ImageSpan点击事件

有两种实现方式

- 附加ClickableSpan（ClickableSpan是一个抽象类）
- 重写组件的onTouchEvent() 

Span that replaces the text it's attached to with a `Drawable` that can be aligned with the bottom or with the baseline of the surrounding text. The drawable can be constructed from varied sources:

- `Bitmap` - see `ImageSpan(android.content.Context, android.graphics.Bitmap)` and `ImageSpan(android.content.Context, android.graphics.Bitmap, int)`
- `Drawable` - see `ImageSpan(android.graphics.drawable.Drawable, int)`
- resource id - see `ImageSpan(android.content.Context, int, int)`
- `Uri` - see `ImageSpan(android.content.Context, android.net.Uri, int)`

The default value for the vertical alignment is `DynamicDrawableSpan#ALIGN_BOTTOM`



### 图片的对齐方式

图片与文字的位置关系需要通过设置 `ImageSpan` 的对齐方式来实现。通过在 `ImageSpan` 的构造方法中传入 `verticalAlignment` 参数可以实现对图片与文字的纵向对齐方式的设置。`ImageSpan` 为开发者提供了两种对齐方式：

1. `ALIGN_BOTTOM`：图片底部与所在行底部对齐。
2. `ALIGN_BASELINE`：图片底部与文字基线对齐。



### 图片绘制计算逻辑

```java
  @Override    
  public void draw(Canvas canvas, CharSequence text, int start, int end,                                    float x, int top, int y, int bottom, Paint paint) {        
      Drawable b = getDrawable();        
      Paint.FontMetricsInt fm = paint.getFontMetricsInt();        
      int transY = (y + fm.descent + y + fm.ascent) / 2 - (b.getBounds().bottom + b.getBounds().top) / 2;        
      canvas.save();        
      canvas.translate(x, transY);        
      b.draw(canvas);        
      canvas.restore();
  }
```

在 `DynamicDrawableSpan` 的 `draw` 方法中实现自己的绘制逻辑，使用该方案来实现图文混排给予了开发者极大的自由度，开发者可以对与文字混排的图片进行更为细致的排版。





### Bitmap

在bitmap上以X，Y坐标(左上角)为起点，而宽与高则是width与height（右下角 ）开始截图

> Bitmap viewBitmap=bitmap.createBitmap(bitmap,x,y,width,height);
>
> 注意：必须x+width要小于或等于bitmap.getWidth()，y+height要小于或等于bitmap.getHeight() 
>
> Bitmap 通过matrix 矩阵变换生成新的Bitmap   以下两个缩放的例子
>
> Bitmap matrixBitmap=bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),  matrix, true);







### EditText的监听接口：TextWatcher

- 输入文字前置处理 `beforeTextChanged`
- 在文本改变过程中处理 `onTextChanged`
- 在文本内容已经改变之后处理 `afterTextChanged`





### setLineSpacing()

原型为public void setLineSpacing(float add, float mult); 
参数add：增加的间距数值，对应android:lineSpacingExtra参数。 
参数mult：增加的间距倍数，对应android:lineSpacingMultiplier参数。

最终结果：原行间距 x mult+add 

tv.setLineSpacing(0, 1f); ————————> getLineHeight() ： 57

tv.setLineSpacing(0, 2f); ————————> getLineHeight() ： 114

tv.setLineSpacing(6, 2f); ————————> getLineHeight() ： 120

tv.setLineSpacing(2, 1.5f); ————————> getLineHeight() ： 88



## Glide加载图片和直接从文件中读取有什么不同？

- 从文件中读取图片文件

  ```java
  public BitmapDrawable(Resources res, String filepath) {
          Bitmap bitmap = null;
          try (FileInputStream stream = new FileInputStream(filepath)) {
              bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(res, stream),
                      (decoder, info, src) -> {
                  decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
              });
          } catch (Exception e) {
              /*  do nothing. This matches the behavior of BitmapFactory.decodeFile()
                  If the exception happened on decode, mBitmapState.mBitmap will be null.
              */
          } finally {
              init(new BitmapState(bitmap), res);
              if (mBitmapState.mBitmap == null) {
                  android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
              }
          }
      }
  ```

- 使用Glide读取图片文件

  ```java
  BitmapTarget target = new BitmapTarget();
  Glide.with(context)
       .asBitmap()
       .load(source)
       .into(target);
  ```

  



## Drawable

需要初始化边框大小，否则图片不会被渲染在界面上。

```java
int width = drawable.getIntrinsicWidth();
int height = drawable.getIntrinsicHeight();
drawable.setBounds(0, 0, width, height);
```





## Bitmap转换成Drawable

```java
// 获取颜色格式
Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE 
    ? Bitmap.Config.ARGB_8888
    : Bitmap.Config.RGB_565;
// 创建bitmap
Bitmap bitmap = Bitmap.createBitmap(width, heigh, config);
// 创建bitmap画布
Canvas canvas = new Canvas(bitmap);
// 将drawable 内容画到画布中
drawable.draw(canvas);
```





## 对图片进行缩放操作，使图片宽度自适应，高度成比例缩放

```java
int width = bitmap.getWidth();
int height = bitmap.getHeight();

Matrix matrix = new Matrix();
float scale = ((float)getWidth() / width);
matrix.postScale(scale, scale);

Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
BitmapDrawable drawable = new BitmapDrawable(getResources(), newBitmap);
drawable.setBounds(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
```





## InputConnection的作用

InputConnection接口是接收输入的应用程序与InputMethod间的通讯通道。它可以完成以下功能，如读取光标周围的文本，向文本框提交文本，向应用程序提交原始按键事件。InputConnection有几个关键方法，通过重写这几个方法，我们基本可以拦截软键盘的所有输入和点击事件。

- boolean commitText(CharSequence text, int newCursorPosition);

  当输入法输入了字符，包括表情，字母、文字、数字和符号等内容，会回调该方法。

  

- boolean sendKeyEvent(KeyEvent event);

  当有按键输入时，该方法会被回调。比如点击退格键时，搜狗输入法应该就是通过调用该方法，发送keyEvent的，但谷歌输入法却不会调用该方法，而是调用deleteSurroundingText方法。
  
- boolean deleteSurroundingText(int beforeLength, int afterLength);

  当有文本删除操作时（剪切，点击退格键），会触发该方法。
  
- boolean finishComposingText();

  结束组合文本输入的时候，回调该方法。
  

## 如何使用InputConnection?
- 实现了一个 InputConnection子类。

- 与EditText和输入法建立连接。
  

## 该如何传递给EditText使用呢?
与EditText和输入法建立连接时，EditText的onCreateInputConnection()方法会被触发。当输入法要和指定View建立连接的时候，系统会通过该方法返回一个InputConnection实例给输入法。所以我们要复写EditText的这个方法，返回我们自己的InputConnection。但实际上EditText的父类TextView已经复写该方法了，并返回了一个 EditableInputConnection 实例，这个类是隐藏的，而且是专门用来连接文本框和输入法的，如果我们要复写一个InputConnection，那么就要完完全全地把EditableInputConnection 功能给照搬下来，否则EditText功能无法正常使用，这成本太高了而且也不好维护。


所幸 android 提供了InputConnection 的代理类InputConnectionWrapper类。







## 商业软件中常见的修饰词

| 描述方式     | 说明   | 含义                                                        |
| ------------ | ------ | ----------------------------------------------------------- |
| Snapshot     | 快照版 | 尚不稳定、尚处于开发中的版本                                |
| Alpha        | 内部版 | 严重缺陷基本完成修正并通过复测，但需要完整的功能测试        |
| Beta         | 测试版 | 相对Alpha版有很大的改进，消除了严重的错误，但还存在一些缺陷 |
| RC           | 终测版 | Release Candidate（最终测试），即将作为正式版发布           |
| Demo         | 演示版 | 只集成了正式版部分功能，无法升级                            |
| SP           | SP1    | 是Service Pack的意思，表示升级包，相信大家在windows中都见过 |
| Release      | 稳定版 | 功能相对稳定，可以对外发行，但有时间限制                    |
| Trial        | 试用版 | 试用版，仅对部分用户发行                                    |
| Full Version | 完整版 | 即正式版，已发布                                            |
| Unregistered | 未注册 | 有功能或时间限制的版本                                      |
| Standard     | 标准版 | 能满足正常使用的功能的版本                                  |
| Lite         | 精简版 | 只含有正式版的核心功能                                      |
| Enhance      | 增强版 | 正式版，功能优化的版本                                      |
| Ultimate     | 旗舰版 | 标配版本的升级，体验更好                                    |
| Professiona  | 专业版 | 针对要求更高、专业性更强的使用群体发行的版本                |
| Free         | 自由版 | 自由免费使用的版本                                          |
| Upgrade      | 升级版 | 有功能增强或修复了已知缺陷                                  |
| Retail       | 零售版 | 单独发售                                                    |
| Cardware     | 共享版 | 公用许可证（iOS签证）                                       |
| LTS          | 维护版 | 该版本需要长期维护                                          |



### Spring版本命名规则

| 描述方式 | 说明     | 含义                                                         |
| -------- | -------- | ------------------------------------------------------------ |
| Snapshot | 快照版   | 尚不稳定、尚处于开发中的版本                                 |
| Release  | 稳定版   | 功能相对稳定，可以对外发行，但有时间限制                     |
| GA       | 正式版   | 代表广泛可用的稳定版（General Availability）                 |
| M        | 里程碑版 | 具有一些全新的功能或具有里程碑意义的版本（M是Milestone的意思） |
| RC       | 终测版   | Release Candidate（最终测试），即将作为正式版发布            |



