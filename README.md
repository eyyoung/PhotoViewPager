PhotoViewPager
==============

 

本库提供看大图功能，主要功能：

-   图片打开动画

-   下载图片

-   下载进度

-   缩略图，大图，原图展示

-   支持超大图片展示

-   支持GIF动态图片

-   支持自定义增加界面新功能  

-   支持小视频播放

 

Demo
----

![](<https://github.com/eyyoung/PhotoViewPager/raw/demo/demo.gif>)

 

Gradle依赖
TODO

并可在Application初始化时配置默认的磁盘缓存与内存缓存方案规则：

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
ImageLoaderIniter.INSTANCE.init();
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

如使用自有的磁盘缓存与内存缓存方案规则，请自行在下面方法传递，

看大图界面打开函数：

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerManager#startView(FragmentActivity, //Activity
        ImageView, //ImageView,可为空
        ArrayList<Info>, //图片信息
        int, //默认页面
        Callback, //回调（用于获取界面上对应的控件），可为空
        IPhotoViewPagerConfiguration)//磁盘与内存缓存配置，为空使用全局配置
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

仅获取看大图Fragment
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

PhotoViewPagerManager.getView(java.util.ArrayList<? extends com.nd.android.sdp.common.photoviewpager.pojo.Info>, //图片信息
        int）

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

PhotoViewPagerManager.getView(java.util.ArrayList<? extends com.nd.android.sdp.common.photoviewpager.pojo.Info>, //图片信息
        int, //默认页面
        Callback, //回调（用于获取界面上对应的控件），可为空
        IPhotoViewPagerConfiguration) //磁盘与内存缓存配置，为空使用全局配置

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

媒体信息
========

Info类型先支持两种：图片与视频

PicInfo 图片信息

-   url 大图URL（必填）

-   previewUrl 缩略图URL（必填）

-   origUrl 原图URL（选填）

-   size 原图大小（选填）

-   md5值（选填）

 构造方式：

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PicInfo.newBuilder()
       .previewUrl(thumbPath)
       .origUrl(originalPath)
       .url(fullPath)
       .size(pictureKeyMessage.getSize())
       .md5(pictureKeyMessage.getMd5())
       .build();
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

VideoInfo 视频信息

-   thumb 缩略图URL（必填）

-   bigthumb 用于未进行播放时展示的缩略图URL（必填）

-   videoUrl 视频URL（必填）

-   size 视频大小（选填）

-   md5 视频md5值（选填）

构造方式：

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
VideoInfo.newBuilder()
         .videoUrl(originalPath)
         .thumb(thumbPath)
         .bigthumb(fullPath)
         .size(pictureKeyMessage.getSize())
         .md5(pictureKeyMessage.getMd5())
         .build();
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

界面动画Callback
================

startView方法的最后一个callback方法用于获取界面上的ImageView,通过获取当前界面对应的ImageView，内部会将该ImageView的Drawable重用用于预览显示，并实现相应的开启和关闭动画

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Callback#getPreviewView(String previewUrl)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

该回调方法previewUrl为传入的最小级别的url，调用方可通过设置tag的方法对相应的控件进行循环遍历以获取到对应的ImageView

 

界面自定义
==========

支持界面自定义，通过

<h3>Deprecated<h3>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#setOnViewCreatedListener
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
监听界面创建完成，其中回调的参数view为RelativeLayout，可在其上添加自定义元素（按钮）
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
OnViewCreatedListener#onViewCreated(View view)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

v1.2.x中上述方法过期，使用
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#setOnViewCreatedListenerV2
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
OnViewCreatedListenerV2#onViewCreated(RelativeLayout view)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

支持长按事件自定义

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#setOnPictureLongClickListenerV2
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

删除某个页面

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#deletePosition
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

保存当前照片

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#saveCurrentPhoto
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

设置缺省图片

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#setDefaultRes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

本库依赖：
----------

-   [GIF动态库](<https://github.com/koral--/android-gif-drawable>)

-   RxJava

-   <https://github.com/rahatarmanahmed/CircularProgressView>

-   OKHTTP

-   <https://github.com/davemorrissey/subsampling-scale-image-view> (代码内置)
