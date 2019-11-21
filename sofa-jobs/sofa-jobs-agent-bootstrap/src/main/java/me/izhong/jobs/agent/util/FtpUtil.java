package me.izhong.jobs.agent.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

@Slf4j
public class FtpUtil {
	/**
	 * 发送文件到ftp服务器
	 * 
	 * @param host
	 *            主机名，格式为<ftp|sftp>://hostname:port
	 * @param user
	 *            用户名
	 * @param pass
	 *            密码
	 * @param destDir
	 *            文件上传目录
	 * @param destFile
	 *            文件名
	 * @param srcFile
	 *            源文件
	 * @throws Exception
	 */
	public static void putFileToFtp(String host, String user, String pass,
			String destDir, String destFile, File srcFile) throws Exception {
		if (host.startsWith("sftp")) {
			putSftp(host, user, pass, destDir, destFile, srcFile);
		} else if (host.startsWith("ftp")) {
			putFtp(host, user, pass, destDir, destFile, srcFile);
		}
	}

	public static boolean getFileFromFtp(String host, String user, String pass,
			String srcDir, String srcFile, File destFile)
	{
		try
		{
			if (host.startsWith("sftp"))
			{
				getSftp(host, user, pass, srcDir, srcFile, destFile);
			} else if (host.startsWith("ftp"))
			{
				getFtp(host, user, pass, srcDir, srcFile, destFile);
			}
		}
		catch (Exception e)
		{
			log.error("下载失败",e);
			return false;
		}
		return true;
	}

	public static List<String> listFtpFilesInDir(String host, String user,
			String pass, String destDir) throws Exception {
		if (host.startsWith("sftp")) {
			return listSftpFiles(host, user, pass, destDir);
		} else if (host.startsWith("ftp")) {
			return listFtpFiles(host, user, pass, destDir);
		} else {
			return new ArrayList<String>();
		}
	}

	public static void deleteFileFromFtp(String host, String user, String pass,
									String destDir, String destFile) throws Exception {
		if (host.startsWith("sftp")) {
			deleteSftp(host, user, pass, destDir, destFile);
		} else if (host.startsWith("ftp")) {
			deleteFtp(host, user, pass, destDir, destFile);
		}
	}

	private static void getFtp(String host, String user, String pass,
			String srcDir, String srcFile, File destFile) throws Exception {
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		FTPClient ftp = new FTPClient();
		ftp.connect(s2[0], Integer.parseInt(s2[1]));

		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new Exception("FTP server refused connection.");
		}

		log.info("login");
		if (!ftp.login(user, pass)) {
			throw new Exception("FTP login fail.");
		}

		ftp.setFileType(FTP.BINARY_FILE_TYPE);
		ftp.setBufferSize(1024);
		log.info("cd");
		ftp.changeWorkingDirectory(srcDir);

		// 设置被动模式
		ftp.enterLocalPassiveMode();

		@Cleanup
		FileOutputStream fos = new FileOutputStream(destFile);
		log.info("get");
		boolean result = ftp.retrieveFile(srcFile, fos);

		log.info("disconnect");
		ftp.disconnect();

