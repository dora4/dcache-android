# Dora Cache Doc

> This document is written for the open source project https://github.com/dora4/dcache-android. It is not easy for the author, please click 🌟 to support the author.

**dcache** is an open-source offline data caching framework for Android, designed to provide a simple and efficient way to cache network request results and other data for use when the device is offline. It can be used to cache various types of data, including strings, JSON, images, audio, and video. Below are the main features of dcache:

- **Easy to Use**: dcache offers an easy-to-use API that makes data caching extremely simple. You can cache data locally with just a few lines of code without dealing with complex caching logic.
- **Highly Configurable**: dcache provides rich configuration options, allowing you to adjust parameters such as the ORM framework and cache path according to your needs. You can also customize caching strategies to meet different business requirements.
- **Supports Extension**: dcache can be easily integrated with other libraries and frameworks, such as OkHttp and Retrofit. If you need more advanced features, such as cache encryption and compression, you can also easily extend dcache to meet your needs.

In short, dcache is a simple, highly configurable, and extendable offline data caching framework for Android, ideal for use in various Android applications.

### Preparation Before Development

#### Development Environment

Android Studio, Gradle

#### Required Skills

SQL, Retrofit, Kotlin Coroutines, etc.

#### Gradle Dependency Configuration

```groovy
maven { url 'https://jitpack.io' }
// Stable version 3.2.9, for the latest version use the version successfully compiled on Jitpack (marked in green).
def stable_version = '3.2.9s'
implementation "com.github.dora4:dcache-android:$stable_version"
```

### Documentation (Tutorial: https://github.com/dora4/DoraCacheSample)

#### One. dcache ORM Details

1. **Configuration Initialization**

   ```kotlin
   Orm.init(this, OrmConfig.Builder()
                   .database("dcache_sample")
                   .tables(User::class.java)
                   .version(1)
                   .build())
   ```

   Add a line of configuration in the entry of your custom Application class. The `database` is the database name, `version` starts from 1 and increments by 1 each time, and `tables` is used to configure the tables that need to be initialized. All tables in dcache must implement the `OrmTable` interface.

2. **Annotations**

   - **Table and Column Related Annotations**

     These are not mandatory configurations.

     - `@Table`: This annotation, configured above the class name of the `OrmTable` implementation class, specifies the name of the table a class maps to. If not configured, the default mapping rule is used.
     - `@Column`: This annotation, configured on the member attributes of the `OrmTable` implementation class, specifies the name of the column a property maps to. If not configured, the default mapping rule is used.
     - `@Ignore`: This annotation takes precedence over `@Column` and is configured on the member attributes of the `OrmTable` implementation class. The attributes configured with this annotation will not be mapped as table columns.

   - **Constraint Related Annotations**

     To configure a primary key, use either `@PrimaryKey` or `@Id`; the rest are not mandatory.

     - `@NotNull`: Configured on the member attributes of the `OrmTable` implementation class, specifies that this field is not nullable.
     - `@PrimaryKey`: Configured on the member attributes of the `OrmTable` implementation class, specifies that this field is the primary key of the table.
     - `@Id`: Similar to `@PrimaryKey` but also specifies that the field name is "_id", equivalent to `@PrimaryKey` + `@Column("_id")`.
     - `@Unique`: Configured on the member attributes of the `OrmTable` implementation class, indicates that this field's value is never duplicated in this table.
     - `@Default`: Configured on the member attributes of the `OrmTable` implementation class to specify a default value for the field.

