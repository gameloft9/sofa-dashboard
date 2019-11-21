import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.DateUtils
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator
import org.bouncycastle.openpgp.examples.ByteArrayHandler

import java.security.Security
import java.util.concurrent.TimeUnit


try {

    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));

    Date yesterday = DateUtils.addDays(new Date(), -1);
    println "脚本操作异常，错误信息:" + yesterday
    //TimeUnit.SECONDS.sleep(300)
    log.info("log 测试");
    log.info("log 测试2 {}",yesterday);


    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    String passPhrase = "DickBeck";
    char[] passArray = passPhrase.toCharArray();
    byte[] original = "Hello world".getBytes();

    System.out.println("Starting PGP test");
    byte[] encrypted = ByteArrayHandler.encrypt(original, passArray, "iway", PGPEncryptedDataGenerator.CAST5, true);

    System.out.println("\nencrypted data = '"+new String(encrypted)+"'");
    byte[] decrypted= ByteArrayHandler.decrypt(encrypted,passArray);

    System.out.println("\ndecrypted data = '"+new String(decrypted)+"'");

    encrypted = ByteArrayHandler.encrypt(original, passArray, "iway", PGPEncryptedDataGenerator.CAST5, true);

    System.out.println("\n22");
    System.out.println("\n ========= encrypted data = '"+new String(encrypted)+"'");

    decrypted= ByteArrayHandler.decrypt(encrypted, passArray);

    System.out.println("\ndecrypted data = '"+new String(decrypted)+"'");


//    encrypted = ByteArrayHandler.encrypt(original, passArray, "iway", PGPEncryptedDataGenerator.CAST5, false);
//
//    System.out.println("\nencrypted data = '"+new String(org.bouncycastle.util.encoders.Hex.encode(encrypted))+"'");
//    decrypted= ByteArrayHandler.decrypt(encrypted, passArray);
//
//    System.out.println("\ndecrypted data = '"+new String(decrypted)+"'");

    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}


