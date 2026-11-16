package com.curtainsmall.expr2.model;

/**
 * 任务实体，对应 SQLite 表 task 的一行。
 */
public class Task {

    /** 待办 */
    public static final int STATUS_TODO = 0;
    /** 进行中 */
    public static final int STATUS_DOING = 1;
    /** 已完成 */
    public static final int STATUS_DONE = 2;

    public long id;
    /** 任务类别，例如 学习 / 工作 / 生活 */
    public String category = "";
    /** 任务标题 */
    public String title = "";
    /** 任务内容 */
    public String content = "";
    /** 任务时间（毫秒时间戳） */
    public long dueTime;
    /** 任务状态：STATUS_TODO / STATUS_DOING / STATUS_DONE */
    public int status = STATUS_TODO;
    /** 归属用户，用于“查看个人的所有任务” */
    public String owner = "";

    public boolean isDone() {
        return status == STATUS_DONE;
    }
}