3. **CRUD Operations**

   Use the `OrmDao` object to perform CRUD operations on a table. The `OrmDao` object can be obtained via `DaoFactory.getDao()`. Typically, `OrmDao` is saved as a member variable for reuse. If asynchronous operations are needed, please use methods with names ending in "Async".

   - **Inserting Data**

     ```kotlin
     DaoFactory.getDao(User::class.java).insert(user)
     ```

     Note that `insert` can be used to insert a single data item or a list of data items.

   - **Deleting Data**

     ```kotlin
     DaoFactory.getDao(User::class.java).delete(user)
     ```

   - **Updating Data**

     ```kotlin
     DaoFactory.getDao(User::class.java).update(user)
     ```

   - **Querying Data**

     ```kotlin
     // Query a single data item
     DaoFactory.getDao(User::class.java).selectOne(queryBuilder)
     DaoFactory.getDao(User::class.java).selectOne(whereBuilder)
     DaoFactory.getDao(User::class.java).selectOne(condition)
     // Query multiple data items
     DaoFactory.getDao(User::class.java).select(queryBuilder)
     DaoFactory.getDao(User::class.java).select(whereBuilder)
     DaoFactory.getDao(User::class.java).select(condition)
     // Query all data in the table
     DaoFactory.getDao(User::class.java).selectAll()
     ```

     - **Condition (Understanding)**

       A general term for common query conditions, which may be used when integrating external ORM frameworks.

       - **selection**: The "where" clause, without the "where" keyword, can contain "?" placeholders.
       - **selectionArgs**: All values for the "?" placeholders.

     - **WhereBuilder**

       ```java
       public WhereBuilder addWhereEqualTo(String column, Object value) {
           return append(null, column + EQUAL_HOLDER, value);
       }
       ```

       For example, you can use `addWhereEqualTo()` to add a "key = value" condition. Other similar methods include `addWhereNotEqualTo()`, `addWhereGreaterThan()`, `addWhereLessThan()`, `and()`, `or()`, `not()`, `parenthesesLeft()`, and `parenthesesRight()`.

       > **Related Methods**
       >
       > - **addWhereNotEqualTo**: Not equal to
       > - **addWhereGreaterThan**: Greater than
       > - **addWhereLessThan**: Less than
       > - **addWhereGreaterThanOrEqualTo**: Greater than or equal to
       > - **addWhereLessThanOrEqualTo**: Less than or equal to
       > - **and**: Logical AND
       > - **or**: Logical OR
       > - **not**: Logical NOT
       > - **parenthesesLeft**: Left parenthesis
       > - **parenthesesRight**: Right parenthesis

     - **QueryBuilder**

       Supports operations such as `where`, `orderBy`, `limit`, and `groupBy`.

   - **Counting Records**

     ```kotlin
     val num1 = DaoFactory.getDao(User::class.java).count(queryBuilder)
     val num2 = DaoFactory.getDao(User::class.java).count(whereBuilder)
     val num3 = DaoFactory.getDao(User::class.java).count(condition)
     val num4 = DaoFactory.getDao(User::class.java).countAll()
     ```

     Use the count series methods to query the number of records.

4. **Other Considerations**

   - **Mapping Complex Data Types**

     ```java
     @Convert(converter = StringListConverter.class, columnType = String.class)
     @Column("complex_object")
     private List<String> object;
     ```

     The `@Convert` annotation allows you to save complex data types, such as `ArrayList`. Typically, you convert complex data types into a formatted `String` type to save them in the database. When reading data, a converter is automatically used for decoding. The `converter` can be customized, and `columnType` specifies the actual data type saved in the database.

   - **Table Structure Upgrades**

     ```java
     @Override
     public boolean isUpgradeRecreated() {
         return false;
     }
     ```

     To upgrade the table structure, simply increase the database version number in the configuration by 1. Override the `isUpgradeRecreated()` method in the implementation class of `OrmTable` to determine whether to retain the old data after the table upgrade. If `return true` (not recommended), the old data will be cleared during the table upgrade. It is recommended to use `OrmMigration` provided by the framework to transfer old data to new fields. In this case, you need to override the data migration-related methods in the implementation class of `OrmTable`.

   - **Transaction Operations**

     1. **Single-Table Transactions**

        ```kotlin
        Transaction.execute(User::class.java) {
            // All three users must be deleted, or the entire transaction will fail
            it.delete(WhereBuilder.create().addWhereEqualTo("user_id", "10000001"))
            it.delete(WhereBuilder.create().addWhereEqualTo("user_id", "10000002"))
            it.delete(WhereBuilder.create().addWhereEqualTo("user_id", "10000003"))
        }
        ```

        You can perform transactional operations within a code block using `Transaction.execute()`. The specified generic type determines the type of `OrmDao` to use; in this example, `it` represents `OrmDao<User>`.

     2. **Multi-Table Transactions**

        ```kotlin
        // Scan music files on the phone
        if (musics.size > 0) {
            // If there are no songs, there is no need to query song information
            Transaction.execute {
                // Query and save artist information
                val artists = queryArtist(context)
                artistDao.insert(artists)
                // Query and save album information
                val albums = queryAlbum(context)
                albumDao.insert(albums)
                // Query and save song folder information
                val folders = queryFolder(context)
                folderDao.insert(folders)
            }
        }
        ```

