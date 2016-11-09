package com.alibaba.aliyun.crazyacking.spider.common;

import com.alibaba.aliyun.crazyacking.spider.queue.CommentUrlQueue;
import com.alibaba.aliyun.crazyacking.spider.queue.FollowUrlQueue;
import com.alibaba.aliyun.crazyacking.spider.queue.RepostUrlQueue;
import com.alibaba.aliyun.crazyacking.spider.queue.WeiboUrlQueue;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by crazyacking on 2015/4/3.
 */
@Component("initializer")
public class Initializer {

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
    private static Connection conn = null;

    /**
     * 数据库中读取用户账号，并生成第一页微博的url，放入WeiboUrlQueue
     */
    public static synchronized void initWeiboUrl() {
        String querySql = "SELECT accountID FROM INIT_USER WHERE isFetched = 0";
        PreparedStatement ps;
        Statement st;
        ResultSet rs;
        String accountID = null;

        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            st = conn.createStatement();
            rs = st.executeQuery(querySql);
            if (rs.next()) {
                accountID = rs.getString("accountID");
                ps = conn.prepareStatement("UPDATE INIT_USER SET isFetched = 1 WHERE accountID = ?");
                ps.setString(1, accountID);
                ps.execute();
                ps.close();
            }
            rs.close();
            st.close();

            conn.commit();
            if (accountID != null) {
                /*
                * 将初始用户的粉丝页面的Url入队
				* */
                logger.info("accountID=" + accountID);
                WeiboUrlQueue.addElement("http://weibo.com/p/100505" + accountID + "/follow?relate=fans&from=100505&wvr=6&mod=headfans&current=fans#place");
                logger.info("the url is =   " + "http://weibo.com/p/100505" + accountID + "/follow?relate=fans&from=100505&wvr=6&mod=headfans&current=fans#place");
            }
        } catch (SQLException e) {
            logger.error("", e);
            /*
            提交失败 roll back，并将放入队列的URL拿出来
             */
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.toString());
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    /**
     * 数据库中读取用微博账号，并生成第一页评论的url，放入CommentUrlQueue
     */
    public static synchronized void initCommentUrl() {
        String querySql = "SELECT weiboID FROM weibo WHERE isCommentFetched = 0 LIMIT 1";
        PreparedStatement ps;
        Statement st;
        ResultSet rs;
        String weiboID = null;

        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            st = conn.createStatement();
            rs = st.executeQuery(querySql);
            if (rs.next()) {
                weiboID = rs.getString("weiboID");
                ps = conn.prepareStatement("UPDATE weibo SET isCommentFetched = 1 WHERE weiboID = ?");
                ps.setString(1, weiboID);
                ps.execute();
                ps.close();
            }
            rs.close();
            st.close();

            conn.commit();
            if (weiboID != null) {
                CommentUrlQueue.addElement(Constants.COMMENT_BASE_STR + weiboID + "?page=1");
            }
        } catch (SQLException e) {
            logger.error("", e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.toString());
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    /**
     * 数据库中读取微博账号，并生成第一页转发的url，放入WeiboUrlQueue
     */
    public static synchronized void initRepostUrl() {
        String querySql = "SELECT weiboID FROM weibo WHERE isRepostFetched = 0 LIMIT 1";
        PreparedStatement ps;
        Statement st;
        ResultSet rs;
        String weiboID = null;

        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            st = conn.createStatement();
            rs = st.executeQuery(querySql);
            if (rs.next()) {
                weiboID = rs.getString("weiboID");
                ps = conn.prepareStatement("UPDATE weibo SET isRepostFetched = 1 WHERE weiboID = ?");
                ps.setString(1, weiboID);
                ps.execute();
                ps.close();
            }
            rs.close();
            st.close();

            conn.commit();
            if (weiboID != null) {
                RepostUrlQueue.addElement(Constants.REPOST_BASE_STR + weiboID + "?page=1");
            }
        } catch (SQLException e) {
            logger.error("", e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.toString());
            }
        }
    }

    /**
     * 从account.txt中读取用户账号，并生成用户主页的url，放入AccountInfoUrlQueue
     */
    public static void initAbnormalUrl() {
        try {
            List<String> lines = FileUtils.readLines(new File(Constants.ABNORMAL_WEIBO_CLEANED_PATH));
            for (String line : lines) {
                WeiboUrlQueue.addElement(line);
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public static int initFollowUrl() {
        String querySql = "SELECT follower, LEVEL FROM follower WHERE isFetched = 0 ORDER BY LEVEL ASC LIMIT 1";
        PreparedStatement ps;
        Statement st;
        ResultSet rs;
        String followerID = null;
        int level = Integer.MAX_VALUE;

        try {
            /*
            获取本轮follower，level
             */
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            st = conn.createStatement();
            rs = st.executeQuery(querySql);
            if (rs.next()) {
                followerID = rs.getString("follower");
                level = rs.getInt("level");
                ps = conn.prepareStatement("UPDATE follower SET isFetched = 1 WHERE follower = ?");
                ps.setString(1, followerID);
                ps.execute();
                ps.close();
            }
            rs.close();
            st.close();

            conn.commit();
            /*
            当本轮level < Constants.LEVEL，才添加队列URL
             */
            if (level < Constants.LEVEL) {
                FollowUrlQueue.addElement("http://weibo.cn/" + followerID + "/follow");
            }
        } catch (SQLException e) {
            logger.error("", e);
            /*
            提交失败 roll back
             */
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.toString());
            }
        }

        return level;
    }

    /**
     * 从配置文件中读取配置信息：数据库连接、相关文件根目录、爬虫任务类型
     */
    @PostConstruct
    public void initParams() {
        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream("classpath:conf\\spider.properties"));
            Properties properties = new Properties();
            properties.load(in);

            System.out.println(properties.size());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                System.out.println("key:" + entry.getKey());
                System.out.println("value:" + entry.getValue());
            }

            /*
            从配置文件中读取数据库连接参数
             */
            DBConnector.DB_URL = properties.getProperty("db.url");
            DBConnector.DB_NAME = properties.getProperty("db.name");
            DBConnector.DB_USER = properties.getProperty("db.username");
            DBConnector.DB_PASSWD = properties.getProperty("db.password");

            /*
            从配置文件中读取根目录，并设置相关文件地址
             */
            Constants.ROOT_DISK = properties.getProperty("spider.rootDisk");
            Constants.REPOST_LOG_PATH = Constants.ROOT_DISK + "repost_log.txt";
            Constants.COMMENT_LOG_PATH = Constants.ROOT_DISK + "comment_log.txt";
            Constants.SWITCH_ACCOUNT_LOG_PATH = Constants.ROOT_DISK + "switch_account_log.txt";
            Constants.ACCOUNT_PATH = Constants.ROOT_DISK + "account.txt";
            Constants.ACCOUNT_RESULT_PATH = Constants.ROOT_DISK + "account_result.txt";
            Constants.LOGIN_ACCOUNT_PATH = Constants.ROOT_DISK + "login_account.txt";
            Constants.ABNORMAL_ACCOUNT_PATH = Constants.ROOT_DISK + "abnormal_account.txt";
            Constants.ABNORMAL_WEIBO_PATH = Constants.ROOT_DISK + "abnormal_weibo.txt";
            Constants.ABNORMAL_WEIBO_CLEANED_PATH = Constants.ROOT_DISK + "abnormal_weibo_cleaned.txt";

            /*
            从配置文件中读取微博相关参数
             */
            Constants.CHECK_WEIBO_NUM = Boolean.parseBoolean(properties.getProperty("weibo.checkWeiboNum", "false"));
            if (Constants.CHECK_WEIBO_NUM) {
                Constants.WEIBO_NO_MORE_THAN = Integer.parseInt(properties.getProperty("weibo.maxWeiboNum"));
            }
            in.close();

            conn = DBConnector.getConnection();

        } catch (FileNotFoundException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
