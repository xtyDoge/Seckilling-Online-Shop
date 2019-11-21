package com.miaosha.service.model;

import lombok.Data;


@Data
public class EmailModel{

    private String receiver;

    private String subject;

    private String content;

    public EmailModel(String receiver,String subject,String content){
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
    }

}
