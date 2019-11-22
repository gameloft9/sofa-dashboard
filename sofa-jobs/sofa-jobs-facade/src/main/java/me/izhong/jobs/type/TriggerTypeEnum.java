package me.izhong.jobs.type;

public enum TriggerTypeEnum {

    MANUAL("手动"),
    CRON("定时"),
    RETRY("重试"),
    CHILD("子任务"),
    SCRIPT("脚本"),
    CONTINUE("继续");

    TriggerTypeEnum(String title){
        this.title = title;
    }
    private String title;
    public String getTitle() {
        return title;
    }

}
