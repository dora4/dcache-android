# dcache-android![Release](https://jitpack.io/v/dora4/dcache-android.svg)

dcache是一个开源的Android离线数据缓存框架，旨在提供一种简单而高效的方式来缓存网络请求的结果和其他数据，以便在手机没有网络的时候使用历史缓存数据。它可以用于缓存各种类型的数据，包括字符串、JSON、图片、音频和视频等。以下是dcache的主要特点：

简单易用：dcache提供了简单易用的API，使得缓存数据变得非常容易。您只需几行代码即可将数据缓存到本地，而不需要处理复杂的缓存逻辑。

可配置性强：dcache提供了丰富的配置选项，您可以根据自己的需求来调整缓存所使用的orm框架、缓存路径等参数。同时，您也可以自定义缓存策略，从而满足不同的业务需求。

支持扩展：dcache可以轻松地与其他库和框架集成，例如OkHttp、Retrofit等。如果您需要更高级的功能，例如缓存加密和压缩等，您也可以轻松地扩展dcache以满足自己的需求。

总之，dcache是一个简单易用、可配置性强、支持扩展的Android离线数据缓存框架，非常适合用于各种Android应用程序中。

For instructions on using dcache-android library into your existing applications, see https://github.com/dora4/DoraCacheSample .









### 开发前的准备

#### 开发环境

Android Studio、Gradle

#### 需要具备的技能

SQLite数据库和Android网络数据请求相关的基础知识

#### gradle依赖配置

```groovy
maven { url 'https://jitpack.io' }

def latest_version = 'x.x.x'

api "com.github.dora4:dcache-android:$latest_version"
```
