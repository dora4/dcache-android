<a href="./README.zh-CN.md">简体中文</a> ｜ <a href="./README.md">English</a>

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
// Stable version 2.4.13, for the latest version use the version successfully compiled on Jitpack (marked in green).
def stable_version = '2.4.13'
implementation "com.github.dora4:dcache-android:$stable_version"
```

### Documentation (Usage Example: https://github.com/dora4/DoraCacheSample)

#### 1. dcache ORM Details

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

   Use the `OrmDao` object to perform CRUD operations on a table. The `OrmDao` object can be obtained via `DaoFactory.getDao()`. Typically, `OrmDao` is saved as a member variable for reuse.

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

   - **Counting Records**

     ```kotlin
     val num1 = DaoFactory.getDao(User::class.java).count(queryBuilder)
     val num2 = DaoFactory.getDao(User::class.java).count(whereBuilder)
     val num3 = DaoFactory.getDao(User::class.java).count(condition)
     val num4 = DaoFactory.getDao(User::class.java).countAll()
     ```

     Use the count series methods to query the number of records.

#### 2. Reading and Parsing Network Data

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

