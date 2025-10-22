-- ================================================================
-- GameVault 完整数据库结构
-- 包含核心业务表和论坛功能表
-- ================================================================
\c gamevault_forum;
-- 设置基本配置
SET timezone = 'Asia/Singapore';
SET client_encoding = 'UTF8';

-- ================================================================
-- 1. 论坛功能表
-- ================================================================

-- 内容表（通用内容实体，支持帖子、回复、评论等）
CREATE TABLE IF NOT EXISTS contents (
                                        content_id BIGSERIAL PRIMARY KEY,
                                        content_type VARCHAR(20) NOT NULL,
    title VARCHAR(200),
    body TEXT NOT NULL,
    body_plain TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT,
    reply_to BIGINT,
    status VARCHAR(20) DEFAULT 'active',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contents_parent FOREIGN KEY (parent_id) REFERENCES contents(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_contents_reply_to FOREIGN KEY (reply_to) REFERENCES contents(content_id) ON DELETE SET NULL
    );

-- 属性定义表（定义可用的属性类型）
CREATE TABLE IF NOT EXISTS attribute_definitions (
                                                     attr_id SERIAL PRIMARY KEY,
                                                     attr_name VARCHAR(50) UNIQUE NOT NULL,
    attr_type VARCHAR(20) NOT NULL,
    description TEXT,
    is_required BOOLEAN DEFAULT false,
    default_value TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 内容属性表（存储具体的属性值）
CREATE TABLE IF NOT EXISTS content_attributes (
                                                  id SERIAL PRIMARY KEY,
                                                  content_id BIGINT,
                                                  attr_id INTEGER,
                                                  attr_value TEXT,
                                                  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  CONSTRAINT fk_content_attrs_content FOREIGN KEY (content_id) REFERENCES contents(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_attrs_attr FOREIGN KEY (attr_id) REFERENCES attribute_definitions(attr_id) ON DELETE CASCADE,
    UNIQUE(content_id, attr_id)
    );

-- 统计类型定义
CREATE TABLE IF NOT EXISTS metric_definitions (
                                                  metric_id SERIAL PRIMARY KEY,
                                                  metric_name VARCHAR(50) UNIQUE NOT NULL,
    metric_type VARCHAR(20) NOT NULL,
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 内容统计表
CREATE TABLE IF NOT EXISTS content_metrics (
                                               id SERIAL PRIMARY KEY,
                                               content_id BIGINT,
                                               metric_id INTEGER,
                                               metric_value INTEGER DEFAULT 0,
                                               updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               CONSTRAINT fk_content_metrics_content FOREIGN KEY (content_id) REFERENCES contents(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_metrics_metric FOREIGN KEY (metric_id) REFERENCES metric_definitions(metric_id) ON DELETE CASCADE,
    UNIQUE(content_id, metric_id)
    );

-- 关系类型定义
CREATE TABLE IF NOT EXISTS relationship_types (
                                                  type_id SERIAL PRIMARY KEY,
                                                  type_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 用户-内容关系表
CREATE TABLE IF NOT EXISTS user_content_relations (
                                                      id SERIAL PRIMARY KEY,
                                                      user_id BIGINT,
                                                      content_id BIGINT,
                                                      relation_type_id INTEGER,
                                                      created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ucr_content FOREIGN KEY (content_id) REFERENCES contents(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_ucr_type FOREIGN KEY (relation_type_id) REFERENCES relationship_types(type_id) ON DELETE CASCADE,
    UNIQUE(user_id, content_id, relation_type_id)
    );

-- ================================================================
-- 3. 创建索引
-- ================================================================


-- 内容表索引
CREATE INDEX IF NOT EXISTS idx_contents_type ON contents(content_type);
CREATE INDEX IF NOT EXISTS idx_contents_author ON contents(author_id);
CREATE INDEX IF NOT EXISTS idx_contents_parent ON contents(parent_id);
CREATE INDEX IF NOT EXISTS idx_contents_status ON contents(status);
CREATE INDEX IF NOT EXISTS idx_contents_created ON contents(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_contents_type_status ON contents(content_type, status);
CREATE INDEX IF NOT EXISTS idx_contents_reply_to ON contents(reply_to);

-- 属性查询索引
CREATE INDEX IF NOT EXISTS idx_content_attrs_content ON content_attributes(content_id);
CREATE INDEX IF NOT EXISTS idx_content_attrs_attr ON content_attributes(attr_id);

-- 统计查询索引
CREATE INDEX IF NOT EXISTS idx_content_metrics_content ON content_metrics(content_id);
CREATE INDEX IF NOT EXISTS idx_content_metrics_metric ON content_metrics(metric_id);

-- 关系查询索引
CREATE INDEX IF NOT EXISTS idx_relations_user ON user_content_relations(user_id);
CREATE INDEX IF NOT EXISTS idx_relations_content ON user_content_relations(content_id);
CREATE INDEX IF NOT EXISTS idx_relations_type ON user_content_relations(relation_type_id);


-- ================================================================
-- 5. 初始化系统数据
-- ================================================================

-- 初始化属性定义
INSERT INTO attribute_definitions (attr_name, attr_type, description, is_required) VALUES
                                                                                       ('category', 'string', '帖子分类', false),
                                                                                       ('forum', 'string', '所属论坛', false),
                                                                                       ('tags', 'json', '标签列表', false),
                                                                                       ('priority', 'integer', '优先级', false),
                                                                                       ('is_pinned', 'boolean', '是否置顶', false),
                                                                                       ('is_locked', 'boolean', '是否锁定', false),
                                                                                       ('game_title', 'string', '相关游戏名称', false),
                                                                                       ('difficulty_level', 'integer', '难度等级', false),
                                                                                       ('platform', 'string', '游戏平台', false)
    ON CONFLICT (attr_name) DO NOTHING;

-- 初始化统计类型
INSERT INTO metric_definitions (metric_name, metric_type, description) VALUES
                                                                           ('view_count', 'counter', '浏览次数'),
                                                                           ('like_count', 'counter', '点赞数量'),
                                                                           ('reply_count', 'counter', '回复数量'),
                                                                           ('share_count', 'counter', '分享次数'),
                                                                           ('bookmark_count', 'counter', '收藏次数'),
                                                                           ('report_count', 'counter', '举报次数'),
                                                                           ('score', 'score', '综合评分')
    ON CONFLICT (metric_name) DO NOTHING;

-- 初始化关系类型
INSERT INTO relationship_types (type_name, description) VALUES
                                                            ('like', '用户点赞内容'),
                                                            ('bookmark', '用户收藏内容'),
                                                            ('follow', '用户关注内容'),
                                                            ('report', '用户举报内容'),
                                                            ('view', '用户浏览内容')
    ON CONFLICT (type_name) DO NOTHING;

-- ================================================================
-- 6. 创建视图和函数
-- ================================================================

-- 帖子列表视图
-- 帖子列表视图
CREATE OR REPLACE VIEW post_list_view AS
SELECT
    c.content_id as post_id,
    c.title,
    c.body as content,
    c.body_plain as content_plain,
    c.created_date,
    c.status as is_active,
    c.author_id,
    COALESCE(ca_category.attr_value, '未分类') as category,
    COALESCE(cm_views.metric_value, 0) as view_count,
    COALESCE(cm_likes.metric_value, 0) as like_count,
    COALESCE(cm_replies.metric_value, 0) as reply_count
FROM contents c
         LEFT JOIN content_attributes ca_category ON (
    c.content_id = ca_category.content_id
        AND ca_category.attr_id = (SELECT attr_id FROM attribute_definitions WHERE attr_name = 'category')
    )
         LEFT JOIN content_metrics cm_views ON (
    c.content_id = cm_views.content_id
        AND cm_views.metric_id = (SELECT metric_id FROM metric_definitions WHERE metric_name = 'view_count')
    )
         LEFT JOIN content_metrics cm_likes ON (
    c.content_id = cm_likes.content_id
        AND cm_likes.metric_id = (SELECT metric_id FROM metric_definitions WHERE metric_name = 'like_count')
    )
         LEFT JOIN content_metrics cm_replies ON (
    c.content_id = cm_replies.content_id
        AND cm_replies.metric_id = (SELECT metric_id FROM metric_definitions WHERE metric_name = 'reply_count')
    )
WHERE c.content_type = 'post' AND c.status = 'active'
ORDER BY c.created_date DESC;

-- 增加统计指标的函数
CREATE OR REPLACE FUNCTION increment_metric(p_content_id BIGINT, p_metric_name VARCHAR)
RETURNS void AS $$
BEGIN
INSERT INTO content_metrics (content_id, metric_id, metric_value)
VALUES (
           p_content_id,
           (SELECT metric_id FROM metric_definitions WHERE metric_name = p_metric_name),
           1
       )
    ON CONFLICT (content_id, metric_id)
    DO UPDATE SET
    metric_value = content_metrics.metric_value + 1,
               updated_date = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- 添加属性的函数
CREATE OR REPLACE FUNCTION set_content_attribute(
    p_content_id BIGINT,
    p_attr_name VARCHAR,
    p_attr_value TEXT
)
RETURNS void AS $$
BEGIN
INSERT INTO content_attributes (content_id, attr_id, attr_value)
VALUES (
           p_content_id,
           (SELECT attr_id FROM attribute_definitions WHERE attr_name = p_attr_name),
           p_attr_value
       )
    ON CONFLICT (content_id, attr_id)
    DO UPDATE SET
    attr_value = p_attr_value,
               created_date = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

