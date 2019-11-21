import com.chinaums.GM.SM4Util
import com.gmongo.GMongo
import groovy.sql.Sql
import me.izhong.jobs.agent.util.ContextUtil
import me.izhong.jobs.agent.util.JobStatsUtil
import oracle.sql.TIMESTAMP
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils

import java.sql.Timestamp
import java.text.SimpleDateFormat


try {

    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));

    String run_envs = ContextUtil.getRunEnv();
    println("run_envs:${run_envs}")
    println("isProd:${ContextUtil.isProd()}")

    //bmst表 papp,papp

    def bmsUrl ="jdbc:oracle:thin:@144.131.254.240:1521:nbcsora"
    def bmsUser = "papp"
    def bmsPass= "x"
    def url = "jdbc:oracle:thin:@172.30.251.120:1521:nuisdb"
    def user = "batnuis"
    def pass = "x"
    def bmsSql = Sql.newInstance( bmsUrl,bmsUser,bmsPass)
    def sql = Sql.newInstance( url ,user, pass)


    def merchantApplastSyncTime = getLastSyncTime("T_MCHNT_APP");
    if(merchantApplastSyncTime == null)
        merchantApplastSyncTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2001-01-01 00:00:00")

    //转换格式为string
    merchantApplastSyncTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(merchantApplastSyncTime)
    println("开始同步时间:${merchantApplastSyncTime}")

    def list = new ArrayList()
    def count = 0

