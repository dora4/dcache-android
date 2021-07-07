# DoraCache使用文档![Release](https://jitpack.io/v/dora4/dcache-android.svg)
简介：一个使用在Android平台的数据缓存框架，支持将model数据从后端接口下载后，简单的配置即可自动映射到数据库，并在断网的情况下可以离线读取。



### 开发前的准备

#### 开发环境

Android Studio、gradle

#### 需要具备的技能

SQLite数据库和Android网络数据请求相关的基础知识

#### gradle依赖配置

```groovy
maven { url 'https://jitpack.io' }

def latest_version = '1.0.3'

api "com.github.dora4:dcache-android:$latest_version"
```



### 使用文档

#### 一、dcache的orm详解

1. **配置初始化**

   ```kotlin
   Orm.init(this, OrmConfig.Builder()
                   .database("dcache_sample")
                   .tables(Account::class.java)
                   .version(1)
                   .build())
   ```

   在自定义的Application类的入口加入一行配置，database为数据库名，version从1开始每次递增1，tables用来配置需要初始化的表，dcache中所有的表需要实现OrmTable接口。

2. **注解详解**

   - 表和列相关

     - @Table

       此注解配置在OrmTable的实现类的类名之上，用来指定一个类映射到表的名称

     - @Column

       此注解配置在OrmTable的实现类的成员属性之上，用来指定一个属性映射到字段的名称

     - @Ignore

       此注解的优先级高于@Column，配置在OrmTable的实现类的成员属性之上，配置了此注解的成员属性，不会作为表的字段进行映射

   - 约束相关

     - @NotNull

       此注解配置在OrmTable的实现类的成员属性之上，用来指定这个字段为非空字段

     - @PrimaryKey

       此注解配置在OrmTable的实现类的成员属性之上，用来指定这个字段为表的主键

     - @Id

       此注解配置在OrmTable的实现类的成员属性之上，作用类似于@PrimaryKey，并

       在它的基础上指定了该字段名为”_id“，相当于@PrimaryKey+@Column("\_id")

     - @Unique

       此注解配置在OrmTable的实现类的成员属性之上，表示这个字段的值在这张表中从不重复

     - @Default

       此注解配置在OrmTable的实现类的成员属性之上，通过它可以给字段指定默认值

3. **CRUD操作**

   - 插入数据

     ```kotlin
     DaoFactory.getDao(Account::class.java).insert(Account(generateAccKey(),
                         "D"+generateAccKey(), "P"+generateAccKey()))
     ```

     insert不仅可以被用来插入单条数据，也可以插入一个List数据

   - 删除数据

     ```kotlin
     val selectOne = DaoFactory.getDao(Account::class.java)
                         .selectOne(QueryBuilder.create().orderBy(QueryBuilder.ID))
                 if (selectOne != null) {
                     DaoFactory.getDao(Account::class.java).delete(selectOne)
                 }
     ```

   - 更新数据

     ```kotlin
     DaoFactory.getDao(Account::class.java).update(Account("这个是key",
                         "D"+generateAccKey(), "P"+generateAccKey()))
     ```

   - 查询数据

     - Condition

       selection：where子句，不带where，可以带”？“占位符

       selectionArgs：”？“占位符的所有值

     - WhereBuilder

       where子句的构建类，通过WhereBuilder.create ()创建实例

       ```java
       public WhereBuilder addWhereEqualTo(String column, Object value) {
               return append(null, column + EQUAL_HOLDER, value);
           }
       ```

       可以通过调用addWhereEqualTo添加key=value条件。

     - QueryBuilder

       支持where、orderBy、limit、groupBy等

   - 查询记录数

     ```kotlin
     val count = DaoFactory.getDao(Account::class.java).selectCount()
     ```

     通过selectCount查询符合查询条件的记录条数。