		if (result == false)
			throw new Exception("Cannot get remote file");
	}

	private static void getSftp(String host, String user, String pass,
			String srcDir, String srcFile, File destFile) throws Exception {
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		JSch jsch = new JSch();

		Session session = jsch.getSession(user, s2[0], Integer.parseInt(s2[1]));

		session.setPassword(pass);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		session.setConfig(sshConfig);

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		session.connect();

		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;

		log.info("cd");
		c.cd(srcDir);
		@Cleanup
		FileOutputStream fos = new FileOutputStream(destFile);
		log.info("get");
		c.get(srcFile, fos);

		log.info("disconnect");
		c.disconnect();
		session.disconnect();
	}

	private static void putSftp(String host, String user, String pass,
			String destDir, String destFile, File srcFile) throws Exception {
		log.info("u:{},p:{}",user,pass);
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		JSch jsch = new JSch();
		Session session = jsch.getSession(user, s2[0], Integer.parseInt(s2[1]));

		session.setPassword(pass);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		session.setConfig(sshConfig);

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		session.connect(1500);

		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;

		try{
			Vector content = c.ls(destDir);
			if(content == null) {
				log.info("mkdir");
				c.mkdir(destDir);
			}
		} catch (Exception e) {
			log.info("mkdir");
			c.mkdir(destDir);
		}

		log.info("cd");
		c.cd(destDir);
		@Cleanup
		FileInputStream fis = new FileInputStream(srcFile);
		@Cleanup
		BufferedInputStream bfi = new BufferedInputStream(fis);
		log.info("put");
		c.put(bfi, destFile);

		log.info("disconnect");
		c.disconnect();
		session.disconnect();
	}

	private static void putFtp(String host, String user, String pass,
			String destDir, String destFile, File srcFile) throws Exception {
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		FTPClient ftp = new FTPClient();
		ftp.connect(s2[0], Integer.parseInt(s2[1]));

		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new Exception("FTP server refused connection.");
		}

		log.info("login");
		if (!ftp.login(user, pass)) {
			throw new Exception("FTP login fail.");
		}

		ftp.setBufferSize(1024);
		ftp.setFileType(FTP.BINARY_FILE_TYPE);

		log.info("cd");
		if (!ftp.changeWorkingDirectory(destDir)) {
			log.info("mkdir");
			if(destDir.length() > 0 && destDir.indexOf("/") > 0){
				String[] temp = destDir.split("/");
				for(String dd : temp){
					if(ftp.changeWorkingDirectory(destDir)) {
						break;
					} else {
						if(!ftp.changeWorkingDirectory(dd)) {
							log.info("mkdir:{}",dd);
							ftp.makeDirectory(dd);
							if (!ftp.changeWorkingDirectory(dd)) {
								throw new Exception("FTP mkdir fail. des:" + destDir);
							}
						}
					}
				}
			}
		}

		// 设置被动模式
		ftp.enterLocalPassiveMode();

		@Cleanup
		FileInputStream fis = new FileInputStream(srcFile);
		@Cleanup
		BufferedInputStream bfi = new BufferedInputStream(fis);
		log.info("put");
		ftp.storeFile(destFile, bfi);

		log.info("disconnect");
		ftp.disconnect();
	}

	private static List<String> listFtpFiles(String host, String user,
			String pass, String destDir) throws Exception {
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		FTPClient ftp = new FTPClient();
		ftp.connect(s2[0], Integer.parseInt(s2[1]));

		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new Exception("FTP server refused connection.");
		}

		log.info("login");
		if (!ftp.login(user, pass)) {
			throw new Exception("FTP login fail.");
		}

		ftp.setFileType(FTP.BINARY_FILE_TYPE);

		log.info("cd");
		ftp.changeWorkingDirectory(destDir);

		// 设置被动模式
		ftp.enterLocalPassiveMode();

		log.info("ls");
		String[] names = ftp.listNames();
		List<String> list = new ArrayList<String>();
		if(names != null)
			for (String name : names) {
				if (name != null) {
					list.add(name);
				}
			}

		log.info("disconnect");
		ftp.disconnect();

		return list;
	}

	private static List<String> listSftpFiles(String host, String user,
			String pass, String destDir) throws Exception {
		log.info("ftp:{},user:{},p:{}",host,user,pass);
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		JSch jsch = new JSch();
		Session session = jsch.getSession(user, s2[0], Integer.parseInt(s2[1]));

		session.setPassword(pass);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		sshConfig.put("userauth.gssapi-with-mic","no");
		session.setConfig(sshConfig);
		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		session.connect(1500);

		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;

		log.info("ls");
		Vector v = c.ls(destDir);
		List<String> list = new ArrayList<String>(v.size());
		for (Object o : v) {
			String s = o.toString();
			String[] parts = s.split(" ");
			list.add(parts[parts.length - 1]);
		}

		log.info("disconnect");
		c.disconnect();
		session.disconnect();

		return list;
	}

	private static void deleteFtp(String host, String user, String pass,
							   String destDir, String destFile) throws Exception {
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		FTPClient ftp = new FTPClient();
		ftp.connect(s2[0], Integer.parseInt(s2[1]));

		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new Exception("FTP server refused connection.");
		}

		log.info("login");
		if (!ftp.login(user, pass)) {
			throw new Exception("FTP login fail.");
		}

		ftp.setFileType(FTP.BINARY_FILE_TYPE);
		log.info("cd:{}",destDir);
		ftp.changeWorkingDirectory(destDir);

		// 设置被动模式
		ftp.enterLocalPassiveMode();

		log.info("delete:{}",destFile);
		boolean result = ftp.deleteFile(destFile);

		log.info("disconnect");
		ftp.disconnect();

		if (result == false)
			throw new Exception("Cannot get remote file");
	}

	private static void deleteSftp(String host, String user, String pass,
								String destDir, String destFile) throws Exception {
		String s1[] = host.split("://");
		String s2[] = s1[1].split(":");

		JSch jsch = new JSch();

		Session session = jsch.getSession(user, s2[0], Integer.parseInt(s2[1]));

		session.setPassword(pass);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		session.setConfig(sshConfig);

		log.info("connect {}:{}", s2[0], Integer.parseInt(s2[1]));
		session.connect();

		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;

		log.info("cd:{}",destDir);
		c.cd(destDir);
		log.info("delete :{}",destFile);
		c.rm(destFile);

		log.info("disconnect");
		c.disconnect();
		session.disconnect();
	}

	static public void main(String args[]) throws Exception {
		List<String> files = listFtpFilesInDir("ftp://mirrors.kernel.org:21",
				"anonymous", "adf", "centos");
		for (String file : files) {
			System.out.println(file);
		}
	}

}