//    def selectMchntApp = "select mchnt_id, mapp_no, inst_id, apptype_id, mapp_id, " +
//            "ACQUIRERINSTNO, UPDATEDATE,developingside_code," +
//            "FEE_TYPE, FEE_LEVEL from t_mchnt_app where " +
//            " UPDATEDATE >= to_date(?, 'yyyy-mm-dd hh24:mi:ss') and rownum < 10"
    def selectMchntApp = ''' select * from t_mchnt_app where 
UPDATEDATE >= to_date(?, 'yyyy-mm-dd hh24:mi:ss') and rownum < 10000  '''
    def selectMchnt = "select mchnt_briefname,mchnt_name,mcc_id, mchnt_status, province_id, city_id, country_id, office_address_genc, idx_office_address_genc, bussiness_accept, mchnt_mobile_genc, idx_mchnt_mobile_genc from t_merchant where mchnt_id=?"
    def selectInst = "select organize_code from t_organize where organize_id=?"

    Timestamp latestUpdateTime
    bmsSql.eachRow(selectMchntApp, [merchantApplastSyncTime], { row ->
        String mchnt_id = row["mchnt_id"]
        String mapp_no = row["mapp_no"]
        String inst_id = row["inst_id"]
        String apptype_id = row["apptype_id"]
        String mapp_id = row["mapp_id"]
        String acquirerinstno = row["ACQUIRERINSTNO"]
        String feeType = row["FEE_TYPE"]
        String feeLevel = row["FEE_LEVEL"]
        String mcc_id = null
        String mchnt_briefname = null
        String mchnt_name=null
        String organize_code = null
        String mchnt_status = null
        String province_id = null
        String city_id = null
        String country_id = null
        String office_address = null
        String T0Flag = null
        String CustomerType = null
        String certNo = null // 法人证件号
        String businessAccept = null
        String mobile = null
        String MerchantSource = null
        String developingside_code = row["developingside_code"]

        //log.info("开始处理商户: {} {}", mapp_no,apptype_id)
        bmsSql.eachRow(selectMchnt, [mchnt_id], {
            mchnt_briefname = it["mchnt_briefname"]
            mchnt_name=it["mchnt_name"]

            def dbMccId = it["mcc_id"]
            if (dbMccId) {
                mcc_id = String.format("%04d", dbMccId.toInteger())
            }
            mchnt_status = it["mchnt_status"]
            province_id = it["province_id"]
            city_id = it["city_id"]
            country_id = it["country_id"]
            def idx_office_address_genc = it["idx_office_address_genc"]
            def office_address_genc = it["office_address_genc"]
            //office_address = SM4ConverterUtil.convertToEntityAttribute(idx_office_address_genc, office_address_genc)
            businessAccept = it["bussiness_accept"]
            def idx_mchnt_mobile_genc = it["idx_mchnt_mobile_genc"]
            def mchnt_mobile_genc = it["mchnt_mobile_genc"]
            //SM4Util.sm4enc(keyIdx, plaintext)
            if(StringUtils.isNotBlank(mchnt_mobile_genc)) {
                mchnt_mobile_genc = SM4Util.sm4dec("A001", mchnt_mobile_genc)
                println("解密手机号:${mchnt_mobile_genc}")
            }
            //mobile = SM4ConverterUtil.convertToEntityAttribute(idx_mchnt_mobile_genc, mchnt_mobile_genc)
        })
        bmsSql.eachRow(selectInst, [inst_id], {
            organize_code = it["organize_code"]
        })

        Timestamp updateDate = row["UPDATEDATE"]
        if(latestUpdateTime == null || latestUpdateTime.before(updateDate))
            latestUpdateTime = updateDate

        if (mapp_no && mchnt_briefname&&mchnt_name && organize_code && mchnt_status) {
            list.add(["mid"          : mapp_no,
                      "merchant_name": mchnt_name,
                      "mchnt_briefname":mchnt_briefname,
                      "sub_inst"     : organize_code,
                      "app_type_id"  : apptype_id,
                      "mcc"          : mcc_id,
                      "mchnt_uuid"   : mchnt_id,
                      "active"       : mchnt_status,
                      "province_code": province_id,
                      "city_code"    : city_id,
                      "country_id"   : country_id,
                      "address"      : office_address,
                      "acq_code"     : acquirerinstno,
                      "T0_FLAG"      : T0Flag,
                      "CUSTOMER_TYPE": CustomerType,
                      "BUSINESS_ACCEPT": businessAccept,
                      "MOBILE"       : mobile,
                      "CERT_NO"      : certNo,
                      "DEVELOPINGSIDE_CODE" : developingside_code,
                      "MERCHANT_SOURCE" : MerchantSource,
                      "updateDate" : updateDate
                     ])
            count++
        }
        if (list.size() == 100) {
            insertMchntAppIntoNetpay(list)
        }
    })
    insertMchntAppIntoNetpay(list)
    if(latestUpdateTime !=null)
        updateControlTable("T_MCHNT_APP", new Date(latestUpdateTime.getTime()))
    //主循环结束
    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}

void insertMchntAppIntoNetpay(List merList) {
    merList.each {
        def m = it;
        println("要插入到网服商户:${m['mid']} ${m['merchant_name']}  ${m['updateDate']} ")
    }
    merList.clear()
}

private def getLastSyncTime(tableName) {
    String date = JobStatsUtil.getValue1("sync_control_"+tableName);
    if(date != null ) {
        log.info("getLastSyncTime: table {} last sync time {}", tableName, date)
        return  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
    }
    return null
}

private void updateControlTable(String tableName, Date date) {
    String s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    log.info("设置同步时间 tableName ={} date {}", tableName, s)
    JobStatsUtil.insertOrUpdate("sync_control_"+tableName,"sync_control",s)
}


void show(){
    println("show =============")
}

void updateMerAddTable(String mid, String field, String value, Sql sql) {
    def stmt = "update NPF_SUB_MER_ADD set ${field} = :value, update_time=sysdate where mid = :mid";
    sql.execute(stmt, [value: value, mid: mid]);
}

String getStatus(String mid, String field, Sql sql) {
    def stmt = "select ${field} from NPF_SUB_MER_ADD where mid = :mid";
    String status = null;
    sql.eachRow(stmt, [mid: mid]) { row ->
        status = row[field];
    };
    return status;
}


