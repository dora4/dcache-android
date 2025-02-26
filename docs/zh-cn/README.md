# Dora Cache开发文档

> 此文档为开源项目 https://github.com/dora4/dcache-android 而写。作者不易，请点🌟来支持作者。

dcache是一个开源的Android离线数据缓存框架，旨在提供一种简单而高效的方式来缓存网络请求的结果和其他数据，以便在手机没有网络的时候使用历史缓存数据。它可以用于缓存各种类型的数据，包括字符串、JSON、图片、音频和视频等。以下是dcache的主要特点：

简单易用：dcache提供了简单易用的API，使得缓存数据变得非常容易。您只需几行代码即可将数据缓存到本地，而不需要处理复杂的缓存逻辑。

可配置性强：dcache提供了丰富的配置选项，您可以根据自己的需求来调整缓存所使用的orm框架、缓存路径等参数。同时，您也可以自定义缓存策略，从而满足不同的业务需求。

支持扩展：dcache可以轻松地与其他库和框架集成，例如OkHttp、Retrofit等。如果您需要更高级的功能，例如缓存加密和压缩等，您也可以轻松地扩展dcache以满足自己的需求。

总之，dcache是一个简单易用、可配置性强、支持扩展的Android离线数据缓存框架，非常适合用于各种Android应用程序中。

### 开发前的准备

#### 开发环境

Android Studio、Gradle

#### 需要具备的技能

SQL、Retrofit、Kotlin协程等

#### Gradle依赖配置

```groovy
maven { url 'https://jitpack.io' }
// 稳定版本3.2.9，最新版本请使用Jitpack成功编译（带有绿色版本标识）的版本
def stable_version = '3.2.9'
implementation "com.github.dora4:dcache-android:$stable_version"
```

### 使用文档（ 使用示例 <https://github.com/dora4/DoraCacheSample> ）

#### 一、dcache的orm详解

1. **配置初始化**

   ```kotlin
   Orm.init(this, OrmConfig.Builder()
                   .database("dcache_sample")
                   .tables(User::class.java)
                   .version(1)
                   .build())
   ```

   在自定义的Application类的入口加入一行配置，database为数据库名，version从1开始每次递增1，tables用来配置需要初始化的表，dcache中所有的表需要实现OrmTable接口。

2. **注解**

   * 表和列相关

     	并非必需配置。

     * @Table

       此注解配置在OrmTable的实现类的类名之上，用来指定一个类映射到表的名称，不配置则使用默认映射规则。

     * @Column

       此注解配置在OrmTable的实现类的成员属性之上，用来指定一个属性映射到字段的名称，不配置则使用默认映射规则。

     * @Ignore

       此注解的优先级高于@Column，配置在OrmTable的实现类的成员属性之上，配置了此注解的成员属性，不会作为表的字段进行映射。

   * 约束相关

     	配置主键用的@PrimaryKey和@Id必须使用其中一个，其他并非必需配置。

     * @NotNull

       此注解配置在OrmTable的实现类的成员属性之上，用来指定这个字段为非空字段。

     * @PrimaryKey

       此注解配置在OrmTable的实现类的成员属性之上，用来指定这个字段为表的主键。

     * @Id

       此注解配置在OrmTable的实现类的成员属性之上，作用类似于@PrimaryKey，并

       在它的基础上指定了该字段名为”\_id“，相当于@PrimaryKey+\@Column("\_id")。

     * @Unique

       此注解配置在OrmTable的实现类的成员属性之上，表示这个字段的值在这张表中从不重复。

     * @Default

       此注解配置在OrmTable的实现类的成员属性之上，通过它可以给字段指定默认值。

