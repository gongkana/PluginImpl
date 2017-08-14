package com.nantian.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.os.Environment;
import android.util.Log;

/**
 * wrap android的log类，进行功能性扩展，关于日志的常量和属性等主要参考android.util.Log
 * 
 * <ol>
 * 扩展功能：
 * <li>日志写入文件，log.txt log1.txt，每个文件大小1M，超过大小进行日志回滚
 * <li>日志按定义的级别写入文件，只记录直接之上的日志，级别分级和android.util.Log一致
 * <li>日志打包导出成zip压缩文件
 * </ol>
 * 
 * @author y42579
 * 
 */
public final class HLog
{

    /**
     * The logging tag used by this class with android.util.Log.
     */
    protected static final String H_LOG_TAG = "HLOG";

    /** 开发者模式日志记录 */
    private static final int DEV_MODE = 1;

    /** 当前的使用的级别 */
  //private static int logLevel = Log.VERBOSE;

    private static int logLevel = Log.VERBOSE;

    /** sdcard 地址目录 */
    private static final String SD_Card = Environment.getExternalStorageDirectory().getPath();

    private static final String LOG_PATH_PRE = SD_Card + "/Nantian/Log/hid_log";

    private static final String LOG_PATH_SUF = ".txt";

    private static final String SYS_LOG_PATH = SD_Card + "/Nantian/Log/hid_sys.txt";

    private static final String DEV_LOG_PATH = SD_Card + "/Nantian/Log/hid_dev.txt";

    private static final String LogPath = SD_Card + "/Nantian/Log";

    /**
     * 压缩文件路径
     */
    private static final String LogFilePathZip = "/sdcard/log/LogInfoZip.zip";

    /** 文件最大容量 */
    public static final int FILE_MAX_SIZE = 262144;

    /** 最大压缩文件大小 */
    public static final int ZIP_FILE_MAX_SIZE = 524288;

    /** 压缩文件时的缓冲区 */
    private static final int BUFF_SIZE = 1024 * 512; // 512k Byte

    /** 行的分割符 */
    public static final String LINE_SPLIT = System.getProperty("line.seperator", "\n");

    /** 不记录日志的返回值 */
    public static final int NO_LOG = -1;

    /** 总的文件数 */
    private static final int totalFileNum = 10;

    /** 当前正在写的文件,从1开始 */
    private static int curIndex = 1;

    private HLog()
    {
    }

    /**
     * Send a {@link #VERBOSE} log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg)
    {
        if (isNeedLog(Log.VERBOSE))
        {
            writeFile(Log.VERBOSE, tag, msg, null);
            return Log.v(tag, msg);

        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr)
    {
        if (isNeedLog(Log.VERBOSE))
        {
            writeFile(Log.VERBOSE, tag, msg, tr);
            return Log.d(tag, msg, tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #DEBUG} log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg)
    {
        if (isNeedLog(Log.DEBUG))
        {
            writeFile(Log.DEBUG, tag, msg, null);
            return Log.d(tag, msg);
        }
        else
        {
            return NO_LOG;
        }
    }

    public static int d(String tag, int msg)
    {
        if (isNeedLog(Log.DEBUG))
        {
            writeFile(Log.DEBUG, tag, String.valueOf(msg), null);
            return Log.d(tag, String.valueOf(msg));
        }
        else
        {
            return NO_LOG;
        }
    }

    public static int d(String tag, boolean msg)
    {
        if (isNeedLog(Log.DEBUG))
        {
            writeFile(Log.DEBUG, tag, String.valueOf(msg), null);
            return Log.d(tag, String.valueOf(msg));
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr)
    {
        if (isNeedLog(Log.DEBUG))
        {
            writeFile(Log.DEBUG, tag, msg, tr);
            return Log.d(tag, msg, tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send an {@link #INFO} log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg)
    {
        if (isNeedLog(Log.INFO))
        {
            writeFile(Log.INFO, tag, msg, null);
            return Log.i(tag, msg);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr)
    {
        if (isNeedLog(Log.INFO))
        {
            writeFile(Log.INFO, tag, msg, tr);
            return Log.i(tag, msg, tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #WARN} log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg)
    {
        if (isNeedLog(Log.WARN))
        {
            writeFile(Log.WARN, tag, msg, null);
            return Log.w(tag, msg);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr)
    {
        if (isNeedLog(Log.WARN))
        {
            writeFile(Log.WARN, tag, msg, tr);
            return Log.w(tag, msg, tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     * 
     * The default level of any tag is set to INFO. This means that any level above and including INFO will be logged.
     * Before you make any calls to a logging method you should check to see if your tag should be logged. You can
     * change the default level by setting a system property: 'setprop log.tag.&lt;YOUR_LOG_TAG> &lt;LEVEL>' Where level
     * is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPPRESS will turn off all logging for your
     * tag. You can also create a local.prop file that with the following in it: 'log.tag.&lt;YOUR_LOG_TAG>=&lt;LEVEL>'
     * and place that in /data/local.prop.
     * 
     * @param tag The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     * @throws IllegalArgumentException is thrown if the tag.length() > 23.
     */
    public static boolean isLoggable(String tag, int level)
    {
        return Log.isLoggable(tag, level);
    }

