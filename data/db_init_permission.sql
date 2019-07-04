CREATE TABLE IF NOT EXISTS perms_def (
  id int NOT NULL AUTO_INCREMENT,
  name varchar(64) NOT NULL,
  isgroup tinyint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name (name)
);;

CREATE TABLE IF NOT EXISTS perms_roles (
  rid bigint NOT NULL,
  sid bigint NOT NULL,
  pid int NOT NULL,
  PRIMARY KEY (rid,sid,pid)
);;

CREATE TABLE IF NOT EXISTS perms_users (
  uid bigint NOT NULL,
  sid bigint NOT NULL,
  pid int NOT NULL,
  PRIMARY KEY (uid,sid,pid)
);;

DROP PROCEDURE IF EXISTS testUserPerm;;
DROP PROCEDURE IF EXISTS testFullUserPerm;;
DROP PROCEDURE IF EXISTS testRolePerm;;
DROP PROCEDURE IF EXISTS registerPerm;;
DROP PROCEDURE IF EXISTS grantPermToUser;;
DROP PROCEDURE IF EXISTS grantPermToRole;;
DROP PROCEDURE IF EXISTS revokePermFromUser;;
DROP PROCEDURE IF EXISTS revokePermFromRole;;

CREATE PROCEDURE testUserPerm(IN sid BIGINT, IN uid BIGINT, IN perm VARCHAR(64), OUT res BOOLEAN)
proc: BEGIN
	DECLARE nbs INT DEFAULT LENGTH(perm) - LENGTH(REPLACE(perm, '.', '')) + 1;
	DECLARE itr INT DEFAULT 1;
	DECLARE cur INT;

	IF EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = (SELECT id FROM perms_def WHERE name = '*')) THEN
    	SET res = true;
		LEAVE proc;
	END IF;

	WHILE itr <= nbs DO
		SET cur = (SELECT id FROM perms_def WHERE name = SUBSTRING_INDEX(perm, '.', itr));
		
		IF EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = cur) THEN
			SET res = true;
			LEAVE proc;
		END IF;
		
		SET itr = itr + 1;
	END WHILE;
	
	SET res = false;
END
;;

CREATE PROCEDURE testFullUserPerm(IN sid BIGINT, IN uid BIGINT, IN roles VARCHAR(380), IN perm VARCHAR(64), OUT res BOOLEAN)
proc: BEGIN
	DECLARE nbs INT DEFAULT LENGTH(perm) - LENGTH(REPLACE(perm, '.', '')) + 1;
	DECLARE itr INT DEFAULT 1;
	DECLARE cur INT;
	DECLARE root_pid BIGINT;
	SELECT id INTO root_pid FROM perms_def WHERE name = '*';
	
	IF EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = root_pid) THEN
    	SET res = true;
		LEAVE proc;
	END IF;
	
	IF EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND FIND_IN_SET(rid, roles) AND pid = root_pid) THEN
    	SET res = true;
		LEAVE proc;
	END IF;
	
	WHILE itr <= nbs DO
		SET cur = (SELECT id FROM perms_def WHERE name = SUBSTRING_INDEX(perm, '.', itr));
		
		IF EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND FIND_IN_SET(rid, roles) AND pid = cur) THEN
			SET res = true;
			LEAVE proc;
		END IF;
		
		IF EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = cur) THEN
			SET res = true;
			LEAVE proc;
		END IF;
		
		SET itr = itr + 1;
	END WHILE;
	
	SET res = false;
END
;;

CREATE PROCEDURE testRolePerm(IN sid BIGINT, IN rid BIGINT, IN perm VARCHAR(64), OUT res BOOLEAN)
proc: BEGIN
	DECLARE nbs INT DEFAULT LENGTH(perm) - LENGTH(REPLACE(perm, '.', '')) + 1;
	DECLARE itr INT DEFAULT 1;
	DECLARE cur INT;
	
	IF EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND rid = rid AND pid = (SELECT id FROM perms_def WHERE name = '*')) THEN
    	SET res = true;
		LEAVE proc;
	END IF;
	
	WHILE itr <= nbs DO
		SET cur = (SELECT id FROM perms_def WHERE name = SUBSTRING_INDEX(perm, '.', itr));
		
		IF EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND rid = rid AND pid = cur) THEN
			SET res = true;
			LEAVE proc;
		END IF;
		
		SET itr = itr + 1;
	END WHILE;
	
	SET res = false;
END
;;

