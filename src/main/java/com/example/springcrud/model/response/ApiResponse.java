package com.example.springcrud.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder   //need to study
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private String errorCode;

    private String message;

    private String token;

}
