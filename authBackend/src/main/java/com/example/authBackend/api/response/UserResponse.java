package com.example.authBackend.api.response;
import com.example.authBackend.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private List<UserDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
    private boolean lastPage;
    private String sortBy;
    private String sortOrder;
}
