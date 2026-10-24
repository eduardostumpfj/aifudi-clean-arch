package com.aifudi_clean_arch.backend.core.usecase.subrole;

import com.aifudi_clean_arch.backend.core.exception.ProtectedSubRoleException;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class DeleteSubRoleUseCase {
    private final SubRoleGateway subRoleGateway;

    public void deleteSubRole(UUID subrole_id) {
        var current = subRoleGateway.getById(subrole_id)
                .orElseThrow(() ->
                        new SubRoleNotFoundException(subrole_id));

        if(current.isMainOwner()){
            throw new ProtectedSubRoleException(subrole_id);
        }


       var isDeleted = subRoleGateway.delete(subrole_id);

       if(!isDeleted){
           throw new SubRoleNotFoundException(subrole_id);
       }
    }
}
