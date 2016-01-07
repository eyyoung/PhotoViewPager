PhotoViewPager
==============

 

最新版本：0.3.79

 

本库提供看大图功能，主要功能：

-   图片打开动画

-   下载图片

-   下载进度

-   缩略图，大图，原图展示

-   支持超大图片展示

-   支持GIF动态图片

-   支持自定义增加界面新功能  

 

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
compile("com.nd.android.sdp.common:photoview-iml:0.3.73") {
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
PhotoViewPagerManager#start(FragmentActivity, //Activity
        ImageView, //ImageView,可为空
        ArrayList<PicInfo>, //图片信息
        int, //默认页面
        Callback, //回调（用于获取界面上对应的控件），可为空
        IPhotoViewPagerConfiguration)//磁盘与内存缓存配置，为空使用全局配置
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

PicInfo 图片信息

-   url 大图URL

-   previewUrl 缩略图URL

-   origUrl 原图URL

-   size 原图大小

 

本库依赖：
----------

-   [GIF动态库](<https://github.com/koral--/android-gif-drawable>)

-   RxJava

-   <https://github.com/rahatarmanahmed/CircularProgressView>

-   OKHTTP

-   <https://github.com/davemorrissey/subsampling-scale-image-view> (代码内置)