3. **CRUD操作**

   我们使用OrmDao对象来操作一张表的增删改查操作，通过DaoFactory.getDao()获取OrmDao对象，通常我们会将OrmDao保存为成员变量以复用。如果需要使用异步操作，请使用方法名以Async结尾的方法。

   * 插入数据

     ```kotlin
     DaoFactory.getDao(User::class.java).insert(user)
     ```

     注意，insert不仅可以被用来插入单条数据，也可以插入一个List数据。

   * 删除数据

     ```kotlin
     DaoFactory.getDao(User::class.java).delete(user)
     ```

   * 更新数据

     ```kotlin
     DaoFactory.getDao(User::class.java).update(user)
     ```

   * 查询数据

     ```kotlin
     // 查询单条数据
     DaoFactory.getDao(User::class.java).selectOne(queryBuilder)
     DaoFactory.getDao(User::class.java).selectOne(whereBuilder)
     DaoFactory.getDao(User::class.java).selectOne(condition)
     // 查询多条数据
     DaoFactory.getDao(User::class.java).select(queryBuilder)
     DaoFactory.getDao(User::class.java).select(whereBuilder)
     DaoFactory.getDao(User::class.java).select(condition)
     // 查询整张表数据
     DaoFactory.getDao(User::class.java).selectAll()
     ```

     * Condition（了解）

       它是一个通用查询条件的统称，整合外部ORM框架可能才会用到它。

       selection：where子句，不带where，可以带”？“占位符

       selectionArgs：”？“占位符的所有值

     * WhereBuilder

       where子句的构建类，通过WhereBuilder.create()创建实例

       ```java
       public WhereBuilder addWhereEqualTo(String column, Object value) {
           return append(null, column + EQUAL_HOLDER, value);
       }
       ```

       如可以通过调用addWhereEqualTo()添加“key=value”的条件。其他类似方法还有addWhereNotEqualTo()、addWhereGreaterThan()、addWhereLessThan()、and()、or()、not()、parenthesesLeft()和parenthesesRight()等。

       > <b>相关方法</b>
       >
       > addWhereNotEqualTo：不等于
       >
       > addWhereGreaterThan：大于
       >
       > addWhereLessThan：小于
       >
       > addWhereGreaterThanOrEqualTo：不小于
       >
       > addWhereLessThanOrEqualTo：不大于
       >
       > and：与
       >
       > or：或
       >
       > not：非
       >
       > parenthesesLeft：左括号
       >
       > parenthesesRight：右括号

     * QueryBuilder

       支持where、orderBy、limit、groupBy等

   * 查询记录条数

     ```kotlin
     val num1 = DaoFactory.getDao(User::class.java).count(queryBuilder)
     val num2 = DaoFactory.getDao(User::class.java).count(whereBuilder)
     val num3 = DaoFactory.getDao(User::class.java).count(condition)
     val num4 = DaoFactory.getDao(User::class.java).countAll()
     ```

     使用count系列方法查询记录条数。

4. **其他注意事项**

   * 添加混淆规则
   
     ```pro
     -keep class * implements dora.db.table.OrmTable { *; }
     ```
     
   * 复杂数据类型字段映射

     ```java
     @Convert(converter = StringListConverter.class, columnType = String.class)
     @Column("complex_object")
     private List<String> object;
     ```

     使用@Convert注解可以保存复杂的数据类型，例如ArrayList。一般将复杂数据类型转成格式化后的String类型保存到数据库，读取数据的时候使用转换器自动进行解码操作。converter转换器可以自定义，columnType为你保存到数据库的实际数据类型。

   * 表结构升级

     ```java
     @Override
     public boolean isUpgradeRecreated() {
         return false;
     }
     ```

     只需要在配置中将数据库版本号+1，即可自动进行表结构的升级。在OrmTable的实现类重写isUpgradeRecreated()来确定表升级后是否保留之前的旧数据。如果return true（不建议），则在表升级时将旧数据清空。建议通过框架提供的OrmMigration来转移旧数据到新的字段，这样的话，你需要在OrmTable的实现类中重写与数据迁移相关的方法。

   * 事务操作

     1. 单表事务

        ```kotlin
        Transaction.execute(User::class.java) {
            // 以下三个user要同时删除，否则整个事务操作失败
            it.delete(WhereBuilder.create().addWhereEqualTo("user_id", "10000001"))
            it.delete(WhereBuilder.create().addWhereEqualTo("user_id", "10000002"))
            it.delete(WhereBuilder.create().addWhereEqualTo("user_id", "10000003"))
        }
        ```

        使用Transaction.execute()可以在代码块中执行事务操作，指定何种泛型就是何种类型的OrmDao，如这里it指代的是OrmDao\<User>。

     2. 多表事务

        ```kotlin
        // 扫描手机歌曲
        if (musics.size > 0) {
            // 歌曲都没有就没有必要查询歌曲信息了
            Transaction.execute {
                // 查询并保存艺术家信息
                val artists = queryArtist(context)
                artistDao.insert(artists)
                // 查询并保存专辑信息
                val albums = queryAlbum(context)
                albumDao.insert(albums)
                // 查询并保存歌曲文件夹信息
                val folders = queryFolder(context)
                folderDao.insert(folders)
            }
        }
        ```

#### 二、网络数据的读取和解析