#### Two.Reading and Parsing Network Data

   1. **Configuration and Usage**

      - **Categorizing Interfaces by Module**

         Use Retrofit to dynamically proxy interfaces. Based on Retrofit's usage, all Restful API wrapper classes must implement the `ApiService` interface so that they can be managed by the `RetrofitManager` class.

      - **Basic Configuration**

        - **URL and OkHttpClient Configuration**

          - **Kotlin Configuration**

            You can initialize the configuration by calling the `init` method of `RetrofitManager`.

            ```kotlin
            // Kotlin configuration example
            RetrofitManager.init {
                okhttp {
                    // Since the return value of the add() method is boolean, this still needs to return `this`
                    networkInterceptors().add(FormatLogInterceptor())
                    this
                }
                // Multiple Base URLs can be mapped
                mappingBaseUrl(TestOneService::class.java, "http://api.example1.com")
                mappingBaseUrl(TestTwoService::class.java, "http://api.example2.com")
            }
            ```

          - **Java Configuration**

            ```java
            // Java configuration example
            RetrofitManager.getConfig()
                        .setClient(okhttpClient)
                        .mappingBaseUrl(TestOneService.class, "http://api.example1.com")
                        .mappingBaseUrl(TestTwoService.class, "http://api.example2.com");   
            ```

      - **Interceptor Configuration**

        - **FormatLogInterceptor**

          The dora.http.log.FormatLogInterceptor is an interceptor for formatted log output. You can add it to format the data returned by the server into a log format and output it to the logcat.

      - **RetrofitManager**

          Use RetrofitManager to manage all ApiService instances. An interface must inherit from the ApiService interface to be managed by RetrofitManager.

          | API            | Description                                                  |
          | -------------- | ------------------------------------------------------------ |
          | checkService   | Checks if an API service is available. If not, it indicates that `mappingBaseUrl()` was not called during the initialization to set the Base URL. |
          | getService     | Retrieves the API service object.                            |
          | removeService  | Removes the API service object.                              |
          | mappingBaseUrl | Binds a Base URL to the API service.                         |

      - **Getting Started**

        ```kotlin
        // Method 1: Asynchronous (parallel) request, call directly
        RetrofitManager.getService(UserService::class.java).getUser().enqueue(object : DoraCallback<User>() {

            override fun onFailure(code: Int, msg: String?) {
            }

            override fun onSuccess(data: User) {
            }
        })

        // Method 2 (recommended): Synchronous (serial) request, use high-order functions such as api, result, and request within the net scope
        // to wrap the Restful API requests for easy data merging
        net {
            val user1 = api {
                RetrofitManager.getService(UserService::class.java).getUser()
            }
            val user2 = result {
                RetrofitManager.getService(UserService::class.java).getUser()
            }
            // Merge data from multiple interfaces here...
        }
        ```

