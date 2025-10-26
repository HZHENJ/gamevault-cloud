-- Social 服务数据库
CREATE DATABASE gamevault_social;

-- Shopping 服务数据库
CREATE DATABASE gamevault_shopping;

-- Forum 服务数据库
CREATE DATABASE gamevault_forum;

CREATE DATABASE gamevault_developer;

GRANT ALL PRIVILEGES ON DATABASE gamevault_social TO gamevault_user;
GRANT ALL PRIVILEGES ON DATABASE gamevault_shopping TO gamevault_user;
GRANT ALL PRIVILEGES ON DATABASE gamevault_forum TO gamevault_user;
GRANT ALL PRIVILEGES ON DATABASE gamevault_developer TO gamevault_user;