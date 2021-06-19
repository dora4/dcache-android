# dcache-android
一个使用在Android平台的数据缓存框架，支持将model数据从后端接口下载后，简单的配置即可自动映射到数据库，并在断网的情况下可以离线读取


  dora-db的orm相关代码

  ```java
  @Table("pop_music")
  public class PopMusic implements OrmTable {

      @Id
      long id;
      @Column("music_name")
      String musicName;
      @Column("music_artist")
      String musicArtist;

      public PopMusic(String name, String artist) {
          this.musicName = name;
          this.musicArtist = artist;
      }

      public String getMusicName() {
          return musicName;
      }

      public void setMusicName(String musicName) {
          this.musicName = musicName;
      }

      public String getMusicArtist() {
          return musicArtist;
      }

      public void setMusicArtist(String musicArtist) {
          this.musicArtist = musicArtist;
      }

      @Override
      public PrimaryKeyEntity getPrimaryKey() {
          return new PrimaryKeyId(id);
      }

      @Override
      public boolean isUpgradeRecreated() {
          return false;
      }
  }
  ```

  dora-cache的缓存逻辑相关代码

  ```java
  public class PopMusicRepository extends BaseDatabaseCacheRepository<PopMusic> {

      @Inject
      public PopMusicRepository() {
          super(PopMusic.class);
      }

      /**
       * 告诉框架怎么加载这部分数据。
       *
       * @param callback
       */
      @Override
      protected void onLoadFromNetwork(DoraListCallback<PopMusic> callback) {
          RetrofitManager.getService(MusicService.class).popMusicGet().enqueue(callback);
      }

      /**
       * 数据过滤条件，从数据库查询出来之前会先过滤不要的数据。
       */
      @Override
      protected WhereBuilder where() {
          return super.where();
      }
  }
  ```

  界面调用逻辑代码（如果不为集合数据，则调用getData()方法）

  ```java
          repository.getListData().observe(this, new Observer<List<PopMusic>>() {
              @Override
              public void onChanged(List<PopMusic> popMusics) {
                  ViewUtils.configRecyclerView(mBinding.rvDataCache).setAdapter(new PopMusicAdapter(popMusics));
              }
          });
  ```


如果帮您节省了大量的开发时间，对您有所帮助，欢迎您的赞赏！

捐赠虚拟货币

柚子(EOS): 钱包地址 - doramusic123 , 备注TAG - 你的github用户名
USDT(TRC-20链): 钱包地址 - TYVXzqctSSPSTeVPYg7i7qbEtSxwrAJQ5y
以太坊(ETH): 钱包地址 - 0x5dA12D7DB7B5D6C8D2Af78723F6dCE4A3C89caB9