1. **配置和使用**

   * 按模块对接口进行分类

     使用Retrofit对接口进行动态代理，在Retrofit的使用基础上，所有Restful API接口的包装类必须实现ApiService接口，这样才能使用RetrofitManager类进行管理。业务模块分类后，将同一类Restful API接口加入到相同的ApiService实现类中，遵循单一职责的原则。

   * 基本配置

     * URL和OkHttpClient的配置

       * Kotlin配置

         你可以通过调用RetrofitManager的init方法进行配置的初始化。

         ```kotlin
         // Kotlin配置示例
         RetrofitManager.init {
             okhttp {
                 // 这里由于add()方法的返回值是boolean，所以最终还需要返回this
                 networkInterceptors().add(FormatLogInterceptor())
                 this
             }
             // 可以映射多个Base URL地址
             mappingBaseUrl(TestOneService::class.java, "http://api.example1.com")
             mappingBaseUrl(TestTwoService::class.java, "http://api.example2.com")
         }
         ```

         也可以通过扩展RetrofitManager来进行url和服务的注册。

       * Java配置

         ```java
         // Java配置示例
         RetrofitManager.getConfig()
                     .setClient(okhttpClient)
                     .mappingBaseUrl(TestOneService.class, "http://api.example1.com")
                     .mappingBaseUrl(TestTwoService.class, "http://api.example2.com");
         ```

* 拦截器配置（了解）

  * FormatLogInterceptor

    dora.http.log.FormatLogInterceptor它是一个格式化输出日志的拦截器，你可以添加它将服务端响应的数据以日志形式格式化后输出到logcat中。

* RetrofitManager

  通过RetrofitManager来管理所有ApiService，一个接口只有继承了ApiService接口，才能被RetrofitManager管理。

  | API            | 描述                                                         |
  | -------------- | ------------------------------------------------------------ |
  | checkService   | 检测一个API服务是否可用。如果不可用，则没有在初始化配置时调用mappingBaseUrl()进行Base URL的映射 |
  | getService     | 获取API服务对象                                              |
  | removeService  | 移除API服务对象                                              |
  | mappingBaseUrl | 给API服务绑定Base URL                                        |

* 开始使用

  ```kotlin
  // 方式一：异步（并行）请求，直接调用即可
  RetrofitManager.getService(UserService::class.java).getUser().enqueue(object : DoraCallback<User>() {
      override fun onFailure(code: Int, msg: String?) {
      }

      override fun onSuccess(data: User) {
      }
  })

  // 方式二（推荐使用）：同步（串行）请求，在net作用域内使用api、result以及request等高阶函数包装的Restful API请求，
  // 可以很方便的进行数据的合并处理
  net {
      val user1 = api {
          RetrofitManager.getService(UserService::class.java).getUser()
      }
      val user2 = result {
          RetrofitManager.getService(UserService::class.java).getUser()
      }
      // 在这里合并多个接口的数据...
  }
  ```

2. **其它注意事项**

   * DoraCallback和DoraListCallback这两个回调接口扩展自retrofit2.Callback，DoraListCallback为List类型的数据量身打造。

   * net作用域request、api和result的区别

     首先这三个方法都需要在net作用域内使用，net作用域的请求是串行执行的，且都需要使用DoraHttp中提供的这些高阶函数包裹。

     request：使用外部框架执行网络请求，比如自己使用okhttp进行请求，注意使用它要使用releaseLock()方法释放锁。

     api：RetrofitManager请求执行失败，会抛出异常，你需要捕获DoraHttpException来查看异常信息。

     ```kotlin
     val user = try {
        api { RetrofitManager.getService(UserService::class.java).getUser() }
     } catch (e: DoraHttpException) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
     }
     ```

     result：RetrofitManager请求执行失败，直接返回null，不会抛出异常。

     ```kotlin
     val user = result { RetrofitManager.getService(UserService::class.java).getUser() }
     ```

     我们来看看整体的代码。

     ```kotlin
     net {
         val user1 = try {
             api { RetrofitManager.getService(UserService::class.java).getUser() }
         } catch (e: DoraHttpException) {
             Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
         }
         val user2 = result { RetrofitManager.getService(UserService::class.java).getUser() }
         val user3 = request {
             // 伪代码，你自己的网络请求，省略若干行...
             var success = true
             if (success) {
                 // 成功的回调里面要释放锁
                 it.releaseLock(user)
             } else {
                 // 失败的回调里面也要释放锁
                 it.releaseLock(null)
             }      
             Log.e("这行代码不会被执行，释放了锁后，request函数的代码执行就结束了，无论后面是否还有代码")
         }
         // 打印这些数据
         Toast.makeText(this, "$user1--$user2--$user3", Toast.LENGTH_SHORT).show()
     }
     ```

