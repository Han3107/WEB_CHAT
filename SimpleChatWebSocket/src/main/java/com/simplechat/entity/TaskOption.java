package com.simplechat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "task_options")
public class TaskOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer optionId;
    
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column(nullable = false, length = 255)
    private String optionText;
    
    @Column
    private Integer optionOrder;
    
    @Column
    private Integer voteCount = 0;

    public TaskOption() {}
    public TaskOption(Task task, String optionText) {
        this.task = task;
        this.optionText = optionText;
    }

    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public Integer getOptionOrder() { return optionOrder; }
    public void setOptionOrder(Integer optionOrder) { this.optionOrder = optionOrder; }
    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }
}