    /*
     * Send a {@link #WARN} log message and log the exception.
     * 
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     * log call occurs.
     * 
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr)
    {
        if (isNeedLog(Log.WARN))
        {
            writeFile(Log.WARN, tag, null, tr);
            return Log.w(tag, tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send an {@link #ERROR} log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg)
    {
        if (isNeedLog(Log.ERROR))
        {
            writeFile(Log.ERROR, tag, msg, null);
            return Log.e(tag, msg);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send an {@link #ERROR} log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param tr An exception to log
     */
    public static int e(String tag, Throwable tr)
    {
        if (isNeedLog(Log.ERROR))
        {
            String errMsg = getStackTraceString(tr);
            writeFile(Log.ERROR, tag, errMsg, null);
            return Log.e(tag, errMsg);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * 开发者模式，主要分析哪些邮箱处理占用了太多的时间
     * 
     * @param tag
     * @param msg
     * @return
     */
    public static int x(String tag, String msg)
    {
        if (isNeedLog(DEV_MODE))
        {
            writeFile(DEV_MODE, tag, msg, null);
            return Log.e(tag, msg);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr)
    {
        if (isNeedLog(Log.ERROR))
        {
            writeFile(Log.ERROR, tag, msg, tr);
            return Log.e(tag, msg, tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The error will always be logged at level
     * ASSERT with the call stack. Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated immediately with an error dialog.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static int wtf(String tag, String msg)
    {
        if (isNeedLog(Log.ASSERT))
        {
            writeFile(Log.ASSERT, tag, msg, null);
            return Log.w(tag, msg, null);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to {@link #wtf(String, String)},
     * with an exception to log.
     * @param tag Used to identify the source of a log message.
     * @param tr An exception to log.
     */
    public static int wtf(String tag, Throwable tr)
    {
        if (isNeedLog(Log.ASSERT))
        {
            writeFile(Log.ASSERT, tag, tr.getMessage(), tr);
            return Log.w(tag, tr.getMessage(), tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to {@link #wtf(String, Throwable)}
     * , with a message as well.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log. May be null.
     */
    public static int wtf(String tag, String msg, Throwable tr)
    {
        if (isNeedLog(Log.ASSERT))
        {
            writeFile(Log.ASSERT, tag, msg, tr);
            return Log.w(tag, tr.getMessage(), tr);
        }
        else
        {
            return NO_LOG;
        }
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr)
    {
        return Log.getStackTraceString(tr);
    }

    /**
     * Low-level logging call.
     * @param priority The priority/type of this log message
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
     *        log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg)
    {
        return Log.println(priority, tag, msg);
    }

    
    public static int printLog(int priority, String tag, String msg, Throwable tr){
    	if (isNeedLog(priority)){
    		writeFile(priority, tag, msg, tr);
    		return println(priority, tag, msg + '\n' + getStackTraceString(tr));
    	}
    	return NO_LOG;
    }
    /**
     * 将日志信息写入自定义的文件
     * @param priority 日志级别
     * @param tag 日志的tag
     * @param msg 详细的日志信息
     * @param tr 异常
     */
    private static void writeFile(int priority, String tag, String msg, Throwable tr)
    {
        // TODO 写文件时多线程的问题需要考虑
        if (tag == null || tag.length() == 0)
        {
            tag = "[null] ";
        }
        else
        {
            tag = "[" + tag + "] ";
        }

        StringBuilder fmsg = new StringBuilder(getCurTime());
        fmsg.append(" [").append(Thread.currentThread().getName()).append("]");
        fmsg.append(tag).append(getLevelStr(priority)).append(" ");
        if (null != msg)
        {
            fmsg.append(msg).append(LINE_SPLIT);
        }

        if (null != tr)
        {
            fmsg.append(getStackTraceString(tr)).append(LINE_SPLIT);
        }

        FileWriter fw = null;
        File file = null;
        try
        {
            file = new File(getLogFile(priority));
            fw = new FileWriter(file, true);
            fw.append(fmsg);
        }
        catch (IOException e)
        {
            Log.e(H_LOG_TAG, "Write log into file falied.", e);
        }
        finally
        {
            try
            {
                if (null != fw)
                    fw.close();
            }
            catch (IOException e)
            {
                Log.e(H_LOG_TAG, "Close file handler falied.", e);
            }
        }

    }

    /**
     * 是否需要进行log打印和记日志
     * @param priority 日志的优先级
     * @return
     */
    private static boolean isNeedLog(int priority)
    {
        return priority >= logLevel;
    }

   
    /**
     * 获取非开发者模式的日志
     * @return
     */
    public static String getLogFile()
    {
        return getLogFile(Log.VERBOSE);
    }

    /**
     * 返回当前要记录的文件名
     * @return
     */
    public static String getLogFile(int priority)
    {
    	// 开发者模式日志
        if (priority <= DEV_MODE)
        {
            return DEV_LOG_PATH;
        }

        // 如果当前日志是第一个，则判断最合适的记录文件是多少
        boolean isFixFirst = false;
        if (1 == curIndex)
        {
            long lastModified = -1;
            int lastModifiedIndex = 1;
            // 日志文件
            for (int i = 1; i <= totalFileNum; i++)
            {
                File file = new File(getLogFileByIndex(i));
                if (!file.exists())
                {
                    curIndex = i;
                    break;
                }
                else if (file.lastModified() < lastModified || -1 == lastModified)
                {
                    // 文件存在,且修改日期小于最小的日期，则记下来
                    lastModified = file.lastModified();
                    lastModifiedIndex = i;
                }
            }

            // 得出最终的index:没有空文件且最早修改的文件
            if (1 == curIndex && 1 != lastModifiedIndex)
            {
                curIndex = lastModifiedIndex - 1; // 后面会增1
            }
            else
            {
                isFixFirst = (1 == curIndex) ? true : false;
            }
        }

        String logName = getLogFileByIndex(curIndex);
        File file = new File(logName);
        if (file.length() > FILE_MAX_SIZE)
        {
            if (isFixFirst)
            {
                curIndex = 1;
            }
            else
            {
                curIndex = (curIndex >= totalFileNum) ? 1 : (curIndex + 1);
            }
            String nextFileName = getLogFileByIndex(curIndex);
            File nextFile = new File(nextFileName);
            if (nextFile.exists())
            {
                nextFile.delete();
            }
            return nextFileName;
        }
        else
        {
            return logName;
        }
    }

    /**
     * 获取日志文件名
     * @param index 记录日志的序号
     * @return
     */
    private static String getLogFileByIndex(int index)
    {
        return LOG_PATH_PRE + index + LOG_PATH_SUF;
    }

    /**
     * 获取日志对应级别的字符串，用于自定义的文件记录
     * @param priority 日志优先级
     * @return 优先级对应的字符标志
     */
    private static String getLevelStr(int priority)
    {
        switch (priority)
        {
            case DEV_MODE:
                return "[DEV]";
            case Log.VERBOSE:
                return "[VERBOSE]";
            case Log.DEBUG:
                return "[DEBUG]";
            case Log.INFO:
                return "[INFO]";
            case Log.WARN:
                return "[WARN]";
            case Log.ERROR:
                return "[ERROR]";
            case Log.ASSERT:
                return "[ASSERT]";

            default:
                return "[UNKNOW]";
        }
    }

    /**
     * 获取特定格式的时间
     */
    private static String getCurTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd/ HH:mm:ss");
        return "[" + formatter.format(System.currentTimeMillis()) + "] ";
    }

    /**
     * 批量压缩文件（夹）
     * @param resFileList 要压缩的文件（夹）列表
     * @param zipFile 生成的压缩文件
     * @throws IOException 当压缩过程出错时抛出
     */
    private static void zipFiles(List<File> resFileList, File zipFile)
    {
        ZipOutputStream zipout = null;
        try
        {
            zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
            for (File resFile : resFileList)
            {
                zipFile(resFile, zipout, LogPath);
            }

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (zipout != null)
                {
                    zipout.close();
                }
            }
            catch (IOException e)
            {
                HLog.e("LogInfo.zipFiles", e);
            }
        }
    }

    /**
     * 压缩文件
     * @param resFile 需要压缩的文件（夹）
     * @param zipout 压缩的目的文件
     * @param rootpath 压缩的文件路径
     * @throws FileNotFoundException 找不到文件时抛出
     * @throws IOException 当压缩过程出错时抛出
     */
    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath)
    {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName();
        if (resFile.isDirectory())
        {
            File[] fileList = resFile.listFiles();
            if (null == fileList)
            {
                return;
            }

            for (File file : fileList)
            {
                zipFile(file, zipout, rootpath);
            }
        }
        else
        {
            byte buffer[] = new byte[BUFF_SIZE];
            BufferedInputStream in = null;
            try
            {
                in = new BufferedInputStream(new FileInputStream(resFile), BUFF_SIZE);
                zipout.putNextEntry(new ZipEntry(rootpath));
                int realLength;
                while ((realLength = in.read(buffer)) != -1)
                {
                    zipout.write(buffer, 0, realLength);
                }

                zipout.flush();
                zipout.closeEntry();
            }
            catch (FileNotFoundException e)
            {
                HLog.e("LogInfo.zipFile", e);
            }
            catch (IOException e)
            {
                HLog.e("LogInfo.zipFile", e);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        HLog.e("LogInfo.zipFile", e);
                    }
                }
            }
        }
    }

    /**
     * 读取当前正在写的日志文件，以字符串返回
     * @return
     */
    public static String readLog()
    {
        BufferedReader br = null;
        File file = new File(getLogFile());

        // 读取文件内容放在字符串里面
        try
        {
            StringBuilder sb = new StringBuilder();
            if (file.exists())
            {
                sb.append("-------------" + file.getCanonicalPath());
                br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while (line != null)
                {
                    sb.append(line + LINE_SPLIT);
                    line = br.readLine();
                }
            }
            if (sb.length() >= 0)
            {
                return sb.toString();
            }
            return "";
        }
        catch (IOException e)
        {
            HLog.e(H_LOG_TAG, e);
            return "";
        }
        finally
        {
            try
            {
                if (br != null)
                {
                    br.close();
                }
            }
            catch (IOException e)
            {
                HLog.e(H_LOG_TAG, e);
            }
        }
    }

    /**
     * 删除log文件
     */
    public static void delFile()
    {
        // 日志文件
        for (int i = 1; i <= totalFileNum; i++)
        {
            File file = new File(getLogFileByIndex(i));
            if (file.exists())
            {
                file.delete();
            }
        }
    }

    public static int getLogLevel()
    {
        return logLevel;
    }

    public static void setLogLevel(int logLevel)
    {
        /*
         * public static final int VERBOSE = 2;
         * 
         * public static final int DEBUG = 3;
         * 
         * public static final int INFO = 4;
         * 
         * public static final int WARN = 5;
         * 
         * public static final int ERROR = 6;
         * 
         * public static final int ASSERT = 7;
         */
        HLog.logLevel = logLevel;
    }

}
