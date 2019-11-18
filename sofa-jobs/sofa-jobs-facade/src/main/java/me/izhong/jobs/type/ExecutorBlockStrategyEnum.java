package me.izhong.jobs.type;

public enum ExecutorBlockStrategyEnum {

    SERIAL_EXECUTION("顺序"),
    CONCURRENT_EXECUTION("并行"),
    DISCARD_LATER("丢弃"),
    COVER_EARLY("覆盖");

    private String title;
    private ExecutorBlockStrategyEnum (String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorBlockStrategyEnum item:ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}