#### 三、repository的使用

1. **数据缓存的设计思维**

   通常所说的数据缓存包括数据库缓存和内存缓存两种。内存缓存是指以内存保存的数据优先，而数据库缓存指以数据保存的数据优先。内存缓存的整体逻辑是，在app冷启动（无后台进程启动）的时候，从数据库加载数据到内存，然后全局共享数据，网络请求返回新的数据后，会同时更新内存中缓存的数据和数据库缓存的数据，以内存保存的数据为准，数据库保存的数据只用在下一次app冷启动的预加载。而数据库缓存的整体逻辑是，在有网络连接的情况下请求数据将数据显示在界面上，并缓存到数据库，在无网络连接的情况下，则从数据库中取离线数据。框架中的repository实现的是前者。

2. **数据模式**

   数据模式集合模式和非集合模式两种，默认为集合数据模式。一个repository要么处于集合数据模式，要么处于非集合数据模式。且一经指定，不会修改。同一种类型的数据，无论使用集合还是非集合模式，都可以创建多个repository。一个repository只与某个Restful API接口绑定，不与数据类型绑定。

3. **@Repository、@ListRepository和BaseRepository**

   早期版本通过@Repository的isListMode的值来指定数据模式，最新版本则通过@Repository和@ListRepository注解本身来区分数据模式。BaseRepository为所有数据缓存逻辑的基类，数据缓存流程控制在其子类实现。在使用前，你需要重写repository的获取网络数据的onLoadFromNetwork()方法，才能通过fetchData或fetchListData获取到数据。<u>注意只需要重写对应的一个onLoadFromNetwork()方法。</u>

4. **使用示例**

   ```kotlin
   // 单处刷新数据
   val repository = UserRepository(this, User::class.java)
   repository.fetchListData().observe(this, Observer<List<User>> {
       // 使用数据刷新UI
   })

   // 多处刷新数据
   val repository = UserRepository(this, User::class.java)
   repository.getListLiveData().observe(this, Observer<List<User>> {
       // 使用数据刷新UI
   })
   // 第一处刷新数据
   repository.fetchListData()
   // 第二处刷新数据
   repository.fetchListData()
   ```

   如果为常规模式（非集合模式），则应该调用fetchData。

5. **内存缓存数据处理**

   * 数据抓取（了解）

     * DataFetcher

       抓取常规模式数据的实现类。

     * ListDataFetcher

       抓取集合模式数据的实现类。

   * 分页（了解）

     * DataPager

       将数据设置到UI之前，用它处理数据分页。

     * 基于访问者设计模式的数据读取

       ```kotlin
       // 从repository中获取分页器，仅限集合数据模式
       val pager = repository.obtainPager()
       // 设置分页数据结果的回调
       pager.setPageCallback(object : PageCallback<User> {
           override fun onResult(models: List<User>) {
               // 每次接受访问者的访问都会回调这里
           }
       })
       // 使用默认的分页访问者访问数据
       pager.accept(DefaultPageDataVisitor<User>())
       ```

6. **整合其他主流ORM框架**

   通常情况下，在一个已经成型的项目中，更换orm框架抛开开发成本不说，风险也是很大的。所以这里提供了一种无缝衔接主流orm框架的接口CacheHolder和ListCacheHolder。顾名思义，ListCacheHolder用于集合数据模式下的repository。repository默认采用的orm框架是内置的dora-db（dora.db包），如果你使用它，则无需考虑整合orm框架的问题。如果你使用的是其他orm框架，比如room、greendao或是ormlite，你就需要自己更换CacheHolder了。以下为整合相关源代码，你可以参考它进行整合。

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

       private lateinit var dao: OrmDao<T>

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

   另外，你也可以使用官方提供的dcache扩展包来更换数据库orm框架，如有改进意见，或有整合好的扩展包，欢迎你的投稿！

   ```groovy
   implementation 'com.github.dora4:dcache-room-support:1.8'
   implementation 'com.github.dora4:dcache-greendao-support:1.2'
   ```