CREATE PROCEDURE registerPerm(IN perm VARCHAR(64))
BEGIN
	DECLARE nbs INT DEFAULT LENGTH(perm) - LENGTH(REPLACE(perm, '.', '')) + 1;
	DECLARE itr INT DEFAULT 1;
	DECLARE cur VARCHAR(64);
	
	WHILE itr < nbs DO
		SET cur = SUBSTRING_INDEX(perm, '.', itr);
		INSERT INTO perms_def (name, isgroup) VALUES (cur, true) ON DUPLICATE KEY UPDATE isgroup = true;
		SET itr = itr + 1;
	END WHILE;
	
	SET cur = SUBSTRING_INDEX(perm, '.', itr);
	INSERT IGNORE INTO perms_def (name, isgroup) VALUES (cur, false);
END
;;

CREATE PROCEDURE grantPermToUser(IN sid BIGINT, IN uid BIGINT, IN perm VARCHAR(64), OUT res INT)
proc: BEGIN	
	DECLARE nbs INT DEFAULT LENGTH(perm) - LENGTH(REPLACE(perm, '.', '')) + 1;
	DECLARE itr INT DEFAULT 1;
	DECLARE cur INT;
	
	IF NOT EXISTS (SELECT 1 FROM perms_def WHERE name = perm) THEN
		SET res = 4;
		LEAVE proc;
	END IF;
	
	
    IF EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = (SELECT id FROM perms_def WHERE name = '*')) THEN
    	SET res = 1;
		LEAVE proc;
	END IF;
	
	WHILE itr <= nbs DO
		SET cur = (SELECT id FROM perms_def WHERE name = SUBSTRING_INDEX(perm, '.', itr));
		
		IF EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = cur) THEN
			SET res = 1;
			LEAVE proc;
		END IF;
		
		SET itr = itr + 1;
	END WHILE;
	
	INSERT INTO perms_users (sid, uid, pid) VALUES (sid, uid, cur);
	SET res = 0;
END
;;

CREATE PROCEDURE grantPermToRole(IN sid BIGINT, IN rid BIGINT, IN perm VARCHAR(64), OUT res INT)
proc: BEGIN	
	DECLARE nbs INT DEFAULT LENGTH(perm) - LENGTH(REPLACE(perm, '.', '')) + 1;
	DECLARE itr INT DEFAULT 1;
	DECLARE cur INT;
	
	IF NOT EXISTS (SELECT 1 FROM perms_def WHERE name = perm) THEN
		SET res = 4;
		LEAVE proc;
	END IF;
	
	IF EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND rid = rid AND pid = (SELECT id FROM perms_def WHERE name = '*')) THEN
    	SET res = 1;
		LEAVE proc;
	END IF;
	
	WHILE itr <= nbs DO
		SET cur = (SELECT id FROM perms_def WHERE name = SUBSTRING_INDEX(perm, '.', itr));
		
		IF EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND rid = rid AND pid = cur) THEN
			SET res = 1;
			LEAVE proc;
		END IF;
		
		SET itr = itr + 1;
	END WHILE;
	
	INSERT INTO perms_roles (sid, rid, pid) VALUES (sid, rid, cur);
	SET res = 0;
END
;;

CREATE PROCEDURE revokePermFromUser(IN sid BIGINT, IN uid BIGINT, IN perm VARCHAR(64), OUT res INT)
proc: BEGIN
	DECLARE ppid INT DEFAULT (SELECT id FROM perms_def WHERE name = perm);
	
	IF ppid IS NULL THEN
		SET res = 4;
		LEAVE proc;
	END IF;
	
	IF NOT EXISTS (SELECT 1 FROM perms_users WHERE sid = sid AND uid = uid AND pid = ppid) THEN
		SET res = 1;
		LEAVE proc;
	END IF;
	
	DELETE FROM perms_users WHERE sid = sid AND uid = uid AND pid IN (SELECT id FROM perms_def WHERE name LIKE CONCAT(perm, '%'));
	SET res = 0;
END
;;

CREATE PROCEDURE revokePermFromRole(IN sid BIGINT, IN rid BIGINT, IN perm VARCHAR(64), OUT res INT)
proc: BEGIN
	DECLARE ppid INT DEFAULT (SELECT id FROM perms_def WHERE name = perm);
	
	IF ppid IS NULL THEN
		SET res = 4;
		LEAVE proc;
	END IF;
	
	IF NOT EXISTS (SELECT 1 FROM perms_roles WHERE sid = sid AND rid = rid AND pid = ppid) THEN
		SET res = 1;
		LEAVE proc;
	END IF;
	
	DELETE FROM perms_roles WHERE sid = sid AND rid = rid AND pid IN (SELECT id FROM perms_def WHERE name LIKE CONCAT(perm, '%'));
	SET res = 0;
END
;;
