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

#### Gradle依赖配置

```groovy
maven { url 'https://jitpack.io' }
// 稳定版本1.8.6，最新版本为Jitpack编译出来的绿色版本
def latest_version = '1.8.6'
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
                          .selectOne(QueryBuilder.create().orderBy(OrmTable.INDEX_ID))
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
      val count = DaoFactory.getDao(Account::class.java).count()
      ```

      通过count查询符合查询条件的记录条数。

4. **其他注意事项**

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
          val selectOne = it.selectOne(QueryBuilder.create().orderBy(OrmTable.INDEX_ID))
          if (selectOne != null) {
              it.delete(selectOne)
          }
      }
      ```

      使用Transaction.execute()可以在代码块中执行事务操作，it指代的是OrmDao&lt;Account&gt;。

#### 二、网络数据的读取和解析

1. **配置和使用**

    - 按模块对接口进行分类

      所有api接口必须实现ApiService，才可以通过RetrofitManager进行管理，业务模块分类后，将同一类url加入到相同的Service中，有助于职责的清晰划分。

    - 基本配置

        - URL和OkHttpClient的配置

            - Kotlin配置

              你可以通过调用RetrofitManager的init方法进行网络请求的相关配置。

              ```kotlin
                  RetrofitManager.init {
                      okhttp {
                          // add返回值是boolean，所以调用了networkInterceptors还需要返回this
                          networkInterceptors().add(FormatLogInterceptor())
                          this
                      }
                      mappingBaseUrl(TestService::class.java, "http://api.k780.com")
                      mappingBaseUrl(AccountService::class.java, "http://github.com/dora4")
                  }
              ```

              也可以通过扩展JRetrofitManager来进行url和服务的注册。

            - Java配置

              ```java
              // 配置url
              RetrofitManager.getConfig()
                                    .setClient(okhttpClient)
                                    .rxJava(true)
                          .mappingBaseUrl(TestService.class, "http://api.k780.com")
                          .mappingBaseUrl(AccountService.class, "http://github.com/dora4");
              ```

 - 拦截器配置

   - Token拦截器

     你可以直接给RetrofitManager的client添加一个token拦截器来拦截token。

   - 格式化输出响应数据到日志

     你可以添加dora.http.log.FormatLogInterceptor来将响应数据以日志形式格式化输出。

- API服务相关

  我们通过RetrofitManager来管理服务，API服务即继承了ApiService接口的Retrofit的API接口。只有一个接口继承了ApiService接口，才能被RetrofitManager管理。

  | RetrofitManager的方法 | 描述                                                         |
  | --------------------- | ------------------------------------------------------------ |
  | checkService          | 检测一个API服务是否可用，如果不可用，则通过mappingBaseUrl()进行注册 |
  | getService            | 获取API服务对象                                              |
  | removeService         | 移除API服务对象                                              |
  | mappingBaseUrl        | 给API服务绑定base url                                        |

- 开始使用

  ```kotlin
          // 方式一：并行请求，直接调用即可
          RetrofitManager.getService(AccountService::class.java).getAccount()
                  .enqueue(object : DoraCallback<Account>() {
   
                      override fun onFailure(code: Int, msg: String?) {
                      }
   
                      override fun onSuccess(data: Account) {
                      }
                  })
          // 方式二：串行请求，在net作用域内的api请求，可以很方便的进行数据的合并处理，推荐使用
          net {
              val account1 = api {
                  RetrofitManager.getService(AccountService::class.java).getAccount()
              }
              val account2 = result {
                  RetrofitManager.getService(AccountService::class.java).getAccount()
              }
          }
  ```

2. **其它注意事项**

    - DoraCallback和DoraListCallback这两个回调接口扩展自retrofit2.Callback，DoraListCallback可以方便用于集合数据的回调。

    - net作用域下request、api和result的区别

      首先这三个方法都需要在net作用域下执行，net作用域下的请求是串行执行的。

      request：用来自己执行网络请求，比如自己使用okhttp进行请求。

      api：使用RetrofitManager请求，如果执行失败，会抛出异常，你需要捕获DoraHttpException来查看异常信息。

      ```kotlin
      val testRequest = try {
                      api {
                          RetrofitManager
                                  .getService(TestService::class.java).testRequest()
                      }
                  } catch (e: DoraHttpException) {
                      Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                  }
      ```

      result：使用DoraRetrofitManager请求，如果执行失败，直接返回null，不会抛出异常。

      ```kotlin
      val testRequest3 = result {
                      RetrofitManager
                              .getService(TestService::class.java).testRequest()
                  }
      ```

      我们来看看整体的代码。

      ```kotlin
      net {
      		val testRequest = try {
          		api {
              		RetrofitManager.getService(TestService::class.java).testRequest()
              }
          } catch (e: DoraHttpException) {
              Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
          }
          val testRequest2 = api {
              RetrofitManager.getService(TestService::class.java).testRequest()
          }
          val testRequest3 = result {
              RetrofitManager.getService(TestService::class.java).testRequest()
          }
          request {
              // 你自己的网络请求
              var isSuccess = true
              if (isSuccess) {
              		// 成功的回调里面要释放锁
                  it.releaseLock()
              } else {
                  // 失败的回调里面也要释放锁
                  it.releaseLock()
              }      
              // 释放了锁后，request函数的代码执行就结束了，无论后面还有没有代码
              Log.e("这行代码不会被执行，你也可以释放锁来跳出循环，直接结束函数调用")
          }
          Toast.makeText(this, "$testRequest--$testRequest2--$testRequest3", Toast.LENGTH_SHORT).show()
      }
      ```

#### 三、repository的使用

1. **数据缓存的设计思维**

   通常所说的数据缓存包括数据库缓存和内存缓存两种。内存缓存是指以内存保存的数据优先，而数据库缓存指以数据保存的数据优先。内存缓存的整体逻辑是，在app冷启动的时候，从数据库加载数据到内存，然后全局共享数据，网络请求返回新的数据后，会同时更新内存中缓存的数据和数据库缓存的数据，以内存保存的数据为准，数据库保存的数据只用在下一次app冷启动的预加载。而数据库缓存的整体逻辑是，在有网络连接的情况下请求数据将数据显示在界面上，并缓存到数据库，在断网的情况下，则从数据库中取离线数据。

2. **集合数据模式和非集合数据模式**

   通过修改isListMode的值来改变数据模式，一个repository要么处于集合数据模式，要么处于非集合数据模式，默认为集合数据模式。

3. **@Repository和BaseRepository**

   isListMode，默认为true，如果修改为false，则表示这个Repository被用来缓存非集合数据。而BaseRepository为所有数据缓存逻辑的基类，数据缓存流程控制在其子类实现。在使用前，你需要重写Repository的获取网络数据的方法<u>onLoadFromNetwork</u>，才能通过fetchData或fetchListData获取到数据。

4. **使用示例**

   ```kotlin
       val repository = AccountRepository(this, Account::class.java)
       repository.fetchListData().observe(this,
           Observer<List<Account>> {
   		})
   ```
   
如果设置了isListMode为false，则应该调用fetchData。
   
5. **本地缓存数据处理**

    - 过滤
        - DataFetcher

          fetch非集合类型数据的实现类。

        - ListDataFetcher

          fetch集合类型数据的实现类。
    - 分页
        - DataPager

          在将数据加载到界面前，用它来对数据进行分页。

        - 基于访问者模式的数据读取

          ```kotlin
                  // 从Repository中获取分页器，仅限集合数据模式
                  val pager = repository.obtainPager()
                  // 设置分页数据结果的回调
                  pager.setPageCallback(object : PageCallback<Account> {
                      override fun onResult(models: List<Account>) {
                      }
                  })
                  // 使用默认的分页访问者访问数据
                  pager.accept(DefaultPageDataVisitor<Account>())
          ```

6. **整合ORM框架**

   通常情况下，在一个已经成型的项目，更换orm框架抛开开发成本先不说，风险也是很大的。所以这里提供了一种无缝衔接主流orm框架的接口CacheHolder和ListCacheHolder。顾名思义，ListCacheHolder用于集合数据模式下的Repository。默认Repository采用的orm框架是内置的dora-db，如果你使用dora-db，你就无须考虑整合orm框架的问题。如果你用的是市面上主流的orm框架，比如greendao、ormlite或是realm，甚至是room，你就需要自己更换CacheHolder了。以下提供和dora-db整合的源代码，你可以参考它进行整合。

   ```kotlin
   @RepositoryType(BaseRepository.CacheStrategy.DATABASE_CACHE)
   abstract class DoraDatabaseCacheRepository<T: OrmTable>(context: Context)
       : BaseDatabaseCacheRepository<T>(context) {
   
       override fun createCacheHolder(clazz: Class<T>): CacheHolder<T> {
           return DoraCacheHolder<T, T>(clazz)
       }
   
       override fun createListCacheHolder(clazz: Class<T>): CacheHolder<List<T>> {
           return DoraListCacheHolder<T, T>(clazz)
       }
   }
   ```

   ```kotlin
   class DoraListCacheHolder<M, T : OrmTable>(var clazz: Class<out OrmTable>) : ListCacheHolder<M>() {
   
       lateinit var dao: OrmDao<T>
   
       override fun init() {
           dao = DaoFactory.getDao(clazz) as OrmDao<T>
       }
       
       override fun queryCache(condition: Condition): List<M>? {
           return dao.select(WhereBuilder.create(condition)) as List<M>?
       }
   
       override fun removeOldCache(condition: Condition) {
           dao.delete(WhereBuilder.create(condition))
       }
   
       override fun addNewCache(models: List<M>) {
           dao.insert(models as List<T>)
       }
   }
   ```

你也可以使用官方提供的dcache扩展包来更换数据库orm框架。

```groovy
implementation 'com.github.dora4:dcache-room-support:1.4'
implementation 'com.github.dora4:dcache-greendao-support:1.1'
```
**附：StateFlow学习资料**

2.2.0版本新增对StateFlow的支持，数据载体LiveData和StateFlow自由选择。
视频教程：https://www.youtube.com/watch?v=SP_btJHXqN8

Tip：BIP39助记词安全保存推荐使用[【隐私保险箱】](https://dorachat.oss-cn-hongkong.aliyuncs.com/Dora_Box_1.11.apk)🎉
