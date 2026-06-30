package fr.lebarapp.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
    @NotNull(message = "L'identifiant de la table ne peut pas être nul")
    Long tableId,
    String customerName,
    @NotEmpty(message = "Au moins un article doit être commandé")
    @Valid
    List<OrderLineRequest> items
) {}
