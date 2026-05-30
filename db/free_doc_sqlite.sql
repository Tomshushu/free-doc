PRAGMA foreign_keys = OFF;

-- ----------------------------
-- Table structure for doc
-- ----------------------------
DROP TABLE IF EXISTS doc;
CREATE TABLE doc (
  doc_id TEXT NOT NULL,
  directory_id TEXT,
  doc_icon TEXT,
  doc_title TEXT,
  doc_content TEXT,
  team_id TEXT,
  project_id TEXT,
  create_user TEXT,
  create_time TEXT,
  update_user TEXT,
  update_time TEXT,
  PRIMARY KEY (doc_id)
);
CREATE INDEX idx_doc_directory_id ON doc(directory_id);
CREATE INDEX idx_doc_team_id ON doc(team_id);
CREATE INDEX idx_doc_project_id ON doc(project_id);

-- ----------------------------
-- Table structure for doc_comments
-- ----------------------------
DROP TABLE IF EXISTS doc_comments;
CREATE TABLE doc_comments (
  comment_id TEXT NOT NULL,
  doc_id TEXT NOT NULL,
  parent_comment_id TEXT,
  content TEXT NOT NULL,
  create_by TEXT NOT NULL,
  create_time TEXT DEFAULT (datetime('now')),
  update_time TEXT DEFAULT (datetime('now')),
  PRIMARY KEY (comment_id),
  FOREIGN KEY (parent_comment_id) REFERENCES doc_comments(comment_id) ON DELETE CASCADE
);
CREATE INDEX idx_comments_doc_id ON doc_comments(doc_id);
CREATE INDEX idx_comments_parent ON doc_comments(parent_comment_id);
CREATE INDEX idx_comments_create_time ON doc_comments(create_time);

-- ----------------------------
-- Table structure for doc_directory
-- ----------------------------
DROP TABLE IF EXISTS doc_directory;
CREATE TABLE doc_directory (
  id TEXT NOT NULL,
  name TEXT,
  pid TEXT,
  pids TEXT,
  project_id TEXT,
  team_id TEXT,
  create_user TEXT,
  create_time TEXT,
  update_time TEXT,
  update_user TEXT,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for doc_project
-- ----------------------------
DROP TABLE IF EXISTS doc_project;
CREATE TABLE doc_project (
  project_id TEXT NOT NULL,
  project_name TEXT,
  project_icon TEXT,
  team_id TEXT,
  project_desc TEXT,
  create_user TEXT,
  create_time TEXT,
  PRIMARY KEY (project_id)
);
CREATE INDEX idx_doc_project_team_id ON doc_project(team_id);

-- ----------------------------
-- Table structure for doc_project_user
-- ----------------------------
DROP TABLE IF EXISTS doc_project_user;
CREATE TABLE doc_project_user (
  id TEXT NOT NULL,
  project_id TEXT,
  user_id TEXT,
  type TEXT,
  permission TEXT,
  PRIMARY KEY (id)
);
CREATE INDEX idx_project_user_project ON doc_project_user(project_id);
CREATE INDEX idx_project_user_user ON doc_project_user(user_id);

-- ----------------------------
-- Table structure for doc_share
-- ----------------------------
DROP TABLE IF EXISTS doc_share;
CREATE TABLE doc_share (
  share_id TEXT NOT NULL,
  share_code TEXT NOT NULL,
  target_type TEXT NOT NULL,
  target_id TEXT NOT NULL,
  password TEXT,
  expire_time TEXT,
  create_user TEXT,
  create_time TEXT,
  PRIMARY KEY (share_id)
);
CREATE UNIQUE INDEX uk_share_code ON doc_share(share_code);
CREATE INDEX idx_share_target ON doc_share(target_type, target_id);

-- ----------------------------
-- Table structure for doc_team
-- ----------------------------
DROP TABLE IF EXISTS doc_team;
CREATE TABLE doc_team (
  team_id TEXT NOT NULL,
  team_name TEXT,
  team_icon TEXT,
  team_desc TEXT,
  create_user TEXT,
  create_time TEXT,
  PRIMARY KEY (team_id)
);

-- ----------------------------
-- Table structure for doc_team_user
-- ----------------------------
DROP TABLE IF EXISTS doc_team_user;
CREATE TABLE doc_team_user (
  id TEXT NOT NULL,
  user_id TEXT,
  team_id TEXT,
  type TEXT,
  is_default INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for doc_tmp
-- ----------------------------
DROP TABLE IF EXISTS doc_tmp;
CREATE TABLE doc_tmp (
  tmp_id TEXT NOT NULL,
  team_id TEXT,
  project_id TEXT,
  tmp_icon TEXT,
  tmp_title TEXT,
  tmp_content TEXT,
  type TEXT,
  create_user TEXT,
  create_time TEXT,
  PRIMARY KEY (tmp_id)
);

-- ----------------------------
-- Table structure for doc_user
-- ----------------------------
DROP TABLE IF EXISTS doc_user;
CREATE TABLE doc_user (
  user_id TEXT NOT NULL,
  user_name TEXT,
  user_icon TEXT,
  account TEXT,
  password TEXT,
  salt TEXT,
  create_user TEXT,
  create_time TEXT,
  PRIMARY KEY (user_id)
);

-- ----------------------------
-- Table structure for doc_version
-- ----------------------------
DROP TABLE IF EXISTS doc_version;
CREATE TABLE doc_version (
  version_id TEXT NOT NULL,
  version_num INTEGER,
  doc_id TEXT,
  doc_content TEXT,
  diff_content TEXT,
  is_current INTEGER,
  content_hash TEXT,
  create_time TEXT,
  create_user TEXT,
  PRIMARY KEY (version_id)
);

PRAGMA foreign_keys = ON;
