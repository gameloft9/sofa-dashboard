import com.gmongo.GMongo
import groovy.sql.Sql
import me.izhong.jobs.agent.util.ContextUtil


try {

    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));

    String run_envs = ContextUtil.getRunEnv();
    println("run_envs:${run_envs}")
    println("isProd:${ContextUtil.isProd()}")

    def url = "jdbc:oracle:thin:@172.30.251.120:1521:nuisdb"
    def user = "batnuis"
    def pass = "x"
    def sql = Sql.newInstance( url ,user, pass)


    def mongo = new GMongo("172.30.251.33:27017");
    def db = mongo.getDB("nuis")
    def collectionName = db.getCollectionNames();
    println("collectionName: ${collectionName}")
    def coll = db.getCollection("pre_match_info")

    def data = coll.find([:])
    println("data: ${data}")
    println("数量: ${coll.find().count()}")

    coll.find().limit(10).skip(0).each {
        // println "\t-- ${it}"
    }

    show()

    //def stmt =  "SELECT app.inst_id, app.mchnt_id from t_mchnt_app app where app.APPTYPE_ID = ? and app.mapp_no = ? and app.INNER_MCHNT_FLAG = '1' ";
    def stmt =  "SELECT app.inst_id, app.mchnt_id from t_mchnt_app app where app.INNER_MCHNT_FLAG = '1' and rownum <= 30";

    def count = 0;
    sql.eachRow(stmt, [],{ row ->
        String inst_id = row.getAt("inst_id");
        String mchnt_id = row.getAt("mchnt_id");

//        TIMESTAMP create_time = row.getAt("create_time");
//        TIMESTAMP update_time = row.getAt("update_time");
        println("inst_id:${inst_id} mchnt_id:${mchnt_id} ")
        count++;
        if (count % 100 == 0) {
            log.info("Count={}", count);
        }
    });

    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
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


