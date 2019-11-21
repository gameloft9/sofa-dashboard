import me.izhong.jobs.agent.util.FtpUtil

try {

    //查询文件演示
    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));
    def host = "ftp://172.30.251.92:21"
    def user = "dev_nuis"
    def pass = "x"
    List<String> ftpFiles = FtpUtil.listFtpFilesInDir(
            host
            ,user,pass,"battest/68Temp/");

    String fn = "";
    ftpFiles.each {
        def fileName = it
        fn = fileName
        println "文件名字:${fileName}"
    };

    //下载文件演示
    File tmpFile = File.createTempFile("you",".zip");
    tmpFile.deleteOnExit()
    FtpUtil.getFileFromFtp(host,user,pass,"battest/68Temp/",fn,tmpFile);
    println(">>> tmpFile path:${tmpFile.getAbsolutePath()}")

    //上传文件演示
    FtpUtil.putFileToFtp(host,user,pass,"battest/68Temp/", "destFileNametxt", tmpFile)

    Thread.sleep(60*1000)
    //删除演示
    FtpUtil.deleteFileFromFtp(host,user,pass,"battest/68Temp/", "destFileNametxt")
    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}


