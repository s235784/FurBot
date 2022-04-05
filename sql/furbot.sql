-- phpMyAdmin SQL Dump
-- version 5.1.3
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2022-04-05 21:32:05
-- 服务器版本： 5.7.26
-- PHP 版本： 7.3.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `furbot`
--

-- --------------------------------------------------------

--
-- 表的结构 `api_setting`
--

CREATE TABLE `api_setting` (
  `id` int(10) NOT NULL,
  `api_name` text COLLATE utf8_unicode_ci NOT NULL,
  `api_url` text COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `global_setting`
--

CREATE TABLE `global_setting` (
  `id` int(10) NOT NULL,
  `setting_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `setting_value` text COLLATE utf8_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `group_setting`
--

CREATE TABLE `group_setting` (
  `id` int(10) NOT NULL,
  `group_id` int(10) NOT NULL COMMENT 'QQ群号',
  `setting_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '设置的名字',
  `setting_value` text COLLATE utf8_unicode_ci COMMENT '设置的值'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `group_setting_pixiv`
--

CREATE TABLE `group_setting_pixiv` (
  `id` int(11) NOT NULL,
  `group_id` int(10) UNSIGNED NOT NULL COMMENT 'QQ群号',
  `pixiv_id` int(10) DEFAULT NULL COMMENT 'pixiv的用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `pixiv_member`
--

CREATE TABLE `pixiv_member` (
  `id` int(11) NOT NULL,
  `pixiv_id` int(11) NOT NULL,
  `pixiv_account` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT 'pixiv的用户名',
  `pixiv_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'pixiv的昵称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转储表的索引
--

--
-- 表的索引 `api_setting`
--
ALTER TABLE `api_setting`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `global_setting`
--
ALTER TABLE `global_setting`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `group_setting`
--
ALTER TABLE `group_setting`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `group_setting_pixiv`
--
ALTER TABLE `group_setting_pixiv`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `pixiv_member`
--
ALTER TABLE `pixiv_member`
  ADD PRIMARY KEY (`id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `api_setting`
--
ALTER TABLE `api_setting`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `global_setting`
--
ALTER TABLE `global_setting`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `group_setting`
--
ALTER TABLE `group_setting`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `group_setting_pixiv`
--
ALTER TABLE `group_setting_pixiv`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `pixiv_member`
--
ALTER TABLE `pixiv_member`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
