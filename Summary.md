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

