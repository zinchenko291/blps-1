package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderApiRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;

    @NotEmpty
    private List<@Valid CreateOrderItemApiRequest> items;
}