4. 其他注意事项

   - 复杂数据类型字段映射

     ```java
     @Convert(converter = StringListConverter.class, columnType = String.class)
     @Column("acc_child_values")
     private List<String> accChildValues;
     ```

     使用@Convert注解可以保存复杂的数据类型，例如ArrayList，一般将复杂数据类型转成格式化后的字符串类型保存到数据库，读取数据的时候进行自动解码操作。converter类型转换器可以自行定义，columnType为你保存到数据库的实际数据类型。

   - 表结构升级

     ```java
       @Override
       public boolean isUpgradeRecreated() {
           return false;
       }
     ```

     只需要在配置中将数据库版本提升1，即可自动进行表结构的升级。在OrmTable的实现类重写isUpgradeRecreated()来确定表升级后是否要清空之前保存的数据，如果return true，则在表升级后将数据清空。

   - 事务操作

     ```kotlin
     Transaction.execute(Account::class.java) {
                     val selectOne = 			it.selectOne(QueryBuilder.create().orderBy(OrmTable.INDEX_ID))
                     if (selectOne != null) {
                         it.delete(selectOne)
                     }
                 }
     ```

     使用Transaction.execute()可以在代码块中执行事务操作，it指代的是OrmDao&lt;Account&gt;。

#### 二、网络数据的读取和解析

1. 自定义RetrofitManager

   - 按模块对接口进行分类

     所有api接口必须实现ApiService，才可以通过DoraRetrofitManager进行管理，业务模块分类后，将同一类url加入到相同的Service中，有助于职责的清晰划分。

   - 基本配置

     - URL配置

       你可以使用DoraRetrofitManager进行简单的配置发起网络请求，通过registerBaseUrl进行url和服务的注册。

       ```kotlin
       
               DoraRetrofitManager.registerBaseUrl(AccountService::class.java, "http://github.com/dora4/").registerBaseUrl(AccountServiceV2::class.java, "http://github.com/dora4/dcache-android")
       ```

       也可以通过扩展BaseRetrofitManager来进行url和服务的注册。

     - OkHttpClient配置

       DoraRetrofitManager直接可以获取到client对象。

       ```kotlin
       val authenticator = DoraRetrofitManager.client.authenticator
       val cookieJar = DoraRetrofitManager.client.cookieJar
       val interceptors = DoraRetrofitManager.client.interceptors
       val networkInterceptors = DoraRetrofitManager.client.networkInterceptors
       ```

       - 拦截器配置

         - Token拦截器

           你可以直接给DoraRetrofitManager的client添加一个token拦截器来拦截token。

         - 格式化输出响应数据到日志

           你可以添加dora.http.log.FormatLogInterceptor来将响应数据以日志形式格式化输出。

   - 开始使用

     ```kotlin
             // 方式一：并行请求，直接调用即可
             DoraRetrofitManager.getService(AccountService::class.java).getAccount()
                     .enqueue(object : DoraCallback<Account>() {
     
                         override fun onFailure(code: Int, msg: String?) {
                         }
     
                         override fun onSuccess(data: Account) {
                         }
                     })
             // 方式二：串行请求，在net作用域内的api请求，可以很方便的进行数据的合并处理，推荐使用
             net {
                 val account1 = api {
                     DoraRetrofitManager.getService(AccountService::class.java).getAccount()
                 }
                 val account2 = api {
                     DoraRetrofitManager.getService(AccountService::class.java).getAccount()
                 }
             }
     ```

2. DoraCallback和DoraListCallback

   这两个回调接口扩展自retrofit2.Callback，DoraListCallback用于集合数据的回调。

#### 三、repository的使用

1. 数据缓存的设计思维
2. BaseRepository和@Repository
3. 本地缓存数据处理
   - 过滤
     - DataFetcher
     - ListDataFetcher
   - 分页
     - DataPager
     - 基于访问者模式的数据读取

#### 四、感谢作者

如果帮您节省了大量的开发时间，对您有所帮助，欢迎您的赞赏！

捐赠虚拟货币

| 代币           | 钱包地址                                   | 备注                                                        |
| -------------- | ------------------------------------------ | ----------------------------------------------------------- |
| 柚子(EOS)      | doramusic123                               | TAG中直接填写你的github用户名                               |
| USDT(TRC-20链) | TYVXzqctSSPSTeVPYg7i7qbEtSxwrAJQ5y         | 发送你的钱包地址和github用户名至邮箱dora924666990@gmail.com |
| 以太坊(ETH)    | 0x5dA12D7DB7B5D6C8D2Af78723F6dCE4A3C89caB9 | 发送你的钱包地址和github用户名至邮箱dora924666990@gmail.com |