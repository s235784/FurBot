# FurBot
 一个简单的QQ机器人

功能列表

- [x] 发送随机的Pixiv画师作品
- [x] 发送指定的Pixiv画师作品
- [x] 查找图片出处（SauceNAO）
- [x] 留言
- [ ] 发送Twitter图片



## 搭建

### 环境配置

要运行本项目请先安装好以下依赖

- IDEA
- Openjdk11 （不低于jdk8）
- Mysql （MariaDB）
- Redis

### 转发API配置

因大陆无法正常访问Pixiv，需要使用一台在海外的服务器来转发获取Pixiv图片的请求，把[此项目](https://github.com/s235784/FurBotAPI)的`picture.php`丢到服务器上即可。（需要php运行环境）

### 数据库配置

新建`furbot`数据库，导入sql/furbot.sql文件，然后插入以下数据

```sql
INSERT INTO `api_setting` (`id`, `api_name`, `api_url`) VALUES
(1, 'hibi_pixiv_member', 'https://api.obfs.dev/api/pixiv/member'),
(2, 'hibi_pixiv_member_illust', 'https://api.obfs.dev/api/pixiv/member_illust'),
(3, 'bot_picture', 'https://xxxx.com/picture.php(转发api的地址)'),
(4, 'hibi_sauce_from', 'https://api.obfs.dev/api/sauce/');

INSERT INTO `global_setting` (`id`, `setting_name`, `setting_value`) VALUES
(1, 'super_admin_qq', '管理员的qq');
```

以上api中以`hibi`开头的，均来自于这个项目[HibiAPI](https://github.com/mixmoe/HibiAPI)，如果官方的api地址不能用了，也可以考虑自己搭建HibiAPI。

### 软件配置

然后clone本仓库或者下载仓库代码，并用IDEA打开。在src/main/resources目录下创建`application-dev.yml`文件，并写入以下信息

```yaml
spring:
  datasource:
    username: user  # 数据库用户名
    password: pass  # 数据库密码
    url: jdbc:mysql://localhost:3306/furbot?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver

bot:
  number: 1234567898  # 机器人QQ号
  password: 1234567898  # 机器人QQ密码
```

完成之后即可运行查看效果。



## 感谢

- [Mirai - 提供 QQ Android 协议支持的高效率机器人库](https://github.com/mamoe/mirai)
- [HibiAPI - 一个实现了多种常用站点的易用化 API 的程序](https://github.com/mixmoe/HibiAPI)



## 许可证

``` license
Copyright 2022, NuoTian       

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