2. **Other Notes**

   - DoraCallback and DoraListCallback: These callback interfaces extend from retrofit2.Callback, with DoraListCallback designed specifically for List type data.

   - Differences between request, api, and result in the net scope

     All three methods need to be used within the net scope. Requests in the net scope are executed serially, and they must be wrapped using these higher-order functions provided by DoraHttp.

     request: Uses an external framework to make network requests, such as using okhttp for requests. Make sure to release the lock with the releaseLock() method.

     api: If a request made by RetrofitManager fails, it throws an exception. You need to catch DoraHttpException to view the exception information.

     ```kotlin
     val user = try {
        api { RetrofitManager.getService(UserService::class.java).getUser() }
     } catch (e: DoraHttpException) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
     }
     ```

      result: If a request made by RetrofitManager fails, it directly returns null without throwing an exception.

     ```kotlin
     val user = result { RetrofitManager.getService(UserService::class.java).getUser() }
     ```

     Let's look at the overall code.

     ```kotlin
     net {
         val user1 = try {
             api { RetrofitManager.getService(UserService::class.java).getUser() }
         } catch (e: DoraHttpException) {
             Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
         }
         val user2 = result { RetrofitManager.getService(UserService::class.java).getUser() }
         val user3 = request {
             // Pseudo-code, your own network request, omitted lines...
             var success = true
             if (success) {
                 // Remember to release the lock in the success callback
                 it.releaseLock(user)
             } else {
                 // Remember to release the lock in the failure callback as well
                 it.releaseLock(null)
             }      
             Log.e("This line of code will not be executed, after releasing the lock, the code execution of the request function ends, regardless of whether there is more code afterward")
         }
         // Print these data
         Toast.makeText(this, "$user1--$user2--$user3", Toast.LENGTH_SHORT).show()
     }
     ```

#### Three.Using Repositories

- **Design Philosophy of Data Caching**

  Data caching typically includes two types: database caching and in-memory caching. In-memory caching prioritizes data stored in memory, whereas database caching prioritizes data stored in the database. The overall logic for in-memory caching is to load data from the database into memory during a cold startup (when there are no background processes), then share the data globally. When new data is returned by a network request, both the cached data in memory and the database are updated. The data in memory takes precedence, and the data saved in the database is used only for preloading on the next app cold start. For database caching, the logic is to request data and display it on the interface when there is a network connection, and cache it in the database. When there is no network connection, offline data is fetched from the database. The repository implementation in the framework follows the former approach.

- **Data Mode**

  There are two data modes: collection mode and non-collection mode. The default is collection data mode. A repository is either in collection data mode or non-collection mode. Once specified, it will not change. Regardless of whether a collection or non-collection mode is used, multiple repositories can be created for the same type of data. A repository is only bound to a specific Restful API interface, not to a data type.

- **@Repository, @ListRepository, and BaseRepository**

  In earlier versions, the data mode was specified by the value of isListMode in the @Repository annotation. In the latest version, the data mode is distinguished by the @Repository and @ListRepository annotations themselves. BaseRepository serves as the base class for all data caching logic, and data caching process control is implemented in its subclasses. Before use, you need to override the onLoadFromNetwork() method to fetch network data in the repository so that data can be fetched via fetchData or fetchListData. Note: Only the corresponding onLoadFromNetwork() method needs to be overridden.

- **Usage Example**

  ```kotlin
  // Refresh data in a single place
  val repository = UserRepository(this, User::class.java)
  repository.fetchListData().observe(this, Observer<List<User>> {
      // Use data to refresh UI
  })

  // Refresh data in multiple places
  val repository = UserRepository(this, User::class.java)
  repository.getListLiveData().observe(this, Observer<List<User>> {
      // Use data to refresh UI
  })
  // First data refresh
  repository.fetchListData()
  // Second data refresh
  repository.fetchListData()
  ```

  If using the regular mode (non-collection mode), fetchData should be called.

- **Handling In-Memory Cached Data**

  - Data Fetching (For Understanding)

    DataFetcher: Class that implements fetching data in regular mode.

    ListDataFetcher: Class that implements fetching data in collection mode.

  - Pagination (For Understanding)

    - DataPager: Used to handle data pagination before setting data to the UI.

    - Data Reading Based on the Visitor Design Pattern

      ```kotlin
      // Obtain a pager from the repository, limited to collection data mode
      val pager = repository.obtainPager()
      // Set the callback for pagination data results
      pager.setPageCallback(object : PageCallback<User> {
          override fun onResult(models: List<User>) {
              // This is called every time the visitor accesses data
          }
      })
      // Use the default pagination visitor to access data
      pager.accept(DefaultPageDataVisitor<User>())
      ```

