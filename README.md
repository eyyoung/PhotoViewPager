PhotoViewPager
==============

 

最新版本：

[Nexus仓库Latest
Version](<http://nexus.sdp.nd/nexus/#nexus-search;gav~com.nd.android.sdp.common~photoview~~~>)

 

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

![](<http://git.sdp.nd/im-component/photoviewpager/raw/origin_pic/demo/demo.gif>)

 

Gradle依赖

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
compile("com.nd.android.sdp.common:photoview:0.x.x") {
    transitive = true
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

本库需提供磁盘缓存规则，如使用Universal-ImageLoader库，请依赖

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
compile("com.nd.android.sdp.common:photoview-iml:1.0.140") {
    transitive = true
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PhotoViewPagerFragment#setOnViewCreatedListener
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

监听界面创建完成，其中回调的参数view为RelativeLayout，可在其上添加自定义元素（按钮）

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
OnViewCreatedListener#onViewCreated(View view)
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

 

本库依赖：
----------

-   [GIF动态库](<https://github.com/koral--/android-gif-drawable>)

-   RxJava

-   <https://github.com/rahatarmanahmed/CircularProgressView>

-   OKHTTP

-   <https://github.com/davemorrissey/subsampling-scale-image-view> (代码内置)