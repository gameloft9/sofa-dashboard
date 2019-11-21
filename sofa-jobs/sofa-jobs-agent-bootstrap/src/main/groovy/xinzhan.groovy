import groovy.sql.Sql
import me.izhong.jobs.agent.util.FtpUtil
import me.izhong.jobs.agent.util.JobStatsUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

Logger  log = LoggerFactory.getLogger("xxx");

try {


    //HashMap<String,String> params = params;
    //log.info("参数是:{} {}",params,params.get("xx"));

    int[] busiTypes = [1,53,68,9,9999];

    SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
//    String blocId = UisbatchConfig.getPropertiesValue("uis.dbs.groupcode");
    String blocId = "00010015250000";
    Calendar now = Calendar.getInstance();
    //清算日期为当前时间的前一天
    now.add(Calendar.DAY_OF_MONTH, -1);
    String settleDate = formater.format(now.getTime());
    settleDate = "20190515"
    String doneKey ="xinzhan_bill_"+settleDate;
//    value1 = JobStatsUtil.getValue1(doneKey)
//    if(value1 != null) {
//        return ;
//    }

    def url = "jdbc:oracle:thin:@144.131.254.240:1521:nbcsora"
    def user = "uat_nuis"
    def pass = "uat_nuis_P2018_Test"
    def sql = Sql.newInstance( url ,user, pass)

    def ftphost = "ftp://172.30.251.92:21"
    def ftpuser = "dev_nuis"
    def ftppass = "CDE#4rfv"
    def ftppath = "battest/68Temp/"+blocId.substring(0,6)+"/bloc/"+blocId+"/upload/";
    //def ftppath = "battest/68Temp/upload/";

    BufferedWriter writer = null;
    FileOutputStream fos = null;
    File file =null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    long start = System.currentTimeMillis();
    try{

        file=File.createTempFile("xinzhan","DBSPOS_"+settleDate+".txt");
        file.deleteOnExit();

        log.info("ftp_path:"+ftppath+"blocId "+blocId+" DbsBankCreatefile start");

        fos = new FileOutputStream(file);
        writer = new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));

        writer.write("H|"+sdf.format(new Date()));
        writer.newLine();

        long tranNum = 0;   //总笔数
        BigDecimal tranAmt = new BigDecimal(0);  //总金额
        Map<Integer,Long> sMap = new HashMap<Integer,Long>();

        Calendar calendar = Calendar.getInstance();
        Date date = sdf.parse(settleDate);
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        Date date1 = calendar.getTime();

        log.info(settleDate+" "+blocId+" DbsBank has "+ tranNum +" records to make file");
        def selectTrans ='''
SELECT /*+ use_nl(tbl_bke_ncard_dtl) ordered index(tbl_bke_ncard_dtl,IX_SETT_NCARD_DTL$MER_ID#SETT_)*/
 tbl_bke_ncard_dtl.MER_NO as merNo,
 a.Mchnt_Name as merName,
 tbl_bke_ncard_dtl.SETT_DATE as settleDate,
 to_char(tbl_bke_ncard_dtl.Trx_Dtm, 'yyyymmdd') as tranDate,
 tbl_bke_ncard_dtl.LOC_TRX_TM as tranTime,
 tbl_bke_ncard_dtl.BUSI_TYPE_ID as busiType,
 tbl_bke_ncard_dtl.IN_CARD_TERM_ID as termNo,
 tbl_bke_ncard_dtl.TRX_AMT as trxAmt,
 tbl_bke_ncard_dtl.SETT_ACCT as setAcct,
 tbl_bke_ncard_dtl.TRX_DISC_AT as discAmt,
 tbl_bke_ncard_dtl.SYS_TRX_NO as sysNo,
 tbl_bke_trx_type.trx_type_name as tranType,
 tbl_bke_ncard_dtl.RETRI_REF_NO as refNo,
 tbl_bke_ncard_dtl.PRI_ACCT_NO as cardNo,
 nvl(bank.bank_name, tbl_bke_ncard_dtl.iss_ins_id_cd) as issInsIdCd,
 u.mapp_setaccntno_genc as setaccntnoGenc,
 u.idx_mapp_setaccntno_genc as idxSetaccntnoGenc,
 tbl_bke_ncard_dtl.ADDI_ATTR1 as ADDI_ATTR1,
 tbl_bke_ncard_dtl.ADDI_ATTR2 as ADDI_ATTR2,
 tbl_bke_ncard_dtl.ADDI_ATTR8 as addi_ATTR8,
 get_cardtype_new(TBL_BKE_NCARD_DTL.pri_acct_no,
                  TBL_BKE_NCARD_DTL.debit_credit_flag) as cardType,
 tbl_bke_ncard_dtl.Mer_Id as merId
  FROM (SELECT i.MCHNT_ID, i.Mchnt_Name
          FROM t_merchant i
         where i.mchnt_id in
               (select j.mer_id
                  from tbl_bke_group_rel_mer j
                 where j.group_code in
                       (SELECT g.group_code
                          FROM tbl_bke_group_info g
                         START WITH g.group_code = ?
                        CONNECT BY PRIOR g.group_code = g.par_group_code))) a,
       tbl_bke_ncard_dtl,
       tbl_bke_trx_type,
       tbl_bke_bank_info bank,
       t_mchnt_app u
 WHERE tbl_bke_ncard_dtl.SETT_DATE = ?
   AND tbl_bke_ncard_dtl.MER_ID = a.MCHNT_ID
   and tbl_bke_ncard_dtl.BUSI_TYPE_ID in (?,?,?,?,?)
   AND tbl_bke_ncard_dtl.trans_id = tbl_bke_trx_type.trx_type_code(+)
   and substr(tbl_bke_ncard_dtl.iss_ins_id_cd, 0, 4) = bank.bank_code(+)
   and tbl_bke_ncard_dtl.MER_ID = u.mchnt_id(+)
   and decode(tbl_bke_ncard_dtl.BUSI_TYPE_ID,
              9999,
              1,
              tbl_bke_ncard_dtl.BUSI_TYPE_ID) = u.apptype_id(+)
  	'''

        sql.eachRow(selectTrans, [blocId,settleDate,*busiTypes], { row ->
            String merNo = row["merNo"]
            String merName = row["merName"]
            println("merNo:${merNo} merName:${merName}")
            writer.write(merNo+","+merName);
            writer.newLine();
        })

        writer.flush();
        log.info("ftp"+ftppath+"blocId "+blocId+" DbsBankCreatefile Finish");

        writer.write("T|"+tranNum);
        writer.newLine();
        writer.flush();
        //加密文件

        //上送文件
        FtpUtil.putFileToFtp(ftphost,ftpuser,ftppass,ftppath,file.getName(),file);
        // 上送done文件
        File doneFile = File.createTempFile("ftp","random");
        doneFile.deleteOnExit();
        FtpUtil.putFileToFtp(ftphost,ftpuser,ftppass,ftppath,file.getName()+".done",doneFile);

        //记录这个文件已经处理
        long end = System.currentTimeMillis();
        JobStatsUtil.insertOrUpdate(doneKey,"对账单_星展","耗时"+(end-start)/1000+"秒");
        //让这个定时任务明天再跑
    }catch(Exception e){
        throw e;
    } finally{
        try{
            if(writer!=null){
                writer.close();
            }
            if(fos != null){
                fos.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}


