-- 这个脚本只在 PostgreSQL 容器首次启动时执行一次

-- Profile 服务数据库
CREATE DATABASE gamevault_profile;

-- Library 服务数据库
CREATE DATABASE gamevault_library;

-- Social 服务数据库
CREATE DATABASE gamevault_social;

-- Messaging 服务数据库
CREATE DATABASE gamevault_messaging;

-- File 服务数据库
CREATE DATABASE gamevault_file;

-- Shopping 服务数据库
CREATE DATABASE gamevault_shopping;

-- Forum 服务数据库
CREATE DATABASE gamevault_forum;

CREATE DATABASE gamevault_developer;