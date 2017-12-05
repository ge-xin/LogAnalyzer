CREATE TABLE log (ip VARCHAR(45) NOT NULL,  date TIMESTAMP(3), request VARCHAR(255), status SMALLINT, user_agent VARCHAR(255), PRIMARY KEY (ip, date));
CREATE TABLE block (ip VARCHAR(45) NOT NULL,  reason VARCHAR(255), PRIMARY KEY (ip));

# (1) Write MySQL query to find IPs that mode more than a certain number of requests for a given time period.
SELECT `ip` FROM (
	SELECT `ip`, `date` FROM `log`
    WHERE `date` >= '2017-01-01.13:00:00' AND `date` <= '2017-01-01.14:00:00'
)visit_record GROUP BY `ip` HAVING COUNT(`date`) >= 100

#(2) Write MySQL query to find requests made by a given IP.
SELECT * FROM `log` WHERE `ip` = "192.168.169.194";