- **Integration with Other Mainstream ORM Frameworks**

  Generally, replacing the ORM framework in an established project is risky and costly in terms of development. Therefore, an interface CacheHolder and ListCacheHolder is provided for seamless integration with mainstream ORM frameworks. ListCacheHolder is used for the repository in collection data mode. The default ORM framework used by the repository is the built-in dora-db (under the dora.db package). If you use it, there is no need to consider ORM framework integration. If you use other ORM frameworks like Room, GreenDAO, or OrmLite, you will need to replace CacheHolder yourself. The following is the related source code for integration, which you can refer to.

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

      override fun queryCache(condition: Condition): List<M> {
          return dao.query(condition) as List<M>
      }

      override fun insertCache(models: List<M>) {
          dao.insert(models as List<T>)
      }

      override fun deleteCache(condition: Condition) {
          dao.deleteByCondition(condition)
      }
  }
  ```

  Additionally, you can use the official dcache extension package to replace the database ORM framework. If you have any suggestions for improvements or have an integrated extension package, you are welcome to contribute!

  ```groovy
  implementation 'com.github.dora4:dcache-room-support:1.8'
  implementation 'com.github.dora4:dcache-greendao-support:1.2'
  ```

- **Paged Caching**

  For example, when displaying banner images, the backend returns all the data to the client at once since there are only a few records. However, when logged in as a system administrator, a different interface should show all data, including those with switches turned off that are not visible to the client. In this case, all data should be cached so that it can be accessed offline under both roles. This means the data can be retrieved either all at once or in a paged manner.

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

      fun setAdmin(isAdmin: Boolean): BannerRepository {
          this.isAdmin = isAdmin
          return this
      }

      override fun query(): Condition {
          return if (isAdmin) {
              super.query()
          } else {
              // Return all data without paging
              QueryBuilder.create().toCondition()
          }
      }

      override fun onLoadFromNetwork(
          callback: DoraPageListCallback<BannerInfo>,
          // No need to manually handle success; the framework will automatically handle it.
          // However, errors must be handled to display them in the UI, such as a failure during parsing.
          listener: OnLoadStateListener?
      ) {
          if (isAdmin) {
              val req = ReqProductByPage(PRODUCT_NAME, getPageSize(), getPageNo())
              getService(HomeService::class.java).getBanners(req.toRequestBody()).enqueue(
                  PageListResultAdapter<BannerInfo, ApiResult<BannerInfo>>(callback)
                  as Callback<ApiResult<PageDTO<BannerInfo>>>)
          } else {
              getService(HomeService::class.java).getBanners(PRODUCT_NAME).enqueue(
                  PageListResultAdapter<BannerInfo, ApiResult<BannerInfo>>(callback)
                  as Callback<ApiResult<MutableList<BannerInfo>>>)
          }
      }

      override fun createCacheHolderFactory(): DatabaseCacheHolderFactory<BannerInfo> {
          return DatabaseCacheHolderFactory(BannerInfo::class.java)
      }
  }
  ```

  Let's see how this repository is used in practice.

  ```kotlin
  // UI Layer
  binding.slBannerInfoList.setOnSwipeListener(object : SwipeLayout.OnSwipeListener {

      override fun onRefresh(swipeLayout: SwipeLayout) {
      }

      override fun onLoadMore(swipeLayout: SwipeLayout) {
          bannerRepository.onLoadMore {
              swipeLayout.loadMoreFinish(if (it) SwipeLayout.SUCCEED else SwipeLayout.FAIL)
          }
      }
  })
  // Data Layer
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
  // Use the default page size of 10 items and load the first page
  bannerRepository.setAdmin(true).onRefresh()
  ```

  In scenarios where the total number of data items is constantly changing, such as chat messages, we often use a snapshot approach by specifying a timestamp up to which data is considered fixed in size. In this case, the interface would need to include an additional timestamp parameter, and the cache would also need to consider this timestamp for data filtering.
