package link.alab.WebfluxSecurity.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationRequest implements Serializable {

    private static final long serialVersionUID = -6986746375915710855L;

    private String username;


    private String password;

}
