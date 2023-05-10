# dcache-android![Release](https://jitpack.io/v/dora4/dcache-android.svg)

dcache是一个开源的Android离线数据缓存框架，旨在提供一种简单而高效的方式来缓存网络请求的结果和其他数据，以便在手机没有网络的时候使用历史缓存数据。它可以用于缓存各种类型的数据，包括字符串、JSON、图片、音频和视频等。以下是dcache的主要特点：

简单易用：dcache提供了简单易用的API，使得缓存数据变得非常容易。您只需几行代码即可将数据缓存到本地，而不需要处理复杂的缓存逻辑。

可配置性强：dcache提供了丰富的配置选项，您可以根据自己的需求来调整缓存所使用的orm框架、缓存路径等参数。同时，您也可以自定义缓存策略，从而满足不同的业务需求。

支持扩展：dcache可以轻松地与其他库和框架集成，例如OkHttp、Retrofit等。如果您需要更高级的功能，例如缓存加密和压缩等，您也可以轻松地扩展dcache以满足自己的需求。

总之，dcache是一个简单易用、可配置性强、支持扩展的Android离线数据缓存框架，非常适合用于各种Android应用程序中。









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

#### 捐赠虚拟货币支持开源项目

| 链名称            | 钱包地址                                               | 备注                                                         |
| --------------- | ------------------------------------------------------ | ------------------------------------------------------------ |
| USDT(TRC-20链)  | TYVXzqctSSPSTeVPYg7i7qbEtSxwrAJQ5y                     | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币（推荐，转账快且手续费低） |
| ETH(ERC-20链)   | 0x5dA12D7DB7B5D6C8D2Af78723F6dCE4A3C89caB9             | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币，以太坊L1本链的chainId=1，如为以太坊兼容链，请在邮箱中说明，比如bsc的chainId=56，polygon的chainId=137 |
| SOL(Solana链)   | Fra3Ap9JyXo36F9JpPEemq9yXfJrGVjT57GUu7MPG9Dd           | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币 |
| DOGE(狗狗币)    | DJXHQjvgFYMFfcB7vUynz9umfyYe57jBGG                     | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币，打赏专用货币 |
| LUNA(新luna链)  | terra19yvx9q4hap6cy4gv37haw7yx8gzyddfy9q3vlw           | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币 |
| BTC(OMNI链)     | 3K9SGPMhWFCgoiikxahoakzTtPwVWEWAR8                     | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币，转账较慢，安全性极高 |
| ATOM(Cosmos链)  | cosmos1lqevtw9njpg9gte3ujpykjx9tgtkg22kx37zgq          | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币 |
| DOT(Polkadot链) | 14uT8bbKZk98PC4jMzuofK7QreCNYChi4HCF72Lv5CzzqpTN       | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币，转账快，手续费适中 |
| FIL(Filecoin链) | f1cu4o6d64bijlvpdnudl3wki4ckxic7ysckpc37a              | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币，转账慢，但手续费极低 |
| BCH(比特现金)   | bitcoincash:qpfwvmx84nweq4y7k9aq0ra8tq4zttq48ydfau9s4g | 先发送github用户名至邮箱dora924666990@gmail.com再发送加密货币，转账速度适中，手续费很低 |

广告：助记词保管推荐【隐私保险箱】，https://www.pgyer.com/m8WB