7. **分页缓存**

   以展示banner图为例，后端返回给用户端的数据是一次性全部返回的，因为就那么几条数据。但是以系统管理员身份登录的时候，另外的界面则应该显示所有数据，包括开关是关闭不展示给用户端看的。这个时候应该把所有数据都缓存下来，如果离线了，能以两种身份读取离线缓存的数据。即可以一次性全部获取，也可以分页。

   ```kotlin
   package com.dorachat.dorachat.repository

   import android.content.Context
   import android.os.Build
   import androidx.annotation.RequiresApi
   import com.dorachat.dorachat.common.AppConfig.Companion.PRODUCT_NAME
   import com.dorachat.dorachat.http.ApiResult
   import com.dorachat.dorachat.http.PageDTO
   import com.dorachat.dorachat.http.service.HomeService
   import com.dorachat.dorachat.model.BannerInfo
   import com.dorachat.dorachat.model.request.home.ReqProductByPage
   import dora.cache.DoraPageListCallback
   import dora.cache.data.adapter.ListResultAdapter
   import dora.cache.data.adapter.PageListResultAdapter
   import dora.cache.data.fetcher.OnLoadStateListener
   import dora.cache.factory.DatabaseCacheHolderFactory
   import dora.cache.repository.DoraPageDatabaseCacheRepository
   import dora.cache.repository.ListRepository
   import dora.db.builder.Condition
   import dora.db.builder.QueryBuilder
   import dora.http.retrofit.RetrofitManager.getService
   import retrofit2.Callback
   import javax.inject.Inject

   @ListRepository
   class BannerRepository @Inject constructor(context: Context) :
       DoraPageDatabaseCacheRepository<BannerInfo>(context) {

       private var isAdmin: Boolean = false

       fun setAdmin(isAdmin: Boolean) : BannerRepository {
           this.isAdmin = isAdmin
           return this
       }

       override fun query(): Condition {
           return if (isAdmin) {
               super.query()
           } else {
               // 不分页，返回全部数据
               QueryBuilder.create().toCondition()
           }
       }

       override fun onLoadFromNetwork(
           callback: DoraPageListCallback<BannerInfo>,
         	// 成功不用回调成功，框架会自动帮你回调。但错误要回调错误，让界面层显示错误，比如在解析到某个字段时，读取到特定
           // 的标识认定为失败，不过这种情况不常用。
           listener: OnLoadStateListener?
       ) {
           if (isAdmin) {
               val req = ReqProductByPage(PRODUCT_NAME, getPageSize(), getPageNo())
               getService(HomeService::class.java).getBanners(req.toRequestBody()).enqueue(
                   PageListResultAdapter<BannerInfo, ApiResult<BannerInfo>>(callback)
                           as Callback<ApiResult<PageDTO<BannerInfo>>>
               )
           } else {
               getService(HomeService::class.java).getBanners(PRODUCT_NAME).enqueue(
                   PageListResultAdapter<BannerInfo, ApiResult<BannerInfo>>(callback)
                           as Callback<ApiResult<MutableList<BannerInfo>>>
               )
           }
       }

       override fun createCacheHolderFactory(): DatabaseCacheHolderFactory<BannerInfo> {
           return DatabaseCacheHolderFactory(BannerInfo::class.java)
       }
   }
   ```

      我们再看一下调用处怎么调用它。

   ```kotlin
   // UI层
   binding.slBannerInfoList.setOnSwipeListener(object : SwipeLayout.OnSwipeListener {

       override fun onRefresh(swipeLayout: SwipeLayout) {
       }

       override fun onLoadMore(swipeLayout: SwipeLayout) {
           bannerRepository.onLoadMore {
               swipeLayout.loadMoreFinish(if (it) SwipeLayout.SUCCEED else SwipeLayout.FAIL)
           }
       }
   })
   // 数据层
   bannerRepository.observeData(this, object : DoraPageDatabaseCacheRepository.AdapterDelegate<BannerInfo> {

       override fun addData(data: MutableList<BannerInfo>) {
           adapter.addData(data)
           binding.emptyLayout.showContent()
       }

       override fun setList(data: MutableList<BannerInfo>) {
           adapter.setList(data)
           binding.emptyLayout.showContent()
       }
   })
   // 使用默认的每页大小，也就是每页10条数据，加载第一页
   bannerRepository.setAdmin(true).onRefresh()
   ```

   另外对于数据总条数是不断变化的场景，比如聊天消息，我们通常采用对数据进行快照的方式，也就是指定数据截止的时间戳。在这个时间节点之前的数据，我们可以认为是固定的大小。这样接口就需要多传一个timestamp的参数了，对于缓存也是一样的，也需要考虑这个timestamp进行数据的过滤。
