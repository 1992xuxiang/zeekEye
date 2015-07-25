package cn.edu.hut.crazyacking.spider;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import cn.edu.hut.crazyacking.spider.utils.Constants;
import cn.edu.hut.crazyacking.spider.utils.DBConn;
import cn.edu.hut.crazyacking.spider.utils.Utils;
import cn.edu.hut.crazyacking.spider.worker.impl.UrlAbnormalWeiboWorker;
import cn.edu.hut.crazyacking.spider.worker.impl.UrlCommentWorker;
import cn.edu.hut.crazyacking.spider.worker.impl.UrlFollowWorker;
import cn.edu.hut.crazyacking.spider.worker.impl.UrlRepostWorker;
import cn.edu.hut.crazyacking.spider.worker.impl.UrlWeiboWorker;
import cn.edu.hut.crazyacking.spider.parser.WebDataExtraction;
import cn.edu.hut.crazyacking.spider.storage.PageStorage;

/**
 * Created by crazyacking on 2015/3/25.
 */

public class WeiboSpiderStarter {
    private static final Logger Log = Logger.getLogger(WeiboSpiderStarter.class.getName());
    private static int WORKER_NUM = 1;
    private static String TYPE;

    public static void main(String[] args) throws IOException, InterruptedException {


        // ��ʼ�����ò���
        initializeParams();

        // ����type�ж�������������
        if (TYPE.equals("weibo")) {
            fetchWeibo();
        } else if (TYPE.equals("comment")) {
            fetchComment();
        } else if (TYPE.equals("repost")) {
            fetchRepost();
        } else if (TYPE.equals("abnormal")) {
            fetchAbnormalWeibo();
        } else if (TYPE.equals("follow")) {
            fetchFollowee();
        } else {
            Log.error("Unknown crawl type: " + TYPE + ".\n Exit...");
        }


        PageStorage pageStorage = new PageStorage();
        pageStorage.WebPageStorage();

    }

    /**
     * �������ļ��ж�ȡ������Ϣ�����ݿ����ӡ�����ļ���Ŀ¼��������������
     */
    private static void initializeParams() {
        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream("conf\\spider.properties"));
            Properties properties = new Properties();
            properties.load(in);

            // �������ļ��ж�ȡ���ݿ����Ӳ���
            DBConn.CONN_URL = properties.getProperty("DB.connUrl");
            DBConn.DB_NAME = properties.getProperty("DB.name");
            DBConn.USERNAME = properties.getProperty("DB.username");
            DBConn.PASSWORD = properties.getProperty("DB.password");

            // �������ļ��ж�ȡ��Ŀ¼������������ļ���ַ
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

            // �������ļ��ж�ȡ������������
            WeiboSpiderStarter.TYPE = properties.getProperty("spider.type");

            // �������ļ��ж�ȡfollow��ȡ��ز���
            if (TYPE.equals("follow")) {
                Constants.LEVEL = Integer.parseInt(properties.getProperty("follow.level"));
                Constants.FANS_NO_MORE_THAN = Integer.parseInt(properties.getProperty("follow.maxFansNum"));
            }

            // �������ļ��ж�ȡ΢����ز���
            Constants.CHECK_WEIBO_NUM = Boolean.parseBoolean(properties.getProperty("weibo.checkWeiboNum", "false"));
            if (Constants.CHECK_WEIBO_NUM) {
                Constants.WEIBO_NO_MORE_THAN = Integer.parseInt(properties.getProperty("weibo.maxWeiboNum"));
            }

            in.close();
        } catch (FileNotFoundException e) {
            Log.error(e);
        } catch (IOException e) {
            Log.error(e);
        }
    }

    private static void fetchWeibo() {
        Log.info("\n\n\n===========================================================\n\t\t������������ĺ����Ƽ�ϵͳ\n\t\t\t\t ----------------------zeekEye    \n===========================================================\n");

        // ��ʼ���˺Ŷ���
        /*
        * ���ļ��е��˺Ŷ��뵽AccountQueue��
		* */
        Utils.readAccountFromFile();

        // ��ʼ��΢��ҳ������
        /*
        * �����ݿ���ȡ������ȡ�û�ID�������ʼ�û���˿ҳ���Url�����������ȡ����WeiboUrlQueue
		* */
        Utils.initializeWeiboUrl();

        // ��������worker�߳�
        for (int i = 0; i < WORKER_NUM; i++) {
            new Thread(new UrlWeiboWorker()).start();
        }
    }

    private static void fetchAbnormalWeibo() {
        Log.info("\n\n\n===========================\n     Abnormal Weibo\n===========================\n");
        // ��ʼ���˺Ŷ���
        Utils.readAccountFromFile();

        // ��ʼ��΢��ҳ������
        Utils.initializeAbnormalWeiboUrl();

        // ��������worker�߳�
        for (int i = 0; i < WORKER_NUM; i++) {
            new Thread(new UrlAbnormalWeiboWorker()).start();
        }
    }

    private static void fetchComment() {
        Log.info("\n\n\n===========================\n     Fetch Comment\n===========================\n");
        // ��ʼ���˺Ŷ���
        Utils.readAccountFromFile();

        // ��ʼ������ҳ������
        Utils.initializeCommentUrl();

        // ��������worker�߳�
        for (int i = 0; i < WORKER_NUM; i++) {
            new Thread(new UrlCommentWorker()).start();
        }
    }

    private static void fetchRepost() {
        Log.info("\n\n\n===========================\n     Fetch Repost\n===========================\n");
        // ��ʼ���˺Ŷ���
        Utils.readAccountFromFile();

        // ��ʼ��ת��ҳ������
        Utils.initializeRepostUrl();

        // ��������worker�߳�
        for (int i = 0; i < WORKER_NUM; i++) {
            new Thread(new UrlRepostWorker()).start();
        }
    }

    private static void fetchFollowee() {
        Log.info("\n\n\n===========================\n     Fetch Followee\n===========================\n");
        // ��ʼ���˺Ŷ���
        Utils.readAccountFromFile();

        // ��ʼ����עҳ������
        UrlFollowWorker.CURRENT_LEVEL = Utils.initializeFollowUrl();

        // ��������worker�߳�
        for (int i = 0; i < WORKER_NUM; i++) {
            new Thread(new UrlFollowWorker()).start();
        }
    }
}
