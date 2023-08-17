package com.example.hioa.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SearchApprovalBpmnForm {
    @NotBlank(message = "instanceId不能为空")
    private String instanceId;

